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
package org.spongepowered.common.mixin.inventory.api.inventory.container;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.LecternContainer;
import net.minecraft.tileentity.LecternTileEntity;
import org.spongepowered.api.block.entity.BlockEntity;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.inventory.DefaultSingleBlockCarrier;

import java.lang.reflect.Field;

@Mixin(LecternContainer.class)
public abstract class LecternContainerMixin_BlockCarrier_Inventory_API implements DefaultSingleBlockCarrier {

    @Shadow @Final private IInventory lectern;
    private LecternTileEntity impl$lectern;

    @Override
    public ServerLocation getLocation() {
        if (this.impl$lectern == null) {
            try {
                final Field field = this.lectern.getClass().getDeclaredField("this$0");
                field.setAccessible(true);
                this.impl$lectern = (LecternTileEntity) field.get(this.lectern);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
        }
        return ((BlockEntity) this.impl$lectern).getServerLocation();
    }

    @Override
    public World<?, ?> getWorld() {
        return ((BlockEntity) this.lectern).getWorld();
    }

}
