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
package co.aikar.timings;

import static co.aikar.timings.TimingsManager.HISTORY;
import static co.aikar.util.JSONUtil.appendObjectData;
import static co.aikar.util.JSONUtil.createObject;
import static co.aikar.util.JSONUtil.pair;
import static co.aikar.util.JSONUtil.toArray;
import static co.aikar.util.JSONUtil.toArrayMapper;
import static co.aikar.util.JSONUtil.toObjectMapper;

import co.aikar.util.JSONUtil;
import co.aikar.util.JSONUtil.JSONPair;
import com.google.common.base.Function;
import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.server.MinecraftServer;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.command.CommandSource;
import org.spongepowered.api.util.command.source.ConsoleSource;
import org.spongepowered.api.util.command.source.RconSource;
import org.spongepowered.common.Sponge;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.zip.GZIPOutputStream;

@SuppressWarnings("rawtypes")
class TimingsExport extends Thread {

    private final CommandSource sender;
    private final Map out;
    private final TimingHistory[] history;

    TimingsExport(CommandSource sender, Map out, TimingHistory[] history) {
        super("Timings paste thread");
        this.sender = sender;
        this.out = out;
        this.history = history;
    }

    /**
     * Builds an XML report of the timings to be uploaded for parsing.
     *
     * @param sender Who to report to
     */
    static void reportTimings(CommandSource sender) {
        Map parent = createObject(
                // Get some basic system details about the server
                pair("version", Sponge.getGame().getPlatform().getVersion()),
                pair("maxplayers", Sponge.getGame().getServer().getMaxPlayers()),
                pair("start", TimingsManager.timingStart / 1000),
                pair("end", System.currentTimeMillis() / 1000),
                pair("sampletime", (System.currentTimeMillis() - TimingsManager.timingStart) / 1000));
        if (!TimingsManager.privacy) {
            appendObjectData(parent,
                    pair("server", Sponge.ECOSYSTEM_NAME),
                    pair("motd", Sponge.getGame().getServer().getMotd()),
                    pair("online-mode", Sponge.getGame().getServer().getOnlineMode()),
                    pair("icon", MinecraftServer.getServer().getServerStatusResponse().getFavicon()));
        }

        final Runtime runtime = Runtime.getRuntime();

        parent.put("system", createObject(
                pair("timingcost", getCost()),
                pair("name", System.getProperty("os.name")),
                pair("version", System.getProperty("os.version")),
                pair("arch", System.getProperty("os.arch")),
                pair("totalmem", runtime.totalMemory()),
                pair("usedmem", runtime.totalMemory() - runtime.freeMemory()),
                pair("maxmem", runtime.maxMemory()),
                pair("cpu", runtime.availableProcessors()),
                pair("runtime", (System.currentTimeMillis() / 1000) - TimingsManager.SERVER_START)));

        Set<BlockType> tileEntityTypeSet = Sets.newHashSet();
        Set<EntityType> entityTypeSet = Sets.newHashSet();

        int size = HISTORY.size();
        TimingHistory[] history = new TimingHistory[size + 1];
        int i = 0;
        for (TimingHistory timingHistory : HISTORY) {
            tileEntityTypeSet.addAll(timingHistory.tileEntityTypeSet);
            entityTypeSet.addAll(timingHistory.entityTypeSet);
            history[i++] = timingHistory;
        }

        history[i] = new TimingHistory(); // Current snapshot
        tileEntityTypeSet.addAll(history[i].tileEntityTypeSet);
        entityTypeSet.addAll(history[i].entityTypeSet);

        Map handlers = createObject();
        for (TimingIdentifier.TimingGroup group : TimingIdentifier.GROUP_MAP.values()) {
            for (TimingHandler id : group.handlers) {
                if (!id.timed && !id.isSpecial()) {
                    continue;
                }
                handlers.put(id.id, toArray(
                        group.id,
                        id.name));
            }
        }

        parent.put("idmap", createObject(
                pair("groups", toObjectMapper(
                        TimingIdentifier.GROUP_MAP.values(), new Function<TimingIdentifier.TimingGroup, JSONPair>() {

                            @Override
                            public JSONPair apply(TimingIdentifier.TimingGroup group) {
                                return pair(group.id, group.name);
                            }
                        })),
                pair("handlers", handlers),
                pair("worlds", toObjectMapper(TimingHistory.worldMap.entrySet(), new Function<Map.Entry<String, Integer>, JSONPair>() {

                    @Override
                    public JSONPair apply(Map.Entry<String, Integer> input) {
                        return pair(input.getValue(), input.getKey());
                    }
                })),
                pair("tileentity",
                        toObjectMapper(tileEntityTypeSet, new Function<BlockType, JSONPair>() {

                            @Override
                            public JSONPair apply(BlockType input) {
                                return pair(input.getId(), input.getName());
                            }
                        })),
                pair("entity",
                        toObjectMapper(entityTypeSet, new Function<EntityType, JSONPair>() {

                            @Override
                            public JSONPair apply(EntityType input) {
                                return pair(input.getId(), input.getName());
                            }
                        }))));

        // Information about loaded plugins

        parent.put("plugins", toObjectMapper(Sponge.getGame().getPluginManager().getPlugins(),
                new Function<PluginContainer, JSONPair>() {

                    @Override
                    public JSONPair apply(PluginContainer plugin) {
                        return pair(plugin.getName(), createObject(
                                pair("version", plugin.getVersion())
                        /*
                         * TODO More metadata pair("description",
                         * String.valueOf(plugin.getDescription().getDescription
                         * ()).trim()), pair("website",
                         * plugin.getDescription().getWebsite()),
                         * pair("authors",
                         * StringUtils.join(plugin.getDescription().getAuthors()
                         * , ", "))
                         */
                        ));
                    }
                }));

        // Information on the users Config

        parent.put("config", createObject(
                pair("sponge", mapAsJSON(Sponge.getGlobalConfig().getRootNode(), null))));

        new TimingsExport(sender, parent, history).start();
    }

