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
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.event.lifecycle.RegisterDataPackValueEvent;
import org.spongepowered.api.fluid.FluidType;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.registry.DefaultedRegistryType;
import org.spongepowered.api.registry.RegistryType;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.tag.BlockTypeTags;
import org.spongepowered.api.tag.Tag;
import org.spongepowered.api.tag.TagRegistration;
import org.spongepowered.api.tag.TagTypes;
import org.spongepowered.api.tag.Taggable;
import org.spongepowered.api.util.blockray.RayTrace;
import org.spongepowered.api.util.blockray.RayTraceResult;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.jvm.Plugin;

@Plugin("tagtest")
public class TagTest {

    @Inject
    public PluginContainer pluginContainer;

    @Inject
    public Logger logger;

    private static final TypeToken<Tag<BlockType>> BLOCK_TYPE_TAG_TOKEN = new TypeToken<Tag<BlockType>>() {};
    private static final TypeToken<Tag<EntityType<?>>> ENTITY_TYPE_TAG_TOKEN = new TypeToken<Tag<EntityType<?>>>() {};
    private static final TypeToken<Tag<ItemType>> ITEM_TYPE_TAG_TOKEN = new TypeToken<Tag<ItemType>>() {};
    private static final TypeToken<Tag<FluidType>> FLUID_TYPE_TAG_TOKEN = new TypeToken<Tag<FluidType>>() {};

    @Listener
    public void registerTags(final RegisterDataPackValueEvent<@NonNull TagRegistration> event) {
        logger.info("Adding tags.");

        final TagRegistration tagRegistration = Tag.builder()
                .key(ResourceKey.of(pluginContainer, "wool"))
                .type(TagTypes.BLOCK_TYPE.get())
                .addValue(BlockTypes.GRASS.get())
                .build();

        event.register(tagRegistration);

        final TagRegistration woolLog = Tag.builder()
                .key(BlockTypeTags.WOOL.location())
                .type(TagTypes.BLOCK_TYPE.get())
                .addValue(BlockTypes.OAK_LOG.get())
                .build();

        event.register(woolLog);

        final TagRegistration woolGrass = Tag.builder()
                .key(ResourceKey.minecraft("wool"))
                .type(TagTypes.BLOCK_TYPE.get())
                .addValue(BlockTypes.GRASS_BLOCK.get())
                .build();

        event.register(woolGrass);

        final TagRegistration underwaterDiamond = Tag.builder()
                .key(BlockTypeTags.UNDERWATER_BONEMEALS.location())
                .type(TagTypes.BLOCK_TYPE.get())
                .addValue(BlockTypes.DIAMOND_BLOCK.get())
                .build();

        event.register(underwaterDiamond);
    }

