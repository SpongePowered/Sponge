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
package org.spongepowered.common.mixin.api.mcp.resource.pack;

import com.google.gson.JsonParseException;
import net.minecraft.resources.IResourcePack;
import net.minecraft.resources.ResourcePackType;
import net.minecraft.resources.data.IMetadataSectionSerializer;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.api.resource.ResourcePath;
import org.spongepowered.api.resource.meta.MetaParseException;
import org.spongepowered.api.resource.meta.MetaSection;
import org.spongepowered.api.resource.pack.Pack;
import org.spongepowered.api.resource.pack.PackType;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.resource.SpongeResourcePath;
import org.spongepowered.common.resource.meta.SpongeMetadataSectionSerializer;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Mixin(IResourcePack.class)
@Implements(@Interface(iface = Pack.class, prefix = "pack$"))
public interface MixinIResourcePack_API extends Pack {

    // @formatter:off
    @Shadow InputStream getResourceStream(ResourcePackType type, ResourceLocation location) throws IOException;
    @Shadow Collection<ResourceLocation> getAllResourceLocations(ResourcePackType type, String namespace, String pathIn, int maxDepth, Predicate<String> filter);
    @Shadow @Nullable <T> T getMetadata(IMetadataSectionSerializer<T> deserializer) throws IOException;
    @Shadow boolean resourceExists(ResourcePackType type, ResourceLocation location);
    @Shadow Set<String> getResourceNamespaces(ResourcePackType type);
    @Shadow String shadow$getName();
    // @formatter:on

    @Override
    @SuppressWarnings("ConstantConditions")
    default InputStream openStream(PackType type, ResourcePath path) throws IOException {
        return this.getResourceStream((ResourcePackType) (Object) type, SpongeResourcePath.toVanilla(path));
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    default Collection<ResourcePath> find(PackType type, String namespace, String prefix, int depth, Predicate<String> filter) {
        return this.getAllResourceLocations((ResourcePackType) (Object) type, namespace, prefix, depth, filter)
                .stream()
                .map(SpongeResourcePath::fromVanilla)
                .collect(Collectors.toList());
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    default boolean exists(PackType type, ResourcePath path) {
        return this.resourceExists((ResourcePackType) (Object) type, SpongeResourcePath.toVanilla(path));
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    default Set<String> getNamespaces(PackType type) {
        return this.getResourceNamespaces((ResourcePackType) (Object) type);
    }

    @Override
    default <T> Optional<T> getMetadata(MetaSection<T> section) {
        try {
            return Optional.ofNullable(this.getMetadata(new SpongeMetadataSectionSerializer<>(section)));
        } catch (IOException | JsonParseException e) {
            throw new MetaParseException(e);
        }
    }

    @Intrinsic
    default String pack$getName() {
        return shadow$getName();
    }
}
