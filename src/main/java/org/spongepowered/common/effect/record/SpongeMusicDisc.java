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
package org.spongepowered.common.effect.record;


import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundLevelEventPacket;
import net.minecraft.world.item.RecordItem;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.effect.sound.SoundType;
import org.spongepowered.api.effect.sound.music.MusicDisc;
import org.spongepowered.common.accessor.world.item.RecordItemAccessor;
import org.spongepowered.math.vector.Vector3i;

import java.util.Objects;

public final class SpongeMusicDisc implements MusicDisc {

    /**
     * This is the effect ID that is used by the Effect packet to play a record effect.
     * http://wiki.vg/Protocol#Effect
     */
    private static final int EFFECT_ID = 1010;

    private final RecordItem item;
    private final int id;

    public SpongeMusicDisc(final RecordItem item) {
        this.item = item;
        this.id = BuiltInRegistries.ITEM.getId(item);
    }

    public int getId() {
        return this.id;
    }

    @Override
    public SoundType sound() {
        return (SoundType) ((RecordItemAccessor) this.item).accessor$sound();
    }

    public static ClientboundLevelEventPacket createPacket(final Vector3i position, final @Nullable MusicDisc recordType) {
        Objects.requireNonNull(position, "position");
        final BlockPos pos = new BlockPos(position.x(), position.y(), position.z());
        // see RecordItem.useOn
        return new ClientboundLevelEventPacket(SpongeMusicDisc.EFFECT_ID, pos, recordType == null ? 0 :
                ((SpongeMusicDisc) recordType).getId(), false);
    }
}
