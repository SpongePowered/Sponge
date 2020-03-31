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
package org.spongepowered.common.data.provider.entity;

import com.google.common.collect.ImmutableList;
import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.entity.item.FallingBlockEntity;
import net.minecraft.entity.item.FireworkRocketEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.item.ItemFrameEntity;
import net.minecraft.entity.item.minecart.MinecartCommandBlockEntity;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.merchant.villager.VillagerProfession;
import net.minecraft.entity.monster.BlazeEntity;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.entity.monster.EndermanEntity;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.ParrotEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.passive.RabbitEntity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.entity.projectile.PotionEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MerchantOffer;
import net.minecraft.item.MerchantOffers;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.PotionUtils;
import net.minecraft.stats.ServerStatisticsManager;
import net.minecraft.stats.Stat;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.GameType;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.CatType;
import org.spongepowered.api.data.type.DyeColor;
import org.spongepowered.api.data.type.HandPreference;
import org.spongepowered.api.data.type.ParrotType;
import org.spongepowered.api.data.type.PickupRule;
import org.spongepowered.api.data.type.Profession;
import org.spongepowered.api.data.type.RabbitType;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.item.merchant.TradeOffer;
import org.spongepowered.api.statistic.Statistic;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Color;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.util.rotation.Rotation;
import org.spongepowered.common.bridge.LocationTargetingBridge;
import org.spongepowered.common.bridge.entity.AggressiveEntityBridge;
import org.spongepowered.common.bridge.entity.EntityBridge;
import org.spongepowered.common.bridge.entity.item.ItemEntityBridge;
import org.spongepowered.common.bridge.entity.monster.ShulkerEntityBridge;
import org.spongepowered.common.bridge.entity.player.BedLocationHolder;
import org.spongepowered.common.bridge.entity.player.PlayerEntityBridge;
import org.spongepowered.common.bridge.entity.player.ServerPlayerEntityBridge;
import org.spongepowered.common.bridge.explosives.ExplosiveBridge;
import org.spongepowered.common.bridge.stats.StatisticsManagerBridge;
import org.spongepowered.common.data.provider.DataProviderRegistry;
import org.spongepowered.common.data.provider.DataProviderRegistryBuilder;
import org.spongepowered.common.data.provider.commandblock.CommandBlockLogicDataProviders;
import org.spongepowered.common.data.provider.entity.ageable.AgeableEntityCanBreedProvider;
import org.spongepowered.common.data.provider.entity.areaeffectcloud.AreaEffectCloudEntityParticleEffectProvider;
import org.spongepowered.common.data.provider.entity.armorstand.ArmorStandEntityBodyRotationsProvider;
import org.spongepowered.common.data.provider.entity.armorstand.ArmorStandEntityPlacingDisabledProvider;
import org.spongepowered.common.data.provider.entity.armorstand.ArmorStandEntityRotationProvider;
import org.spongepowered.common.data.provider.entity.armorstand.ArmorStandEntityTakingDisabledProvider;
import org.spongepowered.common.data.provider.entity.base.EntityFireDamageDelayProvider;
import org.spongepowered.common.data.provider.entity.base.EntityInvulnerabilityTicksProvider;
import org.spongepowered.common.data.provider.entity.horse.AbstractHorseEntityIsSaddledProvider;
import org.spongepowered.common.data.provider.entity.horse.AbstractHorseEntityTamedOwnerProvider;
import org.spongepowered.common.data.provider.entity.horse.AbstractHorseEntityTamedProvider;
import org.spongepowered.common.data.provider.entity.horse.HorseEntityHorseColorProvider;
import org.spongepowered.common.data.provider.entity.horse.HorseEntityHorseStyleProvider;
import org.spongepowered.common.data.provider.entity.living.LivingEntityActiveItemProvider;
import org.spongepowered.common.data.provider.entity.living.LivingEntityBodyRotationsProvider;
import org.spongepowered.common.data.provider.entity.living.LivingEntityChestRotationProvider;
import org.spongepowered.common.data.provider.entity.living.LivingEntityHeadRotationProvider;
import org.spongepowered.common.data.provider.entity.living.LivingEntityHealthProvider;
import org.spongepowered.common.data.provider.entity.living.LivingEntityLastAttackerProvider;
import org.spongepowered.common.data.provider.entity.living.LivingEntityMaxAirProvider;
import org.spongepowered.common.data.provider.entity.living.LivingEntityMaxHealthProvider;
import org.spongepowered.common.data.provider.entity.living.LivingEntityPotionEffectsProvider;
import org.spongepowered.common.data.provider.entity.living.LivingEntityRemainingAirProvider;
import org.spongepowered.common.data.provider.entity.minecart.AbstractMinecartEntityBlockOffsetProvider;
import org.spongepowered.common.data.provider.entity.minecart.AbstractMinecartEntityBlockStateProvider;
import org.spongepowered.common.data.provider.entity.player.PlayerEntityCanFlyProvider;
import org.spongepowered.common.data.provider.entity.player.PlayerEntityExhaustionProvider;
import org.spongepowered.common.data.provider.entity.player.PlayerEntityExperienceFromStartOfLevelValueProvider;
import org.spongepowered.common.data.provider.entity.player.PlayerEntityExperienceLevelProvider;
import org.spongepowered.common.data.provider.entity.player.PlayerEntityExperienceProvider;
import org.spongepowered.common.data.provider.entity.player.PlayerEntityExperienceSinceLevelProvider;
import org.spongepowered.common.data.provider.entity.player.PlayerEntityFlyingSpeedProvider;
import org.spongepowered.common.data.provider.entity.player.PlayerEntityFoodLevelProvider;
import org.spongepowered.common.data.provider.entity.player.PlayerEntityIsFlyingProvider;
import org.spongepowered.common.data.provider.entity.player.PlayerEntitySaturationProvider;
import org.spongepowered.common.data.provider.entity.player.PlayerEntityWalkingSpeedProvider;
import org.spongepowered.common.data.provider.entity.user.UserFirstDatePlayedProvider;
import org.spongepowered.common.data.provider.entity.user.UserLastDatePlayedProvider;
import org.spongepowered.common.data.provider.entity.vanishable.VanishableEntityInvisibleProvider;
import org.spongepowered.common.data.provider.entity.vanishable.VanishableEntityVanishIgnoresCollisionProvider;
import org.spongepowered.common.data.provider.entity.vanishable.VanishableEntityVanishPreventsTargetingProvider;
import org.spongepowered.common.data.provider.entity.vanishable.VanishableEntityVanishProvider;
import org.spongepowered.common.data.provider.entity.wolf.WolfEntityIsWetProvider;
import org.spongepowered.common.data.provider.util.FireworkUtils;
import org.spongepowered.common.data.type.SpongeCatType;
import org.spongepowered.common.data.type.SpongeParrotType;
import org.spongepowered.common.data.type.SpongeRabbitType;
import org.spongepowered.common.data.util.PotionEffectHelper;
import org.spongepowered.common.entity.living.human.HumanEntity;
import org.spongepowered.common.item.util.ItemStackUtil;
import org.spongepowered.common.mixin.accessor.entity.AreaEffectCloudEntityAccessor;
import org.spongepowered.common.mixin.accessor.entity.EntityAccessor;
import org.spongepowered.common.mixin.accessor.entity.LivingEntityAccessor;
import org.spongepowered.common.mixin.accessor.entity.MobEntityAccessor;
import org.spongepowered.common.mixin.accessor.entity.item.ArmorStandEntityAccessor;
import org.spongepowered.common.mixin.accessor.entity.item.ExperienceOrbEntityAccessor;
import org.spongepowered.common.mixin.accessor.entity.item.FallingBlockEntityAccessor;
import org.spongepowered.common.mixin.accessor.entity.item.FireworkRocketEntityAccessor;
import org.spongepowered.common.mixin.accessor.entity.item.HangingEntityAccessor;
import org.spongepowered.common.mixin.accessor.entity.monster.BlazeEntityAccessor;
import org.spongepowered.common.mixin.accessor.entity.monster.CreeperEntityAccessor;
import org.spongepowered.common.mixin.accessor.entity.monster.EndermanEntityAccessor;
import org.spongepowered.common.mixin.accessor.entity.monster.VindicatorEntityAccessor;
import org.spongepowered.common.mixin.accessor.entity.monster.ZombiePigmanEntityAccessor;
import org.spongepowered.common.mixin.accessor.entity.projectile.AbstractArrowEntityAccessor;
import org.spongepowered.common.mixin.accessor.entity.projectile.ShulkerBulletEntityAccessor;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.VecHelper;

