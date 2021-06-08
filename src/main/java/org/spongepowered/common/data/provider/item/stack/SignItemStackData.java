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
package org.spongepowered.common.data.provider.item.stack;

import com.google.common.collect.Lists;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.api.data.Keys;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.common.data.provider.DataProviderRegistrator;
import org.spongepowered.common.util.Constants;

import java.util.List;

public final class SignItemStackData {

    private SignItemStackData() {
    }

    // @formatter:off
    public static void register(final DataProviderRegistrator registrator) {
        registrator
                .asMutable(ItemStack.class)
                    .create(Keys.SIGN_LINES)
                        .get(h -> {
                            final CompoundTag tag = h.getTagElement(Constants.Item.BLOCK_ENTITY_TAG);
                            if (tag == null) {
                                return null;
                            }
                            final String id = tag.getString(Constants.Item.BLOCK_ENTITY_ID);
                            if (!id.equalsIgnoreCase(Constants.TileEntity.SIGN)) {
                                return null;
                            }
                          final GsonComponentSerializer gcs = GsonComponentSerializer.gson();
                            final List<Component> texts = Lists.newArrayListWithCapacity(4);
                            for (int i = 0; i < 4; i++) {
                                texts.add(gcs.deserialize(tag.getString("Text" + (i + 1))));
                            }
                            return texts;
                        })
                        .set((h, v) -> {
                            final GsonComponentSerializer gcs = GsonComponentSerializer.gson();
                            final CompoundTag tag = h.getOrCreateTagElement(Constants.Item.BLOCK_ENTITY_TAG);
                            tag.putString(Constants.Item.BLOCK_ENTITY_ID, Constants.TileEntity.SIGN);
                            for (int i = 0; i < 4; i++) {
                                final Component line = v.size() > i ? v.get(i) : Component.empty();
                                if (line == null) {
                                    throw new IllegalArgumentException("A null line was given at index " + i);
                                }
                                tag.putString("Text" + (i + 1), gcs.serialize(line));
                            }
                        })
                        .delete(h -> h.removeTagKey(Constants.Item.BLOCK_ENTITY_TAG));
    }
    // @formatter:on
}
