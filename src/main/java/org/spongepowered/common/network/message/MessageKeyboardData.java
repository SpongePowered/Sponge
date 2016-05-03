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

import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.api.network.Message;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.common.keyboard.SpongeKeyBinding;
import org.spongepowered.common.keyboard.SpongeKeyCategory;
import org.spongepowered.common.registry.type.keyboard.KeyCategoryRegistryModule;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class MessageKeyboardData implements Message {

    private Collection<SpongeKeyCategory> keyCategories;
    private Collection<SpongeKeyBinding> keyBindings;

    public MessageKeyboardData() {
    }

    public MessageKeyboardData(Collection<SpongeKeyCategory> keyCategories, Collection<SpongeKeyBinding> keyBindings) {
        this.keyCategories = keyCategories;
        this.keyBindings = keyBindings;
    }

    public Collection<SpongeKeyCategory> getKeyCategories() {
        return this.keyCategories;
    }

    public Collection<SpongeKeyBinding> getKeyBindings() {
        return this.keyBindings;
    }

    @Override
    public void readFrom(ChannelBuf buf) {
        Map<Integer, SpongeKeyCategory> categories = new HashMap<>();
        int keyCategoriesCount = buf.readShort();
        for (int i = 0; i < keyCategoriesCount; i++) {
            int internalId = buf.readShort();
            String id = buf.readString();
            Text title = TextSerializers.JSON.deserializeUnchecked(buf.readString());

            SpongeKeyCategory keyCategory = new SpongeKeyCategory(id, title);
            keyCategory.setInternalId(internalId);

            categories.put(internalId, keyCategory);
        }
        this.keyCategories = new HashSet<>(categories.values());
        int keyBindingCount = buf.readShort();
        this.keyBindings = new HashSet<>(keyBindingCount);
        for (int i = 0; i < keyBindingCount; i++) {
            int internalId = buf.readShort();
            String id = buf.readString();
            int categoryId = buf.readShort();
            Text displayName = TextSerializers.JSON.deserializeUnchecked(buf.readString());

            SpongeKeyCategory keyCategory = categories.get(categoryId);
            if (keyCategory == null) {
                keyCategory = (SpongeKeyCategory) KeyCategoryRegistryModule.getInstance().getByInternalId(categoryId)
                        .orElseThrow(() -> new IllegalArgumentException("Received key binding with unknown category"));
            }
            SpongeKeyBinding keyBinding = new SpongeKeyBinding(id, keyCategory, displayName);
            keyCategory.setInternalId(internalId);

            this.keyBindings.add(keyBinding);
        }
    }

    @Override
    public void writeTo(ChannelBuf buf) {
        buf.writeShort((short) this.keyCategories.size());
        for (SpongeKeyCategory category : this.keyCategories) {
            buf.writeShort((short) category.getInternalId());
            buf.writeString(category.getId());
            buf.writeString(TextSerializers.JSON.serialize(category.getTitle()));
        }
        buf.writeShort((short) this.keyBindings.size());
        for (SpongeKeyBinding keyBinding : this.keyBindings) {
            buf.writeShort((short) keyBinding.getInternalId());
            buf.writeString(keyBinding.getId());
            buf.writeShort((short) keyBinding.getCategory().getInternalId());
            buf.writeString(TextSerializers.JSON.serialize(keyBinding.getDisplayName()));
        }
    }
}