import java.util.Map;
import java.util.stream.Collectors;

public class EntityDataProviders extends DataProviderRegistryBuilder {

    public EntityDataProviders(DataProviderRegistry registry) {
        super(registry);
    }

    @Override
    public void register() {
        register(new ArrowEntityPotionEffectsProvider());
        register(new BoatEntityWoodTypeProvider());
        register(new MobEntityDominantHandProvider());

        // Other
        register(BlazeEntity.class, Keys.IS_AFLAME,
                BlazeEntity::isBurning,
                (accessor, value) -> ((BlazeEntityAccessor) accessor).accessor$setOnFire(value));

        register(AggressiveEntityBridge.class, Keys.IS_ANGRY,
                AggressiveEntityBridge::bridge$isAngry,
                AggressiveEntityBridge::bridge$setAngry);

        register(PigEntity.class, Keys.IS_SADDLED,
                PigEntity::getSaddled,
                PigEntity::setSaddled);

        registerAbstractMinecartEntityData();
        registerFireworkRocketEntityData();
        registerPaintingEntityData();
        registerAbstractArrowEntityData();
        registerAreaEffectCloudEntityData();
        registerFallingBlockEntityData();
        registerArmorStandEntityData();
        registerMinecartCommandBlockEntityData();
        registerVanishableEntityData();
        registerVindicatorEntityData();
        registerIronGolemEntityData();
        registerSheepEntityData();
        registerZombiePigmanEntityData();
        registerWolfEntityData();
        registerEndermanEntityData();
        registerEndermiteEntityData();
        registerCreeperEntityData();
        registerPlayerEntityData();
        registerUserData();
        registerSlimeEntityData();
        registerHorseEntityData();
        registerAbstractHorseEntityData();
        registerTameableEntityData();
        registerAgeableEntityData();
        registerExplosiveData();
        registerItemEntityData();
        registerAgentEntityData();
        registerLivingEntityData();
        registerEntityData();

        register(ExperienceOrbEntityAccessor.class, Keys.EXPERIENCE, ExperienceOrbEntityAccessor::accessor$getXpValue, ExperienceOrbEntityAccessor::accessor$setXpValue);
        register(HangingEntityAccessor.class, Keys.DIRECTION,
                e -> e.accessor$facingDirection() == null ? Direction.NONE : Constants.DirectionFunctions.getFor(e.accessor$facingDirection()),
                (e, v) -> e.accessor$updateFacingWithBoundingBox(Constants.DirectionFunctions.getFor(v)));

        // TODO deduplicate code
        register(CatEntity.class, Keys.CAT_TYPE, e -> {
            int type = e.getCatType();
            return Sponge.getRegistry().getCatalogRegistry().getAllOf(CatType.class)
                    .filter(t -> ((SpongeCatType)t).getMetadata() == type)
                    .findFirst().orElse(null);
        }, (e, v) -> e.setCatType(((SpongeCatType)v).getMetadata()));
        register(ParrotEntity.class, Keys.PARROT_TYPE, e -> {
            int type = e.getVariant();
            return Sponge.getRegistry().getCatalogRegistry().getAllOf(ParrotType.class)
                    .filter(t -> ((SpongeParrotType)t).getMetadata() == type)
                    .findFirst().orElse(null);
        }, (e, v) -> e.setVariant(((SpongeParrotType)v).getMetadata()));
        register(RabbitEntity.class, Keys.RABBIT_TYPE, e -> {
            int type = e.getRabbitType();
            return Sponge.getRegistry().getCatalogRegistry().getAllOf(RabbitType.class)
                    .filter(t -> ((SpongeRabbitType)t).getMetadata() == type)
                    .findFirst().orElse(null);
        }, (e, v) -> e.setRabbitType(((SpongeRabbitType)v).getMetadata()));

        register(new TameableEntityTamedProvider());
        register(new TameableEntityTamedOwnerProvider());

        register(ItemFrameEntity.class, Keys.ITEM_STACK_SNAPSHOT, // TODO removal allowed?
                e -> ItemStackUtil.snapshotOf(e.getDisplayedItem()), (e, i) -> e.setDisplayedItem(ItemStackUtil.fromSnapshotToNative(i)));
        register(ItemEntity.class, Keys.ITEM_STACK_SNAPSHOT, // TODO no removal allowed?
                e -> ItemStackUtil.snapshotOf(e.getItem()), (e, i) -> e.setItem(ItemStackUtil.fromSnapshotToNative(i)));

        register(ItemFrameEntity.class, Keys.ROTATION,
                e -> Rotation.fromDegrees(e.getRotation() * 45).get(), (e, r) -> e.setItemRotation(r.getAngle() / 45));

        register(ShulkerBulletEntityAccessor.class, Keys.DIRECTION,
                e -> e.accessor$getDirection() == null ? Direction.NONE : Constants.DirectionFunctions.getFor(e.accessor$getDirection()),
                (e, d) -> e.accessor$setDirection(Constants.DirectionFunctions.getFor(d)));

        register(ShulkerEntityBridge.class, Keys.DIRECTION, ShulkerEntityBridge::bridge$getDirection, ShulkerEntityBridge::bridge$setDirection);

        register(HumanEntity.class, Keys.SKIN_UNIQUE_ID, e -> e.getSkinUuid(), (e, p) -> e.setSkinUuid(p));

        register(Entity.class, Keys.PASSENGERS,
                e -> e.getPassengers().stream().map(org.spongepowered.api.entity.Entity.class::cast).collect(Collectors.toList()),
                (e, p) -> {
                    e.getPassengers().clear();
                    p.forEach(p1 -> e.getPassengers().add((Entity)p1));
                });

        register(BedLocationHolder.class, Keys.RESPAWN_LOCATIONS, BedLocationHolder::bridge$getBedlocations, BedLocationHolder::bridge$setBedLocations);
        register(LocationTargetingBridge.class, Keys.TARGET_LOCATION, LocationTargetingBridge::bridge$getTargetedLocation, LocationTargetingBridge::bridge$setTargetedLocation);

        register(ShulkerBulletEntityAccessor.class, Keys.TARGET_ENTITY,
                e -> (org.spongepowered.api.entity.Entity) e.accessor$getTarget(),
                (e, te) -> e.accessor$setTarget((Entity)te));

        register(VillagerEntity.class, Keys.PROFESSION,
                e -> (Profession) e.getVillagerData().getProfession(),
                (e, p) -> e.setVillagerData(e.getVillagerData().withProfession((VillagerProfession) p)));

        register(FireworkRocketEntity.class, Keys.FIREWORK_FLIGHT_MODIFIER,
                e -> {
                    final ItemStack item = FireworkUtils.getItem(e);
                    final CompoundNBT fireworks = item.getOrCreateChildTag(Constants.Item.Fireworks.FIREWORKS);
                    if (fireworks.contains(Constants.Item.Fireworks.FLIGHT)) {
                        return (int) fireworks.getByte(Constants.Item.Fireworks.FLIGHT);
                    }
                    return null;
                },
                (e, m) -> {
                    final ItemStack item = FireworkUtils.getItem(e);
                    final CompoundNBT fireworks = item.getOrCreateChildTag(Constants.Item.Fireworks.FIREWORKS);
                    fireworks.putByte(Constants.Item.Fireworks.FLIGHT, m.byteValue());
                    ((FireworkRocketEntityAccessor) e).accessor$setLifeTime(10 * m.byteValue() + ((EntityAccessor) e).accessor$getRand().nextInt(6) + ((EntityAccessor) e).accessor$getRand().nextInt(7));
                }
                );
        register(PotionEntity.class, Keys.POTION_EFFECTS,
                e -> PotionUtils.getEffectsFromStack(e.getItem()).stream().map(PotionEffect.class::cast).collect(Collectors.toList())
                // no setter?
                );

        register(new PotionEntityItemProvider());

        register(VillagerEntity.class, Keys.TRADE_OFFERS,
                e -> e.getOffers().stream().map(TradeOffer.class::cast).collect(Collectors.toList()),
                (e, offers) -> e.setOffers(offers.stream().map(MerchantOffer.class::cast).collect(Collectors.toCollection(MerchantOffers::new))));

        register(ServerPlayerEntity.class, Keys.STATISTICS,
                p -> ((StatisticsManagerBridge)p.getStats()).bridge$getStatsData().entrySet().stream().collect(Collectors.toMap(e -> (Statistic)e.getKey(), e -> e.getValue().longValue())),
                (p, stats) -> stats.forEach((k, v) -> p.getStats().setValue(p, (Stat<?>) k, v.intValue())));

        register(ServerPlayerEntityBridge.class, Keys.HEALTH_SCALE, Constants.Entity.Player.DEFAULT_HEALTH_SCALE,
                p -> p.bridge$isHealthScaled() ? p.bridge$getHealthScale() : null,
                (p, s) -> p.bridge$setHealthScale(s) // TODO limit 1-Float.MAX_VALUE
                );

        // TODO revenge target?
        register(LivingEntityAccessor.class, Keys.LAST_DAMAGE_RECEIVED,
                e -> (double) e.accessor$getLastDamage(),
                (e, d) -> e.accessor$setLastDamage(d.floatValue()));

        register(Entity.class, Keys.VEHICLE,
                e -> ((org.spongepowered.api.entity.Entity) e.getRidingEntity()),
                (e, vehicle) -> e.startRiding((Entity) vehicle, true));
        register(Entity.class, Keys.BASE_VEHICLE,
                e -> ((org.spongepowered.api.entity.Entity) e.getLowestRidingEntity()));

        register(new DamagingProjectileEntityAccelerationProvider());

        register(new FusedExplosiveTicksRemainingProvider());

    }

