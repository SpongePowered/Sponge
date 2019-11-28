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
import net.minecraft.nbt.LongNBT;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.StringNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.projectile.Projectile;
import org.spongepowered.api.entity.projectile.source.BlockProjectileSource;
import org.spongepowered.api.entity.projectile.source.ProjectileSource;
import org.spongepowered.common.util.VecHelper;

import java.util.UUID;

public class ProjectileSourceSerializer {

    // TODO Revisit when persistent containers are implemented.
    // Note: ProjectileSource itself does not extend DataContainer

    public static NBTBase toNbt(ProjectileSource projectileSource) {
        if (projectileSource instanceof Entity) {
            return new StringNBT(((Entity) projectileSource).getUniqueId().toString());
        }
        if (projectileSource instanceof BlockProjectileSource) {
            return new LongNBT(VecHelper.toBlockPos(((BlockProjectileSource) projectileSource).getLocation()).func_177986_g());
        }
        return null;
    }

    public static ProjectileSource fromNbt(World worldObj, NBTBase tag) {
        if (tag instanceof StringNBT) {
            Entity entity =
                    ((org.spongepowered.api.world.World) worldObj).getEntity(UUID.fromString(((StringNBT) tag).func_150285_a_())).orElse(null);
            if (entity instanceof ProjectileSource) {
                return (ProjectileSource) entity;
            }
        }
        if (tag instanceof LongNBT) {
            BlockPos pos = BlockPos.func_177969_a(((LongNBT) tag).func_150291_c());
            if (worldObj.func_175667_e(pos)) {
                TileEntity tileEntity = worldObj.func_175625_s(pos);
                if (tileEntity instanceof ProjectileSource) {
                    return (ProjectileSource) tileEntity;
                }
            }
        }
        return ProjectileSource.UNKNOWN;
    }

    public static void writeSourceToNbt(CompoundNBT compound, ProjectileSource projectileSource, net.minecraft.entity.Entity potentialEntity) {
        if (projectileSource == null && potentialEntity instanceof ProjectileSource) {
            projectileSource = (ProjectileSource) potentialEntity;
        }
        NBTBase projectileNbt = toNbt(projectileSource);
        if (projectileNbt != null) {
            compound.func_74782_a("projectileSource", projectileNbt);
        }
    }

    public static void readSourceFromNbt(CompoundNBT compound, Projectile projectile) {
        if (compound.func_74764_b("projectileSource")) {
            projectile.setShooter(fromNbt((World) projectile.getWorld(), compound.func_74781_a("projectileSource")));
        }
    }
}
