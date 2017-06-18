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
package org.spongepowered.common.command.parameter.token;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.spongepowered.api.command.parameter.token.CommandArgs;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.command.parameter.ArgumentParseException;
import org.spongepowered.api.command.parameter.token.SingleArg;

import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.spongepowered.api.util.SpongeApiTranslationHelper.t;

public class SpongeCommandArgs implements CommandArgs {

    private final UUID internalIdentifier = UUID.randomUUID();

    private final List<SingleArg> args;
    private final String raw;
    private ListIterator<SingleArg> iterator;

    public SpongeCommandArgs(List<SingleArg> args, String raw) {
        this.args = args;
        this.iterator = args.listIterator();
        this.raw = raw;
    }

    @Override
    public boolean hasNext() {
        return this.iterator.hasNext();
    }

    @Override
    public String next() throws ArgumentParseException {
        return nextIfPresent().orElseThrow(() -> createError(t("Not enough arguments")));
    }

    @Override
    public Optional<String> nextIfPresent() {
        if (hasNext()) {
            return Optional.of(this.iterator.next().getArg());
        }

        return Optional.empty();
    }

    @Override
    public String peek() throws ArgumentParseException {
        if (hasNext()) {
            this.iterator.next();
            return this.iterator.previous().getArg();
        }

        throw createError(t("Not enough arguments"));
    }

    @Override
    public boolean hasPrevious() {
        return this.iterator.hasPrevious();
    }

    @Override
    public String previous() throws ArgumentParseException {
        if (hasPrevious()) {
            return this.iterator.previous().getArg();
        }

        throw createError(t("Already at the beginning of the argument list."));
    }

    @Override
    public List<String> getAll() {
        return ImmutableList.copyOf(this.args.stream().map(SingleArg::getArg).collect(Collectors.toList()));
    }

    @Override
    public int getCurrentRawPosition() {
        if (hasNext()) {
            SingleArg next = this.iterator.next();
            this.iterator.previous();

            return next.getStartIndex();
        }

        return this.raw.length();
    }

    private int getLastArgStartPosition() {
        if (hasPrevious()) {
            SingleArg previous = this.iterator.previous();
            this.iterator.next();

            return previous.getStartIndex();
        }

        return 0;
    }

    @Override
    public String getRaw() {
        return this.raw;
    }

    @Override
    public State getState() {
        return new InternalState(this.iterator.previousIndex(), this.internalIdentifier);
    }

    @Override
    public void setState(State state) {
        Preconditions.checkArgument(state instanceof InternalState, "This is not a state obtained from getState");

        InternalState toRestore = (InternalState) state;
        Preconditions.checkArgument(toRestore.internalIdentifier.equals(this.internalIdentifier), "This is not a state from this object");

        this.iterator = this.args.listIterator(toRestore.index + 1);
    }

    @Override
    public ArgumentParseException createError(Text message) {
        return new ArgumentParseException(message, getRaw(), getLastArgStartPosition());
    }

    @Override
    public ArgumentParseException createError(Text message, Throwable inner) {
        return new ArgumentParseException(message, inner, getRaw(), getLastArgStartPosition());
    }

    @Override
    public String rawArgsFromCurrentPosition() {
        if (hasNext()) {
            this.iterator.next();
            return getRaw().substring(this.iterator.previous().getStartIndex());
        }

        return "";
    }

    static class InternalState implements State {
        private final int index;
        private final UUID internalIdentifier;

        InternalState(int index, UUID internalIdentifier) {
            this.index = index;
            this.internalIdentifier = internalIdentifier;
        }
    }
}
