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
package org.spongepowered.common.mixin.api.mcp.world;

import net.minecraft.world.GameType;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.text.translation.SpongeTranslation;

import java.util.Locale;

import javax.annotation.Nullable;

@Mixin(GameType.class)
@Implements(@Interface(iface = GameMode.class, prefix = "gamemode$"))
public abstract class GameTypeMixin_API {

    @Shadow public abstract String shadow$getName();

    @Nullable private String api$id;
    @Nullable private SpongeTranslation api$translation;

    public String gamemode$getId() {
        if (this.api$id == null) {
            final String gameTypeName = this.shadow$getName().equals("") ? "not_set" : this.shadow$getName().toLowerCase(Locale.ENGLISH);
            this.api$id = SpongeImplHooks.getModIdFromClass(this.getClass()) + ":" + gameTypeName;
        }
        return this.api$id;
    }

    @Intrinsic
    public String gamemode$getName() {
        return shadow$getName();
    }

    public Translation gamemode$getTranslation() {
        if (this.api$translation == null) {
            this.api$translation = new SpongeTranslation("gameMode." + this.shadow$getName().toLowerCase(Locale.ENGLISH));
        }
        return this.api$translation;
    }
}
