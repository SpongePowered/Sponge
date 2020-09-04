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
package org.spongepowered.common.data.provider.util;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.common.SpongeCommon;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public final class GameProfileUtils {

    public static GameProfile resolveProfileIfNecessary(final GameProfile profile) {
        if (profile.getProperties().stream().anyMatch(property -> property.getName().equals("textures"))) {
            return profile;
        }
        // Skulls need a name in order to properly display -> resolve if no name is contained in the given profile
        final CompletableFuture<GameProfile> future = Sponge.getGame().getServer().getGameProfileManager().getProfile(profile);
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            SpongeCommon.getLogger().debug("Exception while trying to fill skull GameProfile for '" + profile + "'", e);
            return profile;
        }
    }
}
