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
package org.spongepowered.common.mixin.api.common.util;

import com.google.common.base.MoreObjects;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.event.cause.entity.damage.source.BlockDamageSource;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.mixin.api.minecraft.util.DamageSourceMixin_API;
import org.spongepowered.common.util.MinecraftBlockDamageSource;

@Mixin(value = MinecraftBlockDamageSource.class, priority = 991)
public abstract class MinecraftBlockDamageSourceMixin_API extends DamageSourceMixin_API implements BlockDamageSource {

    @Shadow(remap = false) @Final
    private BlockSnapshot blockSnapshot;
    @Shadow(remap = false) @Final private ServerLocation location;

    @Override
    public ServerLocation location() {
        return this.location;
    }

    @Override
    public BlockSnapshot blockSnapshot() {
        return this.blockSnapshot;
    }

    @Override
    public String toString() {
        final ResourceKey resourceKey = Sponge.game().registries().registry(RegistryTypes.DAMAGE_TYPE).valueKey(this.type());
        return MoreObjects.toStringHelper("BlockDamageSource")
            .add("Name", this.shadow$getMsgId())
            .add("Key", resourceKey)
            .add("BlockSnapshot", this.blockSnapshot())
            .add("Location", this.location)
            .toString();
    }
}

