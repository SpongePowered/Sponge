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
package org.spongepowered.common.network;

import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.EntityDataManager;
import org.spongepowered.common.bridge.entity.player.EntityPlayerMPBridge;
import org.spongepowered.common.mixin.core.entity.EntityLivingBaseAccessor;
import org.spongepowered.common.mixin.core.network.datasync.EntityDataManagerAccessor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

public class SpoofedEntityDataManager extends EntityDataManager {

    private final EntityDataManager cached;

    public SpoofedEntityDataManager(EntityDataManager cached, Entity owningEntity) {
        super(owningEntity);
        this.cached = cached;
    }

    @Override
    public <T> void register(DataParameter<T> key, T value) {
        this.cached.register(key, value);
    }

    @Override
    public <T> T get(DataParameter<T> key) {
        return this.cached.get(key);
    }

    @Override
    public <T> void set(DataParameter<T> key, T value) {
        this.cached.set(key, value);
    }

    @Override
    public <T> void setDirty(DataParameter<T> key) {
        this.cached.setDirty(key);
    }

    @Override
    public boolean isDirty() {
        return this.cached.isDirty();
    }

    @Nullable
    @Override
    public List<DataEntry<?>> getDirty() {
         final List<DataEntry<?>> dirtyEntries = this.cached.getDirty();
        final List<DataEntry<?>> dirty = new ArrayList<>(dirtyEntries.size());
        for (DataEntry<?> dataEntry : dirtyEntries) {
            if (dataEntry.getKey() == EntityLivingBaseAccessor.accessor$getHealthParameter()) {
                dirty.add(new DataEntry<>(EntityLivingBaseAccessor.accessor$getHealthParameter(), ((EntityPlayerMPBridge) ((EntityDataManagerAccessor) this).accessor$getEntity()).bridge$getInternalScaledHealth()));
            } else {
                dirty.add(dataEntry);
            }
        }
        return dirty;
    }

    @Override
    public void writeEntries(PacketBuffer buf) throws IOException {
        this.cached.writeEntries(buf);
    }

    @Nullable
    @Override
    public List<DataEntry<?>> getAll() {
        return this.cached.getAll();
    }

    @Override
    public boolean isEmpty() {
        return this.cached.isEmpty();
    }

    @Override
    public void setClean() {
        this.cached.setClean();
    }
}
