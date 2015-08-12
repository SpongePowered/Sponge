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
package org.spongepowered.common.mixin.core.block.tiles;

import static org.spongepowered.api.data.DataQuery.of;

import net.minecraft.command.ICommandSender;
import net.minecraft.command.server.CommandBlockLogic;
import org.spongepowered.api.block.tileentity.CommandBlock;
import org.spongepowered.api.block.tileentity.TileEntityType;
import org.spongepowered.api.block.tileentity.TileEntityTypes;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.interfaces.IMixinCommandSource;

@NonnullByDefault
@Mixin(net.minecraft.tileentity.TileEntityCommandBlock.class)
public abstract class MixinTileEntityCommandBlock extends MixinTileEntity implements CommandBlock, IMixinCommandSource {

    @Shadow public abstract CommandBlockLogic getCommandBlockLogic();

    @Override
    public ICommandSender asICommandSender() {
        return getCommandBlockLogic();
    }

    @Override
    public void execute() {
        getCommandBlockLogic().trigger(this.worldObj);
    }

    @Override
    public TileEntityType getType() {
        return TileEntityTypes.COMMAND_BLOCK;
    }

    @Override
    @SuppressWarnings("deprecated")
    public DataContainer toContainer() {
        DataContainer container = super.toContainer();
        container.set(of("StoredCommand"), this.getCommandBlockLogic().commandStored);
        container.set(of("SuccessCount"), this.getCommandBlockLogic().successCount);
        container.set(of("CustomName"), this.getCommandBlockLogic().getCustomName());
        container.set(of("DoesTrackOutput"), this.getCommandBlockLogic().shouldTrackOutput());
        if (this.getCommandBlockLogic().shouldTrackOutput()) {
        }
        return container;
    }
}
