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
package org.spongepowered.common.item.inventory.custom;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.IInteractionObject;
import org.spongepowered.api.item.inventory.Carrier;
import org.spongepowered.api.item.inventory.custom.ContainerType;
import org.spongepowered.common.data.type.SpongeContainerTypeEmpty;

import java.util.UUID;

import javax.annotation.Nullable;

public class EmptyViewableCustomInventory extends InventoryBasic implements IInteractionObject {

    private ContainerType type;
    @Nullable private UUID identity; // TODO property
    @Nullable private Carrier carrier; // for shadow

    public EmptyViewableCustomInventory(SpongeContainerTypeEmpty type, @Nullable UUID identity, @Nullable Carrier carrier) {
        super(new TextComponentString("Empty Viewable Custom Inventory"), 0);
        this.type = type;
        this.identity = identity;
        this.carrier = carrier;
    }

    @Override
    public Container createContainer(InventoryPlayer inventoryPlayer, EntityPlayer player) {
        return ((SpongeContainerTypeEmpty) this.type).provideContainer(this, player);
    }

    @Override
    public String getGuiID() {
        return this.type.getKey().toString();
    }

}
