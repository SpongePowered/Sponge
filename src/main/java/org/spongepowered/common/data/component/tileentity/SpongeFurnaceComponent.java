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
package org.spongepowered.common.data.component.tileentity;

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.component.tileentity.FurnaceComponent;
import org.spongepowered.api.data.token.Tokens;
import org.spongepowered.common.data.component.SpongeAbstractComponent;

public class SpongeFurnaceComponent extends SpongeAbstractComponent<FurnaceComponent> implements FurnaceComponent {

    private int remainingBurnTime;
    private int remainingCookTime;

    public SpongeFurnaceComponent() {
        super(FurnaceComponent.class);
    }

    @Override
    public int getRemainingBurnTime() {
        return this.remainingBurnTime;
    }

    @Override
    public FurnaceComponent setRemainingBurnTime(int time) {
        this.remainingBurnTime = time;
        return this;
    }

    @Override
    public int getRemainingCookTime() {
        return this.remainingCookTime;
    }

    @Override
    public FurnaceComponent setRemainingCookTime(int time) {
        this.remainingCookTime = time;
        return this;
    }

    @Override
    public FurnaceComponent copy() {
        return new SpongeFurnaceComponent().setRemainingBurnTime(this.remainingBurnTime).setRemainingCookTime(this.remainingCookTime);
    }

    @Override
    public FurnaceComponent reset() {
        this.remainingBurnTime = 0;
        this.remainingCookTime = 0;
        return this;
    }

    @Override
    public int compareTo(FurnaceComponent o) {
        return (this.remainingBurnTime - o.getRemainingBurnTime()) - (this.remainingCookTime - o.getRemainingCookTime());
    }

    @Override
    public DataContainer toContainer() {
        return new MemoryDataContainer()
                .set(Tokens.REMAINING_FURNACE_BURN_TIME.getQuery(), this.remainingBurnTime)
                .set(Tokens.REMAINING_FURNACE_COOK_TIME.getQuery(), this.remainingCookTime);
    }
}
