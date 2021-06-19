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
package org.spongepowered.vanilla.mixin.core.server.level;

import net.minecraft.Util;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.mixin.core.world.level.LevelMixin;

import java.util.function.BooleanSupplier;

import javax.annotation.Nonnull;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin_Vanilla extends LevelMixin {

    // @formatter:off
    @Shadow @Nonnull public abstract MinecraftServer shadow$getServer();
    // @formatter:on

    private final long[] vanilla$recentTickTimes = new long[100];
    private long vanilla$preTickTime = 0L;

    @Override
    public long[] bridge$recentTickTimes() {
        return this.vanilla$recentTickTimes;
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void vanilla$capturePreTickTime(final BooleanSupplier param0, final CallbackInfo ci) {
        this.vanilla$preTickTime = Util.getNanos();
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void vanilla$capturePostTickTime(final BooleanSupplier param0, final CallbackInfo ci) {
        final long postTickTime = Util.getNanos();

        this.vanilla$recentTickTimes[this.shadow$getServer().getTickCount() % 100] = postTickTime - this.vanilla$preTickTime;
    }
}
