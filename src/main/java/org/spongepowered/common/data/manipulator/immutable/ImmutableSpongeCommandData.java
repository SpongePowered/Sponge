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

import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.manipulator.immutable.ImmutableCommandData;
import org.spongepowered.api.data.manipulator.mutable.CommandData;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.value.Value.Immutable;
import org.spongepowered.api.data.value.immutable.ImmutableBoundedValue;
import org.spongepowered.api.text.Text;
import org.spongepowered.common.data.manipulator.immutable.common.AbstractImmutableData;
import org.spongepowered.common.data.manipulator.mutable.SpongeCommandData;
import org.spongepowered.common.data.value.SpongeValueFactory;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeOptionalValue;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;

import java.util.Optional;

import javax.annotation.Nullable;

public class ImmutableSpongeCommandData extends AbstractImmutableData<ImmutableCommandData, CommandData> implements ImmutableCommandData {

    private final String storedCommand;
    private final int success;
    private final boolean tracks;
    @Nullable private final Text lastOutput;

    private final Immutable<String> storedValue;
    private final ImmutableBoundedValue<Integer> successValue;
    private final Immutable<Boolean> tracksValue;
    private final org.spongepowered.api.data.value.OptionalValue.Immutable<Text> lastOutputValue;

    public ImmutableSpongeCommandData(String storedCommand, int success, boolean tracks, @Nullable Text lastOutput) {
        super(ImmutableCommandData.class);
        this.storedCommand = storedCommand;
        this.success = success;
        this.tracks = tracks;
        this.lastOutput = lastOutput;

        this.storedValue = new ImmutableSpongeValue<>(Keys.COMMAND, this.storedCommand);
        this.successValue = SpongeValueFactory.boundedBuilder(Keys.SUCCESS_COUNT)
                .actualValue(this.success)
                .defaultValue(0)
                .minimum(0)
                .maximum(Integer.MAX_VALUE)
                .build()
                .asImmutable();
        this.tracksValue = ImmutableSpongeValue.cachedOf(Keys.TRACKS_OUTPUT, false, this.tracks);
        this.lastOutputValue = new ImmutableSpongeOptionalValue<>(Keys.LAST_COMMAND_OUTPUT, Optional.ofNullable(this.lastOutput));

        this.registerGetters();
    }

    @Override
    public Immutable<String> storedCommand() {
        return this.storedValue;
    }

    @Override
    public ImmutableBoundedValue<Integer> successCount() {
        return this.successValue;
    }

    @Override
    public Immutable<Boolean> doesTrackOutput() {
        return this.tracksValue;
    }

    @Override
    public org.spongepowered.api.data.value.OptionalValue.Immutable<Text> lastOutput() {
        return this.lastOutputValue;
    }

    @Override
    public CommandData asMutable() {
        return new SpongeCommandData()
                .setSuccessCount(this.success)
                .setStoredCommand(this.storedCommand)
                .shouldTrackOutput(this.tracks)
                .setLastOutput(this.lastOutput == null ? Text.of() : this.lastOutput);
    }

    @Override
    public DataContainer toContainer() {
        return super.toContainer()
                .set(Keys.COMMAND, this.storedCommand)
                .set(Keys.SUCCESS_COUNT, this.success)
                .set(Keys.TRACKS_OUTPUT, this.tracks)
                .set(Keys.LAST_COMMAND_OUTPUT.getQuery(), this.lastOutput == null ? "" : this.lastOutput.toString());
    }

    public String getStoredCommand() {
        return this.storedCommand;
    }

    public int getSuccess() {
        return this.success;
    }

    public boolean getTracks() {
        return this.tracks;
    }

    public Optional<Text> getLastOutput() {
        return Optional.ofNullable(this.lastOutput);
    }

    @Override
    protected void registerGetters() {
        this.registerKeyValue(Keys.COMMAND, ImmutableSpongeCommandData.this::storedCommand);
        this.registerKeyValue(Keys.SUCCESS_COUNT, ImmutableSpongeCommandData.this::successCount);
        this.registerKeyValue(Keys.TRACKS_OUTPUT, ImmutableSpongeCommandData.this::doesTrackOutput);
        this.registerKeyValue(Keys.LAST_COMMAND_OUTPUT, ImmutableSpongeCommandData.this::lastOutput);

        this.registerFieldGetter(Keys.COMMAND, ImmutableSpongeCommandData.this::getStoredCommand);
        this.registerFieldGetter(Keys.SUCCESS_COUNT, ImmutableSpongeCommandData.this::getSuccess);
        this.registerFieldGetter(Keys.TRACKS_OUTPUT, ImmutableSpongeCommandData.this::getTracks);
        this.registerFieldGetter(Keys.LAST_COMMAND_OUTPUT, ImmutableSpongeCommandData.this::getLastOutput);
    }
}
