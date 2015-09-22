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
package org.spongepowered.common.data.processor.common;

import com.google.common.collect.Iterables;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntitySkull;
import org.spongepowered.api.data.type.SkullType;
import org.spongepowered.api.data.type.SkullTypes;
import org.spongepowered.common.Sponge;

public class SkullUtils {

    /**
     * There's not really a meaningful default value for this, since it's a CatalogType.
     * However, the Vanilla give command defaults the skeleton type (index 0), so it's used as
     * the default here.
     */
    public static final SkullType DEFAULT_TYPE = SkullTypes.SKELETON;

    public static boolean supportsObject(Object object) {
        return object instanceof TileEntitySkull || isValidItemStack(object);
    }

    public static SkullType getSkullType(int skullType) {
        return Iterables.get(Sponge.getSpongeRegistry().skullTypeMappings.values(), skullType);
    }

    public static boolean isValidItemStack(Object container) {
        return container instanceof ItemStack && ((ItemStack) container).getItem().equals(Items.skull);
    }

    public static SkullType getSkullType(TileEntitySkull tileEntitySkull) {
        return SkullUtils.getSkullType(tileEntitySkull.getSkullType());
    }

    public static void setSkullType(TileEntitySkull tileEntitySkull, int skullType) {
        tileEntitySkull.setType(skullType);
        tileEntitySkull.markDirty();
        tileEntitySkull.getWorld().markBlockForUpdate(tileEntitySkull.getPos());
    }

    public static SkullType getSkullType(ItemStack itemStack) {
        return SkullUtils.getSkullType(itemStack.getMetadata());
    }

}
