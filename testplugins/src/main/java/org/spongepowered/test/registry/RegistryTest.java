/*
 * This file is part of SpongeAPI, licensed under the MIT License (MIT).
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
package org.spongepowered.test.registry;

import com.google.inject.Inject;
import net.kyori.adventure.text.ComponentLike;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.ResourceKeyed;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.advancement.AdvancementTypes;
import org.spongepowered.api.advancement.criteria.trigger.Triggers;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.entity.BlockEntityTypes;
import org.spongepowered.api.block.transaction.Operations;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.managed.clientcompletion.ClientCompletionTypes;
import org.spongepowered.api.command.parameter.managed.standard.ResourceKeyedValueParameters;
import org.spongepowered.api.command.registrar.tree.ClientCompletionKeys;
import org.spongepowered.api.command.selector.SelectorSortAlgorithms;
import org.spongepowered.api.command.selector.SelectorTypes;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.data.type.ArmorMaterials;
import org.spongepowered.api.data.type.ArtTypes;
import org.spongepowered.api.data.type.AttachmentSurfaces;
import org.spongepowered.api.data.type.BannerPatternShapes;
import org.spongepowered.api.data.type.BoatTypes;
import org.spongepowered.api.data.type.BodyParts;
import org.spongepowered.api.data.type.CatTypes;
import org.spongepowered.api.data.type.ChestAttachmentTypes;
import org.spongepowered.api.data.type.ComparatorModes;
import org.spongepowered.api.data.type.DoorHinges;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.data.type.FoxTypes;
import org.spongepowered.api.data.type.HandPreferences;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.data.type.HorseColors;
import org.spongepowered.api.data.type.HorseStyles;
import org.spongepowered.api.data.type.InstrumentTypes;
import org.spongepowered.api.data.type.ItemTiers;
import org.spongepowered.api.data.type.LlamaTypes;
import org.spongepowered.api.data.type.MatterTypes;
import org.spongepowered.api.data.type.MooshroomTypes;
import org.spongepowered.api.data.type.NotePitches;
import org.spongepowered.api.data.type.PandaGenes;
import org.spongepowered.api.data.type.ParrotTypes;
import org.spongepowered.api.data.type.PhantomPhases;
import org.spongepowered.api.data.type.PickupRules;
import org.spongepowered.api.data.type.PistonTypes;
import org.spongepowered.api.data.type.PortionTypes;
import org.spongepowered.api.data.type.ProfessionTypes;
import org.spongepowered.api.data.type.RabbitTypes;
import org.spongepowered.api.data.type.RaidStatuses;
import org.spongepowered.api.data.type.RailDirections;
import org.spongepowered.api.data.type.SkinParts;
import org.spongepowered.api.data.type.SlabPortions;
import org.spongepowered.api.data.type.SpellTypes;
import org.spongepowered.api.data.type.StairShapes;
import org.spongepowered.api.data.type.StructureModes;
import org.spongepowered.api.data.type.TropicalFishShapes;
import org.spongepowered.api.data.type.VillagerTypes;
import org.spongepowered.api.data.type.WireAttachmentTypes;
import org.spongepowered.api.datapack.DataPackTypes;
import org.spongepowered.api.effect.particle.ParticleOptions;
import org.spongepowered.api.effect.particle.ParticleTypes;
import org.spongepowered.api.effect.potion.PotionEffectTypes;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.effect.sound.music.MusicDiscs;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.ai.goal.GoalExecutorTypes;
import org.spongepowered.api.entity.ai.goal.GoalTypes;
import org.spongepowered.api.entity.attribute.AttributeOperations;
import org.spongepowered.api.entity.attribute.type.AttributeTypes;
import org.spongepowered.api.entity.living.monster.boss.dragon.phase.DragonPhaseTypes;
import org.spongepowered.api.entity.living.player.chat.ChatVisibilities;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.entity.DismountTypes;
import org.spongepowered.api.event.cause.entity.MovementTypes;
import org.spongepowered.api.event.cause.entity.SpawnTypes;
import org.spongepowered.api.event.cause.entity.damage.DamageModifierTypes;
import org.spongepowered.api.event.cause.entity.damage.DamageTypes;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.fluid.FluidTypes;
import org.spongepowered.api.item.FireworkShapes;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.enchantment.EnchantmentTypes;
import org.spongepowered.api.item.inventory.ContainerTypes;
import org.spongepowered.api.item.inventory.equipment.EquipmentGroups;
import org.spongepowered.api.item.inventory.equipment.EquipmentTypes;
import org.spongepowered.api.item.inventory.menu.ClickTypes;
import org.spongepowered.api.item.inventory.query.QueryTypes;
import org.spongepowered.api.item.potion.PotionTypes;
import org.spongepowered.api.item.recipe.RecipeTypes;
import org.spongepowered.api.map.color.MapColorTypes;
import org.spongepowered.api.map.color.MapShades;
import org.spongepowered.api.map.decoration.MapDecorationTypes;
import org.spongepowered.api.map.decoration.orientation.MapDecorationOrientations;
import org.spongepowered.api.placeholder.PlaceholderParsers;
import org.spongepowered.api.registry.DefaultedRegistryReference;
import org.spongepowered.api.registry.Registry;
import org.spongepowered.api.registry.RegistryHolder;
import org.spongepowered.api.registry.RegistryScope;
import org.spongepowered.api.registry.RegistryScopes;
import org.spongepowered.api.scheduler.TaskPriorities;
import org.spongepowered.api.scoreboard.CollisionRules;
import org.spongepowered.api.scoreboard.Visibilities;
import org.spongepowered.api.scoreboard.criteria.Criteria;
import org.spongepowered.api.scoreboard.displayslot.DisplaySlots;
import org.spongepowered.api.scoreboard.objective.displaymode.ObjectiveDisplayModes;
import org.spongepowered.api.service.ban.BanTypes;
import org.spongepowered.api.service.economy.account.AccountDeletionResultTypes;
import org.spongepowered.api.service.economy.transaction.TransactionTypes;
import org.spongepowered.api.state.BooleanStateProperties;
import org.spongepowered.api.state.EnumStateProperties;
import org.spongepowered.api.state.IntegerStateProperties;
import org.spongepowered.api.statistic.StatisticCategories;
import org.spongepowered.api.statistic.Statistics;
import org.spongepowered.api.util.Nameable;
import org.spongepowered.api.util.mirror.Mirrors;
import org.spongepowered.api.util.orientation.Orientations;
import org.spongepowered.api.util.rotation.Rotations;
import org.spongepowered.api.world.ChunkRegenerateFlags;
import org.spongepowered.api.world.HeightTypes;
import org.spongepowered.api.world.LightTypes;
import org.spongepowered.api.world.WorldTypes;
import org.spongepowered.api.world.biome.BiomeSamplers;
import org.spongepowered.api.world.biome.Biomes;
import org.spongepowered.api.world.chunk.ChunkStates;
import org.spongepowered.api.world.difficulty.Difficulties;
import org.spongepowered.api.world.gamerule.GameRules;
import org.spongepowered.api.world.generation.structure.Structures;
import org.spongepowered.api.world.portal.PortalTypes;
import org.spongepowered.api.world.schematic.PaletteTypes;
import org.spongepowered.api.world.teleport.TeleportHelperFilters;
import org.spongepowered.api.world.weather.WeatherTypes;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.jvm.Plugin;

import java.lang.reflect.Field;
import java.util.Objects;
import java.util.Optional;

@Plugin("registrytest")
public final class RegistryTest {

    private final PluginContainer plugin;
    private static final Class<?>[] REGISTRY_CLASSES = new Class[] {
            Triggers.class, AdvancementTypes.class, BlockEntityTypes.class, Operations.class, BlockTypes.class, ClientCompletionTypes.class,
            ResourceKeyedValueParameters.class, ClientCompletionKeys.class, SelectorSortAlgorithms.class, SelectorTypes.class, DataFormats.class,
            ArmorMaterials.class, ArtTypes.class, AttachmentSurfaces.class, BannerPatternShapes.class, BoatTypes.class, BodyParts.class,
            CatTypes.class, ChestAttachmentTypes.class, ComparatorModes.class, DoorHinges.class, DyeColors.class, FoxTypes.class, HandPreferences.class,
            HandTypes.class, HorseColors.class, HorseStyles.class, InstrumentTypes.class, ItemTiers.class, LlamaTypes.class, MatterTypes.class,
            MooshroomTypes.class, NotePitches.class, PandaGenes.class, ParrotTypes.class, PhantomPhases.class, PickupRules.class, PistonTypes.class,
            PortionTypes.class, ProfessionTypes.class, RabbitTypes.class, RaidStatuses.class, RailDirections.class, SkinParts.class, SlabPortions.class,
            SpellTypes.class, StairShapes.class, StructureModes.class, TropicalFishShapes.class, VillagerTypes.class, WireAttachmentTypes.class,
            ParticleOptions.class, ParticleTypes.class, PotionEffectTypes.class, MusicDiscs.class, SoundTypes.class,
            GoalExecutorTypes.class, GoalTypes.class, AttributeTypes.class, AttributeOperations.class, DragonPhaseTypes.class, ChatVisibilities.class,
            GameModes.class, EntityTypes.class, DamageModifierTypes.class, DamageTypes.class, DismountTypes.class, MovementTypes.class, SpawnTypes.class,
            FluidTypes.class, EnchantmentTypes.class, EquipmentGroups.class, EquipmentTypes.class, ClickTypes.class, QueryTypes.class, ContainerTypes.class,
            PotionTypes.class, RecipeTypes.class, FireworkShapes.class, ItemTypes.class, PlaceholderParsers.class, TaskPriorities.class, Criteria.class,
            DisplaySlots.class, ObjectiveDisplayModes.class, CollisionRules.class, Visibilities.class, BanTypes.class, AccountDeletionResultTypes.class,
            TransactionTypes.class, BooleanStateProperties.class, EnumStateProperties.class, IntegerStateProperties.class, StatisticCategories.class,
            Statistics.class, Mirrors.class, Orientations.class, Rotations.class, Biomes.class, BiomeSamplers.class, ChunkStates.class, Difficulties.class,
            GameRules.class, Structures.class, PortalTypes.class, PaletteTypes.class, TeleportHelperFilters.class, WeatherTypes.class, ChunkRegenerateFlags.class,
            HeightTypes.class, LightTypes.class, WorldTypes.class, MapDecorationOrientations.class, MapDecorationTypes.class, MapShades.class, MapColorTypes.class,
    };

    @Inject
    public RegistryTest(final PluginContainer plugin) {
        this.plugin = plugin;
    }

    @Listener
    public void onRegisterCommand(final RegisterCommandEvent<Command.Parameterized> event) {
        event.register(this.plugin, Command.builder()
                .setExecutor(context -> {
                    for (Class<?> aClass : REGISTRY_CLASSES) {
                        this.processClass(aClass);
                    }
                    return CommandResult.success();
                })
                .build(), "registrytest");
    }

    private void processClass(Class<?> clazz) {
        RegistryScopes scope = clazz.getAnnotation(RegistryScopes.class);
        if (scope == null) {
            this.plugin.getLogger().error("Class {} is not annotated with @RegistryScopes ", clazz.getName());
            return;
        }

        RegistryHolder holder = null;
        for (RegistryScope registryScope : scope.scopes()) {
            if (registryScope == RegistryScope.GAME) {
                holder = Sponge.getGame().registries();
            } else if (registryScope == RegistryScope.ENGINE) {
                holder = Sponge.getServer().registries();
            } else if (registryScope == RegistryScope.WORLD) {
                holder = Sponge.getGame().getServer().getWorldManager().defaultWorld().registries();
            }
        }
        if (holder == null) {
            this.plugin.getLogger().error("Registry holder not found for Class {}", clazz.getName());
            return;
        }

        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            try {
                Object o = field.get(null);
                if (!(o instanceof DefaultedRegistryReference)) {
                    this.plugin.getLogger().warn("Field value is not an instance of DefaultedRegistryReference: {} in class {}", o.getClass().getSimpleName(), clazz.getName());
                    continue;
                }

                DefaultedRegistryReference<?> reference = (DefaultedRegistryReference<?>) o;
                Object value = reference.get();
                if (!reference.find().isPresent()) {
                    this.plugin.getLogger().warn("DefaultedRegistryReference#find returned empty for key: {}", reference.location().getFormatted());
                } else if (reference.find().get() != reference.get()) {
                    this.plugin.getLogger().warn("Mismatched object for key {}: #find returned {} and #get returned {}", reference.location().getFormatted(), reference.find().get(), reference.get());
                }

                if (value.getClass().getName().startsWith("net.minecraft") && !reference.location().getNamespace().equals(ResourceKey.MINECRAFT_NAMESPACE)) {
                    this.plugin.getLogger().warn("Minecraft class without the minecraft namespace: {}", reference.location().getFormatted());
                }
                if (value.getClass().getName().startsWith("com.mojang.brigadier") && !reference.location().getNamespace().equals(ResourceKey.BRIGADIER_NAMESPACE)) {
                    this.plugin.getLogger().warn("Brigadier class without the brigadier namespace: {}", reference.location().getFormatted());
                }

                Optional<? extends Registry<?>> registryOpt = holder.findRegistry(reference.registry());
                if (!registryOpt.isPresent()) {
                    this.plugin.getLogger().info("Missing Registry ({}) in scope ({})", reference.registry().toString(), scope.scopes());
                }

                if (value instanceof ResourceKeyed && !((ResourceKeyed) value).getKey().equals(reference.location())) {
                    this.plugin.getLogger().error("Mismatched key: Expected ({}) but Sponge is using ({})", ((ResourceKeyed) value).getKey().getFormatted(), reference.location().getFormatted());
                }

                if (value instanceof ResourceKeyed) {
                    Objects.requireNonNull(((ResourceKeyed) value).getKey(), reference.location().getFormatted() + " getKey"); // Ensure this doesn't throw abstract method error or class cast exception
                }

                if (value instanceof ComponentLike) {
                    Objects.requireNonNull(((ComponentLike) value).asComponent(), reference.location().getFormatted() + " asComponent"); // Ensure this doesn't throw abstract method error or class cast exception
                }

                if (value instanceof Nameable) {
                    Objects.requireNonNull(((Nameable) value).getName(), reference.location().getFormatted() + " getName"); // Ensure this doesn't throw abstract method error or class cast exception
                }
            } catch (Throwable e) {
                this.plugin.getLogger().error("Unexpected error occurred", e);
            }
        }
    }
}
