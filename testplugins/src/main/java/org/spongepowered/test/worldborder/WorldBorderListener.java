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
package org.spongepowered.test.worldborder;

import org.apache.logging.log4j.Logger;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.world.ChangeWorldBorderEvent;
import org.spongepowered.api.world.border.WorldBorder;

public final class WorldBorderListener {

    private final Logger logger;

    public WorldBorderListener(final Logger logger) {
        this.logger = logger;
    }

    @Listener
    public void onPlayerChange(final ChangeWorldBorderEvent.World event) {
        this.logger.info("World border changed for world {}",  event.world().key());
        this.logBorder(event);
    }

    @Listener
    public void onPlayerChange(final ChangeWorldBorderEvent.Player event) {
        this.logger.info("World border changed for player {}",  event.player().name());
        this.logBorder(event);
    }

    private void logBorder(final ChangeWorldBorderEvent event) {
        if (event.previousBorder().isPresent()) {
            final WorldBorder worldBorder = event.previousBorder().get();
            this.logBorder("Previous", worldBorder);
        } else {
            this.logger.info("No previous border");
        }

        if (event.newBorder().isPresent()) {
            final WorldBorder worldBorder = event.newBorder().get();
            this.logBorder("New", worldBorder);
        } else {
            this.logger.info("No new border");
        }
    }

    private void logBorder(final String prefix, final WorldBorder worldBorder) {
        this.logger.info("{} - Center: {}, {} - Diameter: {}", prefix, worldBorder.center().x(), worldBorder.center().y(), worldBorder.diameter());
    }

}
