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
package org.spongepowered.forge.mixin.core.api.event.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.eventbus.api.Event;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.event.entity.ChangeEntityWorldEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.forge.mixin.core.api.event.EventMixin_Forge;

import java.util.Collection;
import java.util.Collections;

@Mixin(ChangeEntityWorldEvent.Pre.class)
public interface ChangeEntityWorldEvent_PreMixin_Forge extends EventMixin_Forge {

    @Override
    default @Nullable Collection<? extends Event> bridge$createForgeEvents() {
        final ChangeEntityWorldEvent.Pre thisEvent = (ChangeEntityWorldEvent.Pre) this;
        return Collections.singleton(
                new EntityTravelToDimensionEvent((Entity) thisEvent.entity(), ((ServerLevel) thisEvent.destinationWorld()).dimension()));
    }

}
