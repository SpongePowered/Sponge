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

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

import java.io.IOException;
import java.util.Comparator;
import java.util.Locale;

import javax.lang.model.element.Modifier;

public final class TagGenerator implements Generator {

    private static final RegistryScope SCOPE = RegistryScope.GAME;

    private final String registryTypeName;
    private final ResourceKey<? extends Registry<?>> taggedRegistry;
    private final TypeName typeName;
    private final String relativePackageName;
    private final String targetClassSimpleName;

    public TagGenerator(
        final String registryTypeName,
        final ResourceKey<? extends Registry<?>> taggedRegistry,
        final TypeName typeName,
        final String relativePackageName,
        final String targetClassSimpleName
    ) {
        this.registryTypeName = registryTypeName;
        this.taggedRegistry = taggedRegistry;
        this.typeName = typeName;
        this.relativePackageName = relativePackageName;
        this.targetClassSimpleName = targetClassSimpleName;
    }

    @Override
    public String name() {
        return "elements of tag registry " + this.taggedRegistry;
    }

    @Override
    public void generate(final Context ctx) throws IOException {
        final var clazz = Types.utilityClass(
                this.targetClassSimpleName,
                Generator.GENERATED_FILE_JAVADOCS
        );
        clazz.addAnnotation(Types.suppressWarnings("unused"));
        clazz.addAnnotation(TagGenerator.SCOPE.registryScopeAnnotation());

        final var fieldType = ParameterizedTypeName.get(Types.TAG, this.typeName);
        final ParameterSpec locationParam = ParameterSpec.builder(Types.RESOURCE_KEY, "key", Modifier.FINAL).build();
        final var factoryMethod = MethodSpec.methodBuilder("key")
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                .returns(fieldType)
                .addParameter(locationParam)
                .addCode(
                        "return $T.of($T.$L, $N);",
                        Types.TAG,
                        Types.REGISTRY_TYPES,
                        this.registryTypeName.toUpperCase(Locale.ROOT),
                        locationParam
                ).build();


        ctx.registries().registryOrThrow(this.taggedRegistry).getTagNames()
            .<ResourceLocation>map(TagKey::location)
            .sorted(Comparator.naturalOrder())
            .map(v -> this.makeField(this.targetClassSimpleName, fieldType, factoryMethod, v))
            .forEachOrdered(clazz::addField);

        clazz.addMethod(factoryMethod);

        ctx.write(this.relativePackageName, clazz.build());

        // Then fix up before/after comments
        ctx.compilationUnit(this.relativePackageName, this.targetClassSimpleName);
    }

    private FieldSpec makeField(final String ownType, final TypeName fieldType, final MethodSpec factoryMethod, final ResourceLocation element) {
        return FieldSpec.builder(fieldType, Types.keyToFieldName(element.getPath()), Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer("$L.$N($L)", ownType, factoryMethod, Types.resourceKey(element))
                .build();
    }
}
