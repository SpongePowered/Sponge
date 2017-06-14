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
package org.spongepowered.common.world.pregen;

import org.spongepowered.api.event.EventListener;
import org.spongepowered.api.event.world.ChunkPreGenerationEvent;
import org.spongepowered.api.world.ChunkPreGenerate;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class SpongeChunkPreGenerateListener implements EventListener<ChunkPreGenerationEvent> {

    private final List<Consumer<ChunkPreGenerationEvent>> listeners;
    private final UUID task;

    SpongeChunkPreGenerateListener(UUID task, List<Consumer<ChunkPreGenerationEvent>> listeners) {
        this.task = task;
        this.listeners = listeners;
    }

    @Override
    public void handle(ChunkPreGenerationEvent event) throws Exception {
        ChunkPreGenerate preGenerate = event.getChunkPreGenerate();
        if (preGenerate instanceof SpongeChunkPreGenerateTask &&
                ((SpongeChunkPreGenerateTask) preGenerate).getSpongeTask().getUniqueId().equals(this.task)) {
            this.listeners.forEach(x -> x.accept(event));
        }
    }
}
