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
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.scheduler.ScheduledTask;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageReceiver;

import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

@Plugin(id = "activeitemdatatest", name = "Active Item Data Test", description = "Testing some nice active item data.", version = "0.0.0")
public final class ActiveItemDataTest implements LoadableModule {

    @Nullable private ScheduledTask task;

    @Override
    public void disable(MessageReceiver src) {
        if (this.task != null) {
            this.task.cancel();
            Sponge.getServer().getBroadcastChannel().send(Text.of("Active item task cancelled."));
        }
    }

    @Override
    public void enable(MessageReceiver src) {
        if (this.task != null) {
            this.task.cancel();
        }
        this.task = Sponge.getServer().getScheduler().submit(Task.builder()
                .interval(5, TimeUnit.SECONDS)
                .name("activeitemtask")
                .execute(() -> {
                    for (Player p : Sponge.getServer().getOnlinePlayers()) {
                        p.sendMessage(Text.of(Text.of("Your active item is " +
                                p.get(Keys.ACTIVE_ITEM).orElse(ItemStackSnapshot.empty()).getType().getKey())));
                        p.offer(Keys.ACTIVE_ITEM, ItemStackSnapshot.empty());
                    }
                })
                .delay(5, TimeUnit.SECONDS)
                .build());

        Sponge.getServer().getBroadcastChannel().send(Text.of("Active item task set."));
    }

}
