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
package org.spongepowered.forge.hook;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.common.hooks.WorldHooks;

public class ForgeWorldHooks implements WorldHooks {

    @Override
    public Entity getCustomEntityIfItem(final Entity entity) {
        if (entity.getClass().equals(ItemEntity.class)) {
            final ItemStack stack = ((ItemEntity) entity).getItem();
            final Item item = stack.getItem();
            if (item.hasCustomEntity(stack)) {
                final Entity newEntity = item.createEntity(entity.level, entity, stack);
                if (newEntity != null) {
                    entity.remove(Entity.RemovalReason.DISCARDED);
                    return newEntity;
                }
            }
        }
        return null;
    }

    @Override
    public boolean isRestoringBlocks(final Level world) {
        return world.restoringBlockSnapshots;
    }

    @Override
    public void postLoadWorld(final ServerLevel world) {
        // MinecraftForge.EVENT_BUS.post(new WorldEvent.Load(world)); // TODO SF 1.19.4
    }
}
