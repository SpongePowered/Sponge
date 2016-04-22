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

import net.minecraft.inventory.IInventory;
import net.minecraft.network.Packet;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IInteractionObject;
import org.spongepowered.api.gui.window.TileBackedWindow;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Optional;

public abstract class AbstractSpongeTileContainerWindow<T1 extends TileEntity & IInteractionObject, T2 extends org.spongepowered.api.block.tileentity.TileEntity>
        extends AbstractSpongeContainerWindow implements TileBackedWindow<T2> {

    private final Class<T1> tileClass;
    protected T1 tileEntity;
    protected T1 virtualTile;
    protected Location<World> loc;

    public AbstractSpongeTileContainerWindow(Class<T1> tileClass) {
        this.tileClass = tileClass;
    }

    @Override
    protected IInteractionObject provideInteractionObject() {
        if (this.tileEntity != null) {
            return this.tileEntity;
        }
        if (this.loc != null) {
            Optional<org.spongepowered.api.block.tileentity.TileEntity> te = this.loc.getTileEntity();
            if (te.isPresent() && this.tileClass.isInstance(te.get())) {
                return (IInteractionObject) te.get();
            }
            return null;
        }
        this.virtualTile = createVirtualTile();
        updateVirtual();
        return this.virtualTile;
    }

    protected abstract T1 createVirtualTile();

    @Override
    public void onClientClose(Packet<INetHandlerPlayServer> packet) {
        this.virtualTile = null;
        super.onClientClose(packet);
    }

    protected boolean isVirtual() {
        return this.tileEntity == null && this.loc == null;
    }

    protected void updateIfVirtual() {
        if (!isVirtual()) {
            return;
        }
        if (this.player != null && this.virtualTile != null) {
            updateVirtual();
            if (this.virtualTile instanceof IInventory) {
                this.player.sendAllWindowProperties(this.player.openContainer, (IInventory) this.virtualTile);
            }
        }
    }

    protected void updateVirtual() {
    }

    @Override
    public Optional<Location<World>> getLocation() {
        return Optional.ofNullable(this.loc);
    }

    @Override
    public void setLocation(Location<World> location) {
        checkNotOpen();
        this.loc = location;
        this.tileEntity = null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setTileEntity(T2 tileEntity) {
        checkNotOpen();
        this.tileEntity = (T1) tileEntity;
        this.loc = tileEntity == null ? null : tileEntity.getLocation();
    }

}
