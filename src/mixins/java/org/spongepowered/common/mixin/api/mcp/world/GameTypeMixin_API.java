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

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.GameType;
import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.text.translation.SpongeTranslation;

@Mixin(GameType.class)
public abstract class GameTypeMixin_API implements GameMode {

    @Shadow public abstract ITextComponent shadow$getDisplayName();

    private CatalogKey api$key;
    private SpongeTranslation api$translation;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void api$setKeyAndTranslation(String enumName, int ordinal, int gameTypeId, String gameTypeName, CallbackInfo ci) {
        final PluginContainer container = SpongeImplHooks.getActiveModContainer();

        this.api$key = container.createCatalogKey(gameTypeName.isEmpty() ? "not_set" : gameTypeName);
        this.api$translation = new SpongeTranslation((TranslationTextComponent) this.shadow$getDisplayName());
    }

    @Override
    public CatalogKey getKey() {
        return this.api$key;
    }

    @Override
    public Translation getTranslation() {
        return this.api$translation;
    }
}
