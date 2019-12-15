package org.spongepowered.common.data.provider.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.entity.item.FallingBlockEntity;
import net.minecraft.entity.item.minecart.MinecartCommandBlockEntity;
import net.minecraft.entity.monster.BlazeEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import org.spongepowered.api.data.Keys;
import org.spongepowered.common.bridge.entity.AggressiveEntityBridge;
import org.spongepowered.common.data.provider.DataProviderRegistry;
import org.spongepowered.common.data.provider.DataProviderRegistryBuilder;
import org.spongepowered.common.data.provider.entity.armorstand.ArmorStandEntityBodyRotationsProvider;
import org.spongepowered.common.data.provider.entity.armorstand.ArmorStandEntityPlacingDisabledProvider;
import org.spongepowered.common.data.provider.entity.armorstand.ArmorStandEntityRotationProvider;
import org.spongepowered.common.data.provider.entity.armorstand.ArmorStandEntityTakingDisabledProvider;
import org.spongepowered.common.data.provider.entity.base.EntityDisplayNameProvider;
import org.spongepowered.common.data.provider.entity.base.EntityInvisibleProvider;
import org.spongepowered.common.data.provider.entity.base.EntityInvulnerabilityTicksProvider;
import org.spongepowered.common.data.provider.entity.player.PlayerEntityDominantHandProvider;
import org.spongepowered.common.data.provider.entity.player.PlayerEntityExhaustionProvider;
import org.spongepowered.common.data.provider.entity.player.PlayerEntityFlyingSpeedProvider;
import org.spongepowered.common.data.provider.entity.player.PlayerEntityFoodLevelProvider;
import org.spongepowered.common.data.provider.entity.player.PlayerEntitySaturationProvider;
import org.spongepowered.common.data.provider.entity.player.PlayerEntityWalkingSpeedProvider;
import org.spongepowered.common.data.provider.entity.vanishable.VanishableEntityVanishIgnoresCollisionProvider;
import org.spongepowered.common.data.provider.entity.vanishable.VanishableEntityVanishPreventsTargetingProvider;
import org.spongepowered.common.data.provider.entity.vanishable.VanishableEntityVanishProvider;
import org.spongepowered.common.mixin.accessor.entity.item.ArmorStandEntityAccessor;
import org.spongepowered.common.mixin.accessor.entity.item.FallingBlockEntityAccessor;
import org.spongepowered.common.mixin.accessor.entity.monster.BlazeEntityAccessor;
import org.spongepowered.common.mixin.accessor.tileentity.CommandBlockLogicAccessor;
import org.spongepowered.common.util.VecHelper;

public class EntityDataProviders extends DataProviderRegistryBuilder {

    public EntityDataProviders(DataProviderRegistry registry) {
        super(registry);
    }

    @Override
    protected void register() {
        register(new ArrowEntityPotionEffectsProvider());
        register(new BoatEntityWoodTypeProvider());
        register(new MobEntityDominantHandProvider());

        // Other
        register(BlazeEntity.class, BlazeEntityAccessor.class, Keys.IS_AFLAME,
                BlazeEntity::isBurning,
                BlazeEntityAccessor::accessor$setOnFire);

        register(AbstractArrowEntity.class, Keys.CRITICAL_HIT,
                AbstractArrowEntity::getIsCritical,
                AbstractArrowEntity::setIsCritical);

        register(AggressiveEntityBridge.class, Keys.IS_ANGRY,
                AggressiveEntityBridge::bridge$isAngry,
                AggressiveEntityBridge::bridge$setAngry);

        register(PigEntity.class, Keys.IS_SADDLED,
                PigEntity::getSaddled,
                PigEntity::setSaddled);

        registerFallingBlockEntityData();
        registerArmorStandEntityData();
        registerMinecartCommandBlockEntityData();
        registerSheepEntityData();
        registerVanishableEntityData();
        registerPlayerEntityData();
        registerEntityData();
    }

