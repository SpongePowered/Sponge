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
package org.spongepowered.common.world;

import com.flowpowered.math.vector.Vector3i;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import org.spongepowered.common.interfaces.world.IMixinWorld;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;

public final class WorldUtil {

    public static final Vector3i BLOCK_MIN = new Vector3i(-30000000, 0, -30000000);
    public static final Vector3i BIOME_MIN = new Vector3i(BLOCK_MIN.getX(), 0, BLOCK_MIN.getZ());
    public static final Vector3i BLOCK_MAX = new Vector3i(30000000, 256, 30000000).sub(Vector3i.ONE);
    public static final Vector3i BIOME_MAX = new Vector3i(BLOCK_MAX.getX(), 256, BLOCK_MAX.getZ());
    public static final Vector3i BIOME_SIZE = BIOME_MAX.sub(BIOME_MIN).add(Vector3i.ONE);
    public static final Vector3i BLOCK_SIZE = BLOCK_MAX.sub(BLOCK_MIN).add(Vector3i.ONE);

    private WorldUtil() {}

  @SuppressWarnings("unchecked")
  public static <T extends World> T asNative(org.spongepowered.api.world.World world) {
    return (T) world;
  }

  public static WorldServer asNative(IMixinWorldServer world) {
    return (WorldServer) world;
  }

  public static org.spongepowered.api.world.World fromNative(World world) {
    return (org.spongepowered.api.world.World) world;
  }

  public static org.spongepowered.api.world.World fromNative(IMixinWorld world) {
    return (org.spongepowered.api.world.World) world;
  }

  public static org.spongepowered.api.world.World fromNative(IMixinWorldServer world) {
    return (org.spongepowered.api.world.World) world;
  }
}
