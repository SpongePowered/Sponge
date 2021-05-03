/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.common.sql;

import static com.google.common.base.Preconditions.checkNotNull;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.benmanes.caffeine.cache.RemovalListener;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.sql.SqlManager;
import org.spongepowered.common.applaunch.config.core.SpongeConfigs;
import org.spongepowered.plugin.PluginContainer;

import java.io.Closeable;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.CompletionException;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

/**
 * Implementation of a SQL-using service.
 *
 * <p>This implementation does a few interesting things<br>
 *     - It's thread-safe
 *     - It allows applying additional driver-specific connection
 *     properties -- this allows us to do some light performance tuning in
 *     cases where we don't want to be as conservative as the driver developers
 *     - Caches DataSources. This cache is currently never cleared of stale entries
 *     -- if some plugin makes database connections to a ton of different databases
 *     we may want to implement this, but it is kinda unimportant.
 */
public final class SpongeSqlManager implements SqlManager, Closeable {

    static final Map<String, Properties> PROTOCOL_SPECIFIC_PROPS;
    static final Map<String, BiFunction<PluginContainer, String, String>> PATH_CANONICALIZERS;

    static {
        final ImmutableMap.Builder<String, Properties> build = ImmutableMap.builder();
        final Properties mySqlProps = new Properties();
        mySqlProps.setProperty("useConfigs",
                "maxPerformance"); // Config options based on http://assets.en.oreilly
                // .com/1/event/21/Connector_J%20Performance%20Gems%20Presentation.pdf
        build.put("com.mysql.jdbc.Driver", mySqlProps);
        build.put("org.mariadb.jdbc.Driver", mySqlProps);

        PROTOCOL_SPECIFIC_PROPS = build.build();
        PATH_CANONICALIZERS = ImmutableMap.of("h2", (plugin, orig) -> {
            // Bleh if only h2 had a better way of supplying a base directory... oh well...
            final org.h2.engine.ConnectionInfo h2Info = new org.h2.engine.ConnectionInfo(orig);
            if (!h2Info.isPersistent() || h2Info.isRemote()) {
                return orig;
            }
            if (orig.startsWith("file:")) {
                orig = orig.substring("file:".length());
            }
            final Path origPath = Paths.get(orig);
            if (origPath.isAbsolute()) {
                return origPath.toString();
            }

            return Sponge.configManager().pluginConfig(plugin).directory().resolve(orig).toAbsolutePath().toString();
        });
    }

    private @Nullable LoadingCache<ConnectionInfo, HikariDataSource> connectionCache;

    public SpongeSqlManager() {
        this.buildConnectionCache();
    }

    public void buildConnectionCache() {
        this.connectionCache = null;
        this.connectionCache = Caffeine.newBuilder()
                .removalListener((RemovalListener<ConnectionInfo, HikariDataSource>) ((key, value, cause) -> {
                    if (value != null) {
                        value.close();
                    }
                }))
                .build((key) -> {
                    final HikariConfig config = new HikariConfig();
                    config.setUsername(key.getUser());
                    config.setPassword(key.getPassword());
                    config.setDriverClassName(key.getDriverClassName());
                    // https://github.com/brettwooldridge/HikariCP/wiki/About-Pool-Sizing for info on pool sizing
                    config.setMaximumPoolSize((Runtime.getRuntime().availableProcessors() * 2) + 1);
                    config.setLeakDetectionThreshold(60 * 1000);
                    final Properties driverSpecificProperties = SpongeSqlManager.PROTOCOL_SPECIFIC_PROPS.get(key.getDriverClassName());
                    if (driverSpecificProperties != null) {
                        config.setDataSourceProperties(driverSpecificProperties);
                    }
                    config.setJdbcUrl(key.getAuthlessUrl());
                    return new HikariDataSource(config);
                });
    }
    @Override
    public DataSource dataSource(final String jdbcConnection) throws SQLException {
        return this.dataSource(null, jdbcConnection);
    }

    @Override
    public DataSource dataSource(final PluginContainer plugin, final String jdbcConnection) throws SQLException {
        checkNotNull(this.connectionCache);

        final String jdbcConnectionString = this.connectionUrlFromAlias(jdbcConnection).orElse(jdbcConnection);
        final ConnectionInfo info = ConnectionInfo.fromUrl(plugin, jdbcConnectionString);
        try {
            return this.connectionCache.get(info);
        } catch (final CompletionException e) {
            throw new SQLException(e);
        }
    }

