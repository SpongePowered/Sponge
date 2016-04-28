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

import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.AnimalChest;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerHorseInventory;
import net.minecraft.network.play.server.S0FPacketSpawnMob;
import net.minecraft.network.play.server.S13PacketDestroyEntities;
import net.minecraft.network.play.server.S2DPacketOpenWindow;
import net.minecraft.world.IInteractionObject;
import org.spongepowered.api.data.manipulator.mutable.entity.HorseData;
import org.spongepowered.api.entity.living.animal.Horse;
import org.spongepowered.api.gui.window.HorseInventoryWindow;
import org.spongepowered.common.interfaces.IMixinEntityPlayerMP;

import java.util.Optional;

public class SpongeHorseInventoryWindow extends AbstractSpongeContainerWindow implements HorseInventoryWindow {

    private EntityHorse horse;
    private HorseData horseData;
    private boolean hasChest;

    @Override
    protected boolean show() {
        if (this.horse != null) {
            this.horse.openGUI(this.player);
        } else {
            this.openVirtualGui();
        }
        return this.player.openContainer != this.player.inventoryContainer;
    }

    @Override
    protected IInteractionObject provideInteractionObject() {
        return null; // Unused
    }

    private void openVirtualGui() {
        // Create horse
        EntityHorse horse = new EntityHorse(this.player.worldObj);
        if (this.horseData != null) {
            ((Horse) horse).offer(this.horseData);
        }
        horse.setChested(this.hasChest);
        AnimalChest inventory = new AnimalChest(horse.getName(), horse.isChested() ? 17 : 2);

        // Spawn horse on client
        this.player.playerNetServerHandler.sendPacket(new S0FPacketSpawnMob(horse));

        // Open window on client and configure container
        int windowId = ((IMixinEntityPlayerMP) this.player).incrementAndGetWindowId();
        this.player.playerNetServerHandler.sendPacket(
                new S2DPacketOpenWindow(windowId, "EntityHorse", horse.getDisplayName(), inventory.getSizeInventory(), horse.getEntityId()));
        this.player.openContainer = createContainerHorse(horse, inventory);
        this.player.openContainer.windowId = windowId;
        this.player.openContainer.onCraftGuiOpened(this.player);

        // Despawn horse on client
        this.player.playerNetServerHandler.sendPacket(new S13PacketDestroyEntities(horse.getEntityId()));
    }

    private Container createContainerHorse(EntityHorse horse, AnimalChest horseInventory) {
        return new ContainerHorseInventory(this.player.inventory, horseInventory, horse, this.player) {

            @Override
            public boolean canInteractWith(EntityPlayer playerIn) {
                return playerIn == SpongeHorseInventoryWindow.this.player;
            }
        };
    }

    @Override
    public Optional<Horse> getHorse() {
        return Optional.ofNullable((Horse) this.horse);
    }

    @Override
    public void setHorse(Horse horse) {
        checkNotOpen();
        this.horse = (EntityHorse) horse;
    }

    @Override
    public void setVirtualHorseData(HorseData horseData) {
        this.horseData = horseData;
        // Unfortunately we can't update the client window 'live' because we had
        // to destroy the entity immediately.
    }

    @Override
    public void setVirtualHasChest(boolean hasChest) {
        this.hasChest = hasChest;
    }

    public static class Builder extends SpongeWindowBuilder<HorseInventoryWindow, HorseInventoryWindow.Builder>
            implements HorseInventoryWindow.Builder {

        @Override
        public HorseInventoryWindow build() {
            return new SpongeHorseInventoryWindow();
        }
    }

}
