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
package org.spongepowered.common.mixin.api.minecraft.map;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes;
import org.spongepowered.api.data.type.DyeColor;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.map.decoration.MapDecorationType;
import org.spongepowered.api.registry.DefaultedRegistryReference;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Map;
import java.util.Optional;

@Mixin(net.minecraft.world.level.saveddata.maps.MapDecorationType.class)
public abstract class MapDecorationTypeMixin_API implements MapDecorationType {

    private static final Map<Holder<net.minecraft.world.level.saveddata.maps.MapDecorationType>, DefaultedRegistryReference<DyeColor>> BANNER_COLORS =
            Map.ofEntries(
                    Map.entry(MapDecorationTypes.WHITE_BANNER, DyeColors.WHITE),
                    Map.entry(MapDecorationTypes.ORANGE_BANNER, DyeColors.ORANGE),
                    Map.entry(MapDecorationTypes.MAGENTA_BANNER, DyeColors.MAGENTA),
                    Map.entry(MapDecorationTypes.LIGHT_BLUE_BANNER, DyeColors.LIGHT_BLUE),
                    Map.entry(MapDecorationTypes.YELLOW_BANNER, DyeColors.YELLOW),
                    Map.entry(MapDecorationTypes.LIME_BANNER, DyeColors.LIME),
                    Map.entry(MapDecorationTypes.PINK_BANNER, DyeColors.PINK),
                    Map.entry(MapDecorationTypes.GRAY_BANNER, DyeColors.GRAY),
                    Map.entry(MapDecorationTypes.LIGHT_GRAY_BANNER, DyeColors.LIGHT_GRAY),
                    Map.entry(MapDecorationTypes.CYAN_BANNER, DyeColors.CYAN),
                    Map.entry(MapDecorationTypes.PURPLE_BANNER, DyeColors.PURPLE),
                    Map.entry(MapDecorationTypes.BLUE_BANNER, DyeColors.BLUE),
                    Map.entry(MapDecorationTypes.BROWN_BANNER, DyeColors.BROWN),
                    Map.entry(MapDecorationTypes.GREEN_BANNER, DyeColors.GREEN),
                    Map.entry(MapDecorationTypes.RED_BANNER, DyeColors.RED),
                    Map.entry(MapDecorationTypes.BLACK_BANNER, DyeColors.BLACK)
            );

    @Override
    public Optional<DyeColor> bannerColor() {
        final var holder = BuiltInRegistries.MAP_DECORATION_TYPE.wrapAsHolder((net.minecraft.world.level.saveddata.maps.MapDecorationType) (Object) this);
        return Optional.ofNullable(BANNER_COLORS.get(holder)).map(DefaultedRegistryReference::get);
    }
}
