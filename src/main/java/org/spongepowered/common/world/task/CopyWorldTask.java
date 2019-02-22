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
package org.spongepowered.common.world.task;

import net.minecraft.world.WorldServer;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.storage.SessionLockException;
import net.minecraft.world.storage.WorldInfo;
import org.spongepowered.api.util.file.CopyFileVisitor;
import org.spongepowered.api.util.file.ForwardingFileVisitor;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.interfaces.IMixinMinecraftServer;
import org.spongepowered.common.interfaces.world.IMixinWorldInfo;
import org.spongepowered.common.world.WorldLoader;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Optional;
import java.util.concurrent.Callable;

import javax.annotation.Nullable;

public final class CopyWorldTask implements Callable<Optional<WorldInfo>> {

    private final WorldLoader manager;
    private final Path saveFolder;
    private final WorldInfo info;
    @Nullable private final WorldServer world;
    private final String copyName;

    public CopyWorldTask(WorldLoader manager, Path saveFolder, WorldInfo info, @Nullable WorldServer world,  String copyName) {
        this.manager = manager;
        this.saveFolder = saveFolder;
        this.info = info;
        this.world = world;
        this.copyName = copyName;
    }

    @Override
    public Optional<WorldInfo> call() throws Exception {
        Path oldWorldFolder = this.saveFolder.resolve(this.info.getWorldName());
        final Path newWorldFolder = this.saveFolder.resolve(this.copyName);

        if (Files.exists(newWorldFolder)) {
            return Optional.empty();
        }

        if (this.world != null) {
            try {
                this.world.saveAllChunks(true, null);
            } catch (SessionLockException e) {
                throw new RuntimeException(e);
            }

            ((IMixinMinecraftServer) SpongeImpl.getServer()).setSaveEnabled(false);
        }

        FileVisitor<Path> visitor = new CopyFileVisitor(newWorldFolder);
        if (((IMixinWorldInfo) this.info).getDimensionType() == DimensionType.OVERWORLD) {
            oldWorldFolder = this.saveFolder;
            visitor = new ForwardingFileVisitor<Path>(visitor) {

                private boolean root = true;

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    if (!this.root && Files.exists(dir.resolve("level.dat"))) {
                        return FileVisitResult.SKIP_SUBTREE;
                    }

                    this.root = false;
                    return super.preVisitDirectory(dir, attrs);
                }
            };
        }

        // Copy the world folder
        Files.walkFileTree(oldWorldFolder, visitor);

        // TODO (1.13) - Zidane - Copy WorldInfo, build new DimensionType, write data, register with the manager

        return Optional.empty();
    }
}
