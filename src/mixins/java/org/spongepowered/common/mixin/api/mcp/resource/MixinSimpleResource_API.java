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

import net.minecraft.resources.SimpleResource;
import net.minecraft.util.ResourceLocation;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.resource.ResourcePath;
import org.spongepowered.api.resource.meta.MetaParseException;
import org.spongepowered.api.resource.meta.MetaSection;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.common.resource.ISpongeResource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Optional;

@Mixin(SimpleResource.class)
public abstract class MixinSimpleResource_API implements ISpongeResource {
    // @formatter:off
    @Shadow @Final private ResourceLocation location;
    @Shadow @Final @Nullable private InputStream metadataInputStream;
    // @formatter:on

    private final ResourcePath resourcePath = ResourcePath.of(location.getNamespace(), location.getPath());

    private boolean metadataRead;
    private DataView metadataView;

    @Override
    public ResourcePath getPath() {
        return this.resourcePath;
    }

    @Override
    @Invoker("getInputStream")
    public abstract InputStream getInputStream();

    @Override
    @Invoker("getPackName")
    public abstract String getPack();

    @Override
    public final boolean hasMetadata() {
        // this method is client only, add it back.
        // TODO: special casing for client?
        return this.metadataInputStream != null;
    }

    @Override
    public <T> Optional<T> getMetadata(MetaSection<T> section) throws MetaParseException {
        // this method is client only, add it back.
        // TODO: special casing for client?
        if (!hasMetadata()) {
            return Optional.empty();
        }
        if (this.metadataView == null && !this.metadataRead) {
            this.metadataRead = true;

            try (Reader r = new BufferedReader(new InputStreamReader(this.metadataInputStream))) {
                this.metadataView = DataFormats.JSON.get().readFrom(r);
            } catch (IOException e) {
                throw new MetaParseException("Error while parsing json data.", e);
            }
        }

        return Optional.ofNullable(this.metadataView)
                .flatMap(view -> view.getView(section.getQuery()))
                .map(a -> {
                    try {
                        return section.deserialize(a);
                    } catch (RuntimeException e) {
                        throw new MetaParseException(e);
                    }
                });
    }
}
