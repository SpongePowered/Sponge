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
package org.spongepowered.vanilla.mixin.core.server.packs.repository;

import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.launch.Launch;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.PluginResource;
import org.spongepowered.vanilla.bridge.server.packs.repository.PackRepositoryBridge_Vanilla;
import org.spongepowered.vanilla.launch.plugin.VanillaPluginManager;
import org.spongepowered.vanilla.server.packs.PluginRepositorySource;

import java.util.Arrays;
import java.util.Map;

@Mixin(PackRepository.class)
public abstract class PackRepositoryMixin_Vanilla implements PackRepositoryBridge_Vanilla {

    private final Map<PluginContainer, Pack> vanilla$pluginResourcePacks = new Object2ObjectOpenHashMap<>();

    @Override
    public Pack bridge$pack(final PluginContainer container) {
        return this.vanilla$pluginResourcePacks.get(container);
    }

    @Override
    public void bridge$registerResourcePack(final PluginContainer container, final Pack pack) {
        this.vanilla$pluginResourcePacks.put(container, pack);
    }

    @SuppressWarnings("rawtypes")
    @Redirect(method = "<init>*", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableSet;copyOf([Ljava/lang/Object;)Lcom/google/common/collect/ImmutableSet;"))
    private ImmutableSet vanilla$addPluginRepository(final Object[] elements) {
        final Object[] copied = Arrays.copyOf(elements, elements.length + 1);
        copied[elements.length] = new PluginRepositorySource((PackRepository) (Object) this);
        return ImmutableSet.copyOf(copied);
    }

}
