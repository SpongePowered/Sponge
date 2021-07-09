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
package co.aikar.timings.sponge;

import co.aikar.timings.Timing;
import net.minecraft.world.level.Level;

/**
 * Set of timers per world, to track world specific Timings.
 */
public final class WorldTimingsHandler {

    public final Timing scheduledBlocks;
    public final Timing entityRemoval;
    public final Timing blockEntityTick;
    public final Timing blockEntityPending;
    public final Timing blockEntityRemoval;
    public final Timing tick;
    public final Timing tickEntities;

    public WorldTimingsHandler(final Level level) {
        final String name = level.dimension().location() + " - ";

        this.tick = SpongeTimingsFactory.ofSafe(name + "Tick");
        this.tickEntities = SpongeTimingsFactory.ofSafe(name + "Tick Entities", this.tick);

        this.scheduledBlocks = SpongeTimingsFactory.ofSafe(name + "Scheduled Blocks", this.tick);
        this.entityRemoval = SpongeTimingsFactory.ofSafe(name + "entityRemoval");
        this.blockEntityTick = SpongeTimingsFactory.ofSafe(name + "blockEntityTick");
        this.blockEntityPending = SpongeTimingsFactory.ofSafe(name + "blockEntityPending");
        this.blockEntityRemoval = SpongeTimingsFactory.ofSafe(name + "blockEntityRemoval");
    }
}
