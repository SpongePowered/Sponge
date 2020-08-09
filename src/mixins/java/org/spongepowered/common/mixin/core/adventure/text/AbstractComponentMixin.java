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
package org.spongepowered.common.mixin.core.adventure.text;

import net.kyori.adventure.text.AbstractComponent;
import net.kyori.adventure.text.BlockNBTComponent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.EntityNBTComponent;
import net.kyori.adventure.text.KeybindComponent;
import net.kyori.adventure.text.NBTComponent;
import net.kyori.adventure.text.ScoreComponent;
import net.kyori.adventure.text.SelectorComponent;
import net.kyori.adventure.text.StorageNBTComponent;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.KeybindTextComponent;
import net.minecraft.util.text.NBTTextComponent;
import net.minecraft.util.text.ScoreTextComponent;
import net.minecraft.util.text.SelectorTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.common.bridge.adventure.ComponentBridge;
import org.spongepowered.common.bridge.adventure.StyleBridge;

import java.util.ArrayList;
import java.util.List;

@Mixin(AbstractComponent.class)
public abstract class AbstractComponentMixin implements ComponentBridge {
    private ITextComponent bridge$vanillaComponent;

    @Override
    @SuppressWarnings("ConstantConditions")
    public ITextComponent bridge$asVanillaComponent() {
        // TODO(adventure): in 1.16 evaluate using an ITextComponent wrapper as a first stage for conversion
        if (this.bridge$vanillaComponent == null) {
            if (this instanceof TextComponent) {
                this.bridge$vanillaComponent = new StringTextComponent(((TextComponent) this).content());
            } else if (this instanceof TranslatableComponent) {
                final TranslatableComponent $this = (TranslatableComponent) this;
                final List<ITextComponent> with = new ArrayList<>($this.args().size());
                for (final Component arg : $this.args()) {
                    with.add(SpongeAdventure.asVanilla(arg));
                }
                this.bridge$vanillaComponent = new TranslationTextComponent($this.key(), with.toArray(new Object[0]));
            } else if (this instanceof KeybindComponent) {
                this.bridge$vanillaComponent = new KeybindTextComponent(((KeybindComponent) this).keybind());
            } else if (this instanceof ScoreComponent) {
                final ScoreComponent $this = (ScoreComponent) this;
                this.bridge$vanillaComponent = new ScoreTextComponent($this.name(), $this.objective());
                ((ScoreTextComponent) this.bridge$vanillaComponent).setValue($this.value());
            } else if (this instanceof SelectorComponent) {
                this.bridge$vanillaComponent = new SelectorTextComponent(((SelectorComponent) this).pattern());
            } else if (this instanceof NBTComponent<?, ?>) {
                if (this instanceof BlockNBTComponent) {
                    final BlockNBTComponent $this = (BlockNBTComponent) this;
                    this.bridge$vanillaComponent = new NBTTextComponent.Block(
                      $this.pos().asString(),
                      $this.interpret(),
                      $this.nbtPath()
                    );
                } else if (this instanceof EntityNBTComponent) {
                    final EntityNBTComponent $this = (EntityNBTComponent) this;
                    this.bridge$vanillaComponent = new NBTTextComponent.Entity($this.selector(), $this.interpret(), $this.nbtPath());
                } else if (this instanceof StorageNBTComponent) {
                    // TODO(adventure) 1.16
                }
            }
            for (final Component child : ((Component) this).children()) {
                this.bridge$vanillaComponent.appendSibling(SpongeAdventure.asVanilla(child));
            }
            this.bridge$vanillaComponent.setStyle(((StyleBridge) (Object) ((Component) this).style()).bridge$asVanilla());
        }
        return this.bridge$vanillaComponent;
    }
}
