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
package org.spongepowered.common.data.builder.block.tileentity;

import net.minecraft.tileentity.SignTileEntity;
import net.minecraft.tileentity.TileEntity;
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.text.Text;
import org.spongepowered.common.text.SpongeTexts;

import java.util.List;
import java.util.Optional;

public class SpongeSignBuilder extends AbstractTileBuilder<Sign> {

    public SpongeSignBuilder() {
        super(Sign.class, 1);
    }

    @Override
    protected Optional<Sign> buildContent(DataView container) throws InvalidDataException {
        return super.buildContent(container).flatMap(sign1 -> {
            if (!container.contains(Keys.SIGN_LINES.getQuery())) {
                ((TileEntity) sign1).func_145843_s();
                return Optional.empty();
            }
            List<String> rawLines = container.getStringList(Keys.SIGN_LINES.getQuery()).get();
            List<Text> textLines = SpongeTexts.fromJson(rawLines);
            for (int i = 0; i < 4; i++) {
                ((SignTileEntity) sign1).field_145915_a[i] = SpongeTexts.toComponent(textLines.get(i));
            }
            ((SignTileEntity) sign1).func_145829_t();
            return Optional.of(sign1);
        });
    }
}
