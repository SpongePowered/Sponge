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
package org.spongepowered.common.inject.provider;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.ProvisionException;
import com.google.inject.Scopes;
import com.google.inject.TypeLiteral;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.config.ConfigRoot;
import org.spongepowered.common.config.PluginConfigManager;
import org.spongepowered.common.inject.provider.PluginConfigurationModule.CommentedConfigurationNodeProvider;
import org.spongepowered.common.inject.provider.PluginConfigurationModule.CommentedConfigurationNodeReferenceProvider;
import org.spongepowered.common.inject.provider.PluginConfigurationModule.IHateGuiceInjectorProvider;
import org.spongepowered.common.inject.provider.PluginConfigurationModule.NonSharedDirAsPath;
import org.spongepowered.common.inject.provider.PluginConfigurationModule.NonSharedPathAsPath;
import org.spongepowered.common.inject.provider.PluginConfigurationModule.PrivateCommentedConfigurationNode;
import org.spongepowered.common.inject.provider.PluginConfigurationModule.PrivateCommentedConfigurationNodeReference;
import org.spongepowered.common.inject.provider.PluginConfigurationModule.SharedCommentedConfigurationNode;
import org.spongepowered.common.inject.provider.PluginConfigurationModule.SharedCommentedConfigurationNodeReference;
import org.spongepowered.common.inject.provider.PluginConfigurationModule.SharedDirAsPath;
import org.spongepowered.common.inject.provider.PluginConfigurationModule.TypeSerializers;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.objectmapping.ObjectMapper;
import org.spongepowered.configurate.objectmapping.guice.GuiceObjectMapperProvider;
import org.spongepowered.configurate.reference.ConfigurationReference;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;
import org.spongepowered.plugin.PluginContainer;

import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Welcome to the mess that is configuration.
 */
public final class PluginConfigurationModule extends AbstractModule {

    private static final TypeLiteral<ConfigurationLoader<CommentedConfigurationNode>> COMMENTED_CONFIGURATION_NODE_LOADER = new TypeLiteral<ConfigurationLoader<CommentedConfigurationNode>>() {};
    private static final TypeLiteral<ConfigurationReference<CommentedConfigurationNode>> COMMENTED_CONFIGURATION_NODE_REFERENCE = new TypeLiteral<ConfigurationReference<CommentedConfigurationNode>>() {};

    @Override
    protected void configure() {
        this.bind(Path.class)
                .annotatedWith(ConfigDirAnnotation.NON_SHARED)
                .toProvider(NonSharedDirAsPath.class);
        // Plugin-private directory config file
        this.bind(Path.class)
                .annotatedWith(DefaultConfigAnnotation.NON_SHARED)
                .toProvider(NonSharedPathAsPath.class);
        // Shared-directory config file
        this.bind(Path.class)
                .annotatedWith(DefaultConfigAnnotation.SHARED)
                .toProvider(SharedDirAsPath.class);

        // Access plugin-specific type serializers
        this.bind(TypeSerializerCollection.class)
                .toProvider(TypeSerializers.class)
                .in(Scopes.SINGLETON);

        // Loader for shared-directory config file
        this.bind(PluginConfigurationModule.COMMENTED_CONFIGURATION_NODE_LOADER)
                .annotatedWith(DefaultConfigAnnotation.SHARED)
                .toProvider(SharedCommentedConfigurationNode.class);

        // Loader for plugin-private directory config file
        this.bind(PluginConfigurationModule.COMMENTED_CONFIGURATION_NODE_LOADER)
                .annotatedWith(DefaultConfigAnnotation.NON_SHARED)
                .toProvider(PrivateCommentedConfigurationNode.class);

        // Auto-reloading reference for shared-directory config files
        this.bind(PluginConfigurationModule.COMMENTED_CONFIGURATION_NODE_REFERENCE)
                .annotatedWith(DefaultConfigAnnotation.SHARED)
                .toProvider(SharedCommentedConfigurationNodeReference.class);

        // Auto-reloading reference for plugin-private directory config files
        this.bind(PluginConfigurationModule.COMMENTED_CONFIGURATION_NODE_REFERENCE)
                .annotatedWith(DefaultConfigAnnotation.NON_SHARED)
                .toProvider(PrivateCommentedConfigurationNodeReference.class);

        this.requestStaticInjection(IHateGuiceInjectorProvider.class);
    }

