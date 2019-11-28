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
package org.spongepowered.common.mixin.api.mcp.scoreboard;

import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.text.TextFormatting;
import org.spongepowered.api.scoreboard.Team;
import org.spongepowered.api.scoreboard.Visibility;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.scoreboard.ScorePlayerTeamBridge;
import org.spongepowered.common.bridge.scoreboard.TeamBridge;
import org.spongepowered.common.text.SpongeTexts;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

@Mixin(ScorePlayerTeam.class)
@Implements(@Interface(iface = Team.class, prefix = "team$"))
public abstract class ScorePlayerTeamMixin_API implements Team {

    @Nullable @Shadow @Final @Mutable private Scoreboard scoreboard;
    @Shadow @Final private String name;
    @Shadow @Final private Set<String> membershipSet;

    @Shadow public abstract void shadow$setAllowFriendlyFire(boolean friendlyFire);
    @Shadow public abstract void setSeeFriendlyInvisiblesEnabled(boolean friendlyInvisibles);
    @Shadow public abstract void shadow$setNameTagVisibility(net.minecraft.scoreboard.Team.EnumVisible visibility);
    @Shadow public abstract void shadow$setDeathMessageVisibility(net.minecraft.scoreboard.Team.EnumVisible visibility);
    @Shadow public abstract void shadow$setCollisionRule(net.minecraft.scoreboard.Team.CollisionRule rule);
    @Shadow public abstract void setDisplayName(String name);
    @Shadow public abstract boolean shadow$getAllowFriendlyFire();
    @Shadow public abstract boolean getSeeFriendlyInvisiblesEnabled();
    @Shadow public abstract net.minecraft.scoreboard.Team.EnumVisible shadow$getNameTagVisibility();
    @Shadow public abstract net.minecraft.scoreboard.Team.EnumVisible shadow$getDeathMessageVisibility();
    @Shadow public abstract net.minecraft.scoreboard.Team.CollisionRule shadow$getCollisionRule();
    @Shadow public abstract Collection<String> getMembershipCollection();

    @Intrinsic
    public String team$getName() {
        return this.name;
    }

    @Override
    public Text getDisplayName() {
        return ((ScorePlayerTeamBridge) this).bridge$getDisplayName();
    }

    @Override
    public void setDisplayName(final Text text) {
        ((ScorePlayerTeamBridge) this).bridge$setDisplayName(text);
    }

    @Override
    public TextColor getColor() {
        return ((TeamBridge) this).bridge$getColor();
    }

    @Override
    public void setColor(final TextColor color) {
        ((ScorePlayerTeamBridge) this).bridge$setColor(color);
    }

    @Override
    public Text getPrefix() {
        return ((ScorePlayerTeamBridge) this).bridge$getPrefix();
    }

    @Override
    public void setPrefix(final Text prefix) {
        ((ScorePlayerTeamBridge) this).bridge$setPrefix(prefix);
    }

    @Override
    public Text getSuffix() {
        return ((ScorePlayerTeamBridge) this).bridge$getSuffix();
    }

    @Shadow public abstract void setColor(TextFormatting color);

    @Override
    public void setSuffix(final Text suffix) {
        ((ScorePlayerTeamBridge) this).bridge$setSuffix(suffix);
    }

    @Override
    public boolean allowFriendlyFire() {
        return this.shadow$getAllowFriendlyFire();
    }

    @Intrinsic
    public void team$setAllowFriendlyFire(final boolean allowFriendlyFire) {
        this.shadow$setAllowFriendlyFire(allowFriendlyFire);
    }

    @Override
    public boolean canSeeFriendlyInvisibles() {
        return this.getSeeFriendlyInvisiblesEnabled();
    }

    @Override
    public void setCanSeeFriendlyInvisibles(final boolean enabled) {
        this.setSeeFriendlyInvisiblesEnabled(enabled);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public Visibility getNameTagVisibility() {
        return (Visibility) (Object) this.shadow$getNameTagVisibility();
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void setNameTagVisibility(final Visibility visibility) {
        this.shadow$setNameTagVisibility((net.minecraft.scoreboard.Team.EnumVisible) (Object) visibility);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public Visibility getDeathMessageVisibility() {
        return (Visibility) (Object) this.shadow$getDeathMessageVisibility();
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void setDeathMessageVisibility(final Visibility visibility) {
        this.shadow$setDeathMessageVisibility((net.minecraft.scoreboard.Team.EnumVisible) (Object) visibility);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public org.spongepowered.api.scoreboard.CollisionRule getCollisionRule() {
        return (org.spongepowered.api.scoreboard.CollisionRule) (Object) this.shadow$getCollisionRule();
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void setCollisionRule(final org.spongepowered.api.scoreboard.CollisionRule rule) {
        this.shadow$setCollisionRule((net.minecraft.scoreboard.Team.CollisionRule) (Object) rule);
    }

    @Override
    public Set<Text> getMembers() {
        return this.getMembershipCollection().stream().map(SpongeTexts::fromLegacy).collect(Collectors.toSet());
    }

    @Override
    public void addMember(final Text member) {
        final String legacyName = SpongeTexts.toLegacy(member);
        if (legacyName.length() > 40) {
            throw new IllegalArgumentException(String.format("Member is %s characters long! It must be at most 40.", legacyName.length()));
        }
        if (this.scoreboard != null) {
            this.scoreboard.func_151392_a(legacyName, this.name);
        } else {
            this.membershipSet.add(legacyName); // this is normally done by addPlayerToTeam
        }
    }

    @SuppressWarnings("RedundantCast")
    @Override
    public boolean removeMember(final Text member) {
        final String legacyName = SpongeTexts.toLegacy(member);
        if (this.scoreboard != null) {
            final ScorePlayerTeam realTeam = this.scoreboard.func_96509_i(legacyName);

            if (realTeam == (ScorePlayerTeam) (Object) this) {
                this.scoreboard.func_96512_b(legacyName, realTeam);
                return true;
            }
            return false;
        }
        return this.membershipSet.remove(legacyName);
    }

    @Override
    public Optional<org.spongepowered.api.scoreboard.Scoreboard> getScoreboard() {
        return Optional.ofNullable((org.spongepowered.api.scoreboard.Scoreboard) this.scoreboard);
    }

    @Override
    public boolean unregister() {
        if (this.scoreboard == null) {
            return false;
        }
        this.scoreboard.func_96511_d((ScorePlayerTeam) (Object) this);
        this.scoreboard = null;
        return true;
    }

}
