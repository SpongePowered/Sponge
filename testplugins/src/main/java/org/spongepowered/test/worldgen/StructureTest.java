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
package org.spongepowered.test.worldgen;

import io.leangen.geantyref.TypeToken;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandCompletion;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.datapack.DataPack;
import org.spongepowered.api.datapack.DataPackTypes;
import org.spongepowered.api.datapack.DataPacks;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.cause.entity.SpawnTypes;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.util.blockray.RayTrace;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.api.world.generation.structure.SchematicTemplate;
import org.spongepowered.api.world.generation.structure.Structure;
import org.spongepowered.api.world.generation.structure.StructureTemplate;
import org.spongepowered.api.world.generation.structure.Structures;
import org.spongepowered.api.world.generation.structure.jigsaw.JigsawPool;
import org.spongepowered.api.world.generation.structure.jigsaw.JigsawPools;
import org.spongepowered.api.world.generation.structure.jigsaw.ProcessorList;
import org.spongepowered.api.world.generation.structure.jigsaw.ProcessorListTemplate;
import org.spongepowered.api.world.generation.structure.jigsaw.ProcessorLists;
import org.spongepowered.api.world.generation.structure.jigsaw.ProcessorTypes;
import org.spongepowered.api.world.schematic.Schematic;
import org.spongepowered.api.world.server.DataPackManager;
import org.spongepowered.api.world.server.ServerLocation;

import java.util.Collection;
import java.util.Optional;
import java.util.Random;

public class StructureTest {

    CommandResult placeStructure(final CommandContext commandContext, final Parameter.Value<Structure> param) {
        return FeatureTest.place(commandContext, param, Structures.DESERT_PYRAMID.get(), Structure::place);
    }

    CommandResult registerStructure(final CommandContext ctx) {
        final DataPackManager dpm = Sponge.server().dataPackManager();

        final StructureTemplate structureTemplate = StructureTemplate.builder().from(Structures.IGLOO.get())
                .key(ResourceKey.of("featuretest", "test"))
                .build();

        dpm.save(structureTemplate);

        return CommandResult.success();
    }

    private CommandResult listStructures(CommandContext ctx, final Parameter.Value<String> filterParam) {
        final Optional<String> rawFilter = ctx.one(filterParam);
        final String filter = rawFilter.orElse("minecraft:").toUpperCase();
        boolean invert = rawFilter.isPresent();

        ctx.sendMessage(Identity.nil(), Component.text("Structures:", NamedTextColor.DARK_AQUA));
        Structures.registry().streamEntries().filter(e -> invert == e.key().toString().toUpperCase().contains(filter))
                .forEach(e -> ctx.sendMessage(Identity.nil(), Component.text(" - " + e.key(), NamedTextColor.GRAY)));

        return CommandResult.success();
    }

    private CommandResult listSchematics(final CommandContext ctx, final Parameter.Value<String> filterParam) {
        final Optional<String> rawFilter = ctx.one(filterParam);
        final String filter = rawFilter.orElse("minecraft:").toUpperCase();
        boolean invert = rawFilter.isPresent();

        final DataPackManager dpm = Sponge.server().dataPackManager();
        dpm.find(DataPackTypes.SCHEMATIC).forEach((pack, keys) -> {
            if (keys.isEmpty()) {
                return;
            }
            ctx.sendMessage(Identity.nil(), Component.text("Schematics Pack " + pack.name() + "(" + keys.size() + ")", NamedTextColor.DARK_AQUA));
            keys.stream().filter(key -> invert == key.toString().toUpperCase().contains(filter)).forEach(key -> {
                final Optional<SchematicTemplate> loaded = dpm.load(DataPacks.SCHEMATIC, key).join();

                ctx.sendMessage(Identity.nil(), Component.text(" - " + key + " loaded " + loaded.isPresent(), NamedTextColor.GRAY));
            });

        });

        return CommandResult.success();
    }

