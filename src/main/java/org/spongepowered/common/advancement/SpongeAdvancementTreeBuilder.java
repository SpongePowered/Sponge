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

import net.minecraft.util.ResourceLocation;
import org.spongepowered.api.advancement.Advancement;
import org.spongepowered.api.advancement.AdvancementTree;
import org.spongepowered.common.interfaces.advancement.IMixinAdvancement;

@SuppressWarnings("ConstantConditions")
public class SpongeAdvancementTreeBuilder implements AdvancementTree.Builder {

    private Advancement rootAdvancement;
    private String background;

    public SpongeAdvancementTreeBuilder() {
        reset();
    }

    @Override
    public AdvancementTree.Builder rootAdvancement(Advancement rootAdvancement) {
        checkNotNull(rootAdvancement, "rootAdvancement");
        checkState(!rootAdvancement.getParent().isPresent(), "The root advancement cannot have a parent.");
        checkState(rootAdvancement.getDisplayInfo().isPresent(), "The root advancement must have display info.");
        checkState(((net.minecraft.advancements.Advancement) rootAdvancement).getDisplay().background == null,
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
    public AdvancementTree build(String pluginId, String id) {
        checkState(this.rootAdvancement != null, "Root advancement has not been set");
        final SpongeAdvancementTree advancementTree = new SpongeAdvancementTree(this.rootAdvancement, pluginId, id);
        ((net.minecraft.advancements.Advancement) this.rootAdvancement).getDisplay().background = new ResourceLocation(this.background);
        ((net.minecraft.advancements.Advancement) this.rootAdvancement).parent = null;
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
    public AdvancementTree.Builder from(AdvancementTree value) {
        this.background = value.getBackground();
        this.rootAdvancement = null;
        return this;
    }

    @Override
    public AdvancementTree.Builder reset() {
        this.background = "minecraft:textures/gui/advancements/backgrounds/stone.png";
        this.rootAdvancement = null;
        return this;
    }
}
