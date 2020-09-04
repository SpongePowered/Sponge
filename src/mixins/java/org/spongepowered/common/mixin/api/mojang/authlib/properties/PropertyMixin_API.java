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
package org.spongepowered.common.mixin.api.mojang.authlib.properties;

import com.mojang.authlib.properties.Property;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.profile.property.ProfileProperty;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;

@Mixin(value = Property.class, remap = false)
public abstract class PropertyMixin_API implements ProfileProperty {

    @Shadow @Final private String name;
    @Shadow @Final private String value;
    @Shadow @Final private @Nullable String signature;

    @Override
    public Optional<String> getSignature() { // We don't need to make this @Implements because the signature difference
        return Optional.ofNullable(this.signature);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.name, this.value, this.signature);
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof ProfileProperty)) {
            return false;
        }
        final ProfileProperty other = (ProfileProperty) obj;
        return other.getName().equals(this.name) &&
                other.getValue().equals(this.value) &&
                Objects.equals(other.getSignature().orElse(null), this.signature);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Property.class.getSimpleName() + "[", "]")
                .add("name='" + this.name + "'")
                .add("value='" + this.value + "'")
                .add("signature='" + this.signature + "'")
                .toString();
    }
}
