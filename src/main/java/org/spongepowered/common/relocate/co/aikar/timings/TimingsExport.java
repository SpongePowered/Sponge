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
package org.spongepowered.common.relocate.co.aikar.timings;

import static org.spongepowered.api.Platform.Component.IMPLEMENTATION;

import org.spongepowered.common.relocate.co.aikar.util.JSONUtil;
import org.spongepowered.common.relocate.co.aikar.util.JSONUtil.JsonObjectBuilder;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.Platform;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.entity.BlockEntityType;
import org.spongepowered.api.command.source.RconSource;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.channel.MessageReceiver;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.common.SpongeImpl;

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
    final static List<MessageChannel> requestingReport = Lists.newArrayList();

    TimingsExport(TimingsReportListener listeners, JsonObject out, TimingHistory[] history) {
        super("Timings paste thread");
        this.listeners = listeners;
        this.out = out;
        this.history = history;
    }

    private static String getServerName() {
        return SpongeImpl.getPlugin().getName() + " " + SpongeImpl.getPlugin().getVersion().orElse("");
    }

    /**
     * Builds an XML report of the timings to be uploaded for parsing.
     *
     * @param sender Who to report to
     */
    static void reportTimings() {
        if (requestingReport.isEmpty()) {
            return;
        }
        TimingsReportListener listeners = new TimingsReportListener(requestingReport);

        requestingReport.clear();
        long now = System.currentTimeMillis();
        final long lastReportDiff = now - lastReport;
        if (lastReportDiff < 60000) {
            listeners.send(Text.of(TextColors.RED, "Please wait at least 1 minute in between Timings reports. (" + (int)((60000 - lastReportDiff) / 1000) + " seconds)"));
            listeners.done();
            return;
        }
        final long lastStartDiff = now - TimingsManager.timingStart;
        if (lastStartDiff < 180000) {
            listeners.send(Text.of(TextColors.RED, "Please wait at least 3 minutes before generating a Timings report. Unlike Timings v1, v2 benefits from longer timings and is not as useful with short timings. (" + (int)((180000 - lastStartDiff) / 1000) + " seconds)"));
            listeners.done();
            return;
        }
        listeners.send(Text.of(TextColors.GREEN, "Preparing Timings Report..."));
        lastReport = now;

        Platform platform = SpongeImpl.getGame().getPlatform();
        JsonObjectBuilder builder = JSONUtil.objectBuilder()
                // Get some basic system details about the server
                .add("version", platform.getContainer(IMPLEMENTATION).getVersion().orElse(platform.getMinecraftVersion().getName() + "-DEV"))
                .add("maxplayers", SpongeImpl.getGame().getServer().getMaxPlayers())
                .add("start", TimingsManager.timingStart / 1000)
                .add("end", System.currentTimeMillis() / 1000)
                .add("sampletime", (System.currentTimeMillis() - TimingsManager.timingStart) / 1000);
        if (!TimingsManager.privacy) {
            builder.add("server", getServerName())
                    .add("motd", Sponge.getServer().getMotd().toPlain())
                    .add("online-mode", Sponge.getServer().getOnlineMode())
                    .add("icon", SpongeImpl.getServer().getServerStatusResponse().getFavicon());
        }

        final Runtime runtime = Runtime.getRuntime();
        RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
        builder.add("system", JSONUtil.objectBuilder()
                .add("timingcost", getCost())
                .add("name", System.getProperty("os.name"))
                .add("version", System.getProperty("os.version"))
                .add("jvmversion", System.getProperty("java.version"))
                .add("arch", System.getProperty("os.arch"))
                .add("maxmem", runtime.maxMemory())
                .add("cpu", runtime.availableProcessors())
                .add("runtime", ManagementFactory.getRuntimeMXBean().getUptime())
                .add("flags", RUNTIME_FLAG_JOINER.join(runtimeBean.getInputArguments()))
                .add("gc", JSONUtil.mapArrayToObject(ManagementFactory.getGarbageCollectorMXBeans(), (input) -> {
                    return JSONUtil.singleObjectPair(input.getName(), JSONUtil.arrayOf(input.getCollectionCount(), input.getCollectionTime()));
                })));

        Set<BlockEntityType> tileEntityTypeSet = Sets.newHashSet();
        Set<EntityType> entityTypeSet = Sets.newHashSet();

        int size = TimingsManager.HISTORY.size();
        TimingHistory[] history = new TimingHistory[size + 1];
        int i = 0;
        for (TimingHistory timingHistory : TimingsManager.HISTORY) {
            tileEntityTypeSet.addAll(timingHistory.tileEntityTypeSet);
            entityTypeSet.addAll(timingHistory.entityTypeSet);
            history[i++] = timingHistory;
        }

        history[i] = new TimingHistory(); // Current snapshot
        tileEntityTypeSet.addAll(history[i].tileEntityTypeSet);
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
                .add("groups", JSONUtil.mapArrayToObject(TimingIdentifier.GROUP_MAP.values(), (group) -> {
                    return JSONUtil.singleObjectPair(group.id, group.name);
                }))
                .add("handlers", handlersBuilder)
                .add("worlds", JSONUtil.mapArrayToObject(TimingHistory.worldMap.entrySet(), (entry) -> {
                    return JSONUtil.singleObjectPair(entry.getValue(), entry.getKey());
                }))
                .add("tileentity", JSONUtil.mapArrayToObject(tileEntityTypeSet, (tileEntityType) -> {
                    return JSONUtil.singleObjectPair(TimingsPls.getTileEntityId(tileEntityType), tileEntityType.getName());
                }))
                .add("entity", JSONUtil.mapArrayToObject(entityTypeSet, (entityType) -> {
                    if (entityType == EntityTypes.UNKNOWN) {
                        return null;
                    }
                    return JSONUtil.singleObjectPair(TimingsPls.getEntityId(entityType), entityType.getId());
                })));

        // Information about loaded plugins

        builder.add("plugins", JSONUtil.mapArrayToObject(SpongeImpl.getGame().getPluginManager().getPlugins(), (plugin) -> {
            return JSONUtil.objectBuilder().add(plugin.getId(), JSONUtil.objectBuilder()
                    .add("version", plugin.getVersion().orElse(""))
                    .add("description", plugin.getDescription().orElse(""))
                    .add("website", plugin.getUrl().orElse(""))
                    .add("authors", AUTHOR_LIST_JOINER.join(plugin.getAuthors()))
            ).build();
        }));

        // Information on the users Config

        builder.add("config", JSONUtil.objectBuilder()
                .add("sponge", serializeConfigNode(SpongeImpl.getGlobalConfigAdapter().getRootNode())));

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

    private static JsonElement serializeConfigNode(ConfigurationNode node) {
        if (node.hasMapChildren()) {
            JsonObject object = new JsonObject();
            for (Entry<Object, ? extends ConfigurationNode> entry : node.getChildrenMap().entrySet()) {
                String fullPath = CONFIG_PATH_JOINER.join(entry.getValue().getPath());
                if (fullPath.equals("sponge.sql") || TimingsManager.hiddenConfigs.contains(fullPath)) {
                    continue;
                }
                object.add(entry.getKey().toString(), serializeConfigNode(entry.getValue()));
            }
            return object;
        }
        if (node.hasListChildren()) {
            JsonArray array = new JsonArray();
            for (ConfigurationNode child : node.getChildrenList()) {
                array.add(serializeConfigNode(child));
            }
            return array;
        }
        return JSONUtil.toJsonElement(node.getValue());
    }

    @Override
    public synchronized void start() {
        boolean containsRconSource = false;
        for (MessageReceiver receiver : this.listeners.getChannel().getMembers()) {
            if (receiver instanceof RconSource) {
                containsRconSource = true;
                break;
            }
        }
        if (containsRconSource) {
            this.listeners.send(Text.of(TextColors.RED, "Warning: Timings report done over RCON will cause lag spikes."));
            this.listeners.send(Text.of(TextColors.RED, "You should use ", TextColors.YELLOW,
                    "/sponge timings report" + TextColors.RED, " in game or console."));
            run();
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
                    SpongeImpl.getLogger().warn("Could not get own server hostname when uploading timings - falling back to 'localhost'", e);
                }
            }
            HttpURLConnection con = (HttpURLConnection) new URL("https://timings.aikar.co/post").openConnection();
            con.setDoOutput(true);
            String name = TimingsManager.privacy ? "" : getServerName();
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

            response = getResponse(con);

            if (con.getResponseCode() != 302) {
                this.listeners.send(Text.of(
                        TextColors.RED, "Upload Error: " + con.getResponseCode() + ": " + con.getResponseMessage()));
                this.listeners.send(Text.of(TextColors.RED, "Check your logs for more information"));
                if (response != null) {
                    SpongeImpl.getLogger().fatal(response);
                }
                return;
            }

            timingsURL = con.getHeaderField("Location");
            this.listeners.send(Text.of(TextColors.GREEN, "View Timings Report: ", TextActions.openUrl(new URL(timingsURL)), timingsURL));

            if (response != null && !response.isEmpty()) {
                SpongeImpl.getLogger().info("Timing Response: " + response);
            }
        } catch (IOException ex) {
            this.listeners.send(Text.of(TextColors.RED, "Error uploading timings, check your logs for more information"));
            if (response != null) {
                SpongeImpl.getLogger().fatal(response);
            }
            SpongeImpl.getLogger().fatal("Could not paste timings", ex);
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
            this.listeners.send(Text.of(TextColors.RED, "Error uploading timings, check your logs for more information"));
            SpongeImpl.getLogger().warn(con.getResponseMessage(), ex);
            return null;
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }
}
