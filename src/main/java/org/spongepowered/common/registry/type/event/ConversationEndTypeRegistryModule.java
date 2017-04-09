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
package org.spongepowered.common.registry.type.event;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import org.spongepowered.api.event.cause.conversation.ConversationEndType;
import org.spongepowered.api.event.cause.conversation.ConversationEndTypes;
import org.spongepowered.api.registry.AdditionalCatalogRegistryModule;
import org.spongepowered.api.registry.AlternateCatalogRegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.common.event.conversation.SpongeConversationEndType;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class ConversationEndTypeRegistryModule implements AlternateCatalogRegistryModule<ConversationEndType>,
    AdditionalCatalogRegistryModule<ConversationEndType> {

    @RegisterCatalog(ConversationEndTypes.class)
    private final Map<String, ConversationEndType> endTypeMap = new HashMap<>();

    @Override
    public void registerAdditionalCatalog(ConversationEndType extraCatalog) {
        final String id = checkNotNull(extraCatalog).getId().toLowerCase(Locale.ENGLISH);
        checkArgument(!this.endTypeMap.containsKey(id), "ConversationEndType with the same id is already registered: {}", extraCatalog.getId());
        this.endTypeMap.put(extraCatalog.getId().toLowerCase(Locale.ENGLISH), extraCatalog);
    }

    @Override
    public Map<String, ConversationEndType> provideCatalogMap() {
        final HashMap<String, ConversationEndType> map = new HashMap<>();
        for (Map.Entry<String, ConversationEndType> entry : this.endTypeMap.entrySet()) {
            map.put(entry.getKey().replace("minecraft:", "").replace("sponge:", ""), entry.getValue());
        }
        return map;
    }

    @Override
    public void registerDefaults() {
        this.endTypeMap.put("sponge:error", new SpongeConversationEndType("sponge:error", "Error"));
        this.endTypeMap.put("sponge:finished", new SpongeConversationEndType("sponge:finished", "Finished"));
        this.endTypeMap.put("sponge:forced", new SpongeConversationEndType("sponge:forced", "Forced"));
        this.endTypeMap.put("sponge:quit", new SpongeConversationEndType("sponge:quit", "Quit"));
    }

    @Override
    public Optional<ConversationEndType> getById(String id) {
        return Optional.ofNullable(this.endTypeMap.get(checkNotNull(id).toLowerCase(Locale.ENGLISH)));
    }

    @Override
    public Collection<ConversationEndType> getAll() {
        return ImmutableList.copyOf(this.endTypeMap.values());
    }
}
