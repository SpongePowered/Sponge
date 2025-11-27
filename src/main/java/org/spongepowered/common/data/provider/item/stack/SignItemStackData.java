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

import com.mojang.serialization.DynamicOps;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.entity.SignText;
import org.spongepowered.api.data.Keys;
import org.spongepowered.common.SpongeCommon;
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
                            final CompoundTag tag = h.getOrDefault(DataComponents.BLOCK_ENTITY_DATA, CustomData.EMPTY).copyTag();
                            if (tag.isEmpty()) {
                                return null;
                            }
                            final String id = tag.getString(Constants.Item.BLOCK_ENTITY_ID);
                            if (!id.equalsIgnoreCase(Constants.TileEntity.SIGN)) {
                                return null;
                            }
                            DynamicOps<Tag> $$2 = SpongeCommon.vanillaRegistryAccess().createSerializationContext(NbtOps.INSTANCE);

                            if (tag.contains("front_text")) {
                                return SignText.DIRECT_CODEC
                                    .parse($$2, tag.getCompound("front_text"))
                                    .resultOrPartial()
                                    .map(t -> t.getMessages(false))
                                    .map(List::of)
                                    .stream()
                                    .flatMap(List::stream)
                                    .map(SpongeAdventure::asAdventure)
                                    .toList();
                            }
                            return List.of();
                        })
                        .set((h, v) -> {
                            final CompoundTag tag = new CompoundTag();
                            tag.putString(Constants.Item.BLOCK_ENTITY_ID, Constants.TileEntity.SIGN);
                            DynamicOps<Tag> $$2 = SpongeCommon.vanillaRegistryAccess().createSerializationContext(NbtOps.INSTANCE);
                            final var text = new SignText();
                            for (int i = 0; i < v.size(); ++i) {
                                if (i > 3) {
                                    break;
                                }
                                final var translated = SpongeAdventure.asVanilla(v.get(i));
                                if (translated == null) {
                                    continue;
                                }
                                text.setMessage(i, translated);
                            }
                            SignText.DIRECT_CODEC.encodeStart($$2, text)
                                .resultOrPartial(SpongeCommon.logger()::error)
                                .ifPresent($$1x -> tag.put("front_text", $$1x));

                            h.set(DataComponents.BLOCK_ENTITY_DATA, CustomData.of(tag));
                        })
                        .delete(h -> h.remove(DataComponents.BLOCK_ENTITY_DATA));
    }
    // @formatter:on
}
