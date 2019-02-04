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
package org.spongepowered.common.data.property.store.block;

import org.spongepowered.api.data.property.Properties;
import org.spongepowered.api.data.property.PropertyHolder;
import org.spongepowered.api.data.property.store.DoublePropertyStore;
import org.spongepowered.api.world.Location;

import java.util.OptionalDouble;

@SuppressWarnings({"unchecked", "OptionalUsedAsFieldOrParameterType"})
public class LuminancePropertyStore implements DoublePropertyStore {

    @Override
    public OptionalDouble getDoubleFor(PropertyHolder propertyHolder) {
        if (!(propertyHolder instanceof Location)) {
            return OptionalDouble.empty();
        }
        final Location location = (Location) propertyHolder;
        final OptionalDouble blockLuminance = location.getDoubleProperty(Properties.SKY_LUMINANCE);
        final OptionalDouble skyLuminance = location.getDoubleProperty(Properties.BLOCK_LUMINANCE);
        double maxLuminance = -Double.MAX_VALUE;
        maxLuminance = or(maxLuminance, blockLuminance);
        maxLuminance = or(maxLuminance, skyLuminance);
        return maxLuminance == -Double.MAX_VALUE ? OptionalDouble.of(maxLuminance) : OptionalDouble.empty();
    }

    private static double or(double current, OptionalDouble that) {
        if (that.isPresent()) {
            final double luminance = that.getAsDouble();
            if (luminance > current) {
                return luminance;
            }
        }
        return current;
    }
}
