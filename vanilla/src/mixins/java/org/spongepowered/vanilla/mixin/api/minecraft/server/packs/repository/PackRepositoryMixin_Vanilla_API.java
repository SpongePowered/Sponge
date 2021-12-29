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
package org.spongepowered.vanilla.mixin.api.minecraft.server.packs.repository;

import net.minecraft.server.packs.repository.PackRepository;
import org.spongepowered.api.resource.pack.Pack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.vanilla.bridge.server.packs.repository.PackRepositoryBridge_Vanilla;

import java.util.Objects;

@Mixin(PackRepository.class)
public abstract class PackRepositoryMixin_Vanilla_API implements org.spongepowered.api.resource.pack.PackRepository {

    @Override
    public Pack pack(final PluginContainer container) {
        return (Pack) ((PackRepositoryBridge_Vanilla) this).bridge$pack(Objects.requireNonNull(container, "container"));
    }
}