    private void registerFireworkRocketEntityData() {
        register(FireworkRocketEntity.class, Keys.FIREWORK_EFFECTS, ImmutableList.of(),
                (accessor) -> FireworkUtils.getFireworkEffects(accessor).orElse(null),
                FireworkUtils::setFireworkEffects);
    }

    private void registerPaintingEntityData() {
        register(new PaintingEntityArtProvider());
    }

    private void registerAbstractArrowEntityData() {
        register(AbstractArrowEntity.class, Keys.IS_CRITICAL_HIT,
                AbstractArrowEntity::getIsCritical,
                AbstractArrowEntity::setIsCritical);

        register(AbstractArrowEntity.class, Keys.PICKUP_RULE,
                (accessor) -> (PickupRule) (Object) accessor.pickupStatus,
                (accessor, value) -> accessor.pickupStatus = (AbstractArrowEntity.PickupStatus) (Object) value);

        register(AbstractArrowEntity.class, Keys.KNOCKBACK_STRENGTH,
                (accessor) -> (double) ((AbstractArrowEntityAccessor) accessor).accessor$getKnockbackStrength(),
                (accessor, value) -> accessor.setKnockbackStrength((int) Math.round(value)));
    }

    private void registerVindicatorEntityData() {
        register(VindicatorEntityAccessor.class, Keys.IS_JOHNNY,
                VindicatorEntityAccessor::accessor$getIsJohnny,
                VindicatorEntityAccessor::accessor$setIsJohnny);
    }

