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
package org.spongepowered.common.bridge.server.level;

import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.server.bossevents.CustomBossEvents;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.world.explosion.Explosion;

public interface ServerLevelBridge {

    LevelStorageSource.LevelStorageAccess bridge$getLevelSave();

    ChunkProgressListener bridge$getChunkStatusListener();

    boolean bridge$isLoaded();

    CustomBossEvents bridge$getBossBarManager();

    void bridge$triggerExplosion(Explosion explosion);

    void bridge$handleExplosionPacket(final ServerGamePacketListenerImpl instance, Explosion apiExplosion,
        final ClientboundExplodePacket packet);

    void bridge$setManualSave(boolean state);

    BlockSnapshot bridge$createSnapshot(int x, int y, int z);

    long[] bridge$recentTickTimes();
}
