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

import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.data.DataContainer;
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
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.BlockChangeFlags;
import org.spongepowered.api.world.extent.ArchetypeVolume;
import org.spongepowered.api.world.schematic.BlockPaletteTypes;
import org.spongepowered.api.world.schematic.Schematic;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

@Plugin(id = "copypasta", description = "A simple plugin that allows creating, saving, and pasting schematics with SpongeAPI")
public class CopyPasta {

    public static final String PLUGIN_ID = "copypasta";

    @Inject private Logger logger;
    @Inject private PluginContainer plugin;
    @Inject @ConfigDir(sharedRoot = false) private File config;

    private final Map<UUID, PlayerData> player_data = Maps.newHashMap();
    private File schematicsDir;

    private PlayerData get(Player pl) {
        PlayerData data = this.player_data.get(pl.getUniqueId());
        if (data == null) {
            data = new PlayerData(pl.getUniqueId());
            this.player_data.put(pl.getUniqueId(), data);
        }
        return data;
    }

    @Listener
    public void onGamePreInitialization(GamePreInitializationEvent event) {
        this.schematicsDir = new File(this.config, "schematics");
        this.schematicsDir.mkdirs();
        this.logger.info("Saving schematics to " + this.schematicsDir.getAbsolutePath());
        Sponge.getCommandManager().register(this, Command.builder()
            .setShortDescription(Text.of("Copies a region of the world to your clipboard"))
            .setPermission(PLUGIN_ID + ".command.copy")
            .setTargetedExecutorErrorMessage(Text.of("Only players can run this command"))
            .targetedExecutor((cause, player, args) -> {
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
            }, Player.class)
            .build(), "copy");
        Sponge.getCommandManager().register(this, Command.builder()
            .setShortDescription(Text.of("Pastes your clipboard at your current position"))
            .setPermission(PLUGIN_ID + ".command.paste")
            .setTargetedExecutorErrorMessage(Text.of("Only players can run this command"))
            .targetedExecutor((cause, player, args) -> {
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
            }, Player.class)
            .build(), "paste");
        Sponge.getCommandManager().register(this, Command.builder()
            .setShortDescription(Text.of("Saves your clipboard to disk"))
            .setPermission(PLUGIN_ID + ".command.save")
            .parameter(Parameter.string().setKey("format").build())
            .parameter(Parameter.string().setKey("name").build())
            .setTargetedExecutorErrorMessage(Text.of("Only players can run this command"))
            .targetedExecutor((cause, player, args) -> {
                String format = args.getOneUnchecked("format").toString();
                String name = args.getOneUnchecked("name").toString();
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
                    .paletteType(BlockPaletteTypes.LOCAL)
                    .build();
                DataContainer schematicData = null;
                if ("legacy".equalsIgnoreCase(format)) {
                    schematicData = DataTranslators.LEGACY_SCHEMATIC.translate(schematic);
                } else if ("sponge".equalsIgnoreCase(format)) {
                    schematicData = DataTranslators.SCHEMATIC.translate(schematic);
                }
                File outputFile = new File(this.schematicsDir, name + ".schematic");
                try {
                    DataFormats.NBT.writeTo(new GZIPOutputStream(new FileOutputStream(outputFile)), schematicData);
                    player.sendMessage(Text.of(TextColors.GREEN, "Saved schematic to " + outputFile.getAbsolutePath()));
                } catch (Exception e) {
                    e.printStackTrace();
                    player.sendMessage(Text.of(TextColors.DARK_RED, "Error saving schematic: " + e.getMessage()));
                    return CommandResult.success();
                }
                return CommandResult.success();
            }, Player.class)
            .build(), "save");
        Sponge.getCommandManager().register(this, Command.builder()
            .setShortDescription(Text.of("Loads a schematic from disk to your clipboard"))
            .setPermission(PLUGIN_ID + ".command.load")
            .parameter(Parameter.string().setKey("format").build())
            .parameter(Parameter.string().setKey("name").build())
            .setTargetedExecutorErrorMessage(Text.of("Only players can run this command"))
            .targetedExecutor((cause, player, args) -> {
                String format = args.getOneUnchecked("format").toString();
                String name = args.getOneUnchecked("name").toString();
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
                    schematicData = DataFormats.NBT.readFrom(new GZIPInputStream(new FileInputStream(inputFile)));
                } catch (Exception e) {
                    e.printStackTrace();
                    player.sendMessage(Text.of(TextColors.DARK_RED, "Error loading schematic: " + e.getMessage()));
                    return CommandResult.success();
                }
                Schematic schematic = null;
                if ("legacy".equalsIgnoreCase(format)) {
                    schematic = DataTranslators.LEGACY_SCHEMATIC.translate(schematicData);
                } else if ("sponge".equalsIgnoreCase(format)) {
                    schematic = DataTranslators.SCHEMATIC.translate(schematicData);
                }
                player.sendMessage(Text.of(TextColors.GREEN, "Loaded schematic from " + inputFile.getAbsolutePath()));
                data.setClipboard(schematic);
                return CommandResult.success();
            }, Player.class)
            .build(), "load");
    }

    @Listener
    public void onInteract(InteractBlockEvent.Secondary.MainHand event, @Root Player player) {
        Optional<ItemStack> item = player.getItemInHand(HandTypes.MAIN_HAND);
        if (item.isPresent() && item.get().getType().equals(ItemTypes.WOODEN_AXE) && event.getTargetBlock() != BlockSnapshot.NONE) {
            get(player).setPos2(event.getTargetBlock().getPosition());
            player.sendMessage(Text.of(TextColors.LIGHT_PURPLE, "Position 2 set to " + event.getTargetBlock().getPosition()));
            event.setCancelled(true);
        }
    }

    @Listener
    public void onInteract(InteractBlockEvent.Primary.MainHand event, @Root Player player) {
        Optional<ItemStack> item = player.getItemInHand(HandTypes.MAIN_HAND);
        if (item.isPresent() && item.get().getType().equals(ItemTypes.WOODEN_AXE)) {
            get(player).setPos1(event.getTargetBlock().getPosition());
            player.sendMessage(Text.of(TextColors.LIGHT_PURPLE, "Position 1 set to " + event.getTargetBlock().getPosition()));
            event.setCancelled(true);
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