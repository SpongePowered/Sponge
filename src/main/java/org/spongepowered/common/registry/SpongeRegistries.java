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
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.monster.PhantomEntity;
import net.minecraft.entity.monster.SpellcastingIllagerEntity;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.entity.passive.MooshroomEntity;
import net.minecraft.entity.passive.PandaEntity;
import net.minecraft.entity.passive.fish.TropicalFishEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.item.ItemTier;
import net.minecraft.scoreboard.ScoreCriteria;
import net.minecraft.scoreboard.Team;
import net.minecraft.state.properties.ChestType;
import net.minecraft.state.properties.DoorHingeSide;
import net.minecraft.state.properties.Half;
import net.minecraft.state.properties.NoteBlockInstrument;
import net.minecraft.state.properties.RailShape;
import net.minecraft.state.properties.RedstoneSide;
import net.minecraft.state.properties.SlabType;
import net.minecraft.state.properties.StairsShape;
import net.minecraft.tileentity.BannerPattern;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraft.world.GameType;
import net.minecraft.world.raid.Raid;
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
import org.spongepowered.api.data.type.ArmorMaterial;
import org.spongepowered.api.data.type.AttachmentSurface;
import org.spongepowered.api.data.type.BannerPatternShape;
import org.spongepowered.api.data.type.BoatType;
import org.spongepowered.api.data.type.BodyPart;
import org.spongepowered.api.data.type.ChestAttachmentType;
import org.spongepowered.api.data.type.ComparatorMode;
import org.spongepowered.api.data.type.DoorHinge;
import org.spongepowered.api.data.type.DyeColor;
import org.spongepowered.api.data.type.FoxType;
import org.spongepowered.api.data.type.HandPreference;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.data.type.InstrumentType;
import org.spongepowered.api.data.type.MatterType;
import org.spongepowered.api.data.type.MooshroomType;
import org.spongepowered.api.data.type.PandaGene;
import org.spongepowered.api.data.type.PhantomPhase;
import org.spongepowered.api.data.type.PickupRule;
import org.spongepowered.api.data.type.PistonType;
import org.spongepowered.api.data.type.PortionType;
import org.spongepowered.api.data.type.RaidStatus;
import org.spongepowered.api.data.type.RailDirection;
import org.spongepowered.api.data.type.SkinPart;
import org.spongepowered.api.data.type.SlabPortion;
import org.spongepowered.api.data.type.SpellType;
import org.spongepowered.api.data.type.StairShape;
import org.spongepowered.api.data.type.StructureMode;
import org.spongepowered.api.data.type.ToolType;
import org.spongepowered.api.data.type.TropicalFishShape;
import org.spongepowered.api.data.type.WireAttachmentType;
import org.spongepowered.api.data.type.WoodType;
import org.spongepowered.api.effect.particle.ParticleOption;
import org.spongepowered.api.effect.sound.music.MusicDisc;
import org.spongepowered.api.entity.ai.goal.GoalExecutorType;
import org.spongepowered.api.entity.attribute.AttributeOperation;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
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
import org.spongepowered.api.registry.Registries;
import org.spongepowered.api.scoreboard.CollisionRule;
import org.spongepowered.api.scoreboard.Visibility;
import org.spongepowered.api.scoreboard.criteria.Criterion;
import org.spongepowered.api.scoreboard.objective.displaymode.ObjectiveDisplayMode;
import org.spongepowered.api.service.ban.BanType;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.account.AccountDeletionResultType;
import org.spongepowered.api.world.WorldArchetype;
import org.spongepowered.api.world.difficulty.Difficulty;
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
import org.spongepowered.common.registry.builtin.sponge.BlockOperationStreamGenerator;
import org.spongepowered.common.registry.builtin.sponge.CatalogedValueParameterStreamGenerator;
import org.spongepowered.common.registry.builtin.sponge.ClickTypeStreamGenerator;
import org.spongepowered.common.registry.builtin.sponge.ClientCompletionKeyStreamGenerator;
import org.spongepowered.common.registry.builtin.sponge.ClientCompletionTypeStreamGenerator;
import org.spongepowered.common.registry.builtin.sponge.CommandRegistrarStreamGenerator;
import org.spongepowered.common.registry.builtin.sponge.DamageModifierTypeStreamGenerator;
import org.spongepowered.common.registry.builtin.sponge.DamageTypeStreamGenerator;
import org.spongepowered.common.registry.builtin.sponge.DismountTypeStreamGenerator;
import org.spongepowered.common.registry.builtin.sponge.EventContextKeyStreamGenerator;
import org.spongepowered.common.registry.builtin.sponge.GoalExecutorTypeStreamGenerator;
import org.spongepowered.common.registry.builtin.sponge.KeyStreamGenerator;
import org.spongepowered.common.registry.builtin.sponge.MatterTypeStreamGenerator;
import org.spongepowered.common.registry.builtin.sponge.MovementTypeStreamGenerator;
import org.spongepowered.common.registry.builtin.sponge.MusicDiscStreamGenerator;
import org.spongepowered.common.registry.builtin.sponge.PaletteTypeStreamGenerator;
import org.spongepowered.common.registry.builtin.sponge.ParticleOptionStreamGenerator;
import org.spongepowered.common.registry.builtin.sponge.PlaceholderParserStreamGenerator;
import org.spongepowered.common.registry.builtin.sponge.PortalTypeStreamGenerator;
import org.spongepowered.common.registry.builtin.sponge.QueryTypeStreamGenerator;
import org.spongepowered.common.registry.builtin.sponge.SelectorSortAlgorithmStreamGenerator;
import org.spongepowered.common.registry.builtin.sponge.SelectorTypeStreamGenerator;
import org.spongepowered.common.registry.builtin.sponge.SkinPartStreamGenerator;
import org.spongepowered.common.registry.builtin.sponge.SpawnTypeStreamGenerator;
import org.spongepowered.common.registry.builtin.sponge.TeleportHelperFilterStreamGenerator;
import org.spongepowered.common.registry.builtin.sponge.WeatherStreamGenerator;
import org.spongepowered.common.registry.builtin.sponge.WoodTypeStreamGenerator;
import org.spongepowered.common.registry.builtin.sponge.WorldArchetypeStreamGenerator;
import org.spongepowered.common.registry.builtin.vanilla.CriterionStreamGenerator;
import org.spongepowered.common.registry.builtin.vanilla.EquipmentGroupStreamGenerator;
import org.spongepowered.common.registry.builtin.vanilla.EquipmentTypeStreamGenerator;

