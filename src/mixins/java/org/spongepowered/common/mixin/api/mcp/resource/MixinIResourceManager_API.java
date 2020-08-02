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
package org.spongepowered.common.mixin.api.mcp.resource;

import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.resource.Resource;
import org.spongepowered.api.resource.ResourceManager;
import org.spongepowered.api.resource.ResourcePath;
import org.spongepowered.api.util.CloseableList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.util.CloseableListImpl;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Mixin(IResourceManager.class)
public interface MixinIResourceManager_API extends ResourceManager {

    // @formatter:off
    @Shadow IResource getResource(ResourceLocation resourceLocationIn) throws IOException;
    @Shadow List<IResource> getAllResources(ResourceLocation resourceLocationIn) throws IOException;
    @Shadow Collection<ResourceLocation> getAllResourceLocations(String pathIn, Predicate<String> filter);
    // @formatter:on

    @Override
    default Resource load(ResourcePath path) throws IOException {
        return (Resource) this.getResource((ResourceLocation) (Object) path);
    }

    @Override
    default CloseableList<@NonNull Resource> loadAll(ResourcePath path) throws IOException {
        return CloseableListImpl.create(this.getAllResources((ResourceLocation) (Object) path)
                .stream()
                .map(Resource.class::cast)
                .collect(Collectors.toList()));
    }

    @Override
    default Collection<ResourcePath> find(String pathPrefix, Predicate<String> pathFilter) {
        return this.getAllResourceLocations(pathPrefix, pathFilter)
                .stream()
                .map(ResourcePath.class::cast)
                .collect(Collectors.toList());
    }
}
