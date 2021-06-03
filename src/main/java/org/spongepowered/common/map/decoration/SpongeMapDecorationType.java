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
package org.spongepowered.common.map.decoration;

import net.minecraft.world.level.saveddata.maps.MapDecoration;
import org.spongepowered.api.map.decoration.MapDecorationType;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SpongeMapDecorationType implements MapDecorationType {

    private static final Map<MapDecoration.Type, MapDecorationType> VANILLA_TYPE_MAP = new HashMap<>();

    public static Optional<MapDecorationType> toSpongeType(final MapDecoration.Type vanillaType) {
        return Optional.ofNullable(SpongeMapDecorationType.VANILLA_TYPE_MAP.get(vanillaType));
    }

    private final MapDecoration.Type type;

    public SpongeMapDecorationType(final MapDecoration.Type mapDecorationType) {
        this.type = mapDecorationType;
        SpongeMapDecorationType.VANILLA_TYPE_MAP.put(mapDecorationType, this);
    }

    public MapDecoration.Type getType() {
        return this.type;
    }
}
