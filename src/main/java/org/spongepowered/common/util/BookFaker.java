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

import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.ServerPlayNetHandler;
import net.minecraft.network.play.server.SCustomPayloadPlayPacket;
import net.minecraft.network.play.server.SSetSlotPacket;
import net.minecraft.util.Hand;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.BookView;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;

public class BookFaker {

    public static final int WINDOW_PLAYER_INVENTORY = 0;

    public static void fakeBookView(BookView bookView, Player player) {
        ServerPlayerEntity mcPlayer = (ServerPlayerEntity) player;
        ServerPlayNetHandler receiver = mcPlayer.connection;

        // First we need to send a fake a Book ItemStack with the BookView's
        // contents to the player's hand
        ItemStack item = ItemStack.of(ItemTypes.WRITTEN_BOOK, 1);
        item.offer(Keys.DISPLAY_NAME, bookView.getTitle());
        item.offer(Keys.BOOK_AUTHOR, bookView.getAuthor());
        item.offer(Keys.BOOK_PAGES, bookView.getPages());

        PlayerInventory inventory = mcPlayer.inventory;
        int bookSlot = inventory.mainInventory.size() + inventory.currentItem;
        receiver.sendPacket(new SSetSlotPacket(WINDOW_PLAYER_INVENTORY, bookSlot, ItemStackUtil.toNative(item)));

        // Next we tell the client to open the Book GUI
        PacketBuffer packetbuffer = new PacketBuffer(Unpooled.buffer());
        packetbuffer.writeEnumValue(Hand.MAIN_HAND);
        receiver.sendPacket(new SCustomPayloadPlayPacket("MC|BOpen", packetbuffer));

        // Now we can remove the fake Book since it's contents will have already
        // been transferred to the GUI
        receiver.sendPacket(new SSetSlotPacket(WINDOW_PLAYER_INVENTORY, bookSlot, inventory.getCurrentItem()));
    }

}
