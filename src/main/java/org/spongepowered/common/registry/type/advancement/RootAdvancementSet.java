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
package org.spongepowered.common.registry.type.advancement;

import net.minecraft.advancements.Advancement;
import org.spongepowered.common.SpongeImplHooks;

import java.util.LinkedHashSet;

public final class RootAdvancementSet extends LinkedHashSet<Advancement> {

    private static final long serialVersionUID = 4921542661462938585L;

    @Override
    public boolean add(Advancement advancement) {
        if (super.add(advancement)) {
            if (SpongeImplHooks.isMainThread()) {
                AdvancementTreeRegistryModule.getInstance().registerSilently(advancement);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean remove(Object o) {
        if (super.remove(o)) {
            if (SpongeImplHooks.isMainThread()) {
                AdvancementTreeRegistryModule.getInstance().remove((Advancement) o);
            }
            return true;
        }
        return false;
    }

    @Override
    public void clear() {
        super.clear();
        if (SpongeImplHooks.isMainThread()) {
            AdvancementTreeRegistryModule.getInstance().clear();
        }
    }
}