import java.util.Arrays;
import java.util.stream.Stream;

public final class SpongeRegistries {

    public static void registerGlobalRegistries(final SpongeRegistryHolder holder) {
        holder
                .registerSimple0(Registries.ACCOUNT_DELETION_RESULT_TYPE, false, )
        this
                .generateRegistry(AccountDeletionResultType.class, ResourceKey.sponge("account_deletion_result_type"), AccountDeletionResultTypeGenerator
                        .stream(), true, false)
                .generateRegistry(AdvancementType.class, ResourceKey.minecraft("advancement_type"), Arrays.stream(FrameType.values()), true, false)
                .generateRegistry(ArmorMaterial.class, ResourceKey.minecraft("armor_material"), Arrays.stream(net.minecraft.item.ArmorMaterial.values()), true, false)
                .generateRegistry(AttachmentSurface.class, ResourceKey.minecraft("attach_face"), Arrays.stream(net.minecraft.state.properties.AttachFace.values()), true, false)
                .generateRegistry(AttributeOperation.class, ResourceKey.minecraft("attribute_operation"), Arrays.stream(AttributeModifier.Operation.values()), true, false)
                .generateRegistry(BanType.class, ResourceKey.minecraft("ban_type"), BanTypeStreamGenerator.stream(), true, false)
                .generateRegistry(BannerPatternShape.class, ResourceKey.minecraft("banner_pattern_shape"), Arrays.stream(BannerPattern.values()), true, false)
                .generateRegistry(BoatType.class, ResourceKey.minecraft("boat_type"), Arrays.stream(net.minecraft.entity.item.BoatEntity.Type.values()), true, false)
                .generateRegistry(BodyPart.class, ResourceKey.minecraft("body_part"), BodyPartStreamGenerator.stream(), true, false)
                .generateRegistry(ChestAttachmentType.class, ResourceKey.minecraft("chest_attachment_type"), Arrays.stream(ChestType.values()), true, false)
                .generateRegistry(ClientCompletionKey.class, ResourceKey.sponge("client_completion_key"), ClientCompletionKeyStreamGenerator.stream(), true, false)
                .generateRegistry(ClientCompletionType.class, ResourceKey.sponge("client_completion_type"), ClientCompletionTypeStreamGenerator.stream(), true, false)
                .generateRegistry(CollisionRule.class, ResourceKey.minecraft("collision_rule"), Arrays.stream(Team.CollisionRule.values()), true, false)
                .generateRegistry(ComparatorMode.class, ResourceKey.minecraft("comparator_mode"), Arrays.stream(net.minecraft.state.properties.ComparatorMode.values()), true, false)
                .generateRegistry(Criterion.class, ResourceKey.sponge("criterion"), CriterionStreamGenerator.stream(), true, false)
                .registerRegistry(Currency.class, ResourceKey.sponge("currency"), true)
                .generateRegistry(DamageModifierType.class, ResourceKey.sponge("damage_modifier_type"), DamageModifierTypeStreamGenerator.stream(), true, true)
                .generateRegistry(DamageType.class, ResourceKey.sponge("damage_type"), DamageTypeStreamGenerator.stream(), true, true)
                .generateCallbackRegistry(DataRegistration.class, ResourceKey.sponge("data_registration"), Stream.empty(), (key, value) -> ((SpongeDataManager) Sponge.getGame().getDataManager()).registerCustomDataRegistration((SpongeDataRegistration) value), false, true)
                .generateRegistry(Difficulty.class, ResourceKey.sponge("difficulty"), Arrays.stream(net.minecraft.world.Difficulty.values()), true, false)
                .generateRegistry(DismountType.class, ResourceKey.minecraft("dismount_type"), DismountTypeStreamGenerator.stream(), true, false)
                .generateRegistry(DyeColor.class, ResourceKey.minecraft("dye_color"), Arrays.stream(net.minecraft.item.DyeColor.values()), true, false)
                .generateRegistry(CatalogedValueParameter.class, ResourceKey.sponge("value_parameter"), CatalogedValueParameterStreamGenerator.stream(), true, true)
                .generateRegistry(CommandRegistrar.class, ResourceKey.sponge("command_registrar"), CommandRegistrarStreamGenerator.stream(), true, true)
                .generateRegistry(EquipmentGroup.class, ResourceKey.minecraft("equipment_group"), EquipmentGroupStreamGenerator.stream(), true, false)
                .generateRegistry(EquipmentType.class, ResourceKey.minecraft("equipment_type"), EquipmentTypeStreamGenerator.stream(), true, false)
                .generateRegistry(EventContextKey.class, ResourceKey.sponge("event_context_key"), EventContextKeyStreamGenerator.stream(), true, true)
                .generateRegistry(FoxType.class, ResourceKey.minecraft("fox_type"), Arrays.stream(FoxEntity.Type.values()), true, false)
                .generateRegistry(GameMode.class, ResourceKey.minecraft("game_mode"), Arrays.stream(GameType.values()), true, false)
                .generateRegistry(GoalExecutorType.class, ResourceKey.minecraft("goal_executor_type"), GoalExecutorTypeStreamGenerator.stream(), true, false)
                .generateRegistry(HandPreference.class, ResourceKey.minecraft("hand_preference"), Arrays.stream(HandSide.values()), true, false)
                .generateRegistry(HandType.class, ResourceKey.minecraft("hand_type"), Arrays.stream(Hand.values()), true, false)
                .generateRegistry(DoorHinge.class, ResourceKey.minecraft("door_hinge"), Arrays.stream(DoorHingeSide.values()), true, false)
                .generateRegistry(InstrumentType.class, ResourceKey.minecraft("instrument_type"), Arrays.stream(NoteBlockInstrument.values()), true, false)
                .generateRegistry(Key.class, ResourceKey.sponge("key"), KeyStreamGenerator.stream(), true, true)
                .generateRegistry(MatterType.class, ResourceKey.sponge("matter_type"), MatterTypeStreamGenerator.stream(), true, false)
                .generateRegistry(MooshroomType.class, ResourceKey.minecraft("mooshroom_type"), Arrays.stream(MooshroomEntity.Type.values()), true, false)
                .generateRegistry(MovementType.class, ResourceKey.sponge("movement_type"), MovementTypeStreamGenerator.stream(), true, true)
                .generateRegistry(MusicDisc.class, ResourceKey.minecraft("music_disc"), MusicDiscStreamGenerator.stream(), true, false)
                .generateRegistry(ObjectiveDisplayMode.class, ResourceKey.sponge("objective_display_mode"), Arrays.stream(ScoreCriteria.RenderType.values()),true, false)
                .generateRegistry(PaletteType.class, ResourceKey.sponge("palette"), PaletteTypeStreamGenerator.stream(), true, true)
                .generateRegistry(PandaGene.class, ResourceKey.minecraft("panda_gene"), Arrays.stream(PandaEntity.Type.values()), true, false)
                .generateRegistry(ParticleOption.class, ResourceKey.sponge("particle_option"), ParticleOptionStreamGenerator.stream(), true, false)
                .generateRegistry(PhantomPhase.class, ResourceKey.minecraft("phantom_phase"), Arrays.stream(PhantomEntity.AttackPhase.values()), true, false)
                .generateRegistry(PickupRule.class, ResourceKey.minecraft("pickup_rule"), Arrays.stream(AbstractArrowEntity.PickupStatus.values()), true, false)
                .generateRegistry(PistonType.class, ResourceKey.minecraft("piston_type"), Arrays.stream(net.minecraft.state.properties.PistonType.values()), true, false)
                .generateRegistry(PlaceholderParser.class, ResourceKey.sponge("placeholder"), PlaceholderParserStreamGenerator.stream(), true, true)
                .generateRegistry(PortalType.class, ResourceKey.minecraft("portal_type"), PortalTypeStreamGenerator.stream(), true, true)
                .generateRegistry(PortionType.class, ResourceKey.minecraft("portion_type"), Arrays.stream(Half.values()), true, false)
                .generateRegistry(QueryType.class, ResourceKey.sponge("query_type"), QueryTypeStreamGenerator.stream(), true, true)
                .generateRegistry(RaidStatus.class, ResourceKey.minecraft("raid_status"), Arrays.stream(Raid.Status.values()), true, false)
                .generateRegistry(RailDirection.class, ResourceKey.minecraft("rail_direction"), Arrays.stream(RailShape.values()), true, false)
                .generateRegistry(SelectorSortAlgorithm.class, ResourceKey.minecraft("selector_sort_algorithm"), SelectorSortAlgorithmStreamGenerator.stream(), true, false)
                .generateRegistry(SelectorType.class, ResourceKey.minecraft("selector_type"), SelectorTypeStreamGenerator.stream(), true, false)
                .generateRegistry(SkinPart.class, ResourceKey.minecraft("skin_part"), SkinPartStreamGenerator.stream(), true, false)
                .generateRegistry(SlabPortion.class, ResourceKey.minecraft("slab_portion"), Arrays.stream(SlabType.values()), true, false)
                .generateRegistry(SpawnType.class, ResourceKey.sponge("spawn_type"), SpawnTypeStreamGenerator.stream(), true, true)
                .generateRegistry(SpellType.class, ResourceKey.minecraft("spell_type"), Arrays.stream(SpellcastingIllagerEntity.SpellType.values()), true, false)
                .generateRegistry(StairShape.class, ResourceKey.minecraft("stair_shape"), Arrays.stream(StairsShape.values()), true, false)
                .generateRegistry(StructureMode.class, ResourceKey.minecraft("structure_mode"), Arrays.stream(net.minecraft.state.properties.StructureMode.values()), true, false)
                .generateRegistry(ToolType.class, ResourceKey.minecraft("tool_type"), Arrays.stream(ItemTier.values()), true, false)
                .generateRegistry(TropicalFishShape.class, ResourceKey.minecraft("tropical_fish_shape"), Arrays.stream(TropicalFishEntity.Type.values()), true, false)
                .generateRegistry(Weather.class, ResourceKey.minecraft("weather"), WeatherStreamGenerator.stream(), true, false)
                .generateRegistry(WireAttachmentType.class, ResourceKey.minecraft("wire_attachment_type"), Arrays.stream(RedstoneSide.values()), true, false)
                .generateRegistry(WoodType.class, ResourceKey.minecraft("wood_type"), WoodTypeStreamGenerator.stream(), true, false)
                .generateRegistry(WorldArchetype.class, ResourceKey.minecraft("world_archetype"), WorldArchetypeStreamGenerator.stream(), true, true)
                .generateRegistry(Visibility.class, ResourceKey.minecraft("visibility"), Arrays.stream(Team.Visible.values()), true, false)
                .generateRegistry(ClickType.class, ResourceKey.minecraft("click_type"), ClickTypeStreamGenerator.stream(), true, false)
                .generateRegistry(StringDataFormat.class, ResourceKey.sponge("string_data_format"), Stream.of(new JsonDataFormat(ResourceKey.sponge("json")), new HoconDataFormat(ResourceKey.sponge("hocon"))), true, false)
                .generateRegistry(DataFormat.class, ResourceKey.sponge("data_format"), Stream.of(new NBTDataFormat(ResourceKey.sponge("nbt"))), true, false)
                .generateRegistry(TeleportHelperFilter.class, ResourceKey.sponge("teleport_helper_filter"), TeleportHelperFilterStreamGenerator.stream(), true, false)
                .generateRegistry(Operation.class, ResourceKey.sponge("block_operation"), BlockOperationStreamGenerator.stream(), true, false)
                .generateRegistry(TransactionType.class, ResourceKey.sponge("transaction_type"), BlockTransactionTypeStreamGenerator.stream(), true, false)
        ;
    }
}
