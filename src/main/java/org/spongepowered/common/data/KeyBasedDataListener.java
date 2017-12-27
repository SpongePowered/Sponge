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
package org.spongepowered.common.data;

import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.event.EventListener;
import org.spongepowered.api.event.data.ChangeDataHolderEvent;
import org.spongepowered.api.plugin.PluginContainer;

public class KeyBasedDataListener<E extends DataHolder> implements EventListener<ChangeDataHolderEvent.ValueChange> {


    private final Class<E> holderType;
    private final EventListener<ChangeDataHolderEvent.ValueChange> listener;
    private final PluginContainer owner;

    KeyBasedDataListener(Class<E> holderFilter, EventListener<ChangeDataHolderEvent.ValueChange> listener,
        PluginContainer currentContainer) {
        this.holderType = holderFilter;
        this.listener = listener;
        this.owner = currentContainer;
    }

    @Override
    public void handle(ChangeDataHolderEvent.ValueChange event) throws Exception {
        if (this.holderType.isInstance(event.getTargetHolder())) {
            this.listener.handle(event);
        }
    }

    public PluginContainer getOwner() {
        return this.owner;
    }
}
