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
package org.spongepowered.common.registry.type.entity;

import static org.spongepowered.common.data.util.DataUtil.getData;

import com.google.common.collect.Maps;
import net.minecraft.entity.passive.EntityHorse;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.HorseColor;
import org.spongepowered.api.data.type.HorseColors;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.entity.SpongeHorseColor;
import org.spongepowered.common.registry.type.AbstractPrefixAlternateCatalogTypeRegistryModule;
import java.util.Map;

@RegisterCatalog(HorseColors.class)
public class HorseColorRegistryModule extends AbstractPrefixAlternateCatalogTypeRegistryModule<HorseColor> {


    public static final Map<Integer, HorseColor> HORSE_COLOR_IDMAP = Maps.newHashMap();
    // horse colors
    public static final SpongeHorseColor WHITE = new SpongeHorseColor(0, "minecraft:white", "WHITE");
    public static final SpongeHorseColor CREAMY = new SpongeHorseColor(1, "minecraft:creamy", "CREAMY");
    public static final SpongeHorseColor CHESTNUT = new SpongeHorseColor(2, "minecraft:chestnut", "CHESTNUT");
    public static final SpongeHorseColor BROWN = new SpongeHorseColor(3, "minecraft:brown", "BROWN");
    public static final SpongeHorseColor BLACK = new SpongeHorseColor(4, "minecraft:black", "BLACK");
    public static final SpongeHorseColor GRAY = new SpongeHorseColor(5, "minecraft:gray", "GRAY");
    public static final SpongeHorseColor DARK_BROWN = new SpongeHorseColor(6, "minecraft:dark_brown", "DARK_BROWN");
    private static final HorseColorRegistryModule INSTANCE = new HorseColorRegistryModule();

    private HorseColorRegistryModule() {
        super("minecraft");
    }

    public static HorseColorRegistryModule getInstance() {
        return INSTANCE;
    }

    public static HorseColor getHorseColor(EntityHorse horse) {
        return HORSE_COLOR_IDMAP.get(horse.func_110202_bQ() & 255);
    }

    public static HorseColor getHorseColor(DataView container) {

        return SpongeImpl.getRegistry().getType(HorseColor.class, getData(container, Keys.HORSE_COLOR, String.class)).get();
    }

    @Override
    public void registerDefaults() {
        HORSE_COLOR_IDMAP.put(0, HorseColorRegistryModule.WHITE);
        HORSE_COLOR_IDMAP.put(1, HorseColorRegistryModule.CREAMY);
        HORSE_COLOR_IDMAP.put(2, HorseColorRegistryModule.CHESTNUT);
        HORSE_COLOR_IDMAP.put(3, HorseColorRegistryModule.BROWN);
        HORSE_COLOR_IDMAP.put(4, HorseColorRegistryModule.BLACK);
        HORSE_COLOR_IDMAP.put(5, HorseColorRegistryModule.GRAY);
        HORSE_COLOR_IDMAP.put(6, HorseColorRegistryModule.DARK_BROWN);
        register(WHITE);
        register(CREAMY);
        register(CHESTNUT);
        register(BROWN);
        register(BLACK);
        register(GRAY);
        register(DARK_BROWN);

    }
}
