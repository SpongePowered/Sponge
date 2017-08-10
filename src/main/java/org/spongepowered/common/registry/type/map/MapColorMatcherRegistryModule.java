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

import com.google.common.collect.ImmutableList;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.map.color.MapColor;
import org.spongepowered.api.map.color.MapColorMatchers;
import org.spongepowered.api.map.color.MapColors;
import org.spongepowered.api.map.util.MapColorMatcher;
import org.spongepowered.api.registry.CatalogRegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.api.util.Color;
import org.spongepowered.common.map.SpongeMapColorMatcher;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MapColorMatcherRegistryModule implements CatalogRegistryModule<MapColorMatcher> {

    public static MapColorMatcherRegistryModule getInstance() {
        return Holder.INSTANCE;
    }

    @RegisterCatalog(MapColorMatchers.class)
    private final Map<String, MapColorMatcher> mapColorMatchers = new HashMap<>();

    private static double getQuadraticRGB(Color c1, Color c2) {
        double r1 = c1.getRed();
        double g1 = c1.getGreen();
        double b1 = c1.getBlue();

        double r2 = c2.getRed();
        double g2 = c2.getGreen();
        double b2 = c2.getBlue();

        return Math.sqrt(((r2-r1)*(r2-r1)) + ((g2-g1)*(g2-g1)) + ((b2-b1)*(b2-b1)));
    }

    private static double getQuadraticHSB(Color c1, Color c2) {
        int r1 = c1.getRed();
        int g1 = c1.getGreen();
        int b1 = c1.getBlue();
        float[] hsv1 = new float[3];
        java.awt.Color.RGBtoHSB(r1, g1, b1, hsv1);

        int r2 = c2.getRed();
        int g2 = c2.getGreen();
        int b2 = c2.getBlue();
        float[] hsv2 = new float[3];
        java.awt.Color.RGBtoHSB(r2, g2, b2, hsv2);

        double dist = 0.0f;
        for (int i = 0; i < 3; ++i) {
            dist += (hsv1[i] - hsv2[i])*(hsv1[i] - hsv2[i]);
        }
        return Math.sqrt(dist);
    }

    @Override
    public void registerDefaults() {
        registerMatcher("rgb_unweighted", color -> {
            List<MapColor> colors = MapColors.getAll().stream()
                    .filter(mapColor -> mapColor.base() != MapColors.AIR)
                    .collect(Collectors.toList());

            // Assume there's at least one color
            MapColor lastMatch = colors.get(0);
            double lastDist = getQuadraticRGB(color, lastMatch.getColor());

            for  (MapColor nextOpt : colors) {
                double thisDist = getQuadraticRGB(color, nextOpt.getColor());
                if (thisDist < lastDist) {
                    lastDist = thisDist;
                    lastMatch = nextOpt;
                }
            }
            return lastMatch;
        });
    }

    private void registerMatcher(String id, Function<Color, MapColor> matcher) {
        mapColorMatchers.put(id, new SpongeMapColorMatcher(id, matcher));
    }

    @Override
    public Optional<MapColorMatcher> getById(String id) {
        return Optional.ofNullable(mapColorMatchers.get(id));
    }

    @Override
    public Collection<MapColorMatcher> getAll() {
        return ImmutableList.copyOf(mapColorMatchers.values());
    }

    MapColorMatcherRegistryModule() { }

    private static final class Holder {
        static final MapColorMatcherRegistryModule INSTANCE = new MapColorMatcherRegistryModule();
    }
}
