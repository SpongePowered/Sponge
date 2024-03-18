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
package org.spongepowered.vanilla.generator;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.lang.model.element.Modifier;

/**
 * Generates the registration of catalog classes for {@link BlockState} properties.
 */
public class BlockStateDataProviderGenerator implements Generator {

    @Override
    public String name() {
        return "block state data provider";
    }

    @Override
    public void generate(Context ctx) throws IOException {
        final var clazz = Types.utilityClass(
                "BlockStateDataProvider",
                "<!-- Copy to the register method of BlockStateDataProvider -->"
        );

        final var codeBlock = CodeBlock.builder();
        for (final String property : this.vanillaProperties()) {
            codeBlock.addStatement("BlockStateDataProvider.registerProperty(registrator, BlockStateKeys.$L, BlockStateProperties.$L)", property, property);
        }

        clazz.addMethod(MethodSpec.methodBuilder("register")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addCode(codeBlock.build())
                .build()
        );

        ctx.write("data", clazz.build());

        final var cu = ctx.compilationUnit("data", "BlockStateDataProvider");
    }

    private Set<String> vanillaProperties() {
        return Arrays.stream(BlockStateProperties.class.getDeclaredFields())
                .filter(f -> Property.class.isAssignableFrom(f.getType()))
                .map(Field::getName)
                .collect(Collectors.toCollection(TreeSet::new));
    }
}
