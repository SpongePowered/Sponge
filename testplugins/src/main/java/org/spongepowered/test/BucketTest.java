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

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.filter.type.Exclude;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

@Plugin(id = "bucket_cancel_test", name = "Bucket Cancel Test", description = BucketTest.DESCRIPTION, version = "0.0.0")
public class BucketTest {

    public static final String DESCRIPTION = "Use /bucketcancel to toggle cancelling bucket usage, should be used to test for duplications";
    public static final Text CANCELLED = Text.of(TextColors.RED, "cancelled");
    public static final Text ALLOWED = Text.of(TextColors.GREEN, "allowed");

    private boolean enabled = false;
    @Listener
    @Exclude(ChangeBlockEvent.Post.class)
    public void onBlockChange(ChangeBlockEvent.Pre event, @Root Player player) {
        if (this.enabled) {
            if (event.getContext().containsKey(EventContextKeys.USED_ITEM)) {
                event.setCancelled(true);
            }
            // Technicallyh this should print twice, once for the Break and once for the Post.
            System.err.println(event);
        }
    }

    // Now, due to the nature of buckets and how they're handled,
    // we need to cancel the interactblockevent as well.
    @Listener
    public void onInteract(InteractBlockEvent.Secondary event, @Root Player player) {
        event.getContext().get(EventContextKeys.USED_ITEM).ifPresent(used -> {
            if (used.getType() == ItemTypes.WATER_BUCKET) {
                event.setCancelled(true);
            }
        });

    }

    @Listener
    public void onPreInit(GamePreInitializationEvent event) {
        Sponge.getCommandManager().register(this, CommandSpec.builder()
            .executor(((src, args) -> {
                if (!(src instanceof Player)) {
                    throw new CommandException(Text.of(TextColors.RED, "Must be a player to use this command!"));
                }
                this.enabled = !this.enabled;
                src.sendMessage(Text.of(TextColors.DARK_AQUA, "Buckets are being ", this.enabled ? CANCELLED : ALLOWED));
                return CommandResult.success();
            })).build(), "bucketcancel");
    }

}
