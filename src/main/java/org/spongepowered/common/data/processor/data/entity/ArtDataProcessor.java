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
import org.spongepowered.api.data.type.Art;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.data.ImmutableDataCachingUtil;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeArtData;
import org.spongepowered.common.data.processor.common.AbstractEntitySingleDataProcessor;
import org.spongepowered.common.mixin.core.entity.EntityHangingAccessor;
import org.spongepowered.common.mixin.core.entity.EntityTrackerAccessor;
import org.spongepowered.common.mixin.core.entity.EntityTrackerEntryAccessor;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.data.value.mutable.SpongeValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ArtDataProcessor extends AbstractEntitySingleDataProcessor<PaintingEntity, Art, Value<Art>, ArtData, ImmutableArtData> {

    public ArtDataProcessor() {
        super(PaintingEntity.class, Keys.ART);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    protected boolean set(final PaintingEntity entity, final Art value) {
        if (!entity.field_70170_p.field_72995_K) {
            final PaintingEntity.EnumArt oldArt = entity.field_70522_e;
            entity.field_70522_e = (PaintingEntity.EnumArt) (Object) value;
            ((EntityHangingAccessor) entity).accessor$updateFacingWithBoundingBox(entity.field_174860_b);
            if (!entity.func_70518_d()) {
                entity.field_70522_e = oldArt;
                ((EntityHangingAccessor) entity).accessor$updateFacingWithBoundingBox(entity.field_174860_b);
                return false;
            }

            final EntityTracker paintingTracker = ((ServerWorld) entity.field_70170_p).func_73039_n();
            final EntityTrackerEntry paintingEntry = ((EntityTrackerAccessor) paintingTracker).accessor$getTrackedEntityTable().func_76041_a(entity.func_145782_y());
            final List<ServerPlayerEntity> playerMPs = new ArrayList<>();
            for (final ServerPlayerEntity player : ((EntityTrackerEntryAccessor) paintingEntry).accessor$getTrackingPlayers()) {
                final SDestroyEntitiesPacket packet = new SDestroyEntitiesPacket(entity.func_145782_y());
                player.field_71135_a.func_147359_a(packet);
                playerMPs.add(player);
            }
            for (final ServerPlayerEntity playerMP : playerMPs) {
                SpongeImpl.getGame().getScheduler().createTaskBuilder()
                        .delayTicks(SpongeImpl.getGlobalConfigAdapter().getConfig().getEntity().getPaintingRespawnDelaly())
                        .execute(() -> {
                            final SSpawnPaintingPacket packet = new SSpawnPaintingPacket(entity);
                            playerMP.field_71135_a.func_147359_a(packet);
                        })
                        .submit(SpongeImpl.getPlugin());
            }
            return true;
        }
        return true;
    }

    @Override
    protected Optional<Art> getVal(final PaintingEntity entity) {
        return Optional.of((Art) (Object) entity.field_70522_e);
    }

    @Override
    protected ImmutableValue<Art> constructImmutableValue(final Art value) {
        return ImmutableDataCachingUtil.getValue(ImmutableSpongeValue.class, this.key, value, Constants.Catalog.DEFAULT_ART);
    }

    @Override
    protected ArtData createManipulator() {
        return new SpongeArtData();
    }

    @Override
    protected Value<Art> constructValue(final Art actualValue) {
        return new SpongeValue<>(Keys.ART, Constants.Catalog.DEFAULT_ART, actualValue);
    }

    @Override
    public DataTransactionResult removeFrom(final ValueContainer<?> container) {
        return DataTransactionResult.failNoData();
    }
}