    private void registerZombiePigmanEntityData() {
        register(ZombiePigmanEntityAccessor.class, Keys.ANGER_LEVEL,
                ZombiePigmanEntityAccessor::accessor$getAngerLevel,
                ZombiePigmanEntityAccessor::accessor$setAngerLevel);
    }

    private void registerIronGolemEntityData() {
        register(IronGolemEntity.class, Keys.IS_PLAYER_CREATED,
                IronGolemEntity::isPlayerCreated,
                IronGolemEntity::setPlayerCreated);
    }

    private void registerEndermanEntityData() {
        register(EndermanEntity.class, Keys.IS_SCREAMING,
                EndermanEntity::isScreaming,
                (accessor, value) -> accessor.getDataManager().set(EndermanEntityAccessor.accessor$getScreaming(), value));
    }

    private void registerEndermiteEntityData() {
        register(new EndermiteExpirationDelayProvider());
    }

    private void registerCreeperEntityData() {
        register(CreeperEntity.class, Keys.IS_CHARGED,
                CreeperEntity::getPowered,
                (accessor, value) -> accessor.getDataManager().set(CreeperEntityAccessor.accessor$getPowered(), value));
    }

    private void registerAbstractMinecartEntityData() {
        register(new AbstractMinecartEntityBlockOffsetProvider());
        register(new AbstractMinecartEntityBlockStateProvider());
    }

