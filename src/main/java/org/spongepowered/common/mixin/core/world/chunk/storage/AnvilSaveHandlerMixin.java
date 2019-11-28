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
package org.spongepowered.common.mixin.core.world.chunk.storage;

import net.minecraft.util.datafix.DataFixer;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraft.world.chunk.storage.AnvilSaveHandler;
import net.minecraft.world.chunk.storage.IChunkLoader;
import net.minecraft.world.storage.SaveHandler;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.common.bridge.world.storage.SaveHandlerBridge;

import java.io.File;

@NonnullByDefault
@Mixin(AnvilSaveHandler.class)
public abstract class AnvilSaveHandlerMixin extends SaveHandler {

    public AnvilSaveHandlerMixin(final File p_i46648_1_, final String p_i46648_2_, final boolean p_i46648_3_, final DataFixer p_i46648_4_) {
        super(p_i46648_1_, p_i46648_2_, p_i46648_3_, p_i46648_4_);
    }

    /**
     * @author blood - April 5th, 2015
     * @reason Use individual chunkloaders for multi-dimensions
     *
     * @param provider The world provider
     * @return The chunkloader
     */
    @Override
    @Overwrite
    public IChunkLoader func_75763_a(final WorldProvider provider) {
        // To workaround the issue of every world having a separate save handler
        // we won't be generating a DIMXX folder for chunk loaders since this name is already generated
        // for the world container with provider.getSaveFolder().
        // This allows users to remove our mod and maintain world compatibility.
        return new AnvilChunkLoader(((SaveHandlerBridge) this).bridge$getSpongeWorldDirectory(), this.field_186341_a);
    }
}
