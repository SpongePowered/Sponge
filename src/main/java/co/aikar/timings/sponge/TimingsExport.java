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
package co.aikar.timings.sponge;

import static org.spongepowered.api.Platform.Component.IMPLEMENTATION;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.minecraft.core.Registry;
import org.spongepowered.api.Platform;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.entity.BlockEntityType;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.network.RconConnection;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.applaunch.config.core.SpongeConfigs;
import co.aikar.timings.util.JSONUtil;
import co.aikar.timings.util.JSONUtil.JsonObjectBuilder;
import org.spongepowered.common.launch.Launch;
import org.spongepowered.configurate.ConfigurationNode;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.zip.GZIPOutputStream;

class TimingsExport extends Thread {

    private static final Joiner AUTHOR_LIST_JOINER = Joiner.on(", ");
    private static final Joiner RUNTIME_FLAG_JOINER = Joiner.on(" ");
    private static final Joiner CONFIG_PATH_JOINER = Joiner.on(".");

    private final TimingsReportListener listeners;
    private final JsonObject out;
    private final TimingHistory[] history;
    private static long lastReport = 0;
    final static List<Audience> requestingReport = Lists.newArrayList();

    TimingsExport(TimingsReportListener listeners, JsonObject out, TimingHistory[] history) {
        super("Timings paste thread");
        this.listeners = listeners;
        this.out = out;
        this.history = history;
    }

    private static String getServerName() {
        return Launch.instance().platformPlugin().metadata().name() + " " + Launch.instance().platformPlugin().metadata().version();
    }

