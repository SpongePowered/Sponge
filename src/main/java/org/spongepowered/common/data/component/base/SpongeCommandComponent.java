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
package org.spongepowered.common.data.component.base;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Optional;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.component.base.CommandComponent;
import org.spongepowered.api.data.token.Tokens;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;
import org.spongepowered.common.data.component.SpongeAbstractComponent;

import javax.annotation.Nullable;

public class SpongeCommandComponent extends SpongeAbstractComponent<CommandComponent> implements CommandComponent {

    private String command;
    private int success;
    private boolean tracks;
    @Nullable private Text lastOutput;

    public SpongeCommandComponent() {
        super(CommandComponent.class);
    }

    @Override
    public String getStoredCommand() {
        return this.command;
    }

    @Override
    public CommandComponent setStoredCommand(String command) {
        this.command = checkNotNull(command);
        return this;
    }

    @Override
    public int getSuccessCount() {
        return this.success;
    }

    @Override
    public CommandComponent setSuccessCount(int count) {
        checkArgument(count >= 0);
        this.success = count;
        return this;
    }

    @Override
    public boolean doesTrackOutput() {
        return this.tracks;
    }

    @Override
    public CommandComponent shouldTrackOutput(boolean track) {
        this.tracks = track;
        return this;
    }

    @Override
    public Optional<Text> getLastOutput() {
        return Optional.fromNullable(this.lastOutput);
    }

    @Override
    public CommandComponent setLastOutput(Text message) {
        if (checkNotNull(message, "Null message! Use empty text instead!").toString().isEmpty()) {
            this.lastOutput = null;
        } else {
            this.lastOutput = message;
        }
        return this;
    }

    @Override
    public CommandComponent copy() {
        return new SpongeCommandComponent()
                .setStoredCommand(this.getStoredCommand())
                .setSuccessCount(this.getSuccessCount())
                .shouldTrackOutput(this.doesTrackOutput())
                .setLastOutput(this.getLastOutput().or(Texts.of()));
    }

    @Override
    public CommandComponent reset() {
        return setLastOutput(Texts.of()).setStoredCommand("").setSuccessCount(0).shouldTrackOutput(true);
    }

    @Override
    public int compareTo(CommandComponent o) {
        return 0;
    }

    @Override
    public DataContainer toContainer() {
        return new MemoryDataContainer()
                .set(Tokens.COMMAND.getQuery(), this.command)
                .set(Tokens.SUCCESS_COUNT.getQuery(), this.success)
                .set(Tokens.OUTPUT_TRACKED.getQuery(), this.tracks)
                .set(Tokens.PREVIOUS_OUTPUT.getQuery(), this.lastOutput == null ? "" : this.lastOutput.toString());
    }
}
