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
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.api.data.type.ToolType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Locale;

@Mixin(Item.ToolMaterial.class)
public abstract class MixinItemToolMaterial implements ToolType {

    private String name;
    private String capitalizedName;

    @Inject(method = "<init>", at = @At("RETURN"))
    public void onConstruct(CallbackInfo ci) {
        // This is a giant workaround due to being unable to refer to synthetic
        // methods provided by Enum and the base enum itself does not
        // have a field for the name.
        String toString = this.toString();
        if (toString.equalsIgnoreCase("emerald")) {
            toString = "diamond";
        }
        this.name = toString.toLowerCase(Locale.ENGLISH);
        this.capitalizedName = StringUtils.capitalize(this.name);
    }

    @Override
    public String getId() {
        return this.name;
    }

    @Override
    public String getName() {
        return this.capitalizedName;
    }
}
