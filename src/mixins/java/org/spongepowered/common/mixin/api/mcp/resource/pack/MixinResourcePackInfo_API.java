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

import net.kyori.adventure.text.Component;
import net.minecraft.resources.IResourcePack;
import net.minecraft.resources.ResourcePackInfo;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.api.resource.pack.Pack;
import org.spongepowered.api.resource.pack.PackInfo;
import org.spongepowered.api.resource.pack.PackVersion;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.adventure.SpongeAdventure;

@Mixin(ResourcePackInfo.class)
@Implements(@Interface(iface = PackInfo.class, prefix = "pack$"))
public abstract class MixinResourcePackInfo_API implements PackInfo {

    // @formatter:off
    @Shadow public abstract ITextComponent shadow$getTitle();
    @Shadow public abstract ITextComponent shadow$getDescription();
    @Shadow public abstract IResourcePack shadow$getResourcePack();
    @Shadow public abstract net.minecraft.resources.PackCompatibility shadow$getCompatibility();
    @Shadow public abstract ResourcePackInfo.Priority shadow$getPriority();
    @Shadow public abstract String shadow$getName();
    @Shadow public abstract boolean shadow$isAlwaysEnabled();
    @Shadow public abstract boolean shadow$isOrderLocked();
    // @formatter:on

    @Override
    public Pack getPack() {
        return (Pack) this.shadow$getResourcePack();
    }

    @Override
    public Component getTitle() {
        return SpongeAdventure.asAdventure(shadow$getTitle());
    }

    @Override
    public Component getDescription() {
        return SpongeAdventure.asAdventure(shadow$getDescription());
    }

    @Override
    public PackVersion getVersion() {
        return (PackVersion) (Object) this.shadow$getCompatibility();
    }

    @Override
    public Priority getPriority() {
        return (Priority) (Object) shadow$getPriority();
    }

    @Intrinsic
    public String pack$getName() {
        return shadow$getName();
    }

    @Intrinsic
    public boolean pack$isForced() {
        return shadow$isAlwaysEnabled();
    }

    @Intrinsic
    public boolean pack$isLocked() {
        return shadow$isOrderLocked();
    }
}
