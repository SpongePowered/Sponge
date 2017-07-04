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

import static org.spongepowered.api.command.args.GenericArguments.playerOrSource;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.SkullType;
import org.spongepowered.api.data.type.SkullTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.text.Text;

import java.util.UUID;

@Plugin(id = "skulltest", name = "SkullTest", description = "A plugin to test Skulls")
public class SkullTest {

    final static Text SKULL = Text.of("skulltype");
    private static final Text PLAYER = Text.of("player");

    @Listener
    public void onInit(GameInitializationEvent event) {
        Sponge.getCommandManager().register(this, getSkullCommand(), "skullme");
    }

    private static CommandCallable getSkullCommand() {
        return CommandSpec.builder()
            .description(Text.of("Skull command"))
            .extendedDescription(Text.of("commands:\n", "Give you a skull"))
            .arguments(playerOrSource(PLAYER), GenericArguments.integer(SKULL))
            .executor(SkullTest::giveSkull)
            .build();
    }

    private static CommandResult giveSkull(CommandSource commandSource, CommandContext commandContext) {
        Player target = (Player) commandContext.getOne(PLAYER).get();
        int skulltype = (int) commandContext.getOne(SKULL).get();
        SkullType[] skulls = {
                SkullTypes.SKELETON,
                SkullTypes.WITHER_SKELETON,
                SkullTypes.ZOMBIE,
                SkullTypes.PLAYER,
                SkullTypes.CREEPER,
                SkullTypes.ENDER_DRAGON
        };

        final ItemStack stack;
        if((3 == skulltype) || (-1 == skulltype)){
            final GameProfile profile;
            if(skulltype==3){
                profile = target.getProfile();
            } else {
                profile = GameProfile.of(UUID.fromString("4c38ed11-596a-4fd4-ab1d-26f386c1cbac"), "MHF_Blaze");
            }

            stack = ItemStack.builder()
                    .itemType(ItemTypes.SKULL)
                    .keyValue(Keys.SKULL_TYPE, skulls[3])
                    .keyValue(Keys.REPRESENTED_PLAYER, profile)
                    .build();
        } else {
            stack = ItemStack.builder()
                    .itemType(ItemTypes.SKULL)
                    .keyValue(Keys.SKULL_TYPE, skulls[skulltype])
                    .build();
        }

        target.getInventory().offer(stack);
        return CommandResult.success();
    }


}
