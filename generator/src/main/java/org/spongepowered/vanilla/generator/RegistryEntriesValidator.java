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

import static com.github.javaparser.ast.Modifier.createModifierList;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.Modifier.Keyword;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

// Generates a constants file based on registry entries
class RegistryEntriesValidator<V> implements Generator {

    private final String relativePackageName;
    private final String targetClassSimpleName;
    private final ResourceKey<? extends Registry<V>> registry;
    private final Predicate<V> filter;
    private final Set<ResourceLocation> extraEntries;

    RegistryEntriesValidator(
        final String targetRelativePackage,
        final String targetClassSimpleName,
        final ResourceKey<? extends Registry<V>> registry
    ) {
        this(targetRelativePackage, targetClassSimpleName, registry, $ -> true, Set.of());
    }

    RegistryEntriesValidator(
        final String targetRelativePackage,
        final String targetClassSimpleName,
        final ResourceKey<? extends Registry<V>> registry,
        final Predicate<V> filter,
        final Set<ResourceLocation> extraEntries
    ) {
        this.relativePackageName = targetRelativePackage;
        this.targetClassSimpleName = targetClassSimpleName;
        this.registry = registry;
        this.filter = filter;
        this.extraEntries = Set.copyOf(extraEntries);
    }

    @Override
    public String name() {
        return "elements of registry " + this.registry.location();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void generate(final Context ctx) {
        // We read an existing class, then go through existing fields, make sure they exist, and add any new ones.
        final var compilationUnit = ctx.compilationUnit(this.relativePackageName, this.targetClassSimpleName);
        final var primaryTypeDeclaration = compilationUnit.getPrimaryType()
            .orElseThrow(() -> new IllegalStateException("Could not find primary type for registry type " + this.targetClassSimpleName));

        Registry<V> registry = ctx.registries().registry(this.registry).orElse(null);
        if (registry == null) {
            registry = (Registry<V>) Registry.REGISTRY.get(this.registry.location());
            if (registry == null) {
                throw new IllegalArgumentException("Unknown registry " + this.registry);
            }
        }

        // Find index of first field member
        // Take out all field members from the members list
        final var members = primaryTypeDeclaration.getMembers();
        final var fields = new HashMap<ResourceLocation, FieldDeclaration>();
        int lastNonFieldIndex = -1;
        for (final var it = members.listIterator(); it.hasNext();) {
            final var node = it.next();
            if (lastNonFieldIndex == -1 && node instanceof FieldDeclaration) {
                lastNonFieldIndex = it.nextIndex() - 1;
            }

            if (node instanceof FieldDeclaration) {
                final var field = (FieldDeclaration) node;
                fields.put(this.extractFieldIdentifier(field), field);
                it.remove();
            }
        }

        // Now, iterate the registry, discovering which fields were added and removed
        final var added = new HashSet<ResourceLocation>();
        final var processedFields = new ArrayList<FieldDeclaration>(registry.keySet().size());
        final Set<ResourceLocation> allKeys = new HashSet<>(registry.keySet());
        allKeys.addAll(this.extraEntries);
        for (final ResourceLocation key : allKeys) {
            if (!this.filter.test(registry.get(key))) {
                continue;
            }

            final FieldDeclaration existing = fields.remove(key);
            if (existing != null) {
                processedFields.add(existing);
            } else {
                added.add(key);
                processedFields.add(this.makeField(this.targetClassSimpleName, "key", key));
            }
        }

        // Sort field entries and add them back to the class
        processedFields.sort(Comparator.comparing(field -> field.getVariable(0).getNameAsString()));
        primaryTypeDeclaration.getMembers().addAll(lastNonFieldIndex, processedFields);

        if (!added.isEmpty()) {
            Logger.info("Added {} entries to {} that will require manual action to implement: {}", added.size(), this.targetClassSimpleName, added);
        }
        if (!fields.isEmpty()) {
            Logger.info("Removed {} entries from {} because they are no longer present in the game: {}", fields.size(), this.targetClassSimpleName, fields.keySet());
        }
    }

    // Attempt to get a resource location from the field by parsing its initializer
    private ResourceLocation extractFieldIdentifier(final FieldDeclaration declaration) {
        if (declaration.getVariables().isEmpty()) {
            throw new IllegalStateException("No variables for " + declaration);
        }
        final VariableDeclarator var = declaration.getVariable(0);
        final Expression initializer = var.getInitializer().orElse(null);
        if (!(initializer instanceof MethodCallExpr) || ((MethodCallExpr) initializer).getArguments().size() != 1) {
            return new ResourceLocation(var.getNameAsString().toLowerCase(Locale.ROOT)); // a best guess
        }

        final Expression argument = ((MethodCallExpr) initializer).getArgument(0);
        if (!(argument instanceof final MethodCallExpr keyInitializer)
                || keyInitializer.getArguments().size() < 1) {
            return new ResourceLocation(var.getNameAsString().toLowerCase(Locale.ROOT)); // a best guess
        }

        if (keyInitializer.getArguments().size() == 1) { // method name as namespace
            return new ResourceLocation(keyInitializer.getNameAsString(), keyInitializer.getArgument(0).asStringLiteralExpr().asString());
        } else if (keyInitializer.getArguments().size() == 2) { // (namespace, path)
            return new ResourceLocation(
                keyInitializer.getArgument(0).asStringLiteralExpr().asString(),
                keyInitializer.getArgument(1).asStringLiteralExpr().asString()
            );
        } else {
            return new ResourceLocation(var.getNameAsString().toLowerCase(Locale.ROOT)); // a best guess
        }

    }

    private FieldDeclaration makeField(final String ownType, final String factoryMethod, final ResourceLocation element) {
        final FieldDeclaration fieldDeclaration = new FieldDeclaration();
        final VariableDeclarator variable = new VariableDeclarator(StaticJavaParser.parseType("DefaultedRegistryReference<FixMe>"), Types.keyToFieldName(element.getPath()));
        fieldDeclaration.getVariables().add(variable);
        fieldDeclaration.setModifiers(createModifierList(Keyword.PUBLIC, Keyword.STATIC, Keyword.FINAL));
        variable.setInitializer(new MethodCallExpr(new NameExpr(ownType), factoryMethod, new NodeList<>(RegistryEntriesValidator.resourceKey(element))));
        return fieldDeclaration;
    }

    public static MethodCallExpr resourceKey(final ResourceLocation location) {
        Objects.requireNonNull(location, "location");
        final var resourceKey = new NameExpr("ResourceKey");
        return switch (location.getNamespace()) {
            case "minecraft" -> new MethodCallExpr(resourceKey, "minecraft", new NodeList<>(new StringLiteralExpr(location.getPath())));
            case "brigadier" -> new MethodCallExpr(resourceKey, "brigadier", new NodeList<>(new StringLiteralExpr(location.getPath())));
            case "sponge" -> new MethodCallExpr(resourceKey, "sponge", new NodeList<>(new StringLiteralExpr(location.getPath())));
            default -> new MethodCallExpr(
                resourceKey, "of", new NodeList<>(new StringLiteralExpr(location.getNamespace()), new StringLiteralExpr(location.getPath())));
        };
    }
}
