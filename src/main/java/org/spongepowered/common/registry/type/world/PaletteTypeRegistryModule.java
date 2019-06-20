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
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.api.registry.util.RegistrationDependency;
import org.spongepowered.api.world.schematic.PaletteType;
import org.spongepowered.api.world.schematic.PaletteTypes;
import org.spongepowered.common.registry.SpongeAdditionalCatalogRegistryModule;
import org.spongepowered.common.world.schematic.BimapPalette;
import org.spongepowered.common.world.schematic.GlobalPalette;
import org.spongepowered.common.world.schematic.SpongePaletteType;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@RegistrationDependency(BlockPaletteTypeRegistryModule.class)
public class PaletteTypeRegistryModule implements SpongeAdditionalCatalogRegistryModule<PaletteType<?>> {

    @RegisterCatalog(PaletteTypes.class) private final Map<String, PaletteType<?>> paletteMappings = Maps.newConcurrentMap();

    @Override
    public void registerAdditionalCatalog(PaletteType<?> extraCatalog) {
        checkNotNull(extraCatalog);
        String id = extraCatalog.getId();
        checkArgument(id.indexOf(' ') == -1, "Palette Type ID " + id + " may not contain a space");
        this.paletteMappings.put(id.toLowerCase(Locale.ENGLISH), extraCatalog);
    }

    @Override
    public Optional<PaletteType<?>> getById(String id) {
        return Optional.ofNullable(this.paletteMappings.get(id));
    }

    @Override
    public Collection<PaletteType<?>> getAll() {
        return ImmutableList.copyOf(this.paletteMappings.values());
    }

    @SuppressWarnings("deprecation")
    @Override
    public void registerDefaults() {
        registerAdditionalCatalog(org.spongepowered.api.world.schematic.BlockPaletteTypes.GLOBAL);
        registerAdditionalCatalog(org.spongepowered.api.world.schematic.BlockPaletteTypes.LOCAL);
        registerAdditionalCatalog(new SpongePaletteType<>("global_biomes", GlobalPalette::getBiomePalette));
        registerAdditionalCatalog(new SpongePaletteType<>("local_biomes", () -> new BimapPalette<>(PaletteTypes.LOCAL_BIOMES)));
    }

    @Override
    public boolean allowsApiRegistration() {
        return true;
    }

}
