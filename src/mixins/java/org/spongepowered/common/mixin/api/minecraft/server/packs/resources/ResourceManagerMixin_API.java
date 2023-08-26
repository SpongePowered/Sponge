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
package org.spongepowered.common.mixin.api.minecraft.server.packs.resources;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.resource.Resource;
import org.spongepowered.api.resource.ResourcePath;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.resource.SpongeResource;
import org.spongepowered.common.resource.SpongeResourcePath;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mixin(ResourceManager.class)
public interface ResourceManagerMixin_API extends org.spongepowered.api.resource.ResourceManager {

    // @formatter:off
    @Shadow List<net.minecraft.server.packs.resources.Resource> shadow$getResourceStack(ResourceLocation var1);
    @Shadow Map<ResourceLocation, net.minecraft.server.packs.resources.Resource> shadow$listResources(String var1, Predicate<ResourceLocation> var2);
    // @formatter:on

    @Override
    default Resource load(final ResourcePath path) throws IOException {
        final ResourceLocation loc = (ResourceLocation) (Object) Objects.requireNonNull(path, "path").key();
        final net.minecraft.server.packs.resources.Resource resource = ((ResourceProvider) this).getResourceOrThrow(loc);
        // TODO pass optional up to API?
        return new SpongeResource(resource, path);
    }

    @Override
    default Stream<Resource> streamAll(final ResourcePath path) {
        final ResourceLocation loc = (ResourceLocation) (Object) Objects.requireNonNull(path, "path").key();
        return (Stream<Resource>) (Object) this.shadow$getResourceStack(loc).stream();
    }

    @Override
    default Collection<ResourcePath> find(final String pathPrefix, final Predicate<String> pathFilter) {
        Objects.requireNonNull(pathPrefix, "pathPrefix");
        Objects.requireNonNull(pathFilter, "pathFilter");

        final Set<ResourceLocation> mapped = this.shadow$listResources(pathPrefix, loc -> pathFilter.test(loc.getPath())).keySet(); // TODO check filter
        return mapped.stream()
            .map(r -> new SpongeResourcePath((ResourceKey) (Object) r))
            .collect(Collectors.toList());
    }
}