    private void registerFallingBlockEntityData() {
        register(FallingBlockEntityAccessor.class, Keys.BLOCK_STATE,
                (accessor) -> (BlockState) accessor.accessor$getFallTile(),
                (accessor, value) -> accessor.accessor$setFallTile((net.minecraft.block.BlockState) value));

        register(FallingBlockEntityAccessor.class, Keys.CAN_PLACE_AS_BLOCK,
                (accessor) -> !accessor.accessor$getDontSetBlock(),
                (accessor, value) -> accessor.accessor$setDontSetAsBlock(!value));

        // TODO old Key in API? CAN_DROP_AS_ITEM
        register(FallingBlockEntity.class, Keys.SHOULD_DROP,
                (accessor) -> accessor.shouldDropItem,
                (accessor, value) -> accessor.shouldDropItem = value);

        register(FallingBlockEntityAccessor.class, Keys.CAN_HURT_ENTITIES,
                FallingBlockEntityAccessor::accessor$getHurtEntities,
                FallingBlockEntityAccessor::accessor$setHurtEntities);

        register(FallingBlockEntityAccessor.class, Keys.DAMAGE_PER_BLOCK,
                (accessor) -> (double) accessor.accessor$getFallHurtAmount(),
                (accessor, value) -> accessor.accessor$setFallHurtAmount(value.floatValue()));

        register(FallingBlockEntityAccessor.class, Keys.FALL_TIME,
                FallingBlockEntityAccessor::accessor$getFallTime,
                FallingBlockEntityAccessor::accessor$setFallTime);

        register(FallingBlockEntityAccessor.class, Keys.MAX_FALL_DAMAGE,
                (accessor) -> (double) accessor.accessor$getFallHurtMax(),
                (accessor, value) -> accessor.accessor$setFallHurtMax((int) Math.ceil(value)));
    }

