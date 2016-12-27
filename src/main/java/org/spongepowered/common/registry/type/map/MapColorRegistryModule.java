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
package org.spongepowered.common.registry.type.map;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import net.minecraft.block.material.MapColor;
import org.spongepowered.api.map.color.MapColors;
import org.spongepowered.api.map.color.MapShades;
import org.spongepowered.api.registry.CatalogRegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.common.interfaces.block.material.IMixinMapColor;

import java.util.*;

public final class MapColorRegistryModule implements CatalogRegistryModule<org.spongepowered.api.map.color.MapColor.Base> {

    public static MapColorRegistryModule getInstance() {
        return Holder.INSTANCE;
    }

    @RegisterCatalog(value = MapColors.class, ignoredFields = "factory")
    private final Map<String, org.spongepowered.api.map.color.MapColor.Base> mapColorMappings = new HashMap<>();

    @Override
    public Optional<org.spongepowered.api.map.color.MapColor.Base> getById(String id) {
        checkNotNull(id, "id");
        return Optional.ofNullable(this.mapColorMappings.get(id.toLowerCase(Locale.ENGLISH)));
    }

    @Override
    public Collection<org.spongepowered.api.map.color.MapColor.Base> getAll() {
        return ImmutableList.copyOf(this.mapColorMappings.values());
    }

    // If we need more than these we will have to be tricky, theoretically maps
    // like all other items are modifiable by plugins with client work, however
    // because we don't get a listing of base names to colors we cannot handle this
    @Override
    public void registerDefaults() {
        registerColor("adobe", MapColor.adobeColor);
        registerColor("air", MapColor.airColor);
        registerColor("black", MapColor.blackColor);
        registerColor("blue", MapColor.blueColor);
        registerColor("brown", MapColor.brownColor);
        registerColor("clay", MapColor.clayColor);
        registerColor("cloth", MapColor.clothColor);
        registerColor("cyan", MapColor.cyanColor);
        registerColor("diamond", MapColor.diamondColor);
        registerColor("dirt", MapColor.dirtColor);
        registerColor("emerald", MapColor.emeraldColor);
        registerColor("foliage", MapColor.foliageColor);
        registerColor("gold", MapColor.goldColor);
        registerColor("grass", MapColor.grassColor);
        registerColor("gray", MapColor.grayColor);
        registerColor("green", MapColor.greenColor);
        registerColor("ice", MapColor.iceColor);
        registerColor("iron", MapColor.ironColor);
        registerColor("lapis", MapColor.lapisColor);
        registerColor("light_blue", MapColor.lightBlueColor);
        registerColor("lime", MapColor.limeColor);
        registerColor("magenta", MapColor.magentaColor);
        registerColor("netherrack", MapColor.netherrackColor);
        registerColor("obsidian", MapColor.obsidianColor);
        registerColor("pink", MapColor.pinkColor);
        registerColor("purple", MapColor.purpleColor);
        registerColor("quartz", MapColor.quartzColor);
        registerColor("red", MapColor.redColor);
        registerColor("sand", MapColor.sandColor);
        registerColor("silver", MapColor.silverColor);
        registerColor("snow", MapColor.snowColor);
        registerColor("stone", MapColor.stoneColor);
        registerColor("tnt", MapColor.tntColor);
        registerColor("water", MapColor.waterColor);
        registerColor("wood", MapColor.woodColor);
        registerColor("yellow", MapColor.yellowColor);
    }

    // Handles attaching color id's to their base instances since in this one
    // case an enum isn't used
    private void registerColor(String id, MapColor color) {
        ((IMixinMapColor) color).setId(id);
        mapColorMappings.put(id, (org.spongepowered.api.map.color.MapColor.Base) color);
    }

    // This makes life easier, we just extract the base color, the base implementation
    // handles the caching of sub-shaded instances and we don't have to hack things up
    public static Optional<org.spongepowered.api.map.color.MapColor> fromId(int id) {
        id = id & 255;
        int baseId = id / 4;
        int shade = id & 3;
        MapColor baseColor = MapColor.COLORS[baseId];
        if (baseColor == null) {
            return Optional.empty();
        } else {
            // TODO: Revisit and patch to call in registry module for shades
            return Optional.of(((org.spongepowered.api.map.color.MapColor) baseColor).shade(MapShades.BASE));
        }
    }

    MapColorRegistryModule() { }

    private static final class Holder {
        static final MapColorRegistryModule INSTANCE = new MapColorRegistryModule();
    }
}
