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
package org.spongepowered.common.registry.type.world;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import org.spongepowered.api.registry.AlternateCatalogRegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.api.world.schematic.PaletteTypes;
import org.spongepowered.common.registry.SpongeAdditionalCatalogRegistryModule;
import org.spongepowered.common.world.schematic.BimapPalette;
import org.spongepowered.common.world.schematic.BlockPaletteWrapper;
import org.spongepowered.common.world.schematic.GlobalPalette;
import org.spongepowered.common.world.schematic.SpongeBlockPaletteType;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@SuppressWarnings("deprecation")
public class BlockPaletteTypeRegistryModule implements SpongeAdditionalCatalogRegistryModule<org.spongepowered.api.world.schematic.BlockPaletteType>,
    AlternateCatalogRegistryModule<org.spongepowered.api.world.schematic.BlockPaletteType> {

    @RegisterCatalog(org.spongepowered.api.world.schematic.BlockPaletteTypes.class) private final Map<String, org.spongepowered.api.world.schematic.BlockPaletteType> paletteMappings = Maps.newHashMap();

    @Override
    public void registerAdditionalCatalog(final org.spongepowered.api.world.schematic.BlockPaletteType extraCatalog) {
        checkNotNull(extraCatalog);
        final String id = extraCatalog.getId();
        checkArgument(id.indexOf(' ') == -1, "Palette Type ID " + id + " may not contain a space");
        this.paletteMappings.put(id.toLowerCase(Locale.ENGLISH), extraCatalog);
    }

    @Override
    public Optional<org.spongepowered.api.world.schematic.BlockPaletteType> getById(String id) {
        // Special casing because of the same API version.
        if ("global".equalsIgnoreCase(id)) {
            id = "global_blocks";
        } else if ("local".equalsIgnoreCase(id)) {
            id = "local_blocks";
        }
        return Optional.ofNullable(this.paletteMappings.get(id));
    }

    @Override
    public Collection<org.spongepowered.api.world.schematic.BlockPaletteType> getAll() {
        return ImmutableList.copyOf(this.paletteMappings.values());
    }

    @Override
    public void registerDefaults() {
        registerAdditionalCatalog(new SpongeBlockPaletteType("global_blocks", () -> (org.spongepowered.api.world.schematic.BlockPalette) GlobalPalette.getBlockPalette()));
        registerAdditionalCatalog(new SpongeBlockPaletteType("local_blocks", () -> new BlockPaletteWrapper(new BimapPalette<>(PaletteTypes.LOCAL_BLOCKS), org.spongepowered.api.world.schematic.BlockPaletteTypes.LOCAL)));
    }

    @Override
    public boolean allowsApiRegistration() {
        return true;
    }

    @Override
    public Map<String, org.spongepowered.api.world.schematic.BlockPaletteType> provideCatalogMap() {
        final HashMap<String, org.spongepowered.api.world.schematic.BlockPaletteType> map = new HashMap<>();
        this.paletteMappings.forEach((key, value) -> map.put(key.split("_")[0], value));
        return map;
    }
}