    private void registerArmorStandEntityData() {
        register(ArmorStandEntity.class, Keys.ARMOR_STAND_HAS_ARMS,
                ArmorStandEntity::getShowArms,
                (accessor, value) -> ((ArmorStandEntityAccessor) accessor).accessor$setShowArms(value));

        register(ArmorStandEntity.class, Keys.ARMOR_STAND_HAS_BASE_PLATE,
                (accessor) -> !accessor.hasNoBasePlate(),
                (accessor, value) -> ((ArmorStandEntityAccessor) accessor).accessor$setNoBasePlate(!value));

        register(ArmorStandEntity.class, Keys.ARMOR_STAND_HAS_MARKER,
                ArmorStandEntity::hasMarker,
                (accessor, value) -> ((ArmorStandEntityAccessor) accessor).accessor$setMarker(value));

        register(ArmorStandEntity.class, Keys.ARMOR_STAND_IS_SMALL,
                ArmorStandEntity::isSmall,
                (accessor, value) -> ((ArmorStandEntityAccessor) accessor).accessor$setSmall(value));

        register(new ArmorStandEntityRotationProvider<>(Keys.CHEST_ROTATION,
                ArmorStandEntity::getBodyRotation,
                ArmorStandEntity::setBodyRotation));

        register(new ArmorStandEntityRotationProvider<>(Keys.HEAD_ROTATION,
                ArmorStandEntity::getHeadRotation,
                ArmorStandEntity::setHeadRotation));

        register(new ArmorStandEntityRotationProvider<>(Keys.LEFT_ARM_ROTATION,
                ArmorStandEntityAccessor::accessor$getLeftArmRotation,
                ArmorStandEntity::setLeftArmRotation));

        register(new ArmorStandEntityRotationProvider<>(Keys.LEFT_LEG_ROTATION,
                ArmorStandEntityAccessor::accessor$getLeftLegRotation,
                ArmorStandEntity::setLeftLegRotation));

        register(new ArmorStandEntityRotationProvider<>(Keys.RIGHT_ARM_ROTATION,
                ArmorStandEntityAccessor::accessor$getRightArmRotation,
                ArmorStandEntity::setLeftArmRotation));

        register(new ArmorStandEntityRotationProvider<>(Keys.RIGHT_LEG_ROTATION,
                ArmorStandEntityAccessor::accessor$getRightLegRotation,
                ArmorStandEntity::setLeftLegRotation));

        register(new ArmorStandEntityBodyRotationsProvider());
        register(new ArmorStandEntityPlacingDisabledProvider());
        register(new ArmorStandEntityTakingDisabledProvider());
    }

    private void registerMinecartCommandBlockEntityData() {
        new CommandBlockLogicDataProviders<>(this.registry, MinecartCommandBlockEntity.class,
                MinecartCommandBlockEntity::getCommandBlockLogic).register();
    }

    private void registerSlimeEntityData() {
        register(new SlimeEntitySizeProvider());
    }

    private void registerAbstractHorseEntityData() {
        register(new AbstractHorseEntityIsSaddledProvider());
        register(new AbstractHorseEntityTamedOwnerProvider());
        register(new AbstractHorseEntityTamedProvider());
    }

    private void registerHorseEntityData() {
        register(new HorseEntityHorseColorProvider());
        register(new HorseEntityHorseStyleProvider());
    }

    private void registerAreaEffectCloudEntityData() {
        register(AreaEffectCloudEntity.class, Keys.AREA_EFFECT_CLOUD_AGE,
                (accessor) -> accessor.ticksExisted,
                (accessor, value) -> accessor.ticksExisted = value);

        register(AreaEffectCloudEntity.class, Keys.AREA_EFFECT_CLOUD_REAPPLICATION_DELAY,
                (accessor) -> accessor.ticksExisted,
                (accessor, value) -> accessor.ticksExisted = value);

        register(AreaEffectCloudEntity.class, Keys.AREA_EFFECT_CLOUD_COLOR,
                (accessor) -> Color.ofRgb(accessor.getColor()),
                (accessor, value) -> accessor.setColor(value.getRgb()));

        register(AreaEffectCloudEntityAccessor.class, Keys.AREA_EFFECT_CLOUD_DURATION_ON_USE,
                AreaEffectCloudEntityAccessor::accessor$getDurationOnUse,
                AreaEffectCloudEntityAccessor::accessor$setDurationOnUse);

        register(AreaEffectCloudEntity.class, Keys.AREA_EFFECT_CLOUD_DURATION,
                AreaEffectCloudEntity::getDuration,
                AreaEffectCloudEntity::setDuration);

        register(AreaEffectCloudEntity.class, Keys.AREA_EFFECT_CLOUD_RADIUS_ON_USE,
                (accessor) -> (double) ((AreaEffectCloudEntityAccessor) accessor).accessor$getRadiusOnUse(),
                (accessor, value) -> accessor.setRadiusOnUse(value.floatValue()));

        register(AreaEffectCloudEntity.class, Keys.AREA_EFFECT_CLOUD_RADIUS,
                (accessor) -> (double) accessor.getRadius(),
                (accessor, value) -> accessor.setRadius(value.floatValue()));

        register(AreaEffectCloudEntity.class, Keys.AREA_EFFECT_CLOUD_WAIT_TIME,
                (accessor) -> ((AreaEffectCloudEntityAccessor) accessor).accessor$getWaitTime(),
                AreaEffectCloudEntity::setWaitTime);

        register(AreaEffectCloudEntityAccessor.class, Keys.POTION_EFFECTS,
                (accessor) -> PotionEffectHelper.copyAsPotionEffects(accessor.accessor$getEffects()),
                (accessor, value) -> accessor.accessor$setEffects(PotionEffectHelper.copyAsEffectInstances(value)));

        register(new AreaEffectCloudEntityParticleEffectProvider());
    }

