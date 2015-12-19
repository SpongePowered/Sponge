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

import net.minecraft.command.server.CommandBlockLogic;
import net.minecraft.tileentity.TileEntityCommandBlock;
import org.spongepowered.api.Game;
import org.spongepowered.api.block.tileentity.CommandBlock;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.util.persistence.InvalidDataException;
import org.spongepowered.common.data.util.DataQueries;
import org.spongepowered.common.text.SpongeTexts;

import java.util.Optional;

public class SpongeCommandBlockBuilder extends AbstractTileBuilder<CommandBlock> {

    public SpongeCommandBlockBuilder(Game game) {
        super(game);
    }

    @Override
    @SuppressWarnings({"unchecked", "deprecation"})
    public Optional<CommandBlock> build(DataView container) throws InvalidDataException {
        Optional<CommandBlock> commandblockOptional = super.build(container);
        if (!commandblockOptional.isPresent()) {
            throw new InvalidDataException("The container had insufficient data to create a CommandBlock tile entity!");
        }
        CommandBlock commandblock = commandblockOptional.get();
        if (!container.contains(DataQueries.STORED_COMMAND)
                || !container.contains(DataQueries.SUCCESS_COUNT)
                || !container.contains(DataQueries.DOES_TRACK_OUTPUT)) {
            throw new InvalidDataException("The provided container does not contain the data to make a CommandBlock!");
        }
        CommandBlockLogic cmdBlockLogic = ((TileEntityCommandBlock) commandblock).getCommandBlockLogic();
        cmdBlockLogic.setCommand(container.getString(DataQueries.STORED_COMMAND).get());
        cmdBlockLogic.successCount = container.getInt(DataQueries.SUCCESS_COUNT).get();
        cmdBlockLogic.setTrackOutput(container.getBoolean(DataQueries.DOES_TRACK_OUTPUT).get());
        if (cmdBlockLogic.shouldTrackOutput()) {
            cmdBlockLogic.setLastOutput(SpongeTexts.toComponent(Texts.legacy().fromUnchecked(container.getString(DataQueries.TRACKED_OUTPUT).get())));
        }
        ((TileEntityCommandBlock)commandblock).validate();
        return Optional.of(commandblock);
    }
}
