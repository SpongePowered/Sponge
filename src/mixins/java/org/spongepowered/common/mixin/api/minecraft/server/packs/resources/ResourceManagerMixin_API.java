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
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mixin(ResourceManager.class)
public interface ResourceManagerMixin_API extends org.spongepowered.api.resource.ResourceManager {

    // @formatter:off
    @Shadow net.minecraft.server.packs.resources.Resource getResource(ResourceLocation var1) throws IOException;
    @Shadow List<net.minecraft.server.packs.resources.Resource> getResources(ResourceLocation var1) throws IOException;
    @Shadow Collection<ResourceLocation> listResources(String var1, Predicate<String> var2);
    // @formatter:on

    @Override
    default Resource load(final ResourcePath path) throws IOException {
        return new SpongeResource(this.getResource((ResourceLocation) (Object) Objects.requireNonNull(path, "path").key()));
    }

    @Override
    default Stream<Resource> streamAll(final ResourcePath path) throws IOException {
        return (Stream<Resource>) (Object) this.getResources((ResourceLocation) (Object) Objects.requireNonNull(path, "path").key()).stream();
    }

    @Override
    default Collection<ResourcePath> find(final String pathPrefix, final Predicate<String> pathFilter) {
        return this.listResources(Objects.requireNonNull(pathPrefix, "pathPrefix"), Objects.requireNonNull(pathFilter, "pathFilter"))
            .stream()
            .map(r -> new SpongeResourcePath((ResourceKey) (Object) r))
            .collect(Collectors.toList());
    }
}
