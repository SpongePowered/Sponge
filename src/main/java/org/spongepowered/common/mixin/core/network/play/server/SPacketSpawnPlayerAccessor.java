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
package org.spongepowered.common.mixin.core.network.play.server;

import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.server.SSpawnPlayerPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;
import java.util.UUID;

@Mixin(SSpawnPlayerPacket.class)
public interface SPacketSpawnPlayerAccessor {

    @Accessor("entityId") int accessor$getentityId();

    @Accessor("entityId") void accessor$setentityId(int value);

    @Accessor("uniqueId") UUID accessor$getuniqueId();

    @Accessor("uniqueId") void accessor$setuniqueId(UUID value);

    @Accessor("x") double accessor$getx();

    @Accessor("x") void accessor$setx(double value);

    @Accessor("y") double accessor$gety();

    @Accessor("y") void accessor$sety(double value);

    @Accessor("z") double accessor$getZ();

    @Accessor("z") void accessor$setZ(double value);

    @Accessor("yaw") byte accessor$getYaw();

    @Accessor("yaw") void accessor$setYaw(byte value);

    @Accessor("pitch") byte accessor$getPitch();

    @Accessor("pitch") void accessor$setPitch(byte value);

    @Accessor("watcher") EntityDataManager accessor$getWatcher();

    @Accessor("watcher") void accessor$setWatcher(EntityDataManager value);

    @Accessor("dataManagerEntries") List<EntityDataManager.DataEntry<?>> accessor$getDataManagerEntires();

    @Accessor("dataManagerEntries") void accessor$setDataManagerEntires(List<EntityDataManager.DataEntry<?>> entries);

}
