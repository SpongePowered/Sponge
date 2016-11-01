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
package org.spongepowered.common.network.message;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.api.network.Message;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.common.keyboard.ClientKeyBinding;
import org.spongepowered.common.keyboard.SpongeKeyBinding;
import org.spongepowered.common.keyboard.SpongeKeyCategory;
import org.spongepowered.common.registry.type.keyboard.KeyCategoryRegistryModule;

import java.util.BitSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class MessageKeyboardData implements Message {

    private Collection<SpongeKeyCategory> keyCategories;
    private Collection<SpongeKeyBinding> keyBindings;
    private Int2ObjectMap<BitSet> conflictContextData;

    public MessageKeyboardData() {
    }

    public MessageKeyboardData(Collection<SpongeKeyCategory> keyCategories, Collection<SpongeKeyBinding> keyBindings,
            Int2ObjectMap<BitSet> conflictContextData) {
        this.keyCategories = keyCategories;
        this.keyBindings = keyBindings;
        this.conflictContextData = conflictContextData;
    }

    public Collection<SpongeKeyCategory> getKeyCategories() {
        return this.keyCategories;
    }

    public Collection<SpongeKeyBinding> getKeyBindings() {
        return this.keyBindings;
    }

    public Int2ObjectMap<BitSet> getConflictContextData() {
        return this.conflictContextData;
    }

    @Override
    public void readFrom(ChannelBuf buf) {
        final Set<SpongeKeyCategory> keyCategories = new HashSet<>();
        this.keyCategories = keyCategories;
        final Set<SpongeKeyBinding> keyBindings = new HashSet<>();
        this.keyBindings = keyBindings;

        final Int2ObjectMap<SpongeKeyCategory> keyCategoriesById = new Int2ObjectOpenHashMap<>();

        // Read the key categories
        final int keyCategoriesCount = buf.readShort();
        for (int i = 0; i < keyCategoriesCount; i++) {
            final int internalId = buf.readShort();
            final String id = buf.readString();
            final Text title = TextSerializers.JSON.deserializeUnchecked(buf.readString());

            final SpongeKeyCategory keyCategory = new SpongeKeyCategory(id, title, false);
            keyCategory.setInternalId(internalId);
            keyCategoriesById.put(internalId, keyCategory);
            keyCategories.add(keyCategory);
        }

        // Read the key bindings
        final int keyBindingCount = buf.readShort();
        for (int i = 0; i < keyBindingCount; i++) {
            final int internalId = buf.readShort();
            final String id = buf.readString();
            final int categoryId = buf.readShort();
            final int contextId = buf.readShort();
            final Text displayName = TextSerializers.JSON.deserializeUnchecked(buf.readString());

            // Get the key category of this key binding
            SpongeKeyCategory keyCategory = keyCategoriesById.get(categoryId);
            if (keyCategory == null) {
                // Try the default key categories
                keyCategory = (SpongeKeyCategory) KeyCategoryRegistryModule.get().getByInternalId(categoryId)
                        .orElseThrow(() -> new IllegalArgumentException("Received key binding with unknown category"));
            }

            final ClientKeyBinding keyBinding = new ClientKeyBinding(id, contextId, keyCategory, displayName, false);
            keyBinding.setInternalId(internalId);
            keyBindings.add(keyBinding);
        }

        // Read the amount of context data entries
        final int contextDataCount = buf.readShort();

        // Read the conflict context data
        final Int2ObjectMap<BitSet> conflictContextData = new Int2ObjectOpenHashMap<>(contextDataCount);
        this.conflictContextData = conflictContextData;

        for (int i = 0; i < contextDataCount; i++) {
            // Read the length and data entry and extract the fields from it
            final int lengthAndId = buf.readShort() & 0xffff;
            final int length = lengthAndId >> 10;
            final int id = lengthAndId & 0x3ff;

            // Read the data bytes
            final byte[] bytes = buf.readBytes(length);
            conflictContextData.put(id, BitSet.valueOf(bytes));
        }
    }

    @Override
    public void writeTo(ChannelBuf buf) {
        // Write the key categories, doesn't contain any defaults
        buf.writeShort((short) this.keyCategories.size());
        for (SpongeKeyCategory category : this.keyCategories) {
            buf.writeShort((short) category.getInternalId());
            buf.writeString(category.getId());
            buf.writeString(TextSerializers.JSON.serialize(category.getTitle()));
        }
        // Write the key bindings, doesn't contain any defaults
        buf.writeShort((short) this.keyBindings.size());
        for (SpongeKeyBinding keyBinding : this.keyBindings) {
            buf.writeShort((short) keyBinding.getInternalId());
            buf.writeString(keyBinding.getId());
            buf.writeShort((short) keyBinding.getCategory().getInternalId());
            buf.writeShort((short) keyBinding.getContext().getInternalId());
            buf.writeString(TextSerializers.JSON.serialize(keyBinding.getDisplayName()));
        }
        // Write the conflict context data
        buf.writeShort((short) this.conflictContextData.size());
        for (Int2ObjectMap.Entry<BitSet> entry : this.conflictContextData.int2ObjectEntrySet()) {
            final byte[] bytes = entry.getValue().toByteArray();
            // First 6 bits is the data length
            // Last 10 bits are the context id
            // Lets hope no one reaches 511 contexts...
            buf.writeShort((short) (bytes.length << 10 | entry.getIntKey()));
            // Write the byte array
            buf.writeBytes(bytes);
        }
    }
}
