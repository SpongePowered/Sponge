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

import net.minecraft.resources.ResourcePackInfo;
import net.minecraft.resources.ResourcePackList;
import org.spongepowered.api.resource.pack.PackInfo;
import org.spongepowered.api.resource.pack.PackList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Optional;

@Mixin(ResourcePackList.class)
public abstract class MixinResourcePackList_API implements PackList {

    // @formatter:off
    @Shadow public abstract Collection<ResourcePackInfo> getAllPacks();
    @Shadow public abstract Collection<ResourcePackInfo> getAvailablePacks();
    @Shadow public abstract Collection<ResourcePackInfo> getEnabledPacks();
    @Shadow @Nullable public abstract ResourcePackInfo getPackInfo(String name);
    // @formatter:on

    @Override
    public Collection<PackInfo> all() {
        return (Collection) this.getAllPacks();
    }

    @Override
    public Collection<PackInfo> disabled() {
        return (Collection) this.getAvailablePacks();
    }

    @Override
    public Collection<PackInfo> enabled() {
        return (Collection) this.getEnabledPacks();
    }

    @Override
    public Optional<PackInfo> get(String name) {
        return Optional.ofNullable((PackInfo) this.getPackInfo(name));
    }
}
