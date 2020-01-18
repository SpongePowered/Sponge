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

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.data.persistence.DataTranslators;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageReceiver;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.BlockChangeFlags;
import org.spongepowered.api.world.schematic.PaletteTypes;
import org.spongepowered.api.world.schematic.Schematic;
import org.spongepowered.api.world.volume.archetype.ArchetypeVolume;
import org.spongepowered.math.vector.Vector3i;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Map;
import java.util.UUID;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

@SuppressWarnings("deprecation")
@Plugin(id = "copypasta", name = "CopyPasta", description = CopyPasta.DESCRIPTION, version = "0.0.0")
public class CopyPasta implements LoadableModule {

    public static final String PLUGIN_ID = "copypasta";
    public static final String DESCRIPTION = "A simple plugin that allows creating, saving, and pasting schematics with SpongeAPI";

    @Inject private Logger logger;
    @Inject private PluginContainer container;
    @Inject @ConfigDir(sharedRoot = false) private File config;

    private final CopyPastaListener listener = new CopyPastaListener();
    private static final Map<UUID, PlayerData> player_data = Maps.newHashMap();
    private File schematicsDir;

    private static PlayerData get(Player pl) {
        PlayerData data = player_data.get(pl.getUniqueId());
        if (data == null) {
            data = new PlayerData(pl.getUniqueId());
            player_data.put(pl.getUniqueId(), data);
        }
        return data;
    }

    @Listener
    public void onGamePreInitialization(GamePreInitializationEvent event) {
        this.schematicsDir = new File(this.config, "schematics");
        this.schematicsDir.mkdirs();
        this.logger.info("Saving schematics to " + this.schematicsDir.getAbsolutePath());
        Sponge.getCommandManager().register(this.container, Command.builder()
            .setShortDescription(Text.of("Copies a region of the world to your clipboard"))
            .setPermission(PLUGIN_ID + ".command.copy")
            .setExecutor((ctx) -> {
                if (!(ctx.getSubject() instanceof Player)) {
                    ctx.getMessageReceiver().sendMessage(Text.of(TextColors.RED, "Player only."));
                    return CommandResult.success();
                }
                Player player = (Player) ctx.getSubject();
                PlayerData data = get(player);
                if (data.getPos1() == null || data.getPos2() == null) {
                    player.sendMessage(Text.of(TextColors.RED, "You must set both positions before copying"));
                    return CommandResult.success();
                }
                Vector3i min = data.getPos1().min(data.getPos2());
                Vector3i max = data.getPos1().max(data.getPos2());
                ArchetypeVolume volume = player.getWorld().createArchetypeVolume(min, max, player.getLocation().getPosition().toInt());
                data.setClipboard(volume);
                player.sendMessage(Text.of(TextColors.GREEN, "Saved to clipboard."));
                return CommandResult.success();
            })
            .build(), "copy");
        Sponge.getCommandManager().register(this, Command.builder()
            .setShortDescription(Text.of("Pastes your clipboard at your current position"))
            .setPermission(PLUGIN_ID + ".command.paste")
            .setExecutor((args) -> {
                if (!(args.getSubject() instanceof Player)) {
                    args.getMessageReceiver().sendMessage(Text.of(TextColors.RED, "Player only."));
                    return CommandResult.success();
                }
                Player player = (Player) args.getSubject();
                PlayerData data = get(player);
                ArchetypeVolume volume = data.getClipboard();
                if (volume == null) {
                    player.sendMessage(Text.of(TextColors.RED, "You must copy something before pasting"));
                    return CommandResult.success();
                }
                Sponge.getCauseStackManager().pushCause(this);
                volume.apply(player.getLocation(), BlockChangeFlags.ALL);
                Sponge.getCauseStackManager().popCause();
                player.sendMessage(Text.of(TextColors.GREEN, "Pasted clipboard into world."));
                return CommandResult.success();
            })
            .build(), "paste");

        Parameter.Value<String> paramFormat = Parameter.string().setKey("format").build();
        Parameter.Value<String> paramName = Parameter.string().setKey("name").build();

        Sponge.getCommandManager().register(this.container, Command.builder()
            .setShortDescription(Text.of("Saves your clipboard to disk"))
            .setPermission(PLUGIN_ID + ".command.save")
            .parameters(Parameter.seq(paramFormat, paramName))
            .setExecutor((ctx) -> {
                if (!(ctx.getSubject() instanceof Player)) {
                    ctx.getMessageReceiver().sendMessage(Text.of(TextColors.RED, "Player only."));
                    return CommandResult.success();
                }
                String format = ctx.getOne(paramFormat).get();
                String name = ctx.getOne(paramName).get();
                Player player = (Player) ctx.getSubject();
                PlayerData data = get(player);
                ArchetypeVolume volume = data.getClipboard();
                if (volume == null) {
                    player.sendMessage(Text.of(TextColors.RED, "You must copy something before saving"));
                    return CommandResult.success();
                }
                if (!"legacy".equalsIgnoreCase(format) && !"sponge".equalsIgnoreCase(format)) {
                    player.sendMessage(Text.of(TextColors.RED, "Unsupported schematic format, supported formats are [legacy, sponge]"));
                    return CommandResult.success();
                }
                Schematic schematic = Schematic.builder()
                    .volume(data.getClipboard())
                    .metaValue(Schematic.METADATA_AUTHOR, player.getName())
                    .metaValue(Schematic.METADATA_NAME, name)
                    .blockPaletteType(PaletteTypes.GLOBAL_BLOCKS.get())
                    .build();
                DataContainer schematicData = null;
                if ("legacy".equalsIgnoreCase(format)) {
                    schematicData = DataTranslators.LEGACY_SCHEMATIC.get().translate(schematic);
                } else if ("sponge".equalsIgnoreCase(format)) {
                    schematicData = DataTranslators.SCHEMATIC.get().translate(schematic);
                }
                File outputFile = new File(this.schematicsDir, name + ".schematic");
                try {
                    DataFormats.NBT.get().writeTo(new GZIPOutputStream(new FileOutputStream(outputFile)), schematicData);
                    player.sendMessage(Text.of(TextColors.GREEN, "Saved schematic to " + outputFile.getAbsolutePath()));
                } catch (Exception e) {
                    e.printStackTrace();
                    player.sendMessage(Text.of(TextColors.DARK_RED, "Error saving schematic: " + e.getMessage()));
                    return CommandResult.success();
                }
                return CommandResult.success();
            })
            .build(), "save");
        Sponge.getCommandManager().register(this.container, Command.builder()
            .setShortDescription(Text.of("Loads a schematic from disk to your clipboard"))
            .setPermission(PLUGIN_ID + ".command.load")
            .parameters(Parameter.seq(paramFormat, paramName))
            .setExecutor((ctx) -> {
                if (!(ctx.getSubject() instanceof Player)) {
                    ctx.getMessageReceiver().sendMessage(Text.of(TextColors.RED, "Player only."));
                    return CommandResult.success();
                }
                String format = ctx.getOne(paramFormat).get();
                String name = ctx.getOne(paramName).get();
                Player player = (Player) ctx.getSubject();
                PlayerData data = get(player);
                if (!"legacy".equalsIgnoreCase(format) && !"sponge".equalsIgnoreCase(format)) {
                    player.sendMessage(Text.of(TextColors.RED, "Unsupported schematic format, supported formats are [legacy, sponge]"));
                    return CommandResult.success();
                }
                File inputFile = new File(this.schematicsDir, name + ".schematic");
                if (!inputFile.exists()) {
                    player.sendMessage(Text.of(TextColors.RED, "Schematic at " + inputFile.getAbsolutePath() + " not found."));
                    return CommandResult.success();
                }
                DataContainer schematicData = null;
                try {
                    schematicData = DataFormats.NBT.get().readFrom(new GZIPInputStream(new FileInputStream(inputFile)));
                } catch (Exception e) {
                    e.printStackTrace();
                    player.sendMessage(Text.of(TextColors.DARK_RED, "Error loading schematic: " + e.getMessage()));
                    return CommandResult.success();
                }
                Schematic schematic = null;
                if ("legacy".equalsIgnoreCase(format)) {
                    schematic = DataTranslators.LEGACY_SCHEMATIC.get().translate(schematicData);
                } else if ("sponge".equalsIgnoreCase(format)) {
                    schematic = DataTranslators.SCHEMATIC.get().translate(schematicData);
                }
                player.sendMessage(Text.of(TextColors.GREEN, "Loaded schematic from " + inputFile.getAbsolutePath()));
                data.setClipboard(schematic);
                return CommandResult.success();
            })
            .build(), "load");
    }

