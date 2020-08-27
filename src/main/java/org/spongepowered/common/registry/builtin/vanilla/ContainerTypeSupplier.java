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

import org.spongepowered.api.item.inventory.ContainerType;
import org.spongepowered.common.registry.SpongeCatalogRegistry;

public final class ContainerTypeSupplier {

    private ContainerTypeSupplier() {
    }

    public static void registerSuppliers(final SpongeCatalogRegistry registry) {
        registry
            .registerSupplier(ContainerType.class, "generic_9x1", () -> (ContainerType) net.minecraft.inventory.container.ContainerType.GENERIC_9X1)
            .registerSupplier(ContainerType.class, "generic_9x2", () -> (ContainerType) net.minecraft.inventory.container.ContainerType.GENERIC_9X2)
            .registerSupplier(ContainerType.class, "generic_9x3", () -> (ContainerType) net.minecraft.inventory.container.ContainerType.GENERIC_9X3)
            .registerSupplier(ContainerType.class, "generic_9x4", () -> (ContainerType) net.minecraft.inventory.container.ContainerType.GENERIC_9X4)
            .registerSupplier(ContainerType.class, "generic_9x5", () -> (ContainerType) net.minecraft.inventory.container.ContainerType.GENERIC_9X5)
            .registerSupplier(ContainerType.class, "generic_9x6", () -> (ContainerType) net.minecraft.inventory.container.ContainerType.GENERIC_9X6)
            .registerSupplier(ContainerType.class, "generic_3x3", () -> (ContainerType) net.minecraft.inventory.container.ContainerType.GENERIC_3X3)
            .registerSupplier(ContainerType.class, "anvil", () -> (ContainerType) net.minecraft.inventory.container.ContainerType.ANVIL)
            .registerSupplier(ContainerType.class, "beacon", () -> (ContainerType) net.minecraft.inventory.container.ContainerType.BEACON)
            .registerSupplier(ContainerType.class, "blast_furnace", () -> (ContainerType) net.minecraft.inventory.container.ContainerType.BLAST_FURNACE)
            .registerSupplier(ContainerType.class, "brewing_stand", () -> (ContainerType) net.minecraft.inventory.container.ContainerType.BREWING_STAND)
            .registerSupplier(ContainerType.class, "crafting", () -> (ContainerType) net.minecraft.inventory.container.ContainerType.CRAFTING)
            .registerSupplier(ContainerType.class, "enchantment", () -> (ContainerType) net.minecraft.inventory.container.ContainerType.ENCHANTMENT)
            .registerSupplier(ContainerType.class, "furnace", () -> (ContainerType) net.minecraft.inventory.container.ContainerType.FURNACE)
            .registerSupplier(ContainerType.class, "grindstone", () -> (ContainerType) net.minecraft.inventory.container.ContainerType.GRINDSTONE)
            .registerSupplier(ContainerType.class, "hopper", () -> (ContainerType) net.minecraft.inventory.container.ContainerType.HOPPER)
            .registerSupplier(ContainerType.class, "lectern", () -> (ContainerType) net.minecraft.inventory.container.ContainerType.LECTERN)
            .registerSupplier(ContainerType.class, "loom", () -> (ContainerType) net.minecraft.inventory.container.ContainerType.LOOM)
            .registerSupplier(ContainerType.class, "merchant", () -> (ContainerType) net.minecraft.inventory.container.ContainerType.MERCHANT)
            .registerSupplier(ContainerType.class, "shulker_box", () -> (ContainerType) net.minecraft.inventory.container.ContainerType.SHULKER_BOX)
            .registerSupplier(ContainerType.class, "smoker", () -> (ContainerType) net.minecraft.inventory.container.ContainerType.SMOKER)
            .registerSupplier(ContainerType.class, "cartography_table", () -> (ContainerType) net.minecraft.inventory.container.ContainerType.CARTOGRAPHY_TABLE)
            .registerSupplier(ContainerType.class, "stonecutter", () -> (ContainerType) net.minecraft.inventory.container.ContainerType.STONECUTTER)
        ;
    }
}
