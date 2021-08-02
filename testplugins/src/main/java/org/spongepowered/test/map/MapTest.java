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
package org.spongepowered.test.map;

import com.google.common.collect.Sets;
import com.google.inject.Inject;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.entity.Banner;
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
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.action.CreateMapEvent;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.filter.data.Supports;
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
import org.spongepowered.api.map.decoration.MapDecorationType;
import org.spongepowered.api.map.decoration.MapDecorationTypes;
import org.spongepowered.api.map.decoration.orientation.MapDecorationOrientation;
import org.spongepowered.api.map.decoration.orientation.MapDecorationOrientations;
import org.spongepowered.api.registry.RegistryEntry;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.util.blockray.RayTrace;
import org.spongepowered.api.util.blockray.RayTraceResult;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.math.vector.Vector2i;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.jvm.Plugin;
import org.spongepowered.test.LoadableModule;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Plugin("maptest")
public class MapTest implements LoadableModule {

    private final PluginContainer container;
    private final Logger logger;
    private final Listeners listeners;
    private boolean isEnabled = false;

    @Inject
    public MapTest(final Logger logger, final PluginContainer pluginContainer) {
        this.container = pluginContainer;
        this.logger = logger;
        this.listeners = new Listeners(logger);
    }

    @Listener
    public void onGameInit(final RegisterCommandEvent<Command.Parameterized> event) {
        final Command.Builder builder = Command.builder();
        builder.addChild(Command.builder().executor((this::printMapData)).build(), "printMapData");
        builder.addChild(Command.builder().executor((this::setMapNether)).build(), "setmapnether");
        builder.addChild(Command.builder().executor((this::setColorAndLocked)).build(), "setColorAndLocked");
        builder.addChild(Command.builder().executor((this::saveToFile)).build(), "saveMapToFile");
        builder.addChild(Command.builder().executor((this::savePallete)).build(), "savePalleteToFile");
        builder.addChild(Command.builder().executor((this::loadMapFromFile)).build(), "loadMapFromFile");
        builder.addChild(Command.builder().executor((this::listMaps)).build(), "listMaps");
        builder.addChild(Command.builder().executor((this::randomDecorations)).build(), "randomDecorations");
        builder.addChild(Command.builder().executor((this::orientDecorationsDown)).build(), "orientDecorationsDown");
        builder.addChild(Command.builder().executor((this::getMapUUID)).build(), "getmapuuid");
        builder.addChild(Command.builder().executor((this::testMapSerialization)).build(), "testmapserialization");
        builder.addChild(Command.builder().executor((this::create)).build(), "create");
        final Parameter.Value<UUID> uuidParameter = Parameter.uuid().key("uuid").build();
        builder.addChild(Command.builder().addParameter(Parameter.uuid().key("uuid").build()).executor(ctx -> {
            return this.getMapFromUUID(uuidParameter, ctx);
        }).build(), "getmapfromuuid");

        builder.addChild(Command.builder().executor((this::testMapShades)).build(), "testmapshades");
        builder.addChild(Command.builder().executor((this::enableUnlimitedTracking)).build(), "enableunlimitedtracking");
        builder.addChild(Command.builder().executor((this::addNamedDecoration)).build(), "addnameddecoration");
        builder.addChild(Command.builder().executor((this::addWorldBanner)).build(), "addworldbanner");
        builder.addChild(Command.builder().executor((this::recenterMap)).build(), "recentermap");
        builder.addChild(Command.builder().executor((this::setMapWorld)).build(), "setmapworld");

        event.register(this.container, builder.build(), "maptest");
    }

    private CommandResult getMapFromUUID(final Parameter.Value<UUID> uuidParameter, final CommandContext ctx) throws CommandException {
        final Player player = this.requirePlayer(ctx);
        final UUID uuid = ctx.one(uuidParameter).get();
        final ItemStack itemStack = ItemStack.of(ItemTypes.FILLED_MAP, 1);
        final MapInfo mapInfo = Sponge.server().mapStorage().mapInfo(uuid)
                .orElseThrow(() -> new CommandException(Component.text("UUID " + uuid + " was not a valid map uuid!")));
        itemStack.offer(Keys.MAP_INFO, mapInfo);
        player.inventory().offer(itemStack);
        return CommandResult.success();
    }

