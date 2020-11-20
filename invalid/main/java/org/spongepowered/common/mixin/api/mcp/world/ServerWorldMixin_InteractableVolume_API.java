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
package org.spongepowered.common.mixin.invalid.api.mcp.world;

import net.minecraft.world.server.ServerWorld;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.volume.game.InteractableVolume;
import org.spongepowered.asm.mixin.Mixin;

import java.time.Duration;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin_InteractableVolume_API implements InteractableVolume {


    // InteractableVolume
    @Override
    public boolean hitBlock(int x, int y, int z, Direction side, GameProfile profile) {
        return false;
    }

    @Override
    public boolean interactBlock(int x, int y, int z, Direction side, GameProfile profile) {
        return false;
    }

    @Override
    public boolean interactBlockWith(int x, int y, int z, ItemStack itemStack, Direction side, GameProfile profile) {
        return false;
    }

    @Override
    public boolean placeBlock(int x, int y, int z, org.spongepowered.api.block.BlockState block, Direction side, GameProfile profile) {
        return false;
    }

    @Override
    public boolean digBlock(int x, int y, int z, GameProfile profile) {
        return false;
    }

    @Override
    public boolean digBlockWith(int x, int y, int z, ItemStack itemStack, GameProfile profile) {
        return false;
    }

    @Override
    public Duration getBlockDigTimeWith(int x, int y, int z, ItemStack itemStack, GameProfile profile) {
        return null;
    }


}
