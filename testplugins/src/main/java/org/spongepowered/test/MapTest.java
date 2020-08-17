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
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.action.CreateMapEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.world.ExplosionEvent;
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
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Plugin(
        id = "maptest",
        name = "Map Test"
)
public class MapTest {

    @Inject
    private Logger logger;

    public static boolean blueMapsEnabled = false;

    @Listener
    public void onServerStart(GameStartedServerEvent event) {
        createDefaultCommand("getmapdata", new GetMapData());
        createDefaultCommand("setmapnether", new SetMapNether());
        createDefaultCommand("setcolorandlocked", new SetColorAndLocked());
        createDefaultCommand("downloadmap", new DownloadMap());
        createDefaultCommand("createpalette", new CreatePaletteMap());
        createDefaultCommand("setmapfromimage", new SetMapFromImage());
        createDefaultCommand("listmaps", new ListMaps());
        createDefaultCommand("adddecoration", new AddRedDecorations());
        createDefaultCommand("setalldirectionsdown", new SetAllDecorationsFacingDown());
        createDefaultCommand("togglebluemaps", new ToggleBlueMaps());
    }

    @Listener
    public void onTestEvent(ExplosionEvent.Detonate event) {
        event.setCancelled(true);
    }

    public void createDefaultCommand(String name, CommandExecutor executor) {
        CommandSpec spec = CommandSpec.builder()
                .executor(executor)
                .build();

        Sponge.getCommandManager().register(this, spec, name);
    }

    @Listener
    public void onMapCreate(CreateMapEvent event) {
        if (!blueMapsEnabled) {
            return;
        }
        MapInfo mapInfo = event.getMapInfo();
        mapInfo.offer(Keys.MAP_CANVAS, MapCanvas.builder()
                .paint(0,0,127, 127,
                        MapColor.builder()
                                .baseColor(MapColorTypes.BLUE)
                                .build())
                .build());
    }

    public static class GetMapData implements CommandExecutor {

