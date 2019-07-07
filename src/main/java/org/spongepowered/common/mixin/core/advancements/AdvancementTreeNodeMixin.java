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
package org.spongepowered.common.mixin.core.advancements;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementTreeNode;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.advancement.AdvancementTree;
import org.spongepowered.api.advancement.TreeLayout;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.advancement.SpongeAdvancementTree;
import org.spongepowered.common.advancement.SpongeTreeLayout;

@Mixin(AdvancementTreeNode.class)
public class AdvancementTreeNodeMixin {

    @Inject(method = "layout", at = @At("RETURN"))
    private static void onLayout(final Advancement root, final CallbackInfo ci) {
        final AdvancementTree advancementTree = ((org.spongepowered.api.advancement.Advancement) root).getTree().get();
        final TreeLayout layout = new SpongeTreeLayout((SpongeAdvancementTree) advancementTree);
        SpongeImpl.postEvent(SpongeEventFactory.createAdvancementTreeEventGenerateLayout(
                Sponge.getCauseStackManager().getCurrentCause(), layout, advancementTree));
    }
}
