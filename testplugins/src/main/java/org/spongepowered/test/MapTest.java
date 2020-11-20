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
package org.spongepowered.test;

import com.google.inject.Inject;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandExecutor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.action.CreateMapEvent;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.map.MapCanvas;
import org.spongepowered.api.map.MapInfo;
import org.spongepowered.api.map.MapStorage;
import org.spongepowered.api.map.color.MapColor;
import org.spongepowered.api.map.color.MapColorType;
import org.spongepowered.api.map.color.MapColorTypes;
import org.spongepowered.api.map.color.MapShade;
import org.spongepowered.api.map.decoration.MapDecoration;
import org.spongepowered.api.map.decoration.MapDecorationTypes;
import org.spongepowered.api.map.decoration.orientation.MapDecorationOrientation;
import org.spongepowered.api.map.decoration.orientation.MapDecorationOrientations;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.math.vector.Vector2i;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.jvm.Plugin;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Plugin("maptest")
public class MapTest implements LoadableModule {


    @Inject
    private PluginContainer container;

    private final Logger logger;
    private final Listeners listeners;
    private boolean isEnabled = false;

    @Inject
    public MapTest(final Logger logger) {
        this.logger = logger;
        this.listeners = new Listeners();
    }

    @Listener
    public void onGameInit(final RegisterCommandEvent<Command.Parameterized> event) {
        this.createDefaultCommand("getmapdata", (ctx -> {
            final Player player = this.requirePlayer(ctx);
            final ItemStack itemStack = player.getItemInHand(HandTypes.MAIN_HAND);
            if (itemStack.getType() != ItemTypes.FILLED_MAP.get()) {
                throw new CommandException(Component.text("You must hold a map in your hand"));
            }
            final MapInfo mapInfo = itemStack.require(Keys.MAP_INFO);
            final Audience console = Sponge.getServer();
            console.sendMessage(Component.text("the mapdata contains: " + mapInfo.toContainer()));
            console.sendMessage(Component.text("the map contains nbt: " + itemStack.toContainer()));
            //player.sendMessage(Text.of("the map contains vanilla nbt: " + itemStack.get().toContainer().get(DataQuery.of("UnsafeData")).get()));
            return CommandResult.success();
        }), event);

        this.createDefaultCommand("setmapnether", ctx -> {
            Audience audience = ctx.getCause().getAudience();
            final Player player = this.requirePlayer(ctx);
            final ItemStack map = player.getItemInHand(HandTypes.MAIN_HAND);
            if (map.getType() != ItemTypes.FILLED_MAP.get()) {
                throw new CommandException(Component.text("You must hold a map in your hand"));
            }
            final ResourceKey netherKey = ResourceKey.minecraft("the_nether");
            Optional<ServerWorld> nether = Sponge.getServer().getWorldManager().getWorld(netherKey);
            if (!nether.isPresent()) {
                final CompletableFuture<ServerWorld> loadedNether = Sponge.getServer().getWorldManager().loadWorld(netherKey);
                loadedNether.whenComplete((v, e) -> {
                    if (e != null) {
                        audience.sendMessage(Component.text("Failed to load nether world!", NamedTextColor.GREEN));
                        logger.error("Error loading nether world!", e);
                    }
                    else {
                        audience.sendMessage(Component.text("Loaded nether world (dim-1)", NamedTextColor.GREEN));
                    }

                });
                throw new CommandException(Component.text("No nether loaded, trying to load now, please wait"));
            }
            map.require(Keys.MAP_INFO).offer(Keys.MAP_WORLD, nether.get());
            return CommandResult.success();
        }, event);

        this.createDefaultCommand("setcolorandlocked", (ctx -> {
            final Player player = this.requirePlayer(ctx);
            final ItemStack map = player.getItemInHand(HandTypes.MAIN_HAND);
            if (map.getType() != ItemTypes.FILLED_MAP.get()) {
                throw new CommandException(Component.text("You must hold a map in your hand"));
            }
            //map.offer(Keys.MAP_LOCATION, new Vector2i(10000,10000));
            final MapColor color = MapColor.of(MapColorTypes.BLACK_TERRACOTTA);
            final MapInfo mapInfo = map.require(Keys.MAP_INFO);
            mapInfo.offer(Keys.MAP_LOCKED, true);
            mapInfo.offer(Keys.MAP_CANVAS, MapCanvas.builder().paintAll(color).build());
            player.sendMessage(Component.text(mapInfo.require(Keys.MAP_CANVAS).getColor(0,0).toContainer().toString()));
            return CommandResult.success();
        }), event);

        this.createDefaultCommand("downloadmap", ctx -> {
            final Player player = this.requirePlayer(ctx);
            final ItemStack map = player.getItemInHand(HandTypes.MAIN_HAND);
            if (map.getType() != ItemTypes.FILLED_MAP.get()) {
                throw new CommandException(Component.text("You must hold a map in your hand"));
            }
            final Image image = map.require(Keys.MAP_INFO).require(Keys.MAP_CANVAS).toImage();
            final File file = new File("map.png");
            if (!file.isFile()) {
                try {
                    if (!file.createNewFile()) {
                        throw new IOException("failed to create new file :(");
                    }
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                ImageIO.write((BufferedImage)image, "png", file);
            } catch (final IOException e) {
                e.printStackTrace();
            }
            return CommandResult.success();
        }, event);

        this.createDefaultCommand("createpalette", ctx -> {
            final File file = new File("map.png");
            try {
                if (!file.isFile()) {
                    file.createNewFile();
                }
                final MapCanvas.Builder builder = MapCanvas.builder();

                final List<MapColor[]> mapColors = new ArrayList<>();
                for (final MapColorType mapColorType : Sponge.getRegistry().getCatalogRegistry().getAllOf(MapColorType.class)) {
                    final MapColor[] colors = new MapColor[] {
                            MapColor.of(mapColorType),
                            MapColor.builder().baseColor(mapColorType).darkest().build(),
                            MapColor.builder().baseColor(mapColorType).base().build(),
                            MapColor.builder().baseColor(mapColorType).darkest().build()
                    };
                    mapColors.add(colors);
                }
                int x = 0;
                for (final MapColor[] colors : mapColors) {
                    int y = 0;
                    builder.paint(x, y, x, y, colors[0]);
                    y++;
                    builder.paint(x, y, x, y, colors[1]);
                    y++;
                    builder.paint(x, y, x, y, colors[2]);
                    y++;
                    builder.paint(x, y, x, y, colors[3]);
                    x++;
                }
                final MapCanvas canvas = builder.build();
                final Color color = new Color(0,0,0,0);
                ImageIO.write((BufferedImage) canvas.toImage(color),"png", file);
            } catch (final IOException e) {
                Sponge.getServer().sendMessage(Component.text("IOException"));
            }
            return CommandResult.success();
        }, event);

        this.createDefaultCommand("setmapfromimage", ctx -> {
            final Player player = this.requirePlayer(ctx);
            final ItemStack map = player.getItemInHand(HandTypes.MAIN_HAND);
            if (map.getType() != ItemTypes.FILLED_MAP.get()) {
                throw new CommandException(Component.text("You must hold a map in your hand"));
            }
            final File file = new File("map.png");
            if (!file.isFile()) {
                try {
                    if (!file.createNewFile()) {
                        throw new IOException("failed to create new file :(");
                    }
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }
            final BufferedImage image;
            try {
                image = ImageIO.read(file);
            } catch (final IOException e) {
                throw new CommandException(Component.text(e.getMessage()), e);
            }
            final MapCanvas canvas = MapCanvas.builder()
                    .fromImage(image)
                    .build();
            final MapInfo mapInfo = map.require(Keys.MAP_INFO);
            mapInfo.offer(Keys.MAP_LOCKED, true);
            mapInfo.offer(Keys.MAP_CANVAS, canvas);
            return CommandResult.success();
        }, event);

        this.createDefaultCommand("listmaps", ctx -> {
            final Collection<MapInfo> mapInfos = Sponge.getServer().getMapStorage().getAllMapInfos();
            final Audience console = Sponge.getServer();
            console.sendMessage(Component.text(mapInfos.size()));
            final List<MapInfo> list = new ArrayList<>(mapInfos);
            list.sort(Comparator.comparingInt(info -> info.toContainer().getInt(DataQuery.of("UnsafeMapId")).get()));
            for (final MapInfo mapInfo : list) {
                console.sendMessage(Component.text("id: " + mapInfo.toContainer().getInt(DataQuery.of("UnsafeMapId")).get() + " loc: " + mapInfo.get(Keys.MAP_LOCATION).get()));
            }
            return CommandResult.success();
        }, event);

        this.createDefaultCommand("adddecoration", ctx -> {
            final Player player = this.requirePlayer(ctx);
            final ItemStack heldMap = player.getItemInHand(HandTypes.MAIN_HAND);
            if (heldMap.getType() != ItemTypes.FILLED_MAP.get()) {
                throw new CommandException(Component.text("You must hold a map in your hand"));
            }
            final MapInfo mapInfo = heldMap.require(Keys.MAP_INFO);
            mapInfo.offer(Keys.MAP_TRACKS_PLAYERS, true);
            final Set<MapDecoration> decorations = new HashSet<>();
            int x = Byte.MIN_VALUE;
            int y = Byte.MIN_VALUE;

            for (final MapDecorationOrientation dir : Sponge.getRegistry().getCatalogRegistry().getAllOf(MapDecorationOrientation.class)) {
                decorations.add(
                        MapDecoration.builder()
                                .type(MapDecorationTypes.RED_MARKER)
                                .rotation(dir)
                                .position(Vector2i.from(x, y))
                                .build());
                player.sendMessage(Component.text("rotation: " + dir.toString()));
                player.sendMessage(Component.text("x: " + x));
                player.sendMessage(Component.text("y: " + y));
                x += 16;
                if (x > Byte.MAX_VALUE) {
                    y += 16;
                    x = Byte.MIN_VALUE;
                    if (y > Byte.MAX_VALUE) {
                        player.sendMessage(Component.text("out of room, stopping"));
                        mapInfo.offer(Keys.MAP_DECORATIONS, decorations);
                        return CommandResult.success();
                    }
                }
            }
            mapInfo.offer(Keys.MAP_DECORATIONS, decorations);
            return CommandResult.success();
        }, event);

        this.createDefaultCommand("setalldirectionsdown", ctx -> {
            final Player player = this.requirePlayer(ctx);
            final ItemStack heldMap = player.getItemInHand(HandTypes.MAIN_HAND);
            if (heldMap.getType() != ItemTypes.FILLED_MAP.get()) {
                throw new CommandException(Component.text("You must hold a map in your hand"));
            }
            heldMap.require(Keys.MAP_INFO).require(Keys.MAP_DECORATIONS).forEach(decoration -> decoration.setRotation(MapDecorationOrientations.SOUTH));
            return CommandResult.success();
        }, event);

        this.createDefaultCommand("getmapuuid", ctx -> {
            final Player player = this.requirePlayer(ctx);
            final ItemStack map = player.getItemInHand(HandTypes.MAIN_HAND);
            if (map.getType() != ItemTypes.FILLED_MAP.get()) {
                throw new CommandException(Component.text("You must hold a map in your hand!"));
            }
            final Component uuid = Component.text(map.require(Keys.MAP_INFO).getUniqueId().toString());
            player.sendMessage(uuid);
            this.logger.info("map uuid: " + uuid);
            return CommandResult.success();
        }, event);

        this.createDefaultCommand("testmapserialization", ctx -> {
            MapInfo mapInfo = null;
            Audience audience = ctx.getCause().getAudience();
            if (audience instanceof Player) {
                final Player player = (Player) audience;
                mapInfo = player.getItemInHand(HandTypes.MAIN_HAND).get(Keys.MAP_INFO)
                        .orElse(null);
            }
            final MapStorage mapStorage = Sponge.getServer().getMapStorage();
            if (mapInfo == null) {
                mapInfo = mapStorage.getAllMapInfos()
                        .stream().findAny()
                        .orElse(mapStorage.createNewMapInfo().get());
            }

            final DataView mapInfoView = blankMapDecorationIds(mapInfo.toContainer());
            mapInfoView.set(DataQuery.of("MapData", "MapLocked"), true)
                    .set(DataQuery.of("UnsafeMapId"), 10);

            // TODO: no .setRawData in api-8?
            /*
            mapInfo.setRawData(mapInfoView);
            final DataView mapInfoViewAfter = blankMapDecorationIds(mapInfo.toContainer());
            // We change the decoration from things such as a player or frame to a custom decoration, meaning copied decorations will actually persist,
            // instead of instantly disappearing if the player moves or the location of the new mapinfo is different.
            // It acts more like a snapshot.
            checkSerialization(ctx, "MapInfo", mapInfoView.toString(), mapInfoViewAfter.toString());

            MapColor mapColor = MapColor.builder().baseColor(MapColorTypes.BLUE).build();
            final DataView mapColorView = mapColor.toContainer();
            checkSerialization(ctx, "MapColor", mapColorView.toString(),  MapColor.builder().fromContainer(mapColorView).build().toContainer().toString());

            MapCanvas mapCanvas = MapCanvas.builder().paintAll(MapColor.of(MapColorTypes.GREEN)).build();
            checkSerialization(ctx, "MapCanvas", mapCanvas.toContainer().toString(),
                    MapCanvas.builder().fromContainer(mapCanvas.toContainer()).build().toContainer().toString());

            MapDecoration mapDecoration = MapDecoration.builder().type(MapDecorationTypes.BLUE_MARKER).rotation(MapDecorationOrientations.WEST).build();
            final DataView mapDecorationView = mapDecoration.toContainer();
            final DataView mapDecorationViewAfter = MapDecoration.builder().fromContainer(mapDecorationView).build().toContainer();

            mapDecorationView.set(DataQuery.of("id"), "fakeid");
            mapDecorationViewAfter.set(DataQuery.of("id"), "fakeid"); // Ignore ids for comparison

            checkSerialization(ctx, "MapDecoration", mapDecorationView.toString(), mapDecorationViewAfter.toString());*/

            return CommandResult.success();
        }, event);

        this.createDefaultCommand("testpluginmapcreation", ctx -> {
            final Player player = this.requirePlayer(ctx);
            final MapInfo mapInfo = Sponge.getServer().getMapStorage()
                    .createNewMapInfo()
                    .orElseThrow(() -> new CommandException(Component.text("Map creation was cancelled!")));
            final ItemStack itemStack = ItemStack.of(ItemTypes.FILLED_MAP, 1);
            final MapCanvas canvas = MapCanvas.builder()
                    .paintAll(MapColor.of(MapColorTypes.RED))
                    .build();
            mapInfo.offer(Keys.MAP_CANVAS, canvas);
            mapInfo.offer(Keys.MAP_LOCKED, true);
            mapInfo.offer(Keys.MAP_LOCATION, player.getPosition().toInt().toVector2(true));
            itemStack.offer(Keys.MAP_INFO, mapInfo);
            player.getInventory().offer(itemStack);
            return CommandResult.success();
        }, event);

        final Parameter.Value<UUID> uuidParameter = Parameter.uuid().setKey("uuid").build();

        event.register(this.container, Command.builder()
                .parameter(Parameter.uuid().setKey("uuid").build())
                .setExecutor(ctx -> {
                    final Player player = this.requirePlayer(ctx);
                    final UUID uuid = ctx.getOne(uuidParameter).get();
                    final ItemStack itemStack = ItemStack.of(ItemTypes.FILLED_MAP, 1);
                    final MapInfo mapInfo = Sponge.getServer().getMapStorage().getMapInfo(uuid)
                            .orElseThrow(() -> new CommandException(Component.text("UUID " + uuid + " was not a valid map uuid!")));
                    itemStack.offer(Keys.MAP_INFO, mapInfo);
                    player.getInventory().offer(itemStack);
                    return CommandResult.success();
                })
                .build(),
                "getmapfromuuid");

        this.createDefaultCommand("testmapshades", ctx -> {
            final Player player = this.requirePlayer(ctx);
            for (MapShade shade : Sponge.getRegistry().getCatalogRegistry().getAllOf(MapShade.class)) {
                final MapColor mapColor = MapColor.of(MapColorTypes.GREEN.get(), shade);
                final MapCanvas mapCanvas = MapCanvas.builder().paintAll(mapColor).build();
                final MapInfo mapInfo = Sponge.getServer().getMapStorage()
                        .createNewMapInfo()
                        .orElseThrow(() -> new CommandException(Component.text("Unable to create new map!")));
                mapInfo.offer(Keys.MAP_LOCKED, true);
                mapInfo.offer(Keys.MAP_CANVAS, mapCanvas);
                ItemStack itemStack = ItemStack.of(ItemTypes.FILLED_MAP);
                itemStack.offer(Keys.MAP_INFO, mapInfo);
                itemStack.offer(Keys.DISPLAY_NAME, Component.text(shade.getKey().getFormatted()));

                player.getInventory().getPrimary().offer(itemStack);
            }
            return CommandResult.success();
        }, event);

        this.createDefaultCommand("enableunlimitedtracking", ctx -> {
            final Player player = this.requirePlayer(ctx);
            final ItemStack heldMap = player.getItemInHand(HandTypes.MAIN_HAND);

            if (heldMap.getType() != ItemTypes.FILLED_MAP.get()) {
                throw new CommandException(Component.text("You must hold a map in your hand"));
            }
            heldMap.require(Keys.MAP_INFO).offer(Keys.MAP_UNLIMITED_TRACKING, true);
            return CommandResult.success();
        }, event);
    }

    private void checkSerialization(CommandContext ctx, String testName, String expected, String after) {
        final Audience audience = ctx.getCause().getAudience();
        boolean success = expected.equals(after);
        Component text = Component.text("Test of ").append(Component.text(testName, NamedTextColor.BLUE))
                .append(success ? Component.text(" SUCCEEDED", NamedTextColor.GREEN) : Component.text(" FAILED", NamedTextColor.RED));
        audience.sendMessage(text);
        if (!success) {
            logger.info(testName + " Expected: " + expected);
            logger.info(testName + " Real: " + after);
        }
    }

    private DataView blankMapDecorationIds(final DataView dataView) {
        // Blank changes to id since we always change when serializing them to stop conflicts.
        final DataQuery decorations = DataQuery.of("MapData", "Decorations");
        final List<DataView> newData = dataView.getViewList(decorations).get();
        newData.replaceAll(dataView1 -> dataView1.set(DataQuery.of("id"), "fakeid"));
        dataView.set(decorations, newData);
        return dataView;
    }

    /*@Listener
    public void onGameStart(GameStartedServerEvent e) {
        Sponge.getScheduler()
                .createSyncExecutor(this)
                .scheduleAtFixedRate(() -> Sponge.getServer().getPlayer("tyhdefu").ifPresent(player -> player.getItemInHand(HandTypes.MAIN_HAND).filter(itemStack -> itemStack.getType() == ItemTypes.FILLED_MAP)
                        .map(itemStack -> itemStack.require(Keys.MAP_INFO))
                        .map(mapInfo -> mapInfo.require(Keys.MAP_DECORATIONS))
                        .ifPresent(mapDecorations -> mapDecorations.forEach(dec -> player.sendMessage(Text.of(dec.toContainer()))))
                ), 0, 1, TimeUnit.SECONDS);
    }*/

    private Player requirePlayer(final CommandContext source) throws CommandException {
        Audience audience = source.getCause().getAudience();
        if (audience instanceof Player) {
            return (Player) audience;
        }
        throw new CommandException(Component.text("Must be called from player!"));
    }

    private void createDefaultCommand(final String name, final CommandExecutor executor, RegisterCommandEvent<Command.Parameterized> event) {
        final Command.Parameterized command = Command.builder()
                .setExecutor(executor)
                .build();

        event.register(this.container, command, name);
    }

    @Override
    public void enable(CommandContext ctx) {
        Audience audience = ctx.getCause().getAudience();
        if (!this.isEnabled) {
            this.isEnabled = true;
            Sponge.getEventManager().registerListeners(this.container, this.listeners);
            audience.sendMessage(Component.text("Map listeners are enabled. Created maps will now start blue.", NamedTextColor.GREEN));
        } else {
            audience.sendMessage(Component.text("Map listeners are already enabled.", NamedTextColor.YELLOW));
        }
    }

    public static class Listeners {

        @Listener
        public void onMapCreate(final CreateMapEvent event) {
            final MapInfo mapInfo = event.getMapInfo();
            mapInfo.offer(Keys.MAP_CANVAS, MapCanvas.builder()
                    .paint(0,0,127, 127,
                            MapColor.builder()
                                    .baseColor(MapColorTypes.BLUE.get())
                                    .build())
                    .build());
        }
    }

}
