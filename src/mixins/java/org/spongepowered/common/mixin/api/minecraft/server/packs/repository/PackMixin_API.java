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
package org.spongepowered.common.mixin.api.minecraft.server.packs.repository;

import net.kyori.adventure.text.Component;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackCompatibility;
import org.spongepowered.api.resource.pack.PackContents;
import org.spongepowered.api.resource.pack.PackStatus;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.adventure.SpongeAdventure;

import java.util.function.Supplier;

@Mixin(Pack.class)
public abstract class PackMixin_API implements org.spongepowered.api.resource.pack.Pack {

    // @formatter:off
    @Shadow @Final private Supplier<PackResources> supplier;
    @Shadow @Final private net.minecraft.network.chat.Component title;
    @Shadow @Final private net.minecraft.network.chat.Component description;

    @Shadow public abstract String shadow$getId();
    @Shadow public abstract PackCompatibility shadow$getCompatibility();
    @Shadow public abstract boolean shadow$isRequired();
    @Shadow public abstract boolean shadow$isFixedPosition();
    // @formatter:on

    @Override
    public String id() {
        return this.shadow$getId();
    }

    @Override
    public PackContents contents() {
        return (PackContents) this.supplier.get();
    }

    @Override
    public Component title() {
        return SpongeAdventure.asAdventure(this.title);
    }

    @Override
    public Component description() {
        return SpongeAdventure.asAdventure(this.description);
    }

    @Override
    public PackStatus status() {
        return (PackStatus) (Object) this.shadow$getCompatibility();
    }

    @Override
    public boolean isForced() {
        return this.shadow$isRequired();
    }

    @Override
    public boolean isLocked() {
        return this.shadow$isFixedPosition();
    }
}
