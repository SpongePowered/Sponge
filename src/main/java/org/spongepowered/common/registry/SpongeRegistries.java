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
package org.spongepowered.common.registry;

import net.minecraft.advancements.FrameType;
import net.minecraft.entity.passive.fish.TropicalFishEntity;
import net.minecraft.tileentity.BannerPattern;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.advancement.AdvancementType;
import org.spongepowered.api.block.transaction.Operation;
import org.spongepowered.api.command.parameter.managed.clientcompletion.ClientCompletionType;
import org.spongepowered.api.command.parameter.managed.standard.CatalogedValueParameter;
import org.spongepowered.api.command.registrar.CommandRegistrar;
import org.spongepowered.api.command.registrar.tree.ClientCompletionKey;
import org.spongepowered.api.command.selector.SelectorSortAlgorithm;
import org.spongepowered.api.command.selector.SelectorType;
import org.spongepowered.api.data.DataRegistration;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.persistence.DataFormat;
import org.spongepowered.api.data.persistence.StringDataFormat;
import org.spongepowered.api.data.type.BannerPatternShape;
import org.spongepowered.api.data.type.BodyPart;
import org.spongepowered.api.data.type.MatterType;
import org.spongepowered.api.data.type.SkinPart;
import org.spongepowered.api.data.type.TropicalFishShape;
import org.spongepowered.api.effect.particle.ParticleOption;
import org.spongepowered.api.effect.sound.music.MusicDisc;
import org.spongepowered.api.entity.ai.goal.GoalExecutorType;
import org.spongepowered.api.event.EventContextKey;
import org.spongepowered.api.event.cause.entity.DismountType;
import org.spongepowered.api.event.cause.entity.MovementType;
import org.spongepowered.api.event.cause.entity.SpawnType;
import org.spongepowered.api.event.cause.entity.damage.DamageModifierType;
import org.spongepowered.api.event.cause.entity.damage.DamageType;
import org.spongepowered.api.item.inventory.equipment.EquipmentGroup;
import org.spongepowered.api.item.inventory.equipment.EquipmentType;
import org.spongepowered.api.item.inventory.menu.ClickType;
import org.spongepowered.api.item.inventory.query.QueryType;
import org.spongepowered.api.placeholder.PlaceholderParser;
import org.spongepowered.api.registry.RegistryRoots;
import org.spongepowered.api.registry.RegistryType;
import org.spongepowered.api.scoreboard.criteria.Criterion;
import org.spongepowered.api.service.ban.BanType;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.account.AccountDeletionResultType;
import org.spongepowered.api.world.WorldArchetype;
import org.spongepowered.api.world.portal.PortalType;
import org.spongepowered.api.world.schematic.PaletteType;
import org.spongepowered.api.world.teleport.TeleportHelperFilter;
import org.spongepowered.api.world.weather.Weather;
import org.spongepowered.common.data.SpongeDataManager;
import org.spongepowered.common.data.SpongeDataRegistration;
import org.spongepowered.common.data.persistence.HoconDataFormat;
import org.spongepowered.common.data.persistence.JsonDataFormat;
import org.spongepowered.common.data.persistence.NBTDataFormat;
import org.spongepowered.common.event.tracking.context.transaction.type.BlockTransactionTypeStreamGenerator;
import org.spongepowered.common.event.tracking.context.transaction.type.TransactionType;
import org.spongepowered.common.registry.builtin.sponge.PaletteTypeStreamGenerator;
import org.spongepowered.common.registry.builtin.vanilla.CriterionStreamGenerator;
import org.spongepowered.common.registry.builtin.vanilla.EquipmentGroupStreamGenerator;
import org.spongepowered.common.registry.builtin.vanilla.EquipmentTypeStreamGenerator;

import java.util.Arrays;
import java.util.stream.Stream;

public final class SpongeRegistries {

    public static final RegistryType<CommandRegistrar<?>> COMMAND_REGISTRAR = SpongeRegistries.spongeKey("command_registrar");

    public static final RegistryType<QueryType> QUERY_TYPE = SpongeRegistries.spongeKey("query_type");

    public static final RegistryType<SpawnType> SPAWN_TYPE = SpongeRegistries.spongeKey("spawn_type");

    private static <V> RegistryType<V> spongeKey(final String key) {
        return RegistryType.of(RegistryRoots.SPONGE, ResourceKey.sponge(key));
    }

