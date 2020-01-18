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
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.math.vector.Vector3d;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Test for User(Offline-Player) Location and Rotation
 */
@Plugin(id = "offlinelocationtest", name = "Offline Location Test", description = "A plugin to test offline location and rotation", version = "0.0.0")
public class OfflineLocationTest {

    private Set<UUID> set = new HashSet<>();

    @Inject private PluginContainer container;
    @Listener
    public void onGamePreInitialization(GamePreInitializationEvent event) {
        Sponge.getCommandManager().register(this.container, Command.builder()
                .setShortDescription(Text.of("Teleport while offline"))
                .setExecutor((ctx) -> {
                    if (!(ctx.getSubject() instanceof Player)) {
                        ctx.getMessageReceiver().sendMessage(Text.of(TextColors.RED, "Player only."));
                        return CommandResult.success();
                    }
                    Player player = (Player) ctx.getSubject();
                    this.set.add(player.getUniqueId());
                    player.kick(Text.of("You got moved"));
                    return CommandResult.success();
                })
                .build(), "kickandtpme");
    }

    @Listener
    public void onDisconnect(ClientConnectionEvent.Disconnect event, @Root Player player) {
        UUID uuid = player.getUniqueId();
        if (this.set.remove(uuid)) {
            Task task = Task.builder().delayTicks(20).execute(() -> this.run(uuid)).build();
            Sponge.getServer().getScheduler().submit(task);
        }
    }

    private void run(UUID uuid) {
        User user = Sponge.getServiceManager().provideUnchecked(UserStorageService.class).get(uuid).get();
        user.getWorldUniqueId().ifPresent(world -> {
            Vector3d newPos = user.getPosition().add(0, 1, 0);
            user.setLocation(newPos, world);
        });
        Vector3d rot = user.getRotation();
        rot = new Vector3d(-rot.getX(), (rot.getY() + 180) % 360, 0);
        user.setRotation(rot);
    }

}
