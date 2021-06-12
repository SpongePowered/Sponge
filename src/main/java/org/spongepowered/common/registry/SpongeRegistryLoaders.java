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

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.commands.arguments.CompoundTagArgument;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.ScoreHolderArgument;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.commands.arguments.blocks.BlockStateArgument;
import net.minecraft.commands.arguments.coordinates.RotationArgument;
import net.minecraft.commands.arguments.coordinates.Vec2Argument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.commands.synchronization.brigadier.DoubleArgumentSerializer;
import net.minecraft.commands.synchronization.brigadier.FloatArgumentSerializer;
import net.minecraft.commands.synchronization.brigadier.IntegerArgumentSerializer;
import net.minecraft.commands.synchronization.brigadier.LongArgumentSerializer;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.RecordItem;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.phys.Vec2;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.adventure.ResolveOperation;
import org.spongepowered.api.adventure.ResolveOperations;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.transaction.Operation;
import org.spongepowered.api.block.transaction.Operations;
import org.spongepowered.api.command.parameter.managed.ValueParameter;
import org.spongepowered.api.command.parameter.managed.clientcompletion.ClientCompletionType;
import org.spongepowered.api.command.parameter.managed.clientcompletion.ClientCompletionTypes;
import org.spongepowered.api.command.parameter.managed.operator.Operator;
import org.spongepowered.api.command.parameter.managed.operator.Operators;
import org.spongepowered.api.command.parameter.managed.standard.ResourceKeyedValueParameters;
import org.spongepowered.api.command.registrar.CommandRegistrarType;
import org.spongepowered.api.command.registrar.tree.CommandCompletionProvider;
import org.spongepowered.api.command.registrar.tree.CommandCompletionProviders;
import org.spongepowered.api.command.registrar.tree.CommandTreeNodeType;
import org.spongepowered.api.command.registrar.tree.CommandTreeNodeTypes;
import org.spongepowered.api.command.selector.SelectorSortAlgorithm;
import org.spongepowered.api.command.selector.SelectorSortAlgorithms;
import org.spongepowered.api.command.selector.SelectorType;
import org.spongepowered.api.command.selector.SelectorTypes;
import org.spongepowered.api.data.KeyValueMatcher;
import org.spongepowered.api.data.persistence.DataFormat;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.data.type.BodyPart;
import org.spongepowered.api.data.type.BodyParts;
import org.spongepowered.api.data.type.CatType;
import org.spongepowered.api.data.type.CatTypes;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.data.type.HorseColor;
import org.spongepowered.api.data.type.HorseColors;
import org.spongepowered.api.data.type.HorseStyle;
import org.spongepowered.api.data.type.HorseStyles;
import org.spongepowered.api.data.type.LlamaType;
import org.spongepowered.api.data.type.LlamaTypes;
import org.spongepowered.api.data.type.MatterType;
import org.spongepowered.api.data.type.MatterTypes;
import org.spongepowered.api.data.type.NotePitch;
import org.spongepowered.api.data.type.NotePitches;
import org.spongepowered.api.data.type.ParrotType;
import org.spongepowered.api.data.type.ParrotTypes;
import org.spongepowered.api.data.type.RabbitType;
import org.spongepowered.api.data.type.RabbitTypes;
import org.spongepowered.api.data.type.SkinPart;
import org.spongepowered.api.data.type.SkinParts;
import org.spongepowered.api.effect.particle.ParticleOption;
import org.spongepowered.api.effect.particle.ParticleOptions;
import org.spongepowered.api.effect.potion.PotionEffectType;
import org.spongepowered.api.effect.sound.music.MusicDisc;
import org.spongepowered.api.effect.sound.music.MusicDiscs;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.ai.goal.GoalExecutorType;
import org.spongepowered.api.entity.ai.goal.GoalExecutorTypes;
import org.spongepowered.api.entity.ai.goal.GoalType;
import org.spongepowered.api.entity.ai.goal.GoalTypes;
import org.spongepowered.api.entity.ai.goal.builtin.LookAtGoal;
import org.spongepowered.api.entity.ai.goal.builtin.LookRandomlyGoal;
import org.spongepowered.api.entity.ai.goal.builtin.SwimGoal;
import org.spongepowered.api.entity.ai.goal.builtin.creature.AttackLivingGoal;
import org.spongepowered.api.entity.ai.goal.builtin.creature.AvoidLivingGoal;
import org.spongepowered.api.entity.ai.goal.builtin.creature.RandomWalkingGoal;
import org.spongepowered.api.entity.ai.goal.builtin.creature.RangedAttackAgainstAgentGoal;
import org.spongepowered.api.entity.ai.goal.builtin.creature.horse.RunAroundLikeCrazyGoal;
import org.spongepowered.api.entity.ai.goal.builtin.creature.target.FindNearestAttackableTargetGoal;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.entity.DismountType;
import org.spongepowered.api.event.cause.entity.DismountTypes;
import org.spongepowered.api.event.cause.entity.MovementType;
import org.spongepowered.api.event.cause.entity.MovementTypes;
import org.spongepowered.api.event.cause.entity.SpawnType;
import org.spongepowered.api.event.cause.entity.SpawnTypes;
import org.spongepowered.api.event.cause.entity.damage.DamageModifierType;
import org.spongepowered.api.event.cause.entity.damage.DamageModifierTypes;
import org.spongepowered.api.event.cause.entity.damage.DamageType;
import org.spongepowered.api.event.cause.entity.damage.DamageTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.menu.ClickType;
import org.spongepowered.api.item.inventory.menu.ClickTypes;
import org.spongepowered.api.item.inventory.query.QueryType;
import org.spongepowered.api.item.inventory.query.QueryTypes;
import org.spongepowered.api.map.color.MapColorType;
import org.spongepowered.api.map.color.MapColorTypes;
import org.spongepowered.api.map.color.MapShade;
import org.spongepowered.api.map.color.MapShades;
import org.spongepowered.api.map.decoration.MapDecorationType;
import org.spongepowered.api.map.decoration.MapDecorationTypes;
import org.spongepowered.api.map.decoration.orientation.MapDecorationOrientation;
import org.spongepowered.api.map.decoration.orientation.MapDecorationOrientations;
import org.spongepowered.api.placeholder.PlaceholderParser;
import org.spongepowered.api.placeholder.PlaceholderParsers;
import org.spongepowered.api.scoreboard.displayslot.DisplaySlot;
import org.spongepowered.api.scoreboard.displayslot.DisplaySlots;
import org.spongepowered.api.service.ban.Ban;
import org.spongepowered.api.service.ban.BanType;
import org.spongepowered.api.service.ban.BanTypes;
import org.spongepowered.api.service.economy.account.AccountDeletionResultType;
import org.spongepowered.api.service.economy.account.AccountDeletionResultTypes;
import org.spongepowered.api.util.Color;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.util.Nameable;
import org.spongepowered.api.world.Locatable;
import org.spongepowered.api.world.portal.PortalType;
import org.spongepowered.api.world.portal.PortalTypes;
import org.spongepowered.api.world.schematic.PaletteType;
import org.spongepowered.api.world.schematic.PaletteTypes;
import org.spongepowered.api.world.teleport.TeleportHelperFilter;
import org.spongepowered.api.world.teleport.TeleportHelperFilters;
import org.spongepowered.api.world.weather.WeatherType;
import org.spongepowered.api.world.weather.WeatherTypes;
import org.spongepowered.common.accessor.commands.arguments.DimensionArgumentAccessor;
import org.spongepowered.common.accessor.commands.synchronization.ArgumentTypesAccessor;
import org.spongepowered.common.accessor.commands.synchronization.EmptyArgumentSerializerAccessor;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.common.adventure.SpongeResolveOperation;
import org.spongepowered.common.ban.SpongeBanType;
import org.spongepowered.common.block.BlockStateSerializerDeserializer;
import org.spongepowered.common.block.transaction.BlockOperation;
import org.spongepowered.common.command.brigadier.argument.ClientNativeArgumentParser;
import org.spongepowered.common.command.parameter.managed.clientcompletion.SpongeClientCompletionType;
import org.spongepowered.common.command.parameter.managed.operator.SpongeAdditionOperator;
import org.spongepowered.common.command.parameter.managed.operator.SpongeDivisionOperator;
import org.spongepowered.common.command.parameter.managed.operator.SpongeMaxOperator;
import org.spongepowered.common.command.parameter.managed.operator.SpongeMinOperator;
import org.spongepowered.common.command.parameter.managed.operator.SpongeModulusOperator;
import org.spongepowered.common.command.parameter.managed.operator.SpongeMultiplicationOperator;
import org.spongepowered.common.command.parameter.managed.operator.SpongeOperator;
import org.spongepowered.common.command.parameter.managed.operator.SpongeSubtractionOperator;
import org.spongepowered.common.command.parameter.managed.standard.SpongeBigDecimalValueParameter;
import org.spongepowered.common.command.parameter.managed.standard.SpongeBigIntegerValueParameter;
import org.spongepowered.common.command.parameter.managed.standard.SpongeColorValueParameter;
import org.spongepowered.common.command.parameter.managed.standard.SpongeDataContainerValueParameter;
import org.spongepowered.common.command.parameter.managed.standard.SpongeDateTimeValueParameter;
import org.spongepowered.common.command.parameter.managed.standard.SpongeDurationValueParameter;
import org.spongepowered.common.command.parameter.managed.standard.SpongeGameProfileValueParameter;
import org.spongepowered.common.command.parameter.managed.standard.SpongeIPAddressValueParameter;
import org.spongepowered.common.command.parameter.managed.standard.SpongeNoneValueParameter;
import org.spongepowered.common.command.parameter.managed.standard.SpongeOperatorValueParameter;
import org.spongepowered.common.command.parameter.managed.standard.SpongePluginContainerValueParameter;
import org.spongepowered.common.command.parameter.managed.standard.SpongeServerLocationValueParameter;
import org.spongepowered.common.command.parameter.managed.standard.SpongeTargetBlockValueParameter;
import org.spongepowered.common.command.parameter.managed.standard.SpongeTargetEntityValueParameter;
import org.spongepowered.common.command.parameter.managed.standard.SpongeUserValueParameter;
import org.spongepowered.common.command.registrar.BrigadierCommandRegistrar;
import org.spongepowered.common.command.registrar.SpongeCommandRegistrarTypes;
import org.spongepowered.common.command.registrar.SpongeParameterizedCommandRegistrar;
import org.spongepowered.common.command.registrar.SpongeRawCommandRegistrar;
import org.spongepowered.common.command.registrar.tree.key.SpongeAmountCommandTreeNodeType;
import org.spongepowered.common.command.registrar.tree.key.SpongeBasicCommandTreeNodeType;
import org.spongepowered.common.command.registrar.tree.key.SpongeEntityCommandTreeNodeType;
import org.spongepowered.common.command.registrar.tree.key.SpongeRangeCommandTreeNodeType;
import org.spongepowered.common.command.registrar.tree.key.SpongeStringCommandTreeNodeType;
import org.spongepowered.common.command.selector.SpongeSelectorSortAlgorithm;
import org.spongepowered.common.command.selector.SpongeSelectorType;
import org.spongepowered.common.data.nbt.validation.SpongeValidationType;
import org.spongepowered.common.data.nbt.validation.ValidationType;
import org.spongepowered.common.data.nbt.validation.ValidationTypes;
import org.spongepowered.common.data.persistence.HoconDataFormat;
import org.spongepowered.common.data.persistence.JsonDataFormat;
import org.spongepowered.common.data.persistence.NBTDataFormat;
import org.spongepowered.common.data.type.SpongeBodyPart;
import org.spongepowered.common.data.type.SpongeCatType;
import org.spongepowered.common.data.type.SpongeHorseColor;
import org.spongepowered.common.data.type.SpongeHorseStyle;
import org.spongepowered.common.data.type.SpongeLlamaType;
import org.spongepowered.common.data.type.SpongeMatterType;
import org.spongepowered.common.data.type.SpongeNotePitch;
import org.spongepowered.common.data.type.SpongeParrotType;
import org.spongepowered.common.data.type.SpongeRabbitType;
import org.spongepowered.common.data.type.SpongeSkinPart;
import org.spongepowered.common.economy.SpongeAccountDeletionResultType;
import org.spongepowered.common.effect.particle.SpongeParticleOption;
import org.spongepowered.common.effect.record.SpongeMusicDisc;
import org.spongepowered.common.entity.ai.SpongeGoalExecutorType;
import org.spongepowered.common.entity.ai.goal.SpongeGoalType;
import org.spongepowered.common.event.cause.entity.SpongeDismountType;
import org.spongepowered.common.event.cause.entity.SpongeMovementType;
import org.spongepowered.common.event.cause.entity.SpongeSpawnType;
import org.spongepowered.common.event.cause.entity.SpongeSpawnTypes;
import org.spongepowered.common.event.cause.entity.damage.SpongeDamageModifierType;
import org.spongepowered.common.event.cause.entity.damage.SpongeDamageType;
import org.spongepowered.common.event.tracking.context.transaction.type.BlockTransactionType;
import org.spongepowered.common.event.tracking.context.transaction.type.NoOpTransactionType;
import org.spongepowered.common.event.tracking.context.transaction.type.TransactionType;
import org.spongepowered.common.event.tracking.context.transaction.type.TransactionTypes;
import org.spongepowered.common.inventory.menu.handler.SpongeClickType;
import org.spongepowered.common.inventory.query.SpongeOneParamQueryType;
import org.spongepowered.common.inventory.query.SpongeQueryTypes;
import org.spongepowered.common.inventory.query.SpongeTwoParamQueryType;
import org.spongepowered.common.inventory.query.type.GridQuery;
import org.spongepowered.common.inventory.query.type.InventoryTypeQuery;
import org.spongepowered.common.inventory.query.type.ItemStackCustomQuery;
import org.spongepowered.common.inventory.query.type.ItemStackExactQuery;
import org.spongepowered.common.inventory.query.type.ItemStackIgnoreQuantityQuery;
import org.spongepowered.common.inventory.query.type.ItemTypeQuery;
import org.spongepowered.common.inventory.query.type.KeyValueMatcherQuery;
import org.spongepowered.common.inventory.query.type.LensQuery;
import org.spongepowered.common.inventory.query.type.PlayerPrimaryHotbarFirstQuery;
import org.spongepowered.common.inventory.query.type.ReverseQuery;
import org.spongepowered.common.inventory.query.type.SlotLensQuery;
import org.spongepowered.common.inventory.query.type.TypeQuery;
import org.spongepowered.common.inventory.query.type.UnionQuery;
import org.spongepowered.common.item.SpongeItemStackSnapshot;
import org.spongepowered.common.map.color.SpongeMapColorType;
import org.spongepowered.common.map.color.SpongeMapShade;
import org.spongepowered.common.map.decoration.SpongeMapDecorationBannerType;
import org.spongepowered.common.map.decoration.SpongeMapDecorationType;
import org.spongepowered.common.map.decoration.orientation.SpongeMapDecorationOrientation;
import org.spongepowered.common.placeholder.SpongePlaceholderParserBuilder;
import org.spongepowered.common.scoreboard.SpongeDisplaySlot;
import org.spongepowered.common.scoreboard.SpongeDisplaySlotFactory;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.portal.EndPortalType;
import org.spongepowered.common.world.portal.NetherPortalType;
import org.spongepowered.common.world.schematic.SpongePaletteType;
import org.spongepowered.common.world.teleport.ConfigTeleportHelperFilter;
import org.spongepowered.common.world.teleport.DefaultTeleportHelperFilter;
import org.spongepowered.common.world.teleport.FlyingTeleportHelperFilter;
import org.spongepowered.common.world.teleport.NoPortalTeleportHelperFilter;
import org.spongepowered.common.world.teleport.SurfaceOnlyTeleportHelperFilter;
import org.spongepowered.common.world.weather.SpongeWeatherType;
import org.spongepowered.math.vector.Vector2d;
import org.spongepowered.math.vector.Vector3d;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public final class SpongeRegistryLoaders {

    // @formatter:off

    public static RegistryLoader<AccountDeletionResultType> accountDeletionResultType() {
        return RegistryLoader.of(l -> l .mapping(SpongeAccountDeletionResultType::new, m -> m.add(
                AccountDeletionResultTypes.ABSENT,
                AccountDeletionResultTypes.FAILED,
                AccountDeletionResultTypes.SUCCESS,
                AccountDeletionResultTypes.UNDELETABLE
        )));
    }

    public static RegistryLoader<BanType> banType() {
        return RegistryLoader.of(l -> {
            l.add(BanTypes.IP, k -> new SpongeBanType(Ban.IP.class));
            l.add(BanTypes.PROFILE, k -> new SpongeBanType(Ban.Profile.class));
        });
    }

    public static RegistryLoader<TransactionType<@NonNull ?>> blockTransactionTypes() {
        return RegistryLoader.of(l -> {
            l.add(TransactionTypes.BLOCK, k -> new BlockTransactionType());
            l.add(TransactionTypes.ENTITY_DEATH_DROPS, k -> new NoOpTransactionType<>(false, k.value().toUpperCase(Locale.ROOT)));
            l.add(TransactionTypes.NEIGHBOR_NOTIFICATION, k -> new NoOpTransactionType<>(false, k.value().toUpperCase(Locale.ROOT)));
            l.add(TransactionTypes.SPAWN_ENTITY, k -> new NoOpTransactionType<>(false, k.value().toUpperCase(Locale.ROOT)));
        });
    }

    public static RegistryLoader<BodyPart> bodyPart() {
        return RegistryLoader.of(l -> l.mapping(SpongeBodyPart::new, m -> m.add(
                BodyParts.CHEST,
                BodyParts.HEAD,
                BodyParts.LEFT_ARM,
                BodyParts.LEFT_LEG,
                BodyParts.RIGHT_ARM,
                BodyParts.RIGHT_LEG
        )));
    }

    @SuppressWarnings("ConstantConditions")
    public static RegistryLoader<ValueParameter<?>> valueParameter() {
        return RegistryLoader.of(l -> {
            l.add(ResourceKeyedValueParameters.BIG_DECIMAL, SpongeBigDecimalValueParameter::new);
            l.add(ResourceKeyedValueParameters.BIG_INTEGER, SpongeBigIntegerValueParameter::new);
            l.add(ResourceKeyedValueParameters.BLOCK_STATE, k -> ClientNativeArgumentParser.createConverter(k, BlockStateArgument.block(),
                    (reader, cause, state) -> (BlockState) state.getState()));
            l.add(ResourceKeyedValueParameters.BOOLEAN, k -> ClientNativeArgumentParser.createIdentity(k, BoolArgumentType.bool()));
            l.add(ResourceKeyedValueParameters.COLOR, SpongeColorValueParameter::new);
            l.add(ResourceKeyedValueParameters.DATA_CONTAINER, SpongeDataContainerValueParameter::new);
            l.add(ResourceKeyedValueParameters.DATE_TIME, SpongeDateTimeValueParameter::new);
            l.add(ResourceKeyedValueParameters.DOUBLE, k -> ClientNativeArgumentParser.createIdentity(k, DoubleArgumentType.doubleArg()));
            l.add(ResourceKeyedValueParameters.DURATION, SpongeDurationValueParameter::new);
            l.add(ResourceKeyedValueParameters.ENTITY, k -> ClientNativeArgumentParser.createConverter(k, EntityArgument.entity(), (reader, cause, selector) -> (Entity) selector.findSingleEntity((CommandSourceStack) cause)));
            l.add(ResourceKeyedValueParameters.GAME_PROFILE, SpongeGameProfileValueParameter::new);
            l.add(ResourceKeyedValueParameters.INTEGER, k -> ClientNativeArgumentParser.createIdentity(k, IntegerArgumentType.integer()));
            l.add(ResourceKeyedValueParameters.IP, SpongeIPAddressValueParameter::new);
            l.add(ResourceKeyedValueParameters.ITEM_STACK_SNAPSHOT, k -> ClientNativeArgumentParser.createConverter(k, ItemArgument.item(), (reader, cause, converter) -> new SpongeItemStackSnapshot((ItemStack) (Object) converter.createItemStack(1, true))));
            l.add(ResourceKeyedValueParameters.LOCATION, SpongeServerLocationValueParameter::new);
            l.add(ResourceKeyedValueParameters.LONG, k -> ClientNativeArgumentParser.createIdentity(k, LongArgumentType.longArg()));
            l.add(ResourceKeyedValueParameters.MANY_ENTITIES, k -> ClientNativeArgumentParser.createConverter(k, EntityArgument.entities(), (reader, cause, selector) -> selector.findEntities((CommandSourceStack) cause).stream().map(x -> (Entity) x).collect(Collectors.toList())));
            l.add(ResourceKeyedValueParameters.MANY_GAME_PROFILES, k -> ClientNativeArgumentParser.createConverter(k, GameProfileArgument.gameProfile(), (reader, cause, converter) -> converter.getNames((CommandSourceStack) cause)));
            l.add(ResourceKeyedValueParameters.MANY_PLAYERS, k -> ClientNativeArgumentParser.createConverter(k, EntityArgument.players(), (reader, cause, selector) -> selector.findPlayers((CommandSourceStack) cause)));
            l.add(ResourceKeyedValueParameters.NONE, SpongeNoneValueParameter::new);
            l.add(ResourceKeyedValueParameters.OPERATOR, SpongeOperatorValueParameter::new);
            l.add(ResourceKeyedValueParameters.PLAYER, k -> ClientNativeArgumentParser.createConverter(k, EntityArgument.player(), (reader, cause, selector) -> (Player) selector.findSinglePlayer((CommandSourceStack) cause)));
            l.add(ResourceKeyedValueParameters.PLUGIN, SpongePluginContainerValueParameter::new);
            l.add(ResourceKeyedValueParameters.REMAINING_JOINED_STRINGS, k -> ClientNativeArgumentParser.createIdentity(k, StringArgumentType.greedyString()));
            l.add(ResourceKeyedValueParameters.RESOURCE_KEY, k -> ClientNativeArgumentParser.createConverter(k, ResourceLocationArgument.id(), (reader, cause, resourceLocation) -> (ResourceKey) (Object) resourceLocation));
            l.add(ResourceKeyedValueParameters.ROTATION, k -> ClientNativeArgumentParser.createConverter(k, RotationArgument.rotation(), (reader, cause, coords) -> {
                final Vec2 rotation = coords.getRotation((CommandSourceStack) cause);
                return new Vector3d(rotation.x, rotation.y, 0);
            }));
            l.add(ResourceKeyedValueParameters.STRING, k -> ClientNativeArgumentParser.createIdentity(k, StringArgumentType.string()));
            l.add(ResourceKeyedValueParameters.TARGET_BLOCK, SpongeTargetBlockValueParameter::new);
            l.add(ResourceKeyedValueParameters.TARGET_ENTITY, k -> new SpongeTargetEntityValueParameter(k, false));
            l.add(ResourceKeyedValueParameters.TARGET_PLAYER, k -> new SpongeTargetEntityValueParameter(k, true));
            l.add(ResourceKeyedValueParameters.TEXT_FORMATTING_CODE, k -> ClientNativeArgumentParser.createConverter(k, StringArgumentType.string(), (reader, cause, result) -> LegacyComponentSerializer.legacyAmpersand().deserialize(result)));
            l.add(ResourceKeyedValueParameters.TEXT_FORMATTING_CODE_ALL, k -> ClientNativeArgumentParser.createConverter(k, StringArgumentType.greedyString(), (reader, cause, result) -> LegacyComponentSerializer.legacyAmpersand().deserialize(result)));
            l.add(ResourceKeyedValueParameters.TEXT_JSON, k -> ClientNativeArgumentParser.createConverter(k, ComponentArgument.textComponent(), (reader, cause, result) -> SpongeAdventure.asAdventure(result)));
            l.add(ResourceKeyedValueParameters.TEXT_JSON_ALL, k -> ClientNativeArgumentParser.createConverter(k, StringArgumentType.greedyString(), (reader, cause, result) -> GsonComponentSerializer.gson().deserialize(result)));
            l.add(ResourceKeyedValueParameters.URL, k -> ClientNativeArgumentParser.createConverter(k, StringArgumentType.string(),
                    (reader, cause, input) -> {
                        try {
                            return new URL(input);
                        } catch (final MalformedURLException ex) {
                            throw new SimpleCommandExceptionType(new TextComponent("Could not parse " + input + " as a URL"))
                                    .createWithContext(reader);
                        }
                    })
            );
            l.add(ResourceKeyedValueParameters.USER, SpongeUserValueParameter::new);
            l.add(ResourceKeyedValueParameters.UUID, k -> ClientNativeArgumentParser.createIdentity(k, UuidArgument.uuid()));
            l.add(ResourceKeyedValueParameters.VECTOR2D, k -> ClientNativeArgumentParser.createConverter(k, Vec2Argument.vec2(),
                    (reader, cause, result) -> {
                        final net.minecraft.world.phys.Vec3 r = result.getPosition((CommandSourceStack) cause);
                        return new Vector2d(r.x, r.z);
                    })
            );
            l.add(ResourceKeyedValueParameters.VECTOR3D, k -> ClientNativeArgumentParser.createConverter(k, Vec3Argument.vec3(), (reader, cause, result) -> VecHelper.toVector3d(result.getPosition((CommandSourceStack) cause))));
            l.add(ResourceKeyedValueParameters.WORLD, k -> ClientNativeArgumentParser.createConverter(k,
                    DimensionArgument.dimension(),
                    (reader, cause, result) -> Sponge.server().worldManager().world((ResourceKey) (Object) result)
                            .orElseThrow(() -> DimensionArgumentAccessor.accessor$ERROR_INVALID_VALUE().createWithContext(reader, result))
                    ));
        });
    }

    public static RegistryLoader<CatType> catType() {
        return RegistryLoader.of(l -> {
            l.add(10, CatTypes.ALL_BLACK, SpongeCatType::new);
            l.add(1, CatTypes.BLACK, SpongeCatType::new);
            l.add(4, CatTypes.BRITISH_SHORTHAIR, SpongeCatType::new);
            l.add(5, CatTypes.CALICO, SpongeCatType::new);
            l.add(9, CatTypes.JELLIE, SpongeCatType::new);
            l.add(6, CatTypes.PERSIAN, SpongeCatType::new);
            l.add(7, CatTypes.RAGDOLL, SpongeCatType::new);
            l.add(2, CatTypes.RED, SpongeCatType::new);
            l.add(3, CatTypes.SIAMESE, SpongeCatType::new);
            l.add(0, CatTypes.TABBY, SpongeCatType::new);
            l.add(8, CatTypes.WHITE, SpongeCatType::new);
        });
    }

    public static RegistryLoader<ClickType<?>> clickType() {
        return RegistryLoader.of(l -> l.mapping(SpongeClickType::new, m -> m.add(
                ClickTypes.CLICK_LEFT,
                ClickTypes.CLICK_LEFT_OUTSIDE,
                ClickTypes.CLICK_MIDDLE,
                ClickTypes.CLICK_RIGHT,
                ClickTypes.CLICK_RIGHT_OUTSIDE,
                ClickTypes.DOUBLE_CLICK,
                ClickTypes.DRAG_END,
                ClickTypes.DRAG_LEFT_ADD,
                ClickTypes.DRAG_MIDDLE_ADD,
                ClickTypes.DRAG_RIGHT_ADD,
                ClickTypes.DRAG_START,
                ClickTypes.KEY_SWAP,
                ClickTypes.KEY_THROW_ALL,
                ClickTypes.KEY_THROW_ONE,
                ClickTypes.SHIFT_CLICK_LEFT,
                ClickTypes.SHIFT_CLICK_RIGHT
        )));
    }

    public static RegistryLoader<CommandTreeNodeType<?>> clientCompletionKey() {
        final Function<ResourceKey, ArgumentType<?>> fn = key -> ((EmptyArgumentSerializerAccessor<?>) ArgumentTypesAccessor.accessor$BY_NAME().get(key).accessor$serializer()).accessor$constructor().get();
        return RegistryLoader.of(l -> {
            l.add(CommandTreeNodeTypes.ANGLE, k -> new SpongeBasicCommandTreeNodeType(k, fn.apply(k)));
            l.add(CommandTreeNodeTypes.BLOCK_POS, k -> new SpongeBasicCommandTreeNodeType(k, fn.apply(k)));
            l.add(CommandTreeNodeTypes.BLOCK_PREDICATE, k -> new SpongeBasicCommandTreeNodeType(k, fn.apply(k)));
            l.add(CommandTreeNodeTypes.BLOCK_STATE, k -> new SpongeBasicCommandTreeNodeType(k, fn.apply(k)));
            l.add(CommandTreeNodeTypes.BOOL, k -> new SpongeBasicCommandTreeNodeType(k, fn.apply(k)));
            l.add(CommandTreeNodeTypes.COLOR, k -> new SpongeBasicCommandTreeNodeType(k, fn.apply(k)));
            l.add(CommandTreeNodeTypes.COLUMN_POS, k -> new SpongeBasicCommandTreeNodeType(k, fn.apply(k)));
            l.add(CommandTreeNodeTypes.COMPONENT, k -> new SpongeBasicCommandTreeNodeType(k, fn.apply(k)));
            l.add(CommandTreeNodeTypes.DIMENSION, k -> new SpongeBasicCommandTreeNodeType(k, fn.apply(k)));
            l.add(CommandTreeNodeTypes.DOUBLE, k -> SpongeRangeCommandTreeNodeType.createFrom(k, new DoubleArgumentSerializer()));
            l.add(CommandTreeNodeTypes.ENTITY, SpongeEntityCommandTreeNodeType::new);
            l.add(CommandTreeNodeTypes.ENTITY_ANCHOR, k -> new SpongeBasicCommandTreeNodeType(k, fn.apply(k)));
            l.add(CommandTreeNodeTypes.ENTITY_SUMMON, k -> new SpongeBasicCommandTreeNodeType(k, fn.apply(k)));
            l.add(CommandTreeNodeTypes.FLOAT, k -> SpongeRangeCommandTreeNodeType.createFrom(k, new FloatArgumentSerializer()));
            l.add(CommandTreeNodeTypes.FLOAT_RANGE, k -> new SpongeBasicCommandTreeNodeType(k, fn.apply(k)));
            l.add(CommandTreeNodeTypes.FUNCTION, k -> new SpongeBasicCommandTreeNodeType(k, fn.apply(k)));
            l.add(CommandTreeNodeTypes.GAME_PROFILE, k -> new SpongeBasicCommandTreeNodeType(k, fn.apply(k)));
            l.add(CommandTreeNodeTypes.INTEGER, k -> SpongeRangeCommandTreeNodeType.createFrom(k, new IntegerArgumentSerializer()));
            l.add(CommandTreeNodeTypes.INT_RANGE, k -> new SpongeBasicCommandTreeNodeType(k, fn.apply(k)));
            l.add(CommandTreeNodeTypes.ITEM_ENCHANTMENT, k -> new SpongeBasicCommandTreeNodeType(k, fn.apply(k)));
            l.add(CommandTreeNodeTypes.ITEM_PREDICATE, k -> new SpongeBasicCommandTreeNodeType(k, fn.apply(k)));
            l.add(CommandTreeNodeTypes.ITEM_SLOT, k -> new SpongeBasicCommandTreeNodeType(k, fn.apply(k)));
            l.add(CommandTreeNodeTypes.ITEM_STACK, k -> new SpongeBasicCommandTreeNodeType(k, fn.apply(k)));
            l.add(CommandTreeNodeTypes.LONG, k -> SpongeRangeCommandTreeNodeType.createFrom(k, new LongArgumentSerializer()));
            l.add(CommandTreeNodeTypes.MESSAGE, k -> new SpongeBasicCommandTreeNodeType(k, fn.apply(k)));
            l.add(CommandTreeNodeTypes.MOB_EFFECT, k -> new SpongeBasicCommandTreeNodeType(k, fn.apply(k)));
            l.add(CommandTreeNodeTypes.NBT_COMPOUND_TAG, k -> new SpongeBasicCommandTreeNodeType(k, fn.apply(k)));
            l.add(CommandTreeNodeTypes.NBT_PATH, k -> new SpongeBasicCommandTreeNodeType(k, fn.apply(k)));
            l.add(CommandTreeNodeTypes.NBT_TAG, k -> new SpongeBasicCommandTreeNodeType(k, fn.apply(k)));
            l.add(CommandTreeNodeTypes.OBJECTIVE, k -> new SpongeBasicCommandTreeNodeType(k, fn.apply(k)));
            l.add(CommandTreeNodeTypes.OBJECTIVE_CRITERIA, k -> new SpongeBasicCommandTreeNodeType(k, fn.apply(k)));
            l.add(CommandTreeNodeTypes.OPERATION, k -> new SpongeBasicCommandTreeNodeType(k, fn.apply(k)));
            l.add(CommandTreeNodeTypes.PARTICLE, k -> new SpongeBasicCommandTreeNodeType(k, fn.apply(k)));
            l.add(CommandTreeNodeTypes.RESOURCE_LOCATION, k -> new SpongeBasicCommandTreeNodeType(k, fn.apply(k)));
            l.add(CommandTreeNodeTypes.ROTATION, k -> new SpongeBasicCommandTreeNodeType(k, fn.apply(k)));
            l.add(CommandTreeNodeTypes.SCORE_HOLDER, k -> new SpongeAmountCommandTreeNodeType(k, ScoreHolderArgument.scoreHolder(), ScoreHolderArgument.scoreHolders()));
            l.add(CommandTreeNodeTypes.SCOREBOARD_SLOT, k -> new SpongeBasicCommandTreeNodeType(k, fn.apply(k)));
            l.add(CommandTreeNodeTypes.STRING, SpongeStringCommandTreeNodeType::new);
            l.add(CommandTreeNodeTypes.SWIZZLE, k -> new SpongeBasicCommandTreeNodeType(k, fn.apply(k)));
            l.add(CommandTreeNodeTypes.TEAM, k -> new SpongeBasicCommandTreeNodeType(k, fn.apply(k)));
            l.add(CommandTreeNodeTypes.TIME, k -> new SpongeBasicCommandTreeNodeType(k, fn.apply(k)));
            l.add(CommandTreeNodeTypes.UUID, k -> new SpongeBasicCommandTreeNodeType(k, fn.apply(k)));
            l.add(CommandTreeNodeTypes.VEC2, k -> new SpongeBasicCommandTreeNodeType(k, fn.apply(k)));
            l.add(CommandTreeNodeTypes.VEC3, k -> new SpongeBasicCommandTreeNodeType(k, fn.apply(k)));
        });
    }

    public static RegistryLoader<ClientCompletionType> clientCompletionType() {
        return RegistryLoader.of(l -> {
            l.add(ClientCompletionTypes.DECIMAL_NUMBER, k -> new SpongeClientCompletionType(DoubleArgumentType.doubleArg()));
            l.add(ClientCompletionTypes.SNBT, k -> new SpongeClientCompletionType(CompoundTagArgument.compoundTag()));
            l.add(ClientCompletionTypes.NONE, k -> SpongeClientCompletionType.NONE);
            l.add(ClientCompletionTypes.RESOURCE_KEY, k -> new SpongeClientCompletionType(ResourceLocationArgument.id()));
            l.add(ClientCompletionTypes.STRING, k -> new SpongeClientCompletionType(StringArgumentType.string()));
            l.add(ClientCompletionTypes.WHOLE_NUMBER, k -> new SpongeClientCompletionType(LongArgumentType.longArg()));
        });
    }

    public static RegistryLoader<CommandCompletionProvider> clientSuggestionProvider() {
        return RegistryLoader.of(l -> {
            l.add(CommandCompletionProviders.ALL_RECIPES, k -> (CommandCompletionProvider) SuggestionProviders.ALL_RECIPES);
            l.add(CommandCompletionProviders.AVAILABLE_BIOMES, k -> (CommandCompletionProvider) SuggestionProviders.AVAILABLE_BIOMES);
            l.add(CommandCompletionProviders.AVAILABLE_SOUNDS, k -> (CommandCompletionProvider) SuggestionProviders.AVAILABLE_SOUNDS);
            l.add(CommandCompletionProviders.SUMMONABLE_ENTITIES, k -> (CommandCompletionProvider) SuggestionProviders.SUMMONABLE_ENTITIES);
        });
    }

    public static RegistryLoader<CommandRegistrarType<?>> commandRegistrarType() {
        return RegistryLoader.of(l -> {
            l.add(SpongeCommandRegistrarTypes.BRIGADIER, () -> BrigadierCommandRegistrar.TYPE);
            l.add(SpongeCommandRegistrarTypes.MANAGED, () -> SpongeParameterizedCommandRegistrar.TYPE);
            l.add(SpongeCommandRegistrarTypes.RAW, () -> SpongeRawCommandRegistrar.TYPE);
        });
    }

    public static RegistryLoader<DamageModifierType> damageModifierType() {
        return RegistryLoader.of(l -> l.mapping(SpongeDamageModifierType::new, m -> m.add(
                DamageModifierTypes.ABSORPTION,
                DamageModifierTypes.ARMOR,
                DamageModifierTypes.ARMOR_ENCHANTMENT,
                DamageModifierTypes.ATTACK_COOLDOWN,
                DamageModifierTypes.CRITICAL_HIT,
                DamageModifierTypes.DEFENSIVE_POTION_EFFECT,
                DamageModifierTypes.DIFFICULTY,
                DamageModifierTypes.HARD_HAT,
                DamageModifierTypes.MAGIC,
                DamageModifierTypes.NEGATIVE_POTION_EFFECT,
                DamageModifierTypes.OFFENSIVE_POTION_EFFECT,
                DamageModifierTypes.SHIELD,
                DamageModifierTypes.SWEEPING,
                DamageModifierTypes.WEAPON_ENCHANTMENT
        )));
    }

    // TODO Minecraft 1.16.4 - These are not right, someone needs to look into this...
    public static RegistryLoader<DamageType> damageType() {
        return RegistryLoader.of(l -> {
            l.add(DamageTypes.ATTACK, k -> new SpongeDamageType("attack"));
            l.add(DamageTypes.CONTACT, k -> new SpongeDamageType("contact"));
            l.add(DamageTypes.CUSTOM, k -> new SpongeDamageType("custom"));
            l.add(DamageTypes.DROWN, k -> new SpongeDamageType("drown"));
            l.add(DamageTypes.DRYOUT, k -> new SpongeDamageType("dryout"));
            l.add(DamageTypes.EXPLOSIVE, k -> new SpongeDamageType("explosive"));
            l.add(DamageTypes.FALL, k -> new SpongeDamageType("fall"));
            l.add(DamageTypes.FIRE, k -> new SpongeDamageType("inFire"));
            l.add(DamageTypes.GENERIC, k -> new SpongeDamageType("generic"));
            l.add(DamageTypes.HUNGER, k -> new SpongeDamageType("starve"));
            l.add(DamageTypes.MAGIC, k -> new SpongeDamageType("magic"));
            l.add(DamageTypes.MAGMA, k -> new SpongeDamageType("magma"));
            l.add(DamageTypes.PROJECTILE, k -> new SpongeDamageType("projectile"));
            l.add(DamageTypes.SUFFOCATE, k -> new SpongeDamageType("inWall"));
            l.add(DamageTypes.SWEEPING_ATTACK, k -> new SpongeDamageType("sweeping_attack"));
            l.add(DamageTypes.VOID, k -> new SpongeDamageType("outOfWorld"));
        });
    }

    public static RegistryLoader<DismountType> dismountType() {
        return RegistryLoader.of(l -> l.mapping(SpongeDismountType::new, m -> m.add(
                DismountTypes.DEATH,
                DismountTypes.DERAIL,
                DismountTypes.PLAYER
        )));
    }

    public static RegistryLoader<DisplaySlot> displaySlot() {
        return RegistryLoader.of(l -> {
            l.add(0, DisplaySlots.LIST, k -> new SpongeDisplaySlot(0));
            l.add(1, DisplaySlots.SIDEBAR, k -> new SpongeDisplaySlot(1));
            l.add(2, DisplaySlots.BELOW_NAME, k -> new SpongeDisplaySlot(2));

            SpongeDisplaySlotFactory.ColorMapping.COLOR_TO_DISPLAY_SLOT_MAP.forEach((color, s) ->
                            l.add(SpongeDisplaySlot.slotIdFromFormatting(color), s, k -> new SpongeDisplaySlot(color)));
        });
    }

    public static RegistryLoader<GoalExecutorType> goalExecutorType() {
        return RegistryLoader.of(l -> l.mapping(SpongeGoalExecutorType::new, m -> m.add(
                    GoalExecutorTypes.NORMAL,
                    GoalExecutorTypes.TARGET
        )));
    }

    public static RegistryLoader<GoalType> goalType() {
        return RegistryLoader.of(l -> {
            l.add(GoalTypes.ATTACK_LIVING, k -> new SpongeGoalType(AttackLivingGoal.class));
            l.add(GoalTypes.AVOID_LIVING, k -> new SpongeGoalType(AvoidLivingGoal.class));
            l.add(GoalTypes.FIND_NEAREST_ATTACKABLE, k -> new SpongeGoalType(FindNearestAttackableTargetGoal.class));
            l.add(GoalTypes.LOOK_AT, k -> new SpongeGoalType(LookAtGoal.class));
            l.add(GoalTypes.LOOK_RANDOMLY, k -> new SpongeGoalType(LookRandomlyGoal.class));
            l.add(GoalTypes.RANDOM_WALKING, k -> new SpongeGoalType(RandomWalkingGoal.class));
            l.add(GoalTypes.RANGED_ATTACK_AGAINST_AGENT, k -> new SpongeGoalType(RangedAttackAgainstAgentGoal.class));
            l.add(GoalTypes.RUN_AROUND_LIKE_CRAZY, k -> new SpongeGoalType(RunAroundLikeCrazyGoal.class));
            l.add(GoalTypes.SWIM, k -> new SpongeGoalType(SwimGoal.class));
        });
    }

    public static RegistryLoader<HorseColor> horseColor() {
        return RegistryLoader.of(l -> {
            l.add(5, HorseColors.BLACK, SpongeHorseColor::new);
            l.add(3, HorseColors.BROWN, SpongeHorseColor::new);
            l.add(2, HorseColors.CHESTNUT, SpongeHorseColor::new);
            l.add(1, HorseColors.CREAMY, SpongeHorseColor::new);
            l.add(6, HorseColors.DARK_BROWN, SpongeHorseColor::new);
            l.add(5, HorseColors.GRAY, SpongeHorseColor::new);
            l.add(0, HorseColors.WHITE, SpongeHorseColor::new);
        });
    }

    public static RegistryLoader<HorseStyle> horseStyle() {
        return RegistryLoader.of(l -> {
            l.add(4, HorseStyles.BLACK_DOTS, SpongeHorseStyle::new);
            l.add(0, HorseStyles.NONE, SpongeHorseStyle::new);
            l.add(1, HorseStyles.WHITE, SpongeHorseStyle::new);
            l.add(3, HorseStyles.WHITE_DOTS, SpongeHorseStyle::new);
            l.add(2, HorseStyles.WHITEFIELD, SpongeHorseStyle::new);
        });
    }

    public static RegistryLoader<LlamaType> llamaType() {
        return RegistryLoader.of(l -> {
            l.add(2, LlamaTypes.BROWN, SpongeLlamaType::new);
            l.add(0, LlamaTypes.CREAMY, SpongeLlamaType::new);
            l.add(3, LlamaTypes.GRAY, SpongeLlamaType::new);
            l.add(1, LlamaTypes.WHITE, SpongeLlamaType::new);
        });
    }

    public static RegistryLoader<MatterType> matterType() {
        return RegistryLoader.of(l -> l.mapping(SpongeMatterType::new, m -> m.add(
                MatterTypes.GAS,
                MatterTypes.LIQUID,
                MatterTypes.SOLID
        )));
    }

    public static RegistryLoader<MovementType> movementType() {
        return RegistryLoader.of(l -> l.mapping(SpongeMovementType::new, m -> m.add(
                MovementTypes.CHORUS_FRUIT,
                MovementTypes.COMMAND,
                MovementTypes.END_GATEWAY,
                MovementTypes.ENDER_PEARL,
                MovementTypes.ENTITY_TELEPORT,
                MovementTypes.NATURAL,
                MovementTypes.PLUGIN,
                MovementTypes.PORTAL
        )));
    }

    public static RegistryLoader<MusicDisc> musicDisc() {
        return RegistryLoader.of(l -> {
            l.add(MusicDiscs.BLOCKS, k -> new SpongeMusicDisc((RecordItem) Items.MUSIC_DISC_BLOCKS));
            l.add(MusicDiscs.CAT, k -> new SpongeMusicDisc((RecordItem) Items.MUSIC_DISC_CAT));
            l.add(MusicDiscs.CHIRP, k -> new SpongeMusicDisc((RecordItem) Items.MUSIC_DISC_CHIRP));
            l.add(MusicDiscs.FAR, k -> new SpongeMusicDisc((RecordItem) Items.MUSIC_DISC_FAR));
            l.add(MusicDiscs.MALL, k -> new SpongeMusicDisc((RecordItem) Items.MUSIC_DISC_MALL));
            l.add(MusicDiscs.MELLOHI, k -> new SpongeMusicDisc((RecordItem) Items.MUSIC_DISC_MELLOHI));
            l.add(MusicDiscs.MUSIC_DISC_11, k -> new SpongeMusicDisc((RecordItem) Items.MUSIC_DISC_11));
            l.add(MusicDiscs.MUSIC_DISC_13, k -> new SpongeMusicDisc((RecordItem) Items.MUSIC_DISC_13));
            l.add(MusicDiscs.PIGSTEP, k -> new SpongeMusicDisc((RecordItem) Items.MUSIC_DISC_PIGSTEP));
            l.add(MusicDiscs.STAL, k -> new SpongeMusicDisc((RecordItem) Items.MUSIC_DISC_STAL));
            l.add(MusicDiscs.STRAD, k -> new SpongeMusicDisc((RecordItem) Items.MUSIC_DISC_STRAD));
            l.add(MusicDiscs.WAIT, k -> new SpongeMusicDisc((RecordItem) Items.MUSIC_DISC_WAIT));
            l.add(MusicDiscs.WARD, k -> new SpongeMusicDisc((RecordItem) Items.MUSIC_DISC_WARD));
        });
    }

    public static RegistryLoader<NotePitch> notePitch() {
        return RegistryLoader.of(l -> {
            l.add(0, NotePitches.F_SHARP0, SpongeNotePitch::new);
            l.add(1, NotePitches.G0, SpongeNotePitch::new);
            l.add(2, NotePitches.G_SHARP0, SpongeNotePitch::new);
            l.add(3, NotePitches.A1, SpongeNotePitch::new);
            l.add(4, NotePitches.A_SHARP1, SpongeNotePitch::new);
            l.add(5, NotePitches.B1, SpongeNotePitch::new);
            l.add(6, NotePitches.C1, SpongeNotePitch::new);
            l.add(7, NotePitches.C_SHARP1, SpongeNotePitch::new);
            l.add(8, NotePitches.D1, SpongeNotePitch::new);
            l.add(9, NotePitches.D_SHARP1, SpongeNotePitch::new);
            l.add(10, NotePitches.E1, SpongeNotePitch::new);
            l.add(11, NotePitches.F1, SpongeNotePitch::new);
            l.add(12, NotePitches.F_SHARP1, SpongeNotePitch::new);
            l.add(13, NotePitches.G1, SpongeNotePitch::new);
            l.add(14, NotePitches.G_SHARP1, SpongeNotePitch::new);
            l.add(15, NotePitches.A2, SpongeNotePitch::new);
            l.add(16, NotePitches.A_SHARP2, SpongeNotePitch::new);
            l.add(17, NotePitches.B2, SpongeNotePitch::new);
            l.add(18, NotePitches.C2, SpongeNotePitch::new);
            l.add(19, NotePitches.C_SHARP2, SpongeNotePitch::new);
            l.add(20, NotePitches.D2, SpongeNotePitch::new);
            l.add(21, NotePitches.D_SHARP2, SpongeNotePitch::new);
            l.add(22, NotePitches.E2, SpongeNotePitch::new);
            l.add(23, NotePitches.F2, SpongeNotePitch::new);
            l.add(24, NotePitches.F_SHARP2, SpongeNotePitch::new);
        });
    }

    public static RegistryLoader<Operator> operator() {
        return RegistryLoader.of(l -> {
            l.add(Operators.ADDITION, SpongeAdditionOperator::new);
            l.add(Operators.ASSIGN, () -> new SpongeOperator("="));
            l.add(Operators.DIVISION, SpongeDivisionOperator::new);
            l.add(Operators.MAX, SpongeMinOperator::new);
            l.add(Operators.MIN, SpongeMaxOperator::new);
            l.add(Operators.MODULUS, SpongeModulusOperator::new);
            l.add(Operators.MULTIPLICATION, SpongeMultiplicationOperator::new);
            l.add(Operators.SUBTRACTION, SpongeSubtractionOperator::new);
            l.add(Operators.SWAP, () -> new SpongeOperator("><"));
        });
    }

    public static RegistryLoader<Operation> operation() {
        return RegistryLoader.of(l -> l.mapping(BlockOperation::new, m -> m.add(
                Operations.BREAK,
                Operations.DECAY,
                Operations.GROWTH,
                Operations.LIQUID_DECAY,
                Operations.LIQUID_SPREAD,
                Operations.MODIFY,
                Operations.PLACE
        )));
    }

    public static RegistryLoader<PaletteType<?, ?>> paletteType() {
        return RegistryLoader.of(l -> {
            l.add(PaletteTypes.BIOME_PALETTE, k -> new SpongePaletteType<>(
                    (string, registry) -> registry.findValue(ResourceKey.resolve(string)),
                    (registry, biome) -> registry.valueKey(biome).toString()
            ));
            l.add(PaletteTypes.BLOCK_STATE_PALETTE, k -> new SpongePaletteType<>(
                    (string, registry) -> BlockStateSerializerDeserializer.deserialize(string),
                    (registry, blockState) -> BlockStateSerializerDeserializer.serialize(blockState)
            ));
        });
    }

    public static RegistryLoader<ParrotType> parrotType() {
        return RegistryLoader.of(l -> {
            l.add(1, ParrotTypes.BLUE, SpongeParrotType::new);
            l.add(2, ParrotTypes.GREEN, SpongeParrotType::new);
            l.add(4, ParrotTypes.GREY, SpongeParrotType::new);
            l.add(0, ParrotTypes.RED_AND_BLUE, SpongeParrotType::new);
            l.add(3, ParrotTypes.YELLOW_AND_BLUE, SpongeParrotType::new);
        });
    }

    public static RegistryLoader<ParticleOption<?>> particleOption() {
        return RegistryLoader.of(l -> {
            l.add(ParticleOptions.BLOCK_STATE, k -> new SpongeParticleOption<>(BlockState.class));
            l.add(ParticleOptions.COLOR, k -> new SpongeParticleOption<>(Color.class));
            l.add(ParticleOptions.DIRECTION, k -> new SpongeParticleOption<>(Direction.class));
            l.add(ParticleOptions.ITEM_STACK_SNAPSHOT, k -> new SpongeParticleOption<>(ItemStackSnapshot.class));
            l.add(ParticleOptions.OFFSET, k -> new SpongeParticleOption<>(Vector3d.class));
            l.add(ParticleOptions.POTION_EFFECT_TYPE, k -> new SpongeParticleOption<>(PotionEffectType.class));
            l.add(ParticleOptions.QUANTITY, k -> new SpongeParticleOption<>(Integer.class, v -> v < 1 ? new IllegalArgumentException("Quantity must be at least one") : null));
            l.add(ParticleOptions.SCALE, k -> new SpongeParticleOption<>(Double.class, v -> v < 0 ? new IllegalArgumentException("Scale must not be negative") : null));
            l.add(ParticleOptions.VELOCITY, k -> new SpongeParticleOption<>(Vector3d.class));
        });
    }

    public static RegistryLoader<PlaceholderParser> placeholderParser() {
        return RegistryLoader.of(l -> {
            l.add(PlaceholderParsers.CURRENT_WORLD, k -> new SpongePlaceholderParserBuilder()
                    .parser(placeholderText -> Component.text(placeholderText.associatedObject().filter(x -> x instanceof Locatable)
                            .map(x -> ((Locatable) x).serverLocation().worldKey())
                            .orElseGet(() -> Sponge.server().worldManager().defaultWorld().key()).toString()))
                    .build());
            l.add(PlaceholderParsers.NAME, k -> new SpongePlaceholderParserBuilder()
                    .parser(placeholderText -> placeholderText.associatedObject()
                            .filter(x -> x instanceof Nameable)
                            .map(x -> Component.text(((Nameable) x).name()))
                            .orElse(Component.empty()))
                    .build());
        });
    }

    public static RegistryLoader<PortalType> portalType() {
        return RegistryLoader.of(l -> {
            l.add(PortalTypes.END, EndPortalType::new);
            l.add(PortalTypes.NETHER, NetherPortalType::new);
        });
    }

    public static RegistryLoader<QueryType> queryType() {
        return RegistryLoader.of(l -> {
            l.add(QueryTypes.GRID, k -> new SpongeTwoParamQueryType<>(GridQuery::new));
            l.add(QueryTypes.INVENTORY_TYPE, k -> new SpongeOneParamQueryType<>(InventoryTypeQuery::new));
            l.add(QueryTypes.ITEM_STACK_CUSTOM, k -> new SpongeOneParamQueryType<>(ItemStackCustomQuery::new));
            l.add(QueryTypes.ITEM_STACK_EXACT, k -> new SpongeOneParamQueryType<>(ItemStackExactQuery::new));
            l.add(QueryTypes.ITEM_STACK_IGNORE_QUANTITY, k -> new SpongeOneParamQueryType<>(ItemStackIgnoreQuantityQuery::new));
            l.add(QueryTypes.ITEM_TYPE, k -> new SpongeOneParamQueryType<>(ItemTypeQuery::new));
            l.add(QueryTypes.KEY_VALUE, k -> new SpongeOneParamQueryType<KeyValueMatcher<?>>(KeyValueMatcherQuery::new));
            l.add(SpongeQueryTypes.LENS, k -> new SpongeOneParamQueryType<>(LensQuery::new));
            l.add(QueryTypes.PLAYER_PRIMARY_HOTBAR_FIRST, PlayerPrimaryHotbarFirstQuery::new);
            l.add(QueryTypes.REVERSE, ReverseQuery::new);
            l.add(SpongeQueryTypes.SLOT_LENS, k -> new SpongeOneParamQueryType<>(SlotLensQuery::new));
            l.add(QueryTypes.TYPE, k -> new SpongeOneParamQueryType<>(TypeQuery::new));
            l.add(SpongeQueryTypes.UNION, k -> new SpongeOneParamQueryType<>(UnionQuery::new));
        });
    }

    public static RegistryLoader<RabbitType> rabbitType() {
        return RegistryLoader.of(l -> {
            l.add(2, RabbitTypes.BLACK, SpongeRabbitType::new);
            l.add(3, RabbitTypes.BLACK_AND_WHITE, SpongeRabbitType::new);
            l.add(0, RabbitTypes.BROWN, SpongeRabbitType::new);
            l.add(4, RabbitTypes.GOLD, SpongeRabbitType::new);
            l.add(99, RabbitTypes.KILLER, SpongeRabbitType::new);
            l.add(5, RabbitTypes.SALT_AND_PEPPER, SpongeRabbitType::new);
            l.add(1, RabbitTypes.WHITE, SpongeRabbitType::new);
        });
    }

    public static RegistryLoader<ResolveOperation> resolveOperation() {
        return RegistryLoader.of(l -> {
            l.add(ResolveOperations.CONTEXTUAL_COMPONENTS, SpongeResolveOperation::newContextualComponents);
            l.add(ResolveOperations.CUSTOM_TRANSLATIONS, SpongeResolveOperation::newCustomTranslations);
        });
    }

    public static RegistryLoader<SelectorSortAlgorithm> selectorSortAlgorithm() {
        return RegistryLoader.of(l -> {
            l.add(SelectorSortAlgorithms.ORDER_ARBITRARY, k -> new SpongeSelectorSortAlgorithm(EntitySelectorParser.ORDER_ARBITRARY));
            l.add(SelectorSortAlgorithms.ORDER_FURTHEST, k -> new SpongeSelectorSortAlgorithm(EntitySelectorParser.ORDER_FURTHEST));
            l.add(SelectorSortAlgorithms.ORDER_NEAREST, k -> new SpongeSelectorSortAlgorithm(EntitySelectorParser.ORDER_NEAREST));
            l.add(SelectorSortAlgorithms.ORDER_RANDOM, k -> new SpongeSelectorSortAlgorithm(EntitySelectorParser.ORDER_RANDOM));
        });
    }

    public static RegistryLoader<SelectorType> selectorType() {
        return RegistryLoader.of(l -> {
            l.add(SelectorTypes.ALL_ENTITIES, k -> new SpongeSelectorType("@e"));
            l.add(SelectorTypes.ALL_PLAYERS, k -> new SpongeSelectorType("@a"));
            l.add(SelectorTypes.NEAREST_PLAYER, k -> new SpongeSelectorType("@p"));
            l.add(SelectorTypes.RANDOM_PLAYER, k -> new SpongeSelectorType("@r"));
            l.add(SelectorTypes.SOURCE, k -> new SpongeSelectorType("@s"));
        });
    }

    public static RegistryLoader<SkinPart> skinPart() {
        return RegistryLoader.of(l -> {
            l.add(SkinParts.CAPE, k -> new SpongeSkinPart("cape"));
            l.add(SkinParts.HAT, k -> new SpongeSkinPart("hat"));
            l.add(SkinParts.JACKET, k -> new SpongeSkinPart("jacket"));
            l.add(SkinParts.LEFT_PANTS_LEG, k -> new SpongeSkinPart("left_pants_leg"));
            l.add(SkinParts.LEFT_SLEEVE, k -> new SpongeSkinPart("left_sleeve"));
            l.add(SkinParts.RIGHT_PANTS_LEG, k -> new SpongeSkinPart("right_pants_leg"));
            l.add(SkinParts.RIGHT_SLEEVE, k -> new SpongeSkinPart("right_sleeve"));
        });
    }

    public static RegistryLoader<SpawnType> spawnType() {
        return RegistryLoader.<SpawnType>of(l -> {
            l.add(SpongeSpawnTypes.FORCED, k -> new SpongeSpawnType().setForced());
        }).mapping(SpongeSpawnType::new, m -> m.add(
                SpawnTypes.BLOCK_SPAWNING,
                SpawnTypes.BREEDING,
                SpawnTypes.CHUNK_LOAD,
                SpawnTypes.CUSTOM,
                SpawnTypes.DISPENSE,
                SpawnTypes.DROPPED_ITEM,
                SpongeSpawnTypes.ENTITY_DEATH,
                SpawnTypes.EXPERIENCE,
                SpawnTypes.FALLING_BLOCK,
                SpongeSpawnTypes.FORCED,
                SpawnTypes.MOB_SPAWNER,
                SpawnTypes.PASSIVE,
                SpawnTypes.PLACEMENT,
                SpawnTypes.PLUGIN,
                SpawnTypes.PROJECTILE,
                SpawnTypes.SPAWN_EGG,
                SpawnTypes.STRUCTURE,
                SpawnTypes.TNT_IGNITE,
                SpawnTypes.WEATHER,
                SpawnTypes.WORLD_SPAWNER
        ));
    }

    public static RegistryLoader<TeleportHelperFilter> teleportHelperFilter() {
        return RegistryLoader.of(l -> {
            l.add(TeleportHelperFilters.CONFIG, k -> new ConfigTeleportHelperFilter());
            l.add(TeleportHelperFilters.DEFAULT, k -> new DefaultTeleportHelperFilter());
            l.add(TeleportHelperFilters.FLYING, k -> new FlyingTeleportHelperFilter());
            l.add(TeleportHelperFilters.NO_PORTAL, k -> new NoPortalTeleportHelperFilter());
            l.add(TeleportHelperFilters.SURFACE_ONLY, k -> new SurfaceOnlyTeleportHelperFilter());
        });
    }

    public static RegistryLoader<ValidationType> validationType() {
        return RegistryLoader.of(l -> l.mapping(SpongeValidationType::new, m -> m.add(
                ValidationTypes.BLOCK_ENTITY,
                ValidationTypes.ENTITY
        )));
    }

    public static RegistryLoader<WeatherType> weather() {
        return RegistryLoader.of(l -> l.mapping(SpongeWeatherType::new, m -> m.add(
                WeatherTypes.CLEAR,
                WeatherTypes.RAIN,
                WeatherTypes.THUNDER
        )));
    }

    public static RegistryLoader<DataFormat> dataFormat() {
        return RegistryLoader.of(l -> {
            l.add(DataFormats.JSON, k -> new JsonDataFormat());
            l.add(DataFormats.HOCON, k -> new HoconDataFormat());
            l.add(DataFormats.NBT, k -> new NBTDataFormat());
        });
    }

    public static RegistryLoader<MapColorType> mapColorType() {
        return RegistryLoader.of(l -> {
            l.add(MaterialColor.NONE.id, MapColorTypes.NONE, k -> new SpongeMapColorType(MaterialColor.NONE));
            l.add(MaterialColor.GRASS.id, MapColorTypes.GRASS, k -> new SpongeMapColorType(MaterialColor.GRASS));
            l.add(MaterialColor.SAND.id, MapColorTypes.SAND, k -> new SpongeMapColorType(MaterialColor.SAND));
            l.add(MaterialColor.WOOL.id, MapColorTypes.WOOL, k -> new SpongeMapColorType(MaterialColor.WOOL));
            l.add(MaterialColor.FIRE.id, MapColorTypes.FIRE, k -> new SpongeMapColorType(MaterialColor.FIRE));
            l.add(MaterialColor.ICE.id, MapColorTypes.ICE, k -> new SpongeMapColorType(MaterialColor.ICE));
            l.add(MaterialColor.METAL.id, MapColorTypes.METAL, k -> new SpongeMapColorType(MaterialColor.METAL));
            l.add(MaterialColor.PLANT.id, MapColorTypes.PLANT, k -> new SpongeMapColorType(MaterialColor.PLANT));
            l.add(MaterialColor.SNOW.id, MapColorTypes.SNOW, k -> new SpongeMapColorType(MaterialColor.SNOW));
            l.add(MaterialColor.CLAY.id, MapColorTypes.CLAY, k -> new SpongeMapColorType(MaterialColor.CLAY));
            l.add(MaterialColor.DIRT.id, MapColorTypes.DIRT, k -> new SpongeMapColorType(MaterialColor.DIRT));
            l.add(MaterialColor.STONE.id, MapColorTypes.STONE, k -> new SpongeMapColorType(MaterialColor.STONE));
            l.add(MaterialColor.WATER.id, MapColorTypes.WATER, k -> new SpongeMapColorType(MaterialColor.WATER));
            l.add(MaterialColor.WOOD.id, MapColorTypes.WOOD, k -> new SpongeMapColorType(MaterialColor.WOOD));
            l.add(MaterialColor.QUARTZ.id, MapColorTypes.QUARTZ, k -> new SpongeMapColorType(MaterialColor.QUARTZ));
            l.add(MaterialColor.COLOR_ORANGE.id, MapColorTypes.COLOR_ORANGE, k -> new SpongeMapColorType(MaterialColor.COLOR_ORANGE));
            l.add(MaterialColor.COLOR_MAGENTA.id, MapColorTypes.COLOR_MAGENTA, k -> new SpongeMapColorType(MaterialColor.COLOR_MAGENTA));
            l.add(MaterialColor.COLOR_LIGHT_BLUE.id, MapColorTypes.COLOR_LIGHT_BLUE, k -> new SpongeMapColorType(MaterialColor.COLOR_LIGHT_BLUE));
            l.add(MaterialColor.COLOR_YELLOW.id, MapColorTypes.COLOR_YELLOW, k -> new SpongeMapColorType(MaterialColor.COLOR_YELLOW));
            l.add(MaterialColor.COLOR_LIGHT_GREEN.id, MapColorTypes.COLOR_LIGHT_GREEN, k -> new SpongeMapColorType(MaterialColor.COLOR_LIGHT_GREEN));
            l.add(MaterialColor.COLOR_PINK.id, MapColorTypes.COLOR_PINK, k -> new SpongeMapColorType(MaterialColor.COLOR_PINK));
            l.add(MaterialColor.COLOR_GRAY.id, MapColorTypes.COLOR_GRAY, k -> new SpongeMapColorType(MaterialColor.COLOR_GRAY));
            l.add(MaterialColor.COLOR_LIGHT_GRAY.id, MapColorTypes.COLOR_LIGHT_GRAY, k -> new SpongeMapColorType(MaterialColor.COLOR_LIGHT_GRAY));
            l.add(MaterialColor.COLOR_CYAN.id, MapColorTypes.COLOR_CYAN, k -> new SpongeMapColorType(MaterialColor.COLOR_CYAN));
            l.add(MaterialColor.COLOR_PURPLE.id, MapColorTypes.COLOR_PURPLE, k -> new SpongeMapColorType(MaterialColor.COLOR_PURPLE));
            l.add(MaterialColor.COLOR_BLUE.id, MapColorTypes.COLOR_BLUE, k -> new SpongeMapColorType(MaterialColor.COLOR_BLUE));
            l.add(MaterialColor.COLOR_BROWN.id, MapColorTypes.COLOR_BROWN, k -> new SpongeMapColorType(MaterialColor.COLOR_BROWN));
            l.add(MaterialColor.COLOR_GREEN.id, MapColorTypes.COLOR_GREEN, k -> new SpongeMapColorType(MaterialColor.COLOR_GREEN));
            l.add(MaterialColor.COLOR_RED.id, MapColorTypes.COLOR_RED, k -> new SpongeMapColorType(MaterialColor.COLOR_RED));
            l.add(MaterialColor.COLOR_BLACK.id, MapColorTypes.COLOR_BLACK, k -> new SpongeMapColorType(MaterialColor.COLOR_BLACK));
            l.add(MaterialColor.GOLD.id, MapColorTypes.GOLD, k -> new SpongeMapColorType(MaterialColor.GOLD));
            l.add(MaterialColor.DIAMOND.id, MapColorTypes.DIAMOND, k -> new SpongeMapColorType(MaterialColor.DIAMOND));
            l.add(MaterialColor.LAPIS.id, MapColorTypes.LAPIS_LAZULI, k -> new SpongeMapColorType(MaterialColor.LAPIS));
            l.add(MaterialColor.EMERALD.id, MapColorTypes.EMERALD, k -> new SpongeMapColorType(MaterialColor.EMERALD));
            l.add(MaterialColor.PODZOL.id, MapColorTypes.PODZOL, k -> new SpongeMapColorType(MaterialColor.PODZOL));
            l.add(MaterialColor.NETHER.id, MapColorTypes.NETHER, k -> new SpongeMapColorType(MaterialColor.NETHER));
            l.add(MaterialColor.TERRACOTTA_WHITE.id, MapColorTypes.TERRACOTTA_WHITE, k -> new SpongeMapColorType(MaterialColor.TERRACOTTA_WHITE));
            l.add(MaterialColor.TERRACOTTA_ORANGE.id, MapColorTypes.TERRACOTTA_ORANGE, k -> new SpongeMapColorType(MaterialColor.TERRACOTTA_ORANGE));
            l.add(MaterialColor.TERRACOTTA_MAGENTA.id, MapColorTypes.TERRACOTTA_MAGENTA, k -> new SpongeMapColorType(MaterialColor.TERRACOTTA_MAGENTA));
            l.add(MaterialColor.TERRACOTTA_LIGHT_BLUE.id, MapColorTypes.TERRACOTTA_LIGHT_BLUE, k -> new SpongeMapColorType(MaterialColor.TERRACOTTA_LIGHT_BLUE));
            l.add(MaterialColor.TERRACOTTA_YELLOW.id, MapColorTypes.TERRACOTTA_YELLOW, k -> new SpongeMapColorType(MaterialColor.TERRACOTTA_YELLOW));
            l.add(MaterialColor.TERRACOTTA_LIGHT_GREEN.id, MapColorTypes.TERRACOTTA_LIGHT_GREEN, k -> new SpongeMapColorType(MaterialColor.TERRACOTTA_LIGHT_GREEN));
            l.add(MaterialColor.TERRACOTTA_PINK.id, MapColorTypes.TERRACOTTA_PINK, k -> new SpongeMapColorType(MaterialColor.TERRACOTTA_PINK));
            l.add(MaterialColor.TERRACOTTA_GRAY.id, MapColorTypes.TERRACOTTA_GRAY, k -> new SpongeMapColorType(MaterialColor.TERRACOTTA_GRAY));
            l.add(MaterialColor.TERRACOTTA_LIGHT_GRAY.id, MapColorTypes.TERRACOTTA_LIGHT_GRAY, k -> new SpongeMapColorType(MaterialColor.TERRACOTTA_LIGHT_GRAY));
            l.add(MaterialColor.TERRACOTTA_CYAN.id, MapColorTypes.TERRACOTTA_CYAN, k -> new SpongeMapColorType(MaterialColor.TERRACOTTA_CYAN));
            l.add(MaterialColor.TERRACOTTA_PURPLE.id, MapColorTypes.TERRACOTTA_PURPLE, k -> new SpongeMapColorType(MaterialColor.TERRACOTTA_PURPLE));
            l.add(MaterialColor.TERRACOTTA_BLUE.id, MapColorTypes.TERRACOTTA_BLUE, k -> new SpongeMapColorType(MaterialColor.TERRACOTTA_BLUE));
            l.add(MaterialColor.TERRACOTTA_BROWN.id, MapColorTypes.TERRACOTTA_BROWN, k -> new SpongeMapColorType(MaterialColor.TERRACOTTA_BROWN));
            l.add(MaterialColor.TERRACOTTA_GREEN.id, MapColorTypes.TERRACOTTA_GREEN, k -> new SpongeMapColorType(MaterialColor.TERRACOTTA_GREEN));
            l.add(MaterialColor.TERRACOTTA_RED.id, MapColorTypes.TERRACOTTA_RED, k -> new SpongeMapColorType(MaterialColor.TERRACOTTA_RED));
            l.add(MaterialColor.TERRACOTTA_BLACK.id, MapColorTypes.TERRACOTTA_BLACK, k -> new SpongeMapColorType(MaterialColor.TERRACOTTA_BLACK));
            l.add(MaterialColor.CRIMSON_NYLIUM.id, MapColorTypes.CRIMSON_NYLIUM, k -> new SpongeMapColorType(MaterialColor.CRIMSON_NYLIUM));
            l.add(MaterialColor.CRIMSON_STEM.id, MapColorTypes.CRIMSON_STEM, k -> new SpongeMapColorType(MaterialColor.CRIMSON_STEM));
            l.add(MaterialColor.CRIMSON_HYPHAE.id, MapColorTypes.CRIMSON_HYPHAE, k -> new SpongeMapColorType(MaterialColor.CRIMSON_HYPHAE));
            l.add(MaterialColor.WARPED_NYLIUM.id, MapColorTypes.WARPED_NYLIUM, k -> new SpongeMapColorType(MaterialColor.WARPED_NYLIUM));
            l.add(MaterialColor.WARPED_STEM.id, MapColorTypes.WARPED_STEM, k -> new SpongeMapColorType(MaterialColor.WARPED_STEM));
            l.add(MaterialColor.WARPED_HYPHAE.id, MapColorTypes.WARPED_HYPHAE, k -> new SpongeMapColorType(MaterialColor.WARPED_HYPHAE));
            l.add(MaterialColor.WARPED_WART_BLOCK.id, MapColorTypes.WARPED_WART_BLOCK, k -> new SpongeMapColorType(MaterialColor.WARPED_WART_BLOCK));
        });
    }

    public static RegistryLoader<MapDecorationOrientation> mapDecorationOrientation() {
        return RegistryLoader.of(l -> {
            l.add(0, MapDecorationOrientations.SOUTH, k -> new SpongeMapDecorationOrientation(0));
            l.add(1, MapDecorationOrientations.SOUTH_SOUTHWEST, k -> new SpongeMapDecorationOrientation(1));
            l.add(2, MapDecorationOrientations.SOUTHWEST, k -> new SpongeMapDecorationOrientation(2));
            l.add(3, MapDecorationOrientations.WEST_SOUTHWEST, k -> new SpongeMapDecorationOrientation(3));
            l.add(4, MapDecorationOrientations.WEST, k -> new SpongeMapDecorationOrientation(4));
            l.add(5, MapDecorationOrientations.WEST_NORTHWEST, k -> new SpongeMapDecorationOrientation(5));
            l.add(6, MapDecorationOrientations.NORTHWEST, k -> new SpongeMapDecorationOrientation(6));
            l.add(7, MapDecorationOrientations.NORTH_NORTHWEST, k -> new SpongeMapDecorationOrientation(7));
            l.add(8, MapDecorationOrientations.NORTH, k -> new SpongeMapDecorationOrientation(8));
            l.add(9, MapDecorationOrientations.NORTH_NORTHEAST, k -> new SpongeMapDecorationOrientation(9));
            l.add(10, MapDecorationOrientations.NORTHEAST, k -> new SpongeMapDecorationOrientation(10));
            l.add(11, MapDecorationOrientations.EAST_NORTHEAST, k -> new SpongeMapDecorationOrientation(11));
            l.add(12, MapDecorationOrientations.EAST, k -> new SpongeMapDecorationOrientation(12));
            l.add(13, MapDecorationOrientations.EAST_SOUTHEAST, k -> new SpongeMapDecorationOrientation(13));
            l.add(14, MapDecorationOrientations.SOUTHEAST, k -> new SpongeMapDecorationOrientation(14));
            l.add(15, MapDecorationOrientations.SOUTH_SOUTHEAST, k -> new SpongeMapDecorationOrientation(15));
        });
    }

    public static RegistryLoader<MapDecorationType> mapDecorationType() {
        return RegistryLoader.of(l -> {
            l.add(MapDecorationTypes.BLUE_MARKER, k -> new SpongeMapDecorationType(MapDecoration.Type.BLUE_MARKER));
            l.add(MapDecorationTypes.GREEN_MARKER, k -> new SpongeMapDecorationType(MapDecoration.Type.FRAME));
            l.add(MapDecorationTypes.MANSION, k -> new SpongeMapDecorationType(MapDecoration.Type.MANSION));
            l.add(MapDecorationTypes.MONUMENT, k -> new SpongeMapDecorationType(MapDecoration.Type.MONUMENT));
            l.add(MapDecorationTypes.PLAYER_MARKER, k -> new SpongeMapDecorationType(MapDecoration.Type.PLAYER));
            l.add(MapDecorationTypes.PLAYER_OFF_LIMITS, k -> new SpongeMapDecorationType(MapDecoration.Type.PLAYER_OFF_LIMITS));
            l.add(MapDecorationTypes.PLAYER_OFF_MAP, k -> new SpongeMapDecorationType(MapDecoration.Type.PLAYER_OFF_MAP));
            l.add(MapDecorationTypes.RED_MARKER, k -> new SpongeMapDecorationType(MapDecoration.Type.RED_MARKER));
            l.add(MapDecorationTypes.TARGET_POINT, k -> new SpongeMapDecorationType(MapDecoration.Type.TARGET_POINT));
            // banners
            l.add(MapDecorationTypes.BANNER_WHITE, k -> new SpongeMapDecorationBannerType(MapDecoration.Type.BANNER_WHITE, DyeColors.WHITE));
            l.add(MapDecorationTypes.BANNER_ORANGE, k -> new SpongeMapDecorationBannerType(MapDecoration.Type.BANNER_ORANGE, DyeColors.ORANGE));
            l.add(MapDecorationTypes.BANNER_MAGENTA, k -> new SpongeMapDecorationBannerType(MapDecoration.Type.BANNER_MAGENTA, DyeColors.MAGENTA));
            l.add(MapDecorationTypes.BANNER_LIGHT_BLUE, k -> new SpongeMapDecorationBannerType(MapDecoration.Type.BANNER_LIGHT_BLUE, DyeColors.LIGHT_BLUE));
            l.add(MapDecorationTypes.BANNER_YELLOW, k -> new SpongeMapDecorationBannerType(MapDecoration.Type.BANNER_YELLOW, DyeColors.YELLOW));
            l.add(MapDecorationTypes.BANNER_LIME, k -> new SpongeMapDecorationBannerType(MapDecoration.Type.BANNER_LIME, DyeColors.LIME));
            l.add(MapDecorationTypes.BANNER_PINK, k -> new SpongeMapDecorationBannerType(MapDecoration.Type.BANNER_PINK, DyeColors.PINK));
            l.add(MapDecorationTypes.BANNER_GRAY, k -> new SpongeMapDecorationBannerType(MapDecoration.Type.BANNER_GRAY, DyeColors.GRAY));
            l.add(MapDecorationTypes.BANNER_LIGHT_GRAY, k -> new SpongeMapDecorationBannerType(MapDecoration.Type.BANNER_LIGHT_GRAY, DyeColors.LIGHT_GRAY));
            l.add(MapDecorationTypes.BANNER_CYAN, k -> new SpongeMapDecorationBannerType(MapDecoration.Type.BANNER_CYAN, DyeColors.CYAN));
            l.add(MapDecorationTypes.BANNER_PURPLE, k -> new SpongeMapDecorationBannerType(MapDecoration.Type.BANNER_PURPLE, DyeColors.PURPLE));
            l.add(MapDecorationTypes.BANNER_BROWN, k -> new SpongeMapDecorationBannerType(MapDecoration.Type.BANNER_BROWN, DyeColors.BROWN));
            l.add(MapDecorationTypes.BANNER_GREEN, k -> new SpongeMapDecorationBannerType(MapDecoration.Type.BANNER_GREEN, DyeColors.GREEN));
            l.add(MapDecorationTypes.BANNER_RED, k -> new SpongeMapDecorationBannerType(MapDecoration.Type.BANNER_RED, DyeColors.RED));
            l.add(MapDecorationTypes.BANNER_BLACK, k -> new SpongeMapDecorationBannerType(MapDecoration.Type.BANNER_BLACK, DyeColors.BLACK));
            l.add(MapDecorationTypes.BANNER_BLUE, k -> new SpongeMapDecorationBannerType(MapDecoration.Type.BANNER_BLUE, DyeColors.BLUE));
        });
    }

    public static RegistryLoader<MapShade> mapShade() {
        return RegistryLoader.of(l -> {
            l.add(0, MapShades.BASE, k -> new SpongeMapShade(0, 180));
            l.add(1, MapShades.DARK, k -> new SpongeMapShade(1, 220));
            l.add(2, MapShades.DARKER, k -> new SpongeMapShade(2, 255));
            l.add(3, MapShades.DARKEST, k -> new SpongeMapShade(3, 135));
        });
    }

    // @formatter:on
}
