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
package org.spongepowered.common.data.component.entity;

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.component.entity.JoinComponent;
import org.spongepowered.api.data.token.Tokens;
import org.spongepowered.common.data.component.SpongeAbstractComponent;

import java.util.Date;

public class SpongeJoinComponent extends SpongeAbstractComponent<JoinComponent> implements JoinComponent {

    private final long first;
    private final long last;

    public SpongeJoinComponent(long first, long last) {
        super(JoinComponent.class);
        this.first = first;
        this.last = last;
    }

    @Override
    public Date getFirstPlayed() {
        return new Date(this.first);
    }

    @Override
    public Date getLastPlayed() {
        return new Date(this.last);
    }

    @Override
    public JoinComponent copy() {
        return new SpongeJoinComponent(this.first, this.last);
    }

    @Override
    public JoinComponent reset() {
        return this;
    }

    @Override
    public int compareTo(JoinComponent o) {
        return o.getFirstPlayed().compareTo(this.getFirstPlayed()) - o.getLastPlayed().compareTo(this.getLastPlayed());
    }

    @Override
    public DataContainer toContainer() {
        return new MemoryDataContainer()
                .set(Tokens.FIRST_PLAYED.getQuery(), this.first)
                .set(Tokens.LAST_PLAYED.getQuery(), this.last);
    }
}
