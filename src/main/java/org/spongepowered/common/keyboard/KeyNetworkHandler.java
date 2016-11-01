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
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
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
import org.spongepowered.api.keyboard.KeyContexts;
import org.spongepowered.api.network.PlayerConnection;
import org.spongepowered.api.network.RemoteConnection;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.interfaces.entity.player.IMixinEntityPlayerMP;
import org.spongepowered.common.network.message.MessageGuiState;
import org.spongepowered.common.network.message.MessageKeyState;
import org.spongepowered.common.network.message.MessageKeyboardData;
import org.spongepowered.common.network.message.SpongeMessageHandler;
import org.spongepowered.common.registry.type.keyboard.KeyBindingRegistryModule;

import java.util.BitSet;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class KeyNetworkHandler {

    @Listener
    public void onRegisterChannel(ChannelRegistrationEvent.Register event, @First PlayerConnection connection) {
        if (event.getChannel().equals(SpongeMessageHandler.CHANNEL_NAME)) {
            handleKeyRegistration(connection);
        }
    }

    public static void handleKeyRegistration(PlayerConnection connection) {
        final Player player = connection.getPlayer();
        // TODO: Why is the connection still null at this point, is this normal in sp (SpongeForge) ???
        // TODO: A way to avoid this would be a sendTo(PlayerConnection, Message) method
        boolean connectionNull = ((EntityPlayerMP) player).connection == null;
        if (connectionNull) {
            ((EntityPlayerMP) player).connection = (NetHandlerPlayServer) connection;
        }
        // Check if the key bindings were already initialized
        if (!player.getKeyBindings().isEmpty()) {
            return;
        }
        final Set<KeyBinding> keyBindings = new HashSet<>(KeyBindingRegistryModule.get().getAll());

        final RegisterKeyBindingsEvent event = SpongeEventFactory.createRegisterKeyBindingsEvent(
                Cause.source(player).build(), keyBindings, player);
        Sponge.getEventManager().post(event);

        if (!keyBindings.isEmpty()) {
            final Set<SpongeKeyBinding> finalKeyBindings = new HashSet<>();
            final Set<SpongeKeyCategory> keyCategories = new HashSet<>();

            for (KeyBinding keyBinding : keyBindings) {
                if (((SpongeKeyBinding) keyBinding).isDefault()) {
                    continue;
                }
                if (!KeyBindingRegistryModule.get().isRegistered(keyBinding)) {
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
                final Set<SpongeKeyContext> contexts = finalKeyBindings.stream().map(SpongeKeyBinding::getContext)
                        .collect(Collectors.toSet());
                contexts.add((SpongeKeyContext) KeyContexts.GUI);
                contexts.add((SpongeKeyContext) KeyContexts.UNIVERSAL);
                contexts.add((SpongeKeyContext) KeyContexts.IN_GAME);
                contexts.add((SpongeKeyContext) KeyContexts.INVENTORY);
                final Int2ObjectMap<BitSet> conflictContextsData = SpongeKeyContext.compileConflictContexts(contexts);
                SpongeMessageHandler.getChannel().sendTo(player, new MessageKeyboardData(keyCategories, finalKeyBindings,
                        conflictContextsData));
                if (connectionNull) {
                    ((EntityPlayerMP) player).connection = null;
                }
            }

            // All the key bindings that can be used by the player, custom and defaults
            final ImmutableList.Builder<KeyBinding> playerKeyBindings = ImmutableList.builder();
            playerKeyBindings.addAll(finalKeyBindings);
            playerKeyBindings.addAll(KeyBindingRegistryModule.get().getAll().stream()
                    .filter(keyBinding -> ((SpongeKeyBinding) keyBinding).isDefault())
                    .collect(Collectors.toList()));
            ((IMixinEntityPlayerMP) player).setKeyBindings(playerKeyBindings.build());
        }
    }

    public static void handleKeyState(MessageKeyState message, RemoteConnection connection, Platform.Type side) {
        int internalId = message.getKeyBindingId();
        KeyBindingRegistryModule.get().getByInternalId(internalId).ifPresent(keyBinding -> {
            Player player = ((PlayerConnection) connection).getPlayer();
            if (!player.getKeyBindings().contains(keyBinding)) {
                return;
            }
            IMixinEntityPlayerMP mixinPlayer = (IMixinEntityPlayerMP) player;
            boolean state = message.getState();
            long startTime = mixinPlayer.getKeyPressStartTime(internalId);
            if (state) {
                if (startTime != -1) {
                    SpongeImpl.getLogger().warn("The player {} never send a release message before sending a new press message.");
                }
                mixinPlayer.setKeyPressStartTime(internalId, System.currentTimeMillis());
                Cause cause = Cause.source(player).build();
                InteractKeyEvent.Press event = SpongeEventFactory.createInteractKeyEventPress(
                        cause, keyBinding, player);
                Sponge.getEventManager().post(event);
            } else {
                if (startTime == -1) {
                    SpongeImpl.getLogger().warn("The player {} send a release message before sending a press message.");
                    return;
                }
                int pressedTime = (int) ((System.currentTimeMillis() - startTime) / 50);
                mixinPlayer.setKeyPressStartTime(internalId, -1);

                Cause cause = Cause.source(player).build();
                InteractKeyEvent.Release event = SpongeEventFactory.createInteractKeyEventRelease(
                        cause, keyBinding, player, pressedTime);
                Sponge.getEventManager().post(event);
            }
        });
    }

    public static void handleGuiState(MessageGuiState message, RemoteConnection connection, Platform.Type side) {
        final IMixinEntityPlayerMP player = (IMixinEntityPlayerMP) ((PlayerConnection) connection).getPlayer();
        player.setGuiOpen(message.getState());
    }
}
