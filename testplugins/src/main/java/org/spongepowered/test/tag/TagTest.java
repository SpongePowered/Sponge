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
package org.spongepowered.test.tag;

import com.google.inject.Inject;
import io.leangen.geantyref.TypeToken;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.command.parameter.managed.ValueParameter;
import org.spongepowered.api.command.parameter.managed.standard.VariableValueParameters;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.event.lifecycle.RegisterTagEvent;
import org.spongepowered.api.fluid.FluidType;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.registry.DefaultedRegistryType;
import org.spongepowered.api.registry.RegistryKey;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.tag.BlockTypeTags;
import org.spongepowered.api.tag.Tag;
import org.spongepowered.api.tag.Taggable;
import org.spongepowered.api.util.blockray.RayTrace;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

@Plugin("tagtest") //TODO FIX THIS
public final class TagTest {

    private final PluginContainer pluginContainer;
    private final Logger logger;

    @Inject
    public TagTest(final PluginContainer pluginContainer, final Logger logger) {
        this.pluginContainer = pluginContainer;
        this.logger = logger;
    }

    private static final TypeToken<Tag<BlockType>> BLOCK_TYPE_TAG_TOKEN = new TypeToken<Tag<BlockType>>() {};
    private static final TypeToken<Tag<EntityType<@NonNull ?>>> ENTITY_TYPE_TAG_TOKEN = new TypeToken<Tag<EntityType<@NonNull ?>>>() {};
    private static final TypeToken<Tag<ItemType>> ITEM_TYPE_TAG_TOKEN = new TypeToken<Tag<ItemType>>() {};
    private static final TypeToken<Tag<FluidType>> FLUID_TYPE_TAG_TOKEN = new TypeToken<Tag<FluidType>>() {};

    @Listener
    private void registerTags(final RegisterTagEvent event) {
        this.logger.info("Adding tags.");

        event.tag(Tag.of(RegistryTypes.BLOCK_TYPE, ResourceKey.of(this.pluginContainer, "wool")))
            .append(BlockTypes.SHORT_GRASS);

        event.tag(BlockTypeTags.WOOL).append(BlockTypes.OAK_LOG);

        event.tag(Tag.of(RegistryTypes.BLOCK_TYPE, ResourceKey.minecraft("wool")))
            .append(BlockTypes.GRASS_BLOCK);

        event.tag(BlockTypeTags.UNDERWATER_BONEMEALS).append(BlockTypes.DIAMOND_BLOCK);

        final Tag<BlockType> ores = Tag.of(RegistryTypes.BLOCK_TYPE, ResourceKey.of(this.pluginContainer, "ores"));

        event.tag(ores)
            .append(BlockTypes.COAL_ORE)
            .append(BlockTypes.IRON_ORE)
            .append(BlockTypes.LAPIS_ORE)
            .append(BlockTypes.REDSTONE_ORE)
            .append(BlockTypes.EMERALD_ORE)
            .append(BlockTypes.DIAMOND_ORE)
            .append(BlockTypes.NETHER_QUARTZ_ORE)
            .append(BlockTypeTags.GOLD_ORES) // Test gold ore child.
        ;

        event.tag(Tag.of(RegistryTypes.BLOCK_TYPE, ResourceKey.of(this.pluginContainer, "oresandblocks")))
                .append(BlockTypes.COAL_BLOCK)
                .append(BlockTypes.IRON_BLOCK)
                .append(BlockTypes.LAPIS_BLOCK)
                .append(BlockTypes.REDSTONE_BLOCK)
                .append(BlockTypes.GOLD_BLOCK)
                .append(BlockTypes.EMERALD_BLOCK)
                .append(BlockTypes.DIAMOND_BLOCK)
                .append(BlockTypes.QUARTZ_BLOCK)
                .append(ores) // Test child TagTemplate
        ;

        final ResourceKey nonExistentKey = ResourceKey.of("notrealnamespace", "notrealvalue");

        event.tag(Tag.of(RegistryTypes.ITEM_TYPE, ResourceKey.of(this.pluginContainer, "brokenchildtag")))
            .append(Tag.of(RegistryTypes.ITEM_TYPE, nonExistentKey));

        event.tag(Tag.of(RegistryTypes.ITEM_TYPE, ResourceKey.of(this.pluginContainer, "brokenvaluetag")))
            .append(RegistryKey.of(RegistryTypes.ITEM_TYPE, nonExistentKey));

        event.tag(Tag.of(RegistryTypes.ITEM_TYPE, ResourceKey.of(this.pluginContainer, "stillworkingtag")))
            .append(RegistryKey.of(RegistryTypes.ITEM_TYPE, nonExistentKey))
            .append(Tag.of(RegistryTypes.ITEM_TYPE, nonExistentKey))
            .append(ItemTypes.REDSTONE);
    }

