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
package org.spongepowered.common.registry.builtin.vanilla;

import net.minecraft.tileentity.TileEntityType;
import org.spongepowered.api.block.entity.BlockEntityType;
import org.spongepowered.common.registry.SpongeCatalogRegistry;

public final class TileEntityTypeSupplier {

    private TileEntityTypeSupplier() {
    }

    public static void registerSuppliers(SpongeCatalogRegistry registry) {
        registry
            .registerSupplier(BlockEntityType.class, "furnace", () -> (BlockEntityType) TileEntityType.FURNACE)
            .registerSupplier(BlockEntityType.class, "chest", () -> (BlockEntityType) TileEntityType.CHEST)
            .registerSupplier(BlockEntityType.class, "trapped_chest", () -> (BlockEntityType) TileEntityType.TRAPPED_CHEST)
            .registerSupplier(BlockEntityType.class, "ender_chest", () -> (BlockEntityType) TileEntityType.ENDER_CHEST)
            .registerSupplier(BlockEntityType.class, "jukebox", () -> (BlockEntityType) TileEntityType.JUKEBOX)
            .registerSupplier(BlockEntityType.class, "dispenser", () -> (BlockEntityType) TileEntityType.DISPENSER)
            .registerSupplier(BlockEntityType.class, "dropper", () -> (BlockEntityType) TileEntityType.DROPPER)
            .registerSupplier(BlockEntityType.class, "sign", () -> (BlockEntityType) TileEntityType.SIGN)
            .registerSupplier(BlockEntityType.class, "mob_spawner", () -> (BlockEntityType) TileEntityType.MOB_SPAWNER)
            .registerSupplier(BlockEntityType.class, "piston", () -> (BlockEntityType) TileEntityType.PISTON)
            .registerSupplier(BlockEntityType.class, "brewing_stand", () -> (BlockEntityType) TileEntityType.BREWING_STAND)
            .registerSupplier(BlockEntityType.class, "enchanting_table", () -> (BlockEntityType) TileEntityType.ENCHANTING_TABLE)
            .registerSupplier(BlockEntityType.class, "end_portal", () -> (BlockEntityType) TileEntityType.END_PORTAL)
            .registerSupplier(BlockEntityType.class, "beacon", () -> (BlockEntityType) TileEntityType.BEACON)
            .registerSupplier(BlockEntityType.class, "skull", () -> (BlockEntityType) TileEntityType.SKULL)
            .registerSupplier(BlockEntityType.class, "daylight_detector", () -> (BlockEntityType) TileEntityType.DAYLIGHT_DETECTOR)
            .registerSupplier(BlockEntityType.class, "hopper", () -> (BlockEntityType) TileEntityType.HOPPER)
            .registerSupplier(BlockEntityType.class, "comparator", () -> (BlockEntityType) TileEntityType.COMPARATOR)
            .registerSupplier(BlockEntityType.class, "banner", () -> (BlockEntityType) TileEntityType.BANNER)
            .registerSupplier(BlockEntityType.class, "structure_block", () -> (BlockEntityType) TileEntityType.STRUCTURE_BLOCK)
            .registerSupplier(BlockEntityType.class, "end_gateway", () -> (BlockEntityType) TileEntityType.END_GATEWAY)
            .registerSupplier(BlockEntityType.class, "command_block", () -> (BlockEntityType) TileEntityType.COMMAND_BLOCK)
            .registerSupplier(BlockEntityType.class, "shulker_box", () -> (BlockEntityType) TileEntityType.SHULKER_BOX)
            .registerSupplier(BlockEntityType.class, "bed", () -> (BlockEntityType) TileEntityType.BED)
            .registerSupplier(BlockEntityType.class, "conduit", () -> (BlockEntityType) TileEntityType.CONDUIT)
            .registerSupplier(BlockEntityType.class, "barrel", () -> (BlockEntityType) TileEntityType.BARREL)
            .registerSupplier(BlockEntityType.class, "smoker", () -> (BlockEntityType) TileEntityType.SMOKER)
            .registerSupplier(BlockEntityType.class, "blast_furnace", () -> (BlockEntityType) TileEntityType.BLAST_FURNACE)
            .registerSupplier(BlockEntityType.class, "lectern", () -> (BlockEntityType) TileEntityType.LECTERN)
            .registerSupplier(BlockEntityType.class, "bell", () -> (BlockEntityType) TileEntityType.BELL)
            .registerSupplier(BlockEntityType.class, "jigsaw", () -> (BlockEntityType) TileEntityType.JIGSAW)
            .registerSupplier(BlockEntityType.class, "campfire", () -> (BlockEntityType) TileEntityType.CAMPFIRE)
        ;
    }
}
