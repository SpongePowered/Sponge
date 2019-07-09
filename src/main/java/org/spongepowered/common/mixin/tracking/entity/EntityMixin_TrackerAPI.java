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
package org.spongepowered.common.mixin.tracking.entity;

import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.util.Identifiable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.OwnershipTrackedBridge;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.common.entity.PlayerTracker;

import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

@Mixin(value = net.minecraft.entity.Entity.class, priority = 1200)
public abstract class EntityMixin_TrackerAPI implements Entity {

    @Shadow public net.minecraft.world.World world;


    @Override
    public Optional<UUID> getCreator() {
        if (((WorldBridge) this.world).bridge$isFake()) {
            return Optional.empty();
        }
        final Optional<UUID> uuid = ((OwnershipTrackedBridge) this).tracked$getOwnerUUID();
        if (uuid.isPresent()) {
            return uuid;
        }
        return ((OwnershipTrackedBridge) this).tracked$getTrackedUser(PlayerTracker.Type.OWNER).map(Identifiable::getUniqueId);
    }

    @Override
    public Optional<UUID> getNotifier() {
        if (((WorldBridge) this.world).bridge$isFake()) {
            return Optional.empty();
        }
        final Optional<UUID> uuid = ((OwnershipTrackedBridge) this).tracked$getNotifierUUID();
        if (uuid.isPresent()) {
            return uuid;
        }
        return ((OwnershipTrackedBridge) this).tracked$getTrackedUser(PlayerTracker.Type.NOTIFIER).map(Identifiable::getUniqueId);
    }

    @Override
    public void setCreator(@Nullable UUID uuid) {
        if (((WorldBridge) this.world).bridge$isFake()) {
            return;
        }
        ((OwnershipTrackedBridge) this).tracked$setTrackedUUID(PlayerTracker.Type.OWNER, uuid);
    }

    @Override
    public void setNotifier(@Nullable UUID uuid) {
        if (((WorldBridge) this.world).bridge$isFake()) {
            return;
        }
        ((OwnershipTrackedBridge) this).tracked$setTrackedUUID(PlayerTracker.Type.NOTIFIER, uuid);
    }

}
