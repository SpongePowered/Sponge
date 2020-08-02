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

import net.minecraft.client.resources.ReloadListener;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.resources.IResourceManagerReloadListener;
import org.spongepowered.api.data.persistence.DataFormat;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.resource.Resource;
import org.spongepowered.api.resource.ResourceManager;
import org.spongepowered.api.resource.ResourcePath;
import org.spongepowered.api.resource.ResourceReloadListener;
import org.spongepowered.api.resource.ResourceReloadListener.DataTreeReloadListener;
import org.spongepowered.api.resource.ResourceReloadListener.Factory;
import org.spongepowered.api.resource.ResourceReloadListener.PreparedReloadListener;
import org.spongepowered.api.resource.ResourceReloadListener.SimpleReloadListener;
import org.spongepowered.common.SpongeCommon;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class SpongeResourceReloadListenerFactory implements Factory {

    public static final Factory INSTANCE = new SpongeResourceReloadListenerFactory();

    @Override
    public ResourceReloadListener simple(SimpleReloadListener listener) {
        return (ResourceReloadListener) new SpongeSimpleResourceReloadListener(listener);
    }

    @Override
    public <T> ResourceReloadListener prepared(PreparedReloadListener<T> listener) {
        return (ResourceReloadListener) new SpongePreparedResourceReloadListener<>(listener);
    }

    @Override
    public ResourceReloadListener dataTree(String pathPrefix, DataFormat format, DataTreeReloadListener listener) {
        return (ResourceReloadListener) new SpongeJsonResourceReloadListener(pathPrefix, format, listener);
    }

    private static class SpongeSimpleResourceReloadListener implements IResourceManagerReloadListener {

        private final SimpleReloadListener listener;

        public SpongeSimpleResourceReloadListener(SimpleReloadListener listener) {
            this.listener = listener;
        }

        @Override
        public void onResourceManagerReload(IResourceManager resourceManager) {
            listener.onReload((ResourceManager) resourceManager);
        }
    }

    private static class SpongePreparedResourceReloadListener<T> extends ReloadListener<T> {
        private final PreparedReloadListener<T> listener;

        public SpongePreparedResourceReloadListener(PreparedReloadListener<T> listener) {
            this.listener = listener;
        }

        @Override
        protected T prepare(IResourceManager resourceManagerIn, IProfiler profilerIn) {
            return listener.prepare((ResourceManager) resourceManagerIn);
        }

        @Override
        protected void apply(T splashList, IResourceManager resourceManagerIn, IProfiler profilerIn) {
            listener.apply(splashList, (ResourceManager) resourceManagerIn);
        }
    }

    private static class SpongeJsonResourceReloadListener extends ReloadListener<Map<ResourcePath, DataView>> {
        private final String pathPrefix;
        private final DataFormat format;
        private final DataTreeReloadListener listener;

        public SpongeJsonResourceReloadListener(String pathPrefix, DataFormat format, DataTreeReloadListener listener) {
            this.pathPrefix = pathPrefix;
            this.format = format;
            this.listener = listener;
        }

        @Override
        protected Map<ResourcePath, DataView> prepare(IResourceManager resourceManagerIn, IProfiler profilerIn) {
            ResourceManager mgr = (ResourceManager) resourceManagerIn;
            String dataFormat = format.getKey().getValue();
            String suffix = "." + dataFormat.toLowerCase(Locale.ROOT);

            Collection<ResourcePath> paths = mgr.find(pathPrefix, s -> s.endsWith(suffix));
            Map<ResourcePath, DataView> result = new LinkedHashMap<>(paths.size());
            for (ResourcePath path : paths) {
                try (Resource res = mgr.load(path)) {
                    result.put(path, format.readFrom(res.getInputStream()));
                } catch (IOException e) {
                    SpongeCommon.getLogger().warn("Error while loading {} resource '{}'", dataFormat, path, e);
                }
            }
            return result;
        }

        @Override
        protected void apply(Map<ResourcePath, DataView> splashList, IResourceManager resourceManagerIn, IProfiler profilerIn) {
            listener.apply(splashList, (ResourceManager) resourceManagerIn);
        }
    }
}
