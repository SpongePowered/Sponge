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
package org.spongepowered.common.data.processor.data.entity;

import net.minecraft.entity.EntityTracker;
import net.minecraft.entity.EntityTrackerEntry;
import net.minecraft.entity.item.PaintingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.server.SDestroyEntitiesPacket;
import net.minecraft.network.play.server.SSpawnPaintingPacket;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableArtData;
import org.spongepowered.api.data.manipulator.mutable.entity.ArtData;
import org.spongepowered.api.data.type.ArtType;
import org.spongepowered.api.data.value.Value.Immutable;
import org.spongepowered.api.data.value.Value.Mutable;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.data.ImmutableDataCachingUtil;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeArtData;
import org.spongepowered.common.data.processor.common.AbstractEntitySingleDataProcessor;
import org.spongepowered.common.mixin.accessor.entity.item.HangingEntityAccessor;
import org.spongepowered.common.mixin.invalid.accessor.entity.EntityTrackerAccessor;
import org.spongepowered.common.mixin.invalid.accessor.entity.EntityTrackerEntryAccessor;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.data.value.mutable.SpongeValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ArtDataProcessor extends AbstractEntitySingleDataProcessor<PaintingEntity, ArtType, Mutable<ArtType>, ArtData, ImmutableArtData> {

    public ArtDataProcessor() {
        super(PaintingEntity.class, Keys.ART);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    protected boolean set(final PaintingEntity entity, final ArtType value) {
        if (!entity.world.isRemote) {
            final PaintingEntity.EnumArt oldArt = entity.art;
            entity.art = (PaintingEntity.EnumArt) (Object) value;
            ((HangingEntityAccessor) entity).accessor$updateFacingWithBoundingBox(entity.facingDirection);
            if (!entity.onValidSurface()) {
                entity.art = oldArt;
                ((HangingEntityAccessor) entity).accessor$updateFacingWithBoundingBox(entity.facingDirection);
                return false;
            }

            final EntityTracker paintingTracker = ((ServerWorld) entity.world).getEntityTracker();
            final EntityTrackerEntry paintingEntry = ((EntityTrackerAccessor) paintingTracker).accessor$getTrackedEntityHashTable().lookup(entity.getEntityId());
            final List<ServerPlayerEntity> playerMPs = new ArrayList<>();
            for (final ServerPlayerEntity player : ((EntityTrackerEntryAccessor) paintingEntry).accessor$getTrackingPlayers()) {
                final SDestroyEntitiesPacket packet = new SDestroyEntitiesPacket(entity.getEntityId());
                player.connection.sendPacket(packet);
                playerMPs.add(player);
            }
            for (final ServerPlayerEntity playerMP : playerMPs) {
                SpongeImpl.getGame().getScheduler().createTaskBuilder()
                        .delayTicks(SpongeImpl.getGlobalConfigAdapter().getConfig().getEntity().getPaintingRespawnDelaly())
                        .execute(() -> {
                            final SSpawnPaintingPacket packet = new SSpawnPaintingPacket(entity);
                            playerMP.connection.sendPacket(packet);
                        })
                        .submit(SpongeImpl.getPlugin());
            }
            return true;
        }
        return true;
    }

    @Override
    protected Optional<ArtType> getVal(final PaintingEntity entity) {
        return Optional.of((ArtType) (Object) entity.art);
    }

    @Override
    protected Immutable<ArtType> constructImmutableValue(final ArtType value) {
        return ImmutableDataCachingUtil.getValue(ImmutableSpongeValue.class, this.key, value, Constants.Catalog.DEFAULT_ART);
    }

    @Override
    protected ArtData createManipulator() {
        return new SpongeArtData();
    }

    @Override
    protected Mutable<ArtType> constructValue(final ArtType actualValue) {
        return new SpongeValue<>(Keys.ART, Constants.Catalog.DEFAULT_ART, actualValue);
    }

    @Override
    public DataTransactionResult removeFrom(final ValueContainer<?> container) {
        return DataTransactionResult.failNoData();
    }
}
