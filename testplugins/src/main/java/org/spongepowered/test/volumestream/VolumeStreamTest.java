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
package org.spongepowered.test.volumestream;

import com.google.inject.Inject;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.cause.entity.SpawnTypes;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.world.BlockChangeFlags;
import org.spongepowered.api.world.volume.archetype.ArchetypeVolume;
import org.spongepowered.api.world.volume.stream.StreamOptions;
import org.spongepowered.api.world.volume.stream.VolumeApplicators;
import org.spongepowered.api.world.volume.stream.VolumeCollectors;
import org.spongepowered.api.world.volume.stream.VolumePositionTranslators;
import org.spongepowered.math.vector.Vector3i;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.jvm.Plugin;
import org.spongepowered.test.LoadableModule;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Plugin("volumestreamtest")
public final class VolumeStreamTest implements LoadableModule {

    @Inject private PluginContainer plugin;
    @Inject private Logger logger;

    private final CopyPastaListener listener = new CopyPastaListener();
    private static final Map<UUID, PlayerData> player_data = new HashMap<>();

    private static PlayerData get(final Player pl) {
        PlayerData data = VolumeStreamTest.player_data.get(pl.uniqueId());
        if (data == null) {
            data = new PlayerData(pl.uniqueId());
            VolumeStreamTest.player_data.put(pl.uniqueId(), data);
        }
        return data;
    }
    @Override
    public void enable(final CommandContext ctx) {
        Sponge.eventManager().registerListeners(this.plugin, this.listener);
    }

    @Listener
    private void onGamePreInitialization(final RegisterCommandEvent<Command.Parameterized> event) {
        event.register(
            this.plugin,
            Command.builder()
                .shortDescription(Component.text("Copies a region of the world to your clipboard"))
                .permission(this.plugin.metadata().id() + ".command.copy")
                .executor(src -> {
                    if (!(src.cause().root() instanceof Player)) {
                        src.sendMessage(Identity.nil(), Component.text("Player only.", NamedTextColor.RED));
                        return CommandResult.success();
                    }
                    final Player player = (Player) src.cause().root();
                    final PlayerData data = VolumeStreamTest.get(player);
                    if (data.getPos1() == null || data.getPos2() == null) {
                        player.sendMessage(Identity.nil(), Component.text("You must set both positions before copying", NamedTextColor.RED));
                        return CommandResult.success();
                    }
                    final Vector3i min = data.getPos1().min(data.getPos2());
                    final Vector3i max = data.getPos1().max(data.getPos2());
                    data.setOrigin(player.blockPosition());
                    final ArchetypeVolume archetypeVolume = player.world().createArchetypeVolume(min, max, player.blockPosition());
                    data.setClipboard(archetypeVolume);
                    player.sendMessage(Identity.nil(), Component.text("Saved to clipboard.", NamedTextColor.GREEN));
                    return CommandResult.success();
                }).build(),
            "copy"
        );
        event.register(this.plugin,
            Command.builder()
                .shortDescription(Component.text("Pastes your clipboard to where you are standing"))
                .permission(this.plugin.metadata().id() + ".command.paste")
                .executor(src -> {
                    if (!(src.cause().root()  instanceof ServerPlayer)) {
                        src.sendMessage(Identity.nil(), Component.text("Player only.", NamedTextColor.RED));
                        return CommandResult.success();
                    }
                    final ServerPlayer player = (ServerPlayer) src.cause().root();
                    final PlayerData data = VolumeStreamTest.get(player);
                    final ArchetypeVolume volume = data.getClipboard();
                    if (volume == null) {
                        player.sendMessage(Identity.nil(), Component.text("You must copy something before pasting", NamedTextColor.RED));
                        return CommandResult.success();
                    }
                    try (CauseStackManager.StackFrame frame = Sponge.server().causeStackManager().pushCauseFrame()) {
                        frame.pushCause(this.plugin);
                        volume.blockStateStream(volume.blockMin(), volume.blockMax(), StreamOptions.lazily())
                            .apply(VolumeCollectors.of(
                                player.world(),
                                VolumePositionTranslators.relativeTo(player.blockPosition()),
                                VolumeApplicators.applyBlocks(BlockChangeFlags.ALL)
                            ));
                        volume.biomeStream(volume.blockMin(), volume.blockMax(), StreamOptions.lazily())
                            .apply(VolumeCollectors.of(
                                player.world(),
                                VolumePositionTranslators.relativeTo(player.blockPosition()),
                                VolumeApplicators.applyBiomes()
                            ));
                        volume.blockEntityArchetypeStream(volume.blockMin(), volume.blockMax(), StreamOptions.lazily())
                            .apply(VolumeCollectors.of(
                                player.world(),
                                VolumePositionTranslators.relativeTo(player.blockPosition()),
                                VolumeApplicators.applyBlockEntityArchetype()
                            ));
                        frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.PLACEMENT.get());
                        volume.entityArchetypeStream(volume.blockMin(), volume.blockMax(), StreamOptions.lazily())
                            .apply(VolumeCollectors.of(
                                player.world(),
                                VolumePositionTranslators.relativeTo(player.blockPosition()),
                                VolumeApplicators.applyEntityArchetype()
                            ));
                    }
                    src.sendMessage(Identity.nil(), Component.text("Pasted clipboard into world.", NamedTextColor.GREEN));
                    return CommandResult.success();
                }).build(),
            "paste"
        );
    }


    static class CopyPastaListener {

        @Listener
        private void onInteract(final InteractBlockEvent.Secondary event, @Root final Player player) {
            final HandType handUsed = event.context().require(EventContextKeys.USED_HAND);
            event.context().get(EventContextKeys.USED_ITEM).ifPresent(snapshot -> {
                final BlockSnapshot block = event.block();
                if (snapshot.type().equals(ItemTypes.WOODEN_AXE.get()) && block != BlockSnapshot.empty()) {
                    if (HandTypes.MAIN_HAND.get().equals(handUsed)) {
                        VolumeStreamTest.get(player).setPos1(block.position());
                        player.sendMessage(Component.text("Position 1 set to " + block.position(), NamedTextColor.LIGHT_PURPLE));
                    } else if (HandTypes.OFF_HAND.get().equals(handUsed)) {
                        VolumeStreamTest.get(player).setPos2(block.position());
                        player.sendMessage(Component.text("Position 2 set to " + block.position(), NamedTextColor.LIGHT_PURPLE));
                    }
                    event.setCancelled(true);
                }
            });
        }
    }

    public static class PlayerData {

        private final UUID uid;
        private Vector3i pos1;
        private Vector3i pos2;
        private Vector3i origin;
        private ArchetypeVolume clipboard;

        public PlayerData(final UUID uid) {
            this.uid = uid;
        }

        public UUID getUid() {
            return this.uid;
        }

        public Vector3i getPos1() {
            return this.pos1;
        }

        public void setPos1(final Vector3i pos) {
            this.pos1 = pos;
        }

        public Vector3i getPos2() {
            return this.pos2;
        }

        public void setPos2(final Vector3i pos) {
            this.pos2 = pos;
        }

        public ArchetypeVolume getClipboard() {
            return this.clipboard;
        }

        public void setClipboard(final ArchetypeVolume volume) {
            this.clipboard = volume;
        }

        public Vector3i getOrigin() {
            return this.origin;
        }

        public void setOrigin(final Vector3i origin) {
            this.origin = origin;
        }
    }
}
