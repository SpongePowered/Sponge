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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class GameProfileQueryTask implements Runnable {

    private List<CompletableFuture<GameProfile>> futureList = new ArrayList<>();
    private Set<UUID> queuedUuidList = new HashSet<>();
    private static final int REQUEST_LIMIT = SpongeImpl.getGlobalConfig().getConfig().getWorld().getGameProfileLookupBatchSize();

    public synchronized void queueUuid(UUID uuid) {
        this.queuedUuidList.add(UUID.fromString(uuid.toString()));
    }

    public synchronized void removeFromQueue(UUID uuid) {
        this.queuedUuidList.remove(uuid);
    }

    @Override
    public void run() {
        Iterator<CompletableFuture<GameProfile>> futureIterator = this.futureList.iterator();
        while (futureIterator.hasNext()) {
            CompletableFuture<GameProfile> future = futureIterator.next();
            if (future.isDone()) {
                try {
                    GameProfile profile = future.get();
                    this.removeFromQueue(profile.getUniqueId());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                futureIterator.remove();
            }
        }

        if (this.queuedUuidList.isEmpty()) {
            return;
        }

        Iterator<UUID> iterator = this.queuedUuidList.iterator();
        int count = 0;
        while (iterator.hasNext() && count < REQUEST_LIMIT) {
            UUID uuid = iterator.next();
            CompletableFuture<GameProfile> future = Sponge.getServer().getGameProfileManager().get(checkNotNull(uuid, "uniqueId"));
            this.futureList.add(future);
            count++;
        }
    }

}
