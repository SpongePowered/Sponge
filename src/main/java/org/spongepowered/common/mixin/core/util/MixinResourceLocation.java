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
package org.spongepowered.common.mixin.core.util;

import net.minecraft.util.ResourceLocation;
import org.spongepowered.api.CatalogKey;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ResourceLocation.class)
@Implements(@Interface(iface = CatalogKey.class, prefix = "catalog$"))
public abstract class MixinResourceLocation {

    @Final @Shadow protected String namespace;
    @Final @Shadow protected String path;
    @Shadow public abstract String shadow$toString();
    @Shadow public abstract int shadow$compareTo(ResourceLocation location);

    @Intrinsic
    public String catalog$getNamespace() {
        return this.namespace;
    }

    @Intrinsic
    public String catalog$getValue() {
        return this.path;
    }

    @Intrinsic
    public String catalog$toString() {
        return shadow$toString();
    }

    @Intrinsic
    public int catalog$compareTo(CatalogKey o) {
        if (o == null) {
            return 1;
        }
        return shadow$compareTo((ResourceLocation) (Object) o);
    }
}
