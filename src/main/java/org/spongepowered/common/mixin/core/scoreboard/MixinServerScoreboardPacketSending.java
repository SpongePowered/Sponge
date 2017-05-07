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
package org.spongepowered.common.mixin.core.scoreboard;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketDisplayObjective;
import net.minecraft.network.play.server.SPacketScoreboardObjective;
import net.minecraft.network.play.server.SPacketTeams;
import net.minecraft.network.play.server.SPacketUpdateScore;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.interfaces.IMixinServerScoreboard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

@Mixin(ServerScoreboard.class)
public abstract class MixinServerScoreboardPacketSending extends Scoreboard implements IMixinServerScoreboard {

    private static final String SEND_PACKET_METHOD =
            "Lnet/minecraft/server/management/PlayerList;sendPacketToAllPlayers(Lnet/minecraft/network/Packet;)V";
    private static final String SET_CONTAINS = "Ljava/util/Set;contains(Ljava/lang/Object;)Z";

    @Shadow @Final private MinecraftServer scoreboardMCServer;

    private List<EntityPlayerMP> players = new ArrayList<>();

    @Override
    public void sendToPlayers(Packet<?> packet) {
        for (EntityPlayerMP player: this.players) {
            player.connection.sendPacket(packet);
        }
    }

    @Override
    public void addPlayer(EntityPlayerMP player) {
        this.players.add(player);
        this.sendScoreboard(player);
    }

    void sendScoreboard(EntityPlayerMP player) {
        for (ScorePlayerTeam team: this.getTeams()) {
            player.connection.sendPacket(new SPacketTeams(team, 0));
        }

        for (ScoreObjective objective: this.getScoreObjectives()) {
            player.connection.sendPacket(new SPacketScoreboardObjective(objective, 0));
            for (Score score: this.getSortedScores(objective)) {
                player.connection.sendPacket(new SPacketUpdateScore(score));
            }
        }

        for (int i = 0; i < 19; ++i) {
            player.connection.sendPacket(new SPacketDisplayObjective(i, this.getObjectiveInDisplaySlot(i)));
        }
    }

    @Override
    public void removePlayer(EntityPlayerMP player, boolean sendPackets) {
        this.players.remove(player);
        if (sendPackets) {
            this.removeScoreboard(player);
        }
    }

    void removeScoreboard(EntityPlayerMP player) {
        this.removeTeams(player);
        this.removeObjectives(player);
    }

    @SuppressWarnings("unchecked")
    void removeTeams(EntityPlayerMP player) {
        for (ScorePlayerTeam team: this.getTeams()) {
            player.connection.sendPacket(new SPacketTeams(team, 1));
        }
    }

    @SuppressWarnings("unchecked")
    void removeObjectives(EntityPlayerMP player) {
        for (ScoreObjective objective: this.getScoreObjectives()) {
            player.connection.sendPacket(new SPacketScoreboardObjective(objective, 1));
        }
    }

    @Redirect(method = "onScoreUpdated", at = @At(value = "INVOKE", target = SEND_PACKET_METHOD))
    public void onUpdateScoreValue(PlayerList manager, Packet<?> packet) {
        this.sendToPlayers(packet);
    }

    @Redirect(method = "onScoreUpdated", at = @At(value = "INVOKE", target = SET_CONTAINS, remap = false))
    public boolean onUpdateScoreValue(Set<?> set, Object object) {
        return true;
    }

    @Redirect(method = "broadcastScoreUpdate(Ljava/lang/String;)V",
            at = @At(value = "INVOKE", target = SEND_PACKET_METHOD))
    public void onRemoveScore(PlayerList manager, Packet<?> packet) {
        this.sendToPlayers(packet);
    }

    @Redirect(method = "broadcastScoreUpdate(Ljava/lang/String;Lnet/minecraft/scoreboard/ScoreObjective;)V",
            at = @At(value = "INVOKE", target = SEND_PACKET_METHOD))
    public void onRemoveScoreForObjective(PlayerList manager, Packet<?> packet) {
        this.sendToPlayers(packet);
    }

    //@Redirect(method = "setObjectiveInDisplaySlot", at = @At(value = "INVOKE", target = SEND_PACKET_METHOD))
    /*public void onSetObjectiveInDisplaySlot(PlayerList manager, Packet<?> packet) {
        this.sendToPlayers(packet);
    }*/

    @Redirect(method = "addPlayerToTeam", at = @At(value = "INVOKE", target = SEND_PACKET_METHOD))
    public void onAddPlayerToTeam(PlayerList manager, Packet<?> packet) {
        this.sendToPlayers(packet);
    }

    @Redirect(method = "removePlayerFromTeam", at = @At(value = "INVOKE", target = SEND_PACKET_METHOD))
    public void onRemovePlayerFromTeam(PlayerList manager, Packet<?> packet) {
        this.sendToPlayers(packet);
    }

    @Redirect(method = "onObjectiveDisplayNameChanged", at = @At(value = "INVOKE", target = SEND_PACKET_METHOD))
    public void onUpdateObjective(PlayerList manager, Packet<?> packet) {
        this.sendToPlayers(packet);
    }

    @Redirect(method = "onObjectiveDisplayNameChanged", at = @At(value = "INVOKE", target = SET_CONTAINS, remap = false))
    public boolean onUpdateObjective(Set<ScoreObjective> set, Object object) {
        return true;
    }

    @Redirect(method = "onScoreObjectiveRemoved", at = @At(value = "INVOKE", target = SET_CONTAINS, remap = false))
    public boolean onSendDisplayPackets(Set<ScoreObjective> set, Object object) {
        return true;
    }

    @Redirect(method = "broadcastTeamCreated", at = @At(value = "INVOKE", target = SEND_PACKET_METHOD))
    public void onBroadcastTeamCreated(PlayerList manager, Packet<?> packet) {
        this.sendToPlayers(packet);
    }

    @Redirect(method = "broadcastTeamInfoUpdate", at = @At(value = "INVOKE", target = SEND_PACKET_METHOD))
    public void onSendTeamUpdate(PlayerList manager, Packet<?> packet) {
        this.sendToPlayers(packet);
    }

    @Redirect(method = "broadcastTeamRemove", at = @At(value = "INVOKE", target = SEND_PACKET_METHOD))
    public void onRemoveTeam(PlayerList manager, Packet<?> packet) {
        this.sendToPlayers(packet);
    }

    @SuppressWarnings("rawtypes")
    @Redirect(method = "addObjective", at = @At(value = "INVOKE", target = "Ljava/util/List;iterator()Ljava/util/Iterator;", ordinal = 0, remap =
            false))
    public Iterator onGetPlayerIteratorForObjectives(List list) {
        return this.players.iterator();
    }

    @SuppressWarnings("rawtypes")
    @Redirect(method = "sendDisplaySlotRemovalPackets", at = @At(value = "INVOKE", target = "Ljava/util/List;iterator()Ljava/util/Iterator;",
            ordinal = 0, remap = false))
    public Iterator onGetPlayerIterator(List list) {
        return this.players.iterator();
    }
}
