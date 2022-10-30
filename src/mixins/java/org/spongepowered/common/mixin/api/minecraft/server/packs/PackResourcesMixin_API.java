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
package org.spongepowered.common.mixin.api.minecraft.server.packs;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.IoSupplier;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.resource.Resource;
import org.spongepowered.api.resource.ResourcePath;
import org.spongepowered.api.resource.pack.PackContents;
import org.spongepowered.api.resource.pack.PackType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.resource.SpongeResource;
import org.spongepowered.common.resource.SpongeResourcePath;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Mixin(PackResources.class)
public interface PackResourcesMixin_API extends PackContents {

    // @formatter:off
    @Shadow String shadow$packId();
    @Shadow IoSupplier<InputStream> shadow$getResource(net.minecraft.server.packs.PackType var1, ResourceLocation var2) throws IOException;
    @Shadow Set<String> shadow$getNamespaces(net.minecraft.server.packs.PackType var1);
    @Shadow void shadow$listResources(net.minecraft.server.packs.PackType var1, String var2, String var3, PackResources.ResourceOutput var4);

    // @formatter:on

    @Override
    default String name() {
        return this.shadow$packId();
    }

    @Override
    default Optional<Resource> resource(final PackType root, final ResourcePath path) throws IOException {
        return Optional.ofNullable(this.api$createResource(root, path));
    }

    @Override
    default Resource requireResource(final PackType root, final ResourcePath path) throws IOException {
        final Resource resource = this.api$createResource(root, path);
        if (resource == null) {
            throw new NoSuchElementException(MessageFormat.format("Pack type {} does not contain a resource at {}", root, path));
        }
        return resource;
    }

    @Nullable
    default Resource api$createResource(final PackType root, final ResourcePath path) throws IOException {
        final net.minecraft.server.packs.PackType packType = (net.minecraft.server.packs.PackType) (Object) Objects.requireNonNull(root, "root");
        final ResourceLocation loc = (ResourceLocation) (Object) Objects.requireNonNull(path, "path").key();
        final IoSupplier<InputStream> ioSupplier = this.shadow$getResource(packType, loc);
        return new SpongeResource(path, ioSupplier.get());
    }

    @Override
    default Collection<ResourcePath> paths(final PackType root, final String namespace, final String prefix, final Predicate<ResourceKey> filter) {
        Objects.requireNonNull(filter, "filter");
        final net.minecraft.server.packs.PackType packType = (net.minecraft.server.packs.PackType) (Object) Objects.requireNonNull(root, "root");

        final Collection<ResourceLocation> resources = new HashSet<>();
        this.shadow$listResources(packType, Objects.requireNonNull(namespace, "namespace"),
                Objects.requireNonNull(prefix, "prefix"), (loc, stream) -> {
                    if (filter.test((ResourceKey) (Object) loc)) {
                        resources.add(loc);
                    }
                });

        return resources.stream()
            .map(r -> new SpongeResourcePath((ResourceKey) (Object) r))
            .collect(Collectors.toList());
    }

    @Override
    default boolean exists(final PackType root, final ResourcePath path) {
        try {
            final net.minecraft.server.packs.PackType packType = (net.minecraft.server.packs.PackType) (Object) Objects.requireNonNull(root, "root");
            final ResourceLocation loc = (ResourceLocation) (Object) Objects.requireNonNull(path, "path").key();
            final IoSupplier<InputStream> ioSupplier = this.shadow$getResource(packType, loc);
            return ioSupplier != null;
        } catch (IOException ignored) {
            return false;
        }
    }

    @Override
    default Set<String> namespaces(final PackType root) {
        return this.shadow$getNamespaces((net.minecraft.server.packs.PackType) (Object) Objects.requireNonNull(root, "root"));
    }
}
