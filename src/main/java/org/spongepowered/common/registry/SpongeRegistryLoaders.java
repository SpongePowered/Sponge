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

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import io.leangen.geantyref.TypeToken;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.minecraft.command.arguments.BlockStateArgument;
import net.minecraft.command.arguments.ComponentArgument;
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
import net.minecraft.scoreboard.ScoreCriteria;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameType;
import net.minecraft.world.WorldSettings;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.transaction.Operation;
import org.spongepowered.api.block.transaction.Operations;
import org.spongepowered.api.command.parameter.managed.clientcompletion.ClientCompletionType;
import org.spongepowered.api.command.parameter.managed.clientcompletion.ClientCompletionTypes;
import org.spongepowered.api.command.parameter.managed.standard.CatalogedValueParameter;
import org.spongepowered.api.command.parameter.managed.standard.CatalogedValueParameters;
import org.spongepowered.api.command.registrar.CommandRegistrar;
import org.spongepowered.api.command.registrar.tree.ClientCompletionKey;
import org.spongepowered.api.command.registrar.tree.ClientCompletionKeys;
import org.spongepowered.api.command.selector.SelectorSortAlgorithm;
import org.spongepowered.api.command.selector.SelectorSortAlgorithms;
import org.spongepowered.api.command.selector.SelectorType;
import org.spongepowered.api.command.selector.SelectorTypes;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.KeyValueMatcher;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.BodyPart;
import org.spongepowered.api.data.type.BodyParts;
import org.spongepowered.api.data.type.CatType;
import org.spongepowered.api.data.type.CatTypes;
import org.spongepowered.api.data.type.HandType;
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
import org.spongepowered.api.data.value.Value;
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
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.EventContextKey;
import org.spongepowered.api.event.EventContextKeys;
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
import org.spongepowered.api.event.cause.entity.damage.source.DamageSource;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.menu.ClickType;
import org.spongepowered.api.item.inventory.menu.ClickTypes;
import org.spongepowered.api.item.inventory.query.QueryType;
import org.spongepowered.api.item.inventory.query.QueryTypes;
import org.spongepowered.api.placeholder.PlaceholderParser;
import org.spongepowered.api.placeholder.PlaceholderParsers;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.projectile.source.ProjectileSource;
import org.spongepowered.api.registry.RegistryReference;
import org.spongepowered.api.scoreboard.criteria.Criteria;
import org.spongepowered.api.scoreboard.criteria.Criterion;
import org.spongepowered.api.scoreboard.displayslot.DisplaySlot;
import org.spongepowered.api.scoreboard.displayslot.DisplaySlots;
import org.spongepowered.api.service.ban.Ban;
import org.spongepowered.api.service.ban.BanType;
import org.spongepowered.api.service.ban.BanTypes;
import org.spongepowered.api.service.economy.account.AccountDeletionResultType;
import org.spongepowered.api.service.economy.account.AccountDeletionResultTypes;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.util.Color;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.util.Nameable;
import org.spongepowered.api.util.TypeTokens;
import org.spongepowered.api.world.Locatable;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.api.world.ServerLocation;
import org.spongepowered.api.world.WorldArchetype;
import org.spongepowered.api.world.WorldArchetypes;
import org.spongepowered.api.world.dimension.DimensionType;
import org.spongepowered.api.world.dimension.DimensionTypes;
import org.spongepowered.api.world.portal.PortalType;
import org.spongepowered.api.world.portal.PortalTypes;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.api.world.teleport.TeleportHelperFilter;
import org.spongepowered.api.world.teleport.TeleportHelperFilters;
import org.spongepowered.api.world.weather.Weather;
import org.spongepowered.api.world.weather.Weathers;
import org.spongepowered.common.accessor.command.arguments.ArgumentSerializerAccessor;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.common.ban.SpongeBanType;
import org.spongepowered.common.block.transaction.BlockOperation;
import org.spongepowered.common.bridge.ResourceKeyBridge;
import org.spongepowered.common.bridge.world.WorldSettingsBridge;
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
import org.spongepowered.common.command.parameter.managed.standard.SpongeWorldPropertiesValueParameter;
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
import org.spongepowered.common.data.key.SpongeKey;
import org.spongepowered.common.data.key.SpongeKeyBuilder;
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
import org.spongepowered.common.event.SpongeEventContextKey;
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
import org.spongepowered.common.world.SpongeWorldArchetypeBuilder;
import org.spongepowered.common.world.portal.EndPortalType;
import org.spongepowered.common.world.portal.NetherPortalType;
import org.spongepowered.common.world.teleport.ConfigTeleportHelperFilter;
import org.spongepowered.common.world.teleport.DefaultTeleportHelperFilter;
import org.spongepowered.common.world.teleport.FlyingTeleportHelperFilter;
import org.spongepowered.common.world.teleport.NoPortalTeleportHelperFilter;
import org.spongepowered.common.world.teleport.SurfaceOnlyTeleportHelperFilter;
import org.spongepowered.common.world.weather.SpongeWeather;
import org.spongepowered.math.vector.Vector2d;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.plugin.PluginContainer;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;
import java.util.UUID;
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
            l.add(BanTypes.IP, k -> new SpongeBanType(k, Ban.IP.class));
            l.add(BanTypes.PROFILE, k -> new SpongeBanType(k, Ban.Profile.class));
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

    public static RegistryLoader<CatalogedValueParameter<?>> catalogedValueParameter() {
        return RegistryLoader.of(l -> {
            l.add(CatalogedValueParameters.BIG_DECIMAL, SpongeBigDecimalValueParameter::new);
            l.add(CatalogedValueParameters.BIG_INTEGER, SpongeBigIntegerValueParameter::new);
            l.add(CatalogedValueParameters.BLOCK_STATE, k -> StandardCatalogedArgumentParser.createConverter(k, BlockStateArgument.block(), (reader, cause, state) -> (BlockState) state.getState()));
            l.add(CatalogedValueParameters.BOOLEAN, k -> StandardCatalogedArgumentParser.createIdentity(k, BoolArgumentType.bool()));
            l.add(CatalogedValueParameters.COLOR, SpongeColorValueParameter::new); //Includes ColorArgumentParser.color(), but does more. TODO: what does 1.16 do?
            l.add(CatalogedValueParameters.DATA_CONTAINER, SpongeDataContainerValueParameter::new);
            l.add(CatalogedValueParameters.DATE_TIME, SpongeDateTimeValueParameter::new);
            l.add(CatalogedValueParameters.DOUBLE, k -> StandardCatalogedArgumentParser.createIdentity(k, DoubleArgumentType.doubleArg()));
            l.add(CatalogedValueParameters.DURATION, SpongeDurationValueParameter::new);
            l.add(CatalogedValueParameters.ENTITY, k -> StandardCatalogedArgumentParser.createConverter(k, EntityArgument.entity(), (reader, cause, selector) -> (Entity) selector.findSingleEntity(cause.getSource())));
            l.add(CatalogedValueParameters.GAME_PROFILE, SpongeGameProfileValueParameter::new);
            l.add(CatalogedValueParameters.INTEGER, k -> StandardCatalogedArgumentParser.createIdentity(k, IntegerArgumentType.integer()));
            l.add(CatalogedValueParameters.IP, SpongeIPAddressValueParameter::new);
            l.add(CatalogedValueParameters.ITEM_STACK_SNAPSHOT, k -> StandardCatalogedArgumentParser.createConverter(k, ItemArgument.item(), (reader, cause, converter) -> new SpongeItemStackSnapshot((ItemStack) (Object) converter.createItemStack(1, true))));
            l.add(CatalogedValueParameters.LOCATION_ALL, k -> new SpongeServerLocationValueParameter(k, true));
            l.add(CatalogedValueParameters.LOCATION_ONLINE_ONLY, k -> new SpongeServerLocationValueParameter(k, false));
            l.add(CatalogedValueParameters.LONG, k -> StandardCatalogedArgumentParser.createIdentity(k, LongArgumentType.longArg()));
            l.add(CatalogedValueParameters.MANY_ENTITIES, k -> StandardCatalogedArgumentParser.createConverter(k, EntityArgument.entities(), (reader, cause, selector) -> selector.findEntities(cause.getSource()).stream().map(x -> (Entity) x).collect(Collectors.toList())));
            l.add(CatalogedValueParameters.MANY_GAME_PROFILES, k -> StandardCatalogedArgumentParser.createConverter(k, GameProfileArgument.gameProfile(), (reader, cause, converter) -> converter.getNames(cause.getSource())));
            l.add(CatalogedValueParameters.MANY_PLAYERS, k -> StandardCatalogedArgumentParser.createConverter(k, EntityArgument.players(), (reader, cause, selector) -> selector.findPlayers(cause.getSource())));
            l.add(CatalogedValueParameters.NONE, SpongeNoneValueParameter::new);
            l.add(CatalogedValueParameters.PLAYER, k -> StandardCatalogedArgumentParser.createConverter(k, EntityArgument.player(), (reader, cause, selector) -> (Player) selector.findSinglePlayer(cause.getSource())));
            l.add(CatalogedValueParameters.PLUGIN, SpongePluginContainerValueParameter::new);
            l.add(CatalogedValueParameters.REMAINING_JOINED_STRINGS, k -> StandardCatalogedArgumentParser.createIdentity(k, StringArgumentType.greedyString()));
            l.add(CatalogedValueParameters.RESOURCE_KEY, k -> StandardCatalogedArgumentParser.createConverter(k, ResourceLocationArgument.id(), (reader, cause, resourceLocation) -> (ResourceKey) (Object) resourceLocation));
            l.add(CatalogedValueParameters.STRING, k -> StandardCatalogedArgumentParser.createIdentity(k, StringArgumentType.string()));
            l.add(CatalogedValueParameters.TARGET_BLOCK, SpongeTargetBlockValueParameter::new);
            l.add(CatalogedValueParameters.TARGET_ENTITY, k -> new SpongeTargetEntityValueParameter(k, false));
            l.add(CatalogedValueParameters.TARGET_PLAYER, k -> new SpongeTargetEntityValueParameter(k, true));
            l.add(CatalogedValueParameters.TEXT_FORMATTING_CODE, k -> StandardCatalogedArgumentParser.createConverter(k, StringArgumentType.string(), (reader, cause, result) -> SpongeAdventure.legacyAmpersand(result)));
            l.add(CatalogedValueParameters.TEXT_FORMATTING_CODE_ALL, k -> StandardCatalogedArgumentParser.createConverter(k, StringArgumentType.greedyString(), (reader, cause, result) -> SpongeAdventure.legacyAmpersand(result)));
            l.add(CatalogedValueParameters.TEXT_JSON, k -> StandardCatalogedArgumentParser.createConverter(k, ComponentArgument.textComponent(), (reader, cause, result) -> SpongeAdventure.asAdventure(result)));
            l.add(CatalogedValueParameters.TEXT_JSON_ALL, k -> StandardCatalogedArgumentParser.createConverter(k, StringArgumentType.greedyString(), (reader, cause, result) -> SpongeAdventure.json(result)));
            l.add(CatalogedValueParameters.URL, k -> StandardCatalogedArgumentParser.createConverter(k, StringArgumentType.string(),
                    (reader, cause, input) -> {
                        try {
                            return new URL(input);
                        } catch (final MalformedURLException ex) {
                            throw new SimpleCommandExceptionType(new StringTextComponent("Could not parse " + input + " as a URL"))
                                    .createWithContext(reader);
                        }
                    })
            );
            l.add(CatalogedValueParameters.USER, SpongeUserValueParameter::new);
            l.add(CatalogedValueParameters.UUID, k -> StandardCatalogedArgumentParser.createConverter(k, StringArgumentType.string(),
                    (reader, cause, input) -> {
                        try {
                            return UUID.fromString(input);
                        } catch (final IllegalArgumentException ex) {
                            throw new SimpleCommandExceptionType(new StringTextComponent(ex.getMessage()))
                                    .createWithContext(reader);
                        }
                    })
            );
            l.add(CatalogedValueParameters.VECTOR2D, k -> StandardCatalogedArgumentParser.createConverter(k, Vec2Argument.vec2(),
                    (reader, cause, result) -> {
                        final net.minecraft.util.math.vector.Vector3d r = result.getPosition(cause.getSource());
                        return new Vector2d(r.x, r.z);
                    })
            );
            l.add(CatalogedValueParameters.VECTOR3D, k -> StandardCatalogedArgumentParser.createConverter(k, Vec3Argument.vec3(), (reader, cause, result) -> VecHelper.toVector3d(result.getPosition(cause.getSource()))));
            l.add(CatalogedValueParameters.WORLD_PROPERTIES_ALL, k -> new SpongeWorldPropertiesValueParameter(k, true));
            l.add(CatalogedValueParameters.WORLD_PROPERTIES_ONLINE_ONLY, k -> new SpongeWorldPropertiesValueParameter(k, false));
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
        return RegistryLoader.of(l -> {
            l.add(ClientCompletionKeys.BLOCK_PREDICATE, k -> new SpongeBasicClientCompletionKey(k, ((ArgumentSerializerAccessor<?>) k).accessor$constructor().get()));
            l.add(ClientCompletionKeys.BLOCK_STATE, k -> new SpongeBasicClientCompletionKey(k, ((ArgumentSerializerAccessor<?>) k).accessor$constructor().get()));
            l.add(ClientCompletionKeys.BOOL, k -> new SpongeBasicClientCompletionKey(k, ((ArgumentSerializerAccessor<?>) k).accessor$constructor().get()));
            l.add(ClientCompletionKeys.COLOR, k -> new SpongeBasicClientCompletionKey(k, ((ArgumentSerializerAccessor<?>) k).accessor$constructor().get()));
            l.add(ClientCompletionKeys.COMPONENT, k -> new SpongeBasicClientCompletionKey(k, ((ArgumentSerializerAccessor<?>) k).accessor$constructor().get()));
            l.add(ClientCompletionKeys.DIMENSION, k -> new SpongeBasicClientCompletionKey(k, ((ArgumentSerializerAccessor<?>) k).accessor$constructor().get()));
            l.add(ClientCompletionKeys.DOUBLE, k -> new SpongeBasicClientCompletionKey(k, ((ArgumentSerializerAccessor<?>) k).accessor$constructor().get()));
            l.add(ClientCompletionKeys.ENTITY, SpongeEntityClientCompletionKey::new);
            l.add(ClientCompletionKeys.ENTITY_ANCHOR, k -> new SpongeBasicClientCompletionKey(k, ((ArgumentSerializerAccessor<?>) k).accessor$constructor().get()));
            l.add(ClientCompletionKeys.ENTITY_SUMMON, k -> new SpongeBasicClientCompletionKey(k, ((ArgumentSerializerAccessor<?>) k).accessor$constructor().get()));
            l.add(ClientCompletionKeys.FLOAT, k -> new SpongeBasicClientCompletionKey(k, ((ArgumentSerializerAccessor<?>) k).accessor$constructor().get()));
            l.add(ClientCompletionKeys.FUNCTION, k -> new SpongeBasicClientCompletionKey(k, ((ArgumentSerializerAccessor<?>) k).accessor$constructor().get()));
            l.add(ClientCompletionKeys.GAME_PROFILE, k -> new SpongeBasicClientCompletionKey(k, ((ArgumentSerializerAccessor<?>) k).accessor$constructor().get()));
            l.add(ClientCompletionKeys.INTEGER, k -> new SpongeBasicClientCompletionKey(k, ((ArgumentSerializerAccessor<?>) k).accessor$constructor().get()));
            l.add(ClientCompletionKeys.ITEM_ENCHANTMENT, k -> new SpongeBasicClientCompletionKey(k, ((ArgumentSerializerAccessor<?>) k).accessor$constructor().get()));
            l.add(ClientCompletionKeys.ITEM_SLOT, k -> new SpongeBasicClientCompletionKey(k, ((ArgumentSerializerAccessor<?>) k).accessor$constructor().get()));
            l.add(ClientCompletionKeys.LONG, k -> new SpongeBasicClientCompletionKey(k, ((ArgumentSerializerAccessor<?>) k).accessor$constructor().get()));
            l.add(ClientCompletionKeys.MESSAGE, k -> new SpongeBasicClientCompletionKey(k, ((ArgumentSerializerAccessor<?>) k).accessor$constructor().get()));
            l.add(ClientCompletionKeys.MOB_EFFECT, k -> new SpongeBasicClientCompletionKey(k, ((ArgumentSerializerAccessor<?>) k).accessor$constructor().get()));
            l.add(ClientCompletionKeys.NBT_COMPOUND_TAG, k -> new SpongeBasicClientCompletionKey(k, ((ArgumentSerializerAccessor<?>) k).accessor$constructor().get()));
            l.add(ClientCompletionKeys.NBT_PATH, k -> new SpongeBasicClientCompletionKey(k, ((ArgumentSerializerAccessor<?>) k).accessor$constructor().get()));
            l.add(ClientCompletionKeys.NBT_TAG, k -> new SpongeBasicClientCompletionKey(k, ((ArgumentSerializerAccessor<?>) k).accessor$constructor().get()));
            l.add(ClientCompletionKeys.OBJECTIVE, k -> new SpongeBasicClientCompletionKey(k, ((ArgumentSerializerAccessor<?>) k).accessor$constructor().get()));
            l.add(ClientCompletionKeys.OBJECTIVE_CRITERIA, k -> new SpongeBasicClientCompletionKey(k, ((ArgumentSerializerAccessor<?>) k).accessor$constructor().get()));
            l.add(ClientCompletionKeys.OPERATION, k -> new SpongeBasicClientCompletionKey(k, ((ArgumentSerializerAccessor<?>) k).accessor$constructor().get()));
            l.add(ClientCompletionKeys.PARTICLE, k -> new SpongeBasicClientCompletionKey(k, ((ArgumentSerializerAccessor<?>) k).accessor$constructor().get()));
            l.add(ClientCompletionKeys.RESOURCE_LOCATION, k -> new SpongeBasicClientCompletionKey(k, ((ArgumentSerializerAccessor<?>) k).accessor$constructor().get()));
            l.add(ClientCompletionKeys.ROTATION, k -> new SpongeBasicClientCompletionKey(k, ((ArgumentSerializerAccessor<?>) k).accessor$constructor().get()));
            l.add(ClientCompletionKeys.SCORE_HOLDER, k -> new SpongeAmountClientCompletionKey(k, ScoreHolderArgument.scoreHolder(), ScoreHolderArgument.scoreHolders()));
            l.add(ClientCompletionKeys.SCOREBOARD_SLOT, k -> new SpongeBasicClientCompletionKey(k, ((ArgumentSerializerAccessor<?>) k).accessor$constructor().get()));
            l.add(ClientCompletionKeys.STRING, SpongeStringClientCompletionKey::new);
            l.add(ClientCompletionKeys.SWIZZLE, k -> new SpongeBasicClientCompletionKey(k, ((ArgumentSerializerAccessor<?>) k).accessor$constructor().get()));
            l.add(ClientCompletionKeys.TEAM, k -> new SpongeBasicClientCompletionKey(k, ((ArgumentSerializerAccessor<?>) k).accessor$constructor().get()));
            l.add(ClientCompletionKeys.TIME, k -> new SpongeBasicClientCompletionKey(k, ((ArgumentSerializerAccessor<?>) k).accessor$constructor().get()));
            l.add(ClientCompletionKeys.VEC2, k -> new SpongeBasicClientCompletionKey(k, ((ArgumentSerializerAccessor<?>) k).accessor$constructor().get()));
            l.add(ClientCompletionKeys.VEC3, k -> new SpongeBasicClientCompletionKey(k, ((ArgumentSerializerAccessor<?>) k).accessor$constructor().get()));
        });
    }

    public static RegistryLoader<ClientCompletionType> clientCompletionType() {
        return RegistryLoader.of(l -> {
            l.add(ClientCompletionTypes.DECIMAL_NUMBER, k -> new SpongeClientCompletionType(k, DoubleArgumentType.doubleArg()));
            l.add(ClientCompletionTypes.JSON, k -> new SpongeClientCompletionType(k, NBTCompoundTagArgument.compoundTag()));
            l.add(ClientCompletionTypes.NONE, k -> new SpongeClientCompletionType(k, null));
            l.add(ClientCompletionTypes.RESOURCE_KEY, k -> new SpongeClientCompletionType(k, ResourceLocationArgument.id()));
            l.add(ClientCompletionTypes.STRING, k -> new SpongeClientCompletionType(k, StringArgumentType.string()));
            l.add(ClientCompletionTypes.WHOLE_NUMBER, k -> new SpongeClientCompletionType(k, LongArgumentType.longArg()));
        });
    }

    public static RegistryLoader<CommandRegistrar<?>> commandRegistrar() {
        return RegistryLoader.of(l -> {
            l.add(SpongeCommandRegistrars.BRIGADIER, BrigadierCommandRegistrar::new);
            l.add(SpongeCommandRegistrars.MANAGED, SpongeParameterizedCommandRegistrar::new);
            l.add(SpongeCommandRegistrars.RAW, SpongeRawCommandRegistrar::new);
        });
    }

    public static RegistryLoader<Criterion> criterion() {
        return RegistryLoader.of(l -> {
            l.add(Criteria.AIR, k -> SpongeRegistryLoaders.newCriterion(ScoreCriteria.AIR, k));
            l.add(Criteria.ARMOR, k -> SpongeRegistryLoaders.newCriterion(ScoreCriteria.ARMOR, k));
            l.add(Criteria.DEATH_COUNT, k -> SpongeRegistryLoaders.newCriterion(ScoreCriteria.DEATH_COUNT, k));
            l.add(Criteria.DUMMY, k -> SpongeRegistryLoaders.newCriterion(ScoreCriteria.DUMMY, k));
            l.add(Criteria.EXPERIENCE, k -> SpongeRegistryLoaders.newCriterion(ScoreCriteria.EXPERIENCE, k));
            l.add(Criteria.FOOD, k -> SpongeRegistryLoaders.newCriterion(ScoreCriteria.FOOD, k));
            l.add(Criteria.HEALTH, k -> SpongeRegistryLoaders.newCriterion(ScoreCriteria.HEALTH, k));
            l.add(Criteria.LEVEL, k -> SpongeRegistryLoaders.newCriterion(ScoreCriteria.LEVEL, k));
            l.add(Criteria.PLAYER_KILL_COUNT, k -> SpongeRegistryLoaders.newCriterion(ScoreCriteria.KILL_COUNT_PLAYERS, k));
            l.add(Criteria.TOTAL_KILL_COUNT, k -> SpongeRegistryLoaders.newCriterion(ScoreCriteria.KILL_COUNT_ALL, k));
            l.add(Criteria.TRIGGER, k -> SpongeRegistryLoaders.newCriterion(ScoreCriteria.TRIGGER, k));
        });
    }

    private static Criterion newCriterion(final ScoreCriteria criteria, final ResourceKey key) {
        ((ResourceKeyBridge) criteria).bridge$setKey(key);
        return (Criterion) criteria;
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

    public static RegistryLoader<DamageType> damageType() {
        return RegistryLoader.of(l -> l.mapping(SpongeDamageType::new, m -> m.add(
                DamageTypes.ATTACK,
                DamageTypes.CONTACT,
                DamageTypes.CUSTOM,
                DamageTypes.DROWN,
                DamageTypes.DRYOUT,
                DamageTypes.EXPLOSIVE,
                DamageTypes.FALL,
                DamageTypes.FIRE,
                DamageTypes.GENERIC,
                DamageTypes.HUNGER,
                DamageTypes.MAGIC,
                DamageTypes.MAGMA,
                DamageTypes.PROJECTILE,
                DamageTypes.SUFFOCATE,
                DamageTypes.SWEEPING_ATTACK,
                DamageTypes.VOID
        )));
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

    public static RegistryLoader<EventContextKey<?>> eventContextKey() {
        return RegistryLoader.of(l -> {
            l.add(EventContextKeys.AUDIENCE, k -> new SpongeEventContextKey<>(k, Audience.class));
            l.add(EventContextKeys.BLOCK_EVENT_PROCESS, k -> new SpongeEventContextKey<>(k, LocatableBlock.class));
            l.add(EventContextKeys.BLOCK_EVENT_QUEUE, k -> new SpongeEventContextKey<>(k, LocatableBlock.class));
            l.add(EventContextKeys.BLOCK_HIT, k -> new SpongeEventContextKey<>(k, BlockSnapshot.class));
            l.add(EventContextKeys.BLOCK_TARGET, k -> new SpongeEventContextKey<>(k, BlockSnapshot.class));
            l.add(EventContextKeys.COMMAND, k -> new SpongeEventContextKey<>(k, String.class));
            l.add(EventContextKeys.CREATOR, k -> new SpongeEventContextKey<>(k, User.class));
            l.add(EventContextKeys.DAMAGE_TYPE, k -> new SpongeEventContextKey<>(k, DamageType.class));
            l.add(EventContextKeys.DISMOUNT_TYPE, k -> new SpongeEventContextKey<>(k, DismountType.class));
            l.add(EventContextKeys.ENTITY_HIT, k -> new SpongeEventContextKey<>(k, Entity.class));
            l.add(EventContextKeys.FAKE_PLAYER, k -> new SpongeEventContextKey<>(k, Player.class));
            l.add(EventContextKeys.FIRE_SPREAD, k -> new SpongeEventContextKey<>(k, ServerWorld.class));
            l.add(EventContextKeys.GROWTH_ORIGIN, k -> new SpongeEventContextKey<>(k, BlockSnapshot.class));
            l.add(EventContextKeys.IGNITER, k -> new SpongeEventContextKey<>(k, Living.class));
            l.add(EventContextKeys.LAST_DAMAGE_SOURCE, k -> new SpongeEventContextKey<>(k, DamageSource.class));
            l.add(EventContextKeys.LEAVES_DECAY, k -> new SpongeEventContextKey<>(k, ServerWorld.class));
            l.add(EventContextKeys.LIQUID_BREAK, k -> new SpongeEventContextKey<>(k, ServerWorld.class));
            l.add(EventContextKeys.LIQUID_FLOW, k -> new SpongeEventContextKey<>(k, ServerWorld.class));
            l.add(EventContextKeys.LIQUID_MIX, k -> new SpongeEventContextKey<>(k, ServerWorld.class));
            l.add(EventContextKeys.LOCATION, k -> new SpongeEventContextKey<>(k, ServerLocation.class));
            l.add(EventContextKeys.MOVEMENT_TYPE, k -> new SpongeEventContextKey<>(k, MovementType.class));
            l.add(EventContextKeys.NEIGHBOR_NOTIFY_SOURCE, k -> new SpongeEventContextKey<>(k, BlockSnapshot.class));
            l.add(EventContextKeys.NOTIFIER, k -> new SpongeEventContextKey<>(k, User.class));
            l.add(EventContextKeys.PISTON_EXTEND, k -> new SpongeEventContextKey<>(k, ServerWorld.class));
            l.add(EventContextKeys.PISTON_RETRACT, k -> new SpongeEventContextKey<>(k, ServerWorld.class));
            l.add(EventContextKeys.PLAYER, k -> new SpongeEventContextKey<>(k, Player.class));
            l.add(EventContextKeys.PLAYER_BREAK, k -> new SpongeEventContextKey<>(k, ServerWorld.class));
            l.add(EventContextKeys.PLAYER_PLACE, k -> new SpongeEventContextKey<>(k, ServerWorld.class));
            l.add(EventContextKeys.PLUGIN, k -> new SpongeEventContextKey<>(k, PluginContainer.class));
            l.add(EventContextKeys.PROJECTILE_SOURCE, k -> new SpongeEventContextKey<>(k, ProjectileSource.class));
            l.add(EventContextKeys.ROTATION, k -> new SpongeEventContextKey<>(k, Vector3d.class));
            l.add(EventContextKeys.SIMULATED_PLAYER, k -> new SpongeEventContextKey<>(k, GameProfile.class));
            l.add(EventContextKeys.SPAWN_TYPE, k -> new SpongeEventContextKey<>(k, SpawnType.class));
            l.add(EventContextKeys.SUBJECT, k -> new SpongeEventContextKey<>(k, Subject.class));
            l.add(EventContextKeys.USED_HAND, k -> new SpongeEventContextKey<>(k, HandType.class));
            l.add(EventContextKeys.USED_ITEM, k -> new SpongeEventContextKey<>(k, ItemStackSnapshot.class));
            l.add(EventContextKeys.WEAPON, k -> new SpongeEventContextKey<>(k, ItemStackSnapshot.class));
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
            l.add(GoalTypes.ATTACK_LIVING, k -> new SpongeGoalType(k, AttackLivingGoal.class));
            l.add(GoalTypes.AVOID_LIVING, k -> new SpongeGoalType(k, AvoidLivingGoal.class));
            l.add(GoalTypes.FIND_NEAREST_ATTACKABLE, k -> new SpongeGoalType(k, FindNearestAttackableTargetGoal.class));
            l.add(GoalTypes.LOOK_AT, k -> new SpongeGoalType(k, LookAtGoal.class));
            l.add(GoalTypes.LOOK_RANDOMLY, k -> new SpongeGoalType(k, LookRandomlyGoal.class));
            l.add(GoalTypes.RANDOM_WALKING, k -> new SpongeGoalType(k, RandomWalkingGoal.class));
            l.add(GoalTypes.RANGED_ATTACK_AGAINST_AGENT, k -> new SpongeGoalType(k, RangedAttackAgainstAgentGoal.class));
            l.add(GoalTypes.RUN_AROUND_LIKE_CRAZY, k -> new SpongeGoalType(k, RunAroundLikeCrazyGoal.class));
            l.add(GoalTypes.SWIM, k -> new SpongeGoalType(k, SwimGoal.class));
        });
    }

    public static RegistryLoader<HorseColor> horseColor() {
        return RegistryLoader.of(l -> {
            l.add(HorseColors.BLACK, k -> new SpongeHorseColor(k, 5));
            l.add(HorseColors.BROWN, k -> new SpongeHorseColor(k, 3));
            l.add(HorseColors.CHESTNUT, k -> new SpongeHorseColor(k, 2));
            l.add(HorseColors.CREAMY, k -> new SpongeHorseColor(k, 1));
            l.add(HorseColors.DARK_BROWN, k -> new SpongeHorseColor(k, 6));
            l.add(HorseColors.GRAY, k -> new SpongeHorseColor(k, 5));
            l.add(HorseColors.WHITE, k -> new SpongeHorseColor(k, 0));
        });
    }

    public static RegistryLoader<HorseStyle> horseStyle() {
        return RegistryLoader.of(l -> {
            l.add(HorseStyles.BLACK_DOTS, k -> new SpongeHorseStyle(k, 4));
            l.add(HorseStyles.NONE, k -> new SpongeHorseStyle(k, 0));
            l.add(HorseStyles.WHITE, k -> new SpongeHorseStyle(k, 1));
            l.add(HorseStyles.WHITE_DOTS, k -> new SpongeHorseStyle(k, 3));
            l.add(HorseStyles.WHITEFIELD, k -> new SpongeHorseStyle(k, 2));
        });
    }

    public static RegistryLoader<Key<?>> key() {
        return RegistryLoader.of(l -> {
            l.add(Keys.ABSORPTION, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.DOUBLE_VALUE_TOKEN));
            l.add(Keys.ACCELERATION, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.VECTOR_3D_VALUE_TOKEN));
            l.add(Keys.ACTIVE_ITEM, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.ITEM_STACK_SNAPSHOT_VALUE_TOKEN));
            l.add(Keys.AFFECTS_SPAWNING, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.AGE, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.INTEGER_VALUE_TOKEN));
            l.add(Keys.AIRBORNE_VELOCITY_MODIFIER, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.VECTOR_3D_VALUE_TOKEN));
            l.add(Keys.ANGER_LEVEL, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.INTEGER_VALUE_TOKEN));
            l.add(Keys.APPLICABLE_POTION_EFFECTS, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.LIST_POTION_EFFECT_VALUE_TOKEN));
            l.add(Keys.APPLIED_ENCHANTMENTS, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.LIST_ENCHANTMENT_VALUE_TOKEN));
            l.add(Keys.ARMOR_MATERIAL, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.ARMOR_MATERIAL_VALUE_TOKEN));
            l.add(Keys.ART_TYPE, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.ART_TYPE_VALUE_TOKEN));
            l.add(Keys.ATTACHMENT_SURFACE, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.ATTACHMENT_SURFACE_VALUE_TOKEN));
            l.add(Keys.ATTACK_DAMAGE, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.DOUBLE_VALUE_TOKEN));
            l.add(Keys.ATTACK_TIME, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.INTEGER_VALUE_TOKEN));
            l.add(Keys.AUTHOR, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.COMPONENT_VALUE_TOKEN));
            l.add(Keys.AXIS, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.AXIS_VALUE_TOKEN));
            l.add(Keys.BABY_TICKS, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.INTEGER_VALUE_TOKEN));
            l.add(Keys.BANNER_PATTERN_LAYERS, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.LIST_BANNER_PATTERN_LAYER_VALUE_TOKEN));
            l.add(Keys.BASE_SIZE, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.DOUBLE_VALUE_TOKEN));
            l.add(Keys.BASE_VEHICLE, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.ENTITY_VALUE_TOKEN));
            l.add(Keys.BEAM_TARGET_ENTITY, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.LIVING_VALUE_TOKEN));
            l.add(Keys.BIOME_TEMPERATURE, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.DOUBLE_VALUE_TOKEN));
            l.add(Keys.BLAST_RESISTANCE, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.DOUBLE_VALUE_TOKEN));
            l.add(Keys.BLOCK_LIGHT, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.INTEGER_VALUE_TOKEN));
            l.add(Keys.BLOCK_STATE, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BLOCK_STATE_VALUE_TOKEN));
            l.add(Keys.BLOCK_TEMPERATURE, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.DOUBLE_VALUE_TOKEN));
            l.add(Keys.BOAT_TYPE, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOAT_TYPE_VALUE_TOKEN));
            l.add(Keys.BODY_ROTATIONS, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.MAP_BODY_VECTOR3D_VALUE_TOKEN));
            l.add(Keys.BOSS_BAR, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOSS_BAR_VALUE_TOKEN));
            l.add(Keys.BREAKABLE_BLOCK_TYPES, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.SET_BLOCK_TYPE_VALUE_TOKEN));
            l.add(Keys.BREEDER, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.UUID_VALUE_TOKEN));
            l.add(Keys.BREEDING_COOLDOWN, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.INTEGER_VALUE_TOKEN));
            l.add(Keys.BURN_TIME, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.INTEGER_VALUE_TOKEN));
            l.add(Keys.CAN_BREED, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.CAN_DROP_AS_ITEM, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.CAN_FLY, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.CAN_GRIEF, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.CAN_HARVEST, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.SET_BLOCK_TYPE_VALUE_TOKEN));
            l.add(Keys.CAN_HURT_ENTITIES, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.CAN_JOIN_RAID, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.CAN_MOVE_ON_LAND, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.CAN_PLACE_AS_BLOCK, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.CASTING_TIME, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.INTEGER_VALUE_TOKEN));
            l.add(Keys.CAT_TYPE, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.CAT_TYPE_VALUE_TOKEN));
            l.add(Keys.CHAT_COLORS_ENABLED, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.CHAT_VISIBILITY, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.CHAT_VISIBILITY_VALUE_TOKEN));
            l.add(Keys.CHEST_ATTACHMENT_TYPE, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.CHEST_ATTACHMENT_TYPE_VALUE_TOKEN));
            l.add(Keys.CHEST_ROTATION, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.VECTOR_3D_VALUE_TOKEN));
            l.add(Keys.COLOR, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.COLOR_VALUE_TOKEN));
            l.add(Keys.COMMAND, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.STRING_VALUE_TOKEN));
            l.add(Keys.COMPARATOR_MODE, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.COMPARATOR_MODE_VALUE_TOKEN));
            l.add(Keys.CONNECTED_DIRECTIONS, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.SET_DIRECTION_VALUE_TOKEN));
            l.add(Keys.CONTAINER_ITEM, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.ITEM_TYPE_VALUE_TOKEN));
            l.add(Keys.COOLDOWN, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.INTEGER_VALUE_TOKEN));
            l.add(Keys.CREATOR, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.UUID_VALUE_TOKEN));
            l.add(Keys.CURRENT_SPELL, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.SPELL_TYPE_VALUE_TOKEN));
            l.add(Keys.CUSTOM_ATTACK_DAMAGE, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.MAP_ENTITY_TYPE_DOUBLE_VALUE_TOKEN));
            l.add(Keys.CUSTOM_MODEL_DATA, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.INTEGER_VALUE_TOKEN));
            l.add(Keys.CUSTOM_NAME, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.COMPONENT_VALUE_TOKEN));
            l.add(Keys.DAMAGE_ABSORPTION, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.INTEGER_VALUE_TOKEN));
            l.add(Keys.DAMAGE_PER_BLOCK, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.DOUBLE_VALUE_TOKEN));
            l.add(Keys.DECAY_DISTANCE, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.INTEGER_VALUE_TOKEN));
            l.add(Keys.DERAILED_VELOCITY_MODIFIER, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.VECTOR_3D_VALUE_TOKEN));
            l.add(Keys.DESPAWN_DELAY, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.INTEGER_VALUE_TOKEN));
            l.add(Keys.DETONATOR, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.LIVING_VALUE_TOKEN));
            l.add(Keys.DIRECTION, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.DIRECTION_VALUE_TOKEN));
            l.add(Keys.DISPLAY_NAME, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.COMPONENT_VALUE_TOKEN));
            l.add(Keys.DOMINANT_HAND, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.HAND_PREFERENCE_VALUE_TOKEN));
            l.add(Keys.DOOR_HINGE, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.DOOR_HINGE_VALUE_TOKEN));
            l.add(Keys.DO_EXACT_TELEPORT, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.DURATION, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.INTEGER_VALUE_TOKEN));
            l.add(Keys.DURATION_ON_USE, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.INTEGER_VALUE_TOKEN));
            l.add(Keys.DYE_COLOR, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.DYE_COLOR_VALUE_TOKEN));
            l.add(Keys.EATING_TIME, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.INTEGER_VALUE_TOKEN));
            l.add(Keys.EFFICIENCY, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.DOUBLE_VALUE_TOKEN));
            l.add(Keys.EGG_TIME, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.INTEGER_VALUE_TOKEN));
            l.add(Keys.END_GATEWAY_AGE, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.LONG_VALUE_TOKEN));
            l.add(Keys.EQUIPMENT_TYPE, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.EQUIPMENT_TYPE_VALUE_TOKEN));
            l.add(Keys.EXHAUSTION, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.DOUBLE_VALUE_TOKEN));
            l.add(Keys.EXPERIENCE, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.INTEGER_VALUE_TOKEN));
            l.add(Keys.EXPERIENCE_FROM_START_OF_LEVEL, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.INTEGER_VALUE_TOKEN));
            l.add(Keys.EXPERIENCE_LEVEL, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.INTEGER_VALUE_TOKEN));
            l.add(Keys.EXPERIENCE_SINCE_LEVEL, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.INTEGER_VALUE_TOKEN));
            l.add(Keys.EXPLOSION_RADIUS, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.INTEGER_VALUE_TOKEN));
            l.add(Keys.EYE_HEIGHT, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.DOUBLE_VALUE_TOKEN));
            l.add(Keys.EYE_POSITION, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.VECTOR_3D_VALUE_TOKEN));
            l.add(Keys.FALL_DISTANCE, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.DOUBLE_VALUE_TOKEN));
            l.add(Keys.FALL_TIME, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.INTEGER_VALUE_TOKEN));
            l.add(Keys.FIREWORK_EFFECTS, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.LIST_FIREWORK_EFFECT_VALUE_TOKEN));
            l.add(Keys.FIREWORK_FLIGHT_MODIFIER, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.INTEGER_VALUE_TOKEN));
            l.add(Keys.FIREWORK_SHAPE, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.FIREWORK_SHAPE_VALUE_TOKEN));
            l.add(Keys.FIRE_DAMAGE_DELAY, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.INTEGER_VALUE_TOKEN));
            l.add(Keys.FIRE_TICKS, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.INTEGER_VALUE_TOKEN));
            l.add(Keys.FIRST_DATE_JOINED, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.INSTANT_VALUE_TOKEN));
            l.add(Keys.FIRST_TRUSTED, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.UUID_VALUE_TOKEN));
            l.add(Keys.FLUID_ITEM_STACK, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.FLUID_STACK_SNAPSHOT_VALUE_TOKEN));
            l.add(Keys.FLUID_LEVEL, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.INTEGER_VALUE_TOKEN));
            l.add(Keys.FLUID_TANK_CONTENTS, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.MAP_DIRECTION_FLUID_STACK_SNAPSHOT_VALUE_TOKEN));
            l.add(Keys.FLYING_SPEED, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.DOUBLE_VALUE_TOKEN));
            l.add(Keys.FOOD_LEVEL, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.INTEGER_VALUE_TOKEN));
            l.add(Keys.FOX_TYPE, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.FOX_TYPE_VALUE_TOKEN));
            l.add(Keys.FUEL, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.INTEGER_VALUE_TOKEN));
            l.add(Keys.FUSE_DURATION, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.INTEGER_VALUE_TOKEN));
            l.add(Keys.GAME_MODE, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.GAME_MODE_VALUE_TOKEN));
            l.add(Keys.GAME_PROFILE, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.GAME_PROFILE_VALUE_TOKEN));
            l.add(Keys.GENERATION, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.INTEGER_VALUE_TOKEN));
            l.add(Keys.GROWTH_STAGE, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.INTEGER_VALUE_TOKEN));
            l.add(Keys.HARDNESS, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.DOUBLE_VALUE_TOKEN));
            l.add(Keys.HAS_ARMS, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.HAS_BASE_PLATE, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.HAS_CHEST, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.HAS_EGG, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.HAS_FISH, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.HAS_MARKER, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.HAS_PORES_DOWN, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.HAS_PORES_EAST, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.HAS_PORES_NORTH, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.HAS_PORES_SOUTH, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.HAS_PORES_UP, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.HAS_PORES_WEST, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.HAS_VIEWED_CREDITS, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.HEAD_ROTATION, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.VECTOR_3D_VALUE_TOKEN));
            l.add(Keys.HEALING_CRYSTAL, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.END_CRYSTAL_VALUE_TOKEN));
            l.add(Keys.HEALTH, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.DOUBLE_VALUE_TOKEN));
            l.add(Keys.HEALTH_SCALE, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.DOUBLE_VALUE_TOKEN));
            l.add(Keys.HEIGHT, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.DOUBLE_VALUE_TOKEN));
            l.add(Keys.HELD_ITEM, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.ITEM_TYPE_VALUE_TOKEN));
            l.add(Keys.HIDDEN_GENE, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.PANDA_GENE_VALUE_TOKEN));
            l.add(Keys.HIDE_ATTRIBUTES, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.HIDE_CAN_DESTROY, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.HIDE_CAN_PLACE, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.HIDE_ENCHANTMENTS, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.HIDE_MISCELLANEOUS, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.HIDE_UNBREAKABLE, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.HOME_POSITION, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.VECTOR_3I_VALUE_TOKEN));
            l.add(Keys.HORSE_COLOR, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.HORSE_COLOR_VALUE_TOKEN));
            l.add(Keys.HORSE_STYLE, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.HORSE_STYLE_VALUE_TOKEN));
            l.add(Keys.INFINITE_DESPAWN_DELAY, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.INFINITE_PICKUP_DELAY, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.INSTRUMENT_TYPE, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.INSTRUMENT_TYPE_VALUE_TOKEN));
            l.add(Keys.INVERTED, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.INVULNERABILITY_TICKS, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.INTEGER_VALUE_TOKEN));
            l.add(Keys.INVULNERABLE, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.IN_WALL, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.IS_ADULT, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.IS_AFLAME, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.IS_AI_ENABLED, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.IS_ANGRY, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.IS_ATTACHED, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.IS_BEGGING_FOR_FOOD, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.IS_CELEBRATING, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.IS_CHARGED, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.IS_CHARGING_CROSSBOW, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.IS_CLIMBING, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.IS_CONNECTED_EAST, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.IS_CONNECTED_NORTH, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.IS_CONNECTED_SOUTH, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.IS_CONNECTED_UP, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.IS_CONNECTED_WEST, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.IS_CRITICAL_HIT, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.IS_CROUCHING, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.IS_CUSTOM_NAME_VISIBLE, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.IS_DEFENDING, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.IS_DISARMED, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.IS_EATING, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.IS_EFFECT_ONLY, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.IS_ELYTRA_FLYING, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.IS_EXTENDED, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.IS_FACEPLANTED, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.IS_FILLED, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.IS_FLAMMABLE, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.IS_FLYING, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.IS_FRIGHTENED, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.IS_FULL_BLOCK, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.IS_GLOWING, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.IS_GOING_HOME, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.IS_GRAVITY_AFFECTED, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.IS_HISSING, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.IS_IMMOBILIZED, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.IS_INDIRECTLY_POWERED, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.IS_INTERESTED, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.IS_INVISIBLE, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.IS_IN_WATER, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.IS_JOHNNY, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.IS_LAYING_EGG, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.IS_LEADER, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.IS_LIT, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.IS_LYING_DOWN, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.IS_LYING_ON_BACK, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.IS_OCCUPIED, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.IS_ON_RAIL, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.IS_OPEN, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.IS_PASSABLE, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.IS_PATROLLING, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.IS_PERSISTENT, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.IS_PLACING_DISABLED, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.MAP_EQUIPMENT_TYPE_BOOLEAN_VALUE_TOKEN));
            l.add(Keys.IS_PLAYER_CREATED, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.IS_POUNCING, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.IS_POWERED, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.IS_PRIMED, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.IS_PURRING, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.IS_RELAXED, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.IS_REPLACEABLE, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.IS_ROARING, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.IS_ROLLING_AROUND, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.IS_SADDLED, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.IS_SCREAMING, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.IS_SHEARED, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.IS_SILENT, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.IS_SITTING, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.IS_SLEEPING, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.IS_SLEEPING_IGNORED, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.IS_SMALL, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.IS_SNEAKING, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.IS_SNEEZING, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.IS_SNOWY, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.IS_SOLID, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.IS_SPRINTING, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.IS_STANDING, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.IS_STUNNED, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.IS_SURROGATE_BLOCK, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.IS_TAKING_DISABLED, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.MAP_EQUIPMENT_TYPE_BOOLEAN_VALUE_TOKEN));
            l.add(Keys.IS_TAMED, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.IS_TRADING, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.IS_TRAVELING, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.IS_TRUSTING, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.IS_UNBREAKABLE, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.IS_UNHAPPY, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.IS_WATERLOGGED, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.IS_WET, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.ITEM_DURABILITY, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.INTEGER_VALUE_TOKEN));
            l.add(Keys.ITEM_STACK_SNAPSHOT, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.ITEM_STACK_SNAPSHOT_VALUE_TOKEN));
            l.add(Keys.KNOCKBACK_STRENGTH, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.DOUBLE_VALUE_TOKEN));
            l.add(Keys.KNOWN_GENE, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.PANDA_GENE_VALUE_TOKEN));
            l.add(Keys.LAST_ATTACKER, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.ENTITY_VALUE_TOKEN));
            l.add(Keys.LAST_COMMAND_OUTPUT, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.COMPONENT_VALUE_TOKEN));
            l.add(Keys.LAST_DAMAGE_RECEIVED, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.DOUBLE_VALUE_TOKEN));
            l.add(Keys.LAST_DATE_JOINED, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.INSTANT_VALUE_TOKEN));
            l.add(Keys.LAST_DATE_PLAYED, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.INSTANT_VALUE_TOKEN));
            l.add(Keys.LAYER, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.INTEGER_VALUE_TOKEN));
            l.add(Keys.LEASH_HOLDER, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.ENTITY_VALUE_TOKEN));
            l.add(Keys.LEFT_ARM_ROTATION, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.VECTOR_3D_VALUE_TOKEN));
            l.add(Keys.LEFT_LEG_ROTATION, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.VECTOR_3D_VALUE_TOKEN));
            l.add(Keys.LIFE_TICKS, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.INTEGER_VALUE_TOKEN));
            l.add(Keys.LIGHT_EMISSION, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.INTEGER_VALUE_TOKEN));
            l.add(Keys.LLAMA_TYPE, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.LLAMA_TYPE_VALUE_TOKEN));
            l.add(Keys.LOCALE, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.LOCALE_VALUE_TOKEN));
            l.add(Keys.LOCK_TOKEN, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.STRING_VALUE_TOKEN));
            l.add(Keys.LORE, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.LIST_COMPONENT_VALUE_TOKEN));
            l.add(Keys.MATTER_TYPE, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.MATTER_TYPE_VALUE_TOKEN));
            l.add(Keys.MAX_AIR, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.INTEGER_VALUE_TOKEN));
            l.add(Keys.MAX_BURN_TIME, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.INTEGER_VALUE_TOKEN));
            l.add(Keys.MAX_COOK_TIME, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.INTEGER_VALUE_TOKEN));
            l.add(Keys.MAX_DURABILITY, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.INTEGER_VALUE_TOKEN));
            l.add(Keys.MAX_EXHAUSTION, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.DOUBLE_VALUE_TOKEN));
            l.add(Keys.MAX_FOOD_LEVEL, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.INTEGER_VALUE_TOKEN));
            l.add(Keys.MAX_FALL_DAMAGE, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.INTEGER_VALUE_TOKEN));
            l.add(Keys.MAX_HEALTH, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.DOUBLE_VALUE_TOKEN));
            l.add(Keys.MAX_NEARBY_ENTITIES, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.INTEGER_VALUE_TOKEN));
            l.add(Keys.MAX_SATURATION, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.DOUBLE_VALUE_TOKEN));
            l.add(Keys.MAX_SPAWN_DELAY, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.INTEGER_VALUE_TOKEN));
            l.add(Keys.MAX_SPEED, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.DOUBLE_VALUE_TOKEN));
            l.add(Keys.MAX_STACK_SIZE, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.INTEGER_VALUE_TOKEN));
            l.add(Keys.MINECART_BLOCK_OFFSET, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.INTEGER_VALUE_TOKEN));
            l.add(Keys.MIN_SPAWN_DELAY, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.INTEGER_VALUE_TOKEN));
            l.add(Keys.MOISTURE, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.INTEGER_VALUE_TOKEN));
            l.add(Keys.MOOSHROOM_TYPE, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.MOOSHROOM_TYPE_VALUE_TOKEN));
            l.add(Keys.MUSIC_DISC, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.MUSIC_DISC_VALUE_TOKEN));
            l.add(Keys.NEXT_ENTITY_TO_SPAWN, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.WEIGHTED_ENTITY_ARCHETYPE_VALUE_TOKEN));
            l.add(Keys.NOTE_PITCH, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.NOTE_PITCH_VALUE_TOKEN));
            l.add(Keys.NOTIFIER, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.UUID_VALUE_TOKEN));
            l.add(Keys.OCCUPIED_DECELERATION, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.DOUBLE_VALUE_TOKEN));
            l.add(Keys.ON_GROUND, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.ORIENTATION, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.ORIENTATION_VALUE_TOKEN));
            l.add(Keys.PAGES, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.LIST_COMPONENT_VALUE_TOKEN));
            l.add(Keys.PARROT_TYPE, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.PARROT_TYPE_VALUE_TOKEN));
            l.add(Keys.PARTICLE_EFFECT, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.PARTICLE_EFFECT_VALUE_TOKEN));
            l.add(Keys.PASSED_COOK_TIME, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.INTEGER_VALUE_TOKEN));
            l.add(Keys.PASSENGERS, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.LIST_ENTITY_VALUE_TOKEN));
            l.add(Keys.PATTERN_COLOR, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.DYE_COLOR_VALUE_TOKEN));
            l.add(Keys.PHANTOM_PHASE, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.PHANTOM_PHASE_VALUE_TOKEN));
            l.add(Keys.PICKUP_DELAY, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.INTEGER_VALUE_TOKEN));
            l.add(Keys.PICKUP_RULE, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.PICKUP_RULE_VALUE_TOKEN));
            l.add(Keys.PISTON_TYPE, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.PISTON_TYPE_VALUE_TOKEN));
            l.add(Keys.PLACEABLE_BLOCK_TYPES, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.SET_BLOCK_TYPE_VALUE_TOKEN));
            l.add(Keys.PLAIN_PAGES, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.LIST_STRING_VALUE_TOKEN));
            l.add(Keys.PLUGIN_CONTAINER, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.PLUGIN_CONTAINER_VALUE_TOKEN));
            l.add(Keys.PORES, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.SET_DIRECTION_VALUE_TOKEN));
            l.add(Keys.PORTION_TYPE, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.PORTION_TYPE_VALUE_TOKEN));
            l.add(Keys.POTENTIAL_MAX_SPEED, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.DOUBLE_VALUE_TOKEN));
            l.add(Keys.POTION_EFFECTS, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.LIST_POTION_EFFECT_VALUE_TOKEN));
            l.add(Keys.POTION_TYPE, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.POTION_TYPE_VALUE_TOKEN));
            l.add(Keys.POWER, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.INTEGER_VALUE_TOKEN));
            l.add(Keys.PRIMARY_POTION_EFFECT_TYPE, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.POTION_EFFECT_TYPE_VALUE_TOKEN));
            l.add(Keys.PROFESSION_TYPE, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.PROFESSION_VALUE_TOKEN));
            l.add(Keys.PROFESSION_LEVEL, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.INTEGER_VALUE_TOKEN));
            l.add(Keys.RABBIT_TYPE, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.RABBIT_TYPE_VALUE_TOKEN));
            l.add(Keys.RADIUS, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.DOUBLE_VALUE_TOKEN));
            l.add(Keys.RADIUS_ON_USE, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.DOUBLE_VALUE_TOKEN));
            l.add(Keys.RADIUS_PER_TICK, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.DOUBLE_VALUE_TOKEN));
            l.add(Keys.RAID_WAVE, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.RAID_WAVE_VALUE_TOKEN));
            l.add(Keys.RAIL_DIRECTION, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.RAIL_DIRECTION_VALUE_TOKEN));
            l.add(Keys.REAPPLICATION_DELAY, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.INTEGER_VALUE_TOKEN));
            l.add(Keys.REDSTONE_DELAY, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.INTEGER_VALUE_TOKEN));
            l.add(Keys.REMAINING_AIR, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.INTEGER_VALUE_TOKEN));
            l.add(Keys.REMAINING_BREW_TIME, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.INTEGER_VALUE_TOKEN));
            l.add(Keys.REMAINING_SPAWN_DELAY, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.INTEGER_VALUE_TOKEN));
            l.add(Keys.REPLENISHED_FOOD, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.INTEGER_VALUE_TOKEN));
            l.add(Keys.REPLENISHED_SATURATION, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.DOUBLE_VALUE_TOKEN));
            l.add(Keys.REPRESENTED_INSTRUMENT, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.INSTRUMENT_TYPE_VALUE_TOKEN));
            l.add(Keys.REQUIRED_PLAYER_RANGE, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.DOUBLE_VALUE_TOKEN));
            l.add(Keys.RESPAWN_LOCATIONS, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.MAP_UUID_RESPAWN_LOCATION_VALUE_TOKEN));
            l.add(Keys.RIGHT_ARM_ROTATION, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.VECTOR_3D_VALUE_TOKEN));
            l.add(Keys.RIGHT_LEG_ROTATION, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.VECTOR_3D_VALUE_TOKEN));
            l.add(Keys.ROARING_TIME, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.INTEGER_VALUE_TOKEN));
            l.add(Keys.SATURATION, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.DOUBLE_VALUE_TOKEN));
            l.add(Keys.SCALE, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.DOUBLE_VALUE_TOKEN));
            l.add(Keys.SCOREBOARD_TAGS, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.SET_STRING_VALUE_TOKEN));
            l.add(Keys.SECONDARY_POTION_EFFECT_TYPE, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.POTION_EFFECT_TYPE_VALUE_TOKEN));
            l.add(Keys.SECOND_TRUSTED, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.UUID_VALUE_TOKEN));
            l.add(Keys.SHOOTER, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.PROJECTILE_SOURCE_VALUE_TOKEN));
            l.add(Keys.SHOW_BOTTOM, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.SIGN_LINES, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.LIST_COMPONENT_VALUE_TOKEN));
            l.add(Keys.SIZE, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.INTEGER_VALUE_TOKEN));
            l.add(Keys.SKIN_PARTS, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.SET_SKIN_PARTS_VALUE_TOKEN));
            l.add(Keys.SKIN_PROFILE_PROPERTY, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.PROFILE_PROPERTY_VALUE_TOKEN));
            l.add(Keys.SKIN_MOISTURE, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.INTEGER_VALUE_TOKEN));
            l.add(Keys.SKY_LIGHT, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.INTEGER_VALUE_TOKEN));
            l.add(Keys.SLAB_PORTION, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.SLAB_PORTION_VALUE_TOKEN));
            l.add(Keys.SLEEP_TIMER, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.INTEGER_VALUE_TOKEN));
            l.add(Keys.SLOT_INDEX, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.INTEGER_VALUE_TOKEN));
            l.add(Keys.SLOT_POSITION, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.VECTOR_2I_VALUE_TOKEN));
            l.add(Keys.SLOT_SIDE, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.DIRECTION_VALUE_TOKEN));
            l.add(Keys.SLOWS_UNOCCUPIED, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.SNEEZING_TIME, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.INTEGER_VALUE_TOKEN));
            l.add(Keys.SPAWNABLE_ENTITIES, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.WEIGHTED_ENTITY_ARCHETYPE_COLLECTION_VALUE_TOKEN));
            l.add(Keys.SPAWN_COUNT, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.INTEGER_VALUE_TOKEN));
            l.add(Keys.SPAWN_RANGE, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.DOUBLE_VALUE_TOKEN));
            l.add(Keys.SPECTATOR_TARGET, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.ENTITY_VALUE_TOKEN));
            l.add(Keys.STAIR_SHAPE, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.STAIR_SHAPE_VALUE_TOKEN));
            l.add(Keys.STATISTICS, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.MAP_STATISTIC_LONG_VALUE_TOKEN));
            l.add(Keys.STORED_ENCHANTMENTS, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.LIST_ENCHANTMENT_VALUE_TOKEN));
            l.add(Keys.STRENGTH, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.INTEGER_VALUE_TOKEN));
            l.add(Keys.STRUCTURE_AUTHOR, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.STRING_VALUE_TOKEN));
            l.add(Keys.STRUCTURE_IGNORE_ENTITIES, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.STRUCTURE_INTEGRITY, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.DOUBLE_VALUE_TOKEN));
            l.add(Keys.STRUCTURE_MODE, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.STRUCTURE_MODE_VALUE_TOKEN));
            l.add(Keys.STRUCTURE_POSITION, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.VECTOR_3I_VALUE_TOKEN));
            l.add(Keys.STRUCTURE_POWERED, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.STRUCTURE_SEED, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.LONG_VALUE_TOKEN));
            l.add(Keys.STRUCTURE_SHOW_AIR, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.STRUCTURE_SHOW_BOUNDING_BOX, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.STRUCTURE_SIZE, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.VECTOR_3I_VALUE_TOKEN));
            l.add(Keys.STUCK_ARROWS, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.INTEGER_VALUE_TOKEN));
            l.add(Keys.STUNNED_TIME, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.INTEGER_VALUE_TOKEN));
            l.add(Keys.SUCCESS_COUNT, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.INTEGER_VALUE_TOKEN));
            l.add(Keys.SUSPENDED, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.SWIFTNESS, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.DOUBLE_VALUE_TOKEN));
            l.add(Keys.TAMER, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.UUID_VALUE_TOKEN));
            l.add(Keys.TARGET_ENTITY, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.ENTITY_VALUE_TOKEN));
            l.add(Keys.TARGET_LOCATION, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.VECTOR_3D_VALUE_TOKEN));
            l.add(Keys.TARGET_POSITION, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.VECTOR_3I_VALUE_TOKEN));
            l.add(Keys.TICKS_REMAINING, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.INTEGER_VALUE_TOKEN));
            l.add(Keys.TOOL_TYPE, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.TOOL_TYPE_VALUE_TOKEN));
            l.add(Keys.TRACKS_OUTPUT, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.TRADE_OFFERS, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.LIST_TRADE_OFFER_VALUE_TOKEN));
            l.add(Keys.TRANSIENT, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.TROPICAL_FISH_SHAPE, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.TROPICAL_FISH_SHAPE_VALUE_TOKEN));
            l.add(Keys.UNHAPPY_TIME, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.INTEGER_VALUE_TOKEN));
            l.add(Keys.UNIQUE_ID, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.UUID_VALUE_TOKEN));
            l.add(Keys.UNOCCUPIED_DECELERATION, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.DOUBLE_VALUE_TOKEN));
            l.add(Keys.UNSTABLE, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.UPDATE_GAME_PROFILE, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.VANISH, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.VANISH_IGNORES_COLLISION, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.VANISH_PREVENTS_TARGETING, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.VEHICLE, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.ENTITY_VALUE_TOKEN));
            l.add(Keys.VIEW_DISTANCE, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.INTEGER_VALUE_TOKEN));
            l.add(Keys.VELOCITY, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.VECTOR_3D_VALUE_TOKEN));
            l.add(Keys.VILLAGER_TYPE, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.VILLAGER_TYPE_VALUE_TOKEN));
            l.add(Keys.WAIT_TIME, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.INTEGER_VALUE_TOKEN));
            l.add(Keys.WALKING_SPEED, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.DOUBLE_VALUE_TOKEN));
            l.add(Keys.WILL_SHATTER, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.BOOLEAN_VALUE_TOKEN));
            l.add(Keys.WIRE_ATTACHMENTS, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.MAP_DIRECTION_WIRE_ATTACHMENT_VALUE_TOKEN));
            l.add(Keys.WIRE_ATTACHMENT_EAST, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.WIRE_ATTACHMENT_TYPE_VALUE_TOKEN));
            l.add(Keys.WIRE_ATTACHMENT_NORTH, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.WIRE_ATTACHMENT_TYPE_VALUE_TOKEN));
            l.add(Keys.WIRE_ATTACHMENT_SOUTH, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.WIRE_ATTACHMENT_TYPE_VALUE_TOKEN));
            l.add(Keys.WIRE_ATTACHMENT_WEST, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.WIRE_ATTACHMENT_TYPE_VALUE_TOKEN));
            l.add(Keys.WITHER_TARGETS, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.LIST_ENTITY_VALUE_TOKEN));
            l.add(Keys.WOLOLO_TARGET, k -> SpongeRegistryLoaders.newKey(k, TypeTokens.SHEEP_VALUE_TOKEN));
        });
    }

    private static <E, V extends Value<E>> SpongeKey<V, E> newKey(final ResourceKey key, final TypeToken<V> type) {
        return (SpongeKey<V, E>) new SpongeKeyBuilder<>().key(key).type(type).build();
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
            l.add(MusicDiscs.BLOCKS, k -> new SpongeMusicDisc(k, (MusicDiscItem) Items.MUSIC_DISC_BLOCKS));
            l.add(MusicDiscs.CAT, k -> new SpongeMusicDisc(k, (MusicDiscItem) Items.MUSIC_DISC_CAT));
            l.add(MusicDiscs.CHIRP, k -> new SpongeMusicDisc(k, (MusicDiscItem) Items.MUSIC_DISC_CHIRP));
            l.add(MusicDiscs.FAR, k -> new SpongeMusicDisc(k, (MusicDiscItem) Items.MUSIC_DISC_FAR));
            l.add(MusicDiscs.MALL, k -> new SpongeMusicDisc(k, (MusicDiscItem) Items.MUSIC_DISC_MALL));
            l.add(MusicDiscs.MELLOHI, k -> new SpongeMusicDisc(k, (MusicDiscItem) Items.MUSIC_DISC_MELLOHI));
            l.add(MusicDiscs.MUSIC_DISC_11, k -> new SpongeMusicDisc(k, (MusicDiscItem) Items.MUSIC_DISC_11));
            l.add(MusicDiscs.MUSIC_DISC_13, k -> new SpongeMusicDisc(k, (MusicDiscItem) Items.MUSIC_DISC_13));
            l.add(MusicDiscs.PIGSTEP, k -> new SpongeMusicDisc(k, (MusicDiscItem) Items.MUSIC_DISC_PIGSTEP));
            l.add(MusicDiscs.STAL, k -> new SpongeMusicDisc(k, (MusicDiscItem) Items.MUSIC_DISC_STAL));
            l.add(MusicDiscs.STRAD, k -> new SpongeMusicDisc(k, (MusicDiscItem) Items.MUSIC_DISC_STRAD));
            l.add(MusicDiscs.WAIT, k -> new SpongeMusicDisc(k, (MusicDiscItem) Items.MUSIC_DISC_WAIT));
            l.add(MusicDiscs.WARD, k -> new SpongeMusicDisc(k, (MusicDiscItem) Items.MUSIC_DISC_WARD));
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
            l.add(ParticleOptions.BLOCK_STATE, k -> new SpongeParticleOption<>(k, BlockState.class));
            l.add(ParticleOptions.BLOCK_STATE, k -> new SpongeParticleOption<>(k, Color.class));
            l.add(ParticleOptions.BLOCK_STATE, k -> new SpongeParticleOption<>(k, Direction.class));
            l.add(ParticleOptions.BLOCK_STATE, k -> new SpongeParticleOption<>(k, ItemStackSnapshot.class));
            l.add(ParticleOptions.BLOCK_STATE, k -> new SpongeParticleOption<>(k, Vector3d.class));
            l.add(ParticleOptions.BLOCK_STATE, k -> new SpongeParticleOption<>(k, PotionEffectType.class));
            l.add(ParticleOptions.BLOCK_STATE, k -> new SpongeParticleOption<>(k, Integer.class, v -> v < 1 ? new IllegalArgumentException("Quantity must be at least one") : null));
            l.add(ParticleOptions.BLOCK_STATE, k -> new SpongeParticleOption<>(k, Vector3d.class));
        });
    }

    public static RegistryLoader<PlaceholderParser> placeholderParser() {
        return RegistryLoader.of(l -> {
            l.add(PlaceholderParsers.CURRENT_WORLD, k -> new SpongePlaceholderParserBuilder()
                    .key(ResourceKey.sponge("current_world"))
                    .parser(placeholderText -> Component.text(placeholderText.getAssociatedObject().filter(x -> x instanceof Locatable)
                            .map(x -> ((Locatable) x).getServerLocation().getWorldKey())
                            .orElseGet(() -> Sponge.getServer().getWorldManager().getDefaultPropertiesKey()).toString()))
                    .build());
            l.add(PlaceholderParsers.NAME, k -> new SpongePlaceholderParserBuilder()
                    .key(k)
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
            l.add(QueryTypes.GRID, k -> new SpongeTwoParamQueryType<>(k, GridQuery::new));
            l.add(QueryTypes.INVENTORY_TYPE, k -> new SpongeOneParamQueryType<>(k, InventoryTypeQuery::new));
            l.add(QueryTypes.ITEM_STACK_CUSTOM, k -> new SpongeOneParamQueryType<>(k, ItemStackCustomQuery::new));
            l.add(QueryTypes.ITEM_STACK_EXACT, k -> new SpongeOneParamQueryType<>(k, ItemStackExactQuery::new));
            l.add(QueryTypes.ITEM_STACK_IGNORE_QUANTITY, k -> new SpongeOneParamQueryType<>(k, ItemStackIgnoreQuantityQuery::new));
            l.add(QueryTypes.ITEM_TYPE, k -> new SpongeOneParamQueryType<>(k, ItemTypeQuery::new));
            l.add(QueryTypes.KEY_VALUE, k -> new SpongeOneParamQueryType<KeyValueMatcher<?>>(k, KeyValueMatcherQuery::new));
            l.add(SpongeQueryTypes.LENS, k -> new SpongeOneParamQueryType<>(k, LensQuery::new));
            l.add(QueryTypes.PLAYER_PRIMARY_HOTBAR_FIRST, PlayerPrimaryHotbarFirstQuery::new);
            l.add(QueryTypes.REVERSE, ReverseQuery::new);
            l.add(SpongeQueryTypes.SLOT_LENS, k -> new SpongeOneParamQueryType<>(k, SlotLensQuery::new));
            l.add(QueryTypes.TYPE, k -> new SpongeOneParamQueryType<>(k, TypeQuery::new));
            l.add(SpongeQueryTypes.UNION, k -> new SpongeOneParamQueryType<>(k, UnionQuery::new));
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
            l.add(SelectorSortAlgorithms.ORDER_ARBITRARY, k -> new SpongeSelectorSortAlgorithm(k, EntitySelectorParser.ORDER_ARBITRARY));
            l.add(SelectorSortAlgorithms.ORDER_FURTHEST, k -> new SpongeSelectorSortAlgorithm(k, EntitySelectorParser.ORDER_FURTHEST));
            l.add(SelectorSortAlgorithms.ORDER_NEAREST, k -> new SpongeSelectorSortAlgorithm(k, EntitySelectorParser.ORDER_NEAREST));
            l.add(SelectorSortAlgorithms.ORDER_RANDOM, k -> new SpongeSelectorSortAlgorithm(k, EntitySelectorParser.ORDER_RANDOM));
        });
    }

    public static RegistryLoader<SelectorType> selectorType() {
        return RegistryLoader.of(l -> {
            l.add(SelectorTypes.ALL_ENTITIES, k -> new SpongeSelectorType(k, "@e"));
            l.add(SelectorTypes.ALL_PLAYERS, k -> new SpongeSelectorType(k, "@a"));
            l.add(SelectorTypes.NEAREST_PLAYER, k -> new SpongeSelectorType(k, "@p"));
            l.add(SelectorTypes.RANDOM_PLAYER, k -> new SpongeSelectorType(k, "@r"));
            l.add(SelectorTypes.SOURCE, k -> new SpongeSelectorType(k, "@s"));
        });
    }

    public static RegistryLoader<SkinPart> skinPart() {
        return RegistryLoader.of(l -> l.mapping(SpongeSkinPart::new, m -> m.add(
                SkinParts.CAPE,
                SkinParts.HAT,
                SkinParts.JACKET,
                SkinParts.LEFT_PANTS_LEG,
                SkinParts.LEFT_SLEEVE,
                SkinParts.RIGHT_PANTS_LEG,
                SkinParts.RIGHT_SLEEVE
        )));
    }

    public static RegistryLoader<SpawnType> spawnType() {
        return RegistryLoader.<SpawnType>of(l -> {
            l.add(SpongeSpawnTypes.FORCED, k -> new SpongeSpawnType(k).forced());
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

    public static RegistryLoader<Weather> weather() {
        return RegistryLoader.of(l -> l.mapping(SpongeWeather::new, m -> m.add(
                Weathers.CLEAR,
                Weathers.RAIN,
                Weathers.THUNDER
        )));
    }

    public static RegistryLoader<WorldArchetype> worldArchetype() {
        return RegistryLoader.of(l -> {
            l.add(WorldArchetypes.OVERWORLD, k -> SpongeRegistryLoaders.newWorldArchetype(k, DimensionTypes.OVERWORLD));
            l.add(WorldArchetypes.THE_END, k -> SpongeRegistryLoaders.newWorldArchetype(k, DimensionTypes.THE_END));
            l.add(WorldArchetypes.THE_NETHER, k -> SpongeRegistryLoaders.newWorldArchetype(k, DimensionTypes.THE_NETHER));
        });
    }

    private static WorldArchetype newWorldArchetype(final ResourceKey key, final RegistryReference<DimensionType> dimensionType) {
        final WorldSettings archetype = new WorldSettings(SpongeWorldArchetypeBuilder.RANDOM.nextLong(), GameType.SURVIVAL, true, false, WorldType.DEFAULT);
        ((ResourceKeyBridge) (Object) archetype).bridge$setKey(key);
        ((WorldSettingsBridge) (Object) archetype).bridge$setLogicType((SpongeDimensionType) dimensionType.get());
        ((WorldSettingsBridge) (Object) archetype).bridge$setDifficulty(Difficulty.NORMAL);
        if (dimensionType.get() == DimensionTypes.OVERWORLD.get()) {
            ((WorldSettingsBridge) (Object) archetype).bridge$setGenerateSpawnOnLoad(true);
        }
        return (WorldArchetype) (Object)  archetype;
    }

    // @formatter:on
}
