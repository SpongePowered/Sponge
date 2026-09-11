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

import com.google.gson.JsonPrimitive;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.Message;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.PrimitiveCodec;
import io.netty.util.AttributeKey;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.serializer.configurate4.ConfigurateComponentSerializer;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.BlockNBTComponent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentBuilder;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.EntityNBTComponent;
import net.kyori.adventure.text.KeybindComponent;
import net.kyori.adventure.text.NBTComponentBuilder;
import net.kyori.adventure.text.ObjectComponent;
import net.kyori.adventure.text.ScoreComponent;
import net.kyori.adventure.text.SelectorComponent;
import net.kyori.adventure.text.StorageNBTComponent;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.VirtualComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.DataComponentValue;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.flattener.ComponentFlattener;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.ShadowColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.object.PlayerHeadObjectContents;
import net.kyori.adventure.text.object.SpriteObjectContents;
import net.kyori.adventure.text.renderer.ComponentRenderer;
import net.kyori.adventure.text.renderer.TranslatableComponentRenderer;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.util.TriState;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.HoverEvent.Action;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.KeybindContents;
import net.minecraft.network.chat.contents.NbtContents;
import net.minecraft.network.chat.contents.ObjectContents;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.network.chat.contents.ScoreContents;
import net.minecraft.network.chat.contents.SelectorContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.network.chat.contents.data.BlockDataSource;
import net.minecraft.network.chat.contents.data.EntityDataSource;
import net.minecraft.network.chat.contents.data.StorageDataSource;
import net.minecraft.network.chat.contents.objects.AtlasSprite;
import net.minecraft.network.chat.contents.objects.ObjectInfo;
import net.minecraft.network.chat.contents.objects.PlayerSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.CompilableString;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.scores.TeamColor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.adventure.ResolveOperation;
import org.spongepowered.api.adventure.SpongeComponents;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.registry.DefaultedRegistryReference;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.accessor.network.chat.StyleAccessor;
import org.spongepowered.common.bridge.adventure.BossBarBridge;
import org.spongepowered.common.bridge.adventure.ComponentBridge;
import org.spongepowered.common.bridge.adventure.StyleBridge;
import org.spongepowered.common.bridge.world.BossEventBridge;
import org.spongepowered.common.launch.Launch;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;

public final class SpongeAdventure {

    public static final AttributeKey<Locale> CHANNEL_LOCALE = AttributeKey.newInstance("sponge:locale");

    public static final PrimitiveCodec<Component> STRING_CODEC = new PrimitiveCodec<Component>() {
        @Override
        public <T> DataResult<Component> read(final DynamicOps<T> ops, final T input) {
            return ops.getStringValue(input).map(GsonComponentSerializer.gson()::deserialize);
        }

        @Override
        public <T> T write(final DynamicOps<T> ops, final Component value) {
            return ops.createString(GsonComponentSerializer.gson().serialize(value));
        }

        @Override
        public String toString() {
            return "String[Component]";
        }
    };

    public static final ComponentRenderer<SpongeRendererContext> SPONGE_VIRTUAL_COMPONENT_RENDERER = new ComponentRenderer<SpongeRendererContext>() {

        @Override
        public @NotNull Component render(final @NotNull Component component, final @NotNull SpongeRendererContext context) {
            if (component instanceof final VirtualComponent virtualComponent) {
                return this.renderVirtual(virtualComponent, context);
            }
            return component;
        }

        public @NonNull Component renderVirtual(final @NonNull VirtualComponent component, final @NonNull SpongeRendererContext context) {
            context.markContainsVirtualComponents();
            if (component.renderer() instanceof final AudienceVirtualComponentRenderer audienceVirtualComponentRenderer
                && context.audience() != null) {
                return audienceVirtualComponentRenderer.apply(context.audience()).asComponent();
            }
            return component;
        }
    };

    public static final ComponentRenderer<SpongeRendererContext> SPONGE_COMPONENT_RENDERER = new TranslatableComponentRenderer<>() {

        @Override
        protected @NonNull Component renderVirtual(final @NonNull VirtualComponent component, final @NonNull SpongeRendererContext context) {
            return SpongeAdventure.SPONGE_VIRTUAL_COMPONENT_RENDERER.render(component, context);
        }

        @Override
        protected @Nullable MessageFormat translate(final @NonNull String key, final @NonNull SpongeRendererContext context) {
            return GlobalTranslator.translator().translate(key, context.locale());
        }

        @Override
        protected @NonNull Component renderTranslatable(final @NonNull TranslatableComponent component, final @NonNull SpongeRendererContext context) {
            final TriState anyTranslations = GlobalTranslator.translator().hasAnyTranslations();
            if (anyTranslations == TriState.TRUE || anyTranslations == TriState.NOT_SET) {
                final @Nullable Component translated = GlobalTranslator.translator().translate(component, context.locale());
                return translated != null
                    ? translated
                    : super.renderTranslatable(component, context);
            }
            return component;
        }
    };

