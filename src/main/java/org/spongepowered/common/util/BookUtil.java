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
package org.spongepowered.common.util;

import com.google.common.collect.Lists;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.translation.GlobalTranslator;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.ServerPlayNetHandler;
import net.minecraft.network.play.server.SOpenBookWindowPacket;
import net.minecraft.network.play.server.SSetSlotPacket;
import net.minecraft.util.Hand;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.common.item.util.ItemStackUtil;

import java.util.Collection;
import java.util.Locale;
import java.util.Objects;

public final class BookUtil {

    public static final int WINDOW_PLAYER_INVENTORY = 0;
    private static final Locale STARTER_LOCALE = new Locale("placeholder", "LANG");

    public static void fakeBookView(final Book book, final Collection<? extends Player> players) {

        // First we need to send a fake a Book ItemStack with the BookView's
        // contents to the player's hand
        // These values are localized since sending item NBT doesn't trigger translation
        final ItemStack item = ItemStack.of(ItemTypes.WRITTEN_BOOK, 1);
        Locale lastLocale = STARTER_LOCALE;

        for (final Player player : players) {
            if (!Objects.equals(player.getLocale(), lastLocale)) {
                lastLocale = player.getLocale();
                item.offer(Keys.DISPLAY_NAME, GlobalTranslator.render(book.title(), lastLocale));
                item.offer(Keys.AUTHOR, GlobalTranslator.render(book.author(), lastLocale));
                final Locale finalLastLocale = lastLocale;
                item.offer(Keys.PAGES, Lists.transform(book.pages(), page -> GlobalTranslator.render(page, finalLastLocale)));
            }

            final ServerPlayerEntity mcPlayer = (ServerPlayerEntity) player;
            final ServerPlayNetHandler receiver = mcPlayer.connection;

            final PlayerInventory inventory = mcPlayer.inventory;
            final int bookSlot = inventory.mainInventory.size() + inventory.currentItem;
            receiver.sendPacket(new SSetSlotPacket(WINDOW_PLAYER_INVENTORY, bookSlot, ItemStackUtil.toNative(item)));

            // Next we tell the client to open the Book GUI
            receiver.sendPacket(new SOpenBookWindowPacket(Hand.MAIN_HAND));

            // Now we can remove the fake Book since it's contents will have already
            // been transferred to the GUI
            receiver.sendPacket(new SSetSlotPacket(WINDOW_PLAYER_INVENTORY, bookSlot, inventory.getCurrentItem()));
        }
    }

    private BookUtil() {
    }
}
