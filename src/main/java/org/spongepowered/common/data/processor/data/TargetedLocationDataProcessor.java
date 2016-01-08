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

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityEnderEye;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.BlockPos;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.ImmutableTargetedLocationData;
import org.spongepowered.api.data.manipulator.mutable.TargetedLocationData;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.data.processor.common.AbstractEntitySingleDataProcessor;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.interfaces.IMixinEntityPlayer;
import org.spongepowered.common.util.VecHelper;

import java.util.Optional;

public class TargetedLocationDataProcessor extends AbstractEntitySingleDataProcessor<net.minecraft.entity.Entity, Location<World>, Value<Location<World>>, TargetedLocationData, ImmutableTargetedLocationData> {

    public TargetedLocationDataProcessor() {
        super(net.minecraft.entity.Entity.class, Keys.TARGETED_LOCATION);
    }

    @Override
    protected boolean set(Entity dataHolder, Location<World> value) {
        if(dataHolder instanceof EntityPlayer) {
            ((IMixinEntityPlayer) dataHolder).setCompassLocation(VecHelper.toBlockPos(value));
            return true;
        } else if(dataHolder instanceof EntityLiving) {
            final EntityLiving entity = (EntityLiving) dataHolder;
            entity.getNavigator().clearPathEntity();
            entity.getNavigator().setPath(new PathEntity(new PathPoint[]{new PathPoint(value.getBlockX(), value.getBlockY(), value.getBlockZ())}), entity.getAIMoveSpeed());
            return true;
        } else if(dataHolder instanceof EntityEnderEye) {
            // TODO
        }
        return false;
    }

    @Override
    protected Optional<Location<World>> getVal(Entity dataHolder) {
        if(dataHolder instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) dataHolder;
            IMixinEntityPlayer mixinPlayer = (IMixinEntityPlayer) dataHolder;
            BlockPos pos = mixinPlayer.getCompassLocation();
            if(pos == null) pos = player.getEntityWorld().getSpawnPoint();
            return Optional.of(new Location<>((World) player.getEntityWorld(), pos.getX(), pos.getY(), pos.getZ()));
        } else if(dataHolder instanceof EntityLiving) {
            final EntityLiving entity = (EntityLiving) dataHolder;
            final PathPoint path = entity.getNavigator().getPath().getFinalPathPoint();
            if(path == null) {
                return Optional.empty();
            }
            return Optional.of(new Location<>((World) entity.getEntityWorld(), path.xCoord, path.yCoord, path.zCoord));
        } else if(dataHolder instanceof EntityEnderEye) {
            // TODO
        }
        return Optional.empty();
    }

    @Override
    protected ImmutableValue<Location<World>> constructImmutableValue(Location<World> value) {
        return new ImmutableSpongeValue<>(Keys.TARGETED_LOCATION, value);
    }

    @Override
    public boolean supports(DataHolder dataHolder) {
        return dataHolder instanceof EntityEnderEye || dataHolder instanceof EntityLiving;
    }

    @Override
    protected TargetedLocationData createManipulator() {
        //return new SpongeTargetedLocationData(new Location<World>()); what there?
        return null;
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