    public static final ConfigurateComponentSerializer CONFIGURATE = ConfigurateComponentSerializer.builder()
        .scalarSerializer(GsonComponentSerializer.gson())
        .build();

    private static final Set<ServerBossEvent> ACTIVE_BOSS_BARS = ConcurrentHashMap.newKeySet();

    public static final ThreadLocal<Locale> ENCODING_LOCALE = new ThreadLocal<>();
    public static final ThreadLocal<ServerPlayer> ENCODING_PLAYER = new ThreadLocal<>();

    // --------------
    // ---- Core ----
    // --------------

    public static TriState asAdventure(final Tristate state) {
        if (state == Tristate.UNDEFINED) {
            return TriState.NOT_SET;
        } else if (state == Tristate.FALSE) {
            return TriState.FALSE;
        } else if (state == Tristate.TRUE) {
            return TriState.TRUE;
        }
        throw new IllegalArgumentException(state.name());
    }

    // -------------
    // ---- Key ----
    // -------------

    // org.spongepowered.common.mixin.core.adventure.KeyMixin
    public static Key asAdventure(final Identifier key) {
        return (Key) (Object) key;
    }

    public static Key asAdventure(final FontDescription description) {
        return switch (description) {
            case null -> null;
            case FontDescription.Resource fr -> Key.key(fr.id().toString());
            case FontDescription.AtlasSprite as -> Key.key(as.spriteId().toString());
            default -> throw new IllegalStateException("Unexpected value: " + description);
        };
    }

    // ------------------------
    // ---- ChatType.Bound ----
    // ------------------------

    public static ChatType.Bound asVanilla(final RegistryAccess access, final net.kyori.adventure.chat.ChatType.Bound adv) {
        if ((Object) adv instanceof ChatType.Bound vanilla) {
            return vanilla;
        }

        ChatType.Bound vanilla = ChatType.bind(
            ResourceKey.create(Registries.CHAT_TYPE, SpongeAdventure.asVanilla(adv.type().key())),
            access,
            SpongeAdventure.asVanilla(adv.name())
        );

        if (adv.target() != null) {
            vanilla = vanilla.withTargetName(SpongeAdventure.asVanilla(adv.target()));
        }

        return vanilla;
    }

    // -------------------
    // ---- Component ----
    // -------------------

    public static net.minecraft.network.chat.@Nullable Component asVanillaNullable(final @Nullable Component component) {
        if (component == null) {
            return null;
        }
        return SpongeAdventure.asVanilla(component);
    }

    public static net.minecraft.network.chat.Component asVanilla(final Component component) {
        return new AdventureTextComponent(component, SpongeAdventure.SPONGE_COMPONENT_RENDERER);
    }

    public static Optional<net.minecraft.network.chat.Component> asVanillaOpt(final @Nullable Component component) {
        return component == null ? Optional.empty() : Optional.of(((ComponentBridge) component).bridge$asVanillaComponent());
    }

    private static CompilableString<EntitySelector> parseSelector(final String pattern) {
        return EntitySelector.COMPILABLE_CODEC.parse(JsonOps.INSTANCE, new JsonPrimitive(pattern)).getOrThrow();
    }

    private static CompilableString<NbtPathArgument.NbtPath> parseNbtPath(final String path) {
        return NbtContents.NBT_PATH_CODEC.parse(JsonOps.INSTANCE, new JsonPrimitive(path)).getOrThrow();
    }

    private static CompilableString<Coordinates> parseBlockPos(final String pos) {
        return BlockDataSource.BLOCK_POS_CODEC.parse(JsonOps.INSTANCE, new JsonPrimitive(pos)).getOrThrow();
    }

    // no caching
    public static MutableComponent asVanillaMutable(final Component component) {
        final MutableComponent vanilla = SpongeAdventure.asVanillaMutable0(component);
        for (final Component child : component.children()) {
            vanilla.append(((ComponentBridge) child).bridge$asVanillaComponent());
        }
        vanilla.setStyle(((StyleBridge) component.style()).bridge$asVanilla());
        return vanilla;
    }

