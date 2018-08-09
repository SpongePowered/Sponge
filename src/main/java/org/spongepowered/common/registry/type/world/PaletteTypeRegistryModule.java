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
import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.api.world.schematic.BlockPaletteType;
import org.spongepowered.api.world.schematic.BlockPaletteTypes;
import org.spongepowered.common.registry.AbstractCatalogRegistryModule;
import org.spongepowered.common.registry.SpongeAdditionalCatalogRegistryModule;
import org.spongepowered.common.world.schematic.BimapPalette;
import org.spongepowered.common.world.schematic.GlobalPalette;
import org.spongepowered.common.world.schematic.SpongePaletteType;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@RegisterCatalog(BlockPaletteTypes.class)
public class PaletteTypeRegistryModule extends AbstractCatalogRegistryModule<BlockPaletteType>
    implements SpongeAdditionalCatalogRegistryModule<BlockPaletteType> {


    @Override
    public void registerAdditionalCatalog(BlockPaletteType extraCatalog) {
        checkNotNull(extraCatalog);
        String id = extraCatalog.getKey().toString();
        checkArgument(id.indexOf(' ') == -1, "Palette Type ID " + id + " may not contain a space");
        this.map.put(extraCatalog.getKey(), extraCatalog);
    }

    @Override
    public void registerDefaults() {
        registerAdditionalCatalog(new SpongePaletteType("global", () -> GlobalPalette.instance));
        registerAdditionalCatalog(new SpongePaletteType("local", BimapPalette::new));
    }

    @Override
    public boolean allowsApiRegistration() {
        return true;
    }

}
