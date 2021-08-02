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
package org.spongepowered.common.mixin.api.minecraft.world.scores;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import org.spongepowered.api.scoreboard.Team;
import org.spongepowered.api.scoreboard.Visibility;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Interface.Remap;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.world.scores.PlayerTeamBridge;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

@Mixin(PlayerTeam.class)
@Implements(@Interface(iface = Team.class, prefix = "team$", remap = Remap.NONE))
public abstract class PlayerTeamMixin_API implements Team {

    // @formatter:off
    @Nullable @Shadow @Final @Mutable private Scoreboard scoreboard;
    @Shadow @Final private String name;
    @Shadow @Final private Set<String> players;

    @Shadow public abstract void shadow$setAllowFriendlyFire(boolean friendlyFire);
    @Shadow public abstract void shadow$setSeeFriendlyInvisibles(boolean friendlyInvisibles);
    @Shadow public abstract void shadow$setNameTagVisibility(net.minecraft.world.scores.Team.Visibility visibility);
    @Shadow public abstract void shadow$setDeathMessageVisibility(net.minecraft.world.scores.Team.Visibility visibility);
    @Shadow public abstract void shadow$setCollisionRule(net.minecraft.world.scores.Team.CollisionRule rule);
    @Shadow public abstract void shadow$setDisplayName(net.minecraft.network.chat.Component text);
    @Shadow public abstract boolean shadow$isAllowFriendlyFire();
    @Shadow public abstract boolean shadow$canSeeFriendlyInvisibles();
    @Shadow public abstract net.minecraft.world.scores.Team.Visibility shadow$getNameTagVisibility();
    @Shadow public abstract net.minecraft.world.scores.Team.Visibility shadow$getDeathMessageVisibility();
    @Shadow public abstract net.minecraft.world.scores.Team.CollisionRule shadow$getCollisionRule();
    @Shadow public abstract Collection<String> shadow$getPlayers();
    // @formatter:on

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public Component displayName() {
        return ((PlayerTeamBridge) this).bridge$getDisplayName();
    }

    @Override
    public void setDisplayName(final Component text) {
        ((PlayerTeamBridge) this).bridge$setDisplayName(text);
    }

    @Override
    public NamedTextColor color() {
        return ((PlayerTeamBridge) this).bridge$getColor();
    }

    @Override
    public void setColor(final NamedTextColor color) {
        ((PlayerTeamBridge) this).bridge$setColor(color);
    }

    @Override
    public Component prefix() {
        return ((PlayerTeamBridge) this).bridge$getPrefix();
    }

    @Override
    public void setPrefix(final Component prefix) {
        ((PlayerTeamBridge) this).bridge$setPrefix(prefix);
    }

    @Override
    public Component suffix() {
        return ((PlayerTeamBridge) this).bridge$getSuffix();
    }

    @Override
    public void setSuffix(final Component suffix) {
        ((PlayerTeamBridge) this).bridge$setSuffix(suffix);
    }

    @Override
    public boolean allowFriendlyFire() {
        return this.shadow$isAllowFriendlyFire();
    }

    @Intrinsic
    public void team$setAllowFriendlyFire(final boolean allowFriendlyFire) {
        this.shadow$setAllowFriendlyFire(allowFriendlyFire);
    }

    @Intrinsic
    public boolean team$canSeeFriendlyInvisibles() {
        return this.shadow$canSeeFriendlyInvisibles();
    }

    @Override
    public void setCanSeeFriendlyInvisibles(final boolean enabled) {
        this.shadow$setSeeFriendlyInvisibles(enabled);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public Visibility nameTagVisibility() {
        return (Visibility) (Object) this.shadow$getNameTagVisibility();
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void setNameTagVisibility(final Visibility visibility) {
        this.shadow$setNameTagVisibility((net.minecraft.world.scores.Team.Visibility) (Object) visibility);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public Visibility deathMessageVisibility() {
        return (Visibility) (Object) this.shadow$getDeathMessageVisibility();
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void setDeathMessageVisibility(final Visibility visibility) {
        this.shadow$setDeathMessageVisibility((net.minecraft.world.scores.Team.Visibility) (Object) visibility);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public org.spongepowered.api.scoreboard.CollisionRule collisionRule() {
        return (org.spongepowered.api.scoreboard.CollisionRule) (Object) this.shadow$getCollisionRule();
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void setCollisionRule(final org.spongepowered.api.scoreboard.CollisionRule rule) {
        this.shadow$setCollisionRule((net.minecraft.world.scores.Team.CollisionRule) (Object) rule);
    }

    @Override
    public Set<Component> members() {
        final LegacyComponentSerializer lcs = LegacyComponentSerializer.legacySection();
        return this.shadow$getPlayers().stream().map(lcs::deserialize).collect(Collectors.toSet());
    }

    @Override
    public void addMember(final Component member) {
        final String legacyName = LegacyComponentSerializer.legacySection().serialize(member);
        if (legacyName.length() > 40) {
            throw new IllegalArgumentException(String.format("Member is %s characters long! It must be at most 40.", legacyName.length()));
        }
        if (this.scoreboard != null) {
            this.scoreboard.addPlayerToTeam(legacyName, (PlayerTeam) (Object) this);
        } else {
            this.players.add(legacyName); // this is normally done by addPlayerToTeam
        }
    }

    @SuppressWarnings("RedundantCast")
    @Override
    public boolean removeMember(final Component member) {
        final String legacyName = LegacyComponentSerializer.legacySection().serialize(member);
        if (this.scoreboard != null) {
            final PlayerTeam realTeam = this.scoreboard.getPlayersTeam(legacyName);

            if (realTeam == (PlayerTeam) (Object) this) {
                this.scoreboard.removePlayerFromTeam(legacyName, realTeam);
                return true;
            }
            return false;
        }
        return this.players.remove(legacyName);
    }

    @Override
    public Optional<org.spongepowered.api.scoreboard.Scoreboard> scoreboard() {
        return Optional.ofNullable((org.spongepowered.api.scoreboard.Scoreboard) this.scoreboard);
    }

    @Override
    public boolean unregister() {
        if (this.scoreboard == null) {
            return false;
        }
        this.scoreboard.removePlayerTeam((PlayerTeam) (Object) this);
        this.scoreboard = null;
        return true;
    }

}
