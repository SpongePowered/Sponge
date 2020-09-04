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
package org.spongepowered.common.bridge.server.management;

import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.profile.GameProfileCache;

import java.util.Optional;
import java.util.UUID;

public interface PlayerProfileCacheBridge extends GameProfileCache {

    void bridge$setCanSave(boolean flag);

    void bridge$add(com.mojang.authlib.GameProfile profile, boolean full, boolean signed);

    default void bridge$addBasic(final GameProfile profile) {
        this.bridge$add(profile, false, false);
    }

    void bridge$add(GameProfile profile, boolean full, boolean signed);

    Optional<PlayerProfileCache_ProfileEntryBridge> bridge$getEntry(UUID uniqueId);

    Optional<PlayerProfileCache_ProfileEntryBridge> bridge$getEntry(String name);
}
