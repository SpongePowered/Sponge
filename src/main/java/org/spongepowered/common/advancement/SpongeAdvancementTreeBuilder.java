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
package org.spongepowered.common.advancement;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import org.apache.commons.lang3.StringUtils;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.advancement.Advancement;
import org.spongepowered.api.advancement.AdvancementTree;
import org.spongepowered.api.advancement.DisplayInfo;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.common.interfaces.advancement.IMixinAdvancement;
import org.spongepowered.common.interfaces.advancement.IMixinDisplayInfo;

import javax.annotation.Nullable;

@SuppressWarnings("ConstantConditions")
public class SpongeAdvancementTreeBuilder implements AdvancementTree.Builder {

    private String id;
    @Nullable private String name;
    private Advancement rootAdvancement;
    private String background;

    public SpongeAdvancementTreeBuilder() {
        reset();
    }

    @Override
    public AdvancementTree.Builder rootAdvancement(Advancement rootAdvancement) {
        checkNotNull(rootAdvancement, "rootAdvancement");
        checkState(((IMixinAdvancement) rootAdvancement).isRegistered(), "The root advancement must be registered.");
        checkState(!rootAdvancement.getParent().isPresent(), "The root advancement cannot have a parent.");
        checkState(rootAdvancement.getDisplayInfo().isPresent(), "The root advancement must have display info.");
        checkState(((IMixinDisplayInfo) rootAdvancement.getDisplayInfo().get()).getBackground() == null,
                "The root advancement is already used by a different Advancement Tree.");
        this.rootAdvancement = rootAdvancement;
        return this;
    }

    @Override
    public AdvancementTree.Builder background(String background) {
        checkNotNull(background, "background");
        this.background = background;
        return this;
    }

    @Override
    public AdvancementTree.Builder id(String id) {
        checkNotNull(id, "id");
        this.id = id;
        return this;
    }

    @Override
    public AdvancementTree.Builder name(String name) {
        checkNotNull(name, "name");
        this.name = name;
        return this;
    }

    @Override
    public AdvancementTree build() {
        checkState(this.id != null, "The id must be set");
        checkState(this.rootAdvancement != null, "The root advancement must be set");
        final PluginContainer plugin = Sponge.getCauseStackManager().getCurrentCause().first(PluginContainer.class).get();
        final String name = StringUtils.isEmpty(this.name) ? this.rootAdvancement.getDisplayInfo()
                .map(DisplayInfo::getTitle).map(Text::toPlain).orElse(this.id) : this.name;
        final SpongeAdvancementTree advancementTree = new SpongeAdvancementTree(this.rootAdvancement, plugin.getId() + ':' + this.id, name);
        ((IMixinDisplayInfo) this.rootAdvancement.getDisplayInfo().get()).setBackground(this.background);
        ((IMixinAdvancement) this.rootAdvancement).setParent(null);
        applyTree(this.rootAdvancement, advancementTree);
        return advancementTree;
    }

    private static void applyTree(Advancement advancement, AdvancementTree tree) {
        ((IMixinAdvancement) advancement).setTree(tree);
        for (Advancement child : advancement.getChildren()) {
            applyTree(child, tree);
        }
    }

    @Override
    public AdvancementTree.Builder reset() {
        this.background = "minecraft:textures/gui/advancements/backgrounds/stone.png";
        this.rootAdvancement = null;
        this.name = null;
        this.id = null;
        return this;
    }
}
