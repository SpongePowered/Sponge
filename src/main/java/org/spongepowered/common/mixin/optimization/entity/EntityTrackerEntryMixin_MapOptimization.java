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
package org.spongepowered.common.mixin.optimization.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityTrackerEntry;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.item.Item;
import net.minecraft.item.ItemMap;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.bridge.optimization.OptimizedMapDataBridge;

import javax.annotation.Nullable;

@Mixin(EntityTrackerEntry.class)
public abstract class EntityTrackerEntryMixin_MapOptimization {

    @Shadow @Final private Entity trackedEntity;

    /**
     * When processing an EntitYItemFrame containing an ItemMap, we call
     * bridge$updateItemFrameDecoration on its correspoding MapDAta
     *
     * <p>We always return 'null' here in order to prevent the original 'if' block from executing.</p>
     * @param itemStack
     * @return
     */
    @Nullable
    @Redirect(method = "updatePlayerList",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/item/ItemStack;getItem()Lnet/minecraft/item/Item;",
            ordinal = 0))
    private Item mapOptimization$onGetItem(final ItemStack itemStack) {
        if (itemStack.func_77973_b() instanceof ItemMap) {
            ((OptimizedMapDataBridge) ((ItemMap) itemStack.func_77973_b()).func_77873_a(itemStack, this.trackedEntity.field_70170_p)).mapOptimizationBridge$updateItemFrameDecoration((EntityItemFrame) this.trackedEntity);
        }
        return null;
    }

}
