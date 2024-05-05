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
package org.spongepowered.test.chunk;

import com.google.inject.Inject;
import io.leangen.geantyref.TypeToken;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.event.world.chunk.ChunkEvent;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;
import org.spongepowered.test.LoadableModule;

import java.util.HashSet;
import java.util.Set;

@Plugin("chunkeventtest")
public final class ChunkEventTest implements LoadableModule {

    private final PluginContainer plugin;

    private static final boolean LOG_CHUNK_EVENTS = Boolean.getBoolean("sponge.logChunkEvents");

    private boolean logEvents = ChunkEventTest.LOG_CHUNK_EVENTS;
    private boolean cancelBlockSave = false;
    private boolean cancelEntitySave = false;
    private final Set<EntityType<?>> filterEntitySave = new HashSet<>();
    private final Set<EntityType<?>> addEntityLoad = new HashSet<>();
    private final Set<BlockType> filterBlockSave = new HashSet<>();
    private final Set<BlockType> addBlockLoad = new HashSet<>();

    @Inject
    public ChunkEventTest(final Game game, final PluginContainer plugin) {
        this.plugin = plugin;
        if (ChunkEventTest.LOG_CHUNK_EVENTS) {
            game.eventManager().registerListeners(plugin, new ChunkListener());
        }
    }

