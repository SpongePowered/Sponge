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
import org.spongepowered.api.effect.sound.SoundCategories;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.item.inventory.InteractItemEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Task;

@Plugin(id = "stopsoundtest", version = "0.0.0", name = "StopSounds", description = "Stops sounds when right clicking an ender rod")
public class StopSoundTest implements LoadableModule {

    public static final String ID = "stopsoundtest";

    @Inject private PluginContainer pluginContainer;

    private final StopSoundListener listener = new StopSoundListener();



    @Override
    public void enable(CommandSource src) {
        Sponge.getEventManager().registerListeners(this.pluginContainer, this.listener);
    }

    public static class StopSoundListener {

        @Listener
        public void onUseItem(InteractItemEvent event, @First Player player) {
            if (event.getItemStack().getType() != ItemTypes.END_ROD) {
                return;
            }
            player.playSound(SoundTypes.ENTITY_ENDERMEN_DEATH, SoundCategories.MASTER, player.getLocation().getPosition(), 1.0);
            if (event instanceof InteractItemEvent.Secondary) {
                PluginContainer pluginContainer = Sponge.getPluginManager().getPlugin(ID).get();
                Task.builder()
                        .delayTicks(5)
                        .execute(() -> player.stopSounds(SoundTypes.ENTITY_ENDERMEN_DEATH, SoundCategories.MASTER))
                        .submit(pluginContainer);
            }
        }
    }
}