    public static void registerGlobalRegistries(final SpongeRegistryHolder registries) {
        VanillaRegistryLoader.load(registries);
        // ----------------------------------------------------------------------------------------------------
        this
                .generateRegistry(AccountDeletionResultType.class, ResourceKey.sponge("account_deletion_result_type"), AccountDeletionResultTypeGenerator
                        .stream(), true, false)
                .generateRegistry(AdvancementType.class, ResourceKey.minecraft("advancement_type"), Arrays.stream(FrameType.values()), true, false)
                .generateRegistry(BanType.class, ResourceKey.minecraft("ban_type"), BanTypeStreamGenerator.stream(), true, false)
                .generateRegistry(BannerPatternShape.class, ResourceKey.minecraft("banner_pattern_shape"), Arrays.stream(BannerPattern.values()), true, false)
                .generateRegistry(BodyPart.class, ResourceKey.minecraft("body_part"), BodyPartStreamGenerator.stream(), true, false)
                .generateRegistry(ClientCompletionKey.class, ResourceKey.sponge("client_completion_key"), ClientCompletionKeyStreamGenerator.stream(), true, false)
                .generateRegistry(ClientCompletionType.class, ResourceKey.sponge("client_completion_type"), ClientCompletionTypeStreamGenerator.stream(), true, false)
                .generateRegistry(Criterion.class, ResourceKey.sponge("criterion"), CriterionStreamGenerator.stream(), true, false)
                .registerRegistry(Currency.class, ResourceKey.sponge("currency"), true)
                .generateRegistry(DamageModifierType.class, ResourceKey.sponge("damage_modifier_type"), DamageModifierTypeStreamGenerator.stream(), true, true)
                .generateRegistry(DamageType.class, ResourceKey.sponge("damage_type"), DamageTypeStreamGenerator.stream(), true, true)
                .generateCallbackRegistry(DataRegistration.class, ResourceKey.sponge("data_registration"), Stream.empty(), (key, value) -> ((SpongeDataManager) Sponge.getGame().getDataManager()).registerCustomDataRegistration((SpongeDataRegistration) value), false, true)
                .generateRegistry(DismountType.class, ResourceKey.minecraft("dismount_type"), DismountTypeStreamGenerator.stream(), true, false)
                .generateRegistry(CatalogedValueParameter.class, ResourceKey.sponge("value_parameter"), CatalogedValueParameterStreamGenerator.stream(), true, true)
                .generateRegistry(CommandRegistrar.class, ResourceKey.sponge("command_registrar"), CommandRegistrarStreamGenerator.stream(), true, true)
                .generateRegistry(EquipmentGroup.class, ResourceKey.minecraft("equipment_group"), EquipmentGroupStreamGenerator.stream(), true, false)
                .generateRegistry(EquipmentType.class, ResourceKey.minecraft("equipment_type"), EquipmentTypeStreamGenerator.stream(), true, false)
                .generateRegistry(EventContextKey.class, ResourceKey.sponge("event_context_key"), EventContextKeyStreamGenerator.stream(), true, true)
                .generateRegistry(GoalExecutorType.class, ResourceKey.minecraft("goal_executor_type"), GoalExecutorTypeStreamGenerator.stream(), true, false)
                .generateRegistry(Key.class, ResourceKey.sponge("key"), KeyStreamGenerator.stream(), true, true)
                .generateRegistry(MatterType.class, ResourceKey.sponge("matter_type"), MatterTypeStreamGenerator.stream(), true, false)
                .generateRegistry(MovementType.class, ResourceKey.sponge("movement_type"), MovementTypeStreamGenerator.stream(), true, true)
                .generateRegistry(MusicDisc.class, ResourceKey.minecraft("music_disc"), MusicDiscStreamGenerator.stream(), true, false)
                .generateRegistry(PaletteType.class, ResourceKey.sponge("palette"), PaletteTypeStreamGenerator.stream(), true, true)
                .generateRegistry(ParticleOption.class, ResourceKey.sponge("particle_option"), ParticleOptionStreamGenerator.stream(), true, false)
                .generateRegistry(PlaceholderParser.class, ResourceKey.sponge("placeholder"), PlaceholderParserStreamGenerator.stream(), true, true)
                .generateRegistry(PortalType.class, ResourceKey.minecraft("portal_type"), PortalTypeStreamGenerator.stream(), true, true)
                .generateRegistry(QueryType.class, ResourceKey.sponge("query_type"), QueryTypeStreamGenerator.stream(), true, true)
                .generateRegistry(SelectorSortAlgorithm.class, ResourceKey.minecraft("selector_sort_algorithm"), SelectorSortAlgorithmStreamGenerator.stream(), true, false)
                .generateRegistry(SelectorType.class, ResourceKey.minecraft("selector_type"), SelectorTypeStreamGenerator.stream(), true, false)
                .generateRegistry(SkinPart.class, ResourceKey.minecraft("skin_part"), SkinPartStreamGenerator.stream(), true, false)
                .generateRegistry(SpawnType.class, ResourceKey.sponge("spawn_type"), SpawnTypeStreamGenerator.stream(), true, true)
                .generateRegistry(TropicalFishShape.class, ResourceKey.minecraft("tropical_fish_shape"), Arrays.stream(TropicalFishEntity.Type.values()), true, false)
                .generateRegistry(Weather.class, ResourceKey.minecraft("weather"), WeatherStreamGenerator.stream(), true, false)
                .generateRegistry(WoodType.class, ResourceKey.minecraft("wood_type"), WoodTypeStreamGenerator.stream(), true, false)
                .generateRegistry(WorldArchetype.class, ResourceKey.minecraft("world_archetype"), WorldArchetypeStreamGenerator.stream(), true, true)
                .generateRegistry(ClickType.class, ResourceKey.minecraft("click_type"), ClickTypeStreamGenerator.stream(), true, false)
                .generateRegistry(StringDataFormat.class, ResourceKey.sponge("string_data_format"), Stream.of(new JsonDataFormat(ResourceKey.sponge("json")), new HoconDataFormat(ResourceKey.sponge("hocon"))), true, false)
                .generateRegistry(DataFormat.class, ResourceKey.sponge("data_format"), Stream.of(new NBTDataFormat(ResourceKey.sponge("nbt"))), true, false)
                .generateRegistry(TeleportHelperFilter.class, ResourceKey.sponge("teleport_helper_filter"), TeleportHelperFilterStreamGenerator.stream(), true, false)
                .generateRegistry(Operation.class, ResourceKey.sponge("block_operation"), BlockOperationStreamGenerator.stream(), true, false)
                .generateRegistry(TransactionType.class, ResourceKey.sponge("transaction_type"), BlockTransactionTypeStreamGenerator.stream(), true, false)
        ;
    }
}
