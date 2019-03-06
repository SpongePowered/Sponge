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
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;

import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

@Plugin(id = "activeitemdatatest", name = "Active Item Data Test", description = "Testing some nice active item data.", version = "0.0.0")
public final class ActiveItemDataTest implements LoadableModule {

    @Nullable private Task task;

    @Override
    public void disable(CommandSource src) {
        if (this.task != null) {
            this.task.cancel();
            Sponge.getServer().getBroadcastChannel().send(Text.of("Active item task cancelled."));
        }
    }

    @Override
    public void enable(CommandSource src) {
        if (this.task != null) {
            this.task.cancel();
        }
        this.task = Task.builder()
                .interval(5, TimeUnit.SECONDS)
                .name("activeitemtask")
                .execute(() -> {
                    for (Player p : Sponge.getServer().getOnlinePlayers()) {
                        p.sendMessage(Text.of(Text.of("Your active item is " +
                                p.get(Keys.ACTIVE_ITEM).orElse(ItemStackSnapshot.NONE).getType().getId())));
                        p.offer(Keys.ACTIVE_ITEM, ItemStackSnapshot.NONE);
                    }
                })
                .delay(5, TimeUnit.SECONDS)
                .submit(this);

        Sponge.getServer().getBroadcastChannel().send(Text.of("Active item task set."));
    }

}