    /**
     * Builds an XML report of the timings to be uploaded for parsing.
     */
    static void reportTimings() {
        if (TimingsExport.requestingReport.isEmpty()) {
            return;
        }
        TimingsReportListener listeners = new TimingsReportListener(TimingsExport.requestingReport);

        TimingsExport.requestingReport.clear();
        long now = System.currentTimeMillis();
        final long lastReportDiff = now - TimingsExport.lastReport;
        if (lastReportDiff < 60000) {
            listeners.send(Component.text("Please wait at least 1 minute in between Timings reports. (" + (int)((60000 - lastReportDiff) / 1000) + " seconds)", NamedTextColor.RED));
            listeners.done();
            return;
        }
        final long lastStartDiff = now - TimingsManager.timingStart;
        if (lastStartDiff < 180000) {
            listeners.send(Component.text("Please wait at least 3 minutes before generating a Timings report. Unlike Timings v1, v2 benefits from longer timings and is not as useful with short timings. (" + (int)((180000 - lastStartDiff) / 1000) + " seconds)", NamedTextColor.RED));
            listeners.done();
            return;
        }
        listeners.send(Component.text("Preparing Timings Report...", NamedTextColor.GREEN));
        TimingsExport.lastReport = now;

        Platform platform = SpongeCommon.game().platform();
        JsonObjectBuilder builder = JSONUtil.objectBuilder()
                // Get some basic system details about the server
                .add("version", platform.container(IMPLEMENTATION).metadata().version())
                .add("maxplayers", SpongeCommon.game().server().maxPlayers())
                .add("start", TimingsManager.timingStart / 1000)
                .add("end", System.currentTimeMillis() / 1000)
                .add("sampletime", (System.currentTimeMillis() - TimingsManager.timingStart) / 1000);
        if (!TimingsManager.privacy) {
            builder.add("server", TimingsExport.getServerName())
                    .add("motd", PlainTextComponentSerializer.plainText().serialize(Sponge.server().motd()))
                    .add("online-mode", Sponge.server().isOnlineModeEnabled())
                    .add("icon", SpongeCommon.server().getStatus().getFavicon());
        }

        final Runtime runtime = Runtime.getRuntime();
        RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
        builder.add("system", JSONUtil.objectBuilder()
                .add("timingcost", TimingsExport.getCost())
                .add("name", System.getProperty("os.name"))
                .add("version", System.getProperty("os.version"))
                .add("jvmversion", System.getProperty("java.version"))
                .add("arch", System.getProperty("os.arch"))
                .add("maxmem", runtime.maxMemory())
                .add("cpu", runtime.availableProcessors())
                .add("runtime", ManagementFactory.getRuntimeMXBean().getUptime())
                .add("flags", TimingsExport.RUNTIME_FLAG_JOINER.join(runtimeBean.getInputArguments()))
                .add("gc", JSONUtil.mapArrayToObject(ManagementFactory.getGarbageCollectorMXBeans(), (input) -> {
                    return JSONUtil.singleObjectPair(input.getName(), JSONUtil.arrayOf(input.getCollectionCount(), input.getCollectionTime()));
                })));

        Set<BlockEntityType> blockEntityTypeSet = Sets.newHashSet();
        Set<EntityType<?>> entityTypeSet = Sets.newHashSet();

        int size = TimingsManager.HISTORY.size();
        TimingHistory[] history = new TimingHistory[size + 1];
        int i = 0;
        for (TimingHistory timingHistory : TimingsManager.HISTORY) {
            blockEntityTypeSet.addAll(timingHistory.tileEntityTypeSet);
            entityTypeSet.addAll(timingHistory.entityTypeSet);
            history[i++] = timingHistory;
        }

        history[i] = new TimingHistory(); // Current snapshot
        blockEntityTypeSet.addAll(history[i].tileEntityTypeSet);
        entityTypeSet.addAll(history[i].entityTypeSet);

        JsonObjectBuilder handlersBuilder = JSONUtil.objectBuilder();
        for (TimingIdentifier.TimingGroup group : TimingIdentifier.GROUP_MAP.values()) {
            for (TimingHandler id : group.handlers) {
                if (!id.timed && !id.isSpecial()) {
                    continue;
                }
                handlersBuilder.add(id.id, JSONUtil.arrayOf(
                        group.id,
                        id.name));
            }
        }

        builder.add("idmap", JSONUtil.objectBuilder()
                .add("groups", JSONUtil.mapArrayToObject(TimingIdentifier.GROUP_MAP.values(), (group) ->
                        JSONUtil.singleObjectPair(group.id, group.name)))
                .add("handlers", handlersBuilder)
                .add("worlds", JSONUtil.mapArrayToObject(TimingHistory.worldMap.entrySet(), (entry) ->
                        JSONUtil.singleObjectPair(entry.getValue(), entry.getKey())))
                .add("blockentity", JSONUtil.mapArrayToObject(blockEntityTypeSet, (blockEntityType) ->
                    {
                        final ResourceKey resourceKey =
                                Sponge.game().registries().registry(RegistryTypes.BLOCK_ENTITY_TYPE).valueKey(blockEntityType);
                        return JSONUtil.singleObjectPair(TimingsPls.getBlockEntityId(blockEntityType), resourceKey);
                    })
                )
                .add("entity", JSONUtil.mapArrayToObject(entityTypeSet, (entityType) ->
                        {
                            final ResourceKey resourceKey = (ResourceKey) (Object) Registry.ENTITY_TYPE.getKey((net.minecraft.world.entity.EntityType<?>) entityType);
                            return JSONUtil.singleObjectPair(TimingsPls.getEntityId(entityType), resourceKey);
                        })
                )
        );

        // Information about loaded plugins

        builder.add("plugins", JSONUtil.mapArrayToObject(SpongeCommon.game().pluginManager().plugins(), (plugin) -> {
            return JSONUtil.objectBuilder().add(plugin.metadata().id(), JSONUtil.objectBuilder()
                    .add("version", plugin.metadata().version())
                    .add("description", plugin.metadata().description().orElse(""))
                    .add("website", plugin.metadata().links().homepage())
                    .add("authors", TimingsExport.AUTHOR_LIST_JOINER.join(plugin.metadata().contributors()))
            ).build();
        }));

        // Information on the users Config

        builder.add("config", JSONUtil.objectBuilder()
                .add("sponge", TimingsExport.serializeConfigNode(SpongeConfigs.getCommon().getNode())));

        new TimingsExport(listeners, builder.build(), history).start();
    }

