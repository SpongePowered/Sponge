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
package org.spongepowered.common.mixin.optimization.world.entity.decoration;

import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.bridge.optimization.OptimizedMapDataBridge;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.common.mixin.core.world.entity.decoration.HangingEntityMixin;

@Mixin(ItemFrame.class)
public abstract class ItemFrameMixin_Optimization_Map extends HangingEntityMixin {

    @Shadow public abstract ItemStack shadow$getItem();

    @Inject(method = "setItem(Lnet/minecraft/world/item/ItemStack;Z)V", at = @At(value = "HEAD"))
    private void mapOptimization$SetItemUpdateMapData(final ItemStack stack, final boolean p_174864_2_, final CallbackInfo ci) {
        if (((WorldBridge) this.level).bridge$isFake()) {
            return;
        }

        final OptimizedMapDataBridge mapData = (OptimizedMapDataBridge) MapItem.getOrCreateSavedData(stack, this.level);
        if (stack.getItem() instanceof MapItem) {
            mapData.mapOptimizationBridge$updateItemFrameDecoration((ItemFrame) (Object) this);
        } else if (this.shadow$getItem().getItem() instanceof MapItem && stack.isEmpty()) {
            mapData.mapOptimizationBridge$removeItemFrame((ItemFrame) (Object) this);
        }
    }



}
