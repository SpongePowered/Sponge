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
package org.spongepowered.common.text.sink;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.permission.SubjectCollection;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.sink.MessageSink;
import org.spongepowered.api.text.sink.MessageSinkFactory;
import org.spongepowered.api.util.command.CommandSource;
import org.spongepowered.common.Sponge;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.WeakHashMap;

import javax.annotation.Nullable;

/**
 * Implementation of factory to create message sinks.
 */
public class SpongeMessageSinkFactory implements MessageSinkFactory {
    public static final SpongeMessageSinkFactory INSTANCE = new SpongeMessageSinkFactory();

    private SpongeMessageSinkFactory() {}

    private static class PermissionSink extends MessageSink {
        private final String permission;

        private PermissionSink(String permission) {
            this.permission = permission;
        }

        @Override
        public Iterable<CommandSource> getRecipients() {
            PermissionService service =  Sponge.getGame().getServiceManager().provideUnchecked(PermissionService.class);
            return Iterables.concat(
                    Iterables.transform(service.getKnownSubjects().values(), new Function<SubjectCollection, Iterable<CommandSource>>() {
                @Nullable
                @Override
                public Iterable<CommandSource> apply(SubjectCollection input) {
                    return Iterables.filter(Iterables.transform(Maps.filterValues(input.getAllWithPermission(PermissionSink.this.permission),
                                    Predicates.equalTo(true)).keySet(),
                            new Function<Subject, CommandSource>() {
                                @Nullable
                                @Override
                                public CommandSource apply(@Nullable Subject input) {
                                    return input.getCommandSource().orElse(null);
                                }
                            }), Predicates.notNull());
                }
            }));
        }
    }

    @Override
    public MessageSink toPermission(String permission) {
        checkNotNull(permission, "permission");
        return new PermissionSink(permission);
    }

    public static final MessageSink TO_ALL = new MessageSink() {
        @Override
        public Iterable<CommandSource> getRecipients() {
            // TODO: Non-player subjects?
            @SuppressWarnings({"unchecked", "rawtypes"})
            Set<CommandSource> ret = new HashSet(MinecraftServer.getServer().getConfigurationManager().playerEntityList);
            ret.add((CommandSource) MinecraftServer.getServer());
            return ret;
        }
    };

    @Override
    public MessageSink toAll() {
        return TO_ALL;
    }

    public static final MessageSink TO_ALL_PLAYERS = new MessageSink() {
        @Override
        public Iterable<CommandSource> getRecipients() {
            @SuppressWarnings({"unchecked", "rawtypes"})
            Set<CommandSource> ret = new HashSet(MinecraftServer.getServer().getConfigurationManager().playerEntityList);
            return ret;
        }
    };

    @Override
    public MessageSink toAllPlayers() {
        return TO_ALL_PLAYERS;
    }

    public static final MessageSink TO_NONE = new MessageSink() {
        @Override
        public Iterable<CommandSource> getRecipients() {
            return new HashSet<CommandSource>();
        }
    };

    @Override
    public MessageSink toNone() {
        return TO_NONE;
    }

    private static class CombinedSink extends MessageSink {
        private final Iterable<MessageSink> contents;

        private CombinedSink(Iterable<MessageSink> contents) {
            this.contents = contents;
        }

        @Override
        public Text transformMessage(CommandSource target, Text text) {
            Text ret = text;
            for (MessageSink sink : this.contents) {
                Text xformed = sink.transformMessage(target, ret);
                if (xformed != null) {
                    ret = xformed;
                }
            }
            return ret;
        }

        @Override
        public Iterable<CommandSource> getRecipients() {
            return ImmutableSet.copyOf(Iterables.concat(Iterables.transform(this.contents, new Function<MessageSink, Iterable<CommandSource>>() {
                @Nullable
                @Override
                public Iterable<CommandSource> apply(@Nullable MessageSink input) {
                    return input.getRecipients();
                }
            })));
        }
    }

    @Override
    public MessageSink combined(MessageSink... sinks) {
        return new CombinedSink(ImmutableList.copyOf(sinks));
    }

    private static class FixedSink extends MessageSink {
        private final Set<CommandSource> contents;

        private FixedSink(Set<CommandSource> provided) {
            Set<CommandSource> contents = Collections.newSetFromMap(new WeakHashMap<CommandSource, Boolean>());
            contents.addAll(provided);
            this.contents = Collections.unmodifiableSet(contents);
        }

        @Override
        public Iterable<CommandSource> getRecipients() {
            return this.contents;
        }
    }

    @Override
    public MessageSink to(Set<CommandSource> sources) {
        return new FixedSink(sources);
    }

}
