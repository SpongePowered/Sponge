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

import net.minecraft.core.BlockPos;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.Server;
import org.spongepowered.common.command.manager.SpongeCommandManager;
import org.spongepowered.common.profile.SpongeGameProfileManager;
import org.spongepowered.common.scheduler.ServerScheduler;
import org.spongepowered.common.user.SpongeUserManager;
import org.spongepowered.common.util.UsernameCache;
import org.spongepowered.common.world.server.SpongeWorldManager;
import org.spongepowered.common.world.storage.SpongePlayerDataManager;

public interface SpongeServer extends SpongeEngine, Server {

    @Override
    ServerScheduler scheduler();

    @Override
    SpongeWorldManager worldManager();

    SpongePlayerDataManager getPlayerDataManager();

    SpongeGameProfileManager gameProfileManagerIfPresent();

    UsernameCache getUsernameCache();

    @Nullable Integer getBlockDestructionId(BlockPos pos);

    int getOrCreateBlockDestructionId(BlockPos pos);

    SpongeUserManager userManager();

    @Override
    SpongeCommandManager commandManager();
}
