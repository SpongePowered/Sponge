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
package org.spongepowered.common.test.gametest;

import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.gametest.framework.GameTestListener;
import net.minecraft.gametest.framework.GameTestRunner;
import net.minecraft.network.protocol.game.ServerboundAcceptTeleportationPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerLoadedPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.common.mixin.gametest.ServerGamePacketListenerImplAccessor_GameTest;

/**
 * Readies each mock player a test makes with {@link GameTestHelper#makeMockServerPlayerInLevel()} to interact, and
 * removes it from the server once its test is done, whether it passed, failed or timed out.
 */
public final class MockPlayers {

    public static ServerPlayer joined(final GameTestInfo test, final ServerPlayer player) {
        // The join teleport leaves the server waiting on the client, which ignores the player's interactions until acked
        player.connection.handleAcceptTeleportPacket(
            new ServerboundAcceptTeleportationPacket(((ServerGamePacketListenerImplAccessor_GameTest) player.connection).gametest$awaitingTeleport()));
        // It also ignores them until the client says it has loaded the world
        player.connection.handleAcceptPlayerLoad(new ServerboundPlayerLoadedPacket());
        final PlayerList players = test.getLevel().getServer().getPlayerList();
        test.addListener(new GameTestListener() {
            @Override
            public void testStructureLoaded(final GameTestInfo info) {
            }

            @Override
            public void testPassed(final GameTestInfo info, final GameTestRunner runner) {
                MockPlayers.leave(players, player);
            }

            @Override
            public void testFailed(final GameTestInfo info, final GameTestRunner runner) {
                MockPlayers.leave(players, player);
            }

            @Override
            public void testAddedForRerun(final GameTestInfo original, final GameTestInfo rerun, final GameTestRunner runner) {
            }
        });
        return player;
    }

    private static void leave(final PlayerList players, final ServerPlayer player) {
        if (players.getPlayers().contains(player)) {
            players.remove(player);
        }
    }

    private MockPlayers() {
    }
}
