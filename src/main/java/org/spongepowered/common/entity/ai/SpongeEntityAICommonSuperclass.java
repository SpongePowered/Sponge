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
package org.spongepowered.common.entity.ai;

import net.minecraft.entity.ai.goal.Goal;

public abstract class SpongeEntityAICommonSuperclass extends Goal {

    @Override
    public boolean func_75250_a() {
        return this.shouldUpdate();
    }

    @Override
    public boolean func_75253_b() {
        return this.continueUpdating();
    }

    @Override
    public void func_75249_e() {
        this.start();
    }

    @Override
    public void func_75251_c() {
        this.reset();
    }

    @Override
    public void func_75246_d() {
        this.update();
    }

    @Override
    public boolean func_75252_g() {
        return this.canBeInterrupted();
    }

    public abstract boolean canBeInterrupted();

    public abstract void start();

    public abstract boolean shouldUpdate();

    public abstract void update();

    public abstract boolean continueUpdating();

    public abstract void reset();

}

