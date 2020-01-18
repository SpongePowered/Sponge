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


import com.google.inject.Inject;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.entity.BlockEntity;
import org.spongepowered.api.block.entity.BlockEntityType;
import org.spongepowered.api.block.entity.BlockEntityTypes;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.filter.type.Exclude;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.channel.MessageReceiver;

@Plugin(id = "pistontest", name = "Piston Test", description = "A plugin to test cancelling pistons", version = "0.0.0")
public class PistonTest implements LoadableModule {

    @Inject
    private PluginContainer container;

    private final PistonListener listener = new PistonListener();

    @Override
    public void enable(MessageReceiver src) {
        Sponge.getEventManager().registerListeners(this.container, this.listener);
    }

    public static class PistonListener {

        private boolean debugLog = false;

        @Listener
        @Exclude(ChangeBlockEvent.Post.class)
        public void onChangeBlock(ChangeBlockEvent event) {
            final BlockEntityType tileEntityType = event.getCause().first(BlockEntity.class).map(BlockEntity::getType).orElse(BlockEntityTypes.CHEST.get());
            if (tileEntityType == BlockEntityTypes.PISTON.get()) {
                event.setCancelled(true);
                return;
            }
            event.getTransactions().forEach(transaction -> {
                final BlockSnapshot original = transaction.getOriginal();
                final BlockState state = original.getState();
                final BlockType type = state.getType();
                if (type == BlockTypes.PISTON.get() || type == BlockTypes.MOVING_PISTON.get() || type == BlockTypes.PISTON_HEAD.get() || type == BlockTypes.STICKY_PISTON.get()) {
                    event.setCancelled(true);
                }
            });
            if (event.isCancelled()) {
                // Centralized line to link to other breakpoints for testing/debugging
                if (this.debugLog) {
                    System.err.println("Cancelling");
                }
            }

        }
    }
}
