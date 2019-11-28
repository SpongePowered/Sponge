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
package org.spongepowered.common.mixin.api.mcp.inventory;

import com.flowpowered.math.vector.Vector3d;
import net.minecraft.inventory.container.EnchantmentContainer;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.api.item.inventory.Carrier;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.SingleBlockCarrier;
import org.spongepowered.api.item.inventory.type.CarriedInventory;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Specifically to implement the {@link #getInventory()} and {@link #getLocation()}
 * aspects of {@link SingleBlockCarrier} since the remainder of
 * {@link Inventory} implementation is defaulted in {@link SingleBlockCarrier}
 */
@Mixin(EnchantmentContainer.class)
public abstract class ContainerEnchantmentMixin_API implements SingleBlockCarrier {

    @Shadow @Final private net.minecraft.world.World world;
    @Shadow @Final private BlockPos position;

    @Override
    public Location<World> getLocation() {
        return new Location<>(((World) this.world), new Vector3d(this.position.getX(), this.position.getY(), this.position.getZ()));
    }

    @Override
    public World getWorld() {
        return (World) this.world;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public CarriedInventory<? extends Carrier> getInventory() {
        return ((CarriedInventory) this);
    }
}
