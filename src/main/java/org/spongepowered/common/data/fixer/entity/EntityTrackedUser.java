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

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.datafix.IFixableData;
import org.spongepowered.common.util.Constants;

import java.util.UUID;

public class EntityTrackedUser implements IFixableData {

    @Override
    public int getFixVersion() {
        return Constants.Legacy.Entity.TRACKER_ID_VERSION;
    }

    @Override
    public NBTTagCompound fixTagCompound(NBTTagCompound compound) {
        final NBTBase forgeCompound = compound.getTag(Constants.Forge.FORGE_DATA);
        if (forgeCompound != null) {
            final NBTTagCompound forgeData = (NBTTagCompound) forgeCompound;
            final NBTBase spongeCompound = forgeData.getTag(Constants.Sponge.SPONGE_DATA);
            if (spongeCompound != null) {
                final NBTTagCompound spongeData = (NBTTagCompound) spongeCompound;
                process(spongeData, Constants.Sponge.SPONGE_ENTITY_CREATOR);
                process(spongeData, Constants.Sponge.SPONGE_ENTITY_NOTIFIER);
            }
        }
        return compound;
    }

    private static void process(NBTTagCompound spongeData, String type) {
        if (spongeData.hasKey(type, Constants.NBT.TAG_COMPOUND)) {
            final NBTTagCompound creatorTag = spongeData.getCompoundTag(type);
            final long least = creatorTag.getLong(Constants.Legacy.Entity.UUID_LEAST_1_8);
            final long most = creatorTag.getLong(Constants.Legacy.Entity.UUID_MOST_1_8);
            final UUID creator = new UUID(most, least);
            creatorTag.removeTag(Constants.Legacy.Entity.UUID_LEAST_1_8);
            creatorTag.removeTag(Constants.Legacy.Entity.UUID_MOST_1_8);
            creatorTag.setUniqueId(Constants.UUID, creator);
        }
    }
}
