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
package org.spongepowered.common.mixin.core.util;

import net.minecraft.item.Item;
import net.minecraft.util.CooldownTracker;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.bridge.util.CooldownTrackerBridge;

import java.util.Map;

@Mixin(CooldownTracker.class)
public abstract class CooldownTrackerMixin implements CooldownTrackerBridge {

    private int impl$lastSetCooldownResult;

    @Inject(
            method = "setCooldown",
            at = @At(
                    value = "HEAD",
                    remap = false
            ),
            locals = LocalCapture.CAPTURE_FAILHARD,
            cancellable = true
    )
    private void impl$throwEventOnSetAndTrackResult(final Item item, final int ticks, final CallbackInfo ci) {
        this.impl$lastSetCooldownResult = this.impl$throwSetCooldownEvent((ItemType) item, ticks);
        if (this.impl$lastSetCooldownResult == -1) {
            ci.cancel();
        }
    }

    @ModifyVariable(
            method = "setCooldown",
            at = @At(
                    value = "HEAD",
                    remap = false
            ),
            argsOnly = true
    )
    private int impl$setResultOfEvent(int ticks) {
        return this.impl$lastSetCooldownResult;
    }

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Ljava/util/Map$Entry;getKey()Ljava/lang/Object;", remap = false))
    private Object onTick(final Map.Entry<Item, ?> entry) {
        impl$throwEndCooldownEvent((ItemType) entry.getKey());
        return entry.getKey();
    }

    protected int impl$throwSetCooldownEvent(final ItemType type, final int ticks) {
        return 0;
    }

    protected void impl$throwEndCooldownEvent(final ItemType type) {

    }

    @Override
    public boolean bridge$getSetCooldownResult() {
        return this.impl$lastSetCooldownResult != -1;
    }
}
