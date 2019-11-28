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

import static com.google.common.base.Preconditions.checkNotNull;

import com.flowpowered.math.vector.Vector3i;
import net.minecraft.item.Item;
import net.minecraft.network.play.server.SPlaySoundEventPacket;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.api.effect.sound.SoundType;
import org.spongepowered.api.effect.sound.record.RecordType;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.common.SpongeCatalogType;
import org.spongepowered.common.text.translation.SpongeTranslation;

import javax.annotation.Nullable;

public class SpongeRecordType extends SpongeCatalogType.Translatable implements RecordType {

    /**
     * This is the effect ID that is used by the Effect packet to play a record effect.
     * http://wiki.vg/Protocol#Effect
     */
    private static final int EFFECT_ID = 1010;

    private final ItemType recordItem;
    private final SoundType soundType;

    public SpongeRecordType(String id, String translation, ItemType recordItem, SoundType soundType) {
        super(id, new SpongeTranslation(translation));
        this.recordItem = recordItem;
        this.soundType = soundType;
    }

    public int getInternalId() {
        return Item.REGISTRY.getId((Item) recordItem);
    }

    @Override
    public SoundType getSound() {
        return this.soundType;
    }

    public static SPlaySoundEventPacket createPacket(Vector3i position, @Nullable RecordType recordType) {
        checkNotNull(position, "position");
        final BlockPos pos = new BlockPos(position.getX(), position.getY(), position.getZ());
        return new SPlaySoundEventPacket(EFFECT_ID, pos, recordType == null ? 0 :
                ((SpongeRecordType) recordType).getInternalId(), false);
    }
}
