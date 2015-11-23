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
package org.spongepowered.common.util;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.base.Throwables;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.WorldInfo;
import org.apache.commons.io.FileUtils;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.common.interfaces.IMixinMinecraftServer;
import org.spongepowered.common.interfaces.IMixinWorldInfo;
import org.spongepowered.common.registry.type.world.WorldPropertyRegistryModule;
import org.spongepowered.common.service.scheduler.SpongeScheduler;
import org.spongepowered.common.world.DimensionManager;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Callable;

public class ServerUtils {

    private static final ListeningExecutorService executor = SpongeScheduler.getInstance().getListeningExecService();

    public static ListenableFuture<Optional<WorldProperties>> copyWorld(MinecraftServer server, WorldProperties worldProperties, String copyName) {
        checkArgument(WorldPropertyRegistryModule.getInstance().isWorldRegistered(worldProperties.getUniqueId()), "World properties not registered");
        checkArgument(!WorldPropertyRegistryModule.getInstance().isWorldRegistered(copyName), "Destination world name already is registered");
        WorldInfo info = (WorldInfo) worldProperties;
        WorldServer world = DimensionManager.getWorldFromDimId(((IMixinWorldInfo) info).getDimensionId());
        if (world != null) {
            try {
                world.saveAllChunks(true, null);
            } catch (MinecraftException e) {
                Throwables.propagate(e);
            }
            ((IMixinMinecraftServer) server).setSaveEnabled(false);
        }
        ListenableFuture<Optional<WorldProperties>> future = executor.submit(new CopyWorldTask(info, copyName));
        if (world != null) { // World was loaded
            future.addListener(() -> {
                ((IMixinMinecraftServer) server).setSaveEnabled(true);
            } , MoreExecutors.sameThreadExecutor());
        }
        return future;
    }

    public static ListenableFuture<Boolean> deleteWorld(WorldProperties worldProperties) {
        checkArgument(WorldPropertyRegistryModule.getInstance().isWorldRegistered(worldProperties.getUniqueId()), "World properties not registered");
        checkState(DimensionManager.getWorldFromDimId(((IMixinWorldInfo) worldProperties).getDimensionId()) == null, "World not unloaded");
        return executor.submit(new DeleteWorldTask(worldProperties));
    }

    private static class CopyWorldTask implements Callable<Optional<WorldProperties>> {

        private final WorldInfo oldInfo;
        private final String newName;

        public CopyWorldTask(WorldInfo info, String newName) {
            this.oldInfo = info;
            this.newName = newName;
        }

        @Override
        public Optional<WorldProperties> call() throws Exception {
            File rootDir = DimensionManager.getCurrentSaveRootDirectory();
            if (rootDir == null) {
                return Optional.empty();
            }
            File oldDir = new File(rootDir, this.oldInfo.getWorldName());
            File newDir = new File(rootDir, this.newName);

            if (newDir.exists()) {
                return Optional.empty();
            }

            try {
                FileUtils.copyDirectory(oldDir, newDir);
            } catch (IOException e) {
                return Optional.empty();
            }

            WorldInfo info = new WorldInfo(this.oldInfo);
            info.setWorldName(this.newName);
            int dim = DimensionManager.getNextFreeDimId();
            ((IMixinWorldInfo) info).setDimensionId(dim);
            ((IMixinWorldInfo) info).setUUID(UUID.randomUUID());
            WorldPropertyRegistryModule.getInstance().registerWorldProperties((WorldProperties) info);
            ((IMixinMinecraftServer) MinecraftServer.getServer()).getHandler(this.newName).saveWorldInfo(info);
            return Optional.of((WorldProperties) info);
        }

    }

    private static class DeleteWorldTask implements Callable<Boolean> {

        private final WorldProperties props;

        public DeleteWorldTask(WorldProperties props) {
            this.props = props;
        }

        @Override
        public Boolean call() throws Exception {
            File rootDir = DimensionManager.getCurrentSaveRootDirectory();
            if (rootDir == null) {
                return false;
            }
            File dir = new File(rootDir, this.props.getWorldName());
            try {
                FileUtils.deleteDirectory(dir);
                WorldPropertyRegistryModule.getInstance().unregister(this.props);
                return true;
            } catch (IOException e) {
                return false;
            }
        }

    }

}
