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
import com.squareup.javapoet.MethodSpec.Builder;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import java.util.Locale;
import java.util.Objects;

import javax.lang.model.element.Modifier;

enum RegistryScope {
    GAME {
        @Override
        protected CodeBlock registryKeyToReference() {
            return CodeBlock.of("asDefaultedReference($T::game)", Types.SPONGE);
        }

        @Override
        ClassName registryReferenceType() {
            return Types.DEFAULTED_REGISTRY_REFERENCE;
        }

        @Override
        AnnotationSpec registryScopeAnnotation() {
            return RegistryScope.registryScopeAnnotation("GAME");
        }

        @Override
        void populateRegistryGetter(final Builder builder, final CodeBlock registryType) {
            builder.addCode("return $T.game().registry($L);", Types.SPONGE, registryType);
        }
    },
    SERVER {
        @Override
        protected CodeBlock registryKeyToReference() {
            return CodeBlock.of("asDefaultedReference($T::server)", Types.SPONGE);
        }

        @Override
        ClassName registryReferenceType() {
            return Types.DEFAULTED_REGISTRY_REFERENCE;
        }

        @Override
        AnnotationSpec registryScopeAnnotation() {
            return RegistryScope.registryScopeAnnotation("ENGINE");
        }

        @Override
        void populateRegistryGetter(final Builder builder, final CodeBlock registryType) {
            builder.addCode("return $T.server().registry($L);", Types.SPONGE, registryType);
        }
    },
    WORLD {
        @Override
        protected CodeBlock registryKeyToReference() {
            return CodeBlock.of("asReference()");
        }

        @Override
        ClassName registryReferenceType() {
            return Types.REGISTRY_REFERENCE;
        }

        @Override
        AnnotationSpec registryScopeAnnotation() {
            return RegistryScope.registryScopeAnnotation("WORLD");
        }

        @Override
        void populateRegistryGetter(
            final Builder builder,
            final CodeBlock registryType
        ) {
            final var holderParam = ParameterSpec.builder(Types.SERVER_WORLD, "world", Modifier.FINAL).build();
            builder.addParameter(holderParam);
            builder.addCode("return $N.registry($L);", holderParam, registryType);
        }
    };

    protected static AnnotationSpec registryScopeAnnotation(final String registryScope) {
        Objects.requireNonNull(registryScope, "registryScope");
        return AnnotationSpec.builder(Types.REGISTRY_SCOPES)
            .addMember("scopes", "$T.$L", Types.REGISTRY_SCOPE, registryScope.toUpperCase(Locale.ROOT))
            .build();
    }

    protected abstract CodeBlock registryKeyToReference();

    abstract ClassName registryReferenceType();

    abstract AnnotationSpec registryScopeAnnotation();

    abstract void populateRegistryGetter(final MethodSpec.Builder builder, final CodeBlock registryType);

    final MethodSpec registryReferenceFactory(final String registryTypeName, final TypeName valueType) {
        final var locationParam = ParameterSpec.builder(Types.RESOURCE_KEY, "location", Modifier.FINAL).build();
        return MethodSpec.methodBuilder("key")
            .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
            .returns(ParameterizedTypeName.get(this.registryReferenceType(), valueType))
            .addParameter(locationParam)
            .addCode(
                "return $T.of($T.$L, $N).$L;",
                Types.REGISTRY_KEY,
                Types.REGISTRY_TYPES,
                registryTypeName.toUpperCase(Locale.ROOT),
                locationParam,
                this.registryKeyToReference()
            ).build();
    }

    final MethodSpec registryGetter(final String registryTypeName, final TypeName valueType) {
        final var methodBuilder = MethodSpec.methodBuilder("registry")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .returns(ParameterizedTypeName.get(Types.REGISTRY, valueType));

        final CodeBlock registryType = CodeBlock.of("$T.$L", Types.REGISTRY_TYPES, registryTypeName.toUpperCase(Locale.ROOT));
        this.populateRegistryGetter(methodBuilder, registryType);
        return methodBuilder.build();
    }
}
