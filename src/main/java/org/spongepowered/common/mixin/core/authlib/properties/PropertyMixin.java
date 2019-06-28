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
package org.spongepowered.common.mixin.core.authlib.properties;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.mojang.authlib.properties.Property;
import org.spongepowered.api.profile.property.ProfileProperty;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;

@Mixin(value = Property.class, remap = false)
public abstract class PropertyMixin {

    @Shadow public abstract String getName();
    @Shadow public abstract String getValue();
    @Nullable @Shadow public abstract String getSignature();

    @Override
    public boolean equals(@Nullable final Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof ProfileProperty)) {
            return false;
        }

        final ProfileProperty that = (ProfileProperty) other;
        return Objects.equal(this.getName(), that.getName())
                && Objects.equal(this.getValue(), that.getValue())
                && Objects.equal(this.getSignature(), ((Property) that).getSignature());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.getName(), this.getValue(), this.getSignature());
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("name", this.getName())
                .add("value", this.getValue())
                .add("signature", this.getSignature())
                .toString();
    }

}
