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
package org.spongepowered.common.event.tracking.context.transaction.inventory;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import org.spongepowered.api.item.inventory.crafting.CraftingInventory;

import java.util.Optional;

public class CraftingPreviewTransaction extends ContainerBasedTransaction {

    final Player player;
    final CraftingInventory craftingInventory;
    final CraftingContainer craftSlots;

    public CraftingPreviewTransaction(
        final ServerPlayer player, final CraftingInventory craftingInventory, final CraftingContainer craftSlots
    ) {
        super(player.containerMenu);
        this.player = player;
        this.craftingInventory = craftingInventory;
        this.craftSlots = craftSlots;
    }

    @Override
    public Optional<AbsorbingFlowStep> parentAbsorber() {
        return Optional.of((ctx, tx) -> tx.acceptCraftingPreview(ctx, this));
    }

    @Override
    public boolean shouldHaveBeenAbsorbed() {
        return true;
    }
}
