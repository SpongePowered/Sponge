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
package org.spongepowered.common.gui.window;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.network.play.server.S2DPacketOpenWindow;
import net.minecraft.world.IInteractionObject;
import org.spongepowered.api.gui.window.ContainerWindow;
import org.spongepowered.common.interfaces.entity.player.IMixinEntityPlayerMP;

import java.util.Optional;

public abstract class AbstractSpongeContainerWindow extends AbstractSpongeWindow implements ContainerWindow {

    protected abstract IInteractionObject provideInteractionObject();

    @Override
    public Optional<org.spongepowered.api.item.inventory.Container> getContainer() {
        return Optional.empty();// TODO
                                // Optional.ofNullable((org.spongepowered.api.item.inventory.Container)
                                // this.container);
    }

    @Override
    protected boolean show(EntityPlayerMP player) {
        IInteractionObject obj = provideInteractionObject();
        if (obj == null) {
            return false;
        }
        if (shouldCreateVirtualContainer()) {
            displayVirtualGui(player, obj);
        } else {
            if (obj instanceof IInventory) {
                player.displayGUIChest((IInventory) obj);
            } else {
                player.displayGui(obj);
            }
        }
        return player.openContainer != player.inventoryContainer;
    }

    protected boolean shouldCreateVirtualContainer() {
        return false;
    }

    protected abstract boolean isVirtual();

    // Mostly copied from displayGUIChest, except use createVirtualContainer
    private void displayVirtualGui(EntityPlayerMP player, IInteractionObject obj) {
        int windowId = ((IMixinEntityPlayerMP) player).incrementAndGetWindowId();
        S2DPacketOpenWindow packet;
        if (obj instanceof IInventory) {
            packet = new S2DPacketOpenWindow(windowId, obj.getGuiID(), obj.getDisplayName(), ((InventoryPlayer) obj).getSizeInventory());
        } else {
            packet = new S2DPacketOpenWindow(windowId, obj.getGuiID(), obj.getDisplayName());
        }
        player.playerNetServerHandler.sendPacket(packet);
        player.openContainer = createVirtualContainer(obj, player.inventory, player);
        player.openContainer.windowId = windowId;
        player.openContainer.onCraftGuiOpened(player);
    }

    protected Container createVirtualContainer(IInteractionObject obj, InventoryPlayer inventory, EntityPlayerMP player) {
        return obj.createContainer(inventory, player);
    }

    @Override
    protected void sendClose(EntityPlayerMP player) {
        player.closeScreen();
    }

    @Override
    public boolean canDetectClientClose() {
        return true;
    }

    public boolean onClientClose(EntityPlayerMP player) {
        if (!isVirtual()) {
            return false;
        }
        player.closeContainer();
        onClosed(player);
        return true;
    }

}
