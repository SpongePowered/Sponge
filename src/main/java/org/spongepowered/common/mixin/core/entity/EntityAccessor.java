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
package org.spongepowered.common.mixin.core.entity;

import net.minecraft.entity.Entity;
import net.minecraft.network.datasync.DataParameter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Random;

@Mixin(Entity.class)
public interface EntityAccessor {

    @Accessor("nextEntityID")
    static int accessor$getNextEntityId() {
        throw new IllegalStateException("Untransformed EntityAccessor!");
    }

    @Accessor("nextEntityID")
    static void accessor$setNextEntityId(int id) {
        throw new IllegalStateException("Untransformed EntityAccessor!");
    }

    @Accessor("rand") Random accessor$getRandom();

    @Invoker("copyDataFromOld") void accessor$CopyDataFromOldEntity(Entity entity);

    @Invoker("setFlag") void accessor$setEntityFlag(int flag, boolean set);

    @Invoker("getFireImmuneTicks") int accessor$getFireImmuneTicks();

    @Accessor("fire") int accessor$getFire();

    @Accessor("fire") void accessor$setFire(int fire);

    @Accessor("FLAGS")
    static DataParameter<Byte> accessor$getFlagsParameter() {
        throw new IllegalStateException("Untransformed EntityAccessor!");
    }
    @Accessor("AIR")
    static DataParameter<Integer> accessor$getAirParameter() {
        throw new IllegalStateException("Untransformed EntityAccessor!");
    }
    @Accessor("CUSTOM_NAME")
    static DataParameter<String> accessor$getCustomNameParameter() {
        throw new IllegalStateException("Untransformed EntityAccessor!");
    }

    @Accessor("CUSTOM_NAME_VISIBLE")
    static DataParameter<Boolean> accessor$getCustomNameVisibleParameter() {
        throw new IllegalStateException("Untransformed EntityAccessor!");
    }
    @Accessor("SILENT")
    static DataParameter<Boolean> accessor$getSilentParameter() {
        throw new IllegalStateException("Untransformed EntityAccessor!");
    }
    @Accessor("NO_GRAVITY")
    static DataParameter<Boolean> accessor$getNoGravityParameter() {
        throw new IllegalStateException("Untransformed EntityAccessor!");
    }

}