    private void registerTameableEntityData() {
        register(TameableEntity.class, Keys.IS_SITTING,
                TameableEntity::isSitting,
                TameableEntity::setSitting);
    }

    private void registerAgeableEntityData() {
        register(AgeableEntity.class, Keys.AGEABLE_AGE,
                AgeableEntity::getGrowingAge,
                AgeableEntity::setGrowingAge);

        register(AgeableEntity.class, Keys.IS_ADULT,
                (accessor) -> !accessor.isChild(),
                (accessor, value) -> accessor.setGrowingAge(value ? Constants.Entity.Ageable.ADULT : Constants.Entity.Ageable.CHILD));

        register(new AgeableEntityCanBreedProvider());
    }

    private void registerAgentEntityData() {
        register(MobEntityAccessor.class, Keys.IS_AI_ENABLED,
                (accessor) -> !accessor.accessor$isAIDisabled(),
                (accessor, value) -> accessor.accessor$setNoAI(!value));
        register(MobEntityAccessor.class, Keys.IS_PERSISTENT,
                (accessor) -> ((MobEntity) accessor).isNoDespawnRequired(),
                MobEntityAccessor::accessor$setPersistingRequired);
    }

    private void registerLivingEntityData() {
        register(LivingEntity.class, Keys.ABSORPTION,
                (accessor) -> (double) accessor.getAbsorptionAmount(),
                (accessor, value) -> accessor.setAbsorptionAmount(value.floatValue()));

        register(LivingEntity.class, Keys.FALL_DISTANCE,
                (accessor) -> (double) accessor.fallDistance,
                (accessor, value) -> accessor.fallDistance = value.floatValue());

        register(LivingEntity.class, Keys.STUCK_ARROWS,
                LivingEntity::getArrowCountInEntity,
                (accessor, value) -> accessor.setArrowCountInEntity(MathHelper.clamp(value, 0, Integer.MAX_VALUE)));

        register(LivingEntity.class, Keys.IS_ELYTRA_FLYING,
                LivingEntity::isElytraFlying,
                (accessor, value) ->((EntityAccessor) accessor).accessor$setFlag(Constants.Entity.ELYTRA_FLYING_FLAG, value));

        register(new LivingEntityActiveItemProvider());
        register(new LivingEntityBodyRotationsProvider());
        register(new LivingEntityChestRotationProvider());
        register(new LivingEntityHeadRotationProvider());
        register(new LivingEntityHealthProvider());
        register(new LivingEntityLastAttackerProvider());
        register(new LivingEntityMaxAirProvider());
        register(new LivingEntityMaxHealthProvider());
        register(new LivingEntityPotionEffectsProvider());
        register(new LivingEntityRemainingAirProvider());
    }

    private void registerExplosiveData() {
        register(ExplosiveBridge.class, Keys.EXPLOSION_RADIUS,
                (accessor) -> accessor.bridge$getExplosionRadius().map(Integer::doubleValue).orElse(null),
                (accessor, value) -> accessor.bridge$setExplosionRadius(value.intValue()));
    }

    private void registerEntityData() {
        register(Entity.class, Keys.VELOCITY,
                (accessor) -> VecHelper.toVector3d(accessor.getMotion()),
                (accessor, value) -> accessor.setMotion(VecHelper.toVec3d(value)));

        register(Entity.class, Keys.IS_CUSTOM_NAME_VISIBLE,
                Entity::isCustomNameVisible,
                Entity::setCustomNameVisible);

        register(Entity.class, Keys.IS_FLYING,
                (accessor) -> accessor.isAirBorne,
                (accessor, value) -> accessor.isAirBorne = value);

        register(Entity.class, Keys.IS_GRAVITY_AFFECTED,
                (accessor) -> !accessor.hasNoGravity(),
                (accessor, value) -> accessor.setNoGravity(!value));

        register(Entity.class, Keys.EYE_HEIGHT, entity -> (double) entity.getEyeHeight());
        register(Entity.class, Keys.EYE_POSITION, entity -> VecHelper.toVector3d(entity.getEyePosition(1f)));
        register(Entity.class, Keys.IS_WET, Entity::isWet);
        register(Entity.class, Keys.IS_SNEAKING, Entity::isSneaking, Entity::setSneaking);
        register(Entity.class, Keys.IS_SPRINTING, Entity::isSprinting, Entity::setSprinting);
        register(Entity.class, Keys.ON_GROUND, entity -> entity.onGround);
        register(Entity.class, Keys.IS_SILENT, Entity::isSilent, Entity::setSilent);
        register(Entity.class, Keys.IS_GLOWING, Entity::isGlowing, Entity::setGlowing);

        register(EntityBridge.class, Keys.DISPLAY_NAME, (Text) null,
                EntityBridge::bridge$getDisplayNameText,
                EntityBridge::bridge$setDisplayName);

        register(new EntityFireDamageDelayProvider());
        register(new EntityInvulnerabilityTicksProvider());
    }