    @Override
    public void enable(MessageReceiver src) {
        Sponge.getEventManager().registerListeners(this.container, this.listener);
    }

    public static class CopyPastaListener {

        @Listener
        public void onInteract(InteractBlockEvent.Secondary.MainHand event, @Root Player player) {
            ItemStack item = player.getItemInHand(HandTypes.MAIN_HAND.get());
            if (item.getType() == ItemTypes.WOODEN_AXE.get() && event.getBlock() != BlockSnapshot.empty()) {
                get(player).setPos2(event.getBlock().getPosition());
                player.sendMessage(Text.of(TextColors.LIGHT_PURPLE, "Position 2 set to " + event.getBlock().getPosition()));
                event.setCancelled(true);
            }
        }

        @Listener
        public void onInteract(InteractBlockEvent.Primary.MainHand event, @Root Player player) {
            ItemStack item = player.getItemInHand(HandTypes.MAIN_HAND.get());
            if (item.getType() == ItemTypes.WOODEN_AXE.get()) {
                get(player).setPos1(event.getBlock().getPosition());
                player.sendMessage(Text.of(TextColors.LIGHT_PURPLE, "Position 1 set to " + event.getBlock().getPosition()));
                event.setCancelled(true);
            }
        }
    }

    public static class PlayerData {

        private final UUID uid;
        private Vector3i pos1;
        private Vector3i pos2;
        private ArchetypeVolume clipboard;

        public PlayerData(UUID uid) {
            this.uid = uid;
        }

        public UUID getUid() {
            return this.uid;
        }

        public Vector3i getPos1() {
            return this.pos1;
        }

        public void setPos1(Vector3i pos) {
            this.pos1 = pos;
        }

        public Vector3i getPos2() {
            return this.pos2;
        }

        public void setPos2(Vector3i pos) {
            this.pos2 = pos;
        }

        public ArchetypeVolume getClipboard() {
            return this.clipboard;
        }

        public void setClipboard(ArchetypeVolume volume) {
            this.clipboard = volume;
        }
    }
}