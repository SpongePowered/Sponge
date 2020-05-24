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
package org.spongepowered.common.mixin.core.item;

import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.SpongeImplHooks;

@Mixin(Items.class)
public class ItemsMixin {

    /**
     * @author gabizou - June 10th, 2019 - 1.12.2
     * @reason This uses the SpongeImplHooks so that we eliminate
     * more Mixins in SpongeForge and SpongeVanilla. The hook here
     * is to allow a mod in Forge to replace a previously registered
     * item. Vanilla doesn't do this, so we don't have to care about
     * vanilla, but Forge, we have to check against the Item registry.

     * @param key The location id of the item, like "minecraft:diamond_sword"
     * @param item The item instance
     * @param ci callback
     */
    @Inject(method = "register(Lnet/minecraft/util/ResourceLocation;Lnet/minecraft/item/Item;)Lnet/minecraft/item/Item;", at = @At("RETURN"))
    private static void spongeImpl$registerItemWithSpongeRegistry(final ResourceLocation key, final Item item, final CallbackInfoReturnable<Item> ci) {
        SpongeImplHooks.registerItemForSpongeRegistry(key, item);
    }

}