    private CommandResult setMapWorld(final CommandContext ctx) throws CommandException {
        final Player player = this.requirePlayer(ctx);
        final ItemStack heldMap = player.itemInHand(HandTypes.MAIN_HAND);
        if (heldMap.type() != ItemTypes.FILLED_MAP.get()) {
            throw new CommandException(Component.text("You must hold a map in your hand"));
        }
        final MapInfo mapInfo = heldMap.require(Keys.MAP_INFO);
        final ServerWorld serverWorld = (ServerWorld) player.location().world();
        mapInfo.offer(Keys.MAP_WORLD, serverWorld.key());
        player.sendMessage(Component.text("New map world: " + mapInfo.require(Keys.MAP_WORLD)));

        return CommandResult.success();
    }

    private CommandResult recenterMap(final CommandContext ctx) throws CommandException {
        final Player player = this.requirePlayer(ctx);
        final ItemStack heldMap = player.itemInHand(HandTypes.MAIN_HAND);
        if (heldMap.type() != ItemTypes.FILLED_MAP.get()) {
            throw new CommandException(Component.text("You must hold a map in your hand"));
        }
        final MapInfo mapInfo = heldMap.require(Keys.MAP_INFO);
        mapInfo.offer(Keys.MAP_LOCATION, player.location().blockPosition().toVector2(true));
        player.sendMessage(Component.text("New center " + mapInfo.require(Keys.MAP_LOCATION)));

        return CommandResult.success();
    }

    private CommandResult addWorldBanner(final CommandContext ctx) throws CommandException {
        final Player player = this.requirePlayer(ctx);
        final ItemStack heldMap = player.itemInHand(HandTypes.MAIN_HAND);
        if (heldMap.type() != ItemTypes.FILLED_MAP.get()) {
            throw new CommandException(Component.text("You must hold a map in your hand"));
        }
        final MapInfo mapInfo = heldMap.require(Keys.MAP_INFO);
        final RayTraceResult<LocatableBlock> hit = RayTrace.block()
                .sourcePosition(player)
                .direction(player)
                .world(player.serverLocation().world())
                .continueWhileBlock(RayTrace.onlyAir())
                .limit(100)
                .select(a -> a.location().blockEntity().filter(entity -> entity instanceof Banner).isPresent())
                .execute()
                .orElseThrow(() -> new CommandException(Component.text("You must look at a banner")));

        mapInfo.addBannerDecoration(hit.selectedObject().serverLocation());

        return CommandResult.success();
    }

    private CommandResult addNamedDecoration(final CommandContext ctx) throws CommandException {
        final Player player = this.requirePlayer(ctx);
        final ItemStack heldMap = player.itemInHand(HandTypes.MAIN_HAND);
        if (heldMap.type() != ItemTypes.FILLED_MAP.get()) {
            throw new CommandException(Component.text("You must hold a map in your hand"));
        }
        final MapDecoration decoration = MapDecoration.builder()
                .type(MapDecorationTypes.BANNER_BLUE)
                .customName(Component.text("I AM A ").color(NamedTextColor.BLUE).append(BlockTypes.BLUE_BANNER.get()))
                .rotation(MapDecorationOrientations.NORTH)
                .build();
        heldMap.require(Keys.MAP_INFO).offer(Keys.MAP_DECORATIONS, Sets.newHashSet(decoration));
        return CommandResult.success();
    }

    private CommandResult enableUnlimitedTracking(final CommandContext ctx) throws CommandException {
        final Player player = this.requirePlayer(ctx);
        final ItemStack heldMap = player.itemInHand(HandTypes.MAIN_HAND);

        if (heldMap.type() != ItemTypes.FILLED_MAP.get()) {
            throw new CommandException(Component.text("You must hold a map in your hand"));
        }
        heldMap.require(Keys.MAP_INFO).offer(Keys.MAP_UNLIMITED_TRACKING, true);
        return CommandResult.success();
    }

