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
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.kyori.adventure.text.Component;
import net.minecraft.command.arguments.BlockStateArgument;
import net.minecraft.command.arguments.ComponentArgument;
import net.minecraft.command.arguments.DimensionArgument;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.command.arguments.EntitySelectorParser;
import net.minecraft.command.arguments.GameProfileArgument;
import net.minecraft.command.arguments.ItemArgument;
import net.minecraft.command.arguments.NBTCompoundTagArgument;
import net.minecraft.command.arguments.ResourceLocationArgument;
import net.minecraft.command.arguments.ScoreHolderArgument;
import net.minecraft.command.arguments.Vec2Argument;
import net.minecraft.command.arguments.Vec3Argument;
import net.minecraft.item.Items;
import net.minecraft.item.MusicDiscItem;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.transaction.Operation;
import org.spongepowered.api.block.transaction.Operations;
import org.spongepowered.api.command.parameter.managed.ValueParameter;
import org.spongepowered.api.command.parameter.managed.clientcompletion.ClientCompletionType;
import org.spongepowered.api.command.parameter.managed.clientcompletion.ClientCompletionTypes;
import org.spongepowered.api.command.parameter.managed.standard.ResourceKeyedValueParameters;
import org.spongepowered.api.command.registrar.CommandRegistrar;
import org.spongepowered.api.command.registrar.tree.ClientCompletionKey;
import org.spongepowered.api.command.registrar.tree.ClientCompletionKeys;
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
import org.spongepowered.common.accessor.command.arguments.ArgumentSerializerAccessor;
import org.spongepowered.common.accessor.command.arguments.ArgumentTypesAccessor;
import org.spongepowered.common.accessor.command.arguments.DimensionArgumentAccessor;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.common.ban.SpongeBanType;
import org.spongepowered.common.block.BlockStateSerializerDeserializer;
import org.spongepowered.common.block.transaction.BlockOperation;
import org.spongepowered.common.command.brigadier.argument.StandardCatalogedArgumentParser;
import org.spongepowered.common.command.parameter.managed.clientcompletion.SpongeClientCompletionType;
import org.spongepowered.common.command.parameter.managed.standard.SpongeBigDecimalValueParameter;
import org.spongepowered.common.command.parameter.managed.standard.SpongeBigIntegerValueParameter;
import org.spongepowered.common.command.parameter.managed.standard.SpongeColorValueParameter;
import org.spongepowered.common.command.parameter.managed.standard.SpongeDataContainerValueParameter;
import org.spongepowered.common.command.parameter.managed.standard.SpongeDateTimeValueParameter;
import org.spongepowered.common.command.parameter.managed.standard.SpongeDurationValueParameter;
import org.spongepowered.common.command.parameter.managed.standard.SpongeGameProfileValueParameter;
import org.spongepowered.common.command.parameter.managed.standard.SpongeIPAddressValueParameter;
import org.spongepowered.common.command.parameter.managed.standard.SpongeNoneValueParameter;
import org.spongepowered.common.command.parameter.managed.standard.SpongePluginContainerValueParameter;
import org.spongepowered.common.command.parameter.managed.standard.SpongeServerLocationValueParameter;
import org.spongepowered.common.command.parameter.managed.standard.SpongeTargetBlockValueParameter;
import org.spongepowered.common.command.parameter.managed.standard.SpongeTargetEntityValueParameter;
import org.spongepowered.common.command.parameter.managed.standard.SpongeUserValueParameter;
import org.spongepowered.common.command.registrar.BrigadierCommandRegistrar;
import org.spongepowered.common.command.registrar.SpongeCommandRegistrars;
import org.spongepowered.common.command.registrar.SpongeParameterizedCommandRegistrar;
import org.spongepowered.common.command.registrar.SpongeRawCommandRegistrar;
import org.spongepowered.common.command.registrar.tree.key.SpongeAmountClientCompletionKey;
import org.spongepowered.common.command.registrar.tree.key.SpongeBasicClientCompletionKey;
import org.spongepowered.common.command.registrar.tree.key.SpongeEntityClientCompletionKey;
import org.spongepowered.common.command.registrar.tree.key.SpongeStringClientCompletionKey;
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
import org.spongepowered.common.placeholder.SpongePlaceholderParserBuilder;
import org.spongepowered.common.scoreboard.SpongeDisplaySlot;
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
import java.util.UUID;
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
            l.add(TransactionTypes.ENTITY_DEATH_DROPS, k -> new NoOpTransactionType<>(false, k.getValue().toUpperCase(Locale.ROOT)));
            l.add(TransactionTypes.NEIGHBOR_NOTIFICATION, k -> new NoOpTransactionType<>(false, k.getValue().toUpperCase(Locale.ROOT)));
            l.add(TransactionTypes.SPAWN_ENTITY, k -> new NoOpTransactionType<>(false, k.getValue().toUpperCase(Locale.ROOT)));
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
            l.add(ResourceKeyedValueParameters.BLOCK_STATE, () -> StandardCatalogedArgumentParser.createConverter(BlockStateArgument.block(),
                    (reader, cause, state) -> (BlockState) state.getState()));
            l.add(ResourceKeyedValueParameters.BOOLEAN, () -> StandardCatalogedArgumentParser.createIdentity(BoolArgumentType.bool()));
            l.add(ResourceKeyedValueParameters.COLOR, SpongeColorValueParameter::new); //Includes ColorArgumentParser.color(), but does more. TODO: what does 1.16 do?
            l.add(ResourceKeyedValueParameters.DATA_CONTAINER, SpongeDataContainerValueParameter::new);
            l.add(ResourceKeyedValueParameters.DATE_TIME, SpongeDateTimeValueParameter::new);
            l.add(ResourceKeyedValueParameters.DOUBLE, k -> StandardCatalogedArgumentParser.createIdentity(DoubleArgumentType.doubleArg()));
            l.add(ResourceKeyedValueParameters.DURATION, SpongeDurationValueParameter::new);
            l.add(ResourceKeyedValueParameters.ENTITY, k -> StandardCatalogedArgumentParser.createConverter(k, EntityArgument.entity(), (reader, cause, selector) -> (Entity) selector.findSingleEntity(cause.getSource())));
            l.add(ResourceKeyedValueParameters.GAME_PROFILE, SpongeGameProfileValueParameter::new);
            l.add(ResourceKeyedValueParameters.INTEGER, k -> StandardCatalogedArgumentParser.createIdentity(IntegerArgumentType.integer()));
            l.add(ResourceKeyedValueParameters.IP, SpongeIPAddressValueParameter::new);
            l.add(ResourceKeyedValueParameters.ITEM_STACK_SNAPSHOT, k -> StandardCatalogedArgumentParser.createConverter(k, ItemArgument.item(), (reader, cause, converter) -> new SpongeItemStackSnapshot((ItemStack) (Object) converter.createItemStack(1, true))));
            l.add(ResourceKeyedValueParameters.LOCATION, SpongeServerLocationValueParameter::new);
            l.add(ResourceKeyedValueParameters.LONG, k -> StandardCatalogedArgumentParser.createIdentity(LongArgumentType.longArg()));
            l.add(ResourceKeyedValueParameters.MANY_ENTITIES, k -> StandardCatalogedArgumentParser.createConverter(k, EntityArgument.entities(), (reader, cause, selector) -> selector.findEntities(cause.getSource()).stream().map(x -> (Entity) x).collect(Collectors.toList())));
            l.add(ResourceKeyedValueParameters.MANY_GAME_PROFILES, k -> StandardCatalogedArgumentParser.createConverter(k, GameProfileArgument.gameProfile(), (reader, cause, converter) -> converter.getNames(cause.getSource())));
            l.add(ResourceKeyedValueParameters.MANY_PLAYERS, k -> StandardCatalogedArgumentParser.createConverter(k, EntityArgument.players(), (reader, cause, selector) -> selector.findPlayers(cause.getSource())));
            l.add(ResourceKeyedValueParameters.NONE, SpongeNoneValueParameter::new);
            l.add(ResourceKeyedValueParameters.PLAYER, k -> StandardCatalogedArgumentParser.createConverter(k, EntityArgument.player(), (reader, cause, selector) -> (Player) selector.findSinglePlayer(cause.getSource())));
            l.add(ResourceKeyedValueParameters.PLUGIN, SpongePluginContainerValueParameter::new);
            l.add(ResourceKeyedValueParameters.REMAINING_JOINED_STRINGS, k -> StandardCatalogedArgumentParser.createIdentity(StringArgumentType.greedyString()));
            l.add(ResourceKeyedValueParameters.RESOURCE_KEY, k -> StandardCatalogedArgumentParser.createConverter(k, ResourceLocationArgument.id(), (reader, cause, resourceLocation) -> (ResourceKey) (Object) resourceLocation));
            l.add(ResourceKeyedValueParameters.STRING, k -> StandardCatalogedArgumentParser.createIdentity(StringArgumentType.string()));
            l.add(ResourceKeyedValueParameters.TARGET_BLOCK, SpongeTargetBlockValueParameter::new);
            l.add(ResourceKeyedValueParameters.TARGET_ENTITY, k -> new SpongeTargetEntityValueParameter(k, false));
            l.add(ResourceKeyedValueParameters.TARGET_PLAYER, k -> new SpongeTargetEntityValueParameter(k, true));
            l.add(ResourceKeyedValueParameters.TEXT_FORMATTING_CODE, k -> StandardCatalogedArgumentParser.createConverter(k, StringArgumentType.string(), (reader, cause, result) -> SpongeAdventure.legacyAmpersand(result)));
            l.add(ResourceKeyedValueParameters.TEXT_FORMATTING_CODE_ALL, k -> StandardCatalogedArgumentParser.createConverter(k, StringArgumentType.greedyString(), (reader, cause, result) -> SpongeAdventure.legacyAmpersand(result)));
            l.add(ResourceKeyedValueParameters.TEXT_JSON, k -> StandardCatalogedArgumentParser.createConverter(k, ComponentArgument.textComponent(), (reader, cause, result) -> SpongeAdventure.asAdventure(result)));
            l.add(ResourceKeyedValueParameters.TEXT_JSON_ALL, k -> StandardCatalogedArgumentParser.createConverter(k, StringArgumentType.greedyString(), (reader, cause, result) -> SpongeAdventure.json(result)));
            l.add(ResourceKeyedValueParameters.URL, k -> StandardCatalogedArgumentParser.createConverter(k, StringArgumentType.string(),
                    (reader, cause, input) -> {
                        try {
                            return new URL(input);
                        } catch (final MalformedURLException ex) {
                            throw new SimpleCommandExceptionType(new StringTextComponent("Could not parse " + input + " as a URL"))
                                    .createWithContext(reader);
                        }
                    })
            );
            l.add(ResourceKeyedValueParameters.USER, SpongeUserValueParameter::new);
            l.add(ResourceKeyedValueParameters.UUID, k -> StandardCatalogedArgumentParser.createConverter(k, StringArgumentType.string(),
                    (reader, cause, input) -> {
                        try {
                            return UUID.fromString(input);
                        } catch (final IllegalArgumentException ex) {
                            throw new SimpleCommandExceptionType(new StringTextComponent(ex.getMessage()))
                                    .createWithContext(reader);
                        }
                    })
            );
            l.add(ResourceKeyedValueParameters.VECTOR2D, k -> StandardCatalogedArgumentParser.createConverter(k, Vec2Argument.vec2(),
                    (reader, cause, result) -> {
                        final net.minecraft.util.math.vector.Vector3d r = result.getPosition(cause.getSource());
                        return new Vector2d(r.x, r.z);
                    })
            );
            l.add(ResourceKeyedValueParameters.VECTOR3D, k -> StandardCatalogedArgumentParser.createConverter(k, Vec3Argument.vec3(), (reader, cause, result) -> VecHelper.toVector3d(result.getPosition(cause.getSource()))));
            l.add(ResourceKeyedValueParameters.WORLD, k -> StandardCatalogedArgumentParser.createConverter(k,
                    DimensionArgument.dimension(),
                    (reader, cause, result) -> Sponge.getServer().getWorldManager().world((ResourceKey) (Object) result)
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

    public static RegistryLoader<ClientCompletionKey<?>> clientCompletionKey() {
        final Function<ResourceKey, ArgumentType<?>> fn = key -> ((ArgumentSerializerAccessor<?>) ArgumentTypesAccessor.accessor$BY_NAME().get(key).accessor$serializer()).accessor$constructor().get();
        return RegistryLoader.of(l -> {
            l.add(ClientCompletionKeys.BLOCK_PREDICATE, k -> new SpongeBasicClientCompletionKey(k, fn.apply(k)));
            l.add(ClientCompletionKeys.BLOCK_STATE, k -> new SpongeBasicClientCompletionKey(k, fn.apply(k)));
            l.add(ClientCompletionKeys.BOOL, k -> new SpongeBasicClientCompletionKey(k, fn.apply(k)));
            l.add(ClientCompletionKeys.COLOR, k -> new SpongeBasicClientCompletionKey(k, fn.apply(k)));
            l.add(ClientCompletionKeys.COMPONENT, k -> new SpongeBasicClientCompletionKey(k, fn.apply(k)));
            l.add(ClientCompletionKeys.DIMENSION, k -> new SpongeBasicClientCompletionKey(k, fn.apply(k)));
            l.add(ClientCompletionKeys.DOUBLE, k -> new SpongeBasicClientCompletionKey(k, DoubleArgumentType.doubleArg()));
            l.add(ClientCompletionKeys.ENTITY, SpongeEntityClientCompletionKey::new);
            l.add(ClientCompletionKeys.ENTITY_ANCHOR, k -> new SpongeBasicClientCompletionKey(k, fn.apply(k)));
            l.add(ClientCompletionKeys.ENTITY_SUMMON, k -> new SpongeBasicClientCompletionKey(k, fn.apply(k)));
            l.add(ClientCompletionKeys.FLOAT, k -> new SpongeBasicClientCompletionKey(k, FloatArgumentType.floatArg()));
            l.add(ClientCompletionKeys.FUNCTION, k -> new SpongeBasicClientCompletionKey(k, fn.apply(k)));
            l.add(ClientCompletionKeys.GAME_PROFILE, k -> new SpongeBasicClientCompletionKey(k, fn.apply(k)));
            l.add(ClientCompletionKeys.INTEGER, k -> new SpongeBasicClientCompletionKey(k, IntegerArgumentType.integer()));
            l.add(ClientCompletionKeys.ITEM_ENCHANTMENT, k -> new SpongeBasicClientCompletionKey(k, fn.apply(k)));
            l.add(ClientCompletionKeys.ITEM_SLOT, k -> new SpongeBasicClientCompletionKey(k, fn.apply(k)));
            l.add(ClientCompletionKeys.LONG, k -> new SpongeBasicClientCompletionKey(k, LongArgumentType.longArg()));
            l.add(ClientCompletionKeys.MESSAGE, k -> new SpongeBasicClientCompletionKey(k, fn.apply(k)));
            l.add(ClientCompletionKeys.MOB_EFFECT, k -> new SpongeBasicClientCompletionKey(k, fn.apply(k)));
            l.add(ClientCompletionKeys.NBT_COMPOUND_TAG, k -> new SpongeBasicClientCompletionKey(k, fn.apply(k)));
            l.add(ClientCompletionKeys.NBT_PATH, k -> new SpongeBasicClientCompletionKey(k, fn.apply(k)));
            l.add(ClientCompletionKeys.NBT_TAG, k -> new SpongeBasicClientCompletionKey(k, fn.apply(k)));
            l.add(ClientCompletionKeys.OBJECTIVE, k -> new SpongeBasicClientCompletionKey(k, fn.apply(k)));
            l.add(ClientCompletionKeys.OBJECTIVE_CRITERIA, k -> new SpongeBasicClientCompletionKey(k, fn.apply(k)));
            l.add(ClientCompletionKeys.OPERATION, k -> new SpongeBasicClientCompletionKey(k, fn.apply(k)));
            l.add(ClientCompletionKeys.PARTICLE, k -> new SpongeBasicClientCompletionKey(k, fn.apply(k)));
            l.add(ClientCompletionKeys.RESOURCE_LOCATION, k -> new SpongeBasicClientCompletionKey(k, fn.apply(k)));
            l.add(ClientCompletionKeys.ROTATION, k -> new SpongeBasicClientCompletionKey(k, fn.apply(k)));
            l.add(ClientCompletionKeys.SCORE_HOLDER, k -> new SpongeAmountClientCompletionKey(k, ScoreHolderArgument.scoreHolder(), ScoreHolderArgument.scoreHolders()));
            l.add(ClientCompletionKeys.SCOREBOARD_SLOT, k -> new SpongeBasicClientCompletionKey(k, fn.apply(k)));
            l.add(ClientCompletionKeys.STRING, SpongeStringClientCompletionKey::new);
            l.add(ClientCompletionKeys.SWIZZLE, k -> new SpongeBasicClientCompletionKey(k, fn.apply(k)));
            l.add(ClientCompletionKeys.TEAM, k -> new SpongeBasicClientCompletionKey(k, fn.apply(k)));
            l.add(ClientCompletionKeys.TIME, k -> new SpongeBasicClientCompletionKey(k, fn.apply(k)));
            l.add(ClientCompletionKeys.VEC2, k -> new SpongeBasicClientCompletionKey(k, fn.apply(k)));
            l.add(ClientCompletionKeys.VEC3, k -> new SpongeBasicClientCompletionKey(k, fn.apply(k)));
        });
    }

    public static RegistryLoader<ClientCompletionType> clientCompletionType() {
        return RegistryLoader.of(l -> {
            l.add(ClientCompletionTypes.DECIMAL_NUMBER, k -> new SpongeClientCompletionType(DoubleArgumentType.doubleArg()));
            l.add(ClientCompletionTypes.SNBT, k -> new SpongeClientCompletionType(NBTCompoundTagArgument.compoundTag()));
            l.add(ClientCompletionTypes.NONE, k -> SpongeClientCompletionType.NONE);
            l.add(ClientCompletionTypes.RESOURCE_KEY, k -> new SpongeClientCompletionType(ResourceLocationArgument.id()));
            l.add(ClientCompletionTypes.STRING, k -> new SpongeClientCompletionType(StringArgumentType.string()));
            l.add(ClientCompletionTypes.WHOLE_NUMBER, k -> new SpongeClientCompletionType(LongArgumentType.longArg()));
        });
    }

    public static RegistryLoader<CommandRegistrar<?>> commandRegistrar() {
        return RegistryLoader.of(l -> {
            l.add(SpongeCommandRegistrars.BRIGADIER, () -> BrigadierCommandRegistrar.INSTANCE);
            l.add(SpongeCommandRegistrars.MANAGED, () -> SpongeParameterizedCommandRegistrar.INSTANCE);
            l.add(SpongeCommandRegistrars.RAW, () -> SpongeRawCommandRegistrar.INSTANCE);
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
            l.add(2, DisplaySlots.BELOW_NAME, SpongeDisplaySlot::new);
            l.add(0, DisplaySlots.LIST, SpongeDisplaySlot::new);
            l.add(1, DisplaySlots.SIDEBAR_TEAM_NO_COLOR, SpongeDisplaySlot::new);
            l.add(TextFormatting.AQUA.getId() + 3, DisplaySlots.SIDEBAR_TEAM_AQUA, SpongeDisplaySlot::new);
            l.add(TextFormatting.BLACK.getId() + 3, DisplaySlots.SIDEBAR_TEAM_BLACK, SpongeDisplaySlot::new);
            l.add(TextFormatting.BLUE.getId() + 3, DisplaySlots.SIDEBAR_TEAM_BLUE, SpongeDisplaySlot::new);
            l.add(TextFormatting.DARK_AQUA.getId() + 3, DisplaySlots.SIDEBAR_TEAM_DARK_AQUA, SpongeDisplaySlot::new);
            l.add(TextFormatting.DARK_BLUE.getId() + 3, DisplaySlots.SIDEBAR_TEAM_DARK_BLUE, SpongeDisplaySlot::new);
            l.add(TextFormatting.DARK_GRAY.getId() + 3, DisplaySlots.SIDEBAR_TEAM_DARK_GRAY, SpongeDisplaySlot::new);
            l.add(TextFormatting.DARK_GREEN.getId() + 3, DisplaySlots.SIDEBAR_TEAM_DARK_GREEN, SpongeDisplaySlot::new);
            l.add(TextFormatting.DARK_PURPLE.getId() + 3, DisplaySlots.SIDEBAR_TEAM_DARK_PURPLE, SpongeDisplaySlot::new);
            l.add(TextFormatting.DARK_RED.getId() + 3, DisplaySlots.SIDEBAR_TEAM_DARK_RED, SpongeDisplaySlot::new);
            l.add(TextFormatting.GOLD.getId() + 3, DisplaySlots.SIDEBAR_TEAM_GOLD, SpongeDisplaySlot::new);
            l.add(TextFormatting.GRAY.getId() + 3, DisplaySlots.SIDEBAR_TEAM_GRAY, SpongeDisplaySlot::new);
            l.add(TextFormatting.GREEN.getId() + 3, DisplaySlots.SIDEBAR_TEAM_GREEN, SpongeDisplaySlot::new);
            l.add(TextFormatting.LIGHT_PURPLE.getId() + 3, DisplaySlots.SIDEBAR_TEAM_LIGHT_PURPLE, SpongeDisplaySlot::new);
            l.add(TextFormatting.RED.getId() + 3, DisplaySlots.SIDEBAR_TEAM_RED, SpongeDisplaySlot::new);
            l.add(TextFormatting.WHITE.getId() + 3, DisplaySlots.SIDEBAR_TEAM_WHITE, SpongeDisplaySlot::new);
            l.add(TextFormatting.YELLOW.getId() + 3, DisplaySlots.SIDEBAR_TEAM_YELLOW, SpongeDisplaySlot::new);
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
            l.add(MusicDiscs.BLOCKS, k -> new SpongeMusicDisc((MusicDiscItem) Items.MUSIC_DISC_BLOCKS));
            l.add(MusicDiscs.CAT, k -> new SpongeMusicDisc((MusicDiscItem) Items.MUSIC_DISC_CAT));
            l.add(MusicDiscs.CHIRP, k -> new SpongeMusicDisc((MusicDiscItem) Items.MUSIC_DISC_CHIRP));
            l.add(MusicDiscs.FAR, k -> new SpongeMusicDisc((MusicDiscItem) Items.MUSIC_DISC_FAR));
            l.add(MusicDiscs.MALL, k -> new SpongeMusicDisc((MusicDiscItem) Items.MUSIC_DISC_MALL));
            l.add(MusicDiscs.MELLOHI, k -> new SpongeMusicDisc((MusicDiscItem) Items.MUSIC_DISC_MELLOHI));
            l.add(MusicDiscs.MUSIC_DISC_11, k -> new SpongeMusicDisc((MusicDiscItem) Items.MUSIC_DISC_11));
            l.add(MusicDiscs.MUSIC_DISC_13, k -> new SpongeMusicDisc((MusicDiscItem) Items.MUSIC_DISC_13));
            l.add(MusicDiscs.PIGSTEP, k -> new SpongeMusicDisc((MusicDiscItem) Items.MUSIC_DISC_PIGSTEP));
            l.add(MusicDiscs.STAL, k -> new SpongeMusicDisc((MusicDiscItem) Items.MUSIC_DISC_STAL));
            l.add(MusicDiscs.STRAD, k -> new SpongeMusicDisc((MusicDiscItem) Items.MUSIC_DISC_STRAD));
            l.add(MusicDiscs.WAIT, k -> new SpongeMusicDisc((MusicDiscItem) Items.MUSIC_DISC_WAIT));
            l.add(MusicDiscs.WARD, k -> new SpongeMusicDisc((MusicDiscItem) Items.MUSIC_DISC_WARD));
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
            l.add(ParticleOptions.VELOCITY, k -> new SpongeParticleOption<>(Vector3d.class));
        });
    }

    public static RegistryLoader<PlaceholderParser> placeholderParser() {
        return RegistryLoader.of(l -> {
            l.add(PlaceholderParsers.CURRENT_WORLD, k -> new SpongePlaceholderParserBuilder()
                    .parser(placeholderText -> Component.text(placeholderText.getAssociatedObject().filter(x -> x instanceof Locatable)
                            .map(x -> ((Locatable) x).getServerLocation().getWorldKey())
                            .orElseGet(() -> Sponge.getServer().getWorldManager().defaultWorld().getKey()).toString()))
                    .build());
            l.add(PlaceholderParsers.NAME, k -> new SpongePlaceholderParserBuilder()
                    .parser(placeholderText -> placeholderText.getAssociatedObject()
                            .filter(x -> x instanceof Nameable)
                            .map(x -> Component.text(((Nameable) x).getName()))
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

    // @formatter:on
}