    private void registerSheepEntityData() {
        register(SheepEntity.class, Keys.DYE_COLOR,
                (accessor) -> (DyeColor) (Object) accessor.getFleeceColor(),
                (accessor, value) -> accessor.setFleeceColor((net.minecraft.item.DyeColor) (Object) value));

        register(SheepEntity.class, Keys.IS_SHEARED,
                SheepEntity::getSheared,
                SheepEntity::setSheared);
    }

    private void registerWolfEntityData() {
        register(WolfEntity.class, Keys.DYE_COLOR,
                (accessor) -> (DyeColor) (Object) accessor.getCollarColor(),
                (accessor, value) -> accessor.setCollarColor((net.minecraft.item.DyeColor) (Object) value));

        register(new WolfEntityIsWetProvider());
    }

    private void registerVanishableEntityData() {
        register(new VanishableEntityInvisibleProvider());
        register(new VanishableEntityVanishProvider());
        register(new VanishableEntityVanishIgnoresCollisionProvider());
        register(new VanishableEntityVanishPreventsTargetingProvider());
    }

    private void registerPlayerEntityData() {
        register(PlayerEntity.class, Keys.DOMINANT_HAND,
                (accessor) -> (HandPreference) (Object) accessor.getPrimaryHand(),
                (accessor, value) -> accessor.setPrimaryHand((HandSide) (Object) value));

        register(PlayerEntityBridge.class, Keys.AFFECTS_SPAWNING,
                PlayerEntityBridge::bridge$affectsSpawning,
                PlayerEntityBridge::bridge$setAffectsSpawning);

        register(ServerPlayerEntity.class, Keys.GAME_MODE,
                (accessor) -> (GameMode) (Object) accessor.interactionManager.getGameType(),
                (accessor, value) -> accessor.setGameType((GameType) (Object) value));

        register(new PlayerEntityCanFlyProvider());
        register(new PlayerEntityExhaustionProvider());
        register(new PlayerEntityExperienceFromStartOfLevelValueProvider());
        register(new PlayerEntityExperienceLevelProvider());
        register(new PlayerEntityExperienceProvider());
        register(new PlayerEntityExperienceSinceLevelProvider());
        register(new PlayerEntityFlyingSpeedProvider());
        register(new PlayerEntityFoodLevelProvider());
        register(new PlayerEntityIsFlyingProvider());
        register(new PlayerEntitySaturationProvider());
        register(new PlayerEntityWalkingSpeedProvider());
    }

    private void registerItemEntityData() {
        register(ItemEntityBridge.class, Keys.DESPAWN_DELAY,
                ItemEntityBridge::bridge$getDespawnDelay,
                (accessor, value) -> accessor.bridge$setDespawnDelay(accessor.bridge$getPickupDelay(), false));

        register(ItemEntityBridge.class, Keys.PICKUP_DELAY,
                ItemEntityBridge::bridge$getPickupDelay,
                (accessor, value) -> accessor.bridge$setPickupDelay(accessor.bridge$getPickupDelay(), false));

        // TODO above setters do not set the value
        register(ItemEntityBridge.class, Keys.DESPAWN_DELAY,
                ItemEntityBridge::bridge$getDespawnDelay,
                (e, d) -> e.bridge$setDespawnDelay(d, false));
        register(ItemEntityBridge.class, Keys.PICKUP_DELAY,
                ItemEntityBridge::bridge$getPickupDelay,
                (e, d) -> e.bridge$setPickupDelay(d, false));

        register(ItemEntityBridge.class, Keys.INFINITE_DESPAWN_DELAY,
                ItemEntityBridge::bridge$infiniteDespawnDelay,
                (accessor, value) -> accessor.bridge$setDespawnDelay(accessor.bridge$getPickupDelay(), value));

        register(ItemEntityBridge.class, Keys.INFINITE_DESPAWN_DELAY,
                ItemEntityBridge::bridge$infinitePickupDelay,
                (accessor, value) -> accessor.bridge$setPickupDelay(accessor.bridge$getPickupDelay(), value));

        register(ItemEntity.class, Keys.ITEM_STACK_SNAPSHOT,
                (accessor) -> ItemStackUtil.snapshotOf(accessor.getItem()),
                (accessor, value) -> accessor.setItem(ItemStackUtil.fromSnapshotToNative(value)));
    }

    private void registerUserData() {
        register(new UserFirstDatePlayedProvider());
        register(new UserLastDatePlayedProvider());
    }
}
