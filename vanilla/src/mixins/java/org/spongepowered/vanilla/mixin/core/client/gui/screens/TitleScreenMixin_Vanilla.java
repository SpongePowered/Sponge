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
package org.spongepowered.vanilla.mixin.core.client.gui.screens;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.vanilla.client.gui.screen.PluginScreen;

import java.util.Optional;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin_Vanilla extends Screen {

    protected TitleScreenMixin_Vanilla(final Component titleIn) {
        super(titleIn);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void vanilla$addPluginsButton(final CallbackInfo ci) {
        final Optional<AbstractWidget> realmsButton = this.children().stream()
            .filter(b ->  b instanceof AbstractWidget && ((AbstractWidget) b).getMessage().equals(new TranslatableComponent("menu.online")))
            .map(b -> (AbstractWidget) b)
            .findFirst();
        realmsButton.ifPresent(b -> {
            b.setWidth(98);
            b.x = this.width / 2 + 2;
        });

        // Plugins Button
        this.addRenderableWidget(new Button(this.width / 2 - 100, realmsButton.map(b -> b.y).orElse(0), 98, 20, new TextComponent("Plugins"),
            b -> this.minecraft.setScreen(new PluginScreen(this))));
    }
}
