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
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumChatFormatting;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.sink.MessageSink;
import org.spongepowered.api.text.sink.MessageSinks;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.util.command.CommandSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.interfaces.IMixinScoreboard;
import org.spongepowered.common.interfaces.IMixinTeam;
import org.spongepowered.common.registry.SpongeGameRegistry;
import org.spongepowered.common.scoreboard.SpongeTeam;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
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
    @Shadow public Team.EnumVisible field_178778_i; // nameTagVisibility
    @Shadow public Team.EnumVisible field_178776_j; // deathMessageVisiblity
    @Shadow public Set<String> membershipSet;
    @Shadow public abstract Collection getMembershipCollection();

    private SpongeTeam spongeTeam;

    @Override
    public SpongeTeam getSpongeTeam() {
        return this.spongeTeam;
    }

    @Override
    public void setSpongeTeam(SpongeTeam team) {
        this.spongeTeam = team;
    }

    @SuppressWarnings("deprecation")
    @Inject(method = "setTeamName", at = @At("HEAD"), cancellable = true)
    public void onSetTeamName(String name, CallbackInfo ci) {
        if (this.shouldEcho()) {
            this.spongeTeam.allowRecursion = false;
            this.spongeTeam.setDisplayName(Texts.legacy().fromUnchecked(name));
            this.spongeTeam.allowRecursion = true;
            ci.cancel();
        }
    }

    @SuppressWarnings("deprecation")
    @Inject(method = "setNamePrefix", at = @At("HEAD"), cancellable = true)
    public void onSetNamePrefix(String prefix, CallbackInfo ci) {
        if (this.shouldEcho()) {
            this.spongeTeam.allowRecursion = false;
            this.spongeTeam.setPrefix(Texts.legacy().fromUnchecked(prefix));
            this.spongeTeam.allowRecursion = true;
            ci.cancel();
        }
    }

    @SuppressWarnings("deprecation")
    @Inject(method = "setNameSuffix", at = @At("HEAD"), cancellable = true)
    public void onSetNameSuffix(String suffix, CallbackInfo ci) {
        if (this.shouldEcho()) {
            this.spongeTeam.allowRecursion = false;
            this.spongeTeam.setSuffix(Texts.legacy().fromUnchecked(suffix));
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
    public void setNametagVisibility(Team.EnumVisible visibility, CallbackInfo ci) {
        if (this.shouldEcho()) {
            this.spongeTeam.allowRecursion = false;
            this.spongeTeam.setNameTagVisibility(SpongeGameRegistry.enumVisible.get(visibility));
            this.spongeTeam.allowRecursion = true;
            ci.cancel();
        }
    }

    @Inject(method = "func_178773_b", at = @At("HEAD"), cancellable = true)
    public void setDeathMessageVisibility(Team.EnumVisible visibility, CallbackInfo ci) {
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
            this.spongeTeam.setColor(SpongeGameRegistry.enumChatColor.get(formatting));
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
    public boolean isSameTeam(Team other) {
        return ((IMixinTeam) other).getSpongeTeam() == this.spongeTeam;
    }

    private boolean shouldEcho() {
        return (((IMixinScoreboard) this.theScoreboard).echoToSponge() || this.spongeTeam.getScoreboards().size() == 1)
                && this.spongeTeam.allowRecursion;
    }

    @SuppressWarnings("rawtypes")
    public MessageSink getSink() {
        Set<CommandSource> sources = new HashSet<CommandSource>();

        Collection collection = getMembershipCollection();
        Iterator iterator = collection.iterator();

        while (iterator.hasNext()) {
            String s = (String)iterator.next();
            EntityPlayerMP teamPlayer = MinecraftServer.getServer().getConfigurationManager().getPlayerByUsername(s);
            if (teamPlayer != null) {
                sources.add((Player) teamPlayer);
            }
        }
        return MessageSinks.to(sources);
    }

    @SuppressWarnings("rawtypes")
    public MessageSink getSinkForPlayer(EntityPlayerMP player) {
        Set<CommandSource> sources = new HashSet<CommandSource>();

        Collection collection = getMembershipCollection();
        Iterator iterator = collection.iterator();

        while (iterator.hasNext()) {
            String s = (String)iterator.next();
            EntityPlayerMP teamPlayer = player.mcServer.getConfigurationManager().getPlayerByUsername(s);
            if (teamPlayer != null && player != teamPlayer) {
                sources.add((Player) teamPlayer);
            }
        }
        return MessageSinks.to(sources);
    }

    public MessageSink getNonTeamSink() {
        Set<CommandSource> sources = new HashSet<CommandSource>();

        for (int i = 0; i < MinecraftServer.getServer().getConfigurationManager().playerEntityList.size(); ++i) {
            EntityPlayerMP player = (EntityPlayerMP)MinecraftServer.getServer().getConfigurationManager().playerEntityList.get(i);

            if (player.getTeam() != (Team)(Object)this) {
                sources.add((Player) player);
            }
        }
        return MessageSinks.to(sources);
    }
}
