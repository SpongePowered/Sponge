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
package org.spongepowered.common.data.builder.block.tileentity;

import net.minecraft.tileentity.CommandBlockBaseLogic;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityCommandBlock;
import org.spongepowered.api.block.tileentity.CommandBlock;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.common.text.SpongeTexts;
import org.spongepowered.common.util.Constants;

import java.util.Optional;

public class SpongeCommandBlockBuilder extends AbstractTileBuilder<CommandBlock> {

    public SpongeCommandBlockBuilder() {
        super(CommandBlock.class, 1);
    }

    @Override
    protected Optional<CommandBlock> buildContent(final DataView container) throws InvalidDataException {
        return super.buildContent(container).flatMap(commandBlock -> {
            if (!container.contains(
                Constants.TileEntity.CommandBlock.STORED_COMMAND, Constants.TileEntity.CommandBlock.SUCCESS_COUNT, Constants.TileEntity.CommandBlock.DOES_TRACK_OUTPUT)) {
                ((TileEntity) commandBlock).func_145843_s();
                return Optional.empty();
            }
            final CommandBlockBaseLogic cmdBlockLogic = ((TileEntityCommandBlock) commandBlock).func_145993_a();
            cmdBlockLogic.func_145752_a(container.getString(Constants.TileEntity.CommandBlock.STORED_COMMAND).get());
            cmdBlockLogic.func_184167_a(container.getInt(Constants.TileEntity.CommandBlock.SUCCESS_COUNT).get());
            cmdBlockLogic.func_175573_a(container.getBoolean(Constants.TileEntity.CommandBlock.DOES_TRACK_OUTPUT).get());
            if (cmdBlockLogic.func_175571_m()) {
                cmdBlockLogic.func_145750_b(SpongeTexts.toComponent(SpongeTexts.fromLegacy(
                        container.getString(Constants.TileEntity.CommandBlock.TRACKED_OUTPUT).get())));
            }
            ((TileEntityCommandBlock)commandBlock).func_145829_t();
            return Optional.of(commandBlock);
        });
    }
}
