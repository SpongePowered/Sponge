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
package org.spongepowered.common.registry.builtin.supplier;

import net.minecraft.entity.merchant.villager.VillagerProfession;
import org.spongepowered.api.data.type.Profession;
import org.spongepowered.common.registry.SpongeCatalogRegistry;

public final class VillagerProfessionSupplier {

    private VillagerProfessionSupplier() {
    }

    public static void registerSuppliers(SpongeCatalogRegistry registry) {
        registry
            .registerSupplier(Profession.class, "none", () -> (Profession) VillagerProfession.NONE)
            .registerSupplier(Profession.class, "armorer", () -> (Profession) VillagerProfession.ARMORER)
            .registerSupplier(Profession.class, "butcher", () -> (Profession) VillagerProfession.BUTCHER)
            .registerSupplier(Profession.class, "cartographer", () -> (Profession) VillagerProfession.CARTOGRAPHER)
            .registerSupplier(Profession.class, "cleric", () -> (Profession) VillagerProfession.CLERIC)
            .registerSupplier(Profession.class, "farmer", () -> (Profession) VillagerProfession.FARMER)
            .registerSupplier(Profession.class, "fisherman", () -> (Profession) VillagerProfession.FISHERMAN)
            .registerSupplier(Profession.class, "fletcher", () -> (Profession) VillagerProfession.FLETCHER)
            .registerSupplier(Profession.class, "leatherworker", () -> (Profession) VillagerProfession.LEATHERWORKER)
            .registerSupplier(Profession.class, "librarian", () -> (Profession) VillagerProfession.LIBRARIAN)
            .registerSupplier(Profession.class, "mason", () -> (Profession) VillagerProfession.MASON)
            .registerSupplier(Profession.class, "nitwit", () -> (Profession) VillagerProfession.NITWIT)
            .registerSupplier(Profession.class, "shepherd", () -> (Profession) VillagerProfession.SHEPHERD)
            .registerSupplier(Profession.class, "toolsmith", () -> (Profession) VillagerProfession.TOOLSMITH)
            .registerSupplier(Profession.class, "weaponsmith", () -> (Profession) VillagerProfession.WEAPONSMITH)
        ;
    }
}
