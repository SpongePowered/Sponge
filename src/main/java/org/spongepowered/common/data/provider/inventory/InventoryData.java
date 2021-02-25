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
package org.spongepowered.common.data.provider.inventory;

import net.kyori.adventure.text.Component;
import net.minecraft.world.Nameable;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.item.inventory.Container;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.common.bridge.world.inventory.InventoryBridge;
import org.spongepowered.common.bridge.network.chat.BaseComponentBridge;
import org.spongepowered.common.data.provider.DataProviderRegistrator;
import org.spongepowered.common.inventory.custom.CustomInventory;
import org.spongepowered.common.inventory.util.InventoryUtil;

public final class InventoryData {

    private InventoryData() {
    }

    // @formatter:off
    public static void register(final DataProviderRegistrator registrator) {
        registrator
                .asImmutable(Inventory.class)
                    .create(Keys.MAX_STACK_SIZE)
                        .get(h -> (((InventoryBridge) h).bridge$getAdapter().inventoryAdapter$getFabric().fabric$getMaxStackSize()))
                    .create(Keys.PLUGIN_CONTAINER)
                        .get(InventoryUtil::getPluginContainer)
                    .create(Keys.DISPLAY_NAME)
                        .get(InventoryData::findDisplayName)
                    .create(Keys.UNIQUE_ID)
                        .get(h -> {
                            if (h instanceof CustomInventory) {
                                return ((CustomInventory) h).getIdentity();
                            }
                            return null;
                        });
    }
    // @formatter:on

    private static Component findDisplayName(Inventory inventory) {
        if (inventory instanceof Container) {
            for (Inventory viewed : ((Container) inventory).getViewed()) {
                inventory = viewed;
                break;
            }
        }
        if (inventory instanceof Nameable) {
            final net.minecraft.network.chat.Component displayName = ((Nameable) inventory).getDisplayName();
            return ((BaseComponentBridge) displayName).bridge$asAdventureComponent();
        }
        return null;
    }
}
