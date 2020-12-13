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
package org.spongepowered.common.adventure;

import java.util.Set;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.server.SUpdateBossInfoPacket;
import net.minecraft.world.server.ServerBossInfo;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class VanillaBossBarListener implements BossBar.Listener {
    private final ServerBossInfo vanilla;

    public VanillaBossBarListener(final ServerBossInfo vanilla) {
      this.vanilla = vanilla;
    }

    @Override
    public void bossBarNameChanged(@NonNull final BossBar bar, @NonNull final Component oldName, @NonNull final Component newName) {
        this.sendPacket(SUpdateBossInfoPacket.Operation.UPDATE_NAME);
    }

    @Override
    public void bossBarProgressChanged(@NonNull final BossBar bar, final float oldProgress, final float newProgress) {
        this.sendPacket(SUpdateBossInfoPacket.Operation.UPDATE_PCT);
    }

    @Override
    public void bossBarColorChanged(@NonNull final BossBar bar, final BossBar.@NonNull Color oldColor, final BossBar.@NonNull Color newColor) {
        this.sendPacket(SUpdateBossInfoPacket.Operation.UPDATE_STYLE);
    }

    @Override
    public void bossBarOverlayChanged(@NonNull final BossBar bar, final BossBar.@NonNull Overlay oldOverlay, final BossBar.@NonNull Overlay newOverlay) {
        this.sendPacket(SUpdateBossInfoPacket.Operation.UPDATE_STYLE);
    }

    @Override
    public void bossBarFlagsChanged(@NonNull final BossBar bar, @NonNull final Set<BossBar.Flag> flagsAdded, @NonNull final Set<BossBar.Flag> flagsRemoved) {
        this.sendPacket(SUpdateBossInfoPacket.Operation.UPDATE_PROPERTIES);
    }

    private void sendPacket(final SUpdateBossInfoPacket.Operation action) {
        final SUpdateBossInfoPacket packet = new SUpdateBossInfoPacket(action, this.vanilla);
        for (final ServerPlayerEntity player : this.vanilla.getPlayers()) {
            player.connection.sendPacket(packet);
        }
    }
}
