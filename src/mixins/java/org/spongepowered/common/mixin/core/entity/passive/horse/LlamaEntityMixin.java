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
package org.spongepowered.common.mixin.core.entity.passive.horse;

import net.minecraft.entity.passive.horse.LlamaEntity;
import net.minecraft.util.registry.SimpleRegistry;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.type.LlamaType;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.entity.passive.horse.LlamaEntityBridge;

@Mixin(LlamaEntity.class)
public abstract class LlamaEntityMixin implements LlamaEntityBridge {

    // @formatter:off
    @Shadow public abstract int shadow$getVariant();
    @Shadow public abstract void shadow$setVariant(final int p_190710_1_);
    // @formatter:on

    @Override
    public LlamaType bridge$getLlamaType() {
        final SimpleRegistry<LlamaType> registry = (SimpleRegistry<LlamaType>) (Object) Sponge.getGame().registries().registry(RegistryTypes.LLAMA_TYPE);
        return registry.byId(this.shadow$getVariant());
    }

    @Override
    public void bridge$setLlamaType(final LlamaType type) {
        final SimpleRegistry<LlamaType> registry = (SimpleRegistry<LlamaType>) (Object) Sponge.getGame().registries().registry(RegistryTypes.LLAMA_TYPE);
        this.shadow$setVariant(registry.getId(type));
    }
}