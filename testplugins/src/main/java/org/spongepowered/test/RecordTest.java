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

import com.google.inject.Inject;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.property.item.RecordProperty;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.effect.sound.music.MusicDisc;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.item.inventory.InteractItemEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.channel.MessageReceiver;

import java.util.Optional;

@Plugin(id = "record_test", name = "Record Test", description = "Right click to start/stop a record at a position.", version = "0.0.0")
public class RecordTest implements LoadableModule {

    @Inject private PluginContainer container;

    private final RecordListener listener = new RecordListener();

    @Override
    public void enable(MessageReceiver src) {
        Sponge.getEventManager().registerListeners(this.container, this.listener);
    }

    public static class RecordListener {

        @Listener
        public void onPlayerInteract(InteractItemEvent.Secondary event, @Root Player player) {
            final ItemStack itemStack = player.getItemInHand(HandTypes.MAIN_HAND.get());
            if (!itemStack.isEmpty()) {

                final Optional<MusicDisc> optRecord = itemStack.get(RecordProperty.class); // TODO Key missing?
                if (optRecord.isPresent()) {
                    player.playMusicDisc(player.getBlockPosition(), optRecord.get());
                } else if (itemStack.getType() == ItemTypes.SPONGE.get()) {
                    player.stopMusicDisc(player.getBlockPosition());
                }
            }
        }
    }
}
