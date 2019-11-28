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
package org.spongepowered.common.data.fixer.world;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.datafix.IFixableData;
import org.spongepowered.common.util.Constants;

import java.util.UUID;

public class SpongeLevelFixer implements IFixableData {

    @Override
    public int func_188216_a() {
        return Constants.Legacy.World.WORLD_UUID_1_9_VERSION;
    }

    @Override
    public NBTTagCompound func_188217_a(NBTTagCompound compound) {
        { // Fixes the world unique id
            final long least = compound.func_74763_f(Constants.Legacy.World.WORLD_UUID_LEAST_1_8);
            final long most = compound.func_74763_f(Constants.Legacy.World.WORLD_UUID_MOST_1_8);
            final UUID worldId = new UUID(most, least);
            compound.func_82580_o(Constants.Legacy.World.WORLD_UUID_LEAST_1_8);
            compound.func_82580_o(Constants.Legacy.World.WORLD_UUID_MOST_1_8);
            compound.func_186854_a(Constants.UUID, worldId);

        }
        // Fixes the Player Id Table
        if (compound.func_150297_b(Constants.Sponge.SPONGE_PLAYER_UUID_TABLE, Constants.NBT.TAG_LIST)) {
            final NBTTagList playerIdList = compound.func_150295_c(Constants.Sponge.SPONGE_PLAYER_UUID_TABLE, Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < playerIdList.func_74745_c(); i++) {
                final NBTTagCompound playerIdCompound = playerIdList.func_150305_b(i);
                final long least = playerIdCompound.func_74763_f(Constants.Legacy.World.WORLD_UUID_LEAST_1_8);
                final long most = playerIdCompound.func_74763_f(Constants.Legacy.World.WORLD_UUID_MOST_1_8);
                playerIdCompound.func_82580_o(Constants.Legacy.World.WORLD_UUID_LEAST_1_8);
                playerIdCompound.func_82580_o(Constants.Legacy.World.WORLD_UUID_MOST_1_8);
                final UUID playerId = new UUID(most, least);
                playerIdCompound.func_186854_a(Constants.UUID, playerId);
            }
        }
        return compound;
    }
}
