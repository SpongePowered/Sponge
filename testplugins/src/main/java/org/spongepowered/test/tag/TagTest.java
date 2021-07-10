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
import org.spongepowered.api.Sponge;
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
import org.spongepowered.api.event.lifecycle.RegisterDataPackValueEvent;
import org.spongepowered.api.fluid.FluidType;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.registry.DefaultedRegistryType;
import org.spongepowered.api.registry.RegistryEntry;
import org.spongepowered.api.registry.RegistryKey;
import org.spongepowered.api.registry.RegistryType;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.tag.BlockTypeTags;
import org.spongepowered.api.tag.Tag;
import org.spongepowered.api.tag.TagTemplate;
import org.spongepowered.api.tag.TagType;
import org.spongepowered.api.tag.TagTypes;
import org.spongepowered.api.tag.Taggable;
import org.spongepowered.api.util.blockray.RayTrace;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.jvm.Plugin;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Plugin("tagtest")
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
    private static final TypeToken<TagType<@NonNull ?>> TAG_TYPE_TOKEN = new TypeToken<TagType<@NonNull ?>>() {};

    @Listener
    public void registerTags(final RegisterDataPackValueEvent<@NonNull TagTemplate> event) {
        this.logger.info("Adding tags.");

        final TagTemplate tagRegistration = TagTemplate.builder(TagTypes.BLOCK_TYPE)
                .key(ResourceKey.of(this.pluginContainer, "wool"))
                .addValue(BlockTypes.GRASS)
                .build();

        event.register(tagRegistration);

        final TagTemplate woolLog = TagTemplate.builder(TagTypes.BLOCK_TYPE)
                .key(BlockTypeTags.WOOL.location())
                .addValue(BlockTypes.OAK_LOG)
                .build();

        event.register(woolLog);

        final TagTemplate woolGrass = TagTemplate.builder(TagTypes.BLOCK_TYPE)
                .key(ResourceKey.minecraft("wool"))
                .addValue(BlockTypes.GRASS_BLOCK)
                .build();

        event.register(woolGrass);

        final TagTemplate underwaterDiamond = TagTemplate.builder(TagTypes.BLOCK_TYPE)
                .key(BlockTypeTags.UNDERWATER_BONEMEALS.location())
                .addValue(BlockTypes.DIAMOND_BLOCK)
                .build();

        event.register(underwaterDiamond);

        final TagTemplate ores = TagTemplate.builder(TagTypes.BLOCK_TYPE)
                .key(ResourceKey.of(this.pluginContainer, "ores"))
                .addValue(BlockTypes.COAL_ORE)
                .addValue(BlockTypes.IRON_ORE)
                .addValue(BlockTypes.LAPIS_ORE)
                .addValue(BlockTypes.REDSTONE_ORE)
                .addValue(BlockTypes.EMERALD_ORE)
                .addValue(BlockTypes.DIAMOND_ORE)
                .addValue(BlockTypes.NETHER_QUARTZ_ORE)
                .addChild(BlockTypeTags.GOLD_ORES) // Test gold ore child.
                .build();

        event.register(ores);

        final TagTemplate oresAndBlocks = TagTemplate.builder(TagTypes.BLOCK_TYPE)
                .key(ResourceKey.of(this.pluginContainer, "oresandblocks"))
                .addValue(BlockTypes.COAL_BLOCK)
                .addValue(BlockTypes.IRON_BLOCK)
                .addValue(BlockTypes.LAPIS_BLOCK)
                .addValue(BlockTypes.REDSTONE_BLOCK)
                .addValue(BlockTypes.GOLD_BLOCK)
                .addValue(BlockTypes.EMERALD_BLOCK)
                .addValue(BlockTypes.DIAMOND_BLOCK)
                .addValue(BlockTypes.QUARTZ_BLOCK)
                .addChild(ores) // Test child TagTemplate
                .build();

        event.register(oresAndBlocks);

        final ResourceKey nonExistentKey = ResourceKey.of("notrealnamespace", "notrealvalue");
        final TagTemplate brokenChildTag = TagTemplate.builder(TagTypes.ITEM_TYPE)
                .key(ResourceKey.of(this.pluginContainer, "brokenchildtag"))
                .addChild(RegistryKey.of(RegistryTypes.ITEM_TYPE_TAGS, nonExistentKey), true)
                .build();

        event.register(brokenChildTag);

        final TagTemplate brokenValueTag = TagTemplate.builder(TagTypes.ITEM_TYPE)
                .key(ResourceKey.of(this.pluginContainer, "brokenvaluetag"))
                .addValue(RegistryKey.of(RegistryTypes.ITEM_TYPE, nonExistentKey))
                .build();

        event.register(brokenValueTag);

        final TagTemplate stillWorkingTag = TagTemplate.builder(TagTypes.ITEM_TYPE)
                .key(ResourceKey.of(this.pluginContainer, "stillworkingtag"))
                .addValue(RegistryKey.of(RegistryTypes.ITEM_TYPE, nonExistentKey), false)
                .addChild(RegistryKey.of(RegistryTypes.ITEM_TYPE_TAGS, nonExistentKey), false)
                .addValue(ItemTypes.REDSTONE)
                .build();

        event.register(stillWorkingTag);
    }

    @Listener
    public void registerCommands(final RegisterCommandEvent<Command.Parameterized> event) {
        final Parameter.Value<Tag<BlockType>> blockTypeTagParameter = TagTest.makeTagRegistryParameter(TagTest.BLOCK_TYPE_TAG_TOKEN, RegistryTypes.BLOCK_TYPE_TAGS, "blocktag");
        final Command.Parameterized blockHasTag = Command.builder()
                .addParameter(blockTypeTagParameter)
                .executor(ctx -> {
                    final Tag<BlockType> tag = ctx.requireOne(blockTypeTagParameter);

                    final BlockType blockType = TagTest.raytraceBlock(ctx).blockState().type();
                    TagTest.sendTagMessage(blockType, RegistryTypes.BLOCK_TYPE, tag, tag.key(RegistryTypes.BLOCK_TYPE_TAGS), ctx.cause().audience());
                    return CommandResult.success();
                })
                .build();

        final Parameter.Value<Tag<ItemType>> itemTypeTagParameter = TagTest.makeTagRegistryParameter(TagTest.ITEM_TYPE_TAG_TOKEN, RegistryTypes.ITEM_TYPE_TAGS, "itemtag");
        final Command.Parameterized itemHasTag = Command.builder()
                .addParameter(itemTypeTagParameter)
                .executor(ctx -> {
                    final Tag<ItemType> tag = ctx.requireOne(itemTypeTagParameter);

                    final ItemType itemType = TagTest.requireItemInHand(ctx);
                    TagTest.sendTagMessage(itemType, RegistryTypes.ITEM_TYPE, tag, tag.key(RegistryTypes.ITEM_TYPE_TAGS), ctx.cause().audience());
                    return CommandResult.success();
                })
                .build();

        final Parameter.Value<Tag<EntityType<?>>> entityTypeTagParameter = TagTest.makeTagRegistryParameter(TagTest.ENTITY_TYPE_TAG_TOKEN, RegistryTypes.ENTITY_TYPE_TAGS, "entitytag");
        final Command.Parameterized entityHasTag = Command.builder()
                .addParameter(entityTypeTagParameter)
                .executor(ctx -> {
                    final Tag<EntityType<?>> tag = ctx.requireOne(entityTypeTagParameter);

                    final EntityType<?> type = TagTest.raytraceEntity(ctx);

                    TagTest.sendTagMessage(type, RegistryTypes.ENTITY_TYPE, tag, tag.key(RegistryTypes.ENTITY_TYPE_TAGS), ctx.cause().audience());
                    return CommandResult.success();
                })
                .build();

        final Parameter.Value<Tag<FluidType>> fluidTypeTagParameter = TagTest.makeTagRegistryParameter(TagTest.FLUID_TYPE_TAG_TOKEN, RegistryTypes.FLUID_TYPE_TAGS, "fluidtag");
        final Command.Parameterized fluidHasTag = Command.builder()
                .addParameter(fluidTypeTagParameter)
                .executor(ctx -> {
                    final Tag<FluidType> tag = ctx.requireOne(fluidTypeTagParameter);

                    final FluidType fluidType = TagTest.raytraceBlock(ctx).serverLocation().fluid().type();

                    TagTest.sendTagMessage(fluidType, RegistryTypes.FLUID_TYPE, tag, tag.key(RegistryTypes.FLUID_TYPE_TAGS), ctx.cause().audience());

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

        final Parameter.Value<TagType<@NonNull ?>> TAG_TYPE = Parameter.registryElement(TagTest.TAG_TYPE_TOKEN, RegistryTypes.TAG_TYPES).key("tagtype").build();

        final Command.Parameterized listTags = Command.builder()
                .addParameter(TAG_TYPE)
                .executor(ctx -> {
                    final TagType<@NonNull ?> tagType = ctx.requireOne(TAG_TYPE);

                    final List<Component> contents = Sponge.game().registries().registry(tagType.tagRegistry()).streamEntries()
                            .map(RegistryEntry::key)
                            .map(ResourceKey::toString)
                            .map(s -> Component.text(s, NamedTextColor.AQUA))
                            .collect(Collectors.toList());


                    PaginationList.builder()
                            .title(Component.text(tagType.key(RegistryTypes.TAG_TYPES).toString(), NamedTextColor.GOLD))
                            .contents(contents)
                            .sendTo(ctx.cause().audience());
                    return CommandResult.success();
                })
                .build();

        event.register(this.pluginContainer, listTags, "listtags");
    }

    private static <T> Parameter.Value<Tag<T>> makeTagRegistryParameter(final TypeToken<Tag<T>> token, final RegistryType<Tag<T>> registryType, final String key) {
        final ValueParameter<Tag<T>> valueParameter = VariableValueParameters.registryEntryBuilder(
                    VariableValueParameters.RegistryEntryBuilder.GLOBAL_HOLDER_PROVIDER, registryType)
                .defaultNamespace(ResourceKey.MINECRAFT_NAMESPACE)
                .build();
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

    private static <T extends Taggable<@NonNull T>> void sendTagMessage(final T taggable, final DefaultedRegistryType<T> registry, final Tag<T> tag, final ResourceKey tagKey, final Audience audience) {
        final boolean contained = tag.contains(taggable);
        final Component message = contained ? Component.text(taggable.key(registry) + " has tag " + tagKey, NamedTextColor.GREEN)
                : Component.text(taggable.key(registry) + " does not have tag " + tagKey, NamedTextColor.RED);
        audience.sendMessage(message);
    }

    private static <T extends Taggable<@NonNull T>> void sendTags(final Audience audience, final T taggable) {
        final Collection<Tag<T>> tags = taggable.tags();
        final String taggableKey = Sponge.game().registries().registry(taggable.tagType().taggableRegistry()).valueKey(taggable).toString();
        if (tags.isEmpty()) {
            audience.sendMessage(Component.text(taggableKey + " has no tags", NamedTextColor.RED));
            return;
        }
        audience.sendMessage(Component.text(taggableKey + " has tags:", NamedTextColor.GREEN));
        tags.forEach(tag -> audience.sendMessage(Component.text(" - " +
                Sponge.game().registries().registry(taggable.tagType().tagRegistry()).valueKey(tag).toString(), NamedTextColor.BLUE)));
    }
}
