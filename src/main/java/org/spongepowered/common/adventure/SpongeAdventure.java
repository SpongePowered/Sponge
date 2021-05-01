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

import com.mojang.brigadier.Message;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.PrimitiveCodec;
import io.netty.util.AttributeKey;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.api.BinaryTagHolder;
import net.kyori.adventure.serializer.configurate4.ConfigurateComponentSerializer;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.KeybindComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.flattener.ComponentFlattener;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainComponentSerializer;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.translation.TranslationRegistry;
import net.kyori.adventure.translation.Translator;
import net.kyori.adventure.util.Codec;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.core.Registry;
import net.minecraft.locale.Language;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.ChatType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.adventure.ResolveOperation;
import org.spongepowered.api.adventure.SpongeComponents;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.registry.DefaultedRegistryReference;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.accessor.client.KeyMappingAccessor;
import org.spongepowered.common.accessor.network.chat.HoverEvent_ItemStackInfoAccessor;
import org.spongepowered.common.accessor.network.chat.TextColorAccessor;
import org.spongepowered.common.bridge.adventure.BossBarBridge;
import org.spongepowered.common.bridge.adventure.StyleBridge;
import org.spongepowered.common.bridge.network.chat.BaseComponentBridge;
import org.spongepowered.common.bridge.world.BossEventBridge;
import org.spongepowered.common.launch.Launch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SpongeAdventure {
    public static final AttributeKey<Locale> CHANNEL_LOCALE = AttributeKey.newInstance("sponge:locale");
    public static final GsonComponentSerializer GSON = GsonComponentSerializer.builder()
        .legacyHoverEventSerializer(NbtLegacyHoverEventSerializer.INSTANCE)
        .build();
    public static final Codec<CompoundTag, String, IOException, IOException> NBT_CODEC = new Codec<CompoundTag, String, IOException, IOException>() {
        @Override
        public @NonNull CompoundTag decode(final @NonNull String encoded) throws IOException {
            try {
                return TagParser.parseTag(encoded);
            } catch (final CommandSyntaxException e) {
                throw new IOException(e);
            }
        }

        @Override
        public @NonNull String encode(final @NonNull CompoundTag decoded) {
            return decoded.toString();
        }
    };

    public static final PrimitiveCodec<Component> STRING_CODEC = new PrimitiveCodec<Component>() {
        @Override
        public <T> DataResult<Component> read(final DynamicOps<T> ops, final T input) {
            return ops.getStringValue(input).map(SpongeAdventure.GSON::deserialize);
        }

        @Override
        public <T> T write(final DynamicOps<T> ops, final Component value) {
            return ops.createString(SpongeAdventure.GSON.serialize(value));
        }

        @Override
        public String toString() {
            return "String[Component]";
        }
    };

    public static final ConfigurateComponentSerializer CONFIGURATE = ConfigurateComponentSerializer.builder()
            .scalarSerializer(SpongeAdventure.GSON)
            .build();

    private static final Set<ServerBossEvent> ACTIVE_BOSS_BARS = ConcurrentHashMap.newKeySet();
    private static final Pattern LOCALIZATION_PATTERN = Pattern.compile("%(?:(\\d+)\\$)?s");
    public static final ComponentFlattener FLATTENER;
    public static final PlainComponentSerializer PLAIN;
    public static final LegacyComponentSerializer LEGACY_AMPERSAND;
    public static final LegacyComponentSerializer LEGACY_SECTION;

    static {
        final ComponentFlattener.Builder flattenerBuilder = ComponentFlattener.basic().toBuilder();
        if (!Launch.getInstance().isDedicatedServer()) {
            flattenerBuilder.mapper(KeybindComponent.class, SpongeAdventure::resolveKeybind);
        }

        flattenerBuilder.complexMapper(TranslatableComponent.class, (component, consumer) -> {
            final String key = component.key();
            for(final Translator registry : GlobalTranslator.get().sources()) {
                if(registry instanceof TranslationRegistry && ((TranslationRegistry) registry).contains(key)) {
                    consumer.accept(GlobalTranslator.render(component, Locale.getDefault()));
                    return;
                }
            }

            final /* @NonNull */ String translated = Language.getInstance().getOrDefault(key);
            final Matcher matcher = SpongeAdventure.LOCALIZATION_PATTERN.matcher(translated);
            final List<Component> args = component.args();
            int argPosition = 0;
            int lastIdx = 0;
            while (matcher.find()) {
                // append prior
                if (lastIdx < matcher.start()) {
                    consumer.accept(Component.text(translated.substring(lastIdx, matcher.start())));
                }
                lastIdx = matcher.end();

                final /* @Nullable */ String argIdx = matcher.group(1);
                // calculate argument position
                if (argIdx != null) {
                    try {
                        final int idx = Integer.parseInt(argIdx);
                        if (idx < args.size()) {
                            consumer.accept(args.get(idx));
                        }
                    } catch (final NumberFormatException ex) {
                        // ignore, drop the format placeholder
                    }
                } else {
                    final int idx = argPosition++;
                    if (idx < args.size()) {
                        consumer.accept(args.get(idx));
                    }
                }
            }

            // append tail
            if (lastIdx < translated.length()) {
                consumer.accept(Component.text(translated.substring(lastIdx)));
            }
        });

        FLATTENER = flattenerBuilder.build();
        PLAIN = PlainComponentSerializer.builder().flattener(SpongeAdventure.FLATTENER).build();
        LEGACY_AMPERSAND = LegacyComponentSerializer.builder()
            .character(LegacyComponentSerializer.AMPERSAND_CHAR)
            .flattener(SpongeAdventure.FLATTENER)
            .build();
        LEGACY_SECTION = LegacyComponentSerializer.builder()
            .character(LegacyComponentSerializer.SECTION_CHAR)
            .flattener(SpongeAdventure.FLATTENER)
            .build();
    }

    @OnlyIn(Dist.CLIENT)
    private static String resolveKeybind(final KeybindComponent component) {
        final KeyMapping mapping = KeyMappingAccessor.accessor$ALL().get(component.keybind());
        if (mapping != null) {
            return mapping.getTranslatedKeyMessage().getString();
        }
        return component.keybind();
    }

    // -------------
    // ---- Key ----
    // -------------

    // org.spongepowered.common.mixin.core.adventure.KeyMixin
    public static Key asAdventure(final ResourceLocation key) {
        return (Key) (Object) key;
    }

    // -------------------
    // ---- Component ----
    // -------------------

    public static Component json(final String string) {
        return SpongeAdventure.GSON.deserialize(string);
    }

    public static String json(final Component string) {
        return SpongeAdventure.GSON.serialize(string);
    }

    public static @NonNull LegacyComponentSerializer legacy(final char character) {
        if (character == LegacyComponentSerializer.SECTION_CHAR) {
            return SpongeAdventure.LEGACY_SECTION;
        } else if (character == LegacyComponentSerializer.AMPERSAND_CHAR) {
            return SpongeAdventure.LEGACY_AMPERSAND;
        }
        return LegacyComponentSerializer.builder()
            .character(character)
            .flattener(SpongeAdventure.FLATTENER)
            .build();
    }

    public static Component legacy(final char character, final String string) {
        return SpongeAdventure.legacy(character).deserialize(string);
    }

    public static String legacy(final char character, final Component component) {
        return SpongeAdventure.legacy(character).serialize(component);
    }

    public static String legacySection(final Component component) {
        return SpongeAdventure.LEGACY_SECTION.serialize(component);
    }

    public static Component legacySection(final String string) {
        return SpongeAdventure.LEGACY_SECTION.deserialize(string);
    }

    public static String legacyAmpersand(final Component component) {
        return SpongeAdventure.LEGACY_AMPERSAND.serialize(component);
    }

    public static Component legacyAmpersand(final String string) {
        return SpongeAdventure.LEGACY_AMPERSAND.deserialize(string);
    }

    public static String plain(final Component component) {
        return SpongeAdventure.PLAIN.serialize(component);
    }

    public static net.minecraft.network.chat.@Nullable Component asVanillaNullable(final @Nullable Component component) {
        if (component == null) {
            return null;
        }
        return SpongeAdventure.asVanilla(component);
    }

    public static net.minecraft.network.chat.Component asVanilla(final Component component) {
        return new AdventureTextComponent(component, GlobalTranslator.renderer());
    }

    public static Component asAdventure(final net.minecraft.network.chat.Component component) {
        return ((BaseComponentBridge) component).bridge$asAdventureComponent();
    }

    public static Component asAdventure(final Message message) {
        if (message instanceof net.minecraft.network.chat.Component) {
            return SpongeAdventure.asAdventure((net.minecraft.network.chat.Component) message);
        } else if (message instanceof Component) {
            return (Component) message;
        }
        return Component.text(message.getString());
    }

    public static net.minecraft.network.chat.Style asVanilla(final Style style) {
        return ((StyleBridge) (Object) style).bridge$asVanilla();
    }

    public static net.minecraft.network.chat.@Nullable TextColor asVanillaNullable(final @Nullable TextColor color) {
        if (color == null) {
            return null;
        }
        return net.minecraft.network.chat.TextColor.fromRgb(color.value());
    }

    public static ChatFormatting asVanilla(final NamedTextColor color) {
        if (color == NamedTextColor.BLACK) {
            return ChatFormatting.BLACK;
        } else if (color == NamedTextColor.DARK_BLUE) {
            return ChatFormatting.DARK_BLUE;
        } else if (color == NamedTextColor.DARK_GREEN) {
            return ChatFormatting.DARK_GREEN;
        } else if (color == NamedTextColor.DARK_AQUA) {
            return ChatFormatting.DARK_AQUA;
        } else if (color == NamedTextColor.DARK_RED) {
            return ChatFormatting.DARK_RED;
        } else if (color == NamedTextColor.DARK_PURPLE) {
            return ChatFormatting.DARK_PURPLE;
        } else if (color == NamedTextColor.GOLD) {
            return ChatFormatting.GOLD;
        } else if (color == NamedTextColor.GRAY) {
            return ChatFormatting.GRAY;
        } else if (color == NamedTextColor.DARK_GRAY) {
            return ChatFormatting.DARK_GRAY;
        } else if (color == NamedTextColor.BLUE) {
            return ChatFormatting.BLUE;
        } else if (color == NamedTextColor.GREEN) {
            return ChatFormatting.GREEN;
        } else if (color == NamedTextColor.AQUA) {
            return ChatFormatting.AQUA;
        } else if (color == NamedTextColor.RED) {
            return ChatFormatting.RED;
        } else if (color == NamedTextColor.LIGHT_PURPLE) {
            return ChatFormatting.LIGHT_PURPLE;
        } else if (color == NamedTextColor.YELLOW) {
            return ChatFormatting.YELLOW;
        } else if (color == NamedTextColor.WHITE) {
            return ChatFormatting.WHITE;
        }
        throw new IllegalArgumentException();
    }

    public static @Nullable TextColor asAdventure(final net.minecraft.network.chat.@Nullable TextColor color) {
        if (color == null) {
            return null;
        }
        return TextColor.color(((TextColorAccessor) (Object) color).accessor$value());
    }

    public static @Nullable TextColor asAdventure(final ChatFormatting formatting) {
        if (formatting == null) {
            return null;
        }
        final Integer color = formatting.getColor();
        if (color == null) {
            return null;
        }
        return TextColor.color(color);
    }

    public static @Nullable NamedTextColor asAdventureNamed(final @Nullable ChatFormatting color) {
        if (color == null) {
            return null;
        }
        if (color == ChatFormatting.BLACK) {
            return NamedTextColor.BLACK;
        } else if (color == ChatFormatting.DARK_BLUE) {
            return NamedTextColor.DARK_BLUE;
        } else if (color == ChatFormatting.DARK_GREEN) {
            return NamedTextColor.DARK_GREEN;
        } else if (color == ChatFormatting.DARK_AQUA) {
            return NamedTextColor.DARK_AQUA;
        } else if (color == ChatFormatting.DARK_RED) {
            return NamedTextColor.DARK_RED;
        } else if (color == ChatFormatting.DARK_PURPLE) {
            return NamedTextColor.DARK_PURPLE;
        } else if (color == ChatFormatting.GOLD) {
            return NamedTextColor.GOLD;
        } else if (color == ChatFormatting.GRAY) {
            return NamedTextColor.GRAY;
        } else if (color == ChatFormatting.DARK_GRAY) {
            return NamedTextColor.DARK_GRAY;
        } else if (color == ChatFormatting.BLUE) {
            return NamedTextColor.BLUE;
        } else if (color == ChatFormatting.GREEN) {
            return NamedTextColor.GREEN;
        } else if (color == ChatFormatting.AQUA) {
            return NamedTextColor.AQUA;
        } else if (color == ChatFormatting.RED) {
            return NamedTextColor.RED;
        } else if (color == ChatFormatting.LIGHT_PURPLE) {
            return NamedTextColor.LIGHT_PURPLE;
        } else if (color == ChatFormatting.YELLOW) {
            return NamedTextColor.YELLOW;
        }
        return NamedTextColor.WHITE;
    }

    public static @Nullable Boolean asVanillaNullable(final TextDecoration.State state) {
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

    @SuppressWarnings("ConstantConditions")
    public static HoverEvent<?> asAdventure(final net.minecraft.network.chat.HoverEvent event) {
        final net.minecraft.network.chat.HoverEvent.Action<?> action = event.getAction();
        if (action == net.minecraft.network.chat.HoverEvent.Action.SHOW_TEXT) {
            return HoverEvent.showText(SpongeAdventure.asAdventure(event.getValue(net.minecraft.network.chat.HoverEvent.Action.SHOW_TEXT)));
        } else if (action == net.minecraft.network.chat.HoverEvent.Action.SHOW_ENTITY) {
            final net.minecraft.network.chat.HoverEvent.EntityTooltipInfo value = event.getValue(
                net.minecraft.network.chat.HoverEvent.Action.SHOW_ENTITY);
            return HoverEvent.showEntity(
                SpongeAdventure.asAdventure(Registry.ENTITY_TYPE.getKey(value.type)),
                value.id,
                SpongeAdventure.asAdventure(value.name)
            );
        } else if (action == net.minecraft.network.chat.HoverEvent.Action.SHOW_ITEM) {
            final net.minecraft.network.chat.HoverEvent.ItemStackInfo value = event.getValue(
                net.minecraft.network.chat.HoverEvent.Action.SHOW_ITEM);
            return HoverEvent.showItem(
                SpongeAdventure.asAdventure(Registry.ITEM.getKey(((HoverEvent_ItemStackInfoAccessor) value).accessor$item())),
                ((HoverEvent_ItemStackInfoAccessor) value).accessor$count(),
                SpongeAdventure.asBinaryTagHolder(((HoverEvent_ItemStackInfoAccessor) value).accessor$tag())
            );
        }
        throw new IllegalArgumentException(event.toString());
    }

    public static net.minecraft.network.chat.@Nullable ClickEvent asVanillaNullable(final @Nullable ClickEvent event) {
        if (event == null) {
            return null;
        }
        return new net.minecraft.network.chat.ClickEvent(SpongeAdventure.asVanilla(event.action()), event.value());
    }

    public static net.minecraft.network.chat.@Nullable HoverEvent asVanillaNullable(final @Nullable HoverEvent<?> event) {
        if (event == null) {
            return null;
        }
        final HoverEvent.Action<?> action = event.action();
        if (action == HoverEvent.Action.SHOW_TEXT) {
            return new net.minecraft.network.chat.HoverEvent(
                net.minecraft.network.chat.HoverEvent.Action.SHOW_TEXT,
                SpongeAdventure.asVanilla((Component) event.value())
            );
        } else if (action == HoverEvent.Action.SHOW_ENTITY) {
            final HoverEvent.ShowEntity value = (HoverEvent.ShowEntity) event.value();
            return new net.minecraft.network.chat.HoverEvent(
                net.minecraft.network.chat.HoverEvent.Action.SHOW_ENTITY,
                new net.minecraft.network.chat.HoverEvent.EntityTooltipInfo(
                    Registry.ENTITY_TYPE.get(SpongeAdventure.asVanilla(value.type())),
                    value.id(),
                    SpongeAdventure.asVanillaNullable(value.name())
                )
            );
        } else if (action == HoverEvent.Action.SHOW_ITEM) {
            final HoverEvent.ShowItem value = (HoverEvent.ShowItem) event.value();
            return new net.minecraft.network.chat.HoverEvent(
                net.minecraft.network.chat.HoverEvent.Action.SHOW_ITEM,
                HoverEvent_ItemStackInfoAccessor.invoker$new(
                    Registry.ITEM.get(SpongeAdventure.asVanilla(value.item())),
                    value.count(),
                    SpongeAdventure.asVanillaCompound(value.nbt())
                )
            );
        }
        throw new IllegalArgumentException(event.toString());
    }

    public static net.minecraft.network.chat.HoverEvent.Action<?> asVanilla(final HoverEvent.Action<?> action) {
        if (action == HoverEvent.Action.SHOW_TEXT) {
            return net.minecraft.network.chat.HoverEvent.Action.SHOW_TEXT;
        } else if (action == HoverEvent.Action.SHOW_ITEM) {
            return net.minecraft.network.chat.HoverEvent.Action.SHOW_ITEM;
        } else if (action == HoverEvent.Action.SHOW_ENTITY) {
            return net.minecraft.network.chat.HoverEvent.Action.SHOW_ENTITY;
        }
        throw new IllegalArgumentException(action.toString());
    }

    public static ClickEvent.Action asAdventure(final net.minecraft.network.chat.ClickEvent.Action action) {
        if (action == net.minecraft.network.chat.ClickEvent.Action.OPEN_URL) {
            return ClickEvent.Action.OPEN_URL;
        } else if (action == net.minecraft.network.chat.ClickEvent.Action.OPEN_FILE) {
            return ClickEvent.Action.OPEN_FILE;
        } else if (action == net.minecraft.network.chat.ClickEvent.Action.RUN_COMMAND) {
            return ClickEvent.Action.RUN_COMMAND;
        } else if (action == net.minecraft.network.chat.ClickEvent.Action.SUGGEST_COMMAND) {
            return ClickEvent.Action.SUGGEST_COMMAND;
        } else if (action == net.minecraft.network.chat.ClickEvent.Action.CHANGE_PAGE) {
            return ClickEvent.Action.CHANGE_PAGE;
        }
        throw new IllegalArgumentException(action.toString());
    }

    public static net.minecraft.network.chat.ClickEvent.Action asVanilla(final ClickEvent.Action action) {
        if (action == ClickEvent.Action.OPEN_URL) {
            return net.minecraft.network.chat.ClickEvent.Action.OPEN_URL;
        } else if (action == ClickEvent.Action.OPEN_FILE) {
            return net.minecraft.network.chat.ClickEvent.Action.OPEN_FILE;
        } else if (action == ClickEvent.Action.RUN_COMMAND) {
            return net.minecraft.network.chat.ClickEvent.Action.RUN_COMMAND;
        } else if (action == ClickEvent.Action.SUGGEST_COMMAND) {
            return net.minecraft.network.chat.ClickEvent.Action.SUGGEST_COMMAND;
        } else if (action == ClickEvent.Action.CHANGE_PAGE) {
            return net.minecraft.network.chat.ClickEvent.Action.CHANGE_PAGE;
        } else if (action == ClickEvent.Action.COPY_TO_CLIPBOARD) {
            return net.minecraft.network.chat.ClickEvent.Action.COPY_TO_CLIPBOARD;
        }
        throw new IllegalArgumentException(action.toString());
    }

    // Horrible-ness

    public static List<Component> json(final List<String> strings) {
        final List<Component> components = new ArrayList<>();
        for (final String string : strings) {
            components.add(SpongeAdventure.json(string));
        }
        return components;
    }

    public static ListTag listTagJson(final List<Component> components) {
        final ListTag nbt = new ListTag();
        for (final Component component : components) {
            nbt.add(StringTag.valueOf(SpongeAdventure.json(component)));
        }
        return nbt;
    }

    // -----------------
    // ---- BossBar ----
    // -----------------

    public static BossBar asAdventure(final BossEvent bar) {
        return ((BossEventBridge) bar).bridge$asAdventure();
    }

    public static ServerBossEvent asVanillaServer(final BossBar bar) {
        return ((BossBarBridge) bar).bridge$asVanillaServerBar();
    }

    public static BossEvent.BossBarColor asVanilla(final BossBar.Color color) {
        if(color == BossBar.Color.PINK) {
            return BossEvent.BossBarColor.PINK;
        } else if(color == BossBar.Color.BLUE) {
            return BossEvent.BossBarColor.BLUE;
        } else if(color == BossBar.Color.RED) {
            return BossEvent.BossBarColor.RED;
        } else if(color == BossBar.Color.GREEN) {
            return BossEvent.BossBarColor.GREEN;
        } else if(color == BossBar.Color.YELLOW) {
            return BossEvent.BossBarColor.YELLOW;
        } else if(color == BossBar.Color.PURPLE) {
            return BossEvent.BossBarColor.PURPLE;
        } else if(color == BossBar.Color.WHITE) {
            return BossEvent.BossBarColor.WHITE;
        }
        throw new IllegalArgumentException(color.name());
    }

    public static BossBar.Color asAdventure(final BossEvent.BossBarColor color) {
        if(color == BossEvent.BossBarColor.PINK) {
            return BossBar.Color.PINK;
        } else if(color == BossEvent.BossBarColor.BLUE) {
            return BossBar.Color.BLUE;
        } else if(color == BossEvent.BossBarColor.RED) {
            return BossBar.Color.RED;
        } else if(color == BossEvent.BossBarColor.GREEN) {
            return BossBar.Color.GREEN;
        } else if(color == BossEvent.BossBarColor.YELLOW) {
            return BossBar.Color.YELLOW;
        } else if(color == BossEvent.BossBarColor.PURPLE) {
            return BossBar.Color.PURPLE;
        } else if(color == BossEvent.BossBarColor.WHITE) {
            return BossBar.Color.WHITE;
        }
        throw new IllegalArgumentException(color.name());
    }

    public static BossEvent.BossBarOverlay asVanilla(final BossBar.Overlay overlay) {
        if (overlay == BossBar.Overlay.PROGRESS) {
            return BossEvent.BossBarOverlay.PROGRESS;
        } else if (overlay == BossBar.Overlay.NOTCHED_6) {
            return BossEvent.BossBarOverlay.NOTCHED_6;
        } else if (overlay == BossBar.Overlay.NOTCHED_10) {
            return BossEvent.BossBarOverlay.NOTCHED_10;
        } else if (overlay == BossBar.Overlay.NOTCHED_12) {
            return BossEvent.BossBarOverlay.NOTCHED_12;
        } else if (overlay == BossBar.Overlay.NOTCHED_20) {
            return BossEvent.BossBarOverlay.NOTCHED_20;
        }
        throw new IllegalArgumentException(overlay.name());
    }

    public static BossBar.Overlay asAdventure(final BossEvent.BossBarOverlay overlay) {
        if (overlay == BossEvent.BossBarOverlay.PROGRESS) {
            return BossBar.Overlay.PROGRESS;
        } else if (overlay == BossEvent.BossBarOverlay.NOTCHED_6) {
            return BossBar.Overlay.NOTCHED_6;
        } else if (overlay == BossEvent.BossBarOverlay.NOTCHED_10) {
            return BossBar.Overlay.NOTCHED_10;
        } else if (overlay == BossEvent.BossBarOverlay.NOTCHED_12) {
            return BossBar.Overlay.NOTCHED_12;
        } else if (overlay == BossEvent.BossBarOverlay.NOTCHED_20) {
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

    public static @Nullable CompoundTag asVanillaCompound(final @Nullable BinaryTagHolder tag) {
        if (tag == null) {
            return null;
        }
        try {
            return tag.get(SpongeAdventure.NBT_CODEC);
        } catch (final IOException e) {
            return null;
        }
    }

    public static @Nullable BinaryTagHolder asBinaryTagHolder(final @Nullable CompoundTag tag) {
        if (tag == null) {
            return null;
        }
        try {
            return BinaryTagHolder.encode(tag, SpongeAdventure.NBT_CODEC);
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
        return SpongeAdventure.asVanilla(key);
    }

    // Sound

    public static Sound.Source asAdventure(final SoundSource source) {
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

    public static SoundSource asVanilla(final Sound.Source source) {
        switch (source) {
            case MASTER: return SoundSource.MASTER;
            case MUSIC: return SoundSource.MUSIC;
            case RECORD: return SoundSource.RECORDS;
            case WEATHER: return SoundSource.WEATHER;
            case BLOCK: return SoundSource.BLOCKS;
            case HOSTILE: return SoundSource.HOSTILE;
            case NEUTRAL: return SoundSource.NEUTRAL;
            case PLAYER: return SoundSource.PLAYERS;
            case AMBIENT: return SoundSource.AMBIENT;
            case VOICE: return SoundSource.VOICE;
        }
        throw new IllegalArgumentException(source.name());
    }

    public static @Nullable SoundSource asVanillaNullable(final Sound.@Nullable Source source) {
        if (source == null) {
            return null;
        }
        return SpongeAdventure.asVanilla(source);
    }

    public static Iterable<? extends Audience> unpackAudiences(Audience audience) {
        if (audience instanceof ForwardingAudience) {
            List<Audience> list = new ArrayList<>();
            for (Audience subAudience : ((ForwardingAudience) audience).audiences()) {
                SpongeAdventure.unpackAudiences(subAudience).forEach(list::add);
            }
            return list;
        }
        return Collections.singletonList(audience);
    }

    public static class Factory implements SpongeComponents.Factory {
        @Override
        public @NonNull ClickEvent callbackClickEvent(final @NonNull Consumer<CommandCause> callback) {
            Objects.requireNonNull(callback);
            final UUID key = CallbackCommand.INSTANCE.registerCallback(callback);
            return ClickEvent.runCommand(String.format("/%s:%s %s", SpongeCommon.ECOSYSTEM_ID, CallbackCommand.NAME, key));
        }

        @Override
        public @NonNull LegacyComponentSerializer legacySectionSerializer() {
            return SpongeAdventure.LEGACY_SECTION;
        }

        @Override
        public @NonNull LegacyComponentSerializer legacyAmpersandSerializer() {
            return SpongeAdventure.LEGACY_AMPERSAND;
        }

        @Override
        public @NonNull LegacyComponentSerializer legacySerializer(final char character) {
            return SpongeAdventure.legacy(character);
        }

        @Override
        public @NonNull GsonComponentSerializer gsonSerializer() {
            return SpongeAdventure.GSON;
        }

        @Override
        public @NonNull PlainComponentSerializer plainSerializer() {
            return SpongeAdventure.PLAIN;
        }

        @Override
        @SafeVarargs
        public final @NonNull Component render(
            final @NonNull Component component,
            final @NonNull CommandCause senderContext,
            @Nullable Audience viewer,
            final @NonNull DefaultedRegistryReference<ResolveOperation> firstOperation,
            final @NonNull DefaultedRegistryReference<ResolveOperation>@NonNull... otherOperations
        ) {
            Component output = Objects.requireNonNull(component, "component");
            Objects.requireNonNull(senderContext, "senderContext");

            // Unwrap the Audience to an entity
            while (viewer instanceof ForwardingAudience.Single && !(viewer instanceof Entity)) {
                viewer = ((ForwardingAudience.Single) viewer).audience();
            }
            final Entity backing;
            if (viewer instanceof Entity) {
                backing = (Entity) viewer;
            } else {
                backing = null;
            }

            output = ((SpongeResolveOperation) Objects.requireNonNull(firstOperation, "firstOperation").get())
                .resolve(output, senderContext, backing);

            for (final DefaultedRegistryReference<ResolveOperation> ref : otherOperations) {
                output = ((SpongeResolveOperation) ref.get()).resolve(output, senderContext, backing);
            }
            return output;
        }

        @Override
        @SafeVarargs
        public final @NonNull Component render(
            final @NonNull Component component,
            final @NonNull CommandCause senderContext,
            final @NonNull DefaultedRegistryReference<ResolveOperation> firstOperation,
            final @NonNull DefaultedRegistryReference<ResolveOperation>@NonNull... otherOperations
        ) {
            return this.render(component, senderContext, null, firstOperation, otherOperations);
        }
    }

    // Boss bar tracking
    // So we can update viewed bars for players when their locales change

    public static void registerBossBar(final ServerBossEvent mcBar) {
        SpongeAdventure.ACTIVE_BOSS_BARS.add(mcBar);
    }

    public static void unregisterBossBar(final ServerBossEvent mcBar) {
        SpongeAdventure.ACTIVE_BOSS_BARS.remove(mcBar);
    }

    public static void forEachBossBar(final Consumer<ServerBossEvent> info) {
        SpongeAdventure.ACTIVE_BOSS_BARS.forEach(info);
    }
}
