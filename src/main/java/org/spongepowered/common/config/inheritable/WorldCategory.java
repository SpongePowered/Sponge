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
public final class WorldCategory {

    @Setting("item-merge-radius")
    @Comment("The defined merge radius for Item entities such that when two items are \n"
        + "within the defined radius of each other, they will attempt to merge. Usually, \n"
        + "the default radius is set to 0.5 in Vanilla, however, for performance reasons \n"
        + "2.5 is generally acceptable. \n"
        + "Note: Increasing the radius higher will likely cause performance degradation \n"
        + "with larger amount of items as they attempt to merge and search nearby \n"
        + "areas for more items. Setting to a negative value is not supported!")
    public final double itemMergeRadius = 2.5D;

    @Setting("auto-save-interval")
    @Comment("The auto-save tick interval used to save all loaded chunks in a world. \n"
        + "Set to 0 to disable. (Default: 6000) \n"
        + "Note: 20 ticks is equivalent to 1 second.")
    public int autoSaveInterval = 6000;

    @Setting("log-auto-save")
    @Comment("Log when a world auto-saves its chunk data. Note: This may be spammy depending on the auto-save-interval configured for world.")
    public final boolean logAutoSave = false;
}