    @SuppressWarnings("deprecation")
    private static MutableComponent asVanillaMutable0(final Component component) {
        return switch (component) {
            case VirtualComponent virtual -> net.minecraft.network.chat.Component.literal(virtual.content());
            case TextComponent text -> net.minecraft.network.chat.Component.literal(text.content());
            case TranslatableComponent translatable -> {
                final List<net.minecraft.network.chat.Component> with = new ArrayList<>(translatable.arguments().size());
                for (final Component arg : ComponentLike.asComponents(translatable.arguments())) {
                    with.add(((ComponentBridge) arg).bridge$asVanillaComponent());
                }
                yield net.minecraft.network.chat.Component.translatable(translatable.key(), with.toArray(new Object[0]));
            }
            case KeybindComponent keybind -> net.minecraft.network.chat.Component.keybind(keybind.keybind());
            case ScoreComponent score -> net.minecraft.network.chat.Component.score(score.name(), score.objective());
            case SelectorComponent selector -> net.minecraft.network.chat.Component.selector(
                SpongeAdventure.parseSelector(selector.pattern()), SpongeAdventure.asVanillaOpt(selector.separator())
            );
            case BlockNBTComponent block -> net.minecraft.network.chat.Component.nbt(
                SpongeAdventure.parseNbtPath(block.nbtPath()), block.interpret(), false,
                SpongeAdventure.asVanillaOpt(block.separator()),
                new BlockDataSource(SpongeAdventure.parseBlockPos(block.pos().asString())));
            case EntityNBTComponent entity ->
                net.minecraft.network.chat.Component.nbt(
                    SpongeAdventure.parseNbtPath(entity.nbtPath()), entity.interpret(), false,
                    SpongeAdventure.asVanillaOpt(entity.separator()),
                    new EntityDataSource(SpongeAdventure.parseSelector(entity.selector())));
            case StorageNBTComponent storage ->
                net.minecraft.network.chat.Component.nbt(
                    SpongeAdventure.parseNbtPath(storage.nbtPath()), storage.interpret(), false,
                    SpongeAdventure.asVanillaOpt(storage.separator()),
                    new StorageDataSource(SpongeAdventure.asVanilla(storage.storage())));
            case ObjectComponent obj -> net.minecraft.network.chat.Component.object(SpongeAdventure.asVanilla(obj.contents()));
            default ->
                throw new UnsupportedOperationException("Cannot convert Component of type " + component.getClass());
        };
    }

    private static ObjectInfo asVanilla(final net.kyori.adventure.text.object.ObjectContents obj) {
        return switch (obj) {
            case SpriteObjectContents sprite -> new AtlasSprite(SpongeAdventure.asVanilla(sprite.atlas()), SpongeAdventure.asVanilla(sprite.sprite()));
            case PlayerHeadObjectContents player ->  new PlayerSprite(player.id() == null ? ResolvableProfile.createUnresolved(player.name()) : ResolvableProfile.createUnresolved(player.id()), player.hat());
            default -> throw new UnsupportedOperationException("Cannot convert ObjectContents of type " + obj.getClass());
        };
    }

    // no caching
    public static Component asAdventure(final net.minecraft.network.chat.Component component) {
        if (component instanceof final AdventureTextComponent ac) {
            return ac.wrapped();
        }

        final ComponentBuilder<?, ?> builder = SpongeAdventure.asAdventureBuilder(component.getContents());

        for (final net.minecraft.network.chat.Component child : component.getSiblings()) {
            builder.append(SpongeAdventure.asAdventure(child));
        }

        builder.style(((org.spongepowered.common.bridge.network.chat.StyleBridge) (Object) component.getStyle()).bridge$asAdventure());

        return builder.build();
    }

    @SuppressWarnings("deprecation")
    private static ComponentBuilder<?, ?> asAdventureBuilder(final ComponentContents contents) {
        return switch (contents) {
            case PlainTextContents plain -> {
                if (plain == PlainTextContents.EMPTY) {
                    yield Component.empty().toBuilder();
                }
                yield Component.text().content(plain.text());
            }
            case TranslatableContents tc -> {
                final List<Component> argList = Arrays.stream(tc.getArgs())
                    .map(arg -> arg instanceof final net.minecraft.network.chat.Component argComponent ?
                        SpongeAdventure.asAdventure(argComponent) : Component.text(arg.toString())).toList();
                yield Component.translatable().key(tc.getKey()).arguments(argList);
            }
            case KeybindContents kc -> Component.keybind().keybind(kc.getName());
            case ScoreContents sc ->
                Component.score().name(sc.name().mapLeft(CompilableString::source).orThrow()).objective(sc.objective());
            case SelectorContents sc ->
                Component.selector().pattern(sc.selector().source()).separator(SpongeAdventure.asAdventure(sc.separator()));
            case NbtContents nbt -> {
                final NBTComponentBuilder<?, ?> nbtBuilder = switch (nbt.dataSource()) {
                    case BlockDataSource bds ->
                        Component.blockNBT().pos(BlockNBTComponent.Pos.fromString(bds.coordinates().source()));
                    case EntityDataSource eds -> Component.entityNBT().selector(eds.selector().source());
                    case StorageDataSource sds -> Component.storageNBT().storage(SpongeAdventure.asAdventure(sds.id()));
                    default ->
                        throw new UnsupportedOperationException("Cannot convert NBTContents with DataSource " + nbt.dataSource().getClass());
                };
                yield nbtBuilder.nbtPath(nbt.nbtPath().source())
                    .interpret(nbt.interpreting())
                    .separator(SpongeAdventure.asAdventure(nbt.separator()));
            }
            case ObjectContents obj -> Component.object().contents(SpongeAdventure.asAdventure(obj.contents()));
            default ->
                throw new UnsupportedOperationException("Cannot convert ComponentContents of type " + contents.getClass());
        };
    }

