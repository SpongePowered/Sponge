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
package org.spongepowered.common.data.fixer.entity;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.datafix.IFixableData;
import org.spongepowered.common.util.Constants;

import java.util.UUID;

public class EntityTrackedUser implements IFixableData {

    @Override
    public int func_188216_a() {
        return Constants.Legacy.Entity.TRACKER_ID_VERSION;
    }

    @Override
    public CompoundNBT func_188217_a(CompoundNBT compound) {
        final NBTBase forgeCompound = compound.get(Constants.Forge.FORGE_DATA);
        if (forgeCompound != null) {
            final CompoundNBT forgeData = (CompoundNBT) forgeCompound;
            final NBTBase spongeCompound = forgeData.get(Constants.Sponge.SPONGE_DATA);
            if (spongeCompound != null) {
                final CompoundNBT spongeData = (CompoundNBT) spongeCompound;
                process(spongeData, Constants.Sponge.SPONGE_ENTITY_CREATOR);
                process(spongeData, Constants.Sponge.SPONGE_ENTITY_NOTIFIER);
            }
        }
        return compound;
    }

    private static void process(CompoundNBT spongeData, String type) {
        if (spongeData.contains(type, Constants.NBT.TAG_COMPOUND)) {
            final CompoundNBT creatorTag = spongeData.getCompound(type);
            final long least = creatorTag.getLong(Constants.Legacy.Entity.UUID_LEAST_1_8);
            final long most = creatorTag.getLong(Constants.Legacy.Entity.UUID_MOST_1_8);
            final UUID creator = new UUID(most, least);
            creatorTag.remove(Constants.Legacy.Entity.UUID_LEAST_1_8);
            creatorTag.remove(Constants.Legacy.Entity.UUID_MOST_1_8);
            creatorTag.putUniqueId(Constants.UUID, creator);
        }
    }
}
