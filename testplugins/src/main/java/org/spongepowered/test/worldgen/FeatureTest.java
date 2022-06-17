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
import org.spongepowered.api.world.generation.feature.Feature;
import org.spongepowered.api.world.generation.feature.FeatureTemplate;
import org.spongepowered.api.world.generation.feature.Features;
import org.spongepowered.api.world.generation.feature.PlacedFeature;
import org.spongepowered.api.world.generation.feature.PlacedFeatureTemplate;
import org.spongepowered.api.world.generation.feature.PlacedFeatures;
import org.spongepowered.api.world.generation.structure.SchematicTemplate;
import org.spongepowered.api.world.generation.structure.Structure;
import org.spongepowered.api.world.generation.structure.StructureTemplate;
import org.spongepowered.api.world.generation.structure.Structures;
import org.spongepowered.api.world.schematic.Schematic;
import org.spongepowered.api.world.server.DataPackManager;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.math.vector.Vector3i;

import java.util.Optional;
import java.util.function.BiFunction;

public class FeatureTest {

    CommandResult placeFeature(final CommandContext commandContext, final Parameter.Value<Feature> param) {
        return this.place(commandContext, param, Features.TREES_PLAINS.get(), Feature::place);
    }

    CommandResult placePlaced(final CommandContext commandContext, final Parameter.Value<PlacedFeature> param) {
        return this.place(commandContext, param, PlacedFeatures.TREES_PLAINS.get(), PlacedFeature::place);
    }

    CommandResult placeStructure(final CommandContext commandContext, final Parameter.Value<Structure> param) {
        return this.place(commandContext, param, Structures.DESERT_PYRAMID.get(), Structure::place);
    }

    private <T> CommandResult place(final CommandContext ctx, final Parameter.Value<T> resourceKey, final T defaultValue,
            final BiFunction<T, ServerLocation, Boolean> placeFunction) {
        final T feature = ctx.one(resourceKey).orElse(defaultValue);
        final Optional<ServerPlayer> player = ctx.cause().first(ServerPlayer.class);
        if (player.isEmpty()) {
            ctx.sendMessage(Identity.nil(), Component.text("Run as player to place the feature"));
            return CommandResult.success();
        }

        final RayTrace<LocatableBlock> ray = this.viewRay(player.get());
        final ServerLocation location = ray.execute().orElseThrow().selectedObject().serverLocation().relativeTo(Direction.UP);
        if (placeFunction.apply(feature, location)) {
            ctx.sendMessage(Identity.nil(), Component.text("Placed Feature"));
        } else {
            ctx.sendMessage(Identity.nil(), Component.text("Could not place Feature"));
        }
        return CommandResult.success();
    }

    private RayTrace<LocatableBlock> viewRay(final ServerPlayer player) {
        return RayTrace.block().select(RayTrace.nonAir()).limit(100).sourceEyePosition(player).direction(player);
    }

    CommandResult register(final CommandContext ctx) {
        final DataPackManager dpm = Sponge.server().dataPackManager();

        final FeatureTemplate featureTemplate = FeatureTemplate.builder().from(Features.TREES_PLAINS.get())
                .key(ResourceKey.of("featuretest", "test"))
                .build();

        final PlacedFeatureTemplate placedFeatureTemplate1 = PlacedFeatureTemplate.builder().from(PlacedFeatures.TREES_PLAINS.get())
                .key(ResourceKey.of("featuretest", "test"))
                .build();

        final PlacedFeatureTemplate placedFeatureTemplate2 = PlacedFeatureTemplate.builder().from(PlacedFeatures.TREES_PLAINS.get())
                .feature(featureTemplate)
                .key(ResourceKey.of("featuretest", "test2"))
                .build();


        final StructureTemplate structureTemplate = StructureTemplate.builder().from(Structures.IGLOO.get())
                .key(ResourceKey.of("featuretest", "test"))
                .build();

        dpm.save(featureTemplate);
        dpm.save(placedFeatureTemplate1);
        dpm.save(placedFeatureTemplate2);
        dpm.save(structureTemplate);

        return CommandResult.success();
    }

    private CommandResult listFeatures(CommandContext ctx, final Parameter.Value<String> filterParam) {
        final Optional<String> rawFilter = ctx.one(filterParam);
        final String filter = rawFilter.orElse("minecraft:").toUpperCase();
        boolean invert = rawFilter.isPresent();
        ctx.sendMessage(Identity.nil(), Component.text("Features:", NamedTextColor.DARK_AQUA));
        Features.registry().streamEntries().filter(e -> invert == e.key().toString().toUpperCase().contains(filter))
                .forEach(e -> ctx.sendMessage(Identity.nil(), Component.text(" - " + e.key(), NamedTextColor.GRAY)));

        ctx.sendMessage(Identity.nil(), Component.text("Placed Features:", NamedTextColor.DARK_AQUA));
        PlacedFeatures.registry().streamEntries().filter(e -> invert == e.key().toString().toUpperCase().contains(filter))
                .forEach(e -> ctx.sendMessage(Identity.nil(), Component.text(" - " + e.key(), NamedTextColor.GRAY)));

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
                final RayTrace<LocatableBlock> ray = this.viewRay(player.get());
                final ServerLocation location = ray.execute().orElseThrow().selectedObject().serverLocation().relativeTo(Direction.UP);
                schematic.applyToWorld(location.world(), location.blockPosition(), SpawnTypes.CUSTOM);
                ctx.sendMessage(Identity.nil(), Component.text("Placed Schematic: " + key + " from pack " + pack.get().name(), NamedTextColor.DARK_AQUA));
            }
        }
        return CommandResult.success();
    }

    Command.Parameterized featureCmd() {
        final Parameter.Value<Feature> feature = Parameter.registryElement(TypeToken.get(Feature.class), RegistryTypes.FEATURE, "minecraft").key("feature").optional().build();
        final Parameter.Value<PlacedFeature> placedFeature = Parameter.registryElement(TypeToken.get(PlacedFeature.class), RegistryTypes.PLACED_FEATURE, "minecraft").key("feature").optional().build();
        final Parameter.Value<Structure> structure = Parameter.registryElement(TypeToken.get(Structure.class), RegistryTypes.STRUCTURE, "minecraft").key("structure").optional().build();
        final Parameter.Value<String> filter = Parameter.string().key("filter").optional().build();
        return Command.builder()
                .addChild(Command.builder().addParameter(filter).executor(ctx -> this.listFeatures(ctx, filter)).build(), "list")
                .addChild(Command.builder().addParameter(feature).executor(ctx -> this.placeFeature(ctx, feature)).build(), "placeFeature")
                .addChild(Command.builder().addParameter(placedFeature).executor(ctx -> this.placePlaced(ctx, placedFeature)).build(), "placePlaced")
                .addChild(Command.builder().addParameter(structure).executor(ctx -> this.placeStructure(ctx, structure)).build(), "placeStructure")
                .addChild(Command.builder().executor(this::register).build(), "register")
                .build();
    }

    Command.Parameterized schematicCmd() {
        final Parameter.Value<ResourceKey> schematic = Parameter.resourceKey().key("schematic").optional().build();
        final Parameter.Value<String> filter = Parameter.string().key("filter").optional().build();
        return Command.builder()
                .addChild(Command.builder().addParameter(filter).executor(ctx -> this.listSchematics(ctx, filter)).build(), "list")
                .addChild(Command.builder().addParameter(schematic).executor(ctx -> this.placeSchematic(ctx, schematic)).build(), "place")
                .build();
    }


}
