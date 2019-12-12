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
package org.spongepowered.common.mixin.inventory.api.carrier;

import net.minecraft.inventory.container.RepairContainer;
import net.minecraft.util.IWorldPosCallable;
import org.spongepowered.api.item.inventory.Carrier;
import org.spongepowered.api.item.inventory.SingleBlockCarrier;
import org.spongepowered.api.item.inventory.type.CarriedInventory;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.math.vector.Vector3i;

@Mixin(RepairContainer.class)
public abstract class RepairContainerMixin_API implements SingleBlockCarrier {

    @Final @Shadow private IWorldPosCallable field_216980_g;

    @Override
    public Location getLocation() {
        return this.field_216980_g.apply((world, pos) ->
                Location.of(((World) world), new Vector3i(pos.getX(), pos.getY(), pos.getZ()))
        ).orElse(null);
    }

    @Override
    public World getWorld() {
        return this.field_216980_g.apply((world, pos) -> (World) world).orElse(null);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public CarriedInventory<? extends Carrier> getInventory() {
        return (CarriedInventory) this;
    }
}
