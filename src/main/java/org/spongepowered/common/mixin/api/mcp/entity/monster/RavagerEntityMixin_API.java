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
package org.spongepowered.common.mixin.api.mcp.entity.monster;

import net.minecraft.entity.monster.RavagerEntity;
import org.spongepowered.api.entity.living.monster.raider.Ravager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.util.Constants;

@Mixin(RavagerEntity.class)
public abstract class RavagerEntityMixin_API extends AbstractRaiderEntityMixin_API implements Ravager {

    @Shadow private int stunTick;
    @Shadow private int roarTick;
    @Shadow protected abstract boolean shadow$isMovementBlocked();

    @Override
    public boolean isImmobilized() {
        return this.shadow$isMovementBlocked();
    }

    @Override
    public boolean isRoaring() {
        return this.roarTick > 0;
    }

    @Override
    public void setRoaring(boolean roaring) {
        if (roaring) {
            this.roarTick = Constants.Entity.Ravager.ROAR_TIME;
            return;
        }

        this.roarTick = 0;
    }

    @Override
    public boolean isStunned() {
        return this.stunTick > 0;
    }

    @Override
    public void setStunned(boolean stunned) {
        if (stunned) {
            this.stunTick = Constants.Entity.Ravager.STUNNED_TIME;
            return;
        }

        this.stunTick = 0;
    }

}
