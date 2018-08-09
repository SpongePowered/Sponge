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
import net.minecraft.item.ItemArmor;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.data.type.ArmorType;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

import javax.annotation.Nullable;

@Mixin(ItemArmor.ArmorMaterial.class)
public abstract class MixinItemArmorMaterial implements ArmorType {

    @Shadow @Final private String name;
    @Shadow public abstract Item shadow$getRepairItem();

    // getName() end up replacing a method with the same signature in ArmorMaterial
    // at dev time. Since it's capitalized, the client becomes unable to retrieve
    // the texture, as the resource location is wrong.
    private String capitalizedName;
    private CatalogKey key;

    @Nullable
    private Optional<ItemType> repairItemType;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onConstruct(CallbackInfo ci) {
        this.capitalizedName = StringUtils.capitalize(this.name);
    }

    @Override
    public CatalogKey getKey() {
        if (this.key == null) {
            this.key = CatalogKey.resolve(this.name);
        }
        return this.key;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Optional<ItemType> getRepairItemType() {
        if (this.repairItemType == null) {
            this.repairItemType = Optional.ofNullable((ItemType) shadow$getRepairItem());
        }
        return this.repairItemType;
    }

}
