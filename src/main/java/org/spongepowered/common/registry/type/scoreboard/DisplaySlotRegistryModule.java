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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import net.minecraft.util.EnumChatFormatting;
import org.spongepowered.api.scoreboard.displayslot.DisplaySlot;
import org.spongepowered.api.scoreboard.displayslot.DisplaySlots;
import org.spongepowered.common.registry.CatalogRegistryModule;
import org.spongepowered.common.registry.type.text.TextColorRegistryModule;
import org.spongepowered.common.registry.util.RegisterCatalog;
import org.spongepowered.common.registry.util.RegistrationDependency;
import org.spongepowered.common.scoreboard.SpongeDisplaySlot;
import org.spongepowered.common.text.format.SpongeTextColor;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

@RegistrationDependency(TextColorRegistryModule.class)
public final class DisplaySlotRegistryModule implements CatalogRegistryModule<DisplaySlot> {

    @RegisterCatalog(DisplaySlots.class)
    public final Map<String, SpongeDisplaySlot> displaySlotMappings = Maps.newLinkedHashMap();

    public static DisplaySlotRegistryModule getInstance() {
        return Holder.INSTANCE;
    }

    public Optional<DisplaySlot> getForIndex(int id) {
        return Optional.ofNullable(Iterables.get(this.displaySlotMappings.values(), id, null));
    }

    @Override
    public Optional<DisplaySlot> getById(String id) {
        return Optional.ofNullable(this.displaySlotMappings.get(checkNotNull(id).toLowerCase()));
    }

    @Override
    public Collection<DisplaySlot> getAll() {
        return ImmutableList.copyOf(this.displaySlotMappings.values());
    }

    @Override
    public void registerDefaults() {
        this.displaySlotMappings.put("list", new SpongeDisplaySlot("list", null, 0));
        this.displaySlotMappings.put("sidebar", new SpongeDisplaySlot("sidebar", null, 1));
        this.displaySlotMappings.put("below_name", new SpongeDisplaySlot("below_name", null, 2));

        for (Map.Entry<EnumChatFormatting, SpongeTextColor> entry : TextColorRegistryModule.enumChatColor.entrySet()) {
            this.displaySlotMappings.put(entry.getValue().getId(),
                                         new SpongeDisplaySlot(entry.getValue().getId(), entry.getValue(), entry.getKey().getColorIndex() + 3));
        }
    }

    private static final class Holder {

        private static final DisplaySlotRegistryModule INSTANCE = new DisplaySlotRegistryModule();
    }
}
