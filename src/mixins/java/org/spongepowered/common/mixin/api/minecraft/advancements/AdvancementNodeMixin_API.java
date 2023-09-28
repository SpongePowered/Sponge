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
package org.spongepowered.common.mixin.api.minecraft.advancements;

import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableSet;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementNode;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.advancement.Advancement;
import org.spongepowered.api.advancement.AdvancementTemplate;
import org.spongepowered.api.advancement.AdvancementTree;
import org.spongepowered.api.advancement.TreeLayoutElement;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;

@Mixin(AdvancementNode.class)
@Implements(@Interface(iface = AdvancementTree.class, prefix = "tree$"))
public abstract class AdvancementNodeMixin_API implements AdvancementTree {

    @Shadow @Final private AdvancementHolder holder;

    @Shadow public abstract net.minecraft.advancements.Advancement advancement();
    @Shadow public abstract Iterable<AdvancementNode> shadow$children();

    @Override
    public Advancement rootAdvancement() {
        return (Advancement) (Object) this.holder.value();
    }

    @Override
    public ResourceKey key() {
        return (ResourceKey) (Object) this.holder.id();
    }

    @Intrinsic
    public Collection<org.spongepowered.api.advancement.AdvancementNode> tree$children() {
        var set = new HashSet<org.spongepowered.api.advancement.AdvancementNode>();
        for (AdvancementNode child : this.shadow$children()) {
            set.add((org.spongepowered.api.advancement.AdvancementNode) child);
        }
        return set;
    }

    @Override
    public Optional<ResourceKey> backgroundPath() {
        final Optional<DisplayInfo> displayInfo = this.holder.value().display();
        return displayInfo.map(di -> (ResourceKey) (Object) di.getBackground());
    }

    @Override
    public Collection<TreeLayoutElement> layoutElements() {
        final ImmutableSet.Builder<TreeLayoutElement> elements = ImmutableSet.builder();
        this.impl$collectElements((AdvancementNode) (Object) this, elements);
        return elements.build();
    }

    private void impl$collectElements(final AdvancementNode node, final ImmutableSet.Builder<TreeLayoutElement> elements) {
        node.advancement().display().map(TreeLayoutElement.class::cast).ifPresent(elements::add);
        node.children().forEach(child -> this.impl$collectElements(child, elements));
    }

    @Override
    public Optional<TreeLayoutElement> layoutElement(final AdvancementTemplate advancement) {
        if (advancement.advancement().displayInfo().isEmpty()) {
            return Optional.empty();
        }
        return this.layoutElement(advancement.key());
    }

    @Override
    public Optional<TreeLayoutElement> layoutElement(final ResourceKey advancementKey) {
        return this.impl$element(((ResourceLocation) (Object) advancementKey));
    }

    private Optional<TreeLayoutElement> impl$element(final ResourceLocation loc) {
        return this.impl$findElementInfo((AdvancementNode) (Object) this, loc).map(TreeLayoutElement.class::cast);
    }

    private Optional<DisplayInfo> impl$findElementInfo(AdvancementNode node, ResourceLocation key) {
        if (node.holder().id().equals(key)) {
            return node.advancement().display();
        }
        for (final AdvancementNode child : node.children()) {
            final var info = this.impl$findElementInfo(child, key);
            if (info.isPresent()) {
                return info;
            }
        }
        return Optional.empty();
    }
}