    private CommandResult placeSchematic(final CommandContext ctx, final Parameter.Value<ResourceKey> schematicParam) {

        final DataPackManager dpm = Sponge.server().dataPackManager();
        final ResourceKey key = ctx.one(schematicParam).orElse(ResourceKey.minecraft("village/desert/houses/desert_small_house_1"));
        final Optional<DataPack<SchematicTemplate>> pack = dpm.findPack(DataPackTypes.SCHEMATIC, key);
        final Optional<ServerPlayer> player = ctx.cause().first(ServerPlayer.class);
        if (pack.isPresent()) {
            final Optional<SchematicTemplate> loaded = dpm.load(pack.get(), key).join();
            if (loaded.isPresent() && player.isPresent()) {
                final Schematic schematic = loaded.get().schematic();
                final RayTrace<LocatableBlock> ray = FeatureTest.viewRay(player.get());
                final ServerLocation location = ray.execute().orElseThrow().selectedObject().serverLocation().relativeTo(Direction.UP);
                schematic.applyToWorld(location.world(), location.blockPosition(), SpawnTypes.CUSTOM);
                ctx.sendMessage(Identity.nil(), Component.text("Placed Schematic: " + key + " from pack " + pack.get().name(), NamedTextColor.DARK_AQUA));
            }
        }
        return CommandResult.success();
    }

    private CommandResult placeJigsaw(final CommandContext ctx, final Parameter.Value<JigsawPool> jigsawPoolParam) {
        final JigsawPool pool = ctx.one(jigsawPoolParam).orElse(JigsawPools.VILLAGE_DESERT_DECOR.get());
        final JigsawPool.Element element = pool.elements().get(new Random()).iterator().next();
        final Optional<ServerPlayer> player = ctx.cause().first(ServerPlayer.class);
        if (player.isPresent()) {
            final RayTrace<LocatableBlock> ray = FeatureTest.viewRay(player.get());
            final ServerLocation location = ray.execute().orElseThrow().selectedObject().serverLocation().relativeTo(Direction.UP);
            element.place(location);
            ctx.sendMessage(Identity.nil(), Component.text("Placed Jigsaw Element from Pool: " + pool, NamedTextColor.DARK_AQUA));
        }
        return CommandResult.success();
    }

    private CommandResult placeJigsawElement(final CommandContext ctx, final Parameter.Value<ResourceKey> structureParam, final Parameter.Value<ProcessorList> processorListParam) {
        final JigsawPool.Factory factory = JigsawPool.factory();
        final ResourceKey structure = ctx.one(structureParam).orElse(ResourceKey.minecraft("village/desert/desert_lamp_1"));
        final ProcessorList processorList = ctx.one(processorListParam).orElse(ProcessorLists.ZOMBIE_DESERT.get());
        final JigsawPool.Element element = factory.legacy(structure, processorList).apply(factory.rigid());
        final Optional<ServerPlayer> player = ctx.cause().first(ServerPlayer.class);
        if (player.isPresent()) {
            final RayTrace<LocatableBlock> ray = FeatureTest.viewRay(player.get());
            final ServerLocation location = ray.execute().orElseThrow().selectedObject().serverLocation().relativeTo(Direction.UP);
            element.place(location);

            ctx.sendMessage(Identity.nil(), Component.text("Placed Jigsaw Element " + structure + " with " + ProcessorLists.registry().valueKey(processorList), NamedTextColor.DARK_AQUA));
        }
        return CommandResult.success();
    }

    private CommandResult listProcessors(final CommandContext ctx, final Parameter.Value<String> filterParam) {
        final Optional<String> rawFilter = ctx.one(filterParam);
        final String filter = rawFilter.orElse("minecraft:").toUpperCase();
        boolean invert = rawFilter.isPresent();

        ctx.sendMessage(Identity.nil(), Component.text("Processor Lists:", NamedTextColor.DARK_AQUA));
        ProcessorLists.registry().streamEntries().filter(e -> invert == e.key().toString().toUpperCase().contains(filter))
                .forEach(e -> ctx.sendMessage(Identity.nil(), Component.text(" - " + e.key(), NamedTextColor.GRAY)));


        ctx.sendMessage(Identity.nil(), Component.text("Processor Types:", NamedTextColor.DARK_AQUA));
        ProcessorTypes.registry().streamEntries().filter(e -> invert == e.key().toString().toUpperCase().contains(filter))
                .forEach(e -> ctx.sendMessage(Identity.nil(), Component.text(" - " + e.key(), NamedTextColor.GRAY)));

        return CommandResult.success();
    }