    @Listener
    public void registerCommands(final RegisterCommandEvent<Command.Parameterized> event) {

        final Parameter.Value<Tag<BlockType>> BLOCK_TYPE_TAG = Parameter.registryElement(TagTest.BLOCK_TYPE_TAG_TOKEN, RegistryTypes.BLOCK_TYPE_TAGS).key("blocktag").build();
        final Command.Parameterized blockHasTag = Command.builder()
                .addParameter(BLOCK_TYPE_TAG)
                .executor(ctx -> {
                    final Tag<BlockType> tag = ctx.requireOne(BLOCK_TYPE_TAG);

                    final ServerPlayer serverPlayer = ctx.cause().first(ServerPlayer.class)
                            .orElseThrow(() -> new CommandException(Component.text("You must be a player to use this command! (Ray trace)")));
                    final RayTraceResult<@NonNull LocatableBlock> result = RayTrace.block()
                            .select(RayTrace.nonAir())
                            .world(serverPlayer.world())
                            .sourceEyePosition(serverPlayer)
                            .direction(serverPlayer)
                            .execute()
                            .orElseThrow(() -> new CommandException(Component.text("You must look at a block to use this command!")));

                    final BlockType blockType = result.selectedObject().blockState().type();
                    TagTest.sendTagMessage(blockType, RegistryTypes.BLOCK_TYPE, tag, tag.key(RegistryTypes.BLOCK_TYPE_TAGS), ctx.cause().audience());
                    return CommandResult.success();
                })
                .build();

        final Parameter.Value<Tag<ItemType>> ITEM_TYPE_TAG = Parameter.registryElement(TagTest.ITEM_TYPE_TAG_TOKEN, RegistryTypes.ITEM_TYPE_TAGS).key("itemtag").build();
        final Command.Parameterized itemHasTag = Command.builder()
                .addParameter(ITEM_TYPE_TAG)
                .executor(ctx -> {
                    final ServerPlayer serverPlayer = ctx.cause().first(ServerPlayer.class)
                            .orElseThrow(() -> new CommandException(Component.text("You must be a player to use this command! (Held item)")));
                    final ItemStack heldItem = serverPlayer.itemInHand(HandTypes.MAIN_HAND);
                    if (heldItem.isEmpty()) {
                        throw new CommandException(Component.text("You must hold an item in your main hand!"));
                    }
                    final Tag<ItemType> tag = ctx.requireOne(ITEM_TYPE_TAG);

                    final ItemType itemType = heldItem.type();
                    TagTest.sendTagMessage(itemType, RegistryTypes.ITEM_TYPE, tag, tag.key(RegistryTypes.ITEM_TYPE_TAGS), ctx.cause().audience());
                    return CommandResult.success();
                })
                .build();

        final Parameter.Value<Tag<EntityType<?>>> ENTITY_TYPE_TAG = Parameter.registryElement(TagTest.ENTITY_TYPE_TAG_TOKEN, RegistryTypes.ENTITY_TYPE_TAGS).key("entitytag").build();
        final Command.Parameterized entityHasTag = Command.builder()
                .addParameter(ENTITY_TYPE_TAG)
                .executor(ctx -> {
                    final ServerPlayer serverPlayer = ctx.cause().first(ServerPlayer.class)
                            .orElseThrow(() -> new CommandException(Component.text("You must be a player to use this command! (Ray trace)")));

                    final Tag<EntityType<?>> tag = ctx.requireOne(ENTITY_TYPE_TAG);


                    final RayTraceResult<@NonNull Entity> result = RayTrace.entity()
                            .world(serverPlayer.world())
                            .sourceEyePosition(serverPlayer)
                            .direction(serverPlayer)
                            .execute()
                            .orElseThrow(() -> new CommandException(Component.text("You must look at an entity to use this command!")));

                    final EntityType<?> type = result.selectedObject().type();

                    TagTest.sendTagMessage(type, RegistryTypes.ENTITY_TYPE, tag, tag.key(RegistryTypes.ENTITY_TYPE_TAGS), ctx.cause().audience());
                    return CommandResult.success();
                })
                .build();

        final Parameter.Value<Tag<FluidType>> FLUID_TYPE_TAG = Parameter.registryElement(TagTest.FLUID_TYPE_TAG_TOKEN, RegistryTypes.FLUID_TYPE_TAGS).key("fluidtag").build();
        final Command.Parameterized fluidHasTag = Command.builder()
                .addParameter(FLUID_TYPE_TAG)
                .executor(ctx -> {
                    final Tag<FluidType> tag = ctx.requireOne(FLUID_TYPE_TAG);

                    final ServerPlayer serverPlayer = ctx.cause().first(ServerPlayer.class)
                            .orElseThrow(() -> new CommandException(Component.text("You must be a player to use this command! (Ray trace)")));
                    final RayTraceResult<@NonNull LocatableBlock> result = RayTrace.block()
                            .select(RayTrace.nonAir())
                            .world(serverPlayer.world())
                            .sourceEyePosition(serverPlayer)
                            .direction(serverPlayer)
                            .execute()
                            .orElseThrow(() -> new CommandException(Component.text("You must look at a block to use this command!")));

                    final FluidType fluidType = result.selectedObject().serverLocation().fluid().type();

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

        event.register(pluginContainer, hasTag, "hastag");
    }

    private static <T extends Taggable> void sendTagMessage(T taggable, DefaultedRegistryType<T> registry, Tag<T> tag, ResourceKey tagKey, Audience audience) {
        boolean contained = tag.contains(taggable);
        final Component message = contained ? Component.text(taggable.key(registry) + " has tag " + tagKey, NamedTextColor.GREEN)
                : Component.text(taggable.key(registry) + " does not have tag " + tagKey, NamedTextColor.RED);
        audience.sendMessage(message);
    }
}
