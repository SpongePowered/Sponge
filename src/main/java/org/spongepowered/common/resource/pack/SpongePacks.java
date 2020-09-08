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
package org.spongepowered.common.resource.pack;

import net.minecraft.resources.IResourcePack;
import net.minecraft.resources.ResourcePackType;
import net.minecraft.resources.data.IMetadataSectionSerializer;
import net.minecraft.util.ResourceLocation;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.resource.ResourcePath;
import org.spongepowered.api.resource.meta.MetaSection;
import org.spongepowered.api.resource.pack.Pack;
import org.spongepowered.api.resource.pack.PackType;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class SpongePacks {

    private SpongePacks() {}

    public static IResourcePack spongePackToVanilla(Pack pack) {
        return new SpongePackToVanilla(pack);
    }

    public static Pack vanillaPackToSponge(IResourcePack pack) {
        if (pack instanceof SpongePackToVanilla) {
            return ((SpongePackToVanilla) pack).pack;
        }

        return (Pack) pack;
    }

    private static final class SpongePackToVanilla implements IResourcePack {
        private final Pack pack;

        private SpongePackToVanilla(Pack pack) {
            this.pack = pack;
        }

        @Override
        public InputStream getRootResourceStream(String fileName) throws IOException {
            throw new IOException();
        }

        @Override
        public InputStream getResourceStream(ResourcePackType type, ResourceLocation location) throws IOException {
            return pack.openStream((PackType) (Object) type, (ResourcePath) (Object) location);
        }

        @Override
        public Collection<ResourceLocation> getAllResourceLocations(ResourcePackType type, String namespaceIn, String pathIn, int maxDepth, Predicate<String> filter) {
            return pack.find((PackType) (Object) type, namespaceIn, pathIn, maxDepth, filter)
                    .stream()
                    .map(ResourceLocation.class::cast)
                    .collect(Collectors.toList());
        }

        @Override
        public boolean resourceExists(ResourcePackType type, ResourceLocation location) {
            return pack.exists((PackType) (Object) type, (ResourcePath) (Object) location);
        }

        @Override
        public Set<String> getResourceNamespaces(ResourcePackType type) {
            return pack.getNamespaces((PackType) (Object) type);
        }

        @Nullable
        @Override
        public <T> T getMetadata(IMetadataSectionSerializer<T> deserializer) throws IOException {
            return pack.getMetadata((MetaSection<T>) deserializer).orElse(null);
        }

        @Override
        public String getName() {
            return pack.getName();
        }

        @Override
        public void close() throws IOException {
            pack.close();
        }
    }

}
