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

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.text.TextFormatting;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.bridge.scoreboard.ScorePlayerTeamBridge;
import org.spongepowered.common.registry.type.text.TextColorRegistryModule;
import org.spongepowered.common.text.SpongeTexts;
import org.spongepowered.common.text.format.SpongeTextColor;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

@Mixin(ScorePlayerTeam.class)
public abstract class ScorePlayerTeamMixin implements ScorePlayerTeamBridge {

    @Shadow @Final @Mutable @Nullable private Scoreboard scoreboard;
    @Shadow private String displayName;
    @Shadow private TextFormatting color;
    @Shadow private String prefix;
    @Shadow private String suffix;
    @Shadow public abstract Collection<String> getMembershipCollection();

    @SuppressWarnings("NullableProblems") @MonotonicNonNull private Text bridge$displayName;
    @SuppressWarnings("NullableProblems") @MonotonicNonNull private Text bridge$Prefix;
    @SuppressWarnings("NullableProblems") @MonotonicNonNull private Text bridge$Suffix;
    @SuppressWarnings("NullableProblems") @MonotonicNonNull private TextColor bridge$Color;

    // Minecraft doesn't do a null check on scoreboard, so we redirect
    // the call and do it ourselves.
    private void impl$doTeamUpdate() {
        if (this.scoreboard != null) {
            this.scoreboard.func_96538_b((ScorePlayerTeam) (Object) this);
        }
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void impl$setUpDisplayNames(final Scoreboard scoreboardIn, final String name, final CallbackInfo ci) {
        this.bridge$displayName = SpongeTexts.fromLegacy(name);
        this.bridge$Prefix = SpongeTexts.fromLegacy(this.prefix);
        this.bridge$Suffix = SpongeTexts.fromLegacy(this.suffix);
        this.bridge$Color = TextColorRegistryModule.enumChatColor.get(this.color);
    }

    @Redirect(method = "*",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/scoreboard/Scoreboard;broadcastTeamInfoUpdate(Lnet/minecraft/scoreboard/ScorePlayerTeam;)V"))
    private void impl$nullCheckScoreboard(@Nullable final Scoreboard scoreboard, final ScorePlayerTeam team) {
        if (scoreboard != null) {
            scoreboard.func_96538_b(team);
        }
    }

    @Inject(method = "setDisplayName",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/scoreboard/ScorePlayerTeam;displayName:Ljava/lang/String;",
            opcode = Opcodes.PUTFIELD,
            shift = At.Shift.AFTER))
    private void impl$doTeamUpdateForDisplayName(final String name, final CallbackInfo ci) {
        this.bridge$displayName = SpongeTexts.fromLegacy(name);
    }

    @Inject(method = "setPrefix",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/scoreboard/ScorePlayerTeam;prefix:Ljava/lang/String;",
            opcode = Opcodes.PUTFIELD,
            shift = At.Shift.AFTER))
    private void impl$doTeamUpdateForPrefix(final String prefix, final CallbackInfo callbackInfo) {
        this.bridge$Prefix = SpongeTexts.fromLegacy(prefix);
    }

    @Inject(method = "setSuffix",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/scoreboard/ScorePlayerTeam;suffix:Ljava/lang/String;",
            opcode = Opcodes.PUTFIELD,
            shift = At.Shift.AFTER
        ))
    private void impl$doTeamUpdateForSuffix(final String suffix, final CallbackInfo ci) {
        this.bridge$Suffix = SpongeTexts.fromLegacy(suffix);
    }

    @Inject(method = "setColor", at = @At("RETURN"))
    private void impl$doTeamUpdateForFormat(final TextFormatting format, final CallbackInfo ci) {
        this.bridge$Color = TextColorRegistryModule.enumChatColor.get(format);
        // This isn't called by Vanilla, so we inject the call ourselves.
        this.impl$doTeamUpdate();
    }

    // TODO Mixin (0.8) - Remove when we can use accessor mixins in other mixins
    @Nullable
    @Override
    public Scoreboard accessor$getScoreboard() {
        return this.scoreboard;
    }

    // TODO Mixin (0.8) - Remove when we can use accessor mixins in other mixins
    @Override
    public void accessor$setScoreboard(@Nullable Scoreboard scoreboard) {
        this.scoreboard = scoreboard;
    }

    @Override
    public Text bridge$getDisplayName() {
        return this.bridge$displayName;
    }

    @Override
    public void bridge$setDisplayName(final Text text) {
        final String newText = SpongeTexts.toLegacy(text);
        if (newText.length() > 32) {
            throw new IllegalArgumentException(String.format("Display name is %s characters long! It must be at most 32.", newText.length()));
        }
        this.bridge$displayName = text;
        this.displayName = newText;
        this.impl$doTeamUpdate();
    }

    @Override
    public Text bridge$getPrefix() {
        return this.bridge$Prefix;
    }

    @Override
    public void bridge$setPrefix(final Text text) {
        final String newPrefix = SpongeTexts.toLegacy(text);
        if (newPrefix.length() > 16) {
            throw new IllegalArgumentException(String.format("Prefix is %s characters long! It must be at most 16.", newPrefix.length()));
        }
        this.bridge$Prefix = text;
        this.prefix = newPrefix;
        this.impl$doTeamUpdate();
    }

    @Override
    public Text bridge$getSuffix() {
        return this.bridge$Suffix;
    }

    @Override
    public void bridge$setSuffix(final Text suffix) {
        final String newSuffix = SpongeTexts.toLegacy(suffix);
        if (newSuffix.length() > 16) {
            throw new IllegalArgumentException(String.format("Suffix is %s characters long! It must be at most 16.", newSuffix.length()));
        }
        this.bridge$Suffix = suffix;
        this.suffix = newSuffix;
        this.impl$doTeamUpdate();
    }

    @Override
    public void bridge$setColor(TextColor color) {
        if (color.equals(TextColors.NONE)) {
            color = TextColors.RESET;
        }
        this.bridge$Color = color;
        this.color = ((SpongeTextColor) color).getHandle();
        this.impl$doTeamUpdate();
    }

    @SuppressWarnings("EqualsBetweenInconvertibleTypes")
    @Override
    public MessageChannel bridge$getTeamChannel(final ServerPlayerEntity player) {
        return MessageChannel.fixed(this.getMembershipCollection().stream()
                .map(name -> Sponge.getGame().getServer().getPlayer(name))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(member -> member != player)
                .collect(Collectors.toSet()));
    }

    @Override
    public MessageChannel bridge$getNonTeamChannel() {
        return MessageChannel.fixed(Sponge.getGame().getServer().getOnlinePlayers().stream()
                .filter(player -> ((ServerPlayerEntity) player).func_96124_cp() != (Object) this)
                .collect(Collectors.toSet()));
    }
}
