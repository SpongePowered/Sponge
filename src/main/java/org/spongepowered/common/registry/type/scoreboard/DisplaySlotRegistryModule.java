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
package org.spongepowered.common.registry.type.scoreboard;

import com.google.common.collect.Iterables;
import net.minecraft.util.text.TextFormatting;
import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.registry.AlternateCatalogRegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.api.registry.util.RegistrationDependency;
import org.spongepowered.api.scoreboard.displayslot.DisplaySlot;
import org.spongepowered.api.scoreboard.displayslot.DisplaySlots;
import org.spongepowered.common.registry.AbstractCatalogRegistryModule;
import org.spongepowered.common.registry.type.text.TextColorRegistryModule;
import org.spongepowered.common.scoreboard.SpongeDisplaySlot;
import org.spongepowered.common.text.format.SpongeTextColor;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@RegisterCatalog(DisplaySlots.class)
@RegistrationDependency(TextColorRegistryModule.class)
public final class DisplaySlotRegistryModule extends AbstractCatalogRegistryModule<DisplaySlot> implements AlternateCatalogRegistryModule<DisplaySlot> {

    public static DisplaySlotRegistryModule getInstance() {
        return Holder.INSTANCE;
    }

    public Optional<DisplaySlot> getForIndex(int id) {
        return Optional.ofNullable(Iterables.get(this.map.values(), id, null));
    }

    @Override
    public void registerDefaults() {
        register(CatalogKey.minecraft("list"), new SpongeDisplaySlot("list", null, 0));
        register(CatalogKey.minecraft("sidebar"), new SpongeDisplaySlot("sidebar", null, 1));
        register(CatalogKey.minecraft("below_name"), new SpongeDisplaySlot("below_name", null, 2));

        for (Map.Entry<TextFormatting, SpongeTextColor> entry : TextColorRegistryModule.getInstance().enumChatColor.entrySet()) {
            final String name = entry.getValue().getName();
            final SpongeDisplaySlot value = new SpongeDisplaySlot(name, entry.getValue(), entry.getKey().getColorIndex() + 3);
            register(CatalogKey.minecraft(name), value);
        }
    }

    DisplaySlotRegistryModule() {
    }

    private static final class Holder {

        static final DisplaySlotRegistryModule INSTANCE = new DisplaySlotRegistryModule();
    }
}