    /**
     * So... we need a way to get the final Injector used to create the plugin
     * instance, but Guice doesn't want to give us that in the provider for the
     * TypeSerializerCollection.
     *
     * Instead, we request static injection of this class, which collects the
     * necessary information right as the Injector is created, but before it
     * has been passed to the plugin loader to create the plugin instance.
     *
     * Please, please try to find something nicer. I give up.
     */
    static final class IHateGuiceInjectorProvider {

        private static final Map<PluginContainer, Injector> injectors = new ConcurrentHashMap<>();

        @Inject
        static void acceptRegistration(final PluginContainer container, final Injector injector) {
            IHateGuiceInjectorProvider.injectors.put(container, injector);
        }

        public static @Nullable Injector get(final PluginContainer container) {
            return IHateGuiceInjectorProvider.injectors.get(container);
        }

    }

    /**
     * Provides a non-shared (private) directory.
     *
     * {@literal @}ConfigDir(sharedRoot = false) Path configDir;
     */
    static final class NonSharedDirAsPath implements Provider<Path> {

        private final PluginContainer container;
        private final PluginConfigManager mgr;

        @Inject
        NonSharedDirAsPath(final PluginContainer container, final PluginConfigManager mgr) {
            this.container = container;
            this.mgr = mgr;
        }

        @Override
        public Path get() {
            return this.mgr.getPluginConfig(this.container).getDirectory();
        }

    }

    /**
     * Provides a configuration path within a non-shared (private) directory.
     *
     * {@literal @}DefaultConfig(sharedRoot = false) Path configPath;
     */
    static final class NonSharedPathAsPath implements Provider<Path> {

        private final PluginContainer container;
        private final PluginConfigManager mgr;

        @Inject
        NonSharedPathAsPath(final PluginContainer container, final PluginConfigManager mgr) {
            this.container = container;
            this.mgr = mgr;
        }

        @Override
        public Path get() {
            return this.mgr.getPluginConfig(this.container).getConfigPath();
        }

    }

    /**
     * Provides a configuration path within a shared directory.
     *
     * {@literal @}DefaultConfig(sharedRoot = true) Path configFile;
     */
    static final class SharedDirAsPath implements Provider<Path> {

        private final PluginContainer container;
        private final PluginConfigManager mgr;

        @Inject
        SharedDirAsPath(final PluginContainer container, final PluginConfigManager mgr) {
            this.container = container;
            this.mgr = mgr;
        }

        @Override
        public Path get() {
            return this.mgr.getSharedConfig(this.container).getConfigPath();
        }

    }

    abstract static class CommentedConfigurationNodeProvider implements Provider<ConfigurationLoader<CommentedConfigurationNode>> {

        protected final PluginContainer container;
        protected final PluginConfigManager mgr;
        protected final Provider<TypeSerializerCollection> serializers;

        @Inject
        CommentedConfigurationNodeProvider(final PluginContainer container, final PluginConfigManager mgr,
                final Provider<TypeSerializerCollection> serializers) {
            this.container = container;
            this.mgr = mgr;
            this.serializers = serializers;
        }

    }

    static final class SharedCommentedConfigurationNode extends CommentedConfigurationNodeProvider {

        @Inject
        SharedCommentedConfigurationNode(final PluginContainer container, final PluginConfigManager mgr,
                final Provider<TypeSerializerCollection> serializers) {
            super(container, mgr, serializers);
        }

        @Override
        public ConfigurationLoader<CommentedConfigurationNode> get() {
            return this.mgr.getSharedConfig(this.container).getConfig(PluginConfigManager.getOptions(this.serializers.get()));
        }

    }

    static final class PrivateCommentedConfigurationNode extends CommentedConfigurationNodeProvider {

        @Inject
        PrivateCommentedConfigurationNode(final PluginContainer container, final PluginConfigManager mgr,
                final Provider<TypeSerializerCollection> serializers) {
            super(container, mgr, serializers);
        }

