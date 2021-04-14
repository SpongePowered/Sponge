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
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.NbtComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.common.bridge.adventure.ComponentBridge;
import org.spongepowered.common.bridge.adventure.StyleBridge;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Mixin(AbstractComponent.class)
public abstract class AbstractComponentMixin implements ComponentBridge {
    private MutableComponent bridge$vanillaComponent;

    @Override
    @SuppressWarnings("ConstantConditions")
    public net.minecraft.network.chat.Component bridge$asVanillaComponent() {
        if (this.bridge$vanillaComponent == null) {
            if (this instanceof TextComponent) {
                this.bridge$vanillaComponent = new net.minecraft.network.chat.TextComponent(((TextComponent) this).content());
            } else if (this instanceof TranslatableComponent) {
                final TranslatableComponent $this = (TranslatableComponent) this;
                final List<net.minecraft.network.chat.Component> with = new ArrayList<>($this.args().size());
                for (final Component arg : $this.args()) {
                    with.add(((ComponentBridge) arg).bridge$asVanillaComponent());
                }
                this.bridge$vanillaComponent = new net.minecraft.network.chat.TranslatableComponent($this.key(), with.toArray(new Object[0]));
            } else if (this instanceof KeybindComponent) {
                this.bridge$vanillaComponent = new net.minecraft.network.chat.KeybindComponent(((KeybindComponent) this).keybind());
            } else if (this instanceof ScoreComponent) {
                final ScoreComponent $this = (ScoreComponent) this;
                this.bridge$vanillaComponent = new net.minecraft.network.chat.ScoreComponent($this.name(), $this.objective());
            } else if (this instanceof SelectorComponent) {
                //TODO fix this once kyori NBTComponents support minecraft separator
                this.bridge$vanillaComponent = new net.minecraft.network.chat.SelectorComponent(((SelectorComponent) this).pattern(), Optional.of(new net.minecraft.network.chat.TextComponent(",")));
            } else if (this instanceof NBTComponent<?, ?>) {
                if (this instanceof BlockNBTComponent) {
                    final BlockNBTComponent $this = (BlockNBTComponent) this;
                    //TODO fix this once kyori NBTComponents support minecraft separator
                    this.bridge$vanillaComponent = new NbtComponent.BlockNbtComponent($this.nbtPath(), $this.interpret(), $this.pos().asString(), Optional.of(new net.minecraft.network.chat.TextComponent(",")));
                } else if (this instanceof EntityNBTComponent) {
                    final EntityNBTComponent $this = (EntityNBTComponent) this;
                    //TODO fix this once kyori BTComponents support minecraft separator
                    this.bridge$vanillaComponent = new NbtComponent.EntityNbtComponent($this.nbtPath(), $this.interpret(), $this.selector(), Optional.of(new net.minecraft.network.chat.TextComponent(",")));
                } else if (this instanceof StorageNBTComponent) {
                    final StorageNBTComponent $this = (StorageNBTComponent) this;
                    //TODO fix this once kyori NBTComponents support minecraft separator
                    this.bridge$vanillaComponent = new NbtComponent.StorageNbtComponent(
                        $this.nbtPath(),
                        $this.interpret(),
                        SpongeAdventure.asVanilla($this.storage()),
                        Optional.of(new net.minecraft.network.chat.TextComponent(","))
                    );
                }
            }
            for (final Component child : ((Component) this).children()) {
                this.bridge$vanillaComponent.append(((ComponentBridge) child).bridge$asVanillaComponent());
            }
            this.bridge$vanillaComponent.setStyle(((StyleBridge) (Object) ((Component) this).style()).bridge$asVanilla());
        }
        // To prevent potential issues with using this mutable component as part of another text component,
        // of if Minecraft or a mod decides to change this text component, we make a deep copy so that this
        // cache and its siblings are not affected (particularly important for TextComponent#setStyle).
        return this.bridge$vanillaComponent.copy();
    }
}
