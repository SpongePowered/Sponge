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
package org.spongepowered.common.mixin.core.world.damagesource;

import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.bridge.world.damagesource.DamageSourceBridge;

@Mixin(DamageSource.class)
public abstract class DamageSourceMixin implements DamageSourceBridge {

    @Shadow @Final private Holder<net.minecraft.world.damagesource.DamageType> type;
    private ServerLocation impl$blockLocation;
    private BlockSnapshot impl$blockSnapshot;

    @Inject(method = "getLocalizedDeathMessage", cancellable = true, at = @At(value = "RETURN"))
    private void beforeGetDeathMessageReturn(final LivingEntity livingEntity, final CallbackInfoReturnable<Component> cir) {
        // This prevents untranslated keys from appearing in death messages, switching out those that are untranslated with the generic message.
        if (cir.getReturnValue().getString().equals("death.attack." + this.type.value().msgId())) {
            cir.setReturnValue(Component.translatable("death.attack.generic", livingEntity.getDisplayName()));
        }
    }

    @Override
    public void bridge$setBlock(final ServerLocation location, final BlockSnapshot blockSnapshot) {
        this.impl$blockLocation = location;
        this.impl$blockSnapshot = blockSnapshot;
    }

    @Override
    public ServerLocation bridge$blockLocation() {
        return this.impl$blockLocation;
    }

    @Override
    public BlockSnapshot bridge$blockSnapshot() {
        return this.impl$blockSnapshot;
    }

}
