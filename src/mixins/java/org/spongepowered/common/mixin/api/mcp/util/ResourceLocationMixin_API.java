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
package org.spongepowered.common.mixin.api.mcp.util;

import static com.google.common.base.Preconditions.checkNotNull;

import net.minecraft.util.ResourceLocation;
import org.spongepowered.api.CatalogKey;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ResourceLocation.class)
@Implements(value = @Interface(iface = CatalogKey.class, prefix = "catalogKey$"))
public abstract class ResourceLocationMixin_API implements CatalogKey {

    @Shadow public abstract String shadow$getNamespace();
    @Shadow public abstract String shadow$getPath();
    @Shadow public abstract int shadow$compareTo(ResourceLocation p_compareTo_1_);
    @Shadow public abstract String shadow$toString();

    @Intrinsic
    public String catalogKey$getNamespace() {
        return this.shadow$getNamespace();
    }

    @Override
    public String getValue() {
        return this.shadow$getPath();
    }

    @Override
    public String getFormatted() {
        return this.shadow$toString();
    }

    @Override
    public int compareTo(CatalogKey o) {
        checkNotNull(o);
        return this.shadow$compareTo((ResourceLocation) (Object) o);
    }
}
