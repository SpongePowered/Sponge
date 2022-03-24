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
package org.spongepowered.forge.data;

import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.level.storage.WorldData;
import net.minecraftforge.fml.WorldPersistenceHooks;
import org.spongepowered.common.bridge.world.level.storage.PrimaryLevelDataBridge;
import org.spongepowered.common.server.BootstrapProperties;
import org.spongepowered.common.util.Constants;

public final class SpongeLevelDataPersistence implements WorldPersistenceHooks.WorldPersistenceHook {

    public static final SpongeLevelDataPersistence INSTANCE = new SpongeLevelDataPersistence();

    /**
     * This is actually the tag name, no idea why they call this modid...
     * @return The tag name
     */
    @Override
    public String getModId() {
        return Constants.Sponge.Data.V2.SPONGE_DATA;
    }

    @Override
    public CompoundTag getDataForWriting(LevelStorageSource.LevelStorageAccess arg, WorldData arg2) {
        return ((PrimaryLevelDataBridge) arg2).bridge$writeSpongeLevelData();
    }

    @Override
    public void readData(LevelStorageSource.LevelStorageAccess arg, WorldData arg2, CompoundTag arg3) {
        ((PrimaryLevelDataBridge) arg2).bridge$readSpongeLevelData(new Dynamic<>((DynamicOps<Tag>) BootstrapProperties.worldSettingsAdapter, arg3));
    }
}
