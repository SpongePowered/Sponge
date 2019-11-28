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
package org.spongepowered.common.data.fixer.entity.player;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.collect.Maps;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.datafix.IFixableData;
import org.spongepowered.api.util.RespawnLocation;
import org.spongepowered.common.util.Constants;

import java.util.Map;
import java.util.UUID;

public class PlayerRespawnData implements IFixableData {

    @Override
    public int getFixVersion() {
        return Constants.Sponge.PlayerData.RESPAWN_DATA_1_9_VERSION;
    }

    @Override
    public CompoundNBT fixTagCompound(CompoundNBT compound) {
        final Map<UUID, RespawnLocation> spawnLocations = Maps.newHashMap();

        if (compound.contains(Constants.Sponge.User.USER_SPAWN_X, Constants.NBT.TAG_ANY_NUMERIC)
            && compound.contains(Constants.Sponge.User.USER_SPAWN_Y, Constants.NBT.TAG_ANY_NUMERIC)
            && compound.contains(Constants.Sponge.User.USER_SPAWN_Z, Constants.NBT.TAG_ANY_NUMERIC)) {
            Vector3d pos = new Vector3d(compound.getInt(Constants.Sponge.User.USER_SPAWN_X),
                    compound.getInt(Constants.Sponge.User.USER_SPAWN_Y),
                    compound.getInt(Constants.Sponge.User.USER_SPAWN_Z));
//            final UUID key = WorldPropertyRegistryModule.dimIdToUuid(0);
//            spawnLocations.put(key, RespawnLocation.builder().world(key).position(pos).build());
            // This is the point where we need to check the old data, if it is available.
        }
        ListNBT spawnlist = compound.getList(Constants.Sponge.User.USER_SPAWN_LIST, Constants.NBT.TAG_COMPOUND);
        // This is legacy forge versions, not sure how forge is going to be saving it from now on, but
        // we can at least start moving all of this to our own compound and overwrite as necessary
        for (int i = 0; i < spawnlist.tagCount(); i++) {
            CompoundNBT spawndata = spawnlist.getCompound(i);
//            UUID uuid = WorldPropertyRegistryModule.dimIdToUuid(spawndata.getInteger(NbtDataUtil.USER_SPAWN_DIM));
//            if (uuid != null) {
//                spawnLocations.put(uuid, RespawnLocation.builder().world(uuid).position(
//                        new Vector3d(spawndata.getInteger(NbtDataUtil.USER_SPAWN_X),
//                                spawndata.getInteger(NbtDataUtil.USER_SPAWN_Y),
//                                spawndata.getInteger(NbtDataUtil.USER_SPAWN_Z))).build());
//            }
        }
        return compound;
    }
}
