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
package org.spongepowered.common.keyboard;

import com.google.common.collect.ImmutableList;
import org.spongepowered.api.Platform;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.keyboard.InteractKeyEvent;
import org.spongepowered.api.event.keyboard.RegisterKeyBindingsEvent;
import org.spongepowered.api.event.network.ChannelRegistrationEvent;
import org.spongepowered.api.keyboard.KeyBinding;
import org.spongepowered.api.network.PlayerConnection;
import org.spongepowered.api.network.RemoteConnection;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.interfaces.IMixinEntityPlayerMP;
import org.spongepowered.common.network.message.MessageKeyState;
import org.spongepowered.common.network.message.MessageKeyboardData;
import org.spongepowered.common.network.message.SpongeMessageHandler;
import org.spongepowered.common.registry.type.keyboard.KeyBindingRegistryModule;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class KeyNetworkHandler {

    @Listener
    public void onRegisterChannel(ChannelRegistrationEvent.Register event, @First PlayerConnection connection) {
        if (event.getChannel().equals(SpongeMessageHandler.CHANNEL_NAME)) {
            handleKeyRegistration(connection.getPlayer());
        }
    }

    public static void handleKeyRegistration(Player player) {
        // Check if the key bindings were already initialized
        if (!player.getKeyBindings().isEmpty()) {
            return;
        }
        Set<KeyBinding> keyBindings = new HashSet<>(KeyBindingRegistryModule.getInstance().getAll());

        RegisterKeyBindingsEvent event = SpongeEventFactory.createRegisterKeyBindingsEvent(Cause.source(player).build(), keyBindings, player);
        Sponge.getEventManager().post(event);

        if (!keyBindings.isEmpty()) {
            Set<SpongeKeyBinding> finalKeyBindings = new HashSet<>();
            Set<SpongeKeyCategory> keyCategories = new HashSet<>();

            for (KeyBinding keyBinding : keyBindings) {
                if (((SpongeKeyBinding) keyBinding).isDefault()) {
                    continue;
                }
                if (!KeyBindingRegistryModule.getInstance().isRegistered(keyBinding)) {
                    SpongeImpl.getLogger().warn("There was an attempt to apply a key binding that isn't registered: {}", keyBinding.getId());
                    continue;
                }
                finalKeyBindings.add((SpongeKeyBinding) keyBinding);
                if (!((SpongeKeyCategory) keyBinding.getCategory()).isDefault()) {
                    keyCategories.add((SpongeKeyCategory) keyBinding.getCategory());
                }
            }

            // Register the custom client bindings on the client
            if (!finalKeyBindings.isEmpty()) {
                SpongeMessageHandler.getChannel().sendTo(player, new MessageKeyboardData(keyCategories, finalKeyBindings));
            }

            // All the key bindings that can be used by the player, custom and defaults
            ImmutableList.Builder<KeyBinding> playerKeyBindings = ImmutableList.builder();
            playerKeyBindings.addAll(finalKeyBindings);
            playerKeyBindings.addAll(KeyBindingRegistryModule.getInstance().getAll().stream()
                    .filter(keyBinding -> ((SpongeKeyBinding) keyBinding).isDefault())
                    .collect(Collectors.toList()));
            ((IMixinEntityPlayerMP) player).setKeyBindings(playerKeyBindings.build());
        }
    }

    public static void handleKeyState(MessageKeyState message, RemoteConnection connection, Platform.Type side) {
        int internalId = message.getKeyBindingId();
        KeyBindingRegistryModule.getInstance().getByInternalId(internalId).ifPresent(keyBinding -> {
            Player player = ((PlayerConnection) connection).getPlayer();
            if (!player.getKeyBindings().contains(keyBinding)) {
                return;
            }
            IMixinEntityPlayerMP mixinPlayer = (IMixinEntityPlayerMP) player;
            boolean state = message.getState();
            InteractKeyEvent event;
            long startTime = mixinPlayer.getKeyPressStartTime(internalId);
            if (state) {
                if (startTime != -1) {
                    SpongeImpl.getLogger().warn("The player {} never send a release message before sending a new press message.");
                }
                mixinPlayer.setKeyPressStartTime(internalId, System.currentTimeMillis());
                event = SpongeEventFactory.createInteractKeyEventPress(Cause.source(player).build(), keyBinding, player);
            } else {
                if (startTime == -1) {
                    SpongeImpl.getLogger().warn("The player {} send a release message before sending a press message.");
                    return;
                }
                int pressedTime = (int) ((System.currentTimeMillis() - startTime) / 50);
                mixinPlayer.setKeyPressStartTime(internalId, -1);
                event = SpongeEventFactory.createInteractKeyEventRelease(Cause.source(player).build(), keyBinding, player, pressedTime);
            }
            Sponge.getEventManager().post(event);
        });
    }
}
