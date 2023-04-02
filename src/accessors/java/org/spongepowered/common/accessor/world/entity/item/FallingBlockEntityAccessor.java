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
package org.spongepowered.common.accessor.world.entity.item;

import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.common.UntransformedInvokerError;

@Mixin(FallingBlockEntity.class)
public interface FallingBlockEntityAccessor {

    @Accessor("blockState") BlockState accessor$blockState();

    @Accessor("blockState") void accessor$blockState(final BlockState blockState);

    @Accessor("time") int accessor$time();

    @Accessor("time") void accessor$time(final int fallTime);

    @Accessor("cancelDrop") boolean accessor$cancelDrop();

    @Accessor("cancelDrop") void accessor$cancelDrop(final boolean cancelDrop);

    @Accessor("hurtEntities") boolean accessor$hurtEntities();

    @Accessor("hurtEntities") void accessor$hurtEntities(final boolean hurtEntities);

    @Accessor("fallDamageMax") int accessor$fallDamageMax();

    @Accessor("fallDamageMax") void accessor$fallDamageMax(final int fallDamageMax);

    @Accessor("fallDamagePerDistance") float accessor$fallDamagePerDistance();

    @Accessor("fallDamagePerDistance") void accessor$fallDamagePerDistance(final float fallDamageAmount);

    @Invoker("<init>") static FallingBlockEntity invoker$new(final Level world, final double x, final double y, final double z, final BlockState blockState) {
        throw new UntransformedInvokerError();
    }
}
