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
package org.spongepowered.common.mixin.tracker.entity.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.TNTEntity;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.event.tracking.phase.tick.EntityTickContext;
import org.spongepowered.common.mixin.tracker.entity.EntityMixin_Tracker;

import javax.annotation.Nullable;

@Mixin(TNTEntity.class)
public abstract class TNTEntityMixin_Tracker extends EntityMixin_Tracker {

    @Shadow @Nullable private LivingEntity tntPlacedBy;

    @Override
    public void populateFrameModifier(final CauseStackManager.StackFrame frame, final EntityTickContext context) {
        if (this.tntPlacedBy != null) {
            frame.addContext(EventContextKeys.IGNITER, (Living) this.tntPlacedBy);
        }
    }
}