    private static net.kyori.adventure.text.object.ObjectContents asAdventure(final ObjectInfo info) {
        switch (info) {
            case AtlasSprite atlas:
                return net.kyori.adventure.text.object.ObjectContents.sprite(SpongeAdventure.asAdventure(atlas.atlas()), SpongeAdventure.asAdventure(atlas.sprite()));
            case PlayerSprite player:
                final GameProfile profile = player.player().partialProfile();
                return net.kyori.adventure.text.object.ObjectContents.playerHead().name(profile.name()).id(profile.id()).hat(player.hat()).build();
            default:
                throw new UnsupportedOperationException("Cannot convert ObjectInfo of type " + info.getClass());
        }
    }

    public static @Nullable Component asAdventure(final Optional<net.minecraft.network.chat.Component> component) {
        return component.map(SpongeAdventure::asAdventure).orElse(null);
    }

    // no caching
    public static Style asAdventure(final net.minecraft.network.chat.Style mcStyle) {
        final Style.Builder builder = Style.style();
        final StyleAccessor $access = (StyleAccessor) (Object) mcStyle;

        builder.font(SpongeAdventure.asAdventure($access.accessor$font())); // font
        builder.color(SpongeAdventure.asAdventure(mcStyle.getColor())); // color
        // decorations
        builder.decoration(TextDecoration.OBFUSCATED, TextDecoration.State.byBoolean($access.accessor$obfuscated()));
        builder.decoration(TextDecoration.BOLD, TextDecoration.State.byBoolean($access.accessor$bold()));
        builder.decoration(TextDecoration.STRIKETHROUGH, TextDecoration.State.byBoolean($access.accessor$strikethrough()));
        builder.decoration(TextDecoration.UNDERLINED, TextDecoration.State.byBoolean($access.accessor$underlined()));
        builder.decoration(TextDecoration.ITALIC, TextDecoration.State.byBoolean($access.accessor$italic()));
        // events
        final net.minecraft.network.chat.HoverEvent hoverEvent = mcStyle.getHoverEvent();
        if (hoverEvent != null) {
            builder.hoverEvent(SpongeAdventure.asAdventure(hoverEvent));
        }
        final net.minecraft.network.chat.ClickEvent clickEvent = mcStyle.getClickEvent();
        if (clickEvent != null) {
            builder.clickEvent(SpongeAdventure.asAdventure(clickEvent));
        }
        builder.insertion(mcStyle.getInsertion()); // insertion
        return builder.build();
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
        return TextColor.color(color.getValue());
    }

    public static @Nullable TeamColor asVanillaTeamColor(final @Nullable ChatFormatting formatting) {
        if (formatting == null) {
            return null;
        }
        return switch (formatting) {
            case BLACK -> TeamColor.BLACK;
            case DARK_BLUE -> TeamColor.DARK_BLUE;
            case DARK_GREEN -> TeamColor.DARK_GREEN;
            case DARK_AQUA -> TeamColor.DARK_AQUA;
            case DARK_RED -> TeamColor.DARK_RED;
            case DARK_PURPLE -> TeamColor.DARK_PURPLE;
            case GOLD -> TeamColor.GOLD;
            case GRAY -> TeamColor.GRAY;
            case DARK_GRAY -> TeamColor.DARK_GRAY;
            case BLUE -> TeamColor.BLUE;
            case GREEN -> TeamColor.GREEN;
            case AQUA -> TeamColor.AQUA;
            case RED -> TeamColor.RED;
            case LIGHT_PURPLE -> TeamColor.LIGHT_PURPLE;
            case YELLOW -> TeamColor.YELLOW;
            case WHITE -> TeamColor.WHITE;
            default -> null;
        };
    }

