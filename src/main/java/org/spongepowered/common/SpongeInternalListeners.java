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
package org.spongepowered.common;

import net.minecraft.server.MinecraftServer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.world.SaveWorldEvent;
import org.spongepowered.common.bridge.server.management.PlayerProfileCacheBridge;
import org.spongepowered.common.util.SpongeUsernameCache;

public class SpongeInternalListeners {

    public static SpongeInternalListeners getInstance() {
        return Holder.INSTANCE;
    }

    @Listener
    public void onWorldSave(SaveWorldEvent event) {
        if (Sponge.getServer().getWorldManager().getDefaultProperties().isPresent()) {
            if (event.getWorld().getProperties().getUniqueId().equals(Sponge.getServer().getWorldManager().getDefaultProperties().get()
                    .getUniqueId())) {
                SpongeUsernameCache.save();
                final MinecraftServer server = SpongeCommon.getServer();
                ((PlayerProfileCacheBridge) server.getPlayerProfileCache()).bridge$setCanSave(true);
                server.getPlayerProfileCache().save();
                ((PlayerProfileCacheBridge) server.getPlayerProfileCache()).bridge$setCanSave(false);
            }
        }
    }

    SpongeInternalListeners() {}

    private static final class Holder {
        static final SpongeInternalListeners INSTANCE = new SpongeInternalListeners();
    }

}
