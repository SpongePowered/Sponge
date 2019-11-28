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

import com.flowpowered.math.vector.Vector2i;
import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Rotations;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import org.spongepowered.api.util.AABB;
import org.spongepowered.api.world.Location;
import org.spongepowered.common.bridge.world.LocationBridge;

public final class VecHelper {

    // === Flow Vector3d --> BlockPos ===

    public static BlockPos toBlockPos(Vector3d vector) {
        if (vector == null) {
            return null;
        }
        return new BlockPos(vector.getX(), vector.getY(), vector.getZ());
    }

    // === Flow Vector3i --> BlockPos ===

    public static BlockPos toBlockPos(Vector3i vector) {
        if (vector == null) {
            return null;
        }
        return new BlockPos(vector.getX(), vector.getY(), vector.getZ());
    }

    // === SpongeAPI Location --> BlockPos ===
    @SuppressWarnings("ConstantConditions")
    public static BlockPos toBlockPos(Location<?> location) {
        if (location == null) {
            return null;
        }
        return ((LocationBridge) (Object) location).bridge$getBlockPos();
    }
    // === MC BlockPos --> Flow Vector3i ==

    public static Vector3i toVector3i(BlockPos pos) {
        if (pos == null) {
            return null;
        }
        return new Vector3i(pos.getX(), pos.getY(), pos.getZ());
    }

    // === MC BlockPos --> Flow Vector3d ==

    public static Vector3d toVector3d(BlockPos pos) {
        if (pos == null) {
            return null;
        }
        return new Vector3d(pos.getX(), pos.getY(), pos.getZ());
    }
    
    // === Rotations --> Flow Vector ===

    public static Vector3d toVector3d(Rotations rotation) {
        if (rotation == null) {
            return null;
        }
        return new Vector3d(rotation.getX(), rotation.getY(), rotation.getZ());
    }

    // === MC Vec3i --> Flow Vector3i ===

    public static Vector3i toVector3i(Vec3i vector) {
        if (vector == null) {
            return null;
        }
        return new Vector3i(vector.getX(), vector.getY(), vector.getZ());
    }

    // === Flow Vector3i --> MC Vec3i ===

    public static Vec3i toVec3i(Vector3i vector) {
        if (vector == null) {
            return null;
        }
        return new Vec3i(vector.getX(), vector.getY(), vector.getZ());
    }

    // === Flow Vector3d --> MC Vec3d ===

    public static Vec3d toVec3d(Vector3d vector) {
        if (vector == null) {
            return null;
        }
        return new Vec3d(vector.getX(), vector.getY(), vector.getZ());
    }

    // === MC BlockPos --> MC Vec3d

    public static Vec3d toVec3d(BlockPos pos) {
        if (pos == null) {
            return null;
        }
        return new Vec3d(pos.getX(), pos.getY(), pos.getZ());
    }

    // === MC ChunkCoordIntPair ---> Flow Vector3i ===

    public static Vector3i toVec3i(ChunkPos pos) {
        if (pos == null) {
            return null;
        }
        return new Vector3i(pos.x, 0, pos.z);
    }

    // === Flow Vector3i --> MC ChunkCoordIntPair ===

    public static ChunkPos toChunkPos(Vector3i vector) {
        if (vector == null) {
            return null;
        }
        return new ChunkPos(vector.getX(), vector.getZ());
    }

    // === MC Vec3 --> flow Vector3d ==

    public static Vector3d toVector3d(Vec3d vector) {
        if (vector == null) {
            return null;
        }
        return new Vector3d(vector.x, vector.y, vector.z);
    }

    // === Flow Vector3d --> MC Vec3 ==

    public static Vec3i toVec3i(Vector3d vector) {
        if (vector == null) {
            return null;
        }
        return new Vec3i(vector.getX(), vector.getY(), vector.getZ());
    }

    // === Flow Vector --> Rotations ===
    public static Rotations toRotation(Vector3d vector) {
        if (vector == null) {
            return null;
        }
        return new Rotations((float) vector.getX(), (float) vector.getY(), (float) vector.getZ());
    }

    public static boolean inBounds(int x, int y, Vector2i min, Vector2i max) {
        return x >= min.getX() && x <= max.getX() && y >= min.getY() && y <= max.getY();
    }

    public static boolean inBounds(int x, int y, int z, Vector3i min, Vector3i max) {
        return x >= min.getX() && x <= max.getX() && y >= min.getY() && y <= max.getY() && z >= min.getZ() && z <= max.getZ();
    }

    public static boolean inBounds(Vector3d pos, Vector3i min, Vector3i max) {
        return inBounds(pos.getX(), pos.getY(), pos.getZ(), min, max);
    }

    public static boolean inBounds(double x, double y, double z, Vector3i min, Vector3i max) {
        return x >= min.getX() && x <= max.getX() && y >= min.getY() && y <= max.getY() && z >= min.getZ() && z <= max.getZ();
    }

    public static AxisAlignedBB toMinecraftAABB(AABB box) {
        if (box == null) {
            return null;
        }
        return new AxisAlignedBB(
            box.getMin().getX(), box.getMin().getY(), box.getMin().getZ(),
            box.getMax().getX(), box.getMax().getY(), box.getMax().getZ()
        );
    }

    public static AABB toSpongeAABB(AxisAlignedBB box) {
        if (box == null) {
            return null;
        }
        return new AABB(
            new Vector3d(box.minX, box.minY, box.minZ),
            new Vector3d(box.maxX, box.maxY, box.maxZ)
        );
    }
}
