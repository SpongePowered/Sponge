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

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.UUID;
import net.minecraft.network.play.server.SSpawnObjectPacket;

@Mixin(SSpawnObjectPacket.class)
public interface SPacketSpawnObjectAccessor {

    @Accessor("entityId") int accessor$getEntityId();

    @Accessor("entityId") void accessor$setEntityId(int value);

    @Accessor("uniqueId") UUID accessor$getUniqueId();

    @Accessor("uniqueId") void accessor$setUniqueId(UUID value);

    @Accessor("x") double accessor$getX();

    @Accessor("x") void accessor$setX(double value);

    @Accessor("y") double accessor$getY();

    @Accessor("y") void accessor$setY(double value);

    @Accessor("z") double accessor$getZ();

    @Accessor("z") void accessor$setZ(double value);

    @Accessor("speedX") int accessor$getSpeedX();

    @Accessor("speedX") void accessor$setSpeedX(int value);

    @Accessor("speedY") int accessor$getSpeedY();

    @Accessor("speedY") void accessor$setSpeedY(int value);

    @Accessor("speedZ") int accessor$getSpeedZ();

    @Accessor("speedZ") void accessor$setSpeedZ(int value);

    @Accessor("pitch") int accessor$getPitch();

    @Accessor("pitch") void accessor$setPitch(int value);

    @Accessor("yaw") int accessor$getYaw();

    @Accessor("yaw") void accessor$setYaw(int value);

    @Accessor("type") int accessor$getType();

    @Accessor("type") void accessor$setType(int value);

    @Accessor("data") int accessor$getData();

    @Accessor("data") void accessor$setData(int value);



}
