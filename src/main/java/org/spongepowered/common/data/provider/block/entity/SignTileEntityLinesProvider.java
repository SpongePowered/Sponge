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
package org.spongepowered.common.data.provider.block.entity;

import net.minecraft.tileentity.SignTileEntity;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.text.Text;
import org.spongepowered.common.data.provider.GenericMutableDataProvider;
import org.spongepowered.common.text.SpongeTexts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class SignTileEntityLinesProvider extends GenericMutableDataProvider<SignTileEntity, List<Text>> {

    public SignTileEntityLinesProvider() {
        super(Keys.SIGN_LINES);
    }

    @Override
    protected boolean set(SignTileEntity dataHolder, List<Text> value) {
        for (int i = 0; i < dataHolder.signText.length; i++) {
            dataHolder.signText[i] = SpongeTexts.toComponent(i >= value.size() ? Text.empty() : value.get(i));
        }
        dataHolder.markDirty();
        // ((ServerWorld) dataHolder.getWorld()).getPlayerChunkMap().markBlockForUpdate(sign.getPos());
        return true;
    }

    @Override
    protected Optional<List<Text>> getFrom(SignTileEntity dataHolder) {
        final List<Text> lines = new ArrayList<>(dataHolder.signText.length);
        for (int i = 0; i < dataHolder.signText.length; i++) {
            lines.add(SpongeTexts.toText(dataHolder.signText[i]));
        }
        return Optional.of(lines);
    }

    @Override
    protected boolean delete(SignTileEntity dataHolder) {
        return this.set(dataHolder, Collections.emptyList());
    }
}