    @Listener
    private void registerCommands(final RegisterCommandEvent<Command.Parameterized> event) {
        final Parameter.Value<EntityType<@NonNull ?>> entityTypeParam =
                Parameter.registryElement(new TypeToken<>() {}, RegistryTypes.ENTITY_TYPE, "minecraft").key("entityType").build();

        final Parameter.Value<BlockType> blockTypeParam =
                Parameter.registryElement(TypeToken.get(BlockType.class), RegistryTypes.BLOCK_TYPE, "minecraft").key("blockType").build();

        event.register(this.plugin, Command.builder()
                .executor(context -> {
                    this.logEvents = !this.logEvents;
                    final Component newState = Component.text(
                            this.logEvents ? "ON" : "OFF", this.logEvents ? NamedTextColor.GREEN : NamedTextColor.RED);
                    context.sendMessage(Identity.nil(), Component.text("Turning Chunk Log: ").append(newState));
                    return CommandResult.success();
                })
                .build(), "logChunkEvents"
        );
        event.register(this.plugin, Command.builder()
                .executor(context -> {
                    this.cancelBlockSave = !this.cancelBlockSave;
                    final Component newState = Component.text(
                            this.cancelBlockSave ? "OFF" : "ON", this.cancelBlockSave ? NamedTextColor.RED : NamedTextColor.GREEN);
                    context.sendMessage(Identity.nil(), Component.text("Turning Block Save: ").append(newState));
                    return CommandResult.success();
                })
                .build(), "toggleChunkBlockSave"
        );
        event.register(this.plugin, Command.builder()
                .executor(context -> {
                    this.cancelEntitySave = !this.cancelEntitySave;
                    final Component newState = Component.text(
                            this.cancelEntitySave ? "OFF" : "ON", this.cancelEntitySave ? NamedTextColor.RED : NamedTextColor.GREEN);
                    context.sendMessage(Identity.nil(), Component.text("Turning Entity Save: ").append(newState));
                    return CommandResult.success();
                })
                .build(), "toggleChunkEntitySave"
        );
        event.register(this.plugin, Command.builder()
                .addParameter(entityTypeParam)
                .executor(context -> {
                    final EntityType<?> entityType = context.requireOne(entityTypeParam);
                    if (!this.filterEntitySave.contains(entityType)) {
                        this.filterEntitySave.add(entityType);
                        context.sendMessage(Identity.nil(), Component.text("Filtering entity: " + entityType.key(RegistryTypes.ENTITY_TYPE), NamedTextColor.GREEN));
                    } else {
                        this.filterEntitySave.remove(entityType);
                        context.sendMessage(Identity.nil(), Component.text("Removed entity from filter: " + entityType.key(RegistryTypes.ENTITY_TYPE), NamedTextColor.RED));
                    }
                    return CommandResult.success();
                })
                .build(), "chunkSaveEntityFilter"
        );
        event.register(this.plugin, Command.builder()
                .addParameter(entityTypeParam)
                .executor(context -> {
                    final EntityType<?> entityType = context.requireOne(entityTypeParam);
                    if (!this.addEntityLoad.contains(entityType)) {
                        this.addEntityLoad.add(entityType);
                        context.sendMessage(Identity.nil(), Component.text("Adding entity: " + entityType.key(RegistryTypes.ENTITY_TYPE), NamedTextColor.GREEN));
                    } else {
                        this.addEntityLoad.remove(entityType);
                        context.sendMessage(Identity.nil(), Component.text("No longer adding entity: " + entityType.key(RegistryTypes.ENTITY_TYPE), NamedTextColor.RED));
                    }
                    return CommandResult.success();
                })
                .build(), "chunkLoadExtraEntity"
        );
        event.register(this.plugin, Command.builder()
                .addParameter(blockTypeParam)
                .executor(context -> {
                    final BlockType blockType = context.requireOne(blockTypeParam);
                    if (!this.filterBlockSave.contains(blockType)) {
                        this.filterBlockSave.add(blockType);
                        context.sendMessage(Identity.nil(), Component.text("Filtering block: " + blockType.key(RegistryTypes.BLOCK_TYPE), NamedTextColor.GREEN));
                    } else {
                        this.filterBlockSave.remove(blockType);
                        context.sendMessage(Identity.nil(), Component.text("Removed block from filter: " + blockType.key(RegistryTypes.BLOCK_TYPE), NamedTextColor.RED));
                    }
                    return CommandResult.success();
                })
                .build(), "chunkSaveBlockFilter"
        );
        event.register(this.plugin, Command.builder()
                .addParameter(blockTypeParam)
                .executor(context -> {
                    final BlockType blockType = context.requireOne(blockTypeParam);
                    if (!this.addBlockLoad.contains(blockType)) {
                        this.addBlockLoad.add(blockType);
                        context.sendMessage(Identity.nil(), Component.text("Adding block: " + blockType.key(RegistryTypes.BLOCK_TYPE), NamedTextColor.GREEN));
                    } else {
                        this.addBlockLoad.remove(blockType);
                        context.sendMessage(Identity.nil(), Component.text("No longer adding block: " + blockType.key(RegistryTypes.BLOCK_TYPE), NamedTextColor.RED));
                    }
                    return CommandResult.success();
                })
                .build(), "chunkLoadExtraBlock"
        );
    }

    @Override
    public void enable(final CommandContext ctx) {
        Sponge.eventManager().registerListeners(this.plugin, new ChunkListener());
    }


    class ChunkListener {

        @Listener
        public void onChunkGenerated(final ChunkEvent.Generated event) {
            if (ChunkEventTest.this.logEvents) {
                Sponge.game().systemSubject().sendMessage(Component.text("Generated Chunk " + event.chunkPosition() + " in " + event.worldKey().asString()));
            }
        }

        @Listener
        public void onChunkLoad(final ChunkEvent.Load event) {
            if (ChunkEventTest.this.logEvents) {
                Sponge.game().systemSubject().sendMessage(Component.text("Load Chunk " + event.chunkPosition() + " in " + event.worldKey().asString()));
            }
        }

        @Listener
        public void onChunkUnload(final ChunkEvent.Unload event) {
            if (ChunkEventTest.this.logEvents) {
                Sponge.game().systemSubject().sendMessage(Component.text("Unload Chunk " + event.chunkPosition() + " in " + event.worldKey().asString()));
            }
        }