    static long getCost() {
        // Benchmark the users System.nanotime() for cost basis
        int passes = 200;
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

    private static JsonElement serializeConfigNode(final ConfigurationNode node) {
        if (node.isMap()) {
            final JsonObject object = new JsonObject();
            for (final Entry<Object, ? extends ConfigurationNode> entry : node.childrenMap().entrySet()) {
                final String fullPath = TimingsExport.CONFIG_PATH_JOINER.join(entry.getValue().path());
                if (fullPath.equals("sponge.sql") || TimingsManager.hiddenConfigs.contains(fullPath)) {
                    continue;
                }
                object.add(entry.getKey().toString(), TimingsExport.serializeConfigNode(entry.getValue()));
            }
            return object;
        }
        if (node.isList()) {
            final JsonArray array = new JsonArray();
            for (final ConfigurationNode child : node.childrenList()) {
                array.add(TimingsExport.serializeConfigNode(child));
            }
            return array;
        }
        return JSONUtil.toJsonElement(node.raw());
    }

    @Override
    public synchronized void start() {
        boolean containsRconSource = false;
        for (Audience receiver : this.listeners.getChannel().audiences()) {
            if (receiver instanceof RconConnection) {
                containsRconSource = true;
                break;
            }
        }
        if (containsRconSource) {
            this.listeners.send(Component.text("Warning: Timings report done over RCON will cause lag spikes.", NamedTextColor.RED));
            this.listeners.send(Component.text("You should use ", NamedTextColor.RED).append(Component.text("/sponge timings report",
                    NamedTextColor.YELLOW)).append(Component.text(" in game or console.", NamedTextColor.RED)));
            this.run();
        } else {
            super.start();
        }
    }

    @Override
    public void run() {
        this.out.add("data", JSONUtil.mapArray(this.history, TimingHistory::export));

        String response = null;
        String timingsURL = null;
        try {
            String hostname = "localhost";
            if (!TimingsManager.privacy) {
                try {
                    hostname = InetAddress.getLocalHost().getHostName();
                } catch (IOException e) {
                    SpongeCommon.logger().warn("Could not get own server hostname when uploading timings - falling back to 'localhost'", e);
                }
            }
            HttpURLConnection con = (HttpURLConnection) new URL("https://timings.aikar.co/post").openConnection();
            con.setDoOutput(true);
            String name = TimingsManager.privacy ? "" : TimingsExport.getServerName();
            con.setRequestProperty("User-Agent", "Sponge/" + name + "/" + hostname);
            con.setRequestMethod("POST");
            con.setInstanceFollowRedirects(false);

            OutputStream request = new GZIPOutputStream(con.getOutputStream()) {

                {
                    this.def.setLevel(7);
                }
            };

            request.write(JSONUtil.toString(this.out).getBytes("UTF-8"));
            request.close();

            response = this.getResponse(con);

            if (con.getResponseCode() != 302) {
                this.listeners.send(Component.text("Upload Error: " + con.getResponseCode() + ": " + con.getResponseMessage(), NamedTextColor.RED));
                this.listeners.send(Component.text("Check your logs for more information", NamedTextColor.RED));
                if (response != null) {
                    SpongeCommon.logger().fatal(response);
                }
                return;
            }

            timingsURL = con.getHeaderField("Location");
            this.listeners.send(Component.text().content("View Timings Report: ").color(NamedTextColor.GREEN).append(Component.text(timingsURL).clickEvent(ClickEvent.openUrl(timingsURL))).build());

            if (response != null && !response.isEmpty()) {
                SpongeCommon.logger().info("Timing Response: " + response);
            }
        } catch (IOException ex) {
            this.listeners.send(Component.text("Error uploading timings, check your logs for more information", NamedTextColor.RED));
            if (response != null) {
                SpongeCommon.logger().fatal(response);
            }
            SpongeCommon.logger().fatal("Could not paste timings", ex);
        } finally {
            this.listeners.done(timingsURL);
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
            this.listeners.send(Component.text("Error uploading timings, check your logs for more information", NamedTextColor.RED));
            SpongeCommon.logger().warn(con.getResponseMessage(), ex);
            return null;
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }
}
