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

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
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
import org.spongepowered.common.bridge.scoreboard.ScorePlayerTeamBridge;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

@Mixin(ScorePlayerTeam.class)
public abstract class ScorePlayerTeamMixin implements ScorePlayerTeamBridge {

    // @formatter:off
    @Shadow @Final @Mutable @Nullable private Scoreboard scoreboard;
    @Shadow private ITextComponent displayName;
    @Shadow private TextFormatting color;
    @Shadow private ITextComponent prefix;
    @Shadow private ITextComponent suffix;
    @Shadow public abstract Collection<String> getMembershipCollection();
    // @formatter:on

    @SuppressWarnings("NullableProblems") @MonotonicNonNull private Component bridge$displayName;
    @SuppressWarnings("NullableProblems") @MonotonicNonNull private Component bridge$Prefix;
    @SuppressWarnings("NullableProblems") @MonotonicNonNull private Component bridge$Suffix;
    @SuppressWarnings("NullableProblems") @MonotonicNonNull private NamedTextColor bridge$Color;

    // Minecraft doesn't do a null check on scoreboard, so we redirect
    // the call and do it ourselves.
    private void impl$doTeamUpdate() {
        if (this.scoreboard != null) {
            this.scoreboard.onTeamChanged((ScorePlayerTeam) (Object) this);
        }
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void impl$setUpDisplayNames(final Scoreboard scoreboardIn, final String name, final CallbackInfo ci) {
        this.bridge$displayName = SpongeAdventure.legacySection(name);
        this.bridge$Prefix = SpongeAdventure.asAdventure(this.prefix);
        this.bridge$Suffix = SpongeAdventure.asAdventure(this.suffix);
        this.bridge$Color = SpongeAdventure.asAdventureNamed(this.color);
    }

    @Redirect(method = "*",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/scoreboard/Scoreboard;onTeamChanged(Lnet/minecraft/scoreboard/ScorePlayerTeam;)V"))
    private void impl$nullCheckScoreboard(@Nullable final Scoreboard scoreboard, final ScorePlayerTeam team) {
        if (scoreboard != null) {
            scoreboard.onTeamChanged(team);
        }
    }

    @Inject(method = "setDisplayName",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/scoreboard/ScorePlayerTeam;displayName:Lnet/minecraft/util/text/ITextComponent;",
            opcode = Opcodes.PUTFIELD,
            shift = At.Shift.AFTER))
    private void impl$doTeamUpdateForDisplayName(final ITextComponent name, final CallbackInfo ci) {
        this.bridge$displayName = SpongeAdventure.asAdventure(name);
    }

    @Inject(method = "setPrefix",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/scoreboard/ScorePlayerTeam;prefix:Lnet/minecraft/util/text/ITextComponent;",
            opcode = Opcodes.PUTFIELD,
            shift = At.Shift.AFTER))
    private void impl$doTeamUpdateForPrefix(final ITextComponent prefix, final CallbackInfo callbackInfo) {
        this.bridge$Prefix = SpongeAdventure.asAdventure(prefix);
    }

    @Inject(method = "setSuffix",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/scoreboard/ScorePlayerTeam;suffix:Lnet/minecraft/util/text/ITextComponent;",
            opcode = Opcodes.PUTFIELD,
            shift = At.Shift.AFTER
        ))
    private void impl$doTeamUpdateForSuffix(final ITextComponent suffix, final CallbackInfo ci) {
        this.bridge$Suffix = SpongeAdventure.asAdventure(suffix);
    }

    @Inject(method = "setColor", at = @At("RETURN"))
    private void impl$doTeamUpdateForFormat(final TextFormatting format, final CallbackInfo ci) {
        this.bridge$Color = SpongeAdventure.asAdventureNamed(format);
        // This isn't called by Vanilla, so we inject the call ourselves.
        this.impl$doTeamUpdate();
    }

    @Override
    public Component bridge$getDisplayName() {
        return this.bridge$displayName;
    }

    @Override
    public void bridge$setDisplayName(final Component text) {
        final String newText = SpongeAdventure.legacySection(text);
        if (newText.length() > 32) {
            throw new IllegalArgumentException(String.format("Display name is %s characters long! It must be at most 32.", newText.length()));
        }
        this.bridge$displayName = text;
        this.displayName = SpongeAdventure.asVanilla(text);
        this.impl$doTeamUpdate();
    }

    @Override
    public Component bridge$getPrefix() {
        return this.bridge$Prefix;
    }

    @Override
    public void bridge$setPrefix(final Component text) {
        final String newPrefix = SpongeAdventure.legacySection(text);
        if (newPrefix.length() > 16) {
            throw new IllegalArgumentException(String.format("Prefix is %s characters long! It must be at most 16.", newPrefix.length()));
        }
        this.bridge$Prefix = text;
        this.prefix = SpongeAdventure.asVanilla(text);
        this.impl$doTeamUpdate();
    }

    @Override
    public Component bridge$getSuffix() {
        return this.bridge$Suffix;
    }

    @Override
    public void bridge$setSuffix(final Component suffix) {
        final String newSuffix = SpongeAdventure.legacySection(suffix);
        if (newSuffix.length() > 16) {
            throw new IllegalArgumentException(String.format("Suffix is %s characters long! It must be at most 16.", newSuffix.length()));
        }
        this.bridge$Suffix = suffix;
        this.suffix = SpongeAdventure.asVanilla(suffix);
        this.impl$doTeamUpdate();
    }

    @Override
    public void bridge$setColor(NamedTextColor color) {
        this.bridge$Color = color;
        this.color = SpongeAdventure.asVanilla(color);
        this.impl$doTeamUpdate();
    }

    @SuppressWarnings("EqualsBetweenInconvertibleTypes")
    @Override
    public Audience bridge$getTeamChannel(final ServerPlayerEntity player) {
        return Audience.audience(this.getMembershipCollection().stream()
                .map(name -> Sponge.getGame().getServer().getPlayer(name))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(member -> member != player)
                .collect(Collectors.toSet()));
    }

    @Override
    public Audience bridge$getNonTeamChannel() {
        return Audience.audience(Sponge.getGame().getServer().getOnlinePlayers().stream()
                .filter(player -> ((ServerPlayerEntity) player).getTeam() != (Object) this)
                .collect(Collectors.toSet()));
    }
}