        @Override
        public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
            if (!(src instanceof Player)) {
                throw new CommandException(Text.of("Must be called from a player!"));
            }
            Player player = (Player)src;
            Optional<ItemStack> itemStack = player.getItemInHand(HandTypes.MAIN_HAND);
            if (!itemStack.isPresent()) {
                throw new CommandException(Text.of("You must hold a map in your hand"));
            }
            if (itemStack.get().getType() != ItemTypes.FILLED_MAP) {
                throw new CommandException(Text.of("You must hold a map in your hand"));
            }
            MapInfo mapInfo = itemStack.get().get(Keys.MAP_INFO).get();
            ConsoleSource console = Sponge.getServer().getConsole();
            console.sendMessage(Text.of("the mapdata contains: " + mapInfo.toContainer()));
            console.sendMessage(Text.of("the map contains nbt: " + itemStack.get().toContainer()));
            //player.sendMessage(Text.of("the map contains vanilla nbt: " + itemStack.get().toContainer().get(DataQuery.of("UnsafeData")).get()));
            return CommandResult.success();
        }
    }

    public static class SetMapNether implements CommandExecutor {

        @Override
        public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
            if (!(src instanceof Player)) {
                throw new CommandException(Text.of("Must be called from a player!"));
            }
            Player player = (Player)src;
            Optional<ItemStack> heldMap = player.getItemInHand(HandTypes.MAIN_HAND).filter(itemStack -> itemStack.getType() == ItemTypes.FILLED_MAP);
            if (!heldMap.isPresent()) {
                throw new CommandException(Text.of("You must hold a map in your hand"));
            }
            Optional<World> nether = Sponge.getServer().getWorld("DIM-1");
            if (!nether.isPresent()) {
                Optional<World> loadedNether = Sponge.getServer().loadWorld("DIM-1");
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
        }
    }

    public static class SetColorAndLocked implements CommandExecutor {

        @Override
        public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
            if (!(src instanceof Player)) {
                throw new CommandException(Text.of("Must be called from player!"));
            }
            Player player = (Player)src;
            Optional<ItemStack> heldMap = player.getItemInHand(HandTypes.MAIN_HAND).filter(itemStack -> itemStack.getType() == ItemTypes.FILLED_MAP);
            if (!heldMap.isPresent()) {
                throw new CommandException(Text.of("You must hold a map in your hand"));
            }
            ItemStack map = heldMap.get();
            //map.offer(Keys.MAP_LOCATION, new Vector2i(10000,10000));
            MapColor color = MapColor.of(MapColorTypes.BLACK_STAINED_HARDENED_CLAY);
            MapInfo mapInfo = map.require(Keys.MAP_INFO);
            mapInfo.offer(Keys.MAP_LOCKED, true);
            mapInfo.offer(Keys.MAP_CANVAS, MapCanvas.builder().paintAll(color).build());
            player.sendMessage(Text.of(mapInfo.require(Keys.MAP_CANVAS).getColor(0,0)));
            return CommandResult.success();
        }
    }

    public static class DownloadMap implements CommandExecutor {

        @Override
        public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
            if (!(src instanceof Player)) {
                throw new CommandException(Text.of("Must be called from player!"));
            }
            Player player = (Player)src;
            Optional<ItemStack> heldMap = player.getItemInHand(HandTypes.MAIN_HAND).filter(itemStack -> itemStack.getType() == ItemTypes.FILLED_MAP);
            if (!heldMap.isPresent()) {
                throw new CommandException(Text.of("You must hold a map in your hand"));
            }
            ItemStack map = heldMap.get();
            Image image = map.require(Keys.MAP_INFO).require(Keys.MAP_CANVAS).toImage();
            File file = new File("map.png");
            if (!file.isFile()) {
                try {
                    if (!file.createNewFile()) {
                        throw new IOException("failed to create new file :(");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                ImageIO.write((BufferedImage)image, "png", file);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return CommandResult.success();
        }
    }

    public static class CreatePaletteMap implements CommandExecutor {

        @Override
        public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
            File file = new File("map.png");
            try {
                if (!file.isFile()) {
                    file.createNewFile();
                }
                MapCanvas.Builder builder = MapCanvas.builder();

                List<MapColor[]> mapColors = new ArrayList<>();
                for (MapColorType mapColorType : Sponge.getRegistry().getAllOf(MapColorType.class)) {
                    MapColor[] colors = new MapColor[] {
                            MapColor.of(mapColorType),
                            MapColor.builder().baseColor(mapColorType).dark().build(),
                            MapColor.builder().baseColor(mapColorType).darker().build(),
                            MapColor.builder().baseColor(mapColorType).darkest().build()
                    };
                    mapColors.add(colors);
                }
                int x = 0;
                for (MapColor[] colors : mapColors) {
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
                MapCanvas canvas = builder.build();
                Color color = new Color(0,0,0,0);
                ImageIO.write((BufferedImage)canvas.toImage(color),"png",file);
            } catch (IOException e) {
                Sponge.getServer().getConsole().sendMessage(Text.of("IOException"));
            }
            return CommandResult.success();
        }
    }

    public static class SetMapFromImage implements CommandExecutor {

        @Override
        public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
            if (!(src instanceof Player)) {
                throw new CommandException(Text.of("Must be called from player!"));
            }
            Player player = (Player)src;
            Optional<ItemStack> heldMap = player.getItemInHand(HandTypes.MAIN_HAND).filter(itemStack -> itemStack.getType() == ItemTypes.FILLED_MAP);
            if (!heldMap.isPresent()) {
                throw new CommandException(Text.of("You must hold a map in your hand"));
            }
            File file = new File("map.png");
            if (!file.isFile()) {
                try {
                    if (!file.createNewFile()) {
                        throw new IOException("failed to create new file :(");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            BufferedImage image = null;
            try {
                image = ImageIO.read(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
            ItemStack map = heldMap.get();
            MapCanvas canvas = MapCanvas.builder()
                    .fromImage(image)
                    .build();
            MapInfo mapInfo = map.require(Keys.MAP_INFO);
            mapInfo.offer(Keys.MAP_LOCKED, true);
            mapInfo.offer(Keys.MAP_CANVAS, canvas);
            return CommandResult.success();
        }
    }

    public static class ListMaps implements CommandExecutor {

        @Override
        public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
            Set<MapInfo> mapInfos = Sponge.getServer().getMapStorage()
                    .map(MapStorage::getAllMapInfos)
                    .get();
            ConsoleSource console = Sponge.getServer().getConsole();
            console.sendMessage(Text.of(mapInfos.size()));
            for (MapInfo mapInfo : mapInfos) {
                console.sendMessage(Text.of(mapInfo.get(Keys.MAP_LOCATION).get()));
            }
            return CommandResult.success();
        }
    }

    public static class AddRedDecorations implements CommandExecutor {

        @Override
        public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
            if (!(src instanceof Player)) {
                throw new CommandException(Text.of("Command must be used from a player"));
            }
            Player player = (Player)src;
            Optional<ItemStack> heldMap = player.getItemInHand(HandTypes.MAIN_HAND).filter(itemStack -> itemStack.getType() == ItemTypes.FILLED_MAP);
            if (!heldMap.isPresent()) {
                throw new CommandException(Text.of("You must hold a map in your hand"));
            }
            MapInfo mapInfo = heldMap.get().require(Keys.MAP_INFO);
            mapInfo.offer(Keys.MAP_TRACKS_PLAYERS, true);
            Set<MapDecoration> decorations = new HashSet<>();
            int x = Byte.MIN_VALUE;
            int y = Byte.MIN_VALUE;
            Direction[] dirs = new Direction[] {
                    Direction.NORTH, Direction.NORTH_NORTHEAST, Direction.NORTHEAST,
                    Direction.EAST_NORTHEAST, Direction.EAST, Direction.EAST_SOUTHEAST, Direction.SOUTHEAST,
                    Direction.SOUTH_SOUTHEAST, Direction.SOUTH, Direction.SOUTH_SOUTHWEST, Direction.SOUTHWEST,
                    Direction.WEST_SOUTHWEST, Direction.WEST, Direction.WEST_NORTHWEST,
                    Direction.NORTHWEST, Direction.NORTH_NORTHWEST
            };
            for (int i = 0; i < dirs.length; i++) {
                decorations.add(
                        MapDecoration.builder()
                                .type(MapDecorationTypes.RED_MARKER)
                                .rotation(dirs[i])
                                .x(x)
                                .y(y)
                                .build());
                src.sendMessage(Text.of("rotation: " + dirs[i].toString()));
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
        }
    }

    public static class SetAllDecorationsFacingDown implements CommandExecutor {

        @Override
        public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
            if (!(src instanceof Player)) {
                throw new CommandException(Text.of("Command must be used from a player"));
            }
            Player player = (Player)src;
            Optional<ItemStack> heldMap = player.getItemInHand(HandTypes.MAIN_HAND).filter(itemStack -> itemStack.getType() == ItemTypes.FILLED_MAP);
            if (!heldMap.isPresent()) {
                throw new CommandException(Text.of("You must hold a map in your hand"));
            }
            heldMap.get().require(Keys.MAP_INFO).require(Keys.MAP_DECORATIONS).forEach(decoration -> decoration.setRotation(Direction.SOUTH));
            return CommandResult.success();
        }
    }

    public static class ToggleBlueMaps implements CommandExecutor {

        @Override
        public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
            blueMapsEnabled = !blueMapsEnabled;
            src.sendMessage(Text.builder().append(Text.of("Blue map creation is: "))
                    .append(blueMapsEnabled ? Text.of(TextColors.GREEN, "ON")
                            : Text.of(TextColors.RED, "OFF"))
                    .build());
            return CommandResult.success();
        }
    }
}
