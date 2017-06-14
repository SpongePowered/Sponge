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
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.text.TextFormatting;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.scoreboard.Team;
import org.spongepowered.api.scoreboard.Visibility;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.interfaces.IMixinTeam;
import org.spongepowered.common.registry.type.text.TextColorRegistryModule;
import org.spongepowered.common.text.SpongeTexts;
import org.spongepowered.common.text.format.SpongeTextColor;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Mixin(ScorePlayerTeam.class)
@Implements(@Interface(iface = Team.class, prefix = "team$"))
public abstract class MixinScorePlayerTeam extends net.minecraft.scoreboard.Team implements IMixinTeam {

    @Shadow public Scoreboard scoreboard;
    @Shadow public String name;
    @Shadow public Set<String> membershipSet;
    @Shadow public String displayName;
    @Shadow public TextFormatting color;
    @Shadow public String prefix;
    @Shadow public String suffix;
    @Shadow public boolean allowFriendlyFire;
    @Shadow public boolean canSeeFriendlyInvisibles;
    @Shadow public net.minecraft.scoreboard.Team.EnumVisible nameTagVisibility;
    @Shadow public net.minecraft.scoreboard.Team.EnumVisible deathMessageVisibility;
    @Shadow public net.minecraft.scoreboard.Team.CollisionRule collisionRule;

    @Shadow public abstract void setAllowFriendlyFire(boolean friendlyFire);

    private Text spongeDisplayName;
    private Text spongePrefix;
    private Text spongeSuffix;
    private TextColor spongeColor;

    private static final String TEAM_UPDATE_SIGNATURE = "Lnet/minecraft/scoreboard/Scoreboard;broadcastTeamInfoUpdate(Lnet/minecraft/scoreboard/ScorePlayerTeam;)V";

