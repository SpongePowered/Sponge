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
package org.spongepowered.common.data.provider.entity;

import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.persistence.DataContentUpdater;
import org.spongepowered.common.bridge.entity.GrieferBridge;
import org.spongepowered.common.data.ByteToBooleanContentUpdater;
import org.spongepowered.common.data.SpongeDataManager;
import org.spongepowered.common.data.provider.DataProviderRegistrator;
import org.spongepowered.common.util.Constants;

public final class GrieferData {

    private static final DataContentUpdater CAN_GRIEF_UPDATER_BYTE_TO_BOOL_FIX = new ByteToBooleanContentUpdater(1, 2, Keys.CAN_GRIEF);

    private GrieferData() {
    }

    // @formatter:off
    public static void register(final DataProviderRegistrator registrator) {
        registrator
                .asMutable(GrieferBridge.class)
                    .create(Keys.CAN_GRIEF)
                        .get(GrieferBridge::bridge$canGrief)
                        .set(GrieferBridge::bridge$setCanGrief)
                        .supports(GrieferBridge::bridge$isGriefer);
        registrator.spongeDataStore(Keys.CAN_GRIEF.getKey(), 2, new DataContentUpdater[]{GrieferData.CAN_GRIEF_UPDATER_BYTE_TO_BOOL_FIX}, GrieferBridge.class, Keys.CAN_GRIEF);
        SpongeDataManager.INSTANCE.registerLegacySpongeData(Constants.Sponge.Entity.CAN_GRIEF, Keys.CAN_GRIEF.getKey(), Keys.CAN_GRIEF);
    }
    // @formatter:on
}
