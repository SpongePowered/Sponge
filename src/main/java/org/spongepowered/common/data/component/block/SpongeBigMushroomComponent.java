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
package org.spongepowered.common.data.component.block;

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.component.block.BigMushroomComponent;
import org.spongepowered.api.data.token.Tokens;
import org.spongepowered.api.data.type.BigMushroomType;
import org.spongepowered.api.data.type.BigMushroomTypes;
import org.spongepowered.common.data.component.AbstractCatalogTypeComponent;

public class SpongeBigMushroomComponent extends AbstractCatalogTypeComponent<BigMushroomType, BigMushroomComponent> implements BigMushroomComponent {

    public SpongeBigMushroomComponent() {
        // We don't really have a "default" mushroom type because the blocks themselves are "technical blocks" and therefor
        // "not attainable".
        super(BigMushroomComponent.class, BigMushroomTypes.ALL_INSIDE);
    }

    @Override
    public BigMushroomComponent copy() {
        return new SpongeBigMushroomComponent().setValue(this.getValue());
    }

    @Override
    public DataContainer toContainer() {
        return new MemoryDataContainer().set(Tokens.BIG_MUSHROOM_TYPE.getQuery(), this.getValue().getId());
    }
}
