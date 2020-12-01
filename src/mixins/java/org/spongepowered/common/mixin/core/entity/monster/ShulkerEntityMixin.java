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
package org.spongepowered.common.mixin.core.entity.monster;

import net.minecraft.entity.monster.ShulkerEntity;
import net.minecraft.network.datasync.DataParameter;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.data.type.DyeColor;
import org.spongepowered.api.util.Direction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.entity.monster.ShulkerEntityBridge;
import org.spongepowered.common.mixin.core.entity.MobEntityMixin;
import org.spongepowered.common.util.Constants;

@SuppressWarnings("ConstantConditions")
@Mixin(ShulkerEntity.class)
public abstract class ShulkerEntityMixin extends MobEntityMixin implements ShulkerEntityBridge {

    @Shadow @Final protected static DataParameter<Byte> COLOR;
    @Shadow @Final protected static DataParameter<net.minecraft.util.Direction> ATTACHED_FACE;

    @Override
    public @Nullable DyeColor bridge$getColor() {
        final int color = this.dataManager.get(ShulkerEntityMixin.COLOR);
        // The non colored variant
        if (color > 15) {
            return null;
        }
        return (DyeColor) (Object) net.minecraft.item.DyeColor.byId(color);
    }

    @Override
    public void bridge$setColor(final @Nullable DyeColor color) {
        final int value = color == null ? 16 : ((net.minecraft.item.DyeColor) (Object) color).getId();
        this.dataManager.set(ShulkerEntityMixin.COLOR, (byte) value);
    }

    @Override
    public Direction bridge$getDirection() {
        return Constants.DirectionFunctions.getFor(this.dataManager.get(ShulkerEntityMixin.ATTACHED_FACE));
    }

    @Override
    public void bridge$setDirection(final Direction direction) {
        this.dataManager.set(ShulkerEntityMixin.ATTACHED_FACE, Constants.DirectionFunctions.getFor(direction));
    }

}
