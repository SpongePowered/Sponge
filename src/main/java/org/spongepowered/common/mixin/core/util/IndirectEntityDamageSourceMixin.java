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

import com.google.common.base.MoreObjects;
import net.minecraft.entity.Entity;
import net.minecraft.util.IndirectEntityDamageSource;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.bridge.OwnershipTrackedBridge;

import javax.annotation.Nullable;

@Mixin(value = IndirectEntityDamageSource.class, priority = 992)
public abstract class IndirectEntityDamageSourceMixin extends EntityDamageSourceMixin {

    @Shadow @Final @Mutable @Nullable private Entity indirectEntity;

    @Shadow @Nullable public abstract Entity getImmediateSource();

    @Nullable private User impl$owner;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onConstruct(final CallbackInfo callbackInfo) {
        if (!(this.indirectEntity instanceof User) && this.damageSourceEntity != null) { // sources can be null
            this.impl$owner = this.getTrueSource() instanceof OwnershipTrackedBridge
                         ? ((OwnershipTrackedBridge) this.getTrueSource()).tracked$getOwnerReference().orElse(null)
                         : null;
            if (this.indirectEntity == null && this.impl$owner instanceof Entity) {
                this.indirectEntity = (Entity) this.impl$owner;
            }
        }
    }

    @Override
    public String toString() {
        final MoreObjects.ToStringHelper helper = MoreObjects.toStringHelper("IndirectEntityDamageSource")
            .add("Name", this.damageType)
            .add("Type", this.impl$damageType.getId())
            .add("Source", this.getImmediateSource())
            .add("IndirectSource", this.getTrueSource());
        if (this.impl$owner != null) {
            helper.add("SourceOwner", this.impl$owner);
        }
        return helper.toString();
    }
}
