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
package org.spongepowered.common.mixin.accessor.entity;

import net.minecraft.entity.Entity;
import net.minecraft.network.datasync.DataParameter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

@Mixin(Entity.class)
public interface EntityAccessor {

    @Accessor("NEXT_ENTITY_ID") static AtomicInteger accessor$getNextEntityID() {
        throw new IllegalStateException("Untransformed Accessor!");
    }

    @Accessor("FLAGS") static DataParameter<Byte> accessor$getFlags() {
        throw new IllegalStateException("Untransformed Accessor!");
    }

    @Accessor("AIR") static DataParameter<Integer> accessor$getAir() {
        throw new IllegalStateException("Untransformed Accessor!");
    }

    @Accessor("CUSTOM_NAME") static DataParameter<String> accessor$getCustomName() {
        throw new IllegalStateException("Untransformed Accessor!");
    }

    @Accessor("CUSTOM_NAME_VISIBLE") static DataParameter<Boolean> accessor$getCustomNameVisible() {
        throw new IllegalStateException("Untransformed accessor!");
    }

    @Accessor("SILENT") static DataParameter<Boolean> accessor$getSilent() {
        throw new IllegalStateException("Untransformed Accessor!");
    }

    @Accessor("NO_GRAVITY") static DataParameter<Boolean> accessor$getNoGravity() {
        throw new IllegalStateException("Untransformed Accessor!");
    }

    @Accessor("rand") Random accessor$getRand();

    @Accessor("fire") int accessor$getFire();

    @Accessor("fire") void accessor$setFire(int fire);

    @Invoker("copyDataFromOld") void accessor$copyDataFromOld(Entity entity);

    @Invoker("setFlag") void accessor$setFlag(int flag, boolean set);

    @Invoker("getFireImmuneTicks") int accessor$getFireImmuneTicks();
}
