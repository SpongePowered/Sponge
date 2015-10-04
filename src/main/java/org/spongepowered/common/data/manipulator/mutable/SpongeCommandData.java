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

import com.google.common.collect.ComparisonChain;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.ImmutableCommandData;
import org.spongepowered.api.data.manipulator.mutable.CommandData;
import org.spongepowered.api.data.value.mutable.OptionalValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;
import org.spongepowered.common.data.manipulator.immutable.ImmutableSpongeCommandData;
import org.spongepowered.common.data.manipulator.mutable.common.AbstractData;
import org.spongepowered.common.data.value.mutable.SpongeOptionalValue;
import org.spongepowered.common.data.value.mutable.SpongeValue;
import org.spongepowered.common.util.GetterFunction;
import org.spongepowered.common.util.SetterFunction;

import java.util.Optional;

import javax.annotation.Nullable;

public class SpongeCommandData extends AbstractData<CommandData, ImmutableCommandData> implements CommandData {

    private String command;
    private int success;
    private boolean tracks;
    @Nullable private Text lastOutput;

    public SpongeCommandData() {
        super(CommandData.class);
        registerGettersAndSetters();
    }

    @Override
    public Value<String> storedCommand() {
        return new SpongeValue<String>(Keys.COMMAND, getStoredCommand());
    }

    @Override
    public Value<Integer> successCount() {
        return new SpongeValue<Integer>(Keys.SUCCESS_COUNT, getSuccessCount());
    }

    @Override
    public Value<Boolean> doesTrackOutput() {
        return new SpongeValue<Boolean>(Keys.TRACKS_OUTPUT, this.tracks);
    }

    @Override
    public OptionalValue<Text> lastOutput() {
        return new SpongeOptionalValue<Text>(Keys.LAST_COMMAND_OUTPUT, getLastOutput());
    }

    @Override
    public CommandData copy() {
        return new SpongeCommandData()
                .setStoredCommand(this.getStoredCommand())
                .setSuccessCount(this.getSuccessCount())
                .shouldTrackOutput(this.tracks)
                .setLastOutput(this.getLastOutput().orElse(Texts.of()));
    }

    @Override
    public ImmutableCommandData asImmutable() {
        return new ImmutableSpongeCommandData(this.command, this.success, this.tracks, this.lastOutput);
    }

    @Override
    public int compareTo(CommandData o) {
        return ComparisonChain.start()
                .compare(o.doesTrackOutput().get(), this.tracks)
                .compare(o.lastOutput().get().isPresent(), this.lastOutput != null)
                .compare(o.storedCommand().get(), this.command)
                .compare(o.successCount().get().intValue(), this.success)
                .result();
    }

    @Override
    public DataContainer toContainer() {
        return new MemoryDataContainer()
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

    public CommandData setLastOutput(Text message) {
        if (checkNotNull(message, "Null message! Use empty text instead!").toString().isEmpty()) {
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
        registerFieldGetter(Keys.COMMAND, new GetterFunction<Object>() {
            @Override
            public Object get() {
                return getStoredCommand();
            }
        });
        registerFieldSetter(Keys.COMMAND, new SetterFunction<Object>() {
            @Override
            public void set(Object value) {
                setStoredCommand((String) value);
            }
        });
        registerKeyValue(Keys.COMMAND, new GetterFunction<Value<?>>() {
            @Override
            public Value<?> get() {
                return storedCommand();
            }
        });

        // Keys.SUCCESS_COUNT
        registerFieldGetter(Keys.SUCCESS_COUNT, new GetterFunction<Object>() {
            @Override
            public Object get() {
                return getSuccessCount();
            }
        });
        registerFieldSetter(Keys.SUCCESS_COUNT, new SetterFunction<Object>() {
            @Override
            public void set(Object value) {
                setSuccessCount(((Number) value).intValue());
            }
        });
        registerKeyValue(Keys.SUCCESS_COUNT, new GetterFunction<Value<?>>() {
            @Override
            public Value<?> get() {
                return successCount();
            }
        });

        // Keys.TRACKS_OUTPUT
        registerFieldGetter(Keys.TRACKS_OUTPUT, new GetterFunction<Object>() {
            @Override
            public Object get() {
                return tracksOutput();
            }
        });
        registerFieldSetter(Keys.TRACKS_OUTPUT, new SetterFunction<Object>() {
            @Override
            public void set(Object value) {
                shouldTrackOutput((Boolean) value);
            }
        });
        registerKeyValue(Keys.TRACKS_OUTPUT, new GetterFunction<Value<?>>() {
            @Override
            public Value<?> get() {
                return doesTrackOutput();
            }
        });
        // Keys.LAST_COMMAND_OUTPUT
    }
}
