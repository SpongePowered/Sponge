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
import org.spongepowered.api.data.component.entity.SizeComponent;
import org.spongepowered.api.data.token.Tokens;
import org.spongepowered.common.data.component.SpongeAbstractComponent;

public class SpongeSizeComponent extends SpongeAbstractComponent<SizeComponent> implements SizeComponent {

    private final float base;
    private final float height;
    private final float scale;

    public SpongeSizeComponent(float base, float height, float scale) {
        super(SizeComponent.class);
        this.base = base;
        this.height = height;
        this.scale = scale;
    }

    @Override
    public float getBase() {
        return this.base;
    }

    @Override
    public float getHeight() {
        return this.height;
    }

    @Override
    public float getScale() {
        return this.scale;
    }

    @Override
    public SizeComponent copy() {
        return new SpongeSizeComponent(this.base, this.height, this.scale);
    }

    @Override
    public SizeComponent reset() {
        return this; // we don't reset
    }

    @Override
    public int compareTo(SizeComponent o) {
        return (int) Math.floor(o.getBase() - this.base) - (int) Math.floor(o.getHeight() - this.height)
                - (int) Math.floor(o.getScale() - this.scale);
    }

    @Override
    public DataContainer toContainer() {
        return new MemoryDataContainer()
                .set(Tokens.BASE_SIZE.getQuery(), this.base)
                .set(Tokens.HEIGHT.getQuery(), this.height)
                .set(Tokens.BOUNDING_BOX_SCALE.getQuery(), this.scale);
    }
}
