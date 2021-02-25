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
package org.spongepowered.common.data.provider.entity;

import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.npc.VillagerProfession;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.ProfessionType;
import org.spongepowered.api.data.type.VillagerType;
import org.spongepowered.common.data.provider.DataProviderRegistrator;

public final class ZombieVillagerData {

    private ZombieVillagerData() {
    }

    // @formatter:off
    public static void register(final DataProviderRegistrator registrator) {
        registrator
                .asMutable(ZombieVillager.class)
                    .create(Keys.PROFESSION_LEVEL)
                        .get(h -> h.getVillagerData().getLevel())
                        .set((h, v) -> h.setVillagerData(h.getVillagerData().setLevel(v)))
                    .create(Keys.PROFESSION_TYPE)
                        .get(h -> (ProfessionType) h.getVillagerData().getProfession())
                        .set((h, v) -> h.setVillagerData(h.getVillagerData().setProfession((VillagerProfession) v)))
                    .create(Keys.VILLAGER_TYPE)
                        .get(h -> (VillagerType) (Object) h.getVillagerData().getType())
                        .set((h, v) -> h.setVillagerData(h.getVillagerData().setType((net.minecraft.world.entity.npc.VillagerType) (Object) v)));
    }
    // @formatter:on
}
