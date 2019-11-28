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
import org.spongepowered.api.data.type.HorseStyle;
import org.spongepowered.api.data.type.HorseStyles;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.entity.SpongeHorseStyle;
import org.spongepowered.common.registry.type.AbstractPrefixAlternateCatalogTypeRegistryModule;

import java.util.Map;

@RegisterCatalog(HorseStyles.class)
public class HorseStyleRegistryModule extends AbstractPrefixAlternateCatalogTypeRegistryModule<HorseStyle> {

    public static final SpongeHorseStyle WHITE_STYLE = new SpongeHorseStyle(1, "minecraft:white", "WHITE");
    public static final SpongeHorseStyle WHITEFIELD = new SpongeHorseStyle(2, "minecraft:whitefield", "WHITEFIELD");
    public static final SpongeHorseStyle WHITE_DOTS = new SpongeHorseStyle(3, "minecraft:white_dots", "WHITE_DOTS");
    public static final SpongeHorseStyle BLACK_DOTS = new SpongeHorseStyle(4, "minecraft:black_dots", "BLACK_DOTS");
    public static final SpongeHorseStyle NONE = new SpongeHorseStyle(0, "minecraft:none", "NONE");

    public static final Map<Integer, HorseStyle> HORSE_STYLE_IDMAP = Maps.newHashMap();
    private static final HorseStyleRegistryModule INSTANCE = new HorseStyleRegistryModule();

    private HorseStyleRegistryModule() {
        super("minecraft");
    }

    public static HorseStyle getHorseStyle(EntityHorse horse) {
        return HORSE_STYLE_IDMAP.get((horse.func_110202_bQ() & 65280) >> 8);
    }

    public static HorseStyle getHorseStyle(DataView container) {
        return SpongeImpl.getRegistry().getType(HorseStyle.class, getData(container, Keys.HORSE_STYLE, String.class)).get();
    }

    public static HorseStyleRegistryModule getInstance() {
        return INSTANCE;
    }



    @Override
    public void registerDefaults() {
        register(NONE);
        register(WHITE_STYLE);
        register(WHITEFIELD);
        register(WHITE_DOTS);
        register(BLACK_DOTS);

        HORSE_STYLE_IDMAP.put(0, NONE);
        HORSE_STYLE_IDMAP.put(1, WHITE_STYLE);
        HORSE_STYLE_IDMAP.put(2, WHITEFIELD);
        HORSE_STYLE_IDMAP.put(3, WHITE_DOTS);
        HORSE_STYLE_IDMAP.put(4, BLACK_DOTS);

    }
}
