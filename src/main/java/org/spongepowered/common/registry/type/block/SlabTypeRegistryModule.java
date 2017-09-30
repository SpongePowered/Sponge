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
package org.spongepowered.common.registry.type.block;

import net.minecraft.block.BlockStoneSlab;
import net.minecraft.block.BlockStoneSlabNew;
import org.spongepowered.api.data.type.SlabType;
import org.spongepowered.api.data.type.SlabTypes;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.common.registry.type.MinecraftEnumBasedAlternateCatalogTypeRegistryModule;

@RegisterCatalog(SlabTypes.class)
public final class SlabTypeRegistryModule extends MinecraftEnumBasedAlternateCatalogTypeRegistryModule<BlockStoneSlab.EnumType, SlabType> {

    public SlabTypeRegistryModule() {
        super(new String[] {"minecraft"}, // TODO - This needs to play into our alias system.
                id -> id.equals("nether_brick")
                      ? "netherbrick"
                      : id.equals("sandstone")
                        ? "sand"
                        : id.equals("stone_brick")
                          ? "smooth_brick"
                          : id.equals("wood_old")
                            ? "wood"
                            : id.equals("red_sandstone")
                                ? "red_sand"
                                : id);
    }

    @Override
    protected void generateInitialMap() {
        super.generateInitialMap();
        final SlabType redSandstone = (SlabType) (Object) BlockStoneSlabNew.EnumType.RED_SANDSTONE;

        this.catalogTypeMap.put(redSandstone.getId(), redSandstone);
    }

    @Override
    protected BlockStoneSlab.EnumType[] getValues() {
        return BlockStoneSlab.EnumType.values();
    }

}
