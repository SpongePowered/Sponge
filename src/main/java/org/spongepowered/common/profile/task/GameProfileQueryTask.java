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
package org.spongepowered.common.profile.task;

import static com.google.common.base.Preconditions.checkNotNull;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.util.SpongeUsernameCache;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class GameProfileQueryTask implements Runnable {

    private Set<UUID> queuedUuidList = new HashSet<>();
    private static final int LOOKUP_INTERVAL = SpongeImpl.getGlobalConfig().getConfig().getWorld().getGameProfileQueryTaskInterval();

    public void queueUuid(UUID uuid) {
        this.queuedUuidList.add(uuid);
    }

    @Override
    public void run() {
        while (true) {
            if (this.queuedUuidList.isEmpty()) {
                continue;
            }

            Iterator<UUID> iterator = new HashSet<>(this.queuedUuidList).iterator();
            while (iterator.hasNext()) {
                UUID uuid = iterator.next();
                if (SpongeUsernameCache.getLastKnownUsername(uuid) != null) {
                    iterator.remove();
                    continue;
                }
                try {
                    GameProfile profile = Sponge.getServer().getGameProfileManager().get(checkNotNull(uuid, "uniqueId")).get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                iterator.remove();
                try {
                    Thread.sleep(LOOKUP_INTERVAL * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            this.queuedUuidList.clear();
        }
    }

}
