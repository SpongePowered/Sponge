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
package org.spongepowered.common.data.manipulator.immutable;

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.ImmutableCommandData;
import org.spongepowered.api.data.manipulator.mutable.CommandData;
import org.spongepowered.api.data.value.immutable.ImmutableOptionalValue;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;
import org.spongepowered.common.data.manipulator.immutable.common.AbstractImmutableData;
import org.spongepowered.common.data.manipulator.mutable.SpongeCommandData;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeOptionalValue;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;

import java.util.Optional;

public class ImmutableSpongeCommandData extends AbstractImmutableData<ImmutableCommandData, CommandData> implements ImmutableCommandData {

    private final String storedCommand;
    private final int success;
    private final boolean tracks;
    private final Text lastOutput;

    public ImmutableSpongeCommandData(String storedCommand, int success, boolean tracks, Text lastOutput) {
        super(ImmutableCommandData.class);
        this.storedCommand = storedCommand;
        this.success = success;
        this.tracks = tracks;
        this.lastOutput = lastOutput;
        registerGetters();
    }

    @Override
    public ImmutableValue<String> storedCommand() {
        return new ImmutableSpongeValue<String>(Keys.COMMAND, this.storedCommand);
    }

    @Override
    public ImmutableValue<Integer> successCount() {
        return new ImmutableSpongeValue<Integer>(Keys.SUCCESS_COUNT, this.success);
    }

    @Override
    public ImmutableValue<Boolean> doesTrackOutput() {
        return new ImmutableSpongeValue<Boolean>(Keys.TRACKS_OUTPUT, this.tracks);
    }

    @Override
    public ImmutableOptionalValue<Text> lastOutput() {
        return new ImmutableSpongeOptionalValue<Text>(Keys.LAST_COMMAND_OUTPUT, Optional.ofNullable(this.lastOutput));
    }

    @Override
    public ImmutableCommandData copy() {
        return new ImmutableSpongeCommandData(this.storedCommand, this.success, this.tracks, this.lastOutput);
    }

    @Override
    public CommandData asMutable() {
        return new SpongeCommandData()
                .setSuccessCount(this.success)
                .setStoredCommand(this.storedCommand)
                .shouldTrackOutput(this.tracks)
                .setLastOutput(this.lastOutput == null ? Texts.of() : this.lastOutput);
    }

    @Override
    public DataContainer toContainer() {
        return new MemoryDataContainer()
                .set(Keys.COMMAND, this.storedCommand)
                .set(Keys.SUCCESS_COUNT, this.success)
                .set(Keys.TRACKS_OUTPUT, this.tracks)
                .set(Keys.LAST_COMMAND_OUTPUT.getQuery(), this.lastOutput == null ? "" : this.lastOutput.toString());
    }

    @Override
    public int compareTo(ImmutableCommandData o) {
        return 0;
    }

    @Override
    protected void registerGetters() {
        // TODO
    }
}
