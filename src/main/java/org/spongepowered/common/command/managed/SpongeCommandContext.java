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
package org.spongepowered.common.command.managed;

import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.parameter.flag.Flags;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TranslatableText;
import org.spongepowered.api.world.Locatable;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.common.command.parameter.flag.NoFlags;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

public class SpongeCommandContext implements CommandContext {

    private final UUID internalIdentifier = UUID.randomUUID();
    private final boolean isCompletion;
    @Nullable private final CommandSource commandSource;
    @Nullable private final Entity entityTarget;
    @Nullable private final Subject subject;
    @Nullable private final Location<World> location;
    @Nullable private final Location<World> targetBlock;
    private Flags flags = NoFlags.INSTANCE;
    @Nullable private String currentCommand = null;

    private static String textToArgKey(Text key) {
        if (key instanceof TranslatableText) { // Use translation key
            return ((TranslatableText) key).getTranslation().getId();
        }

        return key.toPlain();
    }

    private final ArrayListMultimap<String, Object> parsedArgs;

    public SpongeCommandContext(Cause cause) {
        this(cause, null, false, null);
    }

    public SpongeCommandContext(Cause cause, @Nullable ArrayListMultimap<String, Object> parsedArgs, boolean isCompletion,
            @Nullable Location<World> targetBlock) {
        this.targetBlock = targetBlock;
        this.isCompletion = isCompletion;
        this.parsedArgs = parsedArgs == null ? ArrayListMultimap.create() : parsedArgs;
        this.entityTarget = Command.getEntityFromCause(cause).orElse(null);
        this.commandSource = Command.getCommandSourceFromCause(cause).orElse(null);
        this.location = Command.getLocationFromCause(cause).orElse(null);
        this.subject = Command.getSubjectFromCause(cause).orElse(null);
    }

    public void setFlags(Flags flags) {
        this.flags = flags;
    }

    public Flags getFlags() {
        return this.flags;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getOneUnchecked(Text key) {
        return (T) getOne(key).get();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getOneUnchecked(String key) {
        return (T) getOne(key).get();
    }

    @Override
    public Optional<CommandSource> getCommandSource() {
        return Optional.ofNullable(this.commandSource);
    }

    @Override
    public Optional<Entity> getEntityTarget() {
        if (this.entityTarget == null) {
            if (this.commandSource != null && this.commandSource instanceof Entity) {
                return Optional.of((Entity) this.commandSource);
            }

            return Optional.empty();
        }
        return Optional.of(this.entityTarget);
    }

    @Override
    public Optional<Subject> getSubject() {
        if (this.subject == null) {
            return Optional.ofNullable(this.commandSource);
        }
        return Optional.of(this.subject);
    }

    @Override
    public Optional<Location<World>> getLocation() {
        if (this.location == null) {
            if (this.entityTarget != null) {
                return Optional.of(this.entityTarget.getLocation());
            }

            if (this.commandSource != null && this.commandSource instanceof Locatable) {
                return Optional.of(((Locatable) this.commandSource).getLocation());
            }

            return Optional.empty();
        }

        return Optional.of(this.location);
    }

    @Override
    public Optional<Location<World>> getTargetBlock() {
        return Optional.ofNullable(this.targetBlock);
    }

    @Override
    public boolean isCompletion() {
        return this.isCompletion;
    }

    @Override
    public boolean hasAny(Text key) {
        return hasAny(textToArgKey(key));
    }

    @Override
    public boolean hasAny(String key) {
        return this.parsedArgs.containsKey(key);
    }

    @Override
    public <T> Optional<T> getOne(Text key) {
        return getOne(textToArgKey(key));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<T> getOne(String key) {
        Collection<Object> values = this.parsedArgs.get(key);
        if (values.size() != 1) {
            return Optional.empty();
        }
        return Optional.ofNullable((T) values.iterator().next());
    }

    @Override
    public <T> Collection<T> getAll(Text key) {
        return getAll(textToArgKey(key));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Collection<T> getAll(String key) {
        return Collections.unmodifiableCollection((Collection<T>) this.parsedArgs.get(key));
    }

    @Override
    public void putEntry(Text key, Object value) {
        putEntry(textToArgKey(key), value);
    }

    @Override
    public void putEntry(String key, Object value) {
        checkNotNull(value, "value");
        if (value instanceof Collection) {
            ((Collection<?>) value).forEach(x -> {
                if (x != null) {
                    this.parsedArgs.put(key, x);
                }
            });
        } else {
            this.parsedArgs.put(key, value);
        }
    }

    @Override
    public State getState() {
        return new InternalState(this.internalIdentifier, ImmutableMultimap.copyOf(this.parsedArgs), this.currentCommand);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setState(State state) {
        Preconditions.checkArgument(state instanceof InternalState, "This is not a state obtained from getState");
        InternalState toRestore = (InternalState) state;
        Preconditions.checkArgument(toRestore.internalIdentifier.equals(this.internalIdentifier), "This is not a state from this object");

        this.parsedArgs.clear();
        this.parsedArgs.putAll(((InternalState) state).contextState);

        this.currentCommand = toRestore.currentCommand;
    }

    // Used for determining the current subcommand - if we know what it is.
    public Optional<String> getCurrentCommand() {
        return Optional.ofNullable(this.currentCommand);
    }

    public void setCurrentCommand(@Nullable String command) {
        this.currentCommand = command;
    }

    private static class InternalState implements State {
        private final UUID internalIdentifier;
        private final Multimap<String, Object> contextState;
        @Nullable private final String currentCommand;

        private InternalState(UUID internalIdentifier, Multimap<String, Object> contextState, @Nullable String currentCommand) {
            this.internalIdentifier = internalIdentifier;
            this.contextState = contextState;
            this.currentCommand = currentCommand;
        }
    }

}
