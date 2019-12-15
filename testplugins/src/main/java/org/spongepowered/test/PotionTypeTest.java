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
package org.spongepowered.test;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.potion.PotionType;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageReceiver;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Test Potion Types
 */
@Plugin(id = "potion_type_test", name = "PotionType Test", description = "A plugin to test potion types", version = "0.0.0")
public class PotionTypeTest implements LoadableModule {

    @Override
    public void enable(MessageReceiver src) {
        for (Player player : Sponge.getServer().getOnlinePlayers()) {
            for (int i = 0; i < 5; i++) {
                player.getInventory().offer(this.getRandomPotion());
                player.sendMessage(Text.of("You received random potions!"));
            }
        }
    }

    private ItemStack getRandomPotion() {
        ItemStack potion = ItemStack.of(ItemTypes.POTION, 1);
        List<PotionType> potions = new ArrayList<>(Sponge.getRegistry().getAllOf(PotionType.class));
        int i = new Random().nextInt(potions.size());
        potion.offer(Keys.POTION_TYPE, potions.get(i));
        return potion;
    }
}
