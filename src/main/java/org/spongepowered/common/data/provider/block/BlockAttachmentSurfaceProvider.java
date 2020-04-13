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
package org.spongepowered.common.data.provider.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFaceBlock;
import net.minecraft.state.properties.AttachFace;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.Surface;
import org.spongepowered.common.data.provider.BlockStateDataProvider;

import java.util.Optional;

public class BlockAttachmentSurfaceProvider extends BlockStateDataProvider<Surface> {

    public BlockAttachmentSurfaceProvider() {
        super(Keys.ATTACHMENT_SURFACE, HorizontalFaceBlock.class);
    }

    @Override
    protected Optional<Surface> getFrom(BlockState dataHolder) {
        return Optional.of((Surface) (Object) dataHolder.get(HorizontalFaceBlock.FACE));
    }

    @Override
    protected Optional<BlockState> set(BlockState dataHolder, Surface value) {
        return Optional.ofNullable(dataHolder.with(HorizontalFaceBlock.FACE, (AttachFace)(Object) value));
    }
}
