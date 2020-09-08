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

import net.minecraft.entity.villager.IVillagerType;
import org.spongepowered.api.data.type.VillagerType;
import org.spongepowered.common.registry.SpongeCatalogRegistry;

public final class VillagerTypeSupplier {

    private VillagerTypeSupplier() {
    }

    public static void registerSuppliers(final SpongeCatalogRegistry registry) {
        registry.registerSupplier(VillagerType.class, "desert", () -> (VillagerType) IVillagerType.DESERT)
                .registerSupplier(VillagerType.class, "jungle", () -> (VillagerType) IVillagerType.JUNGLE)
                .registerSupplier(VillagerType.class, "plains", () -> (VillagerType) IVillagerType.PLAINS)
                .registerSupplier(VillagerType.class, "savanna", () -> (VillagerType) IVillagerType.SAVANNA)
                .registerSupplier(VillagerType.class, "snow", () -> (VillagerType) IVillagerType.SNOW)
                .registerSupplier(VillagerType.class, "swamp", () -> (VillagerType) IVillagerType.SWAMP)
                .registerSupplier(VillagerType.class, "taiga", () -> (VillagerType) IVillagerType.TAIGA);
    }
}
