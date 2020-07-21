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

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.command.arguments.NBTCompoundTagArgument;
import net.minecraft.command.arguments.ResourceLocationArgument;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.command.parameter.managed.clientcompletion.ClientCompletionType;
import org.spongepowered.common.command.brigadier.argument.CustomArgumentParser;
import org.spongepowered.common.command.parameter.managed.clientcompletion.SpongeClientCompletionType;

import java.util.stream.Stream;

public final class ClientCompletionTypeStreamGenerator {

    private ClientCompletionTypeStreamGenerator() {
    }

    public static Stream<ClientCompletionType> stream() {
        return Stream.of(
                new SpongeClientCompletionType("decimal_number", DoubleArgumentType.doubleArg()),
                new SpongeClientCompletionType("json", NBTCompoundTagArgument.nbt()),
                CustomArgumentParser.NONE_CLIENT_COMPLETION_TYPE,
                new SpongeClientCompletionType("resource_key", ResourceLocationArgument.resourceLocation()),
                new SpongeClientCompletionType("string", StringArgumentType.string()),
                new SpongeClientCompletionType("whole_number", LongArgumentType.longArg())
        );
    }

}
