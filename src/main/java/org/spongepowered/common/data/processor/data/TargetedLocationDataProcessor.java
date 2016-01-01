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
package org.spongepowered.common.data.processor.data;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityEnderEye;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.BlockPos;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.ImmutableTargetedLocationData;
import org.spongepowered.api.data.manipulator.mutable.TargetedLocationData;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.data.manipulator.immutable.ImmutableSpongeTargetedLocationData;
import org.spongepowered.common.data.manipulator.mutable.SpongeTargetedLocationData;
import org.spongepowered.common.data.processor.common.AbstractSpongeDataProcessor;
import org.spongepowered.common.data.util.DataUtil;
import org.spongepowered.common.interfaces.IMixinEntityPlayer;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

public class TargetedLocationDataProcessor extends AbstractSpongeDataProcessor<TargetedLocationData, ImmutableTargetedLocationData> {

    @Override
    public boolean supports(DataHolder dataHolder) {
        return dataHolder instanceof EntityEnderEye || dataHolder instanceof EntityLiving;
    }

    @Override
    public Optional<TargetedLocationData> from(DataHolder dataHolder) {
        if(dataHolder instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) dataHolder;
            IMixinEntityPlayer mixinPlayer = (IMixinEntityPlayer) dataHolder;
            BlockPos pos = mixinPlayer.getCompassLocation();
            if(pos == null) pos = player.getEntityWorld().getSpawnPoint();
            final TargetedLocationData data = new SpongeTargetedLocationData(new Location<>((World) player.getEntityWorld(), pos.getX(), pos.getY(), pos.getZ()));
            return Optional.of(data);
        } else if(dataHolder instanceof EntityLiving) {
            final EntityLiving entity = (EntityLiving) dataHolder;
            final PathPoint path = entity.getNavigator().getPath().getFinalPathPoint();
            if(path == null) {
                return Optional.empty();
            }
            final TargetedLocationData data = new SpongeTargetedLocationData(new Location<>((World) entity.getEntityWorld(), path.xCoord, path.yCoord, path.zCoord));
            return Optional.of(data);
        } else if(dataHolder instanceof EntityEnderEye) {
            // TODO
        }
        return Optional.empty();
    }

    @Override
    public Optional<TargetedLocationData> createFrom(DataHolder dataHolder) {
        if (dataHolder instanceof EntityPlayer) {
            return from(dataHolder);
        } else if (dataHolder instanceof EntityLiving) {
            if (((EntityLiving) dataHolder).getNavigator().getPath() != null) {
                return from(dataHolder);
            } else {
                return Optional.empty();
            }
        } else if (dataHolder instanceof EntityEnderEye) {
            return from(dataHolder);
        }
        return Optional.empty();
        // just from method?
    }

    @Override
    public Optional<TargetedLocationData> fill(DataHolder dataHolder, TargetedLocationData manipulator, MergeFunction overlap) {
        if (supports(dataHolder)) {
            final TargetedLocationData data = from(dataHolder).orElse(null);
            final TargetedLocationData newData = checkNotNull(overlap.merge(checkNotNull(manipulator), data));
            final Location location = newData.target().get();
            return Optional.of(manipulator.set(Keys.TARGETED_LOCATION, location));
        }
        return Optional.empty();
    }

    @Override
    public Optional<TargetedLocationData> fill(DataContainer container, TargetedLocationData targetedLocationData) {
        final Location location = DataUtil.getData(container, Keys.TARGETED_LOCATION, Location.class);
        return Optional.of(targetedLocationData.set(Keys.TARGETED_LOCATION, location));
    }

    @Override
    public DataTransactionResult set(DataHolder dataHolder, TargetedLocationData manipulator, MergeFunction function) {
        return DataTransactionResult.failResult(manipulator.getValues());
    }

    @Override
    public Optional<ImmutableTargetedLocationData> with(Key<? extends BaseValue<?>> key, Object value, ImmutableTargetedLocationData immutable) {
        if (key == Keys.TARGETED_LOCATION) {
            return Optional.<ImmutableTargetedLocationData>of(new ImmutableSpongeTargetedLocationData(immutable.target().get()));
        }
        return Optional.empty();
    }

    @Override
    public DataTransactionResult remove(DataHolder dataHolder) {
        if (dataHolder instanceof EntityLiving) {
            final DataTransactionResult.Builder builder = DataTransactionResult.builder();
            final Optional<TargetedLocationData> optional = from(dataHolder);
            if (optional.isPresent()) {
                try {
                    EntityLiving living = (EntityLiving) dataHolder;
                    living.getNavigator().clearPathEntity();
                    return builder.replace(optional.get().getValues()).result(DataTransactionResult.Type.SUCCESS).build();
                } catch (Exception e) {
                    SpongeImpl.getLogger().error("There was an issue resetting the target location on an entity!", e);
                    return builder.result(DataTransactionResult.Type.ERROR).build();
                }
            } else {
                return builder.result(DataTransactionResult.Type.SUCCESS).build();
            }
        }
        return DataTransactionResult.failNoData();
    }
}
