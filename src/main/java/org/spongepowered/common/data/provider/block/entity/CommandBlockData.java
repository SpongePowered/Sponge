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
package org.spongepowered.common.data.provider.block.entity;

import org.spongepowered.api.data.Keys;
import org.spongepowered.common.accessor.world.level.BaseCommandBlockAccessor;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.common.data.provider.DataProviderRegistrator;

import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.CommandBlockEntity;

public final class CommandBlockData {

    private CommandBlockData() {
    }

    // @formatter:off
    public static void register(final DataProviderRegistrator registrator) {
        registrator
                .asMutable(CommandBlockEntity.class)
                    .create(Keys.COMMAND)
                        .get(h -> h.getCommandBlock().getCommand())
                        .set((h, v) -> ((BaseCommandBlockAccessor) h.getCommandBlock()).accessor$command(v))
                    .create(Keys.LAST_COMMAND_OUTPUT)
                        .get(h -> {
                            final Component component = ((BaseCommandBlockAccessor) h.getCommandBlock()).accessor$lastOutput();
                            return component == null ? null : SpongeAdventure.asAdventure(component);
                        })
                        .set((h, v) -> h.getCommandBlock().setLastOutput(SpongeAdventure.asVanilla(v)))
                        .delete(h -> h.getCommandBlock().setLastOutput(null))
                    .create(Keys.SUCCESS_COUNT)
                        .get(h -> h.getCommandBlock().getSuccessCount())
                        .set((h, v) -> ((BaseCommandBlockAccessor) h.getCommandBlock()).accessor$successCount(v))
                    .create(Keys.TRACKS_OUTPUT)
                        .get(h -> h.getCommandBlock().acceptsFailure())
                        .set((h, v) -> h.getCommandBlock().setTrackOutput(v));
    }
    // @formatter:on
}
