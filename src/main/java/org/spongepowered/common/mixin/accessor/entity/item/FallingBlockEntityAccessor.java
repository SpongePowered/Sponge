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
package org.spongepowered.common.mixin.accessor.entity.item;

import net.minecraft.block.BlockState;
import net.minecraft.entity.item.FallingBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FallingBlockEntity.class)
public interface FallingBlockEntityAccessor {

    @Accessor("dontSetBlock") boolean accessor$getDontSetBlock();

    @Accessor("dontSetBlock") void accessor$setDontSetAsBlock(boolean dontSetBlock);

    @Accessor("hurtEntities") boolean accessor$getHurtEntities();

    @Accessor("hurtEntities") void accessor$setHurtEntities(boolean hurtEntities);

    @Accessor("fallHurtMax") int accessor$getFallHurtMax();

    @Accessor("fallHurtMax") void accessor$setFallHurtMax(int fallHurtMax);

    @Accessor("fallHurtAmount") float accessor$getFallHurtAmount();

    @Accessor("fallHurtAmount") void accessor$setFallHurtAmount(float fallHurtAmount);

    @Accessor("fallTile") BlockState accessor$getFallTile();

    @Accessor("fallTile") void accessor$setFallTile(BlockState fallTile);

    @Accessor("fallTime") int accessor$getFallTime();

    @Accessor("fallTime") void accessor$setFallTime(int fallTime);
}
