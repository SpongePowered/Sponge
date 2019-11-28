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
    public <T> void func_187214_a(DataParameter<T> key, T value) {
        this.cached.func_187214_a(key, value);
    }

    @Override
    public <T> T func_187225_a(DataParameter<T> key) {
        return this.cached.func_187225_a(key);
    }

    @Override
    public <T> void func_187227_b(DataParameter<T> key, T value) {
        this.cached.func_187227_b(key, value);
    }

    @Override
    public <T> void func_187217_b(DataParameter<T> key) {
        this.cached.func_187217_b(key);
    }

    @Override
    public boolean func_187223_a() {
        return this.cached.func_187223_a();
    }

    @Nullable
    @Override
    public List<DataEntry<?>> func_187221_b() {
         final List<DataEntry<?>> dirtyEntries = this.cached.func_187221_b();
        final List<DataEntry<?>> dirty = new ArrayList<>(dirtyEntries.size());
        for (DataEntry<?> dataEntry : dirtyEntries) {
            if (dataEntry.func_187205_a() == EntityLivingBaseAccessor.accessor$getHealthParameter()) {
                dirty.add(new DataEntry<>(EntityLivingBaseAccessor.accessor$getHealthParameter(), ((EntityPlayerMPBridge) ((EntityDataManagerAccessor) this).accessor$getEntity()).bridge$getInternalScaledHealth()));
            } else {
                dirty.add(dataEntry);
            }
        }
        return dirty;
    }

    @Override
    public void func_187216_a(PacketBuffer buf) throws IOException {
        this.cached.func_187216_a(buf);
    }

    @Nullable
    @Override
    public List<DataEntry<?>> func_187231_c() {
        return this.cached.func_187231_c();
    }

    @Override
    public boolean func_187228_d() {
        return this.cached.func_187228_d();
    }

    @Override
    public void func_187230_e() {
        this.cached.func_187230_e();
    }
}
