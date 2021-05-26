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

import com.github.javaparser.ast.body.TypeDeclaration;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import org.tinylog.Logger;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.Modifier;

/**
 * Generates catalog classes for {@link BlockState} properties.
 */
public class BlockStatePropertiesGenerator implements Generator {

    enum PropertyType {
        ENUM(
            EnumProperty.class,
            "ENUM_STATE_PROPERTY",
            "EnumStateProperties",
            ParameterizedTypeName.get(BlockStatePropertiesGenerator.inStatePkg("EnumStateProperty"), Types.WILDCARD)
        ),
        INTEGER(
            IntegerProperty.class,
            "INTEGER_STATE_PROPERTY",
            "IntegerStateProperties",
            BlockStatePropertiesGenerator.inStatePkg("IntegerStateProperty")
        ),
        BOOLEAN(
            BooleanProperty.class,
            "BOOLEAN_STATE_PROPERTY",
            "BooleanStateProperties",
            BlockStatePropertiesGenerator.inStatePkg("BooleanStateProperty")
        );

        private static final Map<Class<?>, PropertyType> knownProperties = new HashMap<>();

        private final Class<?> propertyType;
        private final String registryTypeName; // field in RegistryTypes
        private final String catalogClassName; // class, in CatalogedBy
        private final TypeName valueType; // X in DefaultedRegistryReference<X>

        PropertyType(final Class<?> propertyType, final String registryTypeName, final String catalogClassName, final TypeName valueType) {
            this.propertyType = propertyType;
            this.registryTypeName = registryTypeName;
            this.catalogClassName = catalogClassName;
            this.valueType = valueType;
        }

        static PropertyType ofProperty(final Property<?> prop) {
           final PropertyType value = PropertyType.knownProperties.get(prop.getClass());
           if (value == null) {
               for (final PropertyType type : PropertyType.knownProperties.values()) {
                   if (type.propertyType.isInstance(prop)) {
                       PropertyType.knownProperties.put(prop.getClass(), type);
                       return type;
                   }
               }
           }

           return value;
        }

        static {
            for (final PropertyType type : PropertyType.values()) {
                PropertyType.knownProperties.put(type.propertyType, type);
            }
        }
    }

    @Override
    public String name() {
        return "block state properties";
    }

    @Override
    public void generate(final Context ctx) throws IOException {
        final Map<PropertyType, Map<Property<?>, Set<ResourceLocation>>> propertyUsages = this.computeUsedProperties();

        for (final var typeToProperty : propertyUsages.entrySet()) {
            this.writeCatalogClass(ctx, typeToProperty.getKey(), typeToProperty.getValue());
        }
    }

    private Map<PropertyType, Map<Property<?>, Set<ResourceLocation>>> computeUsedProperties() {
        // get all block state properties
        final Map<PropertyType, Map<Property<?>, Set<ResourceLocation>>> propertyUsages = new HashMap<>();

        for (final Block block : Registry.BLOCK) {
            for (final Property<?> property : block.defaultBlockState().getProperties()) {
                final var type = PropertyType.ofProperty(property);
                if (type == null) {
                    Logger.warn("Unknown property type for state property {} in block {}", property, Registry.BLOCK.getKey(block));
                }

                propertyUsages.computeIfAbsent(type, $ -> new IdentityHashMap<>()).computeIfAbsent(property, $ -> new HashSet<>()).add(Registry.BLOCK.getKey(block));
            }
        }
        return propertyUsages;
    }

    private void writeCatalogClass(
        final Context ctx,
        final PropertyType type,
        final Map<Property<?>, Set<ResourceLocation>> properties
    ) throws IOException {
        final var clazz = Types.utilityClass(
            type.catalogClassName,
            "<!-- This file is automatically generated. Any manual changes will be overwritten. -->"
        );
        clazz.addAnnotation(Types.suppressWarnings("unused"));

        final var scopeType = RegistryScope.GAME;
        clazz.addAnnotation(scopeType.registryScopeAnnotation());
        final var fieldType = ParameterizedTypeName.get(scopeType.registryReferenceType(), type.valueType);
        final var factoryMethod = scopeType.registryReferenceFactory(type.registryTypeName, type.valueType);

        properties.entrySet().stream()
            .flatMap(entry -> entry.getValue().stream()
                .map(usage -> this.makeField(type.catalogClassName, fieldType, factoryMethod, entry.getKey(), usage)))
            .sorted(Comparator.comparing(field -> field.name))
            .forEachOrdered(clazz::addField);

        clazz.addMethod(factoryMethod);

        ctx.write("state", clazz.build());

        // Then fix up before/after comments
        final var cu = ctx.compilationUnit("state", type.catalogClassName);
        final TypeDeclaration<?> classPrimaryType = cu.getPrimaryType().get();

        final var fields = classPrimaryType.getFields();
        if (!fields.isEmpty()) {
            fields.get(0).setLineComment("@formatter:off");
        }

        final var constructors = classPrimaryType.getConstructors();
        if (!constructors.isEmpty()) {
            constructors.get(0).setLineComment("@formatter:on");
        }

    }

    private FieldSpec makeField(final String ownType, final TypeName fieldType, final MethodSpec factoryMethod, final Property<?> property, final ResourceLocation usage) {
        // todo: add extra information from property type (int properties)

        final String fieldName = Types.keyToFieldName(usage.getPath() + '_' + property.getName());
        return FieldSpec.builder(fieldType, fieldName, Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
            .initializer("$L.$N($L)", ownType, factoryMethod, Types.resourceKey(Types.NAMESPACE_SPONGE, fieldName.toLowerCase(Locale.ROOT)))
            .build();
    }

    private static ClassName inStatePkg(final String name) {
        return ClassName.get("org.spongepowered.api.state", name);
    }
}
