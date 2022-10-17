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
package org.spongepowered.common.accessor.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.state.properties.StructureMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(StructureBlockEntity.class)
public interface StructureBlockEntityAccessor {

    @Accessor("author") String accessor$author();

    @Accessor("author") void accessor$author(final String author);

    @Accessor("structurePos") BlockPos accessor$structurePos();

    @Accessor("structurePos") void accessor$structurePos(final BlockPos structurePos);

    @Accessor("structureSize") Vec3i accessor$structureSize();

    @Accessor("structureSize") void accessor$structureSize(final Vec3i structureSize);

    @Accessor("mode") StructureMode accessor$mode();

    @Accessor("ignoreEntities") boolean accessor$ignoreEntities();

    @Accessor("showAir") boolean accessor$showAir();

    @Accessor("showBoundingBox") boolean accessor$showBoundingBox();

    @Accessor("integrity") float accessor$integrity();

    @Accessor("seed") long accessor$seed();

}