    private CommandResult testMapShades(final CommandContext ctx) throws CommandException {
        final Player player = this.requirePlayer(ctx);
        final Collection<RegistryEntry<MapShade>> mapShades = Sponge.game().registries()
                .registry(RegistryTypes.MAP_SHADE)
                .streamEntries()
                .collect(Collectors.toList());
        for (final RegistryEntry<MapShade> entry : mapShades) {
            final MapColor mapColor = MapColor.of(MapColorTypes.COLOR_GREEN.get(), entry.value());
            final MapCanvas mapCanvas = MapCanvas.builder().paintAll(mapColor).build();
            final MapInfo mapInfo = Sponge.server().mapStorage()
                    .createNewMapInfo()
                    .orElseThrow(() -> new CommandException(Component.text("Unable to create new map!")));
            mapInfo.offer(Keys.MAP_LOCKED, true);
            mapInfo.offer(Keys.MAP_CANVAS, mapCanvas);
            final ItemStack itemStack = ItemStack.of(ItemTypes.FILLED_MAP);
            itemStack.offer(Keys.MAP_INFO, mapInfo);
            itemStack.offer(Keys.CUSTOM_NAME, Component.text(entry.key().formatted()));

            player.inventory().primary().offer(itemStack);
        }
        return CommandResult.success();
    }

    private CommandResult create(final CommandContext ctx) throws CommandException {
        final Player player = this.requirePlayer(ctx);
        final MapInfo mapInfo = Sponge.server().mapStorage()
                .createNewMapInfo()
                .orElseThrow(() -> new CommandException(Component.text("Map creation was cancelled!")));
        final ItemStack itemStack = ItemStack.of(ItemTypes.FILLED_MAP, 1);
        final MapCanvas canvas = MapCanvas.builder()
                .paintAll(MapColor.of(MapColorTypes.COLOR_RED))
                .build();
        mapInfo.offer(Keys.MAP_CANVAS, canvas);
        mapInfo.offer(Keys.MAP_LOCKED, true);
        mapInfo.offer(Keys.MAP_LOCATION, player.position().toInt().toVector2(true));
        itemStack.offer(Keys.MAP_INFO, mapInfo);
        player.inventory().offer(itemStack);
        return CommandResult.success();
    }