        @Override
        public ConfigurationLoader<CommentedConfigurationNode> get() {
            return this.mgr.getPluginConfig(this.container).getConfig(PluginConfigManager.getOptions(this.serializers.get()));
        }

    }

    abstract static class CommentedConfigurationNodeReferenceProvider implements Provider<ConfigurationReference<CommentedConfigurationNode>> {

        protected final PluginContainer container;
        protected final PluginConfigManager mgr;
        protected final Provider<TypeSerializerCollection> serializers;

        @Inject
        CommentedConfigurationNodeReferenceProvider(final PluginContainer container, final PluginConfigManager mgr,
                final Provider<TypeSerializerCollection> serializers) {
            this.container = container;
            this.mgr = mgr;
            this.serializers = serializers;
        }

        /**
         * Set up error logging for the created reference in the plugin's logger.
         *
         * @param file File loaded from
         * @param reference Configuration reference to configure
         * @param <N> node type
         * @return input {@code reference}
         */
        protected <N extends ConfigurationNode> ConfigurationReference<N> configureLogging(final Path file,
                final ConfigurationReference<N> reference) {
            reference.errors().subscribe(error -> {
                final ConfigurationReference.ErrorPhase phase = error.getKey();
                final Throwable cause = error.getValue();
                this.container.getLogger().error("Failed to perform a {} in the configuration for {} at {}:",
                                  phase, this.container.getMetadata().getId(), file, cause);
            });
            return reference;
        }

    }

    static final class SharedCommentedConfigurationNodeReference extends CommentedConfigurationNodeReferenceProvider {

        @Inject
        SharedCommentedConfigurationNodeReference(final PluginContainer container, final PluginConfigManager mgr,
                final Provider<TypeSerializerCollection> serializers) {
            super(container, mgr, serializers);
        }

        @Override
        public ConfigurationReference<CommentedConfigurationNode> get() {
            final ConfigRoot shared = this.mgr.getSharedConfig(this.container);
            try {
                return this.<CommentedConfigurationNode>configureLogging(shared.getConfigPath(), this.mgr.getWatchServiceListener()
                         .listenToConfiguration(path -> shared.getConfig(PluginConfigManager.getOptions(this.serializers.get())), shared.getConfigPath()));
            } catch (final ConfigurateException ex) {
                throw new ProvisionException("Unable to load configuration reference", ex);
            }
        }

    }

    static final class PrivateCommentedConfigurationNodeReference extends CommentedConfigurationNodeReferenceProvider {

        @Inject
        PrivateCommentedConfigurationNodeReference(final PluginContainer container, final PluginConfigManager mgr,
                final Provider<TypeSerializerCollection> serializers) {
            super(container, mgr, serializers);
        }

        @Override
        public ConfigurationReference<CommentedConfigurationNode> get() {
            final ConfigRoot privateRoot = this.mgr.getPluginConfig(this.container);
            try {
                return this.<CommentedConfigurationNode>configureLogging(privateRoot.getConfigPath(), this.mgr.getWatchServiceListener()
                        .listenToConfiguration(path -> privateRoot.getConfig(PluginConfigManager.getOptions(this.serializers.get())),
                                               privateRoot.getConfigPath()));
            } catch (final ConfigurateException ex) {
                throw new ProvisionException("Unable to load configuration reference", ex);
            }
        }

    }

    static final class TypeSerializers implements Provider<TypeSerializerCollection> {

        private final PluginContainer container;
        private final PluginConfigManager mgr;

        @Inject
        TypeSerializers(final PluginContainer container, final PluginConfigManager mgr) {
            this.container = container;
            this.mgr = mgr;
        }

        @Override
        public TypeSerializerCollection get() {
            final @Nullable Injector injector = IHateGuiceInjectorProvider.get(this.container);
            if (injector == null) {
                return this.mgr.getSerializers();
            } else {
                return this.mgr.getSerializers().childBuilder()
                        .registerAnnotatedObjects(ObjectMapper.factoryBuilder()
                              .addDiscoverer(GuiceObjectMapperProvider.injectedObjectDiscoverer(injector))
                              .build())
                        .build();
            }
        }

    }

}
