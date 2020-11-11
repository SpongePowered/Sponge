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
package org.spongepowered.common.mixin.core.adventure.bossbar;

import net.kyori.adventure.bossbar.BossBar;
import net.minecraft.world.server.ServerBossInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.common.bridge.adventure.BossBarBridge;
import org.spongepowered.common.bridge.world.BossInfoBridge;

@Mixin(targets = "net.kyori.adventure.bossbar.BossBarImpl")
public abstract class BossBarImplMixin implements BossBarBridge {
    private ServerBossInfo bridge$vanillaServerBar;

    @Override
    public ServerBossInfo bridge$asVanillaServerBar() {
        if (this.bridge$vanillaServerBar == null) {
            final BossBar $this = (BossBar) this;
            this.bridge$vanillaServerBar = new ServerBossInfo(SpongeAdventure.asVanilla($this.name()), SpongeAdventure.asVanilla($this.color()), SpongeAdventure.asVanilla($this.overlay()));
            final BossInfoBridge bridge = (BossInfoBridge) this.bridge$vanillaServerBar;
            bridge.bridge$copyAndAssign($this);
        }
        return this.bridge$vanillaServerBar;
    }
}
