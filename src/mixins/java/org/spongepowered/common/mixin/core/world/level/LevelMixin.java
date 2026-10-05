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
package org.spongepowered.common.mixin.core.world.level;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.TickingBlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.WritableLevelData;
import net.minecraft.world.phys.AABB;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.bridge.data.VanishableBridge;
import org.spongepowered.common.bridge.world.level.LevelBridge;
import org.spongepowered.common.data.persistence.NBTTranslator;
import org.spongepowered.common.entity.EntityDataUtil;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.DataUtil;
import org.spongepowered.math.vector.Vector3d;

import java.util.List;
import java.util.function.Predicate;

@Mixin(net.minecraft.world.level.Level.class)
public abstract class LevelMixin implements LevelBridge, LevelAccessor {

    // @formatter: off
    @Mutable @Shadow @Final private Holder<DimensionType> dimensionTypeRegistration;
    @Shadow protected float oRainLevel;
    @Shadow protected float rainLevel;
    @Shadow protected float oThunderLevel;
    @Shadow protected float thunderLevel;
    @Shadow @Final public RandomSource random;
    @Shadow @Final protected WritableLevelData levelData;
    @Shadow @Final protected List<TickingBlockEntity> blockEntityTickers;

    @Shadow public abstract LevelData shadow$getLevelData();
    @Shadow public abstract void shadow$updateSkyBrightness();
    @Shadow public abstract net.minecraft.resources.ResourceKey<Level> shadow$dimension();
    @Shadow public abstract DimensionType shadow$dimensionType();
    @Shadow public abstract LevelChunk shadow$getChunkAt(BlockPos p_175726_1_);
    @Shadow public abstract boolean shadow$isRaining();
    @Shadow public abstract net.minecraft.world.level.block.entity.@Nullable BlockEntity shadow$getBlockEntity(BlockPos p_175625_1_);
    @Shadow public abstract RegistryAccess shadow$registryAccess();
    //@Shadow protected abstract void shadow$postGameEventInRadius(@javax.annotation.Nullable net.minecraft.world.entity.Entity $$0, GameEvent $$1, BlockPos $$2, int $$3);
    // @formatter on



    @Override
    public boolean bridge$isFake() {
        return this.isClientSide();
    }

    @Override
    public void bridge$adjustDimensionLogic(final DimensionType dimensionType) {
        this.dimensionTypeRegistration = Holder.direct(dimensionType);

        // TODO Minecraft 1.16.4 - Re-create the WorldBorder due to new coordinate scale, send that updated packet to players
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E extends org.spongepowered.api.entity.Entity> E bridge$createEntity(
        final DataContainer dataContainer,
        final @Nullable Vector3d position,
        final @Nullable Predicate<Vector3d> positionCheck) throws IllegalArgumentException, IllegalStateException {

        final DataView updatedContainer = EntityDataUtil.upgradeEntityContainer(dataContainer);

        final Vector3d proposedPosition;
        if (position == null) {
            proposedPosition = DataUtil.getPosition3d(updatedContainer, Constants.Sponge.SNAPSHOT_WORLD_POSITION);
        } else {
            proposedPosition = position;
        }

        if (positionCheck != null && !positionCheck.test(proposedPosition)) {
            throw new IllegalArgumentException(String.format("Position (%.2f, %.2f, %.2f) is not a valid position in this context.",
                proposedPosition.x(),
                proposedPosition.y(),
                proposedPosition.z()));
        }

        final @Nullable Vector3d rotation;
        if (updatedContainer.contains(Constants.Entity.ROTATION)) {
            rotation = DataUtil.getPosition3d(updatedContainer, Constants.Entity.ROTATION);
        } else {
            rotation = null;
        }

        final @Nullable Vector3d scale;
        if (updatedContainer.contains(Constants.Entity.SCALE)) {
            scale = DataUtil.getPosition3d(updatedContainer, Constants.Entity.SCALE);
        } else {
            scale = null;
        }

        final CompoundTag entityData = updatedContainer.getView(Constants.Entity.V2.DATA)
            .map(NBTTranslator.INSTANCE::translate)
            .orElseThrow(() -> new IllegalArgumentException("Missing entity data!"));
        // mimicing Entity#restoreFrom
        entityData.remove("Dimension");

        final Entity createdEntity = this.bridge$createEntity(EntityDataUtil.entityType(entityData), proposedPosition, false);
        final var e = (net.minecraft.world.entity.Entity) createdEntity;
        e.load(TagValueInput.create(ProblemReporter.DISCARDING, this.shadow$registryAccess(), entityData));
        // position needs a reset
        e.snapTo(proposedPosition.x(), proposedPosition.y(), proposedPosition.z());

        if (rotation != null) {
            createdEntity.setRotation(rotation);
        }
        if (scale != null) {
            createdEntity.setScale(scale);
        }

        return (E) createdEntity;
    }

    @Inject(method = {
        "getEntities(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;)Ljava/util/List;",
        "getEntities(Lnet/minecraft/world/level/entity/EntityTypeTest;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;)Ljava/util/List;",
    }, at = @At("RETURN"))
    private void impl$IgnoreTargetingOfVanishedEntities(
        final @Coerce Object entityIn, final AABB aabb,
        final Predicate<?> filter, final CallbackInfoReturnable<List<net.minecraft.world.entity.Entity>> cir
    ) {
        if (this.bridge$isFake()) {
            return;
        }
        final List<net.minecraft.world.entity.Entity> entities = cir.getReturnValue();
        if (entities == null || entities.isEmpty()) {
            return;
        }
        entities.removeIf(entity -> {
            if (entity instanceof VanishableBridge vb) {
                final var state = vb.bridge$vanishState();
                return state.invisible() && state.untargetable();
            }
            return false;
        });
    }

}
