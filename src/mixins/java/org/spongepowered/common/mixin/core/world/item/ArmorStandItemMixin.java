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
package org.spongepowered.common.mixin.core.world.item;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ArmorStandItem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ArmorStandItem.class)
public abstract class ArmorStandItemMixin {

    /*
     * Fixes https://bugs.mojang.com/browse/MC-196638, where on attempting to
     * place an armor stand, the method useOn attempts to place the stand down
     * twice. We cancel the first attempt here (this mirrors what 1.17 does).
     *
     * Only valid for 1.16.5, as this is fixed in 1.17+
     */
    @Redirect(method = "useOn",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerLevel;addFreshEntityWithPassengers(Lnet/minecraft/world/entity/Entity;)V"))
    private void impl$cancelInitialArmorStandSpawn(final ServerLevel serverLevel, final Entity entity) {
        // no-op, we do this later.
    }

    /*
     * Fixes https://bugs.mojang.com/browse/MC-196638, in tandem with above.
     * This redirect replaces the spawn call with a spawn with passengers call,
     * which is what we no-opped above, and runs it where 1.17+ runs it.
     *
     * Only valid for 1.16.5, as this is fixed in 1.17+
     */
    @Redirect(method = "useOn",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    private boolean impl$spawnPassengersWithEntity(final Level serverLevel, final Entity entity) {
        ((ServerLevel) serverLevel).addFreshEntityWithPassengers(entity);
        return true;
    }

}
