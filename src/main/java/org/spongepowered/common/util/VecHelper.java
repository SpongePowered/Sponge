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
package org.spongepowered.common.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Rotations;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.api.util.AABB;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.common.world.server.SpongeServerLocation;
import org.spongepowered.math.vector.Vector2i;
import org.spongepowered.math.vector.Vector3i;

public final class VecHelper {

    // === Flow Vector3d --> BlockPos ===

    public static BlockPos toBlockPos(final org.spongepowered.math.vector.Vector3d vector) {
        if (vector == null) {
            return null;
        }
        return new BlockPos(vector.x(), vector.y(), vector.z());
    }

    // === Flow Vector3i --> BlockPos ===

    public static BlockPos toBlockPos(final org.spongepowered.math.vector.Vector3i vector) {
        if (vector == null) {
            return null;
        }
        return new BlockPos(vector.x(), vector.y(), vector.z());
    }

    // === SpongeAPI Location --> BlockPos ===
    public static BlockPos toBlockPos(final ServerLocation location) {
        if (location == null) {
            return null;
        }
        return ((SpongeServerLocation) location).asBlockPos();
    }
    // === MC BlockPos --> Flow Vector3i ==

    public static org.spongepowered.math.vector.Vector3i toVector3i(final BlockPos pos) {
        if (pos == null) {
            return null;
        }
        return new org.spongepowered.math.vector.Vector3i(pos.getX(), pos.getY(), pos.getZ());
    }

    // === MC BlockPos --> Flow Vector3d ==

    public static org.spongepowered.math.vector.Vector3d toVector3d(final BlockPos pos) {
        if (pos == null) {
            return null;
        }
        return new org.spongepowered.math.vector.Vector3d(pos.getX(), pos.getY(), pos.getZ());
    }
    
    // === Rotations --> Flow Vector ===

    public static org.spongepowered.math.vector.Vector3d toVector3d(final Rotations rotation) {
        if (rotation == null) {
            return null;
        }
        return new org.spongepowered.math.vector.Vector3d(rotation.getX(), rotation.getY(), rotation.getZ());
    }

    // === MC Vector3i --> Flow Vector3i ===

    public static org.spongepowered.math.vector.Vector3i toVector3i(final Vec3i vector) {
        if (vector == null) {
            return null;
        }
        return new org.spongepowered.math.vector.Vector3i(vector.getX(), vector.getY(), vector.getZ());
    }

    // === Flow Vector3i --> MC Vector3i ===

    public static Vec3i toVanillaVector3i(final org.spongepowered.math.vector.Vector3i vector) {
        if (vector == null) {
            return null;
        }
        return new Vec3i(vector.x(), vector.y(), vector.z());
    }

    // === Flow Vector3d --> MC Vector3d ===

    public static Vec3 toVanillaVector3d(final org.spongepowered.math.vector.Vector3d vector) {
        if (vector == null) {
            return null;
        }
        return new Vec3(vector.x(), vector.y(), vector.z());
    }

    // === MC BlockPos --> MC Vector3d

    public static Vec3 toVanillaVector3d(final BlockPos pos) {
        if (pos == null) {
            return null;
        }
        return new Vec3(pos.getX(), pos.getY(), pos.getZ());
    }

    // === MC ChunkPos ---> Flow Vector3i ===

    public static Vector3i toVector3i(final ChunkPos pos) {
        if (pos == null) {
            return null;
        }
        return new Vector3i(pos.x, 0, pos.z);
    }

    // === Flow Vector3i --> MC ChunkPos ===

    public static ChunkPos toChunkPos(final Vector3i vector) {
        if (vector == null) {
            return null;
        }
        return new ChunkPos(vector.x(), vector.x());
    }

    // === MC Vector3d --> flow Vector3d ==

    public static org.spongepowered.math.vector.Vector3d toVector3d(final Vec3 vector) {
        if (vector == null) {
            return null;
        }
        return new org.spongepowered.math.vector.Vector3d(vector.x, vector.y, vector.z);
    }

    // === Flow Vector3d --> MC Vector3d ==

    public static Vec3i toVector3i(final org.spongepowered.math.vector.Vector3d vector) {
        if (vector == null) {
            return null;
        }
        return new Vec3i(vector.x(), vector.y(), vector.z());
    }

    // === Flow Vector --> Rotations ===
    public static Rotations toRotation(final org.spongepowered.math.vector.Vector3d vector) {
        if (vector == null) {
            return null;
        }
        return new Rotations((float) vector.x(), (float) vector.y(), (float) vector.z());
    }

    public static boolean inBounds(final int x, final int y, final Vector2i min, final Vector2i max) {
        return x >= min.x() && x <= max.x() && y >= min.y() && y <= max.y();
    }

    public static boolean inBounds(final int x, final int y, final int z, final Vec3i min, final Vec3i max) {
        return x >= min.getX() && x <= max.getX() && y >= min.getY() && y <= max.getY() && z >= min.getZ() && z <= max.getZ();
    }

    public static boolean inBounds(final BlockPos pos, final org.spongepowered.math.vector.Vector3i min, final org.spongepowered.math.vector.Vector3i max) {
        return VecHelper.inBounds(pos.getX(), pos.getY(), pos.getZ(), min, max);
    }

    public static boolean inBounds(final org.spongepowered.math.vector.Vector3d pos, final org.spongepowered.math.vector.Vector3i min,
                                   final org.spongepowered.math.vector.Vector3i max) {
        return VecHelper.inBounds(pos.x(), pos.y(), pos.z(), min, max);
    }

    public static boolean inBounds(final double x, final double y, final double z, final org.spongepowered.math.vector.Vector3i min,
                                   final org.spongepowered.math.vector.Vector3i max) {
        return x >= min.x() && x <= max.x() && y >= min.y() && y <= max.y() && z >= min.z() && z <= max.z();
    }

    public static net.minecraft.world.phys.AABB toMinecraftAABB(final AABB box) {
        if (box == null) {
            return null;
        }
        final org.spongepowered.math.vector.Vector3d min = box.min();
        final org.spongepowered.math.vector.Vector3d max = box.max();
        return new net.minecraft.world.phys.AABB(
            min.x(), min.y(), min.z(),
            max.x(), max.y(), max.z()
        );
    }

    public static AABB toSpongeAABB(final net.minecraft.world.phys.AABB box) {
        if (box == null) {
            return null;
        }
        return new SpongeAABB(
            new org.spongepowered.math.vector.Vector3d(box.minX, box.minY, box.minZ),
            new org.spongepowered.math.vector.Vector3d(box.maxX, box.maxY, box.maxZ)
        );
    }

    public static CompoundTag toCompound(final org.spongepowered.math.vector.Vector3d vector) {
        final CompoundTag compound = new CompoundTag();
        compound.putDouble("x", vector.x());
        compound.putDouble("y", vector.y());
        compound.putDouble("z", vector.z());
        return compound;
    }

    public static org.spongepowered.math.vector.Vector3d fromCompound(final CompoundTag compound) {
        return new org.spongepowered.math.vector.Vector3d(compound.getDouble("x"), compound.getDouble("y"), compound.getDouble("z"));
    }

    private VecHelper() {
    }
}
