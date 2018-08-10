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
package org.spongepowered.common.interfaces.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.api.data.manipulator.DataManipulator;

import java.util.List;
import java.util.Optional;

public interface IMixinItem {

    void getManipulatorsFor(ItemStack itemStack, List<DataManipulator<?, ?>> list);

    /**
     * @author gabizou - April 22nd, 2018
     * @reason This adds the ability for forge mods to override the custom item entity
     * to be dropped. The condition is that if the item intends to add a custom entity
     * instead of the item dropped, it will return a non-empty optional. If the custom
     * entity is provided, it will be spawned in the place of the item stack entity, and
     * the related {@link EntityItem} is then removed from the world and spawn list.
     *
     * @param world The world to spawn the entity
     * @param location The location to spawn the entity
     * @param itemstack The item stack to be used for the entity (should be the same item type of this item)
     * @return The custom entity item instead of the original entity to be created
     */
    default Optional<Entity> getCustomEntityItem(World world, Entity location, ItemStack itemstack) {
        return Optional.empty();
    }

}
