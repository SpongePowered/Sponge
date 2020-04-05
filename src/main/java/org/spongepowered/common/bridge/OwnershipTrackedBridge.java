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
package org.spongepowered.common.bridge;

import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.common.config.category.ModuleCategory;
import org.spongepowered.common.entity.PlayerTracker;
import org.spongepowered.common.event.tracking.PhaseTracker;

import java.util.Optional;
import java.util.UUID;

/**
 * An optionally implemented interface to bridge getting the
 * {@link UUID} and/or {@link User} from the targets. This
 * is implemented by {@code OwnershipTrackedMixin_Tracker}
 * backing hooks and implementation by {@code EntityMixin_Tracker},
 * {@code ChunkMixin_OwnershipTracked}, and {@code WorldMixin_OwnershipTracked}.
 * Note that it is not guaranteed this interface is mixed onto
 * {@link Entity} and {@link TileEntity} because of the option
 * for {@link ModuleCategory#useTracking()}. If the tracking is
 * disabled, the {@link PhaseTracker} and it's hooks are still
 * used, but none of the owner/notifier information is persisted
 * or transferred to the target objects.
 */
public interface OwnershipTrackedBridge {

    Optional<UUID> tracked$getOwnerUUID();

    Optional<UUID> tracked$getNotifierUUID();

    Optional<User> tracked$getTrackedUser(PlayerTracker.Type type);

    Optional<User> tracked$getOwnerReference();

    void tracked$setOwnerReference(@Nullable User user);

    Optional<User> tracked$getNotifierReference();

    void tracked$setNotifier(@Nullable User user);

    void tracked$setTrackedUUID(PlayerTracker.Type type, @Nullable UUID uuid);
}
