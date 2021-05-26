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

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;
import net.minecraft.resources.ResourceLocation;

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

import javax.lang.model.element.Modifier;

final class Types {

    private static final Pattern ILLEGAL_FIELD_CHARACTERS = Pattern.compile("[./-]");

    public static final String NAMESPACE_SPONGE = "sponge";

    public static final WildcardTypeName WILDCARD = WildcardTypeName.subtypeOf(TypeName.OBJECT);

    public static final ClassName RESOURCE_KEY = ClassName.get(Context.BASE_PACKAGE, "ResourceKey");

    public static final ClassName SPONGE = ClassName.get(Context.BASE_PACKAGE, "Sponge");

    public static final ClassName DEFAULTED_REGISTRY_REFERENCE = ClassName.get(Context.BASE_PACKAGE + ".registry", "DefaultedRegistryReference");

    public static final ClassName REGISTRY_KEY = ClassName.get(Context.BASE_PACKAGE + ".registry", "RegistryKey");

    public static final ClassName REGISTRY_REFERENCE = ClassName.get(Context.BASE_PACKAGE + ".registry", "RegistryReference");

    public static final ClassName REGISTRY_SCOPES = ClassName.get(Context.BASE_PACKAGE + ".registry", "RegistryScopes");

    public static final ClassName REGISTRY_SCOPE = ClassName.get(Context.BASE_PACKAGE + ".registry", "RegistryScope");

    public static final ClassName REGISTRY_TYPES = ClassName.get(Context.BASE_PACKAGE + ".registry", "RegistryTypes");


    private Types() {
    }

    /**
     * Create a utility class that is final and has a private constructor.
     *
     * @param name class name
     * @param classJd javadoc to apply to the class
     * @param args arguments for the Javadoc
     * @return a configured type builder
     */
    public static TypeSpec.Builder utilityClass(final String name, final String classJd, final Object... args) {
        final var classBuilder = TypeSpec.classBuilder(name)
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .addMethod(MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PRIVATE)
                .build());
        if (classJd != null) {
            classBuilder.addJavadoc(classJd, args);
        }

        return classBuilder;
    }

    static String keyToFieldName(final String key) {
        return Types.ILLEGAL_FIELD_CHARACTERS.matcher(key.toUpperCase(Locale.ROOT)).replaceAll("_");
    }

    public static CodeBlock resourceKey(final ResourceLocation location) {
        Objects.requireNonNull(location, "location");
        return Types.resourceKey(location.getNamespace(), location.getPath());
    }

    public static CodeBlock resourceKey(final String namespace, final String path) {
        return switch (namespace) {
            case "minecraft" -> CodeBlock.of("$T.minecraft($S)", Types.RESOURCE_KEY, path);
            case "brigadier" -> CodeBlock.of("$T.brigadier($S)", Types.RESOURCE_KEY, path);
            case Types.NAMESPACE_SPONGE -> CodeBlock.of("$T.sponge($S)", Types.RESOURCE_KEY, path);
            default -> CodeBlock.of("$T.of($S, $S)", Types.RESOURCE_KEY, namespace, path);
        };
    }

    public static AnnotationSpec suppressWarnings(final String... values) {
        final var builder = AnnotationSpec.builder(SuppressWarnings.class);
        for (final String value : values) {
            builder.addMember("value", "$S", value);
        }
        return builder.build();
    }

}
