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
package org.spongepowered.common.item.inventory.adapter.impl;

import org.spongepowered.api.item.inventory.Container;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.common.item.inventory.lens.Fabric;

public class VanillaContainerAdapter extends AbstractInventoryAdapter {

    /**
     * Used to calculate {@link #getPlugin()}.
     */
    private final Container rootContainer;

    public VanillaContainerAdapter(Fabric inventory, net.minecraft.inventory.container.Container container) {
        super(inventory);
        this.rootContainer = (Container) container;
    }

    @Override
    public PluginContainer getPlugin() {
        PluginContainer plugin = super.getPlugin();
        if (plugin != null) {
            return plugin;
        }
        if (this.rootContainer == null) {
            return null;
        }
        return this.rootContainer.getPlugin();
    }
}
