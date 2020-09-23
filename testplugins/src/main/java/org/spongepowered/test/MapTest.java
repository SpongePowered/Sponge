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
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.action.CreateMapEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.map.MapCanvas;
import org.spongepowered.api.map.MapInfo;
import org.spongepowered.api.map.color.MapColor;
import org.spongepowered.api.map.color.MapColorType;
import org.spongepowered.api.map.color.MapColorTypes;
import org.spongepowered.api.map.decoration.MapDecoration;
import org.spongepowered.api.map.decoration.MapDecorationTypes;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.map.MapStorage;

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

@Plugin(
        id = "maptest",
        name = "Map Test"
)
public class MapTest implements LoadableModule {

    private final Logger logger;
    private final Listeners listeners;
    private boolean isEnabled = false;

    @Inject
    public MapTest(final Logger logger) {
        this.logger = logger;
        this.listeners = new Listeners();
    }

    @Listener
    public void onGameInit(final GameInitializationEvent event) {
        this.createDefaultCommand("getmapdata", ((src, args) -> {
            final Player player = this.requirePlayer(src);
            final Optional<ItemStack> itemStack = player.getItemInHand(HandTypes.MAIN_HAND);
            if (!itemStack.isPresent()) {
                throw new CommandException(Text.of("You must hold a map in your hand"));
            }
            if (itemStack.get().getType() != ItemTypes.FILLED_MAP) {
                throw new CommandException(Text.of("You must hold a map in your hand"));
            }
            final MapInfo mapInfo = itemStack.get().get(Keys.MAP_INFO).get();
            final ConsoleSource console = Sponge.getServer().getConsole();
            console.sendMessage(Text.of("the mapdata contains: " + mapInfo.toContainer()));
            console.sendMessage(Text.of("the map contains nbt: " + itemStack.get().toContainer()));
            //player.sendMessage(Text.of("the map contains vanilla nbt: " + itemStack.get().toContainer().get(DataQuery.of("UnsafeData")).get()));
            return CommandResult.success();
        }));

        this.createDefaultCommand("setmapnether", (src, args) -> {
            final Player player = this.requirePlayer(src);
            final Optional<ItemStack> heldMap = player.getItemInHand(HandTypes.MAIN_HAND).filter(itemStack -> itemStack.getType() == ItemTypes.FILLED_MAP);
            if (!heldMap.isPresent()) {
                throw new CommandException(Text.of("You must hold a map in your hand"));
            }
            Optional<World> nether = Sponge.getServer().getWorld("DIM-1");
            if (!nether.isPresent()) {
                final Optional<World> loadedNether = Sponge.getServer().loadWorld("DIM-1");
                if (loadedNether.isPresent()) {
                    Text.of(TextColors.GREEN, "Loaded nether world (DIM-1)");
                }
                else {
                    throw new CommandException(Text.of("Nether was not loaded and could not be loaded (DIM-1)"));
                }
                nether = loadedNether;
            }
            heldMap.get().require(Keys.MAP_INFO).offer(Keys.MAP_WORLD, nether.get());
            return CommandResult.success();
        });

        this.createDefaultCommand("setcolorandlocked", ((src, args) -> {
            final Player player = this.requirePlayer(src);
            final Optional<ItemStack> heldMap = player.getItemInHand(HandTypes.MAIN_HAND).filter(itemStack -> itemStack.getType() == ItemTypes.FILLED_MAP);
            if (!heldMap.isPresent()) {
                throw new CommandException(Text.of("You must hold a map in your hand"));
            }
            final ItemStack map = heldMap.get();
            //map.offer(Keys.MAP_LOCATION, new Vector2i(10000,10000));
            final MapColor color = MapColor.of(MapColorTypes.BLACK_STAINED_HARDENED_CLAY);
            final MapInfo mapInfo = map.require(Keys.MAP_INFO);
            mapInfo.offer(Keys.MAP_LOCKED, true);
            mapInfo.offer(Keys.MAP_CANVAS, MapCanvas.builder().paintAll(color).build());
            player.sendMessage(Text.of(mapInfo.require(Keys.MAP_CANVAS).getColor(0,0)));
            return CommandResult.success();
        }));

        this.createDefaultCommand("downloadmap", (src, args) -> {
            final Player player = this.requirePlayer(src);
            final Optional<ItemStack> heldMap = player.getItemInHand(HandTypes.MAIN_HAND).filter(itemStack -> itemStack.getType() == ItemTypes.FILLED_MAP);
            if (!heldMap.isPresent()) {
                throw new CommandException(Text.of("You must hold a map in your hand"));
            }
            final ItemStack map = heldMap.get();
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
        });

        this.createDefaultCommand("createpalette", (src, args) -> {
            final File file = new File("map.png");
            try {
                if (!file.isFile()) {
                    file.createNewFile();
                }
                final MapCanvas.Builder builder = MapCanvas.builder();

                final List<MapColor[]> mapColors = new ArrayList<>();
                for (final MapColorType mapColorType : Sponge.getRegistry().getAllOf(MapColorType.class)) {
                    final MapColor[] colors = new MapColor[] {
                            MapColor.of(mapColorType),
                            MapColor.builder().baseColor(mapColorType).light().build(),
                            MapColor.builder().baseColor(mapColorType).base().build(),
                            MapColor.builder().baseColor(mapColorType).dark().build()
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
                Sponge.getServer().getConsole().sendMessage(Text.of("IOException"));
            }
            return CommandResult.success();
        });

        this.createDefaultCommand("setmapfromimage", (src, args) -> {
            final Player player = this.requirePlayer(src);
            final Optional<ItemStack> heldMap = player.getItemInHand(HandTypes.MAIN_HAND).filter(itemStack -> itemStack.getType() == ItemTypes.FILLED_MAP);
            if (!heldMap.isPresent()) {
                throw new CommandException(Text.of("You must hold a map in your hand"));
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
                throw new CommandException(Text.of(e.getMessage()), e);
            }
            final ItemStack map = heldMap.get();
            final MapCanvas canvas = MapCanvas.builder()
                    .fromImage(image)
                    .build();
            final MapInfo mapInfo = map.require(Keys.MAP_INFO);
            mapInfo.offer(Keys.MAP_LOCKED, true);
            mapInfo.offer(Keys.MAP_CANVAS, canvas);
            return CommandResult.success();
        });

        this.createDefaultCommand("listmaps", (src, args) -> {
            final Collection<MapInfo> mapInfos = Sponge.getServer().getMapStorage()
                    .map(MapStorage::getAllMapInfos)
                    .get();
            final ConsoleSource console = Sponge.getServer().getConsole();
            console.sendMessage(Text.of(mapInfos.size()));
            final List<MapInfo> list = new ArrayList<>(mapInfos);
            list.sort(Comparator.comparingInt(info -> info.toContainer().getInt(DataQuery.of("UnsafeMapId")).get()));
            for (final MapInfo mapInfo : list) {
                console.sendMessage(Text.of("id: " + mapInfo.toContainer().getInt(DataQuery.of("UnsafeMapId")).get() + " loc: " + mapInfo.get(Keys.MAP_LOCATION).get()));
            }
            return CommandResult.success();
        });

        this.createDefaultCommand("adddecoration", (src, args) -> {
            final Player player = this.requirePlayer(src);
            final Optional<ItemStack> heldMap = player.getItemInHand(HandTypes.MAIN_HAND).filter(itemStack -> itemStack.getType() == ItemTypes.FILLED_MAP);
            if (!heldMap.isPresent()) {
                throw new CommandException(Text.of("You must hold a map in your hand"));
            }
            final MapInfo mapInfo = heldMap.get().require(Keys.MAP_INFO);
            mapInfo.offer(Keys.MAP_TRACKS_PLAYERS, true);
            final Set<MapDecoration> decorations = new HashSet<>();
            int x = Byte.MIN_VALUE;
            int y = Byte.MIN_VALUE;
            final Direction[] dirs = new Direction[] {
                    Direction.NORTH, Direction.NORTH_NORTHEAST, Direction.NORTHEAST,
                    Direction.EAST_NORTHEAST, Direction.EAST, Direction.EAST_SOUTHEAST, Direction.SOUTHEAST,
                    Direction.SOUTH_SOUTHEAST, Direction.SOUTH, Direction.SOUTH_SOUTHWEST, Direction.SOUTHWEST,
                    Direction.WEST_SOUTHWEST, Direction.WEST, Direction.WEST_NORTHWEST,
                    Direction.NORTHWEST, Direction.NORTH_NORTHWEST
            };
            for (final Direction dir : dirs) {
                decorations.add(
                        MapDecoration.builder()
                                .type(MapDecorationTypes.RED_MARKER)
                                .rotation(dir)
                                .x(x)
                                .y(y)
                                .build());
                src.sendMessage(Text.of("rotation: " + dir.toString()));
                src.sendMessage(Text.of("x: " + x));
                src.sendMessage(Text.of("y: " + y));
                x += 16;
                if (x > Byte.MAX_VALUE) {
                    y += 16;
                    x = Byte.MIN_VALUE;
                    if (y > Byte.MAX_VALUE) {
                        src.sendMessage(Text.of("out of room, stopping"));
                        mapInfo.offer(Keys.MAP_DECORATIONS, decorations);
                        return CommandResult.success();
                    }
                }
            }
            mapInfo.offer(Keys.MAP_DECORATIONS, decorations);
            return CommandResult.success();
        });

        this.createDefaultCommand("setalldirectionsdown", (src, args) -> {
            final Player player = this.requirePlayer(src);
            final Optional<ItemStack> heldMap = player.getItemInHand(HandTypes.MAIN_HAND).filter(itemStack -> itemStack.getType() == ItemTypes.FILLED_MAP);
            if (!heldMap.isPresent()) {
                throw new CommandException(Text.of("You must hold a map in your hand"));
            }
            heldMap.get().require(Keys.MAP_INFO).require(Keys.MAP_DECORATIONS).forEach(decoration -> decoration.setRotation(Direction.SOUTH));
            return CommandResult.success();
        });

        this.createDefaultCommand("getmapuuid", (src, args) -> {
            final Player player = this.requirePlayer(src);
            final Optional<ItemStack> map = player.getItemInHand(HandTypes.MAIN_HAND).filter(itemStack -> itemStack.getType() == ItemTypes.FILLED_MAP);
            if (!map.isPresent()) {
                throw new CommandException(Text.of("You must hold a map in your hand!"));
            }
            final Text uuid = Text.of(map.get().require(Keys.MAP_INFO).getUniqueId().toString());
            src.sendMessage(uuid);
            this.logger.info("map uuid: " + uuid);
            return CommandResult.success();
        });

        this.createDefaultCommand("testmapserialization", (src, args) -> {
            MapInfo mapInfo = null;
            if (src instanceof Player) {
                final Player player = (Player) src;
                mapInfo = player.getItemInHand(HandTypes.MAIN_HAND).filter(item -> item.getType() == ItemTypes.FILLED_MAP)
                        .map(item -> item.require(Keys.MAP_INFO))
                        .orElse(null);
            }
            final MapStorage mapStorage = Sponge.getServer().getMapStorage().get();
            if (mapInfo == null) {
                mapInfo = mapStorage.getAllMapInfos()
                        .stream().findAny()
                        .orElse(mapStorage.createNewMapInfo().get());
            }

            final DataView dataView = mapInfo.toContainer();
            this.logger.info("before: " + dataView);
            dataView.set(DataQuery.of("MapData").then(Keys.MAP_LOCKED.getQuery()), true)
                    .set(DataQuery.of("UnsafeMapId"), 10);
            this.logger.info("setting to: " + dataView);
            mapInfo.setRawData(dataView);
            this.logger.info("after: " + mapInfo.toContainer());
            return CommandResult.success();
        });

        this.createDefaultCommand("testpluginmapcreation", (src, args) -> {
            final Player player = this.requirePlayer(src);
            final MapInfo mapInfo = Sponge.getServer().getMapStorage()
                    .orElseThrow(() -> new CommandException(Text.of("MapStorage was not available")))
                    .createNewMapInfo()
                    .orElseThrow(() -> new CommandException(Text.of("Map creation was cancelled!")));
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
        });

        Sponge.getCommandManager().register(this, CommandSpec.builder()
                .arguments(GenericArguments.uuid(Text.of("uuid")))
                .executor((src, args) -> {
                    final Player player = this.requirePlayer(src);
                    final UUID uuid = args.<UUID>getOne(Text.of("uuid")).get();
                    final ItemStack itemStack = ItemStack.of(ItemTypes.FILLED_MAP, 1);
                    final MapInfo mapInfo = Sponge.getServer().getMapStorage().get().getMapInfo(uuid)
                            .orElseThrow(() -> new CommandException(Text.of("UUID " + uuid + " was not a valid map uuid!")));
                    itemStack.offer(Keys.MAP_INFO, mapInfo);
                    player.getInventory().offer(itemStack);
                    return CommandResult.success();
                })
                .build(), "getmapfromuuid");
    }

    @Override
    public void enable(final CommandSource src) {
        if (!this.isEnabled) {
            this.isEnabled = true;
            Sponge.getEventManager().registerListeners(this, this.listeners);
            src.sendMessage(Text.of(TextColors.GREEN, "Map listeners are enabled. Created maps will now start blue."));
        } else {
            src.sendMessage(Text.of(TextColors.YELLOW, "Map listeners are already enabled."));
        }
    }

    @Override
    public void disable(final CommandSource src) {
        LoadableModule.super.disable(src);
        this.isEnabled = false;
        src.sendMessage(Text.of(TextColors.RED, "Map listeners are disabled. Created maps will no start blue."));
    }

    private Player requirePlayer(final CommandSource source) throws CommandException {
        if (source instanceof Player) {
            return (Player) source;
        }
        throw new CommandException(Text.of("Must be called from player!"));
    }

    private void createDefaultCommand(final String name, final CommandExecutor executor) {
        final CommandSpec spec = CommandSpec.builder()
                .executor(executor)
                .build();

        Sponge.getCommandManager().register(this, spec, name);
    }

    public static class Listeners {

        @Listener
        public void onMapCreate(final CreateMapEvent event) {
            final MapInfo mapInfo = event.getMapInfo();
            mapInfo.offer(Keys.MAP_CANVAS, MapCanvas.builder()
                    .paint(0,0,127, 127,
                            MapColor.builder()
                                    .baseColor(MapColorTypes.BLUE)
                                    .build())
                    .build());
        }
    }

}
