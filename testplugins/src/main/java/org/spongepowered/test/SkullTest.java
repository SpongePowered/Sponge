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
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.managed.CommandExecutor;
import org.spongepowered.api.command.managed.TargetedCommandExecutor;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.SkullType;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.text.Text;

import java.util.UUID;
import java.util.function.BiFunction;

@Plugin(id = "skulltest", name = "Skull Test", description = "A plugin to test Skulls")
public class SkullTest {

    private final static Text SKULL = Text.of("skulltype");
    private static final Text PLAYER = Text.of("player");

    @Listener
    public void onInit(GameInitializationEvent event) {
        Sponge.getCommandManager().register(this,
                Command.builder()
                        .setShortDescription(Text.of("Gives you your player mobHead"))
                        .parameter(Parameter.playerOrSource().setKey(PLAYER).build())
                        .setTargetedExecutorErrorMessage(Text.of("This command can only be executed by players"))
                        .targetedExecutor(giveSkull(SkullTest::playerHead), Player.class)
                        .build(),
                "skullme");

        Sponge.getCommandManager().register(this,
                Command.builder()
                        .setShortDescription(Text.of("Gives you a Marcs Head Format Blaze"))
                        .parameter(Parameter.playerOrSource().setKey(PLAYER).build())
                        .setTargetedExecutorErrorMessage(Text.of("This command can only be executed by players"))
                        .targetedExecutor(giveSkull(SkullTest::blazeHead), Player.class)
                        .build(),
                "skullblaze");

        Sponge.getCommandManager().register(this,
                Command.builder()
                        .setShortDescription(Text.of("Gives you a monster head"))
                        .parameter(Parameter.playerOrSource().setKey(PLAYER).build())
                        .parameter(Parameter.catalogedElement(SkullType.class).setKey(SKULL).build())
                        .setTargetedExecutorErrorMessage(Text.of("This command can only be executed by players"))
                        .targetedExecutor(giveSkull(SkullTest::mobHead), Player.class)
                        .build(),
                "skullmob");
    }

    private static ItemStack.Builder playerHead(CommandContext commandContext, ItemStack.Builder builder) {
        return builder.add(
                Keys.REPRESENTED_PLAYER, commandContext.<Player>getOneUnchecked(PLAYER).getProfile()
        );
    }

    private static ItemStack.Builder blazeHead(CommandContext commandContext, ItemStack.Builder builder) {
        return builder.add(
                Keys.REPRESENTED_PLAYER, GameProfile.of(UUID.fromString("4c38ed11-596a-4fd4-ab1d-26f386c1cbac"), "MHF_Blaze")
        );
    }

    private static ItemStack.Builder mobHead(CommandContext ctx, ItemStack.Builder builder) {
        return builder.add(Keys.SKULL_TYPE, ctx.getOneUnchecked(SKULL));
    }

    private static TargetedCommandExecutor<Player> giveSkull(final BiFunction<CommandContext, ItemStack.Builder, ItemStack.Builder> profile) {
        return (cause, player, commandContext) -> {
            ItemStack.Builder builder = ItemStack.builder().itemType(ItemTypes.SKULL);
            profile.apply(commandContext, builder);
            player.getInventory().offer(builder.build());
            return CommandResult.success();
        };
    }
}
