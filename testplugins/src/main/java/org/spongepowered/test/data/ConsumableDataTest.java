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
package org.spongepowered.test.data;

import com.google.inject.Inject;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.ConsumeEffect;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.effect.potion.PotionEffectTypes;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.util.Ticks;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;
import org.spongepowered.test.LoadableModule;

@Plugin("consumabledata")
public class ConsumableDataTest implements LoadableModule {

    private final PluginContainer plugin;

    @Inject
    public ConsumableDataTest(final PluginContainer plugin) {
        this.plugin = plugin;
    }

    @Override
    public void enable(final CommandContext ctx) {
    }

    @Listener
    public void onRegisterCommand(final RegisterCommandEvent<Command.Parameterized> event) {
        final Command.Builder builder = Command.builder();
        builder.addChild(Command.builder().executor(this::items).build(), "items");
        builder.addChild(Command.builder().executor(this::applyTeleport).build(), "applyteleport");
        builder.addChild(Command.builder().executor(this::applySound).build(), "applysound");
        event.register(this.plugin, builder.build(), "consumabledata");
    }

    private CommandResult items(final CommandContext commandContext) {
        final ServerPlayer player = commandContext.cause().first(ServerPlayer.class).orElse(null);
        if (player == null) {
            return CommandResult.error(Component.text("Must be run ingame by a player"));
        }

        final ItemStack stack1 = ItemStack.of(ItemTypes.BRICKS);
        stack1.offer(Keys.CUSTOM_NAME, Component.text("Remove slowness, get speed & regeneration"));
        stack1.offerSingle(Keys.CONSUME_EFFECTS, ConsumeEffect.playSound(SoundTypes.BLOCK_ANVIL_BREAK));
        stack1.offerSingle(Keys.CONSUME_EFFECTS, ConsumeEffect.teleportRandomly(5));
        stack1.offerSingle(Keys.CONSUME_EFFECTS, ConsumeEffect.removeEffects(PotionEffectTypes.SLOWNESS));
        stack1.offerSingle(Keys.CONSUME_EFFECTS, ConsumeEffect.applyEffects(
            PotionEffect.of(PotionEffectTypes.SPEED, 1, Ticks.of(20 * 30)),
            PotionEffect.of(PotionEffectTypes.REGENERATION, 0, Ticks.of(20 * 30))
        ));

        final ItemStack stack2 = ItemStack.of(ItemTypes.CHORUS_FRUIT);
        stack2.offer(Keys.CUSTOM_NAME, Component.text("Remove all effects, get slowness & haste with 50% chance"));
        stack2.offerSingle(Keys.CONSUME_EFFECTS, ConsumeEffect.clearEffects());
        stack2.offerSingle(Keys.CONSUME_EFFECTS, ConsumeEffect.applyEffects(0.5,
            PotionEffect.of(PotionEffectTypes.SLOWNESS, 1, Ticks.of(20 * 10)),
            PotionEffect.of(PotionEffectTypes.HASTE, 0, Ticks.of(20 * 10))
            ));

        player.inventory().offer(stack1, stack2);
        return CommandResult.success();
    }

    private CommandResult applyTeleport(final CommandContext commandContext) {
        final ServerPlayer player = commandContext.cause().first(ServerPlayer.class).orElse(null);
        if (player == null) {
            return CommandResult.error(Component.text("Must be run ingame by a player"));
        }

        ConsumeEffect.teleportRandomly(20).apply(player);
        return CommandResult.success();
    }

    private CommandResult applySound(final CommandContext commandContext) {
        final ServerPlayer player = commandContext.cause().first(ServerPlayer.class).orElse(null);
        if (player == null) {
            return CommandResult.error(Component.text("Must be run ingame by a player"));
        }

        ConsumeEffect.playSound(SoundTypes.BLOCK_BELL_USE).apply(player);
        return CommandResult.success();
    }
}