    static long getCost() {
        // Benchmark the users System.nanotime() for cost basis
        int passes = 500000;
        TimingHandler SAMPLER1 = SpongeTimingsFactory.ofSafe("Timings Sampler 1");
        TimingHandler SAMPLER2 = SpongeTimingsFactory.ofSafe("Timings Sampler 2");
        TimingHandler SAMPLER3 = SpongeTimingsFactory.ofSafe("Timings Sampler 3");
        TimingHandler SAMPLER4 = SpongeTimingsFactory.ofSafe("Timings Sampler 4");
        TimingHandler SAMPLER5 = SpongeTimingsFactory.ofSafe("Timings Sampler 5");
        TimingHandler SAMPLER6 = SpongeTimingsFactory.ofSafe("Timings Sampler 6");

        long start = System.nanoTime();
        for (int i = 0; i < passes; i++) {
            SAMPLER1.startTiming();
            SAMPLER2.startTiming();
            SAMPLER3.startTiming();
            SAMPLER3.stopTiming();
            SAMPLER4.startTiming();
            SAMPLER5.startTiming();
            SAMPLER6.startTiming();
            SAMPLER6.stopTiming();
            SAMPLER5.stopTiming();
            SAMPLER4.stopTiming();
            SAMPLER2.stopTiming();
            SAMPLER1.stopTiming();
        }
        long timingsCost = (System.nanoTime() - start) / passes / 6;
        SAMPLER1.reset(true);
        SAMPLER2.reset(true);
        SAMPLER3.reset(true);
        SAMPLER4.reset(true);
        SAMPLER5.reset(true);
        SAMPLER6.reset(true);
        return timingsCost;
    }

    private static JsonObject mapAsJSON(ConfigurationNode config, String parentKey) {

        JsonObject object = new JsonObject();
        for (Entry<Object, ? extends ConfigurationNode> entry : config.getChildrenMap().entrySet()) {
            String key = (String) entry.getKey();
            String fullKey = (parentKey != null ? parentKey + "." + key : key);
            if (fullKey.equals("database") || fullKey.equals("settings.bungeecord-addresses") || TimingsManager.hiddenConfigs.contains(fullKey)) {
                continue;
            }
            object.add(key, valAsJSON(entry.getValue(), fullKey));
        }
        return object;
    }

