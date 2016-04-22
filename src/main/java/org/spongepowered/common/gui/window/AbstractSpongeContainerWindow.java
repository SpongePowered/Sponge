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
import net.minecraft.network.Packet;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraft.network.play.server.S2DPacketOpenWindow;
import net.minecraft.world.IInteractionObject;
import org.spongepowered.api.gui.window.ContainerWindow;
import org.spongepowered.common.interfaces.IMixinEntityPlayerMP;

import java.util.Optional;

public abstract class AbstractSpongeContainerWindow extends AbstractSpongeWindow implements ContainerWindow {

    protected abstract IInteractionObject provideInteractionObject();

    @Override
    public Optional<org.spongepowered.api.item.inventory.Container> getContainer() {
        if (this.player == null || this.player.openContainer == this.player.inventoryContainer) {
            return Optional.empty();
        }
        return Optional.of((org.spongepowered.api.item.inventory.Container) this.player.openContainer);
    }

    @Override
    protected boolean show() {
        IInteractionObject obj = provideInteractionObject();
        if (obj == null) {
            return false;
        }
        if (shouldCreateVirtualContainer()) {
            displayVirtualGui(obj);
        } else {
            if (obj instanceof IInventory) {
                this.player.displayGUIChest((IInventory) obj);
            } else {
                this.player.displayGui(obj);
            }
        }
        return this.player.openContainer != this.player.inventoryContainer;
    }

    protected boolean shouldCreateVirtualContainer() {
        return false;
    }

    // Mostly copied from displayGUIChest, except use createVirtualContainer
    private void displayVirtualGui(IInteractionObject obj) {
        int windowId = ((IMixinEntityPlayerMP) this.player).incrementWindowId();
        S2DPacketOpenWindow packet;
        if (obj instanceof IInventory) {
            packet = new S2DPacketOpenWindow(windowId, obj.getGuiID(), obj.getDisplayName(), ((InventoryPlayer) obj).getSizeInventory());
        } else {
            packet = new S2DPacketOpenWindow(windowId, obj.getGuiID(), obj.getDisplayName());
        }
        this.player.playerNetServerHandler.sendPacket(packet);
        this.player.openContainer = createVirtualContainer(obj, this.player.inventory, this.player);
        this.player.openContainer.windowId = windowId;
        this.player.openContainer.onCraftGuiOpened(this.player);
    }

    protected Container createVirtualContainer(IInteractionObject obj, InventoryPlayer inventory, EntityPlayerMP player) {
        return obj.createContainer(inventory, player);
    }

    @Override
    protected boolean close() {
        this.player.closeScreen();
        return true;
    }

    @Override
    public boolean canDetectClientClose() {
        return true;
    }

    @Override
    public void onClientClose(Packet<INetHandlerPlayServer> packet) {
        this.player.closeContainer();
        super.onClientClose(packet);
    }

}