        @Listener
        public void onChunkBlocksLoad(final ChunkEvent.Blocks.Load event) {
            /*ChunkEventTest.this.addBlockLoad.forEach(b ->
                    event.blockVolume().blockStateStream(event.blockVolume().min(), event.blockVolume().max(), StreamOptions.lazily())
                            .filter(e -> ((BlockState) e.type()).type().equals(b))
                            .transform(e -> VolumeElement.of(e.volume(), BlockTypes.PINK_WOOL.get().defaultState(), e.position()))
                            .apply(VolumeCollectors.of(event.blockVolume(), VolumePositionTranslators.identity(), VolumeApplicators.applyBlocks())));*/
            if (ChunkEventTest.this.logEvents) {
                Sponge.game().systemSubject().sendMessage(Component.text("Load Chunk Blocks " + event.chunkPosition() + " in " + event.worldKey().asString()));
            }
        }

        @Listener
        public void onChunkBlocksSavePre(final ChunkEvent.Blocks.Save.Pre event) {
            if (ChunkEventTest.this.cancelBlockSave) {
                event.setCancelled(true);
                return;
            }
            /*ChunkEventTest.this.filterBlockSave.forEach(b ->
                    event.blockVolume().blockStateStream(event.blockVolume().min(), event.blockVolume().max(), StreamOptions.lazily())
                            .filter(e -> ((BlockState) e.type()).type().equals(b))
                            .transform(e -> VolumeElement.of(e.volume(), BlockTypes.AIR.get().defaultState(), e.position()))
                            .apply(VolumeCollectors.of(event.blockVolume(), VolumePositionTranslators.identity(), VolumeApplicators.applyBlocks())));*/
            if (ChunkEventTest.this.logEvents) {
                Sponge.game().systemSubject().sendMessage(Component.text("Pre Save Chunk Blocks " + event.chunkPosition() + " in " + event.worldKey().asString()));
            }
        }

        @Listener
        public void onChunkBlocksSavePost(final ChunkEvent.Blocks.Save.Post event) {
            if (ChunkEventTest.this.logEvents) {
                Sponge.game().systemSubject().sendMessage(Component.text("Post Save Chunk Blocks " + event.chunkPosition() + " in " + event.worldKey().asString()));
            }
        }

        @Listener
        public void onChunkEntitiesLoad(final ChunkEvent.Entities.Load event) {
            ChunkEventTest.this.addEntityLoad.forEach(e -> {
                final Entity entity = event.chunk().createEntity(e, event.chunk().min().add(8, event.chunk().size().y() / 2, 8));
                entity.offer(Keys.TRANSIENT, true);
                entity.offer(Keys.IS_GRAVITY_AFFECTED, false);
                entity.offer(Keys.IS_AI_ENABLED, false);
                event.chunk().spawnEntity(entity);
            });
            if (ChunkEventTest.this.logEvents) {
                Sponge.game().systemSubject().sendMessage(Component.text("Load Chunk Entities " + event.chunkPosition() + " in " + event.worldKey().asString()));
            }
        }

        @Listener
        public void onChunkEntitiesSavePre(final ChunkEvent.Entities.Save.Pre event) {
            if (ChunkEventTest.this.cancelEntitySave) {
                event.setCancelled(true);
                return;
            }
            if (!ChunkEventTest.this.filterEntitySave.isEmpty()) {
                event.chunk().entities().forEach(e -> {
                    if (ChunkEventTest.this.filterEntitySave.contains(e.type())) {
                        e.remove();
                    }
                });
            }
            if (ChunkEventTest.this.logEvents) {
                Sponge.game().systemSubject().sendMessage(Component.text("Pre Save Chunk Entities " + event.chunkPosition() + " in " + event.worldKey().asString()));
            }
        }

        @Listener
        public void onChunkEntitiesSavePost(final ChunkEvent.Entities.Save.Post event) {
            if (ChunkEventTest.this.logEvents) {
                Sponge.game().systemSubject().sendMessage(Component.text("Post Save Chunk Entities " + event.chunkPosition() + " in " + event.worldKey().asString()));
            }
        }
    }
}