    private void registerFallingBlockEntityData() {
        register(FallingBlockEntityAccessor.class, Keys.BLOCK_STATE,
                FallingBlockEntityAccessor::accessor$getFallTile,
                FallingBlockEntityAccessor::accessor$setFallTile, identity());

        register(FallingBlockEntityAccessor.class, Keys.CAN_PLACE_AS_BLOCK,
                (accessor) -> !accessor.accessor$getDontSetBlock(),
                (accessor, value) -> accessor.accessor$setDontSetAsBlock(!value));

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
        register(ArmorStandEntity.class, ArmorStandEntityAccessor.class, Keys.ARMOR_STAND_HAS_ARMS,
                ArmorStandEntity::getShowArms,
                ArmorStandEntityAccessor::accessor$setShowArms);

        register(ArmorStandEntity.class, ArmorStandEntityAccessor.class, Keys.ARMOR_STAND_HAS_BASE_PLATE,
                (accessor) -> !accessor.hasNoBasePlate(),
                (accessor, value) -> accessor.accessor$setNoBasePlate(!value));

        register(ArmorStandEntity.class, ArmorStandEntityAccessor.class, Keys.ARMOR_STAND_HAS_MARKER,
                ArmorStandEntity::hasMarker,
                ArmorStandEntityAccessor::accessor$setMarker);

        register(ArmorStandEntity.class, ArmorStandEntityAccessor.class, Keys.ARMOR_STAND_IS_SMALL,
                ArmorStandEntity::isSmall,
                ArmorStandEntityAccessor::accessor$setSmall);

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
        register(MinecartCommandBlockEntity.class, Keys.COMMAND,
                (accessor) -> accessor.getCommandBlockLogic().getCommand(),
                (accessor, value) -> ((CommandBlockLogicAccessor) accessor.getCommandBlockLogic()).accessor$setCommandStored(value));

        register(MinecartCommandBlockEntity.class, Keys.SUCCESS_COUNT,
                (accessor) -> accessor.getCommandBlockLogic().getSuccessCount(),
                (accessor, value) -> ((CommandBlockLogicAccessor) accessor.getCommandBlockLogic()).accessor$setSuccessCount(value));

        register(MinecartCommandBlockEntity.class, Keys.TRACKS_OUTPUT,
                (accessor) -> accessor.getCommandBlockLogic().shouldReceiveErrors(),
                (accessor, value) -> accessor.getCommandBlockLogic().setTrackOutput(value));

        register(new MinecartCommandBlockEntityLastCommandOutputProvider());
    }

    private void registerEntityData() {
        register(Entity.class, Keys.VELOCITY,
                (accessor) -> VecHelper.toVector3d(accessor.getMotion()),
                (accessor, value) -> accessor.setMotion(VecHelper.toVec3d(value)));

        register(Entity.class, Keys.IS_WET, Entity::isWet);
        register(Entity.class, Keys.IS_SNEAKING, Entity::isSneaking, Entity::setSneaking);
        register(Entity.class, Keys.IS_SPRINTING, Entity::isSprinting, Entity::setSprinting);
        register(Entity.class, Keys.ON_GROUND, entity -> entity.onGround);

        register(new EntityDisplayNameProvider());
        register(new EntityInvisibleProvider());
        register(new EntityInvulnerabilityTicksProvider());
    }

    private void registerSheepEntityData() {
        register(SheepEntity.class, Keys.DYE_COLOR, SheepEntity::getFleeceColor, SheepEntity::setFleeceColor, identity());
        register(SheepEntity.class, Keys.IS_SHEARED, SheepEntity::getSheared, SheepEntity::setSheared);
    }

    private void registerVanishableEntityData() {
        register(new VanishableEntityVanishProvider());
        register(new VanishableEntityVanishIgnoresCollisionProvider());
        register(new VanishableEntityVanishPreventsTargetingProvider());
    }

    private void registerPlayerEntityData() {
        register(new PlayerEntityDominantHandProvider());
        register(new PlayerEntityExhaustionProvider());
        register(new PlayerEntityFlyingSpeedProvider());
        register(new PlayerEntityFoodLevelProvider());
        register(new PlayerEntitySaturationProvider());
        register(new PlayerEntityWalkingSpeedProvider());
    }
}
