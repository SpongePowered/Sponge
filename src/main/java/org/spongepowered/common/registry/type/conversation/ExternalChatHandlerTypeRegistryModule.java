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
package org.spongepowered.common.registry.type.conversation;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import org.spongepowered.api.command.conversation.Conversant;
import org.spongepowered.api.command.conversation.Conversation;
import org.spongepowered.api.command.conversation.ExternalChatHandler;
import org.spongepowered.api.command.conversation.ExternalChatHandlerType;
import org.spongepowered.api.command.conversation.ExternalChatHandlerTypes;
import org.spongepowered.api.registry.AdditionalCatalogRegistryModule;
import org.spongepowered.api.registry.AlternateCatalogRegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.common.command.conversation.SpongeExternalChatHandlerType;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class ExternalChatHandlerTypeRegistryModule implements AlternateCatalogRegistryModule<ExternalChatHandlerType>,
        AdditionalCatalogRegistryModule<ExternalChatHandlerType> {

    private static final ExternalChatHandler constantDiscardHandler = new ExternalChatHandler() {
        @Override
        public boolean process(Text text) {
            return false;
        }

        @Override
        public void finish() {
            // Ignore
        }

        @Override
        public void drainTo(Consumer<Text> consumer) {
            // Ignore
        }

        @Override
        public ExternalChatHandlerType getType() {
            return ExternalChatHandlerTypes.DISCARD;
        }

        @Override
        public void accept(Text text) {
            // Ignore
        }
    };

    @RegisterCatalog(ExternalChatHandlerTypes.class)
    private final Map<String, ExternalChatHandlerType> chatHandlerTypes = new HashMap<>();

    @Override
    public void registerAdditionalCatalog(ExternalChatHandlerType extraCatalog) {
        final String id = checkNotNull(extraCatalog).getId().toLowerCase(Locale.ENGLISH);
        checkArgument(!this.chatHandlerTypes.containsKey(id), "A ExternalChatHandlerType with the same id is already registered: {}",
                extraCatalog.getId());
        this.chatHandlerTypes.put(extraCatalog.getId().toLowerCase(Locale.ENGLISH), extraCatalog);
    }

    @Override
    public Map<String, ExternalChatHandlerType> provideCatalogMap() {
        final HashMap<String, ExternalChatHandlerType> map = new HashMap<>();
        for (Map.Entry<String, ExternalChatHandlerType> entry : this.chatHandlerTypes.entrySet()) {
            map.put(entry.getKey().replace("minecraft:", "").replace("sponge:", ""), entry.getValue());
        }
        return map;
    }

    @Override
    public Optional<ExternalChatHandlerType> getById(String id) {
        return Optional.ofNullable(this.chatHandlerTypes.get(checkNotNull(id).toLowerCase(Locale.ENGLISH)));
    }

    @Override
    public void registerDefaults() {
        this.chatHandlerTypes.put("sponge:pass_through",
                new SpongeExternalChatHandlerType("sponge:pass_through", "Pass Through") {
            @Override
            public ExternalChatHandler createFor(Conversation conversation, Conversant conversant) {
                return new ExternalChatHandler() {

                    @Override
                    public boolean process(Text text) {
                        return true;
                    }

                    @Override
                    public void finish() {
                        // Ignore
                    }

                    @Override
                    public void drainTo(Consumer<Text> consumer) {
                        // Ignore
                    }

                    @Override
                    public ExternalChatHandlerType getType() {
                        return ExternalChatHandlerTypes.PASS_THROUGH;
                    }

                    @Override
                    public void accept(Text text) {
                        conversant.sendMessage(text, true);
                    }
                };
            }
        });
        this.chatHandlerTypes.put("sponge:discard",
                new SpongeExternalChatHandlerType("sponge:discard", "Discard") {
            @Override
            public ExternalChatHandler createFor(Conversation conversation, Conversant conversant) {
                return constantDiscardHandler;
            }
        });
        this.chatHandlerTypes.put("sponge:send_on_completion",
                new SpongeExternalChatHandlerType("sponge:send_on_completion", "Send on Completion") {
            @Override
            public ExternalChatHandler createFor(Conversation conversation, Conversant conversant) {
                return new ExternalChatHandler() {

                    private final Queue<Text> messages = new ArrayDeque<>();

                    @Override
                    public boolean process(Text text) {
                        messages.add(text);
                        return false;
                    }

                    @Override
                    public void finish() {
                        this.messages.forEach(message -> conversant.sendMessage(message, true));
                    }

                    @Override
                    public void drainTo(Consumer<Text> consumer) {
                        this.messages.forEach(consumer);
                    }

                    @Override
                    public ExternalChatHandlerType getType() {
                        return ExternalChatHandlerTypes.SEND_ON_COMPLETION;
                    }

                    @Override
                    public void accept(Text text) {
                        this.messages.offer(text);
                    }
                };
            }
        });
        this.chatHandlerTypes.put("sponge:send_slowly_on_completion",
                new SpongeExternalChatHandlerType("sponge:send_slowly_on_completion", "Send Slowly on Completion") {
            @Override
            public ExternalChatHandler createFor(Conversation conversation, Conversant conversant) {
                return new ExternalChatHandler() {

                    private final Queue<Text> messages = new ArrayDeque<>();

                    @Override
                    public boolean process(Text text) {
                        messages.add(text);
                        return false;
                    }

                    @Override
                    public void finish() {
                        Task.builder()
                                .async()
                                .interval(2, TimeUnit.SECONDS)
                                .name(Objects.toStringHelper(this)
                                        .add("id", getId())
                                        .add("name", getName())
                                        .add("Conversant", conversant.getName())
                                        .toString())
                                .execute(() -> {
                                    // Necessary to check if conversation contains conversant in-case reference is gone
                                    if (!this.messages.isEmpty() && conversation.getConversants().contains(conversant)) {
                                        conversant.sendMessage(this.messages.poll(), true);
                                    }
                                })
                                .submit(conversation.getCreator());
                    }

                    @Override
                    public void drainTo(Consumer<Text> consumer) {
                        this.messages.forEach(consumer);
                    }

                    @Override
                    public ExternalChatHandlerType getType() {
                        return ExternalChatHandlerTypes.SEND_SLOWLY_ON_COMPLETION;
                    }

                    @Override
                    public void accept(Text text) {
                        this.messages.offer(text);
                    }
                };
            }
        });
    }

    @Override
    public Collection<ExternalChatHandlerType> getAll() {
        return ImmutableList.copyOf(this.chatHandlerTypes.values());
    }

}