    private static JsonElement valAsJSON(Object val, final String parentKey) {
        if (!(val instanceof ConfigurationNode)) {
            if (val instanceof List) {
                Iterable<Object> v = (Iterable<Object>) val;
                return toArrayMapper(v, new Function<Object, Object>() {

                    @Override
                    public Object apply(Object input) {
                        return valAsJSON(input, parentKey);
                    }
                });
            } else {
                return new JsonPrimitive(val.toString());
            }
        } else {
            return mapAsJSON((ConfigurationNode) val, parentKey);
        }
    }

    @SuppressWarnings("CallToThreadRun")
    @Override
    public synchronized void start() {
        if (this.sender instanceof RconSource) {
            this.sender.sendMessage(Texts.of(TextColors.RED, "Warning: Timings report done over RCON will cause lag spikes."));
            this.sender.sendMessage(Texts.of(TextColors.RED, "You should use ", TextColors.YELLOW,
                    "/timings report" + TextColors.RED, " in game or console."));
            run();
        } else {
            super.start();
        }
    }

    @Override
    public void run() {
        this.sender.sendMessage(Texts.of(TextColors.GREEN, "Preparing Timings Report..."));

        this.out.put("data", toObjectMapper(this.history, new Function<TimingHistory, JSONPair>() {

            @Override
            public JSONPair apply(TimingHistory input) {
                return input.export();
            }
        }));

        String response = null;
        try {
            HttpURLConnection con = (HttpURLConnection) new URL("http://timings.aikar.co/post").openConnection();
            con.setDoOutput(true);
            con.setRequestProperty("User-Agent", "Sponge/" + Sponge.ECOSYSTEM_NAME + "/" + InetAddress.getLocalHost().getHostName());
            con.setRequestMethod("POST");
            con.setInstanceFollowRedirects(false);

            OutputStream request = new GZIPOutputStream(con.getOutputStream()) {

                {
                    this.def.setLevel(7);
                }
            };

            request.write(JSONUtil.toJsonString(this.out).getBytes("UTF-8"));
            request.close();

            response = getResponse(con);

            if (con.getResponseCode() != 302) {
                this.sender.sendMessage(Texts.of(
                        TextColors.RED, "Upload Error: " + con.getResponseCode() + ": " + con.getResponseMessage()));
                this.sender.sendMessage(Texts.of(TextColors.RED, "Check your logs for more information"));
                if (response != null) {
                    Sponge.getLogger().fatal(response);
                }
                return;
            }

            String location = con.getHeaderField("Location");
            this.sender.sendMessage(Texts.of(TextColors.GREEN, "View Timings Report: " + location));
            if (!(this.sender instanceof ConsoleSource)) {
                Sponge.getLogger().info("View Timings Report: " + location);
            }

            if (response != null && !response.isEmpty()) {
                Sponge.getLogger().info("Timing Response: " + response);
            }
        } catch (IOException ex) {
            this.sender.sendMessage(Texts.of(TextColors.RED, "Error uploading timings, check your logs for more information"));
            if (response != null) {
                Sponge.getLogger().fatal(response);
            }
            Sponge.getLogger().fatal("Could not paste timings", ex);
        }
    }

    private String getResponse(HttpURLConnection con) throws IOException {
        InputStream is = null;
        try {
            is = con.getInputStream();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();

            byte[] b = new byte[1024];
            int bytesRead;
            while ((bytesRead = is.read(b)) != -1) {
                bos.write(b, 0, bytesRead);
            }
            return bos.toString();

        } catch (IOException ex) {
            this.sender.sendMessage(Texts.of(TextColors.RED, "Error uploading timings, check your logs for more information"));
            Sponge.getLogger().warn(con.getResponseMessage(), ex);
            return null;
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }
}
