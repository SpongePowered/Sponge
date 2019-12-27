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
package org.spongepowered.common.item.inventory;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.item.inventory.ContainerType;
import org.spongepowered.common.SpongeCatalogType;
import org.spongepowered.common.inventory.lens.LensCreator;

public class SpongeContainerType extends SpongeCatalogType implements ContainerType {

    private ContainerTypeRegistryModule.ContainerProvider containerProvider;
    private final int size;
    private final int width;
    private final int height;
    private LensCreator lensCreator;

    public SpongeContainerType(CatalogKey key, int size, int width, int height, LensCreator lensCreator, ContainerTypeRegistryModule.ContainerProvider containerProvider) {
        super(key);
        this.containerProvider = containerProvider;
        this.lensCreator = lensCreator;
        this.size = size;
        this.width = width;
        this.height = height;
    }

    public LensCreator getLensCreator() {
        return this.lensCreator;
    }

    public int getSize() {
        return this.size;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public Container provideContainer(int id, IInventory viewed, PlayerEntity viewing) {
        return this.containerProvider.provide(id, viewed, viewing);
    }
}