    @Listener
    private void registerCommands(final RegisterCommandEvent<Command.Parameterized> event) {
        // TODO fix me this.registerHasTagCommand(event);

        final Command.Parameterized blockTags = Command.builder()
                .executor(ctx -> {
                    final BlockType blockType = TagTest.raytraceBlock(ctx).blockState().type();
                    final Audience audience = ctx.cause().audience();
                    TagTest.sendTags(audience, blockType);
                    return CommandResult.success();
                })
                .build();

        final Command.Parameterized itemTags = Command.builder()
                .executor(ctx -> {
                    final ItemType itemType = TagTest.requireItemInHand(ctx);
                    final Audience audience = ctx.cause().audience();
                    TagTest.sendTags(audience, itemType);
                    return CommandResult.success();
                })
                .build();

        final Command.Parameterized entityTags = Command.builder()
                .executor(ctx -> {
                    final EntityType<?> entityType = TagTest.raytraceEntity(ctx);
                    final Audience audience = ctx.cause().audience();
                    TagTest.sendTags(audience, entityType);
                    return CommandResult.success();
                })
                .build();

        final Command.Parameterized fluidTags = Command.builder()
                .executor(ctx -> {
                    final FluidType fluidType = TagTest.raytraceBlock(ctx).blockState().fluidState().type();
                    final Audience audience = ctx.cause().audience();
                    TagTest.sendTags(audience, fluidType);
                    return CommandResult.success();
                })
                .build();

        final Command.Parameterized getTags = Command.builder()
                .addChild(blockTags, "block")
                .addChild(itemTags, "item")
                .addChild(entityTags, "entity")
                .addChild(fluidTags, "fluid")
                .build();

        event.register(this.pluginContainer, getTags, "gettags");
    }

    private void registerHasTagCommand(RegisterCommandEvent<Command.Parameterized> event) {
        final Parameter.Value<Tag<BlockType>> blockTypeTagParameter = TagTest.makeTagRegistryParameter(TagTest.BLOCK_TYPE_TAG_TOKEN, RegistryTypes.BLOCK_TYPE, "blocktag");
        final Command.Parameterized blockHasTag = Command.builder()
                .addParameter(blockTypeTagParameter)
                .executor(ctx -> {
                    final Tag<BlockType> tag = ctx.requireOne(blockTypeTagParameter);

                    final BlockType blockType = TagTest.raytraceBlock(ctx).blockState().type();
                    //TagTest.sendTagMessage(blockType, RegistryTypes.BLOCK_TYPE, tag, tag.key(RegistryTypes.BLOCK_TYPE_TAGS), ctx.cause().audience());
                    return CommandResult.success();
                })
                .build();

        final Parameter.Value<Tag<ItemType>> itemTypeTagParameter = TagTest.makeTagRegistryParameter(TagTest.ITEM_TYPE_TAG_TOKEN, RegistryTypes.ITEM_TYPE, "itemtag");
        final Command.Parameterized itemHasTag = Command.builder()
                .addParameter(itemTypeTagParameter)
                .executor(ctx -> {
                    final Tag<ItemType> tag = ctx.requireOne(itemTypeTagParameter);

                    final ItemType itemType = TagTest.requireItemInHand(ctx);
//                    TagTest.sendTagMessage(itemType, RegistryTypes.ITEM_TYPE, tag, tag.key(RegistryTypes.ITEM_TYPE_TAGS), ctx.cause().audience());
                    return CommandResult.success();
                })
                .build();

        final Parameter.Value<Tag<EntityType<?>>> entityTypeTagParameter = TagTest.makeTagRegistryParameter(TagTest.ENTITY_TYPE_TAG_TOKEN, RegistryTypes.ENTITY_TYPE, "entitytag");
        final Command.Parameterized entityHasTag = Command.builder()
                .addParameter(entityTypeTagParameter)
                .executor(ctx -> {
                    final Tag<EntityType<?>> tag = ctx.requireOne(entityTypeTagParameter);

                    final EntityType<?> type = TagTest.raytraceEntity(ctx);

                    //TagTest.sendTagMessage(type, RegistryTypes.ENTITY_TYPE, tag, tag.key(RegistryTypes.ENTITY_TYPE_TAGS), ctx.cause().audience());
                    return CommandResult.success();
                })
                .build();

        final Parameter.Value<Tag<FluidType>> fluidTypeTagParameter = TagTest.makeTagRegistryParameter(TagTest.FLUID_TYPE_TAG_TOKEN, RegistryTypes.FLUID_TYPE, "fluidtag");
        final Command.Parameterized fluidHasTag = Command.builder()
                .addParameter(fluidTypeTagParameter)
                .executor(ctx -> {
                    final Tag<FluidType> tag = ctx.requireOne(fluidTypeTagParameter);

                    final FluidType fluidType = TagTest.raytraceBlock(ctx).serverLocation().fluid().type();

                    //TagTest.sendTagMessage(fluidType, RegistryTypes.FLUID_TYPE, tag, tag.key(RegistryTypes.FLUID_TYPE_TAGS), ctx.cause().audience());

                    return CommandResult.success();
                })
                .build();

        final Command.Parameterized hasTag = Command.builder()
                .addChild(blockHasTag, "block")
                .addChild(itemHasTag, "item")
                .addChild(entityHasTag, "entity")
                .addChild(fluidHasTag, "fluid")
                .build();

        event.register(this.pluginContainer, hasTag, "hastag");
    }

