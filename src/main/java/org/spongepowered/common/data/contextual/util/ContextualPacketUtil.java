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
package org.spongepowered.common.data.contextual.util;

import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.data.DataPerspective;
import org.spongepowered.api.data.Keys;
import org.spongepowered.common.accessor.world.entity.EntityAccessor;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.common.data.contextual.ContextualData;
import org.spongepowered.common.data.contextual.PerspectiveContainer;
import org.spongepowered.common.network.syncher.SpongeSynchedEntityDataList;

import java.util.ArrayList;
import java.util.List;

public final class ContextualPacketUtil {

    public static ClientboundSetEntityDataPacket createContextualPacket(
            final ClientboundSetEntityDataPacket original, final ContextualData context, final DataPerspective perspective) {
        final @Nullable PerspectiveContainer<?, ?> contextualData = context.dataPerception(perspective);
        if (contextualData == null) {
            return original;
        }
        return ContextualPacketUtil.createContextualPacket(original, contextualData);
    }

    public static ClientboundSetEntityDataPacket createContextualPacket(
            final ClientboundSetEntityDataPacket original, final PerspectiveContainer<?, ?> contextualData) {
        if (original.packedItems() instanceof final SpongeSynchedEntityDataList entityDataList
                && (entityDataList.flags() & contextualData.entityDataFlags()) == 0L) {
            return original;
        }

        List<SynchedEntityData.DataValue<?>> values = new ArrayList<>(original.packedItems().size());
        for (final SynchedEntityData.DataValue<?> dataValue : original.packedItems()) {
            if (dataValue.id() == EntityAccessor.accessor$DATA_CUSTOM_NAME().getId()) {
                values.add(SynchedEntityData.DataValue.create(
                        EntityAccessor.accessor$DATA_CUSTOM_NAME(), SpongeAdventure.asVanillaOpt(contextualData.require(Keys.CUSTOM_NAME))));
            } else {
                values.add(dataValue);
            }
        }

        return new ClientboundSetEntityDataPacket(original.id(), values);
    }
}