    @Override
    public void close() throws IOException {
        if (this.connectionCache != null) {
            this.connectionCache.invalidateAll();
        }
    }

    public static class ConnectionInfo {

        private static final Pattern URL_REGEX = Pattern.compile("(?:jdbc:)?([^:]+):(//)?(?:([^:]+)(?::([^@]+))?@)?(.*)");
        private static final String UTF_8 = StandardCharsets.UTF_8.name();
        private final @Nullable String user;
        private final @Nullable String password;
        private final String driverClassName;
        private final String authlessUrl;
        private final String fullUrl;

        /**
         * Create a new ConnectionInfo with the give parameters
         * @param user The username to use when connecting to th database
         * @param password The password to connect with. If user is not null, password must not be null
         * @param driverClassName The class name of the driver to use for this connection
         * @param authlessUrl A JDBC url for this driver not containing authentication information
         * @param fullUrl The full jdbc url containing user, password, and database info
         */
        public ConnectionInfo(final @Nullable String user, final @Nullable String password, final String driverClassName, final String authlessUrl, final String fullUrl) {
            this.user = user;
            this.password = password;
            this.driverClassName = driverClassName;
            this.authlessUrl = authlessUrl;
            this.fullUrl = fullUrl;
        }

        public @Nullable String getUser() {
            return this.user;
        }

        public @Nullable String getPassword() {
            return this.password;
        }

        public String getDriverClassName() {
            return this.driverClassName;
        }

        public String getAuthlessUrl() {
            return this.authlessUrl;
        }

        public String getFullUrl() {
            return this.fullUrl;
        }


        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || this.getClass() != o.getClass()) {
                return false;
            }
            final ConnectionInfo that = (ConnectionInfo) o;
            return Objects.equal(this.user, that.user)
                    && Objects.equal(this.password, that.password)
                    && Objects.equal(this.driverClassName, that.driverClassName)
                    && Objects.equal(this.authlessUrl, that.authlessUrl)
                    && Objects.equal(this.fullUrl, that.fullUrl);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(this.user, this.password, this.driverClassName, this.authlessUrl, this.fullUrl);
        }

        /**
         * Extracts the connection info from a JDBC url with additional authentication information as specified in {@link SqlManager}.
         *
         *
         * @param container The plugin to put a path relative to
         * @param fullUrl The full JDBC URL as specified in SqlService
         * @return A constructed ConnectionInfo object using the info from the provided URL
         * @throws SQLException If the driver for the given URL is not present
         */
        public static ConnectionInfo fromUrl(final @Nullable PluginContainer container, final String fullUrl) throws SQLException {
            final Matcher match = ConnectionInfo.URL_REGEX.matcher(fullUrl);
            if (!match.matches()) {
                throw new IllegalArgumentException("URL " + fullUrl + " is not a valid JDBC URL");
            }

            final String protocol = match.group(1);
            final boolean hasSlashes = match.group(2) != null;
            final String user = ConnectionInfo.urlDecode(match.group(3));
            final String pass = ConnectionInfo.urlDecode(match.group(4));
            String serverDatabaseSpecifier = match.group(5);
            final BiFunction<PluginContainer, String, String> derelativizer = SpongeSqlManager.PATH_CANONICALIZERS.get(protocol);
            if (container != null && derelativizer != null) {
                serverDatabaseSpecifier = derelativizer.apply(container, serverDatabaseSpecifier);
            }
            final String unauthedUrl = "jdbc:" + protocol + (hasSlashes ? "://" : ":") + serverDatabaseSpecifier;
            final String driverClass = DriverManager.getDriver(unauthedUrl).getClass().getCanonicalName();
            return new ConnectionInfo(user, pass, driverClass, unauthedUrl, fullUrl);
        }

        private static String urlDecode(final String str) {
            try {
                return str == null ? null : URLDecoder.decode(str, ConnectionInfo.UTF_8);
            } catch (final UnsupportedEncodingException e) {
                // If UTF-8 is not supported, we have bigger problems...
                throw new RuntimeException("UTF-8 is not supported on this system", e);
            }
        }
    }

    @Override
    public Optional<String> connectionUrlFromAlias(final String alias) {
        return Optional.ofNullable(SpongeConfigs.getCommon().get().sql.aliases.get(alias));
    }

}
