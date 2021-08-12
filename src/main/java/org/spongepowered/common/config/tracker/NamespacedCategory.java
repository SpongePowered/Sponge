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
package org.spongepowered.common.config.tracker;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.HashMap;
import java.util.Map;

@ConfigSerializable
public final class NamespacedCategory {

    @Setting
    @Comment("If 'false', all tracking for this mod will be disabled.")
    public boolean enabled = true;

    @Setting
    public Map<String, ValueCategory> types = new HashMap<>();

    public boolean enabled() {
        return this.enabled;
    }

    public ValueCategory valueOrCreate(final String value) {
        return this.types.computeIfAbsent(value, k -> new ValueCategory());
    }

    @ConfigSerializable
    public static final class ValueCategory {

        @Setting(TrackerConfig.BLOCK_BULK_CAPTURE)
        public boolean blockBulkCapture = true;

        @Setting(TrackerConfig.ENTITY_BULK_CAPTURE)
        public boolean entityBulkCapture = true;

        @Setting(TrackerConfig.BLOCK_EVENT_CREATION)
        public boolean blockEventCreation = true;

        @Setting(TrackerConfig.ENTITY_EVENT_CREATION)
        public boolean entityEventCreation = true;

        public boolean allowsBlockEvents() {
            return this.blockEventCreation;
        }

        public void setAllowBlockEvents(final boolean allowBlockEvents) {
            this.blockEventCreation = allowBlockEvents;
        }

        public boolean allowsEntityEvents() {
            return this.entityEventCreation;
        }

        public void setAllowEntityEvents(final boolean allowEntityEvents) {
            this.entityEventCreation = allowEntityEvents;
        }

        public boolean capturesBlocksInBulk() {
            return this.blockBulkCapture;
        }

        public void setCaptureBlocksInBulk(final boolean captureBlocksInBulk) {
            this.blockBulkCapture = captureBlocksInBulk;
        }

        public boolean capturesEntitiesInBulk() {
            return this.entityBulkCapture;
        }

        public void setCaptureEntitiesInBulk(final boolean captureEntitiesInBulk) {
            this.entityBulkCapture = captureEntitiesInBulk;
        }
    }
}
