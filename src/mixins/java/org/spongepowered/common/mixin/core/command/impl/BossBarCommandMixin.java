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
package org.spongepowered.common.mixin.core.command.impl;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.command.CommandSource;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.impl.BossBarCommand;
import net.minecraft.server.CustomServerBossInfoManager;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.bridge.world.ServerWorldBridge;

@Mixin(BossBarCommand.class)
public abstract class BossBarCommandMixin {

    @Shadow @Mutable @Final public static SuggestionProvider<CommandSource> SUGGEST_BOSS_BAR = ((context, builder) -> ISuggestionProvider.suggestResource(((ServerWorldBridge) context.getSource().getLevel()).bridge$getBossBarManager().getIds(), builder));

    @Redirect(method = "listBars", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getCustomBossEvents()"
            + "Lnet/minecraft/server/CustomServerBossInfoManager;"))
    private static CustomServerBossInfoManager impl$getBossBarManagerForWorldOnList(final MinecraftServer server, final CommandSource source) {
        return ((ServerWorldBridge) source.getLevel()).bridge$getBossBarManager();
    }

    @Redirect(method = "createBar", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getCustomBossEvents()"
            + "Lnet/minecraft/server/CustomServerBossInfoManager;"))
    private static CustomServerBossInfoManager impl$getBossBarManagerForWorldOnCreate(final MinecraftServer server, final CommandSource source) {
        return ((ServerWorldBridge) source.getLevel()).bridge$getBossBarManager();
    }

    @Redirect(method = "removeBar", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getCustomBossEvents()Lnet/minecraft/server/CustomServerBossInfoManager;"))
    private static CustomServerBossInfoManager impl$getBossBarManagerForWorldOnRemove(final MinecraftServer server, final CommandSource source) {
        return ((ServerWorldBridge) source.getLevel()).bridge$getBossBarManager();
    }

    @Redirect(method = "getBossBar", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getCustomBossEvents()Lnet/minecraft/server/CustomServerBossInfoManager;"))
    private static CustomServerBossInfoManager impl$getBossBarManagerForWorldOnGet(final MinecraftServer server, final CommandContext<CommandSource> source) {
        return ((ServerWorldBridge) source.getSource().getLevel()).bridge$getBossBarManager();
    }
}