    // Minecraft doesn't do a null check on scoreboard, so we redirect
    // the call and do it ourselves.
    private void doTeamUpdate() {
        if (this.scoreboard != null) {
            this.scoreboard.broadcastTeamInfoUpdate((ScorePlayerTeam) (Object) this);
        }
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    public void onInit(CallbackInfo ci) {
        // Initialize cached values
        this.spongeDisplayName = SpongeTexts.fromLegacy(this.displayName);
        this.spongePrefix = SpongeTexts.fromLegacy(this.prefix);
        this.spongeSuffix = SpongeTexts.fromLegacy(this.suffix);
        this.spongeColor = TextColorRegistryModule.enumChatColor.get(this.color);
    }

    public String team$getName() {
        return this.name;
    }

    public Text team$getDisplayName() {
        return this.spongeDisplayName;
    }

    public void team$setDisplayName(Text text) {
        String newText = SpongeTexts.toLegacy(text);
        if (newText.length() > 32) {
            throw new IllegalArgumentException(String.format("Display name is %s characters long! It must be at most 32.", newText.length()));
        }
        this.spongeDisplayName = text;
        this.displayName = newText;
        this.doTeamUpdate();
    }

    public TextColor team$getColor() {
        return this.spongeColor;
    }

    public void team$setColor(TextColor color) {
        this.spongeColor = color;
        this.color = ((SpongeTextColor) color).getHandle();
        this.doTeamUpdate();
    }

    public Text team$getPrefix() {
        return this.spongePrefix;
    }

    public void team$setPrefix(Text prefix) {
        String newPrefix = SpongeTexts.toLegacy(prefix);
        if (newPrefix.length() > 16) {
            throw new IllegalArgumentException(String.format("Prefix is %s characters long! It must be at most 16.", newPrefix.length()));
        }
        this.spongePrefix = prefix;
        this.prefix = newPrefix;
        this.doTeamUpdate();
    }

    public Text team$getSuffix() {
        return this.spongeSuffix;
    }

    public void team$setSuffix(Text suffix) {
        String newSuffix = SpongeTexts.toLegacy(suffix);
        if (newSuffix.length() > 16) {
            throw new IllegalArgumentException(String.format("Suffix is %s characters long! It must be at most 16.", newSuffix.length()));
        }
        this.spongeSuffix = suffix;
        this.suffix = newSuffix;
        this.doTeamUpdate();
    }

    public boolean team$allowFriendlyFire() {
        return this.allowFriendlyFire;
    }

    @Intrinsic
    public void team$setAllowFriendlyFire(boolean allowFriendlyFire) {
        this.setAllowFriendlyFire(allowFriendlyFire);
    }

    public boolean team$canSeeFriendlyInvisibles() {
        return this.canSeeFriendlyInvisibles;
    }

    public void team$setCanSeeFriendlyInvisibles(boolean enabled) {
        this.canSeeFriendlyInvisibles = enabled;
        this.doTeamUpdate();
    }

    public Visibility team$getNameTagVisibility() {
        return (Visibility) (Object) this.nameTagVisibility;
    }

    public void team$setNameTagVisibility(Visibility visibility) {
        this.nameTagVisibility = (EnumVisible) (Object) visibility;
        this.doTeamUpdate();
    }

    public Visibility team$getDeathMessageVisibility() {
        return (Visibility) (Object) this.deathMessageVisibility;
    }

    public void team$setDeathMessageVisibility(Visibility visibility) {
        this.deathMessageVisibility= (EnumVisible) (Object) visibility;
        this.doTeamUpdate();
    }

    public org.spongepowered.api.scoreboard.CollisionRule team$getCollisionRule() {
        return (org.spongepowered.api.scoreboard.CollisionRule) (Object) this.collisionRule;
    }

    public void team$setCollisionRule(org.spongepowered.api.scoreboard.CollisionRule rule) {
        this.collisionRule = (CollisionRule) (Object) rule;
        this.doTeamUpdate();
    }

    public Set<Text> team$getMembers() {
        return this.membershipSet.stream().map(SpongeTexts::fromLegacy).collect(Collectors.toSet());
    }

    public void team$addMember(Text member) {
        String legacyName = SpongeTexts.toLegacy(member);
        if (legacyName.length() > 40) {
            throw new IllegalArgumentException(String.format("Member is %s characters long! It must be at most 40.", legacyName.length()));
        }
        if (this.scoreboard != null) {
            this.scoreboard.addPlayerToTeam(legacyName, this.name);
        } else {
            this.membershipSet.add(legacyName); // this is normally done by addPlayerToTeam
        }
    }

    public boolean team$removeMember(Text member) {
        String legacyName = SpongeTexts.toLegacy(member);
        if (this.scoreboard != null) {
            ScorePlayerTeam realTeam = this.scoreboard.getPlayersTeam(legacyName);

            if (realTeam == (ScorePlayerTeam) (Object) this) {
                this.scoreboard.removePlayerFromTeam(legacyName, realTeam);
                return true;
            }
            return false;
        }
        return this.membershipSet.remove(legacyName);
    }

    @SuppressWarnings("ConstantConditions")
    public Optional<org.spongepowered.api.scoreboard.Scoreboard> team$getScoreboard() {
        return Optional.ofNullable((org.spongepowered.api.scoreboard.Scoreboard) this.scoreboard);
    }

    public boolean team$unregister() {
        if (this.scoreboard == null) {
            return false;
        }
        this.scoreboard.removeTeam((ScorePlayerTeam) (Object) this);
        this.scoreboard = null;
        return true;
    }

    @Redirect(method = "setDisplayName", at = @At(value = "INVOKE", target = TEAM_UPDATE_SIGNATURE))
    private void onSetTeamName(Scoreboard scoreboard, ScorePlayerTeam team) {
        this.doTeamUpdate();
    }

    @Inject(method = "setDisplayName", at = @At("HEAD"))
    public void onSetTeamName(String name, CallbackInfo ci) {
        if (name != null) {
            this.spongeDisplayName = SpongeTexts.fromLegacy(name);
        }
    }

    @Redirect(method = "setPrefix", at = @At(value = "INVOKE", target = TEAM_UPDATE_SIGNATURE))
    private void onSetNamePrefix(Scoreboard scoreboard, ScorePlayerTeam team) {
        this.doTeamUpdate();
    }

    @Inject(method = "setPrefix", at = @At("HEAD"))
    public void onSetNamePrefix(String prefix, CallbackInfo ci) {
        if (prefix != null) {
            this.spongePrefix = SpongeTexts.fromLegacy(prefix);
        }
    }

    @Redirect(method = "setSuffix", at = @At(value = "INVOKE", target = TEAM_UPDATE_SIGNATURE))
    private void onSetNameSuffix(Scoreboard scoreboard, ScorePlayerTeam team) {
        this.doTeamUpdate();
    }

    @Inject(method = "setSuffix", at = @At("HEAD"))
    public void onSetNameSuffix(String suffix, CallbackInfo ci) {
        if (suffix != null) {
            this.spongeSuffix = SpongeTexts.fromLegacy(suffix);
        }
    }

    @Redirect(method = "setAllowFriendlyFire", at = @At(value = "INVOKE", target = TEAM_UPDATE_SIGNATURE))
    private void onSetAllowFriendlyFire(Scoreboard scoreboard, ScorePlayerTeam team) {
        this.doTeamUpdate();
    }

    @Redirect(method = "setSeeFriendlyInvisiblesEnabled", at = @At(value = "INVOKE", target = TEAM_UPDATE_SIGNATURE))
    private void onSetSeeFriendlyInvisiblesEnabled(Scoreboard scoreboard, ScorePlayerTeam team) {
        this.doTeamUpdate();
    }

    @Redirect(method = "setNameTagVisibility", at = @At(value = "INVOKE", target = TEAM_UPDATE_SIGNATURE))
    private void onSetNameTagVisibility(Scoreboard scoreboard, ScorePlayerTeam team) {
        this.doTeamUpdate();
    }

    @Redirect(method = "setDeathMessageVisibility", at = @At(value = "INVOKE", target = TEAM_UPDATE_SIGNATURE))
    private void onSetDeathMessageVisibility(Scoreboard scoreboard, ScorePlayerTeam team) {
        this.doTeamUpdate();
    }

    @Inject(method = "setColor", at = @At("RETURN"))
    private void onSetChatFormat(TextFormatting format, CallbackInfo ci) {
        this.spongeColor = TextColorRegistryModule.enumChatColor.get(format);
        // This isn't called by Vanilla, so we inject the call ourselves.
        this.doTeamUpdate();
    }

    public MessageChannel getChannel() {
        return MessageChannel.fixed(this.getMembershipCollection().stream()
                .map(name -> Sponge.getGame().getServer().getPlayer(name))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet()));
    }

    @Override
    public MessageChannel getTeamChannel(EntityPlayerMP player) {
        return MessageChannel.fixed(this.getMembershipCollection().stream()
                .map(name -> Sponge.getGame().getServer().getPlayer(name))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(member -> member != player)
                .collect(Collectors.toSet()));
    }

    @Override
    public MessageChannel getNonTeamChannel() {
        return MessageChannel.fixed(Sponge.getGame().getServer().getOnlinePlayers().stream()
                .filter(player -> ((EntityPlayerMP) player).getTeam() != this)
                .collect(Collectors.toSet()));
    }
}
