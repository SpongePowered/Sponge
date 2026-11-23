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
package org.spongepowered.common.test.gametest.command;

import net.minecraft.commands.Commands;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.common.command.manager.SpongeCommandManager;
import org.spongepowered.common.command.registrar.BrigadierCommandRegistrar;
import org.spongepowered.common.test.gametest.GameTest;
import org.spongepowered.common.test.gametest.GameTestAssertions;
import org.spongepowered.common.test.gametest.GameTestGroup;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Regression test for #4245: the requirement predicate of a node in the client-bound
 * command tree must be evaluated against a cause that holds the sending player.
 *
 * <p>A requirement that asks for the player must be able to find it while the tree is
 * built for the client, otherwise the node is dropped from the packet and the client
 * shows "Unknown or incomplete command" for a command that executes fine.</p>
 */
@GameTestGroup("command")
public final class CommandTreeGameTests {

    @GameTest(maxTicks = 40)
    public void requirementSeesSendingPlayer(final GameTestHelper helper) {
        final MinecraftServer server = helper.getLevel().getServer();
        // 26.4 Commands no longer holds the dispatcher, so register as vanilla's did, through the brigadier registrar
        final BrigadierCommandRegistrar registrar = SpongeCommandManager.get(server).getBrigadierRegistrar();
        final AtomicInteger evaluations = new AtomicInteger();
        final AtomicBoolean sawPlayer = new AtomicBoolean();

        registrar.register(Commands.literal("spongefix-gametest-requirement")
            .requires(source -> {
                evaluations.incrementAndGet();
                // Sponge's CommandSourceStack implements CommandCause; asking for the player here is the shape
                // from the issue: cause -> cause.first(ServerPlayer.class).isPresent()
                final boolean present = ((CommandCause) source)
                    .first(org.spongepowered.api.entity.living.player.server.ServerPlayer.class)
                    .isPresent();
                // 1.21.11's ClientboundCommandsPacket also tests each requirement against a player-less
                // compilation context to mark restricted nodes, so only the tree building has to see the player
                if (present) {
                    sawPlayer.set(true);
                }
                return true;
            }));

        // The mock player stays in the player list once placed, and any player in range makes the chunks around every
        // test structure spawn mobs for the rest of the run, so it leaves as soon as the tree is built
        final ServerPlayer player = helper.makeMockServerPlayerInLevel();
        try {
            server.getCommands().sendCommands(player);
        } finally {
            server.getPlayerList().remove(player);
        }

        GameTestAssertions.assertTrue(helper, evaluations.get() > 0,
            "The requirement of the test command was never evaluated while building the client-bound command tree");
        GameTestAssertions.assertTrue(helper, sawPlayer.get(),
            "The client-bound command tree was built with a cause that does not contain the sending player (#4245)");
        helper.succeed();
    }
}
