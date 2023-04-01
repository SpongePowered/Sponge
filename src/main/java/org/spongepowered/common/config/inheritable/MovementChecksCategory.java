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
package org.spongepowered.common.config.inheritable;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Setting;

@ConfigSerializable
public final class MovementChecksCategory {

    @Setting("moved-wrongly-threshold")
    @Comment("Sets the threshold for whether the 'player moved wrongly!' check will be enforced")
    public double movedWronglyThreshold = 0.0625;

    @Setting("vehicle-moved-wrongly-threshold")
    @Comment("Sets the threshold for whether the 'vehicle of player moved wrongly!' check will be enforced")
    public double vehicleMovedWronglyThreshold = 0.0625;

    @Setting("moved-too-quickly-threshold")
    @Comment("Sets the threshold for whether the 'player moved too quickly!' check will be enforced")
    public double movedTooQuicklyThreshold = 100.0;

    @Setting("vehicle-moved-too-quickly-threshold")
    @Comment("Sets the threshold for whether the 'vehicle of player moved too quickly!' check will be enforced")
    public double vehicleMovedTooQuicklyThreshold = 100.0;

    @Setting("moved-wrongly")
    @Comment("Controls whether the 'player/entity moved wrongly!' check will be enforced")
    public boolean movedWrongly = true;

    @Setting
    public final PlayerSubCategory player = new PlayerSubCategory();

    @ConfigSerializable
    public static final class PlayerSubCategory {

        @Setting("moved-too-quickly")
        @Comment("Controls whether the 'player moved too quickly!' check will be enforced")
        public boolean movedTooQuickly = true;

        @Setting("vehicle-moved-too-quickly")
        @Comment("Controls whether the 'vehicle of player moved too quickly!' check will be enforced")
        public boolean vehicleMovedTooQuickly = true;
    }
}
