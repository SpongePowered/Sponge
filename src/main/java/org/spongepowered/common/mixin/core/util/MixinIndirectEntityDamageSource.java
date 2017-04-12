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
import net.minecraft.util.EntityDamageSourceIndirect;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.cause.entity.damage.source.IndirectEntityDamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.entity.EntityUtil;

import java.util.Optional;

@Mixin(value = EntityDamageSourceIndirect.class, priority = 992)
public abstract class MixinIndirectEntityDamageSource extends MixinEntityDamageSource implements IndirectEntityDamageSource {

    @Shadow private Entity indirectEntity;

    private Optional<User> owner;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onConstruct(CallbackInfo callbackInfo) {
        if (!(this.indirectEntity instanceof User)) {
            this.owner = EntityUtil.toMixin(super.getSource()).getCreatorUser();
            if (this.indirectEntity == null && this.owner.isPresent() && this.owner.get() instanceof Entity) {
                this.indirectEntity = (Entity) this.owner.get();
            }
        }
    }

    @Override
    public org.spongepowered.api.entity.Entity getIndirectSource() {
        return (org.spongepowered.api.entity.Entity) this.indirectEntity;
    }

    @Override
    public String toString() {
        MoreObjects.ToStringHelper helper = MoreObjects.toStringHelper("IndirectEntityDamageSource")
            .add("Name", this.damageType)
            .add("Type", this.getType().getId())
            .add("Source", this.getSource())
            .add("IndirectSource", this.getIndirectSource());
        if (this.owner != null && this.owner.isPresent()) {
            helper.add("SourceOwner", this.owner.get());
        }
        return helper.toString();
    }
}
