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
package org.spongepowered.common.mixin.core.world.scores;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.api.Sponge;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.common.bridge.world.scores.PlayerTeamBridge;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

@Mixin(PlayerTeam.class)
public abstract class PlayerTeamMixin implements PlayerTeamBridge {

    // @formatter:off
    @Shadow @Final @Mutable @Nullable private Scoreboard scoreboard;
    @Shadow private net.minecraft.network.chat.Component displayName;
    @Shadow private ChatFormatting color;
    @Shadow private net.minecraft.network.chat.Component playerPrefix;
    @Shadow private net.minecraft.network.chat.Component playerSuffix;
    @Shadow public abstract Collection<String> getPlayers();
    // @formatter:on

    private @MonotonicNonNull Component bridge$displayName;
    private @MonotonicNonNull Component bridge$prefix;
    private @MonotonicNonNull Component bridge$suffix;
    private @MonotonicNonNull NamedTextColor bridge$color;

    private void impl$teamChanged() {
        if (this.scoreboard != null) {
            this.scoreboard.onTeamChanged((PlayerTeam) (Object) this);
        }
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void impl$setUpDisplayNames(final Scoreboard scoreboard, final String name, final CallbackInfo ci) {
        this.bridge$displayName = LegacyComponentSerializer.legacySection().deserialize(name);
        this.bridge$prefix = SpongeAdventure.asAdventure(this.playerPrefix);
        this.bridge$suffix = SpongeAdventure.asAdventure(this.playerSuffix);
        this.bridge$color = SpongeAdventure.asAdventureNamed(this.color);
    }

    @Redirect(
        method = "*",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/scores/Scoreboard;onTeamChanged(Lnet/minecraft/world/scores/PlayerTeam;)V"
        )
    )
    private void impl$nullCheckScoreboard(@Nullable final Scoreboard scoreboard, final PlayerTeam team) {
        if (scoreboard != null) {
            scoreboard.onTeamChanged(team);
        }
    }

    @Inject(
        method = "setDisplayName",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/world/scores/PlayerTeam;displayName:Lnet/minecraft/network/chat/Component;",
            opcode = Opcodes.PUTFIELD,
            shift = At.Shift.AFTER
        )
    )
    private void impl$trackDisplayNameChange(final net.minecraft.network.chat.Component name, final CallbackInfo ci) {
        this.bridge$displayName = SpongeAdventure.asAdventure(name);
    }

    @Inject(
        method = "setPlayerPrefix",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/world/scores/PlayerTeam;playerPrefix:Lnet/minecraft/network/chat/Component;",
            opcode = Opcodes.PUTFIELD,
            shift = At.Shift.AFTER
        )
    )
    private void impl$trackPrefixChange(final net.minecraft.network.chat.Component prefix, final CallbackInfo callbackInfo) {
        this.bridge$prefix = SpongeAdventure.asAdventure(prefix);
    }

    @Inject(
        method = "setPlayerSuffix",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/world/scores/PlayerTeam;playerSuffix:Lnet/minecraft/network/chat/Component;",
            opcode = Opcodes.PUTFIELD,
            shift = At.Shift.AFTER
        )
    )
    private void impl$trackSuffixChange(final net.minecraft.network.chat.Component suffix, final CallbackInfo ci) {
        this.bridge$suffix = SpongeAdventure.asAdventure(suffix);
    }

    @Inject(method = "setColor", at = @At("RETURN"))
    private void impl$trackColorChange(final ChatFormatting color, final CallbackInfo ci) {
        this.bridge$color = SpongeAdventure.asAdventureNamed(color);
    }

    @Override
    public Component bridge$getDisplayName() {
        return this.bridge$displayName;
    }

    @Override
    public void bridge$setDisplayName(final Component text) {
        this.bridge$displayName = text;
        this.displayName = SpongeAdventure.asVanilla(text);
        this.impl$teamChanged();
    }

    @Override
    public Component bridge$getPrefix() {
        return this.bridge$prefix;
    }

    @Override
    public void bridge$setPrefix(final Component text) {
        this.bridge$prefix = text;
        this.playerPrefix = SpongeAdventure.asVanilla(text);
        this.impl$teamChanged();
    }

    @Override
    public Component bridge$getSuffix() {
        return this.bridge$suffix;
    }

    @Override
    public void bridge$setSuffix(final Component suffix) {
        this.bridge$suffix = suffix;
        this.playerSuffix = SpongeAdventure.asVanilla(suffix);
        this.impl$teamChanged();
    }

    @Override
    public NamedTextColor bridge$getColor() {
        return this.bridge$color;
    }

    @Override
    public void bridge$setColor(final NamedTextColor color) {
        this.bridge$color = color;
        this.color = SpongeAdventure.asVanilla(color);
        this.impl$teamChanged();
    }

    @SuppressWarnings("EqualsBetweenInconvertibleTypes")
    @Override
    public Audience bridge$getTeamChannel(final ServerPlayer player) {
        return Audience.audience(this.getPlayers().stream()
                .map(name -> Sponge.game().server().player(name))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(member -> member != player)
                .collect(Collectors.toSet()));
    }

    @Override
    public Audience bridge$getNonTeamChannel() {
        return Audience.audience(Sponge.game().server().onlinePlayers().stream()
                .filter(player -> ((ServerPlayer) player).getTeam() != (Object) this)
                .collect(Collectors.toSet()));
    }
}
