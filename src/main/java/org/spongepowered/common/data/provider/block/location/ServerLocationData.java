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
package org.spongepowered.common.data.provider.block.location;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Nameable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.api.block.entity.BlockEntity;
import org.spongepowered.api.block.entity.NameableBlockEntity;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.common.bridge.CustomNameableBridge;
import org.spongepowered.common.bridge.world.level.chunk.LevelChunkBridge;
import org.spongepowered.common.data.provider.DataProviderRegistrator;
import org.spongepowered.common.util.VecHelper;

public final class ServerLocationData {

    private ServerLocationData() {
    }

    // @formatter:off
    public static void register(final DataProviderRegistrator registrator) {
        registrator
                .asMutable(ServerLocation.class)
                    .create(Keys.BIOME_TEMPERATURE)
                        .get(h -> {
                            final Level world = (Level) h.world();
                            final BlockPos pos = VecHelper.toBlockPos(h);
                            final Biome biome = world.getBiome(pos);
                            return (double) biome.getBaseTemperature();
                        })
                    .create(Keys.BLOCK_LIGHT)
                        .get(h -> {
                            final Level world = (Level) h.world();
                            return world.getBrightness(LightLayer.BLOCK, VecHelper.toBlockPos(h));
                        })
                    .create(Keys.BLOCK_TEMPERATURE)
                        .get(h -> {
                            final Level world = (Level) h.world();
                            final BlockPos pos = VecHelper.toBlockPos(h);
                            final Biome biome = world.getBiome(pos);
                            return (double) biome.getTemperature(pos);
                        })
                    .create(Keys.SKY_LIGHT)
                        .get(h -> {
                            final Level world = (Level) h.world();
                            final BlockPos pos = VecHelper.toBlockPos(h);
                            return world.getBrightness(LightLayer.SKY, pos);
                        })
                    .create(Keys.IS_FULL_BLOCK)
                        .get(h -> {
                            final BlockState block = (BlockState) h.block();
                            final Level world = (Level) h.world();
                            final BlockPos pos = VecHelper.toBlockPos(h.position());
                            return block.isSolidRender(world, pos);
                        })
                    .create(Keys.IS_INDIRECTLY_POWERED)
                        .get(h -> {
                            final Level world = (Level) h.world();
                            final BlockPos pos = VecHelper.toBlockPos(h);
                            return world.getBestNeighborSignal(pos) > 0;
                        })
                    .create(Keys.DISPLAY_NAME)
                        .get(h -> SpongeAdventure.asAdventure(((Nameable)h.blockEntity().get()).getDisplayName()))
                        .supports(h -> h.blockEntity().isPresent() && h.blockEntity().get() instanceof NameableBlockEntity)
                    .create(Keys.CUSTOM_NAME)
                        .get(h -> {
                            final BlockEntity blockEntity = h.blockEntity().get();
                            return ((Nameable) blockEntity).hasCustomName() ? SpongeAdventure.asAdventure(((Nameable)blockEntity).getCustomName()) : null;
                        })
                        .set((h, v) -> (((CustomNameableBridge)h.blockEntity().get())).bridge$setCustomDisplayName(SpongeAdventure.asVanilla(v)))
                        .delete(h -> (((CustomNameableBridge)h.blockEntity().get())).bridge$setCustomDisplayName(null))
                        .supports(h -> h.blockEntity().isPresent() && h.blockEntity().get() instanceof NameableBlockEntity)
                    .create(Keys.CREATOR)
                        .get(h -> ((LevelChunkBridge)h.world().chunk(h.chunkPosition())).bridge$getBlockCreatorUUID(VecHelper.toBlockPos(h.blockPosition())).orElse(null))
                        .set((h, v) -> ((LevelChunkBridge)h.world().chunk(h.chunkPosition())).bridge$setBlockCreator(VecHelper.toBlockPos(h.blockPosition()), v))
                        .delete(h -> ((LevelChunkBridge)h.world().chunk(h.chunkPosition())).bridge$setBlockCreator(VecHelper.toBlockPos(h.blockPosition()), null))
                    .create(Keys.NOTIFIER)
                        .get(h -> ((LevelChunkBridge)h.world().chunk(h.chunkPosition())).bridge$getBlockNotifierUUID(VecHelper.toBlockPos(h.blockPosition())).orElse(null))
                        .set((h, v) -> ((LevelChunkBridge)h.world().chunk(h.chunkPosition())).bridge$setBlockNotifier(VecHelper.toBlockPos(h.blockPosition()), v))
                        .delete(h -> ((LevelChunkBridge)h.world().chunk(h.chunkPosition())).bridge$setBlockNotifier(VecHelper.toBlockPos(h.blockPosition()), null))
                    ;
    }
    // @formatter:on
}
