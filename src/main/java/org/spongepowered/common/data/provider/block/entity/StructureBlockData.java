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
package org.spongepowered.common.data.provider.block.entity;

import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.StructureMode;
import org.spongepowered.common.accessor.world.level.block.entity.StructureBlockEntityAccessor;
import org.spongepowered.common.data.provider.DataProviderRegistrator;
import org.spongepowered.common.util.VecHelper;

import net.minecraft.world.level.block.entity.StructureBlockEntity;

public final class StructureBlockData {

    private StructureBlockData() {
    }

    // @formatter:off
    public static void register(final DataProviderRegistrator registrator) {
        registrator
                .asMutable(StructureBlockEntity.class)
                    .create(Keys.STRUCTURE_IGNORE_ENTITIES)
                        .get(h -> ((StructureBlockEntityAccessor) h).accessor$ignoreEntities())
                        .set(StructureBlockEntity::setIgnoreEntities)
                    .create(Keys.STRUCTURE_INTEGRITY)
                        .get(h -> (double) ((StructureBlockEntityAccessor) h).accessor$integrity())
                        .set((h, v) -> h.setIntegrity(v.floatValue()))
                    .create(Keys.STRUCTURE_MODE)
                        .get(h -> (StructureMode) (Object) ((StructureBlockEntityAccessor) h).accessor$mode())
                        .set((h, v) -> h.setMode((net.minecraft.world.level.block.state.properties.StructureMode) (Object) v))
                    .create(Keys.STRUCTURE_POWERED)
                        .get(StructureBlockEntity::isPowered)
                        .set(StructureBlockEntity::setPowered)
                    .create(Keys.STRUCTURE_SEED)
                        .get(h -> ((StructureBlockEntityAccessor) h).accessor$seed())
                        .set(StructureBlockEntity::setSeed)
                    .create(Keys.STRUCTURE_SHOW_AIR)
                        .get(h -> ((StructureBlockEntityAccessor) h).accessor$showAir())
                        .set(StructureBlockEntity::setShowAir)
                    .create(Keys.STRUCTURE_SHOW_BOUNDING_BOX)
                        .get(h -> ((StructureBlockEntityAccessor) h).accessor$showBoundingBox())
                        .set(StructureBlockEntity::setShowBoundingBox)
                .asMutable(StructureBlockEntityAccessor.class)
                    .create(Keys.STRUCTURE_AUTHOR)
                        .get(StructureBlockEntityAccessor::accessor$author)
                        .set(StructureBlockEntityAccessor::accessor$author)
                    .create(Keys.STRUCTURE_POSITION)
                        .get(h -> VecHelper.toVector3i(h.accessor$structurePos()))
                        .set((h, v) -> h.accessor$structurePos(VecHelper.toBlockPos(v)))
                    .create(Keys.STRUCTURE_SIZE)
                        .get(h -> VecHelper.toVector3i(h.accessor$structureSize()))
                        .set((h, v) -> h.accessor$structureSize(VecHelper.toBlockPos(v)));
    }
    // @formatter:on
}