        private static <T> Parameter.Value<Tag<T>> makeTagRegistryParameter(final TypeToken<Tag<T>> token, final DefaultedRegistryType<T> registryType, final String key) {
        final Map<String, Tag<T>> tags = registryType.get().tags().collect(Collectors.toMap(t -> t.key().toString(), t -> t));
        // TODO VariableValueParameters.registryTagBuilder
        // TODO tags are not available when calling this?
        final ValueParameter<Tag<T>> valueParameter = (ValueParameter<Tag<T>>) (Object) VariableValueParameters.staticChoicesBuilder(Tag.class).addChoices(tags).build();
        return Parameter.builder(token, valueParameter).key(key).build();
    }

    private static ServerPlayer requirePlayerRayTrace(final CommandContext ctx) throws CommandException {
        return ctx.cause().first(ServerPlayer.class)
                .orElseThrow(() -> new CommandException(Component.text("You must be a player to use this command! (Ray trace)")));
    }

    private static LocatableBlock raytraceBlock(final CommandContext ctx) throws CommandException {
        final ServerPlayer player = TagTest.requirePlayerRayTrace(ctx);
        return RayTrace.block()
                .select(RayTrace.nonAir())
                .world(player.world())
                .sourceEyePosition(player)
                .direction(player)
                .execute()
                .orElseThrow(() -> new CommandException(Component.text("You must look at a block to use this command!")))
                .selectedObject();
    }

    private static EntityType<@NonNull ?> raytraceEntity(final CommandContext ctx) throws CommandException {
        final ServerPlayer player = TagTest.requirePlayerRayTrace(ctx);
        return RayTrace.entity()
                .world(player.world())
                .sourceEyePosition(player)
                .direction(player)
                .execute()
                .orElseThrow(() -> new CommandException(Component.text("You must look at an entity to use this command!")))
                .selectedObject().type();
    }

    private static ItemType requireItemInHand(final CommandContext ctx) throws CommandException {
        final ServerPlayer serverPlayer = ctx.cause().first(ServerPlayer.class)
                .orElseThrow(() -> new CommandException(Component.text("You must be a player to use this command! (Held item)")));

        final ItemStack heldItem = serverPlayer.itemInHand(HandTypes.MAIN_HAND);
        if (heldItem.isEmpty()) {
            throw new CommandException(Component.text("You must hold an item in your main hand!"));
        }
        return heldItem.type();
    }

    private static <T extends Taggable<@NonNull T>> void sendTags(final Audience audience, final T taggable) {
        final Collection<Tag<T>> tags = taggable.tags();
        final String taggableKey = taggable.registryType().get().valueKey(taggable).toString();
        if (tags.isEmpty()) {
            audience.sendMessage(Component.text(taggableKey + " has no tags", NamedTextColor.RED));
            return;
        }
        audience.sendMessage(Component.text(taggableKey + " has tags:", NamedTextColor.GREEN));
        tags.forEach(tag -> audience.sendMessage(Component.text(" - " + tag.key(), NamedTextColor.BLUE)));
    }
}