    public static @Nullable TextColor asAdventure(final @Nullable ChatFormatting formatting) {
        return SpongeAdventure.asAdventure(SpongeAdventure.asVanillaTeamColor(formatting));
    }

    public static @Nullable TeamColor asVanillaTeamColor(final @Nullable NamedTextColor color) {
        if (color == null) {
            return null;
        }
        if (color == NamedTextColor.BLACK) {
            return TeamColor.BLACK;
        } else if (color == NamedTextColor.DARK_BLUE) {
            return TeamColor.DARK_BLUE;
        } else if (color == NamedTextColor.DARK_GREEN) {
            return TeamColor.DARK_GREEN;
        } else if (color == NamedTextColor.DARK_AQUA) {
            return TeamColor.DARK_AQUA;
        } else if (color == NamedTextColor.DARK_RED) {
            return TeamColor.DARK_RED;
        } else if (color == NamedTextColor.DARK_PURPLE) {
            return TeamColor.DARK_PURPLE;
        } else if (color == NamedTextColor.GOLD) {
            return TeamColor.GOLD;
        } else if (color == NamedTextColor.GRAY) {
            return TeamColor.GRAY;
        } else if (color == NamedTextColor.DARK_GRAY) {
            return TeamColor.DARK_GRAY;
        } else if (color == NamedTextColor.BLUE) {
            return TeamColor.BLUE;
        } else if (color == NamedTextColor.GREEN) {
            return TeamColor.GREEN;
        } else if (color == NamedTextColor.AQUA) {
            return TeamColor.AQUA;
        } else if (color == NamedTextColor.RED) {
            return TeamColor.RED;
        } else if (color == NamedTextColor.LIGHT_PURPLE) {
            return TeamColor.LIGHT_PURPLE;
        } else if (color == NamedTextColor.YELLOW) {
            return TeamColor.YELLOW;
        } else if (color == NamedTextColor.WHITE) {
            return TeamColor.WHITE;
        }
        return null;
    }

    public static @Nullable TextColor asAdventure(final @Nullable TeamColor color) {
        if (color == null) {
            return null;
        }
        return TextColor.color(color.rgb());
    }

    public static @Nullable NamedTextColor asAdventureNamed(final @Nullable TeamColor color) {
        if (color == null) {
            return null;
        }
        return switch (color) {
            case BLACK -> NamedTextColor.BLACK;
            case DARK_BLUE -> NamedTextColor.DARK_BLUE;
            case DARK_GREEN -> NamedTextColor.DARK_GREEN;
            case DARK_AQUA -> NamedTextColor.DARK_AQUA;
            case DARK_RED -> NamedTextColor.DARK_RED;
            case DARK_PURPLE -> NamedTextColor.DARK_PURPLE;
            case GOLD -> NamedTextColor.GOLD;
            case GRAY -> NamedTextColor.GRAY;
            case DARK_GRAY -> NamedTextColor.DARK_GRAY;
            case BLUE -> NamedTextColor.BLUE;
            case GREEN -> NamedTextColor.GREEN;
            case AQUA -> NamedTextColor.AQUA;
            case RED -> NamedTextColor.RED;
            case LIGHT_PURPLE -> NamedTextColor.LIGHT_PURPLE;
            case YELLOW -> NamedTextColor.YELLOW;
            case WHITE -> NamedTextColor.WHITE;
        };
    }

    public static @Nullable NamedTextColor asAdventureNamed(final @Nullable ChatFormatting color) {
        if (color == null) {
            return null;
        }
        return switch (color) {
            case BLACK -> NamedTextColor.BLACK;
            case DARK_BLUE -> NamedTextColor.DARK_BLUE;
            case DARK_GREEN -> NamedTextColor.DARK_GREEN;
            case DARK_AQUA -> NamedTextColor.DARK_AQUA;
            case DARK_RED -> NamedTextColor.DARK_RED;
            case DARK_PURPLE -> NamedTextColor.DARK_PURPLE;
            case GOLD -> NamedTextColor.GOLD;
            case GRAY -> NamedTextColor.GRAY;
            case DARK_GRAY -> NamedTextColor.DARK_GRAY;
            case BLUE -> NamedTextColor.BLUE;
            case GREEN -> NamedTextColor.GREEN;
            case AQUA -> NamedTextColor.AQUA;
            case RED -> NamedTextColor.RED;
            case LIGHT_PURPLE -> NamedTextColor.LIGHT_PURPLE;
            case YELLOW -> NamedTextColor.YELLOW;
            default -> NamedTextColor.WHITE; // White color is also just defaulting to white
        };
    }

