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
import com.mojang.authlib.properties.Property;
import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.entity.item.EnderCrystalEntity;
import net.minecraft.entity.item.FallingBlockEntity;
import net.minecraft.entity.item.FireworkRocketEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.item.ItemFrameEntity;
import net.minecraft.entity.item.TNTEntity;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.entity.item.minecart.MinecartCommandBlockEntity;
import net.minecraft.entity.merchant.villager.AbstractVillagerEntity;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.merchant.villager.VillagerProfession;
import net.minecraft.entity.monster.AbstractRaiderEntity;
import net.minecraft.entity.monster.BlazeEntity;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.entity.monster.EndermanEntity;
import net.minecraft.entity.monster.GuardianEntity;
import net.minecraft.entity.monster.PatrollerEntity;
import net.minecraft.entity.monster.PhantomEntity;
import net.minecraft.entity.monster.PillagerEntity;
import net.minecraft.entity.monster.SlimeEntity;
import net.minecraft.entity.monster.SpellcastingIllagerEntity;
import net.minecraft.entity.monster.SpiderEntity;
import net.minecraft.entity.monster.VexEntity;
import net.minecraft.entity.monster.ZombieVillagerEntity;
import net.minecraft.entity.passive.BatEntity;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.entity.passive.DolphinEntity;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.MooshroomEntity;
import net.minecraft.entity.passive.PandaEntity;
import net.minecraft.entity.passive.ParrotEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.passive.PolarBearEntity;
import net.minecraft.entity.passive.RabbitEntity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.passive.TurtleEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.passive.fish.PufferfishEntity;
import net.minecraft.entity.passive.fish.TropicalFishEntity;
import net.minecraft.entity.passive.horse.AbstractChestedHorseEntity;
import net.minecraft.entity.passive.horse.AbstractHorseEntity;
import net.minecraft.entity.passive.horse.LlamaEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.entity.projectile.DamagingProjectileEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.entity.projectile.PotionEntity;
import net.minecraft.entity.villager.IVillagerType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MerchantOffer;
import net.minecraft.item.MerchantOffers;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.PotionUtils;
import net.minecraft.stats.Stat;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.GameType;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.boss.ServerBossBar;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.CatType;
import org.spongepowered.api.data.type.DyeColor;
import org.spongepowered.api.data.type.FoxType;
import org.spongepowered.api.data.type.HandPreference;
import org.spongepowered.api.data.type.MooshroomType;
import org.spongepowered.api.data.type.PandaGene;
import org.spongepowered.api.data.type.ParrotType;
import org.spongepowered.api.data.type.PickupRule;
import org.spongepowered.api.data.type.Profession;
import org.spongepowered.api.data.type.RabbitType;
import org.spongepowered.api.data.type.SpellType;
import org.spongepowered.api.data.type.VillagerType;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.entity.explosive.EnderCrystal;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.animal.Sheep;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.item.merchant.TradeOffer;
import org.spongepowered.api.profile.property.ProfileProperty;
import org.spongepowered.api.projectile.source.ProjectileSource;
import org.spongepowered.api.statistic.Statistic;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Color;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.util.rotation.Rotation;
import org.spongepowered.common.accessor.entity.AreaEffectCloudEntityAccessor;
import org.spongepowered.common.accessor.entity.EntityAccessor;
import org.spongepowered.common.accessor.entity.LivingEntityAccessor;
import org.spongepowered.common.accessor.entity.MobEntityAccessor;
import org.spongepowered.common.accessor.entity.boss.WitherEntityAccessor;
import org.spongepowered.common.accessor.entity.effect.LightningBoltEntityAccessor;
import org.spongepowered.common.accessor.entity.item.ArmorStandEntityAccessor;
import org.spongepowered.common.accessor.entity.item.BoatEntityAccessor;
import org.spongepowered.common.accessor.entity.item.ExperienceOrbEntityAccessor;
import org.spongepowered.common.accessor.entity.item.EyeOfEnderEntityAccessor;
import org.spongepowered.common.accessor.entity.item.FallingBlockEntityAccessor;
import org.spongepowered.common.accessor.entity.item.FireworkRocketEntityAccessor;
import org.spongepowered.common.accessor.entity.item.HangingEntityAccessor;
import org.spongepowered.common.accessor.entity.item.TNTEntityAccessor;
import org.spongepowered.common.accessor.entity.item.minecart.FurnaceMinecartEntityAccessor;
import org.spongepowered.common.accessor.entity.monster.AbstractRaiderEntityAccessor;
import org.spongepowered.common.accessor.entity.monster.BlazeEntityAccessor;
import org.spongepowered.common.accessor.entity.monster.CreeperEntityAccessor;
import org.spongepowered.common.accessor.entity.monster.EndermanEntityAccessor;
import org.spongepowered.common.accessor.entity.monster.EvokerEntityAccessor;
import org.spongepowered.common.accessor.entity.monster.GuardianEntityAccessor;
import org.spongepowered.common.accessor.entity.monster.PatrollerEntityAccessor;
import org.spongepowered.common.accessor.entity.monster.PhantomEntityAccessor;
import org.spongepowered.common.accessor.entity.monster.PillagerEntityAccessor;
import org.spongepowered.common.accessor.entity.monster.RavagerEntityAccessor;
import org.spongepowered.common.accessor.entity.monster.SpellcastingIllagerEntityAccessor;
import org.spongepowered.common.accessor.entity.monster.VexEntityAccessor;
import org.spongepowered.common.accessor.entity.monster.VindicatorEntityAccessor;
import org.spongepowered.common.accessor.entity.monster.ZombiePigmanEntityAccessor;
import org.spongepowered.common.accessor.entity.passive.AnimalEntityAccessor;
import org.spongepowered.common.accessor.entity.passive.FoxEntityAccessor;
import org.spongepowered.common.accessor.entity.passive.MooshroomEntityAccessor;
import org.spongepowered.common.accessor.entity.passive.OcelotEntityAccessor;
import org.spongepowered.common.accessor.entity.passive.TurtleEntityAccessor;
import org.spongepowered.common.accessor.entity.passive.fish.PufferfishEntityAccessor;
import org.spongepowered.common.accessor.entity.passive.horse.LlamaEntityAccessor;
import org.spongepowered.common.accessor.entity.passive.horse.TraderLlamaEntityAccessor;
import org.spongepowered.common.accessor.entity.projectile.AbstractArrowEntityAccessor;
import org.spongepowered.common.accessor.entity.projectile.ShulkerBulletEntityAccessor;
import org.spongepowered.common.bridge.LocationTargetingBridge;
import org.spongepowered.common.bridge.data.InvulnerableTrackedBridge;
import org.spongepowered.common.bridge.entity.AggressiveEntityBridge;
import org.spongepowered.common.bridge.entity.EntityBridge;
import org.spongepowered.common.bridge.entity.item.ItemEntityBridge;
import org.spongepowered.common.bridge.entity.monster.ShulkerEntityBridge;
import org.spongepowered.common.bridge.entity.passive.horse.AbstractHorseEntityBridge;
import org.spongepowered.common.bridge.entity.player.BedLocationHolder;
import org.spongepowered.common.bridge.entity.player.PlayerEntityBridge;
import org.spongepowered.common.bridge.entity.player.ServerPlayerEntityBridge;
import org.spongepowered.common.bridge.explosives.ExplosiveBridge;
import org.spongepowered.common.bridge.stats.StatisticsManagerBridge;
import org.spongepowered.common.bridge.world.raid.RaidBridge;
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
import org.spongepowered.common.data.provider.entity.base.EntityFireTicksProvider;
import org.spongepowered.common.data.provider.entity.base.EntityInvulnerabilityTicksProvider;
import org.spongepowered.common.data.provider.entity.horse.AbstractHorseEntityTamedOwnerProvider;
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
import org.spongepowered.common.data.provider.generic.GrieferCanGriefProvider;
import org.spongepowered.common.data.provider.util.FireworkUtils;
import org.spongepowered.common.data.type.SpongeCatType;
import org.spongepowered.common.data.type.SpongeLlamaType;
import org.spongepowered.common.data.type.SpongeParrotType;
import org.spongepowered.common.data.type.SpongeRabbitType;
import org.spongepowered.common.data.util.PotionEffectHelper;
import org.spongepowered.common.entity.living.human.HumanEntity;
import org.spongepowered.common.item.util.ItemStackUtil;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.VecHelper;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EntityDataProviders extends DataProviderRegistryBuilder {

    public EntityDataProviders(DataProviderRegistry registry) {
        super(registry);
    }

    @Override
    public void register() {
        register(new ArrowEntityPotionEffectsProvider());
        register(new BoatEntityWoodTypeProvider());
        register(BoatEntityAccessor.class, Keys.IS_IN_WATER, b -> b.accessor$getStatus() == BoatEntity.Status.IN_WATER);
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
        registerRavager();
        registerRaider();
        registerPanda();
        registerFox();
        registerTurtle();
        registerCat();

        register(ExperienceOrbEntityAccessor.class, Keys.EXPERIENCE, ExperienceOrbEntityAccessor::accessor$getXpValue, ExperienceOrbEntityAccessor::accessor$setXpValue);
        register(HangingEntityAccessor.class, Keys.DIRECTION,
                e -> e.accessor$facingDirection() == null ? Direction.NONE : Constants.DirectionFunctions.getFor(e.accessor$facingDirection()),
                (e, v) -> e.accessor$updateFacingWithBoundingBox(Constants.DirectionFunctions.getFor(v)));

        register(CatEntity.class, Keys.DYE_COLOR,
                e -> (DyeColor) (Object) e.getCollarColor(),
                (e, c) -> e.setCollarColor(((net.minecraft.item.DyeColor) (Object) c)));

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
        register(LlamaEntity.class, Keys.LLAMA_TYPE, e -> {
            int type = e.getVariant();
            return Sponge.getRegistry().getCatalogRegistry().getAllOf(SpongeLlamaType.class)
                    .filter(t -> ((SpongeLlamaType)t).getMetadata() == type)
                    .findFirst().orElse(null);
        }, (e, v) -> e.setVariant(((SpongeLlamaType)v).getMetadata()));

        register(TameableEntity.class, Keys.IS_TAMED, TameableEntity::isTamed, TameableEntity::setTamed);
        register(new TameableEntityTamedOwnerProvider());

        register(ItemFrameEntity.class, Keys.ITEM_STACK_SNAPSHOT,
                e -> ItemStackUtil.snapshotOf(e.getDisplayedItem()), (e, i) -> e.setDisplayedItem(ItemStackUtil.fromSnapshotToNative(i)));

        register(ItemFrameEntity.class, Keys.ROTATION,
                e -> Rotation.fromDegrees(e.getRotation() * 45).get(), (e, r) -> e.setItemRotation(r.getAngle() / 45));

        register(ShulkerBulletEntityAccessor.class, Keys.DIRECTION,
                e -> e.accessor$getDirection() == null ? Direction.NONE : Constants.DirectionFunctions.getFor(e.accessor$getDirection()),
                (e, d) -> e.accessor$setDirection(Constants.DirectionFunctions.getFor(d)));

        register(ShulkerEntityBridge.class, Keys.DIRECTION, ShulkerEntityBridge::bridge$getDirection, ShulkerEntityBridge::bridge$setDirection);
        register(new ShulkerEntityDyeColorProvider());

        register(HumanEntity.class, Keys.SKIN, e -> (ProfileProperty) e.getSkinProperty(), (e, p) -> e.setSkinProperty((Property) p));
        register(ServerPlayerEntity.class, Keys.SKIN, e -> (ProfileProperty) e.getGameProfile().getProperties().get(ProfileProperty.TEXTURES).iterator().next());

        register(Entity.class, Keys.PASSENGERS,
                e -> e.getPassengers().stream().map(org.spongepowered.api.entity.Entity.class::cast).collect(Collectors.toList()),
                (e, p) -> {
                    e.getPassengers().clear();
                    p.forEach(p1 -> e.getPassengers().add((Entity)p1));
                });

        register(BedLocationHolder.class, Keys.RESPAWN_LOCATIONS, BedLocationHolder::bridge$getBedlocations, BedLocationHolder::bridge$setBedLocations);
        register(LocationTargetingBridge.class, Keys.TARGET_LOCATION, LocationTargetingBridge::bridge$getTargetedLocation, LocationTargetingBridge::bridge$setTargetedLocation);
        register(PatrollerEntity.class, Keys.TARGET_POSITION, e -> VecHelper.toVector3i(e.getPatrolTarget()), (e, pos) -> e.setPatrolTarget(VecHelper.toBlockPos(pos)));
        register(EnderCrystalEntity.class, Keys.TARGET_POSITION, e -> VecHelper.toVector3i(e.getBeamTarget()), (e, pos) -> e.setBeamTarget(VecHelper.toBlockPos(pos)));
        register(TurtleEntityAccessor.class, Keys.TARGET_POSITION, e -> VecHelper.toVector3i(e.accessor$getTravelPos()), (e, pos) -> e.accessor$setTravelPos(VecHelper.toBlockPos(pos)));

        register(ShulkerBulletEntityAccessor.class, Keys.TARGET_ENTITY,
                e -> (org.spongepowered.api.entity.Entity) e.accessor$getTarget(),
                (e, te) -> e.accessor$setTarget((Entity)te));
        register(FishingBobberEntity.class, Keys.TARGET_ENTITY,
                e -> (org.spongepowered.api.entity.Entity) e.caughtEntity,
                (e, te) -> e.caughtEntity = (Entity)te);

        register(VillagerEntity.class, Keys.PROFESSION,
                e -> (Profession) e.getVillagerData().getProfession(),
                (e, p) -> e.setVillagerData(e.getVillagerData().withProfession((VillagerProfession) p)));
        register(ZombieVillagerEntity.class, Keys.PROFESSION,
                e -> (Profession) e.getVillagerData().getProfession(),
                (e, p) -> e.func_213792_a(e.getVillagerData().withProfession((VillagerProfession) p)));

        register(VillagerEntity.class, Keys.PROFESSION_LEVEL,
                e -> e.getVillagerData().getLevel(),
                (e, level) -> e.setVillagerData(e.getVillagerData().withLevel(level)));
        register(ZombieVillagerEntity.class, Keys.PROFESSION_LEVEL,
                e -> e.getVillagerData().getLevel(),
                (e, level) -> e.func_213792_a(e.getVillagerData().withLevel(level)));


        register(VillagerEntity.class, Keys.VILLAGER_TYPE,
                e -> (VillagerType) e.getVillagerData().getType(),
                (e, type) -> e.setVillagerData(e.getVillagerData().withType((IVillagerType) type)));
        register(ZombieVillagerEntity.class, Keys.VILLAGER_TYPE,
                e -> (VillagerType) e.getVillagerData().getType(),
                (e, level) -> e.func_213792_a(e.getVillagerData().withType((IVillagerType) level)));


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
                e -> PotionUtils.getEffectsFromStack(e.getItem()).stream().map(PotionEffect.class::cast).collect(Collectors.toList()),
                (e, effects) -> {
                    e.getItem().removeChildTag(Constants.Item.CUSTOM_POTION_EFFECTS);
                    PotionUtils.appendEffects(e.getItem(), effects.stream().map(EffectInstance.class::cast).collect(Collectors.toList()));
                });

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
        registerDoubleFloat(LivingEntityAccessor.class, Keys.LAST_DAMAGE_RECEIVED,
                LivingEntityAccessor::accessor$getLastDamage, LivingEntityAccessor::accessor$setLastDamage);

        register(Entity.class, Keys.VEHICLE,
                e -> ((org.spongepowered.api.entity.Entity) e.getRidingEntity()),
                (e, vehicle) -> e.startRiding((Entity) vehicle, true));
        register(Entity.class, Keys.BASE_VEHICLE,
                e -> ((org.spongepowered.api.entity.Entity) e.getLowestRidingEntity()));

        register(new DamagingProjectileEntityAccelerationProvider());


        // Fish variant is: size | pattern << 8 | bodyColor << 16 | patternColor << 24
        register(TropicalFishEntity.class, Keys.DYE_COLOR,
                fish -> (DyeColor) (Object) net.minecraft.item.DyeColor.byId((fish.getVariant() >> 16) & 0xFF),
                (fish, dyecolor) -> {
                    int dyeId = ((net.minecraft.item.DyeColor) (Object) dyecolor).getId() << 16;
                    fish.setVariant(fish.getVariant() & 0xFF00FFFF | dyeId << 16);
                });
        register(TropicalFishEntity.class, Keys.PATTERN_COLOR,
                fish -> (DyeColor) (Object) net.minecraft.item.DyeColor.byId((fish.getVariant() >> 24) & 0xFF),
                (fish, dyecolor) -> {
                    int dyeId = ((net.minecraft.item.DyeColor) (Object) dyecolor).getId() << 24;
                    fish.setVariant(fish.getVariant() & 0x00FFFFFF | dyeId << 24);
                });

        registerDoubleFloat(Entity.class, Keys.BASE_SIZE, Entity::getWidth);
        registerDoubleFloat(Entity.class, Keys.HEIGHT, Entity::getHeight);

        register(SlimeEntity.class, Keys.SCALE, e -> (double) (0.255F * (float) e.getSlimeSize()));
        register(PufferfishEntity.class, Keys.SCALE, e -> (double) PufferfishEntityAccessor.accessor$getPuffSize(e.getPuffState()));
        register(PhantomEntity.class, Keys.SCALE, e -> (double) (e.getWidth() + (0.2F * (float)e.getPhantomSize())) / e.getWidth());
        register(LivingEntity.class, Keys.SCALE, e -> (double) e.getRenderScale());
        register(Entity.class, Keys.SCALE, e -> 1d);

        register(GuardianEntity.class, Keys.BEAM_TARGET_ENTITY, e -> (Living) e.getTargetedEntity(), (e, t) -> ((GuardianEntityAccessor)e).accessor$setTargetedEntity(((LivingEntity) t).getEntityId()));

        register(WitherEntityAccessor.class, Keys.BOSS_BAR, e -> (ServerBossBar) e.accessor$getBossInfo());
        register(WitherEntity.class, Keys.TARGET_ENTITIES,
                e -> Stream.of(e.getWatchedTargetId(0), e.getWatchedTargetId(1), e.getWatchedTargetId(2))
                        .map(id -> e.getEntityWorld().getEntityByID(id))
                        // TODO filter null?                    .filter(Objects::nonNull)
                        .map(org.spongepowered.api.entity.Entity.class::cast)
                        .collect(Collectors.toList()),
                (e, targets) -> {
                    for (int i = 0; i < targets.size(); i++) {
                        if (i > 2) { // only 3 heads
                            break;
                        }
                        Entity target = (Entity) targets.get(i);
                        e.updateWatchedTargetId(i, target == null ? 0 : target.getEntityId());
                    }
                });

        register(EnderDragonEntity.class, Keys.HEALING_CRYSTAL, e -> (EnderCrystal) e.closestEnderCrystal, (e, c) -> e.closestEnderCrystal = (EnderCrystalEntity) c);

        register(new EnderCrystalEntityHealthProvider());

        register(AnimalEntityAccessor.class, Keys.BREEDER, AnimalEntityAccessor::accessor$getPlayerInLove, AnimalEntityAccessor::accessor$setPlayerInLove);

        register(new GrieferCanGriefProvider());

        register(TraderLlamaEntityAccessor.class, Keys.DESPAWN_DELAY, TraderLlamaEntityAccessor::accessor$getDespawnDelay, TraderLlamaEntityAccessor::accessor$setDespawnDelay);
        register(LlamaEntity.class, Keys.STRENGTH, LlamaEntity::getStrength, (l, s) -> ((LlamaEntityAccessor)l).accessor$setStrength(s));

        register(ChickenEntity.class, Keys.EGG_TIME, e -> e.timeUntilNextEgg, (e, t) -> e.timeUntilNextEgg = t);

        register(LightningBoltEntityAccessor.class, Keys.DESPAWN_DELAY, LightningBoltEntityAccessor::accessor$getBoltLivingTime, LightningBoltEntityAccessor::accessor$setBoltLivingTime);
        register(LightningBoltEntityAccessor.class, Keys.IS_EFFECT_ONLY, LightningBoltEntityAccessor::accessor$getEffectOnly);

        register(FurnaceMinecartEntityAccessor.class, Keys.FUEL, FurnaceMinecartEntityAccessor::accessor$getFuel, FurnaceMinecartEntityAccessor::accessor$setFuel);

        register(DolphinEntity.class, Keys.HAS_FISH, DolphinEntity::hasGotFish, DolphinEntity::setGotFish);
        register(DolphinEntity.class, Keys.SKIN_MOISTURE, DolphinEntity::getMoistness, DolphinEntity::setMoistness);

        register(WolfEntity.class, Keys.IS_BEGGING_FOR_FOOD, WolfEntity::isBegging, WolfEntity::setBegging);

        register(SpiderEntity.class, Keys.IS_CLIMBING, SpiderEntity::isBesideClimbableBlock, SpiderEntity::setBesideClimbableBlock);

        register(BatEntity.class, Keys.IS_SLEEPING, BatEntity::getIsBatHanging, BatEntity::setIsBatHanging);
        register(FoxEntity.class, Keys.IS_SLEEPING, FoxEntity::isSleeping, (f, isSleeping) -> ((FoxEntityAccessor) f).accessor$setSleeping(isSleeping));
        register(PlayerEntity.class, Keys.IS_SLEEPING, PlayerEntity::isSleeping);
        register(PlayerEntity.class, Keys.IS_SLEEPING_IGNORED, PlayerEntity::isSleeping);

        register(PolarBearEntity.class, Keys.IS_STANDING, PolarBearEntity::isStanding, PolarBearEntity::setStanding);

        register(AbstractVillagerEntity.class, Keys.IS_TRADING, AbstractVillagerEntity::hasCustomer);

        register(MobEntity.class, Keys.LEASH_HOLDER,
                e -> ((org.spongepowered.api.entity.Entity) e.getLeashHolder()),
                (e, holder) -> e.setLeashHolder((Entity) holder, true));

        register(VexEntity.class, Keys.LIFE_TICKS, e -> ((VexEntityAccessor)e).accessor$getLimitedLifeTicks(), VexEntity::setLimitedLife);

        register(MooshroomEntity.class, Keys.MOOSHROOM_TYPE,
                e -> ((MooshroomType) (Object) e.getMooshroomType()),
                (e, t) -> ((MooshroomEntityAccessor)e).accessor$setMooshroomType((MooshroomEntity.Type) (Object) t));

        register(PhantomEntityAccessor.class, Keys.PHANTOM_PHASE, PhantomEntityAccessor::accessor$getAttackPhase, PhantomEntityAccessor::accessor$setAttackPhase);
        register(PhantomEntity.class, Keys.SIZE, PhantomEntity::getPhantomSize, PhantomEntity::setPhantomSize);

        register(AbstractArrowEntity.class, Keys.SHOOTER, e -> (ProjectileSource) e.getShooter(), (e, s) -> e.setShooter((Entity) s)); // TODO other ProjectileSources
        register(DamagingProjectileEntity.class, Keys.SHOOTER, e -> (ProjectileSource) e.shootingEntity, (e, s) -> e.shootingEntity = (LivingEntity) s); // TODO other ProjectileSources

        register(EnderCrystalEntity.class, Keys.SHOW_BOTTOM, EnderCrystalEntity::shouldShowBottom, EnderCrystalEntity::setShowBottom);

        register(EyeOfEnderEntityAccessor.class, Keys.WILL_SHATTER,
                e -> !e.accessor$getShatterOrDrop(),
                (e, willShatter) -> e.accessor$setShatterOrDrop(!willShatter));
    }

    private void registerCat() {
        // register(CatEntity.class, Keys.IS_BEGGING_FOR_FOOD, ); //TODO
        // register(CatEntity.class, Keys.IS_HISSING, ); //TODO
        // register(CatEntity.class, Keys.IS_LYING_DOWN, ); //TODO
        // register(CatEntity.class, Keys.IS_PURRING, ); // TODO
        // register(CatEntity.class, Keys.IS_RELAXED, ); // TODO
        register(OcelotEntityAccessor.class, Keys.IS_TRUSTING, OcelotEntityAccessor::accessor$isTrusting, OcelotEntityAccessor::accessor$setTrusting);
    }

    private void registerTurtle() {
        register(TurtleEntity.class, Keys.HAS_EGG, TurtleEntity::hasEgg, (t, hasEgg) -> ((TurtleEntityAccessor) t).accessor$setHasEgg(hasEgg));
        register(TurtleEntity.class, Keys.HOME_POSITION, t -> VecHelper.toVector3i(t.getHomePosition()), (t, h) -> t.setHome(VecHelper.toBlockPos(h)));
        register(TurtleEntityAccessor.class, Keys.IS_GOING_HOME, TurtleEntityAccessor::accessor$isGoingHome, TurtleEntityAccessor::accessor$setGoingHome);
        register(TurtleEntity.class, Keys.IS_LAYING_EGG, TurtleEntity::isDigging, (t, isLayingEgg) -> ((TurtleEntityAccessor) t).accessor$setDigging(isLayingEgg));
        register(TurtleEntityAccessor.class, Keys.IS_TRAVELING, TurtleEntityAccessor::accessor$isTravelling, TurtleEntityAccessor::accessor$setTravelling);
    }

    private void registerFox() {
        register(FoxEntity.class, Keys.FIRST_TRUSTED,
                f -> f.getDataManager().get(FoxEntityAccessor.accessor$getTrustedUuidMain()).orElse(null),
                (f, u) -> f.getDataManager().set(FoxEntityAccessor.accessor$getTrustedUuidMain(), Optional.ofNullable(u)));
        register(FoxEntity.class, Keys.SECOND_TRUSTED,
                f -> f.getDataManager().get(FoxEntityAccessor.accessor$getTrustedUuidSecondary()).orElse(null),
                (f, u) -> f.getDataManager().set(FoxEntityAccessor.accessor$getTrustedUuidSecondary(), Optional.ofNullable(u)));
        register(FoxEntity.class, Keys.FOX_TYPE,
                e -> (FoxType)(Object) e.getVariantType(),
                (e, v) -> ((FoxEntityAccessor) e).accessor$setVariantType((FoxEntity.Type) (Object) v));
        register(FoxEntity.class, Keys.IS_CROUCHING, FoxEntity::isCrouching, FoxEntity::setCrouching);
        register(FoxEntity.class, Keys.IS_FACEPLANTED, FoxEntity::isStuck, (f, isFaceplanted) -> ((FoxEntityAccessor) f).accessor$setStuck(isFaceplanted));
        register(FoxEntity.class, Keys.IS_INTERESTED, FoxEntity::func_213467_eg, FoxEntity::func_213502_u);
// TODO        register(FoxEntity.class, Keys.IS_DEFENDING, FoxEntity::, FoxEntity::);
        register(FoxEntity.class, Keys.IS_POUNCING, FoxEntity::func_213480_dY, FoxEntity::func_213461_s);

    }

    private void registerPanda() {
//        register(PandaEntity.class, Keys.EATING_TIME, getter, setter); // TODO
        register(PandaEntity.class, Keys.HIDDEN_GENE,
                p -> ((PandaGene) (Object) p.getHiddenGene()),
                (p, g) -> p.setHiddenGene((PandaEntity.Type) (Object) g));
        register(PandaEntity.class, Keys.KNOWN_GENE,
                p -> ((PandaGene) (Object) p.getMainGene()),
                (p, g) -> p.setMainGene((PandaEntity.Type) (Object) g));

//        register(PandaEntity.class, Keys.IS_EATING, getter, setter); // TODO
//        register(PandaEntity.class, Keys.IS_FRIGHTENED, getter, setter); // TODO
        register(PandaEntity.class, Keys.IS_LYING_ON_BACK, PandaEntity::func_213567_dY, PandaEntity::func_213542_s);
        register(PandaEntity.class, Keys.IS_ROLLING_AROUND, PandaEntity::func_213564_eh, PandaEntity::func_213576_v);
//        register(PandaEntity.class, Keys.IS_SNEEZING, PandaEntity::, PandaEntity::); // TODO
//        register(PandaEntity.class, Keys.IS_UNHAPPY, PandaEntity::, PandaEntity::); // TODO
        register(PandaEntity.class, Keys.SNEEZING_TIME, PandaEntity::func_213585_ee, PandaEntity::func_213562_s);
//        register(PandaEntity.class, Keys.UNHAPPY_TIME, PandaEntity::, PandaEntity::); // TODO
    }

    private void registerRavager() {
        register(RavagerEntityAccessor.class, Keys.ATTACK_TIME, RavagerEntityAccessor::accessor$getAttackTick, RavagerEntityAccessor::accessor$setAttackTick);
        register(RavagerEntityAccessor.class, Keys.ROARING_TIME, RavagerEntityAccessor::accessor$getRoarTick, RavagerEntityAccessor::accessor$setRoarTick);
        register(RavagerEntityAccessor.class, Keys.STUNNED_TIME, RavagerEntityAccessor::accessor$getStunTick, RavagerEntityAccessor::accessor$setStunTick);
        register(RavagerEntityAccessor.class, Keys.IS_IMMOBILIZED, r -> r.accessor$getAttackTick() > 0 || r.accessor$getStunTick() > 0 || r.accessor$getRoarTick() > 0);
        register(RavagerEntityAccessor.class, Keys.IS_ROARING, r -> r.accessor$getRoarTick() > 0);
        register(RavagerEntityAccessor.class, Keys.IS_STUNNED, r -> r.accessor$getStunTick() > 0);
    }

    private void registerRaider() {
        register(AbstractRaiderEntity.class, Keys.CAN_JOIN_RAID, AbstractRaiderEntity::func_213658_ej, AbstractRaiderEntity::func_213644_t);
        register(SpellcastingIllagerEntityAccessor.class, Keys.CASTING_TIME, SpellcastingIllagerEntityAccessor::accessor$getSpellTicks, SpellcastingIllagerEntityAccessor::accessor$setSpellTicks);
        register(SpellcastingIllagerEntity.class, Keys.CURRENT_SPELL,
                e -> (SpellType) (Object)((SpellcastingIllagerEntityAccessor)e).accessor$getSpellType(),
                (e, s) -> e.setSpellType((SpellcastingIllagerEntity.SpellType) (Object) s));

        register(AbstractRaiderEntity.class, Keys.IS_CELEBRATING, r -> r.getDataManager().get(AbstractRaiderEntityAccessor.accessor$getDataIsCelebrating()), AbstractRaiderEntity::func_213655_u);
        register(PillagerEntity.class, Keys.IS_CHARGING_CROSSBOW, r -> r.getDataManager().get(PillagerEntityAccessor.accessor$getDataChargingState()), PillagerEntity::setCharging);
        register(PatrollerEntityAccessor.class, Keys.IS_PATROLLING, PatrollerEntityAccessor::accessor$getPatrolling, PatrollerEntityAccessor::accessor$setPatrolling);
        register(AbstractRaiderEntity.class, Keys.IS_LEADER, AbstractRaiderEntity::isLeader, AbstractRaiderEntity::setLeader);
        register(AbstractRaiderEntity.class, Keys.RAID_WAVE, e -> ((RaidBridge)e.getRaid()).bridge$getWaves().get(e.func_213642_em()));

        register(EvokerEntityAccessor.class, Keys.WOLOLO_TARGET,
                e -> (Sheep) e.accessor$getWololoTarget(),
                (e, sheep) -> e.accessor$setWololoTarget((SheepEntity) sheep));
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
                (accessor) -> (double)((AbstractArrowEntityAccessor) accessor).accessor$getKnockbackStrength(),
                (accessor, value) -> accessor.setKnockbackStrength((int) Math.round(value)));

        register(AbstractArrowEntity.class, Keys.ATTACK_DAMAGE, AbstractArrowEntity::getDamage, AbstractArrowEntity::setDamage);
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
        register(AbstractMinecartEntity.class, Keys.IS_ON_RAIL, e -> {
            BlockPos position = e.getPosition();
            if (e.getEntityWorld().getBlockState(position).isIn(BlockTags.RAILS)) {
                return true;
            }
            BlockPos posBelow = position.add(0, -1, 0);
            return e.getEntityWorld().getBlockState(posBelow).isIn(BlockTags.RAILS);
        });
    }

    private void registerFallingBlockEntityData() {
        register(FallingBlockEntityAccessor.class, Keys.BLOCK_STATE,
                (accessor) -> (BlockState) accessor.accessor$getFallTile(),
                (accessor, value) -> accessor.accessor$setFallTile((net.minecraft.block.BlockState) value));

        register(FallingBlockEntityAccessor.class, Keys.CAN_PLACE_AS_BLOCK,
                (accessor) -> !accessor.accessor$getDontSetBlock(),
                (accessor, value) -> accessor.accessor$setDontSetAsBlock(!value));

        register(FallingBlockEntity.class, Keys.CAN_DROP_AS_ITEM,
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
                FallingBlockEntityAccessor::accessor$getFallHurtMax,
                FallingBlockEntityAccessor::accessor$setFallHurtMax);
    }

    private void registerArmorStandEntityData() {
        register(ArmorStandEntity.class, Keys.HAS_ARMS,
                ArmorStandEntity::getShowArms,
                (accessor, value) -> ((ArmorStandEntityAccessor) accessor).accessor$setShowArms(value));

        register(ArmorStandEntity.class, Keys.HAS_BASE_PLATE,
                (accessor) -> !accessor.hasNoBasePlate(),
                (accessor, value) -> ((ArmorStandEntityAccessor) accessor).accessor$setNoBasePlate(!value));

        register(ArmorStandEntity.class, Keys.HAS_MARKER,
                ArmorStandEntity::hasMarker,
                (accessor, value) -> ((ArmorStandEntityAccessor) accessor).accessor$setMarker(value));

        register(ArmorStandEntity.class, Keys.IS_SMALL,
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
        register(AbstractHorseEntityBridge.class, Keys.IS_SADDLED, AbstractHorseEntityBridge::bridge$isSaddled, AbstractHorseEntityBridge::bridge$setSaddled);
        register(AbstractHorseEntity.class, Keys.IS_TAMED, AbstractHorseEntity::isTame, AbstractHorseEntity::setHorseTamed);
        register(new AbstractHorseEntityTamedOwnerProvider());

        register(AbstractChestedHorseEntity.class, Keys.HAS_CHEST, AbstractChestedHorseEntity::hasChest, AbstractChestedHorseEntity::setChested);
    }

    private void registerHorseEntityData() {
        throw new UnsupportedOperationException("Implement me");
//        register(new HorseEntityHorseColorProvider());
//        register(new HorseEntityHorseStyleProvider());
    }

    private void registerAreaEffectCloudEntityData() {

        register(Entity.class, Keys.AGE,
                (accessor) -> accessor.ticksExisted,
                (accessor, value) -> accessor.ticksExisted = value);

        register(AreaEffectCloudEntityAccessor.class, Keys.REAPPLICATION_DELAY,
                AreaEffectCloudEntityAccessor::accessor$getReapplicationDelay,
                AreaEffectCloudEntityAccessor::accessor$setReapplicationDelay);

        register(AreaEffectCloudEntity.class, Keys.COLOR,
                (accessor) -> Color.ofRgb(accessor.getColor()),
                (accessor, value) -> accessor.setColor(value.getRgb()));

        register(AreaEffectCloudEntityAccessor.class, Keys.DURATION_ON_USE,
                AreaEffectCloudEntityAccessor::accessor$getDurationOnUse,
                AreaEffectCloudEntityAccessor::accessor$setDurationOnUse);

        register(AreaEffectCloudEntity.class, Keys.DURATION,
                AreaEffectCloudEntity::getDuration,
                AreaEffectCloudEntity::setDuration);

        registerDoubleFloat(AreaEffectCloudEntity.class, Keys.RADIUS_ON_USE,
                (accessor) ->  ((AreaEffectCloudEntityAccessor) accessor).accessor$getRadiusOnUse(),
                AreaEffectCloudEntity::setRadiusOnUse);

        registerDoubleFloat(AreaEffectCloudEntity.class, Keys.RADIUS_PER_TICK,
                (accessor) ->  ((AreaEffectCloudEntityAccessor) accessor).accessor$getRadiusPerTick(),
                AreaEffectCloudEntity::setRadiusPerTick);

        registerDoubleFloat(AreaEffectCloudEntity.class, Keys. RADIUS, AreaEffectCloudEntity::getRadius, AreaEffectCloudEntity::setRadius);

        register(AreaEffectCloudEntity.class, Keys.WAIT_TIME,
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

        register(AgeableEntity.class, Keys.BABY_TICKS,
                ageableEntity -> ageableEntity.getGrowingAge() < 0 ? -ageableEntity.getGrowingAge() : null,
                (ageableEntity1, age) -> ageableEntity1.setGrowingAge(-age));

        register(AgeableEntity.class, Keys.BREEDING_COOLDOWN,
                ageableEntity -> ageableEntity.getGrowingAge() >= 0 ? ageableEntity.getGrowingAge() : null,
                (ageableEntity1, age) -> ageableEntity1.setGrowingAge(age));

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
        registerDoubleFloat(LivingEntity.class, Keys.ABSORPTION, LivingEntity::getAbsorptionAmount, LivingEntity::setAbsorptionAmount);

        registerDoubleFloat(LivingEntity.class, Keys.FALL_DISTANCE,
                (accessor) -> accessor.fallDistance,
                (accessor, value) -> accessor.fallDistance = value);

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

        register(LivingEntity.class, Keys.WALKING_SPEED,
                e -> e.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getValue(),
                (e, s) -> e.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(s));
    }

    private void registerExplosiveData() {
        register(ExplosiveBridge.class, Keys.EXPLOSION_RADIUS,
                (accessor) -> accessor.bridge$getExplosionRadius().orElse(null),
                ExplosiveBridge::bridge$setExplosionRadius);
        register(TNTEntity.class, Keys.DETONATOR,
                e -> (Living) e.getTntPlacedBy(),
                (e, l) -> ((TNTEntityAccessor) e).accessor$setTntPlacedBy((LivingEntity) l));
        register(new FusedExplosiveFuseDurationProvider());
        register(new FusedExplosiveTicksRemainingProvider());
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

        registerDoubleFloat(Entity.class, Keys.EYE_HEIGHT, Entity::getEyeHeight);
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
        register(new EntityFireTicksProvider());
        register(new EntityInvulnerabilityTicksProvider());
        register(InvulnerableTrackedBridge.class, Keys.INVULNERABLE, InvulnerableTrackedBridge::bridge$getIsInvulnerable, InvulnerableTrackedBridge::bridge$setInvulnerable);

        register(Entity.class, Keys.TAGS, Entity::getTags, (e, tags) -> {
            e.getTags().clear();
            e.getTags().addAll(tags);
        });
    }

    private void registerSheepEntityData() {
        register(SheepEntity.class, Keys.DYE_COLOR,
                (accessor) -> (DyeColor) (Object) accessor.getFleeceColor(),
                (accessor, value) -> accessor.setFleeceColor((net.minecraft.item.DyeColor) (Object) value));

        register(SheepEntity.class, Keys.IS_SHEARED, SheepEntity::getSheared, SheepEntity::setSheared);
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

        register(ServerPlayerEntity.class, Keys.SPECTATOR_TARGET,
                p -> (org.spongepowered.api.entity.Entity) p.getSpectatingEntity(),
                (p, target) -> p.setSpectatingEntity((Entity) target));
    }

    private void registerItemEntityData() {

        register(ItemEntityBridge.class, Keys.DESPAWN_DELAY,
                ItemEntityBridge::bridge$getDespawnDelay,
                (e, d) -> e.bridge$setDespawnDelay(d, false));
        register(ItemEntityBridge.class, Keys.PICKUP_DELAY,
                ItemEntityBridge::bridge$getPickupDelay,
                (e, d) -> e.bridge$setPickupDelay(d, false));

        register(ItemEntityBridge.class, Keys.INFINITE_DESPAWN_DELAY,
                ItemEntityBridge::bridge$infiniteDespawnDelay,
                (accessor, value) -> accessor.bridge$setDespawnDelay(accessor.bridge$getDespawnDelay(), value));

        register(ItemEntityBridge.class, Keys.INFINITE_PICKUP_DELAY,
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
