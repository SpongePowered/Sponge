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
package org.spongepowered.common.data.manipulator.mutable.entity;

import org.spongepowered.api.data.key.Keys;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeSkeletonData;
import org.spongepowered.common.data.manipulator.mutable.common.AbstractSingleCatalogData;

@SuppressWarnings("deprecation")
public class SpongeSkeletonData extends AbstractSingleCatalogData<org.spongepowered.api.data.type.SkeletonType,
    org.spongepowered.api.data.manipulator.mutable.entity.SkeletonData, org.spongepowered.api.data.manipulator.immutable.entity.ImmutableSkeletonData>
    implements org.spongepowered.api.data.manipulator.mutable.entity.SkeletonData {

    public SpongeSkeletonData(org.spongepowered.api.data.type.SkeletonType value) {
        super(org.spongepowered.api.data.manipulator.mutable.entity.SkeletonData.class, value, Keys.SKELETON_TYPE, ImmutableSpongeSkeletonData.class);
    }

    public SpongeSkeletonData() {
        this(org.spongepowered.api.data.type.SkeletonTypes.NORMAL);
    }

}
