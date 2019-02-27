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
package org.spongepowered.common.mixin.core.world;

import net.minecraft.profiler.Profiler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldServerMulti;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.SessionLockException;
import net.minecraft.world.storage.WorldInfo;
import net.minecraft.world.storage.WorldSavedDataStorage;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@NonnullByDefault
@Mixin(WorldServerMulti.class)
public abstract class MixinWorldServerMulti extends WorldServer {

    public MixinWorldServerMulti(MinecraftServer server, ISaveHandler handler, WorldSavedDataStorage storage,
        WorldInfo properties, DimensionType type, Profiler profiler) {
        super(server, handler, storage, properties, type, profiler);
    }

    /**
     * @author bloodmc
     * @reason Uses our own save handler instead of delegating to
     * the "parent" world since multi-world support changes the
     * structure.
     *
     * @throws SessionLockException An exception
     */
    @Override
    @Overwrite
    protected void saveLevel() throws SessionLockException {
        super.saveLevel();
    }

    /**
     * @author blood - February 6th, 2017
     * @reason Since we use a save handler per world, we can safely call super
     */
    @Overwrite
    public WorldServer func_212251_i__() {
        return super.func_212251_i__();
    }
}