    private CommandResult testMapSerialization(final CommandContext ctx) {
        MapInfo mapInfo = null;
        final Audience audience = ctx.cause().audience();
        if (audience instanceof Player) {
            final Player player = (Player) audience;
            mapInfo = player.itemInHand(HandTypes.MAIN_HAND).get(Keys.MAP_INFO)
                    .orElse(null);
        }
        final MapStorage mapStorage = Sponge.server().mapStorage();
        if (mapInfo == null) {
            mapInfo = mapStorage.allMapInfos()
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
    }

    private CommandResult getMapUUID(final CommandContext ctx) throws CommandException {
        final Player player = this.requirePlayer(ctx);
        final ItemStack map = player.itemInHand(HandTypes.MAIN_HAND);
        if (map.type() != ItemTypes.FILLED_MAP.get()) {
            throw new CommandException(Component.text("You must hold a map in your hand!"));
        }
        final Component uuid = Component.text(map.require(Keys.MAP_INFO).uniqueId().toString());
        player.sendMessage(uuid);
        this.logger.info("map uuid: " + uuid);
        return CommandResult.success();
    }

    private CommandResult orientDecorationsDown(final CommandContext ctx) throws CommandException {
        final Player player = this.requirePlayer(ctx);
        final ItemStack heldMap = player.itemInHand(HandTypes.MAIN_HAND);
        if (heldMap.type() != ItemTypes.FILLED_MAP.get()) {
            throw new CommandException(Component.text("You must hold a map in your hand"));
        }
        heldMap.require(Keys.MAP_INFO).require(Keys.MAP_DECORATIONS).forEach(decoration -> decoration.setRotation(MapDecorationOrientations.SOUTH));
        return CommandResult.success();
    }

    private CommandResult randomDecorations(final CommandContext ctx) throws CommandException {
        final Player player = this.requirePlayer(ctx);
        final ItemStack heldMap = player.itemInHand(HandTypes.MAIN_HAND);
        if (heldMap.type() != ItemTypes.FILLED_MAP.get()) {
            throw new CommandException(Component.text("You must hold a map in your hand"));
        }
        player.sendMessage(Component.text("Getting mapInfo"));
        final MapInfo mapInfo = heldMap.require(Keys.MAP_INFO);
        mapInfo.offer(Keys.MAP_TRACKS_PLAYERS, true);
        final Set<MapDecoration> decorations = new HashSet<>();
        int x = Byte.MIN_VALUE;
        int y = Byte.MIN_VALUE;

        final List<MapDecorationType> types = RegistryTypes.MAP_DECORATION_TYPE.get().stream().collect(Collectors.toList());
        final Collection<MapDecorationOrientation> orientations = Sponge.game().registries().registry(RegistryTypes.MAP_DECORATION_ORIENTATION).stream().collect(Collectors.toList());
        player.sendMessage(Component.text("Number of orientations: " + orientations.size()));
        player.sendMessage(Component.text("EAST: " + MapDecorationOrientations.EAST.get().key(RegistryTypes.MAP_DECORATION_ORIENTATION).toString()));
        for (final MapDecorationOrientation dir : orientations) {
            decorations.add(
                    MapDecoration.builder()
                            .type(types.get(player.random().nextInt(types.size())))
                            .rotation(dir)
                            .position(Vector2i.from(x, y))
                            .build());
            player.sendMessage(Component.text(dir.key(RegistryTypes.MAP_DECORATION_ORIENTATION).value()).append(Component.text("x: " + x)).append(Component.text("y: " + y)));
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
    }

    private CommandResult listMaps(final CommandContext ctx) {
        final Collection<MapInfo> mapInfos = Sponge.server().mapStorage().allMapInfos();
        ctx.sendMessage(Identity.nil(), Component.text(mapInfos.size()));
        final List<MapInfo> list = new ArrayList<>(mapInfos);
        list.sort(Comparator.comparingInt(info -> info.toContainer().getInt(DataQuery.of("UnsafeMapId")).get()));
        for (final MapInfo mapInfo : list) {
            ctx.sendMessage(Identity.nil(), Component.text("id: " + mapInfo.toContainer().getInt(DataQuery.of("UnsafeMapId")).get() + " loc: " + mapInfo.get(Keys.MAP_LOCATION).get()));
        }
        return CommandResult.success();
    }

    private CommandResult loadMapFromFile(final CommandContext ctx) throws CommandException {
        final Player player = this.requirePlayer(ctx);
        final ItemStack map = player.itemInHand(HandTypes.MAIN_HAND);
        if (map.type() != ItemTypes.FILLED_MAP.get()) {
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
        mapInfo.offer(Keys.MAP_TRACKS_PLAYERS, false);
        mapInfo.offer(Keys.MAP_DECORATIONS, Collections.emptySet());
        mapInfo.offer(Keys.MAP_LOCKED, true);
        mapInfo.offer(Keys.MAP_CANVAS, canvas);
        return CommandResult.success();
    }

    private CommandResult savePallete(final CommandContext ctx) {
        final File file = new File("pallete.png");
        try {
            if (!file.isFile()) {
                file.createNewFile();
            }
            final MapCanvas.Builder builder = MapCanvas.builder();

            final List<MapColor[]> mapColors = new ArrayList<>();
            for (final MapColorType mapColorType : Sponge.game().registries().registry(RegistryTypes.MAP_COLOR_TYPE).stream().collect(Collectors.toList())) {
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
            Sponge.server().sendMessage(Component.text("IOException"));
        }
        return CommandResult.success();
    }

    private CommandResult saveToFile(final CommandContext ctx) throws CommandException {
        final Player player = this.requirePlayer(ctx);
        final ItemStack map = player.itemInHand(HandTypes.MAIN_HAND);
        if (map.type() != ItemTypes.FILLED_MAP.get()) {
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
    }

    private CommandResult setColorAndLocked(final CommandContext ctx) throws CommandException {
        final Player player = this.requirePlayer(ctx);
        final ItemStack map = player.itemInHand(HandTypes.MAIN_HAND);
        if (map.type() != ItemTypes.FILLED_MAP.get()) {
            throw new CommandException(Component.text("You must hold a map in your hand"));
        }
        //map.offer(Keys.MAP_LOCATION, new Vector2i(10000,10000));
        final MapColor color = MapColor.of(MapColorTypes.TERRACOTTA_BLACK);
        final MapInfo mapInfo = map.require(Keys.MAP_INFO);
        mapInfo.offer(Keys.MAP_LOCKED, true);
        mapInfo.offer(Keys.MAP_CANVAS, MapCanvas.builder().paintAll(color).build());
        player.sendMessage(Component.text(mapInfo.require(Keys.MAP_CANVAS).color(0,0).toContainer().toString()));
        return CommandResult.success();
    }

    private CommandResult setMapNether(final CommandContext ctx) throws CommandException {
        final Audience audience = ctx.cause().audience();
        final Player player = this.requirePlayer(ctx);
        final ItemStack map = player.itemInHand(HandTypes.MAIN_HAND);
        if (map.type() != ItemTypes.FILLED_MAP.get()) {
            throw new CommandException(Component.text("You must hold a map in your hand"));
        }
        final ResourceKey netherKey = ResourceKey.minecraft("the_nether");
        final Optional<ServerWorld> nether = Sponge.server().worldManager().world(netherKey);
        if (!nether.isPresent()) {
            final CompletableFuture<ServerWorld> loadedNether = Sponge.server().worldManager().loadWorld(netherKey);
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
        map.require(Keys.MAP_INFO).offer(Keys.MAP_WORLD, nether.get().key());
        return CommandResult.success();
    }

    private CommandResult printMapData(final CommandContext ctx) throws CommandException {
        final Player player = this.requirePlayer(ctx);
        final ItemStack itemStack = player.itemInHand(HandTypes.MAIN_HAND);
        if (itemStack.type() != ItemTypes.FILLED_MAP.get()) {
            throw new CommandException(Component.text("You must hold a map in your hand"));
        }
        final MapInfo mapInfo = itemStack.require(Keys.MAP_INFO);
        final Audience console = Sponge.systemSubject();

        console.sendMessage(Component.text("the mapdata contains: " + mapInfo.toContainer()));
        console.sendMessage(Component.text("the map contains nbt: " + itemStack.toContainer()));
        //player.sendMessage(Text.of("the map contains vanilla nbt: " + itemStack.get().toContainer().get(DataQuery.of("UnsafeData")).get()));
        return CommandResult.success();
    }

    private void checkSerialization(final CommandContext ctx, final String testName, final String expected, final String after) {
        final Audience audience = ctx.cause().audience();
        final boolean success = expected.equals(after);
        final Component text = Component.text("Test of ").append(Component.text(testName, NamedTextColor.BLUE))
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
        Sponge.scheduler()
                .createSyncExecutor(this)
                .scheduleAtFixedRate(() -> Sponge.getServer().player("tyhdefu").ifPresent(player -> player.getItemInHand(HandTypes.MAIN_HAND).filter(itemStack -> itemStack.getType() == ItemTypes.FILLED_MAP)
                        .map(itemStack -> itemStack.require(Keys.MAP_INFO))
                        .map(mapInfo -> mapInfo.require(Keys.MAP_DECORATIONS))
                        .ifPresent(mapDecorations -> mapDecorations.forEach(dec -> player.sendMessage(Text.of(dec.toContainer()))))
                ), 0, 1, TimeUnit.SECONDS);
    }*/

    private Player requirePlayer(final CommandContext source) throws CommandException {
        final Audience audience = source.cause().audience();
        if (audience instanceof Player) {
            return (Player) audience;
        }
        throw new CommandException(Component.text("Must be called from player!"));
    }

    private void createDefaultCommand(final String name, final CommandExecutor executor, final RegisterCommandEvent<Command.Parameterized> event) {
        final Command.Parameterized command = Command.builder()
                .executor(executor)
                .build();

        event.register(this.container, command, name);
    }

    @Override
    public void enable(final CommandContext ctx) {
        final Audience audience = ctx.cause().audience();
        if (!this.isEnabled) {
            this.isEnabled = true;
            Sponge.eventManager().registerListeners(this.container, this.listeners);
            audience.sendMessage(Component.text("Map listeners are enabled. Created maps will now start blue.", NamedTextColor.GREEN));
        } else {
            audience.sendMessage(Component.text("Map listeners are already enabled.", NamedTextColor.YELLOW));
        }
    }

    public static class Listeners {

        private final Logger logger;

        public Listeners(final Logger logger) {
            this.logger = logger;
        }

        @Listener
        public void onMapCreate(final CreateMapEvent event, @Supports("MAP_CANVAS") @Getter("mapInfo") final MapInfo mapInfo) {
            this.logger.info("ON MAP CREATE EVENT");
            mapInfo.offer(Keys.MAP_CANVAS, MapCanvas.builder()
                    .paintAll(MapColor.of(MapColorTypes.COLOR_BLUE))
                    .build());
            // mapInfo.offer(Keys.MAP_LOCKED, true); // to make the color apply even when held by a player
        }
    }

}