    public static @Nullable Boolean asVanillaNullable(final TextDecoration.State state) {
        if (state == TextDecoration.State.TRUE) {
            return true;
        } else if (state == TextDecoration.State.FALSE) {
            return false;
        }
        return null;
    }

    @SuppressWarnings("ConstantConditions")
    public static HoverEvent<?> asAdventure(final net.minecraft.network.chat.HoverEvent event) {
        return switch (event) {
            case net.minecraft.network.chat.HoverEvent.ShowItem si:
                final var stack = si.item();
                final Registry<Item> itemRegistry = SpongeCommon.vanillaRegistry(Registries.ITEM);
                yield HoverEvent.showItem(
                    SpongeAdventure.asAdventure(stack.typeHolder().unwrap().map(ResourceKey::identifier, itemRegistry::getKey)),
                    stack.count(),
                    SpongeAdventure.asAdventure(stack.components())
                );
            case net.minecraft.network.chat.HoverEvent.ShowEntity se:
                final net.minecraft.network.chat.HoverEvent.EntityTooltipInfo value = se.entity();
                final Registry<EntityType<?>> entityTypeRegistry = SpongeCommon.vanillaRegistry(Registries.ENTITY_TYPE);
                yield HoverEvent.showEntity(
                    SpongeAdventure.asAdventure(entityTypeRegistry.getKey(value.type)),
                    value.uuid,
                    SpongeAdventure.asAdventure(value.name)
                );
            case net.minecraft.network.chat.HoverEvent.ShowText st:
                yield HoverEvent.showText(SpongeAdventure.asAdventure(st.value()));
            default:
                throw new IllegalStateException("Unexpected value: " + event);
        };
    }

    public static @Nullable ClickEvent asAdventure(final  net.minecraft.network.chat.@Nullable ClickEvent ce) {
        if (ce == null) {
            return null;
        }
        return switch (ce) {
            case net.minecraft.network.chat.ClickEvent.OpenUrl ou -> {
                try {
                    yield ClickEvent.openUrl(ou.uri().toURL());
                } catch (MalformedURLException e) {
                    throw new IllegalArgumentException("Invalid URL: " + ou.uri(), e);
                }
            }
            case net.minecraft.network.chat.ClickEvent.OpenFile of -> ClickEvent.openFile(of.file().getPath());
            case net.minecraft.network.chat.ClickEvent.RunCommand rc -> ClickEvent.runCommand(rc.command());
            case net.minecraft.network.chat.ClickEvent.SuggestCommand sc -> ClickEvent.suggestCommand(sc.command());
            case net.minecraft.network.chat.ClickEvent.ChangePage cp -> ClickEvent.changePage(cp.page());
            default -> throw new IllegalStateException("Unexpected value: " + ce);
        };
    }

    public static net.minecraft.network.chat.@Nullable ClickEvent asVanillaNullable(final @Nullable ClickEvent event) {
        if (event == null) {
            return null;
        }
       return switch (event.action()) {
           case OPEN_URL ->{
               URI page;
               try {
                   page = new URI(event.value());
               } catch (final URISyntaxException e) {
                   throw new IllegalArgumentException("Invalid url: " + event.value(), e);
               }
               yield new net.minecraft.network.chat.ClickEvent.OpenUrl(page);
           }
           case COPY_TO_CLIPBOARD -> new net.minecraft.network.chat.ClickEvent.CopyToClipboard(event.value());
           case RUN_COMMAND -> new net.minecraft.network.chat.ClickEvent.RunCommand(event.value());
           case SUGGEST_COMMAND -> new net.minecraft.network.chat.ClickEvent.SuggestCommand(event.value());
           case CHANGE_PAGE ->{
               int page;
               try {
                   page = Integer.parseInt(event.value());
               } catch (final NumberFormatException e) {
                   throw new IllegalArgumentException("Invalid page number: " + event.value(), e);
               }
               yield new net.minecraft.network.chat.ClickEvent.ChangePage(page);
           }
           default -> throw new IllegalArgumentException("Unknown click action: " + event.action());
       };
    }

