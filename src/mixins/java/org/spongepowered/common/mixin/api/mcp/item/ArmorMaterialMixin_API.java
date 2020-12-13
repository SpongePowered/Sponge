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
package org.spongepowered.common.mixin.api.mcp.item;

import net.minecraft.util.SoundEvent;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.data.type.ArmorMaterial;
import org.spongepowered.api.item.recipe.crafting.Ingredient;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeCommon;

import java.util.Optional;
import java.util.function.Supplier;

@Mixin(net.minecraft.item.ArmorMaterial.class)
@Implements(@Interface(iface = ArmorMaterial.class, prefix = "armorMaterial$"))
public abstract class ArmorMaterialMixin_API implements ArmorMaterial {

    // @formatter:off
    @Shadow public abstract net.minecraft.item.crafting.Ingredient shadow$getRepairIngredient();
    // @formatter:on

    private ResourceKey api$key;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void api$setKey(String enumName, int ordinal, String nameIn, int maxDamageFactorIn, int[] damageReductionAmountsIn, int enchantabilityIn,
        SoundEvent equipSoundIn, float p_i48533_8_, float p_i231593_9_, Supplier<net.minecraft.item.crafting.Ingredient> repairMaterialSupplier, CallbackInfo ci) {
        this.api$key = ResourceKey.of(SpongeCommon.getActivePlugin(), nameIn.toLowerCase());
    }

    @Override
    public ResourceKey getKey() {
        return this.api$key;
    }

    @Override
    public Optional<Ingredient> getRepairIngredient() {
        final net.minecraft.item.crafting.Ingredient repairMaterial = this.shadow$getRepairIngredient();
        return Optional.ofNullable(((Ingredient) (Object) repairMaterial));
    }

}
