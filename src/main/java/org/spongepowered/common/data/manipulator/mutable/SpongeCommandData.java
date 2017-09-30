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
package org.spongepowered.common.data.manipulator.mutable;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.ImmutableCommandData;
import org.spongepowered.api.data.manipulator.mutable.CommandData;
import org.spongepowered.api.data.value.mutable.MutableBoundedValue;
import org.spongepowered.api.data.value.mutable.OptionalValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.text.Text;
import org.spongepowered.common.data.manipulator.immutable.ImmutableSpongeCommandData;
import org.spongepowered.common.data.manipulator.mutable.common.AbstractData;
import org.spongepowered.common.data.value.SpongeValueFactory;
import org.spongepowered.common.data.value.mutable.SpongeOptionalValue;
import org.spongepowered.common.data.value.mutable.SpongeValue;

import java.util.Optional;

import javax.annotation.Nullable;

public class SpongeCommandData extends AbstractData<CommandData, ImmutableCommandData> implements CommandData {

    private String command;
    private int success;
    private boolean tracks;
    @Nullable private Text lastOutput;

    public SpongeCommandData() {
        super(CommandData.class);
        this.command = "";
        registerGettersAndSetters();
    }

    @Override
    public Value<String> storedCommand() {
        return new SpongeValue<>(Keys.COMMAND, getStoredCommand());
    }

    @Override
    public MutableBoundedValue<Integer> successCount() {
        return SpongeValueFactory.boundedBuilder(Keys.SUCCESS_COUNT)
                .actualValue(this.success)
                .defaultValue(0)
                .minimum(0)
                .maximum(Integer.MAX_VALUE)
                .build();
    }

    @Override
    public Value<Boolean> doesTrackOutput() {
        return new SpongeValue<>(Keys.TRACKS_OUTPUT, this.tracks);
    }

    @Override
    public OptionalValue<Text> lastOutput() {
        return new SpongeOptionalValue<>(Keys.LAST_COMMAND_OUTPUT, getLastOutput());
    }

    @Override
    public CommandData copy() {
        return new SpongeCommandData()
                .setStoredCommand(this.getStoredCommand())
                .setSuccessCount(this.getSuccessCount())
                .shouldTrackOutput(this.tracks)
                .setLastOutput(this.getLastOutput().orElse(Text.of()));
    }

    @Override
    public ImmutableCommandData asImmutable() {
        return new ImmutableSpongeCommandData(this.command, this.success, this.tracks, this.lastOutput);
    }

    @Override
    public DataContainer toContainer() {
        return super.toContainer()
                .set(Keys.COMMAND.getQuery(), this.command)
                .set(Keys.SUCCESS_COUNT.getQuery(), this.success)
                .set(Keys.TRACKS_OUTPUT.getQuery(), this.tracks)
                .set(Keys.LAST_COMMAND_OUTPUT.getQuery(), this.lastOutput == null ? "" : this.lastOutput.toString());
    }

    // Traditional getters and setters for utility

    public String getStoredCommand() {
        return this.command;
    }

    public SpongeCommandData setStoredCommand(String command) {
        this.command = checkNotNull(command);
        return this;
    }

    public int getSuccessCount() {
        return this.success;
    }

    public SpongeCommandData setSuccessCount(int count) {
        checkArgument(count >= 0);
        this.success = count;
        return this;
    }

    public boolean tracksOutput() {
        return this.tracks;
    }

    public SpongeCommandData shouldTrackOutput(boolean track) {
        this.tracks = track;
        return this;
    }

    public Optional<Text> getLastOutput() {
        return Optional.ofNullable(this.lastOutput);
    }

    public SpongeCommandData setLastOutput(@Nullable Text message) {
        if (message == null) {
            this.lastOutput = null;
            return this;
        }
        if (checkNotNull(message, "message").isEmpty()) {
            this.lastOutput = null;
        } else {
            this.lastOutput = message;
        }
        return this;
    }

    // Beware, all ye to enter here
    @Override
    protected void registerGettersAndSetters() {
        // Keys.COMMAND
        registerFieldGetter(Keys.COMMAND, SpongeCommandData.this::getStoredCommand);
        registerFieldSetter(Keys.COMMAND, this::setStoredCommand);
        registerKeyValue(Keys.COMMAND, SpongeCommandData.this::storedCommand);

        // Keys.SUCCESS_COUNT
        registerFieldGetter(Keys.SUCCESS_COUNT, SpongeCommandData.this::getSuccessCount);
        registerFieldSetter(Keys.SUCCESS_COUNT, this::setSuccessCount);
        registerKeyValue(Keys.SUCCESS_COUNT, SpongeCommandData.this::successCount);

        // Keys.TRACKS_OUTPUT
        registerFieldGetter(Keys.TRACKS_OUTPUT, SpongeCommandData.this::tracksOutput);
        registerFieldSetter(Keys.TRACKS_OUTPUT, this::shouldTrackOutput);
        registerKeyValue(Keys.TRACKS_OUTPUT, SpongeCommandData.this::doesTrackOutput);

        // Keys.LAST_COMMAND_OUTPUT
        registerFieldGetter(Keys.LAST_COMMAND_OUTPUT, SpongeCommandData.this::getLastOutput);
        registerFieldSetter(Keys.LAST_COMMAND_OUTPUT, optional -> this.setLastOutput(optional.orElse(Text.of())));
        registerKeyValue(Keys.LAST_COMMAND_OUTPUT, SpongeCommandData.this::lastOutput);
    }
}
