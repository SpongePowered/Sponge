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

import com.google.common.base.MoreObjects;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.bridge.CreatorTrackedBridge;
import javax.annotation.Nullable;
import net.minecraft.world.damagesource.IndirectEntityDamageSource;
import net.minecraft.world.entity.Entity;

import java.util.UUID;

@Mixin(value = IndirectEntityDamageSource.class, priority = 992)
public abstract class IndirectEntityDamageSourceMixin extends EntityDamageSourceMixin {

    // @formatter:on
    @Shadow @Final @Mutable @Nullable private Entity owner;

    @Shadow @Nullable public abstract Entity shadow$getDirectEntity();
    // @formatter:off

    @Nullable private UUID impl$creatorUUID;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onConstruct(final CallbackInfo callbackInfo) {
        if (this.entity != null) { // sources can be null
            final Entity mcEntity = this.shadow$getEntity();
            this.impl$creatorUUID = mcEntity instanceof CreatorTrackedBridge
                         ? ((CreatorTrackedBridge) mcEntity).tracked$getCreatorUUID().orElse(null)
                         : null;
            if (this.owner == null && this.impl$creatorUUID != null) {
                final ServerPlayer player = SpongeCommon.server().getPlayerList().getPlayer(this.impl$creatorUUID);
                if (player != null) {
                    this.owner = player;
                }
            }
        }
    }

    @Override
    public String toString() {
        final ResourceKey resourceKey = Sponge.game().registries().registry(RegistryTypes.DAMAGE_TYPE).valueKey(this.impl$damageType.get());
        final MoreObjects.ToStringHelper helper = MoreObjects.toStringHelper("IndirectEntityDamageSource")
            .add("Name", this.msgId)
            .add("Type", resourceKey)
            .add("Source", this.shadow$getDirectEntity())
            .add("IndirectSource", this.shadow$getEntity());
        if (this.impl$creatorUUID != null) {
            helper.add("SourceCreator", this.impl$creatorUUID);
        }
        return helper.toString();
    }
}
