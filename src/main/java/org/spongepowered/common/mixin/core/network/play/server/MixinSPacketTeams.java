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
package org.spongepowered.common.mixin.core.network.play.server;

import com.google.common.collect.Lists;
import net.minecraft.network.play.server.SPacketTeams;
import org.spongepowered.api.Server;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.entity.context.store.PlayerContextStore;
import org.spongepowered.common.interfaces.entity.IMixinEntityContext;
import org.spongepowered.common.interfaces.network.play.server.IMixinSPacketTeams;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

@Mixin(SPacketTeams.class)
public class MixinSPacketTeams implements IMixinSPacketTeams {

    @Shadow private String name = "";
    @Shadow private String displayName = "";
    @Shadow private String prefix = "";
    @Shadow private String suffix = "";
    @Shadow private String nameTagVisibility;
    @Shadow private String collisionRule;
    @Shadow private int color;
    @Final @Shadow private Collection<String> players;
    @Shadow private int action;
    @Shadow private int friendlyFlags;

    @Override
    public SPacketTeams translate(final Server server, final Player viewer) {
        if (this.players.size() == 1) {
            @Nullable final Player target = server.getPlayer(this.players.iterator().next(), viewer).orElse(null);
            @Nullable final String fake;
            if (target != null && (fake = ((PlayerContextStore) ((IMixinEntityContext) target).getContextStore()).getFakeName(viewer)) != null) {
                return this.create(Collections.singleton(fake));
            }
        } else if (this.players.size() > 1) {
            @Nullable List<String> players = null;
            for (final String player : this.players) {
                @Nullable final Player target = server.getPlayer(player, viewer).orElse(null);
                @Nullable final String fake;
                if (target != null && (fake = ((PlayerContextStore) ((IMixinEntityContext) target).getContextStore()).getFakeName(viewer)) != null) {
                    if (players == null) {
                        players = this.copyListWithPreviousExcluding(player);
                    }

                    players.add(fake);
                } else if (players != null) {
                    players.add(player);
                }
            }

            if (players != null) {
                return this.create(players);
            }
        }

        return (SPacketTeams) (Object) this;
    }

    private List<String> copyListWithPreviousExcluding(final String excluded) {
        final List<String> players = Lists.newArrayListWithExpectedSize(this.players.size());
        for (final String player : this.players) {
            if (player.equals(excluded)) {
                break;
            }

            players.add(player);
        }

        return players;
    }

    private SPacketTeams create(Collection<String> players) {
        final SPacketTeams packet = new SPacketTeams();
        packet.name = this.name;
        packet.displayName = this.displayName;
        packet.prefix = this.prefix;
        packet.suffix = this.suffix;
        packet.nameTagVisibility = this.nameTagVisibility;
        packet.collisionRule = this.collisionRule;
        packet.color = this.color;
        packet.players = players;
        packet.action = this.action;
        packet.friendlyFlags = this.friendlyFlags;
        return packet;
    }

}
