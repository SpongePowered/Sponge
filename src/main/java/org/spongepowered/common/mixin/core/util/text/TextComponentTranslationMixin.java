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
package org.spongepowered.common.mixin.core.util.text;

import com.google.common.collect.Iterators;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import org.spongepowered.api.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.util.text.ITextComponentBridge;
import org.spongepowered.common.text.TextComponentIterable;
import org.spongepowered.common.text.translation.SpongeTranslation;

import java.util.Iterator;
import java.util.List;

@Mixin(TranslationTextComponent.class)
public abstract class TextComponentTranslationMixin extends TextComponentBaseMixin {

    @Shadow @Final private String key;
    @Shadow @Final private Object[] formatArgs;
    @Shadow List<ITextComponent> children;

    @Shadow abstract void ensureInitialized();

    @Override
    protected Text.Builder impl$createBuilder() {
        return Text.builder(new SpongeTranslation(this.key), wrapFormatArgs(this.formatArgs));
    }

    @Override
    public Iterator<ITextComponent> bridge$childrenIterator() {
        ensureInitialized();
        return Iterators.concat(this.children.iterator(), super.bridge$childrenIterator());
    }

    @Override
    public Iterable<ITextComponent> bridge$withChildren() {
        return new TextComponentIterable(this, false);
    }

    private static Object[] wrapFormatArgs(final Object... formatArgs) {
        final Object[] ret = new Object[formatArgs.length];
        for (int i = 0; i < formatArgs.length; ++i) {
            if (formatArgs[i] instanceof ITextComponentBridge) {
                ret[i] = ((ITextComponentBridge) formatArgs[i]).bridge$toText();
            } else {
                ret[i] = formatArgs[i];
            }
        }
        return ret;
    }

}
