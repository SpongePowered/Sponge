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
package org.spongepowered.common.mixin.core.server.bossevents;

import net.minecraft.network.chat.Component;
import net.minecraft.server.bossevents.CustomBossEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.common.mixin.core.server.level.ServerBossEventMixin;

@Mixin(CustomBossEvent.class)
public abstract class CustomBossEventMixin extends ServerBossEventMixin {
    @Shadow private int max;

    @Redirect(method = {"getValue", "save"},
        at = @At(value = "FIELD", target = "Lnet/minecraft/server/bossevents/CustomBossEvent;value:I"))
    private int impl$valueRead(final CustomBossEvent $this) {
        return (int) (this.bridge$asAdventure().progress() * this.max);
    }

    @Redirect(
        method = {"save"},
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/server/bossevents/CustomBossEvent;name:Lnet/minecraft/network/chat/Component;"
        )
    )
    private Component impl$nameRead(final CustomBossEvent $this) {
        return SpongeAdventure.asVanilla(this.bridge$asAdventure().name());
    }

    // Value writes already update the percent field of superclasses, so we don't need to redirect
}
