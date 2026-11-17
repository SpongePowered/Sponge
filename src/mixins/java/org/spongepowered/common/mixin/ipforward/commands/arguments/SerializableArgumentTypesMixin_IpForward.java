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
package org.spongepowered.common.mixin.ipforward.commands.arguments;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.netty.buffer.Unpooled;
import net.minecraft.commands.arguments.SerializableArgument;
import net.minecraft.commands.arguments.SerializableArgumentTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * If a proxy server tries to inspect the commands sent
 * to the client, it is unable to parse the elements
 * that contain mod specific information due to being
 * unaware of their structure.
 *
 * To fix this, the proxy server expects that non-vanilla
 * elements are wrapped in a magic argument that it can
 * detect to skip its content.
 *
 * The proxy server is expected to unwrap these arguments
 * afterward to allow the client to parse the commands
 * as normal.
 */
@Mixin(SerializableArgumentTypes.class)
public abstract class SerializableArgumentTypesMixin_IpForward {

    //Magic value that was chosen by the proxy implementation.
    private static final int ipForward$MOD_ARGUMENT_ID = -256;

    @ModifyExpressionValue(method = "<clinit>", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/network/codec/StreamCodec;dispatch(Ljava/util/function/Function;Ljava/util/function/Function;)Lnet/minecraft/network/codec/StreamCodec;"))
    private static StreamCodec<RegistryFriendlyByteBuf, SerializableArgument<?>> ipForward$wrapModArguments(
        final StreamCodec<RegistryFriendlyByteBuf, SerializableArgument<?>> original) {
        return new StreamCodec<>() {
            @Override
            public SerializableArgument<?> decode(final RegistryFriendlyByteBuf buf) {
                return original.decode(buf);
            }

            @Override
            public void encode(final RegistryFriendlyByteBuf buf, final SerializableArgument<?> argument) {
                final @Nullable Identifier key = BuiltInRegistries.COMMAND_ARGUMENT_TYPE.getKey(argument.type());
                if (key == null || SerializableArgumentTypesMixin_IpForward.ipForward$isVanillaArgument(key)) {
                    original.encode(buf, argument);
                    return;
                }

                // The type id comes first, the argument's own data follows
                final RegistryFriendlyByteBuf commandData = new RegistryFriendlyByteBuf(Unpooled.buffer(), buf.registries(), buf.ids());
                original.encode(commandData, argument);
                final int id = commandData.readVarInt();

                buf.writeVarInt(SerializableArgumentTypesMixin_IpForward.ipForward$MOD_ARGUMENT_ID);
                buf.writeVarInt(id);
                buf.writeVarInt(commandData.readableBytes());
                buf.writeBytes(commandData);
            }
        };
    }

    private static boolean ipForward$isVanillaArgument(final Identifier key) {
        return switch (key.getNamespace()) {
            case "brigadier" -> true;
            case "minecraft" -> switch (key.getPath()) {
                case "test_argument", "test_class" -> false; //These are normally under IS_RUNNING_IN_IDE, proxies cant handle them.
                default -> true;
            };
            default -> false;
        };
    }
}
