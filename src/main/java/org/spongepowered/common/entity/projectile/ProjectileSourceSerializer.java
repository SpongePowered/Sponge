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
package org.spongepowered.common.entity.projectile;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.LongNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.projectile.Projectile;
import org.spongepowered.api.projectile.source.BlockProjectileSource;
import org.spongepowered.api.projectile.source.ProjectileSource;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.VecHelper;

import java.util.UUID;

import javax.annotation.Nullable;

public class ProjectileSourceSerializer {

    // TODO Revisit when persistent containers are implemented.
    // Note: ProjectileSource itself does not extend DataContainer

    public static INBT toNbt(final ProjectileSource projectileSource) {
        if (projectileSource instanceof Entity) {
            return StringNBT.valueOf(((Entity) projectileSource).getUniqueId().toString());
        }
        else if (projectileSource instanceof BlockProjectileSource) {
            return LongNBT.valueOf(VecHelper.toBlockPos(((BlockProjectileSource) projectileSource).getLocation()).toLong());
        }
        return null;
    }

    public static ProjectileSource fromNbt(final World worldObj, final INBT tag) {
        if (tag instanceof StringNBT) {
            final Entity entity =
                    ((org.spongepowered.api.world.World) worldObj).getEntity(UUID.fromString(tag.getString())).orElse(null);
            if (entity instanceof ProjectileSource) {
                return (ProjectileSource) entity;
            }
        }
        if (tag instanceof LongNBT) {
            final BlockPos pos = BlockPos.fromLong(((LongNBT) tag).getLong());
            if (worldObj.isBlockLoaded(pos)) {
                final TileEntity tileEntity = worldObj.getTileEntity(pos);
                if (tileEntity instanceof ProjectileSource) {
                    return (ProjectileSource) tileEntity;
                }
            }
        }
        return UnknownProjectileSource.UNKNOWN;
    }

    public static void writeSourceToNbt(final CompoundNBT compound, @Nullable ProjectileSource projectileSource, final net.minecraft.entity.Entity potentialEntity) {
        if (projectileSource == null && potentialEntity instanceof ProjectileSource) {
            projectileSource = (ProjectileSource) potentialEntity;
        }
        final INBT projectileNbt = toNbt(projectileSource);
        if (projectileNbt != null) {
            compound.put(Constants.Sponge.Entity.Projectile.PROJECTILE_SOURCE, projectileNbt);
        }
    }

    public static void writeSourceToNbt(final CompoundNBT compound, final ProjectileSource projectileSource, final UUID entityUid) {
        final INBT projectileNbt;
        if (projectileSource == null && entityUid != null) {
            projectileNbt = StringNBT.valueOf(entityUid.toString());
        } else {
            projectileNbt = toNbt(projectileSource);
        }
        if (projectileNbt != null) {
            compound.put(Constants.Sponge.Entity.Projectile.PROJECTILE_SOURCE, projectileNbt);
        }
    }

    public static void readSourceFromNbt(final CompoundNBT compound, final Projectile projectile) {
        if (compound.contains(Constants.Sponge.Entity.Projectile.PROJECTILE_SOURCE)) {
            projectile.offer(Keys.SHOOTER, fromNbt((World) projectile.getWorld(), compound.get(Constants.Sponge.Entity.Projectile.PROJECTILE_SOURCE)));
        }
    }
}
