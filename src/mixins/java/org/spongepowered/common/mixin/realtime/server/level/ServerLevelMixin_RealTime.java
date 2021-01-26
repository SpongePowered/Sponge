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
package org.spongepowered.common.mixin.realtime.server.level;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.storage.ServerLevelData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.common.mixin.realtime.world.level.LevelMixin_RealTime;
import org.spongepowered.common.bridge.RealTimeTrackingBridge;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin_RealTime extends LevelMixin_RealTime implements RealTimeTrackingBridge {

    @Shadow @Final private ServerLevelData serverLevelData;

    @Inject(method = "tick", at = @At("HEAD"))
    private void realTimeImpl$fixTimeOfDayForRealTime(final CallbackInfo ci) {
        if (((WorldBridge) this).bridge$isFake()) {
            return;
        }
        if (this.serverLevelData.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)) {
            // Subtract the one the original tick method is going to add
            final long diff = this.realTimeBridge$getRealTimeTicks() - 1;
            // Don't set if we're not changing it as other mods might be listening for changes
            if (diff > 0) {
                this.serverLevelData.setDayTime(this.serverLevelData.getDayTime() + diff);
            }
        }
    }

}
