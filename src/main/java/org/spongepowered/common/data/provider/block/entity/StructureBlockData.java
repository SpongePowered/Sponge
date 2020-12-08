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

import net.minecraft.tileentity.StructureBlockTileEntity;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.StructureMode;
import org.spongepowered.common.accessor.tileentity.StructureBlockTileEntityAccessor;
import org.spongepowered.common.data.provider.DataProviderRegistrator;
import org.spongepowered.common.util.VecHelper;

public final class StructureBlockData {

    private StructureBlockData() {
    }

    // @formatter:off
    public static void register(final DataProviderRegistrator registrator) {
        registrator
                .asMutable(StructureBlockTileEntity.class)
                    .create(Keys.STRUCTURE_IGNORE_ENTITIES)
                        .get(h -> ((StructureBlockTileEntityAccessor) h).accessor$ignoreEntities())
                        .set(StructureBlockTileEntity::setIgnoresEntities)
                    .create(Keys.STRUCTURE_INTEGRITY)
                        .get(h -> (double) ((StructureBlockTileEntityAccessor) h).accessor$integrity())
                        .set((h, v) -> h.setIntegrity(v.floatValue()))
                    .create(Keys.STRUCTURE_MODE)
                        .get(h -> (StructureMode) (Object) ((StructureBlockTileEntityAccessor) h).accessor$mode())
                        .set((h, v) -> h.setMode((net.minecraft.state.properties.StructureMode) (Object) v))
                    .create(Keys.STRUCTURE_POWERED)
                        .get(StructureBlockTileEntity::isPowered)
                        .set(StructureBlockTileEntity::setPowered)
                    .create(Keys.STRUCTURE_SEED)
                        .get(h -> ((StructureBlockTileEntityAccessor) h).accessor$seed())
                        .set(StructureBlockTileEntity::setSeed)
                    .create(Keys.STRUCTURE_SHOW_AIR)
                        .get(h -> ((StructureBlockTileEntityAccessor) h).accessor$showAir())
                        .set(StructureBlockTileEntity::setShowAir)
                    .create(Keys.STRUCTURE_SHOW_BOUNDING_BOX)
                        .get(h -> ((StructureBlockTileEntityAccessor) h).accessor$showBoundingBox())
                        .set(StructureBlockTileEntity::setShowBoundingBox)
                .asMutable(StructureBlockTileEntityAccessor.class)
                    .create(Keys.STRUCTURE_AUTHOR)
                        .get(StructureBlockTileEntityAccessor::accessor$author)
                        .set(StructureBlockTileEntityAccessor::accessor$author)
                    .create(Keys.STRUCTURE_POSITION)
                        .get(h -> VecHelper.toVector3i(h.accessor$structurePos()))
                        .set((h, v) -> h.accessor$structurePos(VecHelper.toBlockPos(v)))
                    .create(Keys.STRUCTURE_SIZE)
                        .get(h -> VecHelper.toVector3i(h.accessor$structureSize()))
                        .set((h, v) -> h.accessor$structureSize(VecHelper.toBlockPos(v)));
    }
    // @formatter:on
}