    private CommandResult registerProcessor(final CommandContext ctx) {
        final DataPackManager dpm = Sponge.server().dataPackManager();


        final ProcessorListTemplate template = ProcessorListTemplate.builder()
                .from(ProcessorLists.MOSSIFY_70_PERCENT.get())
                .key(ResourceKey.of("processortest", "test"))
                .build();

        dpm.save(template);

        return CommandResult.success();
    }


    Command.Parameterized structureCmd() {
        final Parameter.Value<Structure> structure = Parameter.registryElement(TypeToken.get(Structure.class), RegistryTypes.STRUCTURE, "minecraft").key("structure").optional().build();
        final Parameter.Value<String> filter = Parameter.string().key("filter").optional().build();
        return Command.builder()
                .addChild(Command.builder().addParameter(structure).executor(ctx -> this.placeStructure(ctx, structure)).build(), "placeStructure")
                .addChild(Command.builder().addParameter(filter).executor(ctx -> this.listStructures(ctx, filter)).build(), "list")
                .addChild(Command.builder().executor(this::registerStructure).build(), "register")
                .build();
    }

    Command.Parameterized schematicCmd() {
        final Parameter.Value<ProcessorList> processorList = Parameter.registryElement(TypeToken.get(ProcessorList.class), RegistryTypes.PROCESSOR_LIST, "minecraft").key("processor_list").optional().build();
        final Parameter.Value<ResourceKey> schematic = Parameter.resourceKey().key("schematic").optional().build();
        final Parameter.Value<ResourceKey> structure = Parameter.resourceKey().key("structure").completer((ctx, input) -> {
            final DataPackManager dpm = Sponge.server().dataPackManager();
            return dpm.find(DataPackTypes.SCHEMATIC).values().stream().flatMap(Collection::stream).map(ResourceKey::toString)
                    .filter(s -> s.startsWith(input) || s.startsWith("minecraft:" + input))
                    .map(CommandCompletion::of).toList();
        }).optional().build();
        final Parameter.Value<String> filter = Parameter.string().key("filter").optional().build();
        return Command.builder()
                .addChild(Command.builder().addParameter(filter).executor(ctx -> this.listSchematics(ctx, filter)).build(), "list")
                .addChild(Command.builder().addParameter(schematic).executor(ctx -> this.placeSchematic(ctx, schematic)).build(), "place")
                .addChild(Command.builder().addParameter(structure).addParameter(processorList).executor(ctx -> this.placeJigsawElement(ctx, structure, processorList)).build(), "placeProcessed")
                .build();
    }

    Command.Parameterized jigsawCmd() {
        final Parameter.Value<JigsawPool> jigsawPool = Parameter.registryElement(TypeToken.get(JigsawPool.class), RegistryTypes.JIGSAW_POOL, "minecraft").key("jigsaw_pool").optional().build();
        return Command.builder()
                .addChild(Command.builder().addParameter(jigsawPool).executor(ctx -> this.placeJigsaw(ctx, jigsawPool)).build(), "place")
                .build();
    }

    Command.Parameterized processorCmd() {
        final Parameter.Value<String> filter = Parameter.string().key("filter").optional().build();
        return Command.builder()
                .addChild(Command.builder().addParameter(filter).executor(ctx -> this.listProcessors(ctx, filter)).build(), "list")
                .addChild(Command.builder().executor(ctx -> this.registerProcessor(ctx)).build(), "register")
                .build();
    }



}
