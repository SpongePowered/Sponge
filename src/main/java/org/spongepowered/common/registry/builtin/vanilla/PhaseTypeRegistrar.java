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

import net.minecraft.entity.boss.dragon.phase.PhaseType;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.entity.living.monster.boss.dragon.phase.DragonPhaseType;
import org.spongepowered.common.registry.SpongeCatalogRegistry;

import java.util.stream.Stream;

public final class PhaseTypeRegistrar {

    private PhaseTypeRegistrar() {
    }

    public static void registerRegistry(final SpongeCatalogRegistry registry) {
        registry.generateRegistry(DragonPhaseType.class, ResourceKey.minecraft("dragon_phase_type"), Stream.empty(), false, false);
    }

    public static void registerSuppliers(final SpongeCatalogRegistry registry) {
        registry
            .registerSupplier(DragonPhaseType.class, "holding_pattern", () -> (DragonPhaseType) PhaseType.HOLDING_PATTERN)
            .registerSupplier(DragonPhaseType.class, "strafe_player", () -> (DragonPhaseType) PhaseType.STRAFE_PLAYER)
            .registerSupplier(DragonPhaseType.class, "landing_approach", () -> (DragonPhaseType) PhaseType.LANDING_APPROACH)
            .registerSupplier(DragonPhaseType.class, "landing", () -> (DragonPhaseType) PhaseType.LANDING)
            .registerSupplier(DragonPhaseType.class, "takeoff", () -> (DragonPhaseType) PhaseType.TAKEOFF)
            .registerSupplier(DragonPhaseType.class, "sitting_flaming", () -> (DragonPhaseType) PhaseType.SITTING_FLAMING)
            .registerSupplier(DragonPhaseType.class, "sitting_scanning", () -> (DragonPhaseType) PhaseType.SITTING_SCANNING)
            .registerSupplier(DragonPhaseType.class, "sitting_attacking", () -> (DragonPhaseType) PhaseType.SITTING_ATTACKING)
            .registerSupplier(DragonPhaseType.class, "charging_player", () -> (DragonPhaseType) PhaseType.CHARGING_PLAYER)
            .registerSupplier(DragonPhaseType.class, "dying", () -> (DragonPhaseType) PhaseType.DYING)
            .registerSupplier(DragonPhaseType.class, "hovering", () -> (DragonPhaseType) PhaseType.HOVERING)
        ;
    }
}
