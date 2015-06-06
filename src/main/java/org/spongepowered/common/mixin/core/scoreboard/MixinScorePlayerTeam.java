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

import com.google.common.base.Optional;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.EnumChatFormatting;
import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.entity.player.User;
import org.spongepowered.api.scoreboard.Team;
import org.spongepowered.api.scoreboard.Visibility;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.ChunkManager;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.Sponge;
import org.spongepowered.common.interfaces.IMixinScoreboard;
import org.spongepowered.common.interfaces.IMixinTeam;
import org.spongepowered.common.registry.SpongeGameRegistry;
import org.spongepowered.common.scoreboard.SpongeTeam;
import org.spongepowered.common.scoreboard.SpongeVisibility;
import org.spongepowered.common.text.format.SpongeTextColor;

import java.util.HashSet;
import java.util.Set;

@NonnullByDefault
@Mixin(ScorePlayerTeam.class)
public abstract class MixinScorePlayerTeam extends MixinTeam implements IMixinTeam {

    @Shadow public Scoreboard theScoreboard;
    @Shadow public String registeredName;
    @Shadow public String teamNameSPT;
    @Shadow public EnumChatFormatting chatFormat;
    @Shadow public String namePrefixSPT;
    @Shadow public String colorSuffix;
    @Shadow public boolean allowFriendlyFire;
    @Shadow public boolean canSeeFriendlyInvisibles;
    @Shadow public net.minecraft.scoreboard.Team.EnumVisible field_178778_i; // nameTagVisibility
    @Shadow public net.minecraft.scoreboard.Team.EnumVisible field_178776_j; // deathMessageVisiblity
    @Shadow public Set<String> membershipSet;

    private SpongeTeam spongeTeam;

    @Override
    public SpongeTeam getSpongeTeam() {
        return spongeTeam;
    }

    @Override
    public void setSpongeTeam(SpongeTeam team) {
        this.spongeTeam = team;
    }

    @Inject(method = "setTeamName", at = @At("HEAD"), cancellable = true)
    public void onSetTeamName(String name, CallbackInfo ci) {
        if (this.shouldEcho()) {
            this.spongeTeam.allowRecursion = false;
            this.spongeTeam.setDisplayName(Texts.fromLegacy(name));
            this.spongeTeam.allowRecursion = true;
            ci.cancel();
        }
    }

    @Inject(method = "setNamePrefix", at = @At("HEAD"), cancellable = true)
    public void onSetNamePrefix(String prefix, CallbackInfo ci) {
        if (this.shouldEcho()) {
            this.spongeTeam.allowRecursion = false;
            this.spongeTeam.setPrefix(Texts.fromLegacy(prefix));
            this.spongeTeam.allowRecursion = true;
            ci.cancel();
        }
    }

    @Inject(method = "setNameSuffix", at = @At("HEAD"), cancellable = true)
    public void onSetNameSuffix(String suffix, CallbackInfo ci) {
        if (this.shouldEcho()) {
            this.spongeTeam.allowRecursion = false;
            this.spongeTeam.setSuffix(Texts.fromLegacy(suffix));
            this.spongeTeam.allowRecursion = true;
            ci.cancel();
        }
    }

    @Inject(method = "setAllowFriendlyFire", at = @At("HEAD"), cancellable = true)
    public void onSetAllowFriendlyFire(boolean allowFriendlyFire, CallbackInfo ci) {
        if (this.shouldEcho()) {
            this.spongeTeam.allowRecursion = false;
            this.spongeTeam.setAllowFriendlyFire(allowFriendlyFire);
            this.spongeTeam.allowRecursion = true;
            ci.cancel();
        }
    }

    @Inject(method = "setSeeFriendlyInvisiblesEnabled", at = @At("HEAD"), cancellable = true)
    public void onSetSeeFriendlyInvisiblesEnable(boolean canSeeFriendlyInvisibles, CallbackInfo ci) {
        if (this.shouldEcho()) {
            this.spongeTeam.allowRecursion = false;
            this.spongeTeam.setCanSeeFriendlyInvisibles(canSeeFriendlyInvisibles);
            this.spongeTeam.allowRecursion = true;
            ci.cancel();
        }
    }

    @Inject(method = "func_178772_a", at = @At("HEAD"), cancellable = true)
    public void setNametagVisibility(net.minecraft.scoreboard.Team.EnumVisible visibility, CallbackInfo ci) {
        if (this.shouldEcho()) {
            this.spongeTeam.allowRecursion = false;
            this.spongeTeam.setNameTagVisibility(SpongeGameRegistry.enumVisible.get(visibility));
            this.spongeTeam.allowRecursion = true;
            ci.cancel();
        }
    }

    @Inject(method = "func_178773_b", at = @At("HEAD"), cancellable = true)
    public void setDeathMessageVisibility(net.minecraft.scoreboard.Team.EnumVisible visibility, CallbackInfo ci) {
        if (this.shouldEcho()) {
            this.spongeTeam.allowRecursion = false;
            this.spongeTeam.setDeathTextVisibility(SpongeGameRegistry.enumVisible.get(visibility));
            this.spongeTeam.allowRecursion = true;
            ci.cancel();
        }
    }

    @Inject(method = "setChatFormat", at = @At("HEAD"), cancellable = true)
    public void onSetChatFormat(EnumChatFormatting formatting, CallbackInfo ci) {
        if (this.shouldEcho()) {
            this.spongeTeam.allowRecursion = false;
            this.spongeTeam.setColor(((SpongeGameRegistry) Sponge.getGame().getRegistry()).enumChatColor.get(formatting));
            this.spongeTeam.allowRecursion = true;
            ci.cancel();
        }
    }

    @Inject(method = "setChatFormat", at = @At("RETURN"))
    public void onSetChatFormatEnd(EnumChatFormatting formatting, CallbackInfo ci) {
        // In vanilla, this is always set in conjunction with the prefix and suffix,
        // so the packet is only sent in the prefix and suffix setters. The color
        // can be set independently with Sponge, so it's necessary to send the packet
        // here.
        this.theScoreboard.sendTeamUpdate((ScorePlayerTeam) (Object) this);
    }

    @Override
    public boolean isSameTeam(net.minecraft.scoreboard.Team other) {
        return ((IMixinTeam) other).getSpongeTeam() == this.spongeTeam;
    }

    private boolean shouldEcho() {
        return (((IMixinScoreboard) theScoreboard).echoToSponge() || this.spongeTeam.getScoreboards().size() == 1) && this.spongeTeam.allowRecursion;
    }
}
