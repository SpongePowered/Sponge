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
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IInteractionObject;
import org.spongepowered.api.gui.window.TileBackedWindow;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Optional;

public abstract class AbstractSpongeTileContainerWindow<McTile extends TileEntity & IInteractionObject, ApiTile extends org.spongepowered.api.block.tileentity.TileEntity>
        extends AbstractSpongeContainerWindow implements TileBackedWindow<ApiTile> {

    private final Class<McTile> tileClass;
    protected McTile tileEntity;
    protected McTile virtualTile;
    protected Location<World> loc;

    public AbstractSpongeTileContainerWindow(Class<McTile> tileClass) {
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

    protected abstract McTile createVirtualTile();

    @Override
    protected void onClosed(EntityPlayerMP player) {
        this.virtualTile = null;
        super.onClosed(player);
    }

    @Override
    protected boolean isVirtual() {
        return this.tileEntity == null && this.loc == null;
    }

    protected void updateIfVirtual() {
        if (!isVirtual()) {
            return;
        }
        if (!this.players.isEmpty() && this.virtualTile != null) {
            updateVirtual();
            if (this.virtualTile instanceof IInventory) {
                for (EntityPlayerMP player : this.players) {
                    player.sendAllWindowProperties(player.openContainer, (IInventory) this.virtualTile);
                }
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
    public void setTileEntity(ApiTile tileEntity) {
        checkNotOpen();
        this.tileEntity = (McTile) tileEntity;
        this.loc = tileEntity == null ? null : tileEntity.getLocation();
    }

}