    public static net.minecraft.network.chat.@Nullable HoverEvent asVanillaNullable(final @Nullable HoverEvent<?> event) {
        if (event == null) {
            return null;
        }
        final HoverEvent.Action<?> action = event.action();
        if (action == HoverEvent.Action.SHOW_TEXT) {
            return new net.minecraft.network.chat.HoverEvent.ShowText(SpongeAdventure.asVanilla((Component) event.value()));
        } else if (action == HoverEvent.Action.SHOW_ENTITY) {
            final HoverEvent.ShowEntity value = (HoverEvent.ShowEntity) event.value();
            final Registry<EntityType<?>> entityTypeRegistry = SpongeCommon.vanillaRegistry(Registries.ENTITY_TYPE);
            return new net.minecraft.network.chat.HoverEvent.ShowEntity(
                new net.minecraft.network.chat.HoverEvent.EntityTooltipInfo(
                    entityTypeRegistry.getValue(SpongeAdventure.asVanilla(value.type())),
                    value.id(),
                    SpongeAdventure.asVanillaNullable(value.name())
                )
            );
        } else if (action == HoverEvent.Action.SHOW_ITEM) {
            final HoverEvent.ShowItem value = (HoverEvent.ShowItem) event.value();
            final Registry<Item> itemRegistry = SpongeCommon.vanillaRegistry(Registries.ITEM);
            final var item = itemRegistry.wrapAsHolder(itemRegistry.getValue(SpongeAdventure.asVanilla(value.item())));
            return new net.minecraft.network.chat.HoverEvent.ShowItem(new ItemStackTemplate(item, value.count(), SpongeAdventure.asVanilla(value.dataComponents())));
        }
        throw new IllegalArgumentException(event.toString());
    }

