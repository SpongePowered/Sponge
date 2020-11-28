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
package org.spongepowered.common.accessor.entity.player;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ServerPlayerEntity.class)
public interface ServerPlayerEntityAccessor {

    @Accessor("invulnerableDimensionChange") void accessor$setInvulnerableDimensionChange(boolean invulnerableDimensionChange);

    @Accessor("language") String accessor$getLanguage();

    @Accessor("seenCredits") boolean accessor$getSeenCredits();

    @Accessor("chatColours") boolean accessor$getChatColours();

    @Accessor("seenCredits") void accessor$setSeenCredits(boolean seenCredits);

    @Accessor("enteredNetherPosition") void accessor$setEnteredNetherPosition(Vec3d value);

    @Accessor("lastExperience") void accessor$setLastExperience(int value);

    @Accessor("lastHealth") void accessor$setLastHealth(float value);

    @Accessor("lastFoodLevel") void accessor$setLastFoodLevel(int value);

    @Invoker("func_213846_b") void accessor$func_213846_b(ServerWorld toWorld);
}
