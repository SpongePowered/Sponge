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
package org.spongepowered.common.data.type;

import net.kyori.adventure.text.Component;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.type.SkinPart;
import org.spongepowered.api.registry.RegistryTypes;

import net.minecraft.core.MappedRegistry;

public final class SpongeSkinPart implements SkinPart {

    private final Component component;

    public SpongeSkinPart(String part) {
        this.component = Component.translatable("options.modelPart." + part);
    }

    @Override
    public Component asComponent() {
        return this.component;
    }

    public boolean test(final int flags) {
        return (flags & this.getMask()) != 0;
    }

    public int getMask() {
        final MappedRegistry<SkinPart> registry = (MappedRegistry<SkinPart>) (Object) Sponge.game().registries().registry(RegistryTypes.SKIN_PART);
        return 1 << registry.getId(this);
    }
}
