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
package org.spongepowered.common.resource;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.spongepowered.api.resource.Resource;
import org.spongepowered.api.resource.ResourceLoader;
import org.spongepowered.api.resource.ResourceLocation;
import org.spongepowered.api.resource.ResourceManager;
import org.spongepowered.api.util.Priority;
import org.spongepowered.common.SpongeImpl;

import java.nio.file.Path;
import java.nio.file.ProviderNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class SpongeResourceManager implements ResourceManager {

    private List<SortableLoader> loaders = Lists.newArrayList();
    private List<SortableLoader> newLoaders = Lists.newArrayList();

    private Map<ResourceLocation, List<Resource>> allResources = Maps.newHashMap();

    private boolean reloadLock;
    private boolean reloadDirty;

    @Override
    public Optional<Resource> getResource(ResourceLocation location) {
        return getAllResources(location).stream().findFirst();
    }

    @Override
    public List<Resource> getAllResources(ResourceLocation location) {

        List<Resource> resources = allResources.get(location);
        if (resources == null) {
            resources = Lists.newArrayList();

            for (ResourceLoader loader : getResourceLoaders()) {
                loader.getResource(location).ifPresent(resources::add);
            }
            allResources.put(location, resources);
        }
        return resources;
    }

    @Override
    public Optional<Resource> getAnyResource(ResourceLocation... locations) {
        Optional<Resource> res = Optional.empty();
        for (ResourceLocation loc : locations) {
            res = getResource(loc);
            if (res.isPresent()) {
                break;
            }
        }
        return res;
    }

    /**
     * The reload lock prevents needless reloads. For example during startup, plugins may add
     * or be added as a resource provider. If the reload wasn't prevented
     * @param lock
     */
    public void setReloadLock(boolean lock) {
        boolean reload = !lock && reloadLock && reloadDirty;
        this.reloadLock = lock;
        this.reloadDirty = false;
        if (reload) {
            reloadResources();
        }

    }

    @Override
    public void reloadResources() {
        if (this.reloadLock) {
            reloadDirty = true;
        } else {
            allResources.clear();
            this.loaders = Lists.newArrayList(this.newLoaders);
        }
    }

    @Override
    public List<ResourceLoader> getResourceLoaders() {
        return this.loaders.stream().sorted()
                .map(SortableLoader::getLoader)
                .collect(Collectors.toList());
    }

    @Override
    public void registerResourceLoader(ResourceLoader loader, Priority priority) {
        this.newLoaders.add(new SortableLoader(loader, priority));
        reloadResources();
    }

    @Override
    public boolean unregisterResourceLoader(ResourceLoader loader) {
        boolean removed = newLoaders.removeIf(sortableLoader -> sortableLoader.getLoader().equals(loader));
        if (removed) {
            reloadResources();
        }
        return removed;
    }

    @Override
    public Optional<ResourceLoader> registerFileResourceLoader(Path path, Priority priority) {
        try {
            ResourceLoader res = new FileResourceLoader(path);
            registerResourceLoader(res, priority);
            return Optional.of(res);
        } catch (ProviderNotFoundException e) {
            SpongeImpl.getLogger().warn("Unable to create resource loader from {}", path, e);
        }
        return Optional.empty();
    }

    @Override
    public ResourceLocation newResourceLocation(String domain, String path) {

        return (ResourceLocation) new net.minecraft.util.ResourceLocation(domain, path);
    }
}
