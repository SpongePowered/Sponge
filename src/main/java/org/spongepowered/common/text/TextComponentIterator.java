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
package org.spongepowered.common.text;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.UnmodifiableIterator;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.common.bridge.util.text.ITextComponentBridge;

import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.annotation.Nullable;

public class TextComponentIterator extends UnmodifiableIterator<ITextComponent> {

    private ITextComponentBridge component;
    private Iterator<ITextComponent> children;
    @Nullable private Iterator<ITextComponent> currentChildIterator;

    public TextComponentIterator(final ITextComponentBridge component) {
        this.component = checkNotNull(component, "component");
    }

    public TextComponentIterator(final Iterator<ITextComponent> children) {
        this.children = checkNotNull(children, "children");
        if (this.children.hasNext()) {
            this.setCurrentChildIterator();
        }
    }

    @Override
    public boolean hasNext() {
        return this.component != null || (this.currentChildIterator != null && this.currentChildIterator.hasNext());
    }

    // In order for this method to work properly, 'currentChildIterator' must be ready to return an element
    // (i.e its 'hasNext()' method returns true) when this method returns. If this condition can no longer be met,
    // we're done iterating.
    @Override
    public ITextComponent next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }

        if (this.component != null) {
            return this.init();
        }

        final ITextComponent result = this.currentChildIterator.next();

        if (!this.currentChildIterator.hasNext() && this.children.hasNext()) {
            this.setCurrentChildIterator();
        }

        return result;
    }

    private ITextComponent init() {
        this.children = this.component.bridge$childrenIterator();

        final ITextComponent result = (ITextComponent) this.component;
        this.component = null;

        // An iterator of an empty TextComponentTranslation doesn't have children. Thus, calling 'this.currentChildIterator.next()'
        // at the end of this method will lead to a NoSuchElementException. To fix this, we
        // initialize currentChildIterator so that the following call to 'hasNext()' will properly return 'false' if necessary
        if (this.children.hasNext()) {
            this.setCurrentChildIterator();
        }

        return result;
    }

    private void setCurrentChildIterator() {
        this.currentChildIterator = ((ITextComponentBridge) this.children.next()).bridge$withChildren().iterator();
    }

}
