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
package org.spongepowered.common.mixin.core.command.arguments;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import org.objectweb.asm.Opcodes;
import net.minecraft.command.arguments.ArgumentTypes;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.command.arguments.IArgumentSerializer;
import net.minecraft.command.arguments.serializers.DoubleArgumentSerializer;
import net.minecraft.command.arguments.serializers.FloatArgumentSerializer;
import net.minecraft.command.arguments.serializers.IntArgumentSerializer;
import net.minecraft.command.arguments.serializers.LongArgumentSerializer;
import net.minecraft.command.arguments.serializers.StringArgumentSerializer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.api.command.registrar.tree.ClientCompletionKey;
import org.spongepowered.api.command.registrar.tree.CommandTreeBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.common.bridge.command.arguments.ArgumentTypes_EntryBridge;
import org.spongepowered.common.command.registrar.tree.EmptyCommandTreeBuilder;
import org.spongepowered.common.command.registrar.tree.EntityCommandTreeBuilder;
import org.spongepowered.common.command.registrar.tree.RangeCommandTreeBuilder;
import org.spongepowered.common.command.registrar.tree.StringCommandTreeBuilder;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

@Mixin(ArgumentTypes.class)
public abstract class ArgumentTypesMixin {
/*
    // Targeting the LAST put.
    @Redirect(method = "register",
            slice = @Slice(
                    from = @At(value = "FIELD", opcode = Opcodes.GETSTATIC, target = "net/minecraft/command/arguments/ArgumentTypes.ID_TYPE_MAP:Ljava/util/Map;"),
                    to = @At("TAIL")),
            at = @At(value = "INVOKE", target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"))
    private static <T extends ArgumentType<?>> Object impl$registerClientCompletionKey(
            final Map<ResourceLocation, Object> idTypeMap,
            @Coerce final Object resourceLocation,
            @Coerce final Object entry,
            final String namespace,
            final Class<?> argumentType,
            final IArgumentSerializer<?> serializer) {

        if (serializer instanceof DoubleArgumentSerializer) {
            impl$addToValue(entry,
                    (Function<ClientCompletionKey<CommandTreeBuilder.Range<Double>>, CommandTreeBuilder.Range<Double>>)
                            (k -> new RangeCommandTreeBuilder<>(k, impl$doubleSerializer((DoubleArgumentSerializer) serializer))));
        } else if (serializer instanceof FloatArgumentSerializer) {
            impl$addToValue(entry,
                    (Function<ClientCompletionKey<CommandTreeBuilder.Range<Float>>, CommandTreeBuilder.Range<Float>>)
                            (k -> new RangeCommandTreeBuilder<>(k, impl$floatSerializer((FloatArgumentSerializer) serializer))));
        } else if (serializer instanceof IntArgumentSerializer) {
            impl$addToValue(entry,
                    (Function<ClientCompletionKey<CommandTreeBuilder.Range<Integer>>, CommandTreeBuilder.Range<Integer>>)
                            (k -> new RangeCommandTreeBuilder<>(k, impl$intSerializer((IntArgumentSerializer) serializer))));
        } else if (serializer instanceof LongArgumentSerializer) {
            impl$addToValue(entry,
                    (Function<ClientCompletionKey<CommandTreeBuilder.Range<Long>>, CommandTreeBuilder.Range<Long>>)
                            (k -> new RangeCommandTreeBuilder<>(k, impl$longSerializer((LongArgumentSerializer) serializer))));
        } else if (serializer instanceof StringArgumentSerializer) {
            impl$addToValue(entry, StringCommandTreeBuilder::new);
        } else if (serializer instanceof EntityArgument) {
            impl$addToValue(entry, EntityCommandTreeBuilder::new);
        } else {
            // TODO: this is obviously not right, but I need this to compile.
            impl$addToValue(entry, (ClientCompletionKey<CommandTreeBuilder.Basic> parameterType) -> new EmptyCommandTreeBuilder(parameterType,
                    StringArgumentType.greedyString()));
        }

        // Abusing generics here...
       return idTypeMap.put((ResourceLocation) resourceLocation, entry);
    }
*/
    private static <T extends CommandTreeBuilder<T>> void impl$addToValue(final Object value, final Function<ClientCompletionKey<T>, T> key) {
        ((ArgumentTypes_EntryBridge<?, T>) value).bridge$setCommandTreeBuilderProvider(key);
    }

    private static BiConsumer<PacketBuffer, RangeCommandTreeBuilder<Double>> impl$doubleSerializer(
            final DoubleArgumentSerializer serializer) {
        return (buffer, commandTreeBuilder) -> {
            final double min = commandTreeBuilder.getMin().orElse(-Double.MAX_VALUE);
            final double max = commandTreeBuilder.getMax().orElse(Double.MAX_VALUE);
            serializer.write(DoubleArgumentType.doubleArg(min, max), buffer);
        };
    }

    private static BiConsumer<PacketBuffer, RangeCommandTreeBuilder<Integer>> impl$intSerializer(
            final IntArgumentSerializer serializer) {
        return (buffer, commandTreeBuilder) -> {
            final int min = commandTreeBuilder.getMin().orElse(-Integer.MAX_VALUE);
            final int max = commandTreeBuilder.getMax().orElse(Integer.MAX_VALUE);
            serializer.write(IntegerArgumentType.integer(min, max), buffer);
        };
    }

    private static BiConsumer<PacketBuffer, RangeCommandTreeBuilder<Long>> impl$longSerializer(
            final LongArgumentSerializer serializer) {
        return (buffer, commandTreeBuilder) -> {
            final long min = commandTreeBuilder.getMin().orElse(-Long.MAX_VALUE);
            final long max = commandTreeBuilder.getMax().orElse(Long.MAX_VALUE);
            serializer.write(LongArgumentType.longArg(min, max), buffer);
        };
    }

    private static BiConsumer<PacketBuffer, RangeCommandTreeBuilder<Float>> impl$floatSerializer(
            final FloatArgumentSerializer serializer) {
        return (buffer, commandTreeBuilder) -> {
            final float min = commandTreeBuilder.getMin().orElse(-Float.MAX_VALUE);
            final float max = commandTreeBuilder.getMax().orElse(Float.MAX_VALUE);
            serializer.write(FloatArgumentType.floatArg(min, max), buffer);
        };
    }

}