    public static Action asVanilla(final HoverEvent.Action<?> action) {
        if (action == HoverEvent.Action.SHOW_TEXT) {
            return Action.SHOW_TEXT;
        } else if (action == HoverEvent.Action.SHOW_ITEM) {
            return Action.SHOW_ITEM;
        } else if (action == HoverEvent.Action.SHOW_ENTITY) {
            return Action.SHOW_ENTITY;
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
        final GsonComponentSerializer gcs = GsonComponentSerializer.gson();
        final List<Component> components = new ArrayList<>();
        for (final String string : strings) {
            components.add(gcs.deserialize(string));
        }
        return components;
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
        if (color == BossBar.Color.PINK) {
            return BossEvent.BossBarColor.PINK;
        } else if (color == BossBar.Color.BLUE) {
            return BossEvent.BossBarColor.BLUE;
        } else if (color == BossBar.Color.RED) {
            return BossEvent.BossBarColor.RED;
        } else if (color == BossBar.Color.GREEN) {
            return BossEvent.BossBarColor.GREEN;
        } else if (color == BossBar.Color.YELLOW) {
            return BossEvent.BossBarColor.YELLOW;
        } else if (color == BossBar.Color.PURPLE) {
            return BossEvent.BossBarColor.PURPLE;
        } else if (color == BossBar.Color.WHITE) {
            return BossEvent.BossBarColor.WHITE;
        }
        throw new IllegalArgumentException(color.name());
    }

    public static BossBar.Color asAdventure(final BossEvent.BossBarColor color) {
        if (color == BossEvent.BossBarColor.PINK) {
            return BossBar.Color.PINK;
        } else if (color == BossEvent.BossBarColor.BLUE) {
            return BossBar.Color.BLUE;
        } else if (color == BossEvent.BossBarColor.RED) {
            return BossBar.Color.RED;
        } else if (color == BossEvent.BossBarColor.GREEN) {
            return BossBar.Color.GREEN;
        } else if (color == BossEvent.BossBarColor.YELLOW) {
            return BossBar.Color.YELLOW;
        } else if (color == BossEvent.BossBarColor.PURPLE) {
            return BossBar.Color.PURPLE;
        } else if (color == BossEvent.BossBarColor.WHITE) {
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

    @SuppressWarnings({"rawtypes", "unchecked" })
    public static DataComponentPatch asVanilla(final Map<Key, DataComponentValue> componentMap) {
        if (componentMap == null) {
            return DataComponentPatch.EMPTY;
        }
        final DataComponentPatch.Builder builder = DataComponentPatch.builder();
        componentMap.forEach((key, value) -> BuiltInRegistries.DATA_COMPONENT_TYPE.getOptional(SpongeAdventure.asVanilla(key)).ifPresent(type -> {
            if (value instanceof SpongeDataComponentValue(Optional value1)) {
                builder.set((DataComponentType) type, value1.orElse(null));
            }
        }));
        return builder.build();
    }

    public static Map<Key, DataComponentValue> asAdventure(final DataComponentPatch components) {
        if (components == null) {
            return Map.of();
        }

        Map<Key, DataComponentValue> map = new HashMap<>();
        final DataComponentPatch.SplitResult split = components.split();
        split.added().forEach(component -> map.put(SpongeAdventure.asAdventure(BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(component.type())),
                new SpongeDataComponentValue<>(Optional.of(component.value()))));
        split.removed().forEach(type -> map.put(SpongeAdventure.asAdventure(BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(type)),
                new SpongeDataComponentValue<>(Optional.empty())));
        return map;
    }

    public static @Nullable Integer asVanillaNullable(@Nullable ShadowColor shadowColor) {
        if (shadowColor == null) {
            return null;
        }

        return shadowColor.value();
    }

    private record SpongeDataComponentValue<T>(Optional<T> value) implements DataComponentValue {

    }

    // Key

    public static Identifier asVanilla(final Key key) {
        if ((Object) key instanceof Identifier) {
            return (Identifier) (Object) key;
        }
        return Identifier.fromNamespaceAndPath(key.namespace(), key.value());
    }

    public static @Nullable Identifier asVanillaLocation(final @Nullable Key key) {
        if (key == null) {
            return null;
        }
        return Identifier.fromNamespaceAndPath(key.namespace(), key.value());
    }

    public static @Nullable FontDescription asVanillaFontDescription(final @Nullable Key key) {
        if (key == null) {
            return null;
        }
        return new FontDescription.Resource(SpongeAdventure.asVanilla(key));
    }

    // Sound

    public static Sound.Source asAdventure(final SoundSource source) {
        return switch (source) {
            case MASTER -> Sound.Source.MASTER;
            case MUSIC -> Sound.Source.MUSIC;
            case RECORDS -> Sound.Source.RECORD;
            case WEATHER -> Sound.Source.WEATHER;
            case BLOCKS -> Sound.Source.BLOCK;
            case HOSTILE -> Sound.Source.HOSTILE;
            case NEUTRAL -> Sound.Source.NEUTRAL;
            case PLAYERS -> Sound.Source.PLAYER;
            case AMBIENT -> Sound.Source.AMBIENT;
            case VOICE -> Sound.Source.VOICE;
            case UI -> Sound.Source.UI;
        };
    }

    public static SoundSource asVanilla(final Sound.Source source) {
        return switch (source) {
            case MASTER -> SoundSource.MASTER;
            case MUSIC -> SoundSource.MUSIC;
            case RECORD -> SoundSource.RECORDS;
            case WEATHER -> SoundSource.WEATHER;
            case BLOCK -> SoundSource.BLOCKS;
            case HOSTILE -> SoundSource.HOSTILE;
            case NEUTRAL -> SoundSource.NEUTRAL;
            case PLAYER -> SoundSource.PLAYERS;
            case AMBIENT -> SoundSource.AMBIENT;
            case VOICE -> SoundSource.VOICE;
            case UI -> SoundSource.UI;
        };
    }

    public static @Nullable SoundSource asVanillaNullable(final Sound.@Nullable Source source) {
        if (source == null) {
            return null;
        }
        return SpongeAdventure.asVanilla(source);
    }

    public static class Factory implements SpongeComponents.Factory {
        @Override
        public @NonNull ClickEvent callbackClickEvent(final @NonNull Consumer<CommandCause> callback) {
            Objects.requireNonNull(callback);
            final UUID key = CallbackCommand.INSTANCE.registerCallback(callback);
            return ClickEvent.runCommand(String.format("/%s:%s %s", Launch.instance().id(), CallbackCommand.NAME, key));
        }

        @Override
        @SafeVarargs
        public final @NonNull Component render(
            final @NonNull Component component,
            final @NonNull CommandCause senderContext,
            final @NonNull Audience viewer,
            final @NonNull DefaultedRegistryReference<ResolveOperation> firstOperation,
            final @NonNull DefaultedRegistryReference<ResolveOperation> @NonNull ... otherOperations
        ) {
            Component output = Objects.requireNonNull(component, "component");
            Objects.requireNonNull(senderContext, "senderContext");

            output = ((SpongeResolveOperation) Objects.requireNonNull(firstOperation, "firstOperation").get())
                .resolve(output, senderContext, viewer);

            for (final DefaultedRegistryReference<ResolveOperation> ref : otherOperations) {
                output = ((SpongeResolveOperation) ref.get()).resolve(output, senderContext, viewer);
            }
            return output;
        }

        @Override
        @SafeVarargs
        public final @NonNull Component render(
            final @NonNull Component component,
            final @NonNull CommandCause senderContext,
            final @NonNull DefaultedRegistryReference<ResolveOperation> firstOperation,
            final @NonNull DefaultedRegistryReference<ResolveOperation> @NonNull ... otherOperations
        ) {
            return this.render(component, senderContext, null, firstOperation, otherOperations);
        }

        @Override
        public ComponentFlattener flattener() {
            return ComponentFlattenerProvider.INSTANCE;
        }

        @Override
        public VirtualComponent receiverVirtualComponent(final @NonNull Function<Audience, ComponentLike> apply, final @NonNull String fallbackValue) {
            return Component.virtual(Audience.class, new AudienceVirtualComponentRenderer(apply, fallbackValue));
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
