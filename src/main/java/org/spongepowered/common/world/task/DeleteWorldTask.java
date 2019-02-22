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

import org.spongepowered.api.util.file.DeleteFileVisitor;
import org.spongepowered.common.world.WorldLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;

public final class DeleteWorldTask implements Callable<Boolean> {

    private final WorldLoader manager;
    private final Path saveFolder;
    private final String folderName;

    public DeleteWorldTask(WorldLoader manager, Path saveFolder, String folderName) {
        this.manager = manager;
        this.saveFolder = saveFolder;
        this.folderName = folderName;
    }

    @Override
    public Boolean call() {
        final Path worldFolder = this.saveFolder.resolve(this.folderName);
        if (!Files.exists(worldFolder)) {
            this.manager.unregisterWorldInfo(this.folderName);
            return true;
        }

        try {
            Files.walkFileTree(worldFolder, DeleteFileVisitor.INSTANCE);
            this.manager.unregisterWorldInfo(this.folderName);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

}