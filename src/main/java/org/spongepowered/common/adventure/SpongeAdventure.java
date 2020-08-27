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
package org.spongepowered.common.adventure;

import com.google.common.collect.Lists;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.EnumSet;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.api.BinaryTagHolder;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainComponentSerializer;
import net.kyori.adventure.util.Codec;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.BossInfo;
import net.minecraft.world.server.ServerBossInfo;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.adventure.SpongeComponents;
import org.spongepowered.common.bridge.adventure.BossBarBridge;
import org.spongepowered.common.bridge.adventure.ComponentBridge;
import org.spongepowered.common.bridge.adventure.StyleBridge;
import org.spongepowered.common.bridge.util.text.TextComponentBridge;
import org.spongepowered.common.bridge.world.BossInfoBridge;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

public final class SpongeAdventure {
    public static final SpongeCallback CALLBACK_COMMAND = new SpongeCallback();
    public static final GsonComponentSerializer GSON = GsonComponentSerializer.builder()
        .legacyHoverEventSerializer(NbtLegacyHoverEventSerializer.INSTANCE)
        .downsampleColors() // TODO(1.16) remove
        .emitLegacyHoverEvent() // TODO(1.16) remove
        .build();
    public static final Codec<CompoundNBT, String, IOException, IOException> NBT_CODEC = new Codec<CompoundNBT, String, IOException, IOException>() {
        @Override
        public @NonNull CompoundNBT decode(final @NonNull String encoded) throws IOException {
            try {
                return JsonToNBT.getTagFromJson(encoded);
            } catch (final CommandSyntaxException e) {
                throw new IOException(e);
            }
        }

        @Override
        public @NonNull String encode(final @NonNull CompoundNBT decoded) {
            return decoded.toString();
        }
    };

    public static Component legacy(final char character, final String string) {
        return LegacyComponentSerializer.legacy(character).deserialize(string);
    }

    public static String legacy(final char character, final Component component) {
        return LegacyComponentSerializer.legacy(character).serialize(component);
    }

    public static String legacySection(final Component component) {
        return LegacyComponentSerializer.legacySection().serialize(component);
    }

    public static Component legacySection(final String string) {
        return LegacyComponentSerializer.legacySection().deserialize(string);
    }

    public static String legacyAmpersand(final Component component) {
        return LegacyComponentSerializer.legacyAmpersand().serialize(component);
    }

    public static Component legacyAmpersand(final String string) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(string);
    }

    public static Component json(final String string) {
        return GSON.deserialize(string);
    }

    public static String json(final Component string) {
        return GSON.serialize(string);
    }

    public static String plain(final Component component) {
        return PlainComponentSerializer.plain().serialize(component);
    }

    public static ITextComponent asVanilla(final Component component) {
        return ((ComponentBridge) component).bridge$asVanillaComponent();
    }

    public static Component asAdventure(final ITextComponent component) {
        return ((TextComponentBridge) component).bridge$asAdventureComponent();
    }

    public static net.minecraft.util.text.Style asVanilla(final Style style) {
        return ((StyleBridge) (Object) style).bridge$asVanilla();
    }

    public static TextFormatting asVanilla(final NamedTextColor color) {
        if (color == NamedTextColor.BLACK) {
            return TextFormatting.BLACK;
        } else if (color == NamedTextColor.DARK_BLUE) {
            return TextFormatting.DARK_BLUE;
        } else if (color == NamedTextColor.DARK_GREEN) {
            return TextFormatting.DARK_GREEN;
        } else if (color == NamedTextColor.DARK_AQUA) {
            return TextFormatting.DARK_AQUA;
        } else if (color == NamedTextColor.DARK_RED) {
            return TextFormatting.DARK_RED;
        } else if (color == NamedTextColor.DARK_PURPLE) {
            return TextFormatting.DARK_PURPLE;
        } else if (color == NamedTextColor.GOLD) {
            return TextFormatting.GOLD;
        } else if (color == NamedTextColor.GRAY) {
            return TextFormatting.GRAY;
        } else if (color == NamedTextColor.DARK_GRAY) {
            return TextFormatting.DARK_GRAY;
        } else if (color == NamedTextColor.BLUE) {
            return TextFormatting.BLUE;
        } else if (color == NamedTextColor.GREEN) {
            return TextFormatting.GREEN;
        } else if (color == NamedTextColor.AQUA) {
            return TextFormatting.AQUA;
        } else if (color == NamedTextColor.RED) {
            return TextFormatting.RED;
        } else if (color == NamedTextColor.LIGHT_PURPLE) {
            return TextFormatting.LIGHT_PURPLE;
        } else if (color == NamedTextColor.YELLOW) {
            return TextFormatting.YELLOW;
        } else if (color == NamedTextColor.WHITE) {
            return TextFormatting.WHITE;
        }
        throw new IllegalArgumentException();
    }

    public static @Nullable NamedTextColor asAdventureNamed(final @Nullable TextFormatting color) {
        if (color == null) {
            return null;
        }
        if (color == TextFormatting.BLACK) {
            return NamedTextColor.BLACK;
        } else if (color == TextFormatting.DARK_BLUE) {
            return NamedTextColor.DARK_BLUE;
        } else if (color == TextFormatting.DARK_GREEN) {
            return NamedTextColor.DARK_GREEN;
        } else if (color == TextFormatting.DARK_AQUA) {
            return NamedTextColor.DARK_AQUA;
        } else if (color == TextFormatting.DARK_RED) {
            return NamedTextColor.DARK_RED;
        } else if (color == TextFormatting.DARK_PURPLE) {
            return NamedTextColor.DARK_PURPLE;
        } else if (color == TextFormatting.GOLD) {
            return NamedTextColor.GOLD;
        } else if (color == TextFormatting.GRAY) {
            return NamedTextColor.GRAY;
        } else if (color == TextFormatting.DARK_GRAY) {
            return NamedTextColor.DARK_GRAY;
        } else if (color == TextFormatting.BLUE) {
            return NamedTextColor.BLUE;
        } else if (color == TextFormatting.GREEN) {
            return NamedTextColor.GREEN;
        } else if (color == TextFormatting.AQUA) {
            return NamedTextColor.AQUA;
        } else if (color == TextFormatting.RED) {
            return NamedTextColor.RED;
        } else if (color == TextFormatting.LIGHT_PURPLE) {
            return NamedTextColor.LIGHT_PURPLE;
        } else if (color == TextFormatting.YELLOW) {
            return NamedTextColor.YELLOW;
        } else if (color == TextFormatting.WHITE) {
            return NamedTextColor.WHITE;
        }
        throw new IllegalArgumentException();
    }

    public static Boolean asVanilla(final TextDecoration.State state) {
        if (state == TextDecoration.State.TRUE) {
            return true;
        } else if (state == TextDecoration.State.FALSE) {
            return false;
        }
        return null;
    }

    public static ChatType asVanilla(final MessageType type) {
        if (type == MessageType.SYSTEM) {
            return ChatType.SYSTEM;
        } else if (type == MessageType.CHAT) {
            return ChatType.CHAT;
        }
        throw new IllegalArgumentException(type.name());
    }

    public static HoverEvent.Action<?> asAdventure(final net.minecraft.util.text.event.HoverEvent.Action action) {
        if (action == net.minecraft.util.text.event.HoverEvent.Action.SHOW_TEXT) {
            return HoverEvent.Action.SHOW_TEXT;
        } else if (action == net.minecraft.util.text.event.HoverEvent.Action.SHOW_ITEM) {
            return HoverEvent.Action.SHOW_ITEM;
        } else if (action == net.minecraft.util.text.event.HoverEvent.Action.SHOW_ENTITY) {
            return HoverEvent.Action.SHOW_ENTITY;
        }
        throw new IllegalArgumentException(action.toString());
    }

    public static net.minecraft.util.text.event.HoverEvent.Action asVanilla(final HoverEvent.Action<?> action) {
        if (action == HoverEvent.Action.SHOW_TEXT) {
            return net.minecraft.util.text.event.HoverEvent.Action.SHOW_TEXT;
        } else if (action == HoverEvent.Action.SHOW_ITEM) {
            return net.minecraft.util.text.event.HoverEvent.Action.SHOW_ITEM;
        } else if (action == HoverEvent.Action.SHOW_ENTITY) {
            return net.minecraft.util.text.event.HoverEvent.Action.SHOW_ENTITY;
        }
        throw new IllegalArgumentException(action.toString());
    }

    public static ClickEvent.Action asAdventure(final net.minecraft.util.text.event.ClickEvent.Action action) {
        if (action == net.minecraft.util.text.event.ClickEvent.Action.OPEN_URL) {
            return ClickEvent.Action.OPEN_URL;
        } else if (action == net.minecraft.util.text.event.ClickEvent.Action.OPEN_FILE) {
            return ClickEvent.Action.OPEN_FILE;
        } else if (action == net.minecraft.util.text.event.ClickEvent.Action.RUN_COMMAND) {
            return ClickEvent.Action.RUN_COMMAND;
        } else if (action == net.minecraft.util.text.event.ClickEvent.Action.SUGGEST_COMMAND) {
            return ClickEvent.Action.SUGGEST_COMMAND;
        } else if (action == net.minecraft.util.text.event.ClickEvent.Action.CHANGE_PAGE) {
            return ClickEvent.Action.CHANGE_PAGE;
        }
        throw new IllegalArgumentException(action.toString());
    }

    public static net.minecraft.util.text.event.ClickEvent.Action asVanilla(final ClickEvent.Action action) {
        if (action == ClickEvent.Action.OPEN_URL) {
            return net.minecraft.util.text.event.ClickEvent.Action.OPEN_URL;
        } else if (action == ClickEvent.Action.OPEN_FILE) {
            return net.minecraft.util.text.event.ClickEvent.Action.OPEN_FILE;
        } else if (action == ClickEvent.Action.RUN_COMMAND) {
            return net.minecraft.util.text.event.ClickEvent.Action.RUN_COMMAND;
        } else if (action == ClickEvent.Action.SUGGEST_COMMAND) {
            return net.minecraft.util.text.event.ClickEvent.Action.SUGGEST_COMMAND;
        } else if (action == ClickEvent.Action.CHANGE_PAGE) {
            return net.minecraft.util.text.event.ClickEvent.Action.CHANGE_PAGE;
        } else if (action == ClickEvent.Action.COPY_TO_CLIPBOARD) {
            throw new UnsupportedOperationException("newer minecraft");
        }
        throw new IllegalArgumentException(action.toString());
    }

    // Horrible-ness

    public static List<Component> json(final List<String> strings) {
        final List<Component> components = Lists.newArrayList();
        for (final String string : strings) {
            components.add(json(string));
        }
        return components;
    }

    public static ListNBT listTagJson(final List<Component> components) {
        final ListNBT nbt = new ListNBT();
        for (final Component component : components) {
            nbt.add(StringNBT.valueOf(json(component)));
        }
        return nbt;
    }

    // -----------------
    // ---- BossBar ----
    // -----------------

    public static BossBar asAdventure(final BossInfo bar) {
        return ((BossInfoBridge) bar).bridge$asAdventure();
    }

    public static ServerBossInfo asVanillaServer(final BossBar bar) {
        return ((BossBarBridge) bar).bridge$asVanillaServerBar();
    }

    public static BossInfo.Color asVanilla(final BossBar.Color color) {
        if(color == BossBar.Color.PINK) {
            return BossInfo.Color.PINK;
        } else if(color == BossBar.Color.BLUE) {
            return BossInfo.Color.BLUE;
        } else if(color == BossBar.Color.RED) {
            return BossInfo.Color.RED;
        } else if(color == BossBar.Color.GREEN) {
            return BossInfo.Color.GREEN;
        } else if(color == BossBar.Color.YELLOW) {
            return BossInfo.Color.YELLOW;
        } else if(color == BossBar.Color.PURPLE) {
            return BossInfo.Color.PURPLE;
        } else if(color == BossBar.Color.WHITE) {
            return BossInfo.Color.WHITE;
        }
        throw new IllegalArgumentException(color.name());
    }

    public static BossBar.Color asAdventure(final BossInfo.Color color) {
        if(color == BossInfo.Color.PINK) {
            return BossBar.Color.PINK;
        } else if(color == BossInfo.Color.BLUE) {
            return BossBar.Color.BLUE;
        } else if(color == BossInfo.Color.RED) {
            return BossBar.Color.RED;
        } else if(color == BossInfo.Color.GREEN) {
            return BossBar.Color.GREEN;
        } else if(color == BossInfo.Color.YELLOW) {
            return BossBar.Color.YELLOW;
        } else if(color == BossInfo.Color.PURPLE) {
            return BossBar.Color.PURPLE;
        } else if(color == BossInfo.Color.WHITE) {
            return BossBar.Color.WHITE;
        }
        throw new IllegalArgumentException(color.name());
    }

    public static BossInfo.Overlay asVanilla(final BossBar.Overlay overlay) {
        if (overlay == BossBar.Overlay.PROGRESS) {
            return BossInfo.Overlay.PROGRESS;
        } else if (overlay == BossBar.Overlay.NOTCHED_6) {
            return BossInfo.Overlay.NOTCHED_6;
        } else if (overlay == BossBar.Overlay.NOTCHED_10) {
            return BossInfo.Overlay.NOTCHED_10;
        } else if (overlay == BossBar.Overlay.NOTCHED_12) {
            return BossInfo.Overlay.NOTCHED_12;
        } else if (overlay == BossBar.Overlay.NOTCHED_20) {
            return BossInfo.Overlay.NOTCHED_20;
        }
        throw new IllegalArgumentException(overlay.name());
    }

    public static BossBar.Overlay asAdventure(final BossInfo.Overlay overlay) {
        if (overlay == BossInfo.Overlay.PROGRESS) {
            return BossBar.Overlay.PROGRESS;
        } else if (overlay == BossInfo.Overlay.NOTCHED_6) {
            return BossBar.Overlay.NOTCHED_6;
        } else if (overlay == BossInfo.Overlay.NOTCHED_10) {
            return BossBar.Overlay.NOTCHED_10;
        } else if (overlay == BossInfo.Overlay.NOTCHED_12) {
            return BossBar.Overlay.NOTCHED_12;
        } else if (overlay == BossInfo.Overlay.NOTCHED_20) {
            return BossBar.Overlay.NOTCHED_20;
        }
        throw new IllegalArgumentException(overlay.name());
    }

    public static Set<BossBar.Flag> asAdventureFlags(final boolean darkenScreen, final boolean playBossMusic, final boolean createWorldFog) {
        final Set<BossBar.Flag> flags = EnumSet.noneOf(BossBar.Flag.class);
        if (darkenScreen) {
            flags.add(BossBar.Flag.DARKEN_SCREEN);
        }
        if (playBossMusic) {
            flags.add(BossBar.Flag.PLAY_BOSS_MUSIC);
        }
        if (createWorldFog) {
            flags.add(BossBar.Flag.CREATE_WORLD_FOG);
        }
        return flags;
    }

    // NBT

    public static @Nullable BinaryTagHolder asBinaryTagHolder(final @Nullable CompoundNBT tag) {
        if (tag == null) {
            return null;
        }
        try {
            return BinaryTagHolder.encode(tag, NBT_CODEC);
        } catch (final IOException e) {
            return null;
        }
    }

    // Key

    public static ResourceLocation asVanilla(final Key key) {
        if ((Object) key instanceof ResourceLocation) {
            return (ResourceLocation) (Object) key;
        }
        return new ResourceLocation(key.namespace(), key.value());
    }

    public static @Nullable ResourceLocation asVanillaNullable(final @Nullable Key key) {
        if (key == null) {
            return null;
        }
        return asVanilla(key);
    }

    // Sound

    public static Sound.Source asAdventure(final SoundCategory source) {
        switch (source) {
            case MASTER: return Sound.Source.MASTER;
            case MUSIC: return Sound.Source.MUSIC;
            case RECORDS: return Sound.Source.RECORD;
            case WEATHER: return Sound.Source.WEATHER;
            case BLOCKS: return Sound.Source.BLOCK;
            case HOSTILE: return Sound.Source.HOSTILE;
            case NEUTRAL: return Sound.Source.NEUTRAL;
            case PLAYERS: return Sound.Source.PLAYER;
            case AMBIENT: return Sound.Source.AMBIENT;
            case VOICE: return Sound.Source.VOICE;
        }
        throw new IllegalArgumentException(source.name());
    }

    public static SoundCategory asVanilla(final Sound.Source source) {
        switch (source) {
            case MASTER: return SoundCategory.MASTER;
            case MUSIC: return SoundCategory.MUSIC;
            case RECORD: return SoundCategory.RECORDS;
            case WEATHER: return SoundCategory.WEATHER;
            case BLOCK: return SoundCategory.BLOCKS;
            case HOSTILE: return SoundCategory.HOSTILE;
            case NEUTRAL: return SoundCategory.NEUTRAL;
            case PLAYER: return SoundCategory.PLAYERS;
            case AMBIENT: return SoundCategory.AMBIENT;
            case VOICE: return SoundCategory.VOICE;
        }
        throw new IllegalArgumentException(source.name());
    }

    public static @Nullable SoundCategory asVanillaNullable(final Sound.@Nullable Source source) {
        if (source == null) {
            return null;
        }
        return asVanilla(source);
    }

    public static class Factory implements SpongeComponents.Factory {
        @Override
        public @NonNull ClickEvent callbackClickEvent(final @NonNull Consumer<CommandCause> callback) {
            final UUID key = SpongeAdventure.CALLBACK_COMMAND.registerCallback(callback);
            return ClickEvent.runCommand("/sponge:callback " + key.toString());
        }
    }
}
