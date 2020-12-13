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
package org.spongepowered.common.registry.builtin.sponge;

import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.command.arguments.IArgumentSerializer;
import net.minecraft.command.arguments.ScoreHolderArgument;
import net.minecraft.command.arguments.serializers.StringArgumentSerializer;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.command.registrar.tree.ClientCompletionKey;
import org.spongepowered.common.accessor.command.arguments.ArgumentSerializerAccessor;
import org.spongepowered.common.accessor.command.arguments.ArgumentTypesAccessor;
import org.spongepowered.common.accessor.command.arguments.ArgumentTypes_EntryAccessor;
import org.spongepowered.common.command.registrar.tree.key.SpongeAmountClientCompletionKey;
import org.spongepowered.common.command.registrar.tree.key.SpongeBasicClientCompletionKey;
import org.spongepowered.common.command.registrar.tree.key.SpongeEntityClientCompletionKey;
import org.spongepowered.common.command.registrar.tree.key.SpongeRangeClientCompletionKey;
import org.spongepowered.common.command.registrar.tree.key.SpongeStringClientCompletionKey;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

public final class ClientCompletionKeyStreamGenerator {

    private ClientCompletionKeyStreamGenerator() {
    }

    public static Stream<? extends ClientCompletionKey<?>> stream() {
        return ArgumentTypesAccessor.accessor$BY_NAME()
                .entrySet()
                .stream()
                .map(ClientCompletionKeyStreamGenerator::transform)
                .filter(Objects::nonNull);
    }

    private static ClientCompletionKey<?> transform(final Map.Entry<ResourceLocation, ArgumentTypes_EntryAccessor> entry) {
        final ResourceKey key = ResourceKey.sponge(entry.getKey().getPath());
        final IArgumentSerializer<?> serializer = entry.getValue().accessor$serializer();
        if (serializer instanceof ArgumentSerializerAccessor) {
            return new SpongeBasicClientCompletionKey(key, ((ArgumentSerializerAccessor<?>) serializer).accessor$constructor().get());
        }
        if (serializer instanceof EntityArgument.Serializer) {
            return new SpongeEntityClientCompletionKey(key);
        }
        if (serializer instanceof StringArgumentSerializer) {
            return new SpongeStringClientCompletionKey(key);
        }
        if (serializer instanceof ScoreHolderArgument.Serializer) {
            return new SpongeAmountClientCompletionKey(key, ScoreHolderArgument.scoreHolder(), ScoreHolderArgument.scoreHolders());
        }

        // Last of all, this is okay to return null as it'll get transformed out.
        return SpongeRangeClientCompletionKey.createFrom(key, serializer);
    }

}
