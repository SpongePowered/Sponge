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
package org.spongepowered.common.mixin.api.mcp.entity;

import net.kyori.adventure.text.Component;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.common.bridge.ResourceKeyBridge;

@Mixin(net.minecraft.entity.EntityType.class)
@Implements(@Interface(iface = EntityType.class, prefix = "entitytype$"))
public abstract class EntityTypeMixin_API<T extends Entity> implements EntityType<T> {

    @Shadow public abstract ITextComponent shadow$getName();
    @Shadow public abstract boolean shadow$func_225437_d();
    @Shadow public abstract boolean shadow$isSerializable();
    @Shadow public abstract boolean shadow$isImmuneToFire();
    @Shadow public abstract boolean shadow$isSummonable();

    @Override
    public ResourceKey getKey() {
        return ((ResourceKeyBridge) this).bridge$getKey();
    }

    @Override
    public Component asComponent() {
        return SpongeAdventure.asAdventure(this.shadow$getName());
    }

    @Override
    public boolean isTransient() {
        return !this.shadow$isSerializable();
    }

    @Override
    public boolean isFlammable() {
        return !this.shadow$isImmuneToFire();
    }

    @Override
    public boolean canSpawnAwayFromPlayer() {
        return this.shadow$func_225437_d();
    }

    @Intrinsic
    public boolean entitytype$isSummonable() {
        return this.shadow$isSummonable();
    }
}
