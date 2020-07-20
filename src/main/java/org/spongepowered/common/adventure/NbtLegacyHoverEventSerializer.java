/*
 * This file is part of adventure, licensed under the MIT License.
 *
 * Copyright (c) 2020 KyoriPowered
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.spongepowered.common.adventure;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.api.BinaryTagHolder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.gson.LegacyHoverEventSerializer;
import net.kyori.adventure.text.serializer.plain.PlainComponentSerializer;
import net.kyori.adventure.util.Codec;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.JsonToNBT;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.IOException;
import java.util.UUID;

public final class NbtLegacyHoverEventSerializer implements LegacyHoverEventSerializer {
    public static final NbtLegacyHoverEventSerializer INSTANCE = new NbtLegacyHoverEventSerializer();
    private static final Codec<CompoundNBT, String, CommandSyntaxException, RuntimeException> SNBT_CODEC = Codec.of(JsonToNBT::getTagFromJson, INBT::toString);

    static final String ITEM_TYPE = "id";
    static final String ITEM_COUNT = "Count";
    static final String ITEM_TAG = "tag";

    static final String ENTITY_NAME = "name";
    static final String ENTITY_TYPE = "type";
    static final String ENTITY_ID = "id";

    private NbtLegacyHoverEventSerializer() {
    }

    @Override
    public HoverEvent.ShowItem deserializeShowItem(final Component input) throws IOException {
        final String rawContent = PlainComponentSerializer.plain().serialize(input);
        try {
            final CompoundNBT contents = SNBT_CODEC.decode(rawContent);
            final CompoundNBT tag = contents.getCompound(ITEM_TAG);
            return new HoverEvent.ShowItem(
                Key.of(contents.getString(ITEM_TYPE)),
                contents.contains(ITEM_COUNT) ? contents.getByte(ITEM_COUNT) : 1,
                tag.isEmpty() ? null : BinaryTagHolder.encode(tag, SNBT_CODEC)
            );
        } catch (final CommandSyntaxException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public HoverEvent.@NonNull ShowEntity deserializeShowEntity(final Component input, final Codec.Decoder<Component, String, ? extends RuntimeException> componentCodec) throws IOException {
        final String raw = PlainComponentSerializer.plain().serialize(input);
        try {
            final CompoundNBT contents = SNBT_CODEC.decode(raw);
            return new HoverEvent.ShowEntity(
                Key.of(contents.getString(ENTITY_TYPE)),
                UUID.fromString(contents.getString(ENTITY_ID)),
                componentCodec.decode(contents.getString(ENTITY_NAME))
            );
        } catch (final CommandSyntaxException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public @NonNull Component serializeShowItem(final HoverEvent.@NonNull ShowItem input) throws IOException {
        final CompoundNBT tag = new CompoundNBT();
        tag.putString(ITEM_TYPE, input.item().asString());
        tag.putByte(ITEM_COUNT, (byte) input.count());
        if (input.nbt() != null) {
            try {
                tag.put(ITEM_TAG, input.nbt().get(SNBT_CODEC));
            } catch (final CommandSyntaxException ex) {
                throw new IOException(ex);
            }
        }

        return TextComponent.of(SNBT_CODEC.encode(tag));
    }

    @Override
    public @NonNull Component serializeShowEntity(final HoverEvent.ShowEntity input, final Codec.Encoder<Component, String, ? extends RuntimeException> componentCodec) {
        final CompoundNBT tag = new CompoundNBT();
        tag.putString(ENTITY_ID, input.id().toString());
        tag.putString(ENTITY_TYPE, input.type().asString());
        if (input.name() != null) {
            tag.putString(ENTITY_NAME, componentCodec.encode(input.name()));
        }
        return TextComponent.of(SNBT_CODEC.encode(tag));
    }
}
