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
package org.spongepowered.common.mixin.api.mcp.advancements;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.collect.ImmutableList;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.api.advancement.AdvancementTree;
import org.spongepowered.api.advancement.criteria.AdvancementCriterion;
import org.spongepowered.api.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.bridge.advancements.AdvancementBridge;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nullable;

@Mixin(Advancement.class)
public class AdvancementMixin_API implements org.spongepowered.api.advancement.Advancement {

    @Shadow @Final @Mutable @Nullable private Advancement parent;
    @Shadow @Final @Mutable private String[][] requirements;
    @Shadow @Final @Mutable private Map<String, Criterion> criteria;
    @Shadow @Final @Nullable private DisplayInfo display;
    @Shadow @Final private Set<Advancement> children;
    @Shadow @Final private ITextComponent displayText;


    @Override
    public Optional<AdvancementTree> getTree() {
        checkState(SpongeImplHooks.isMainThread());
        return ((AdvancementBridge) this).bridge$getTree();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Collection<org.spongepowered.api.advancement.Advancement> getChildren() {
        checkState(SpongeImplHooks.isMainThread());
        return ImmutableList.copyOf((Collection) this.children);
    }

    @Override
    public AdvancementCriterion getCriterion() {
        checkState(SpongeImplHooks.isMainThread());
        return ((AdvancementBridge) this).bridge$getCriterion();
    }
    @SuppressWarnings("unchecked")
    @Override
    public Optional<org.spongepowered.api.advancement.Advancement> getParent() {
        checkState(SpongeImplHooks.isMainThread());
        return (Optional<org.spongepowered.api.advancement.Advancement>) (Optional<?>) ((AdvancementBridge) this).bridge$getParent();
    }

    @Override
    public Optional<org.spongepowered.api.advancement.DisplayInfo> getDisplayInfo() {
        checkState(SpongeImplHooks.isMainThread());
        return Optional.ofNullable((org.spongepowered.api.advancement.DisplayInfo) this.display);
    }

    @Override
    public List<Text> toToastText() {
        checkState(SpongeImplHooks.isMainThread());
        return ((AdvancementBridge) this).bridge$getToastText();
    }

    @Override
    public final String getId() {
        checkState(SpongeImplHooks.isMainThread());
        return ((AdvancementBridge) this).bridge$getId();
    }

    @Override
    public String getName() {
        checkState(SpongeImplHooks.isMainThread());
        return ((AdvancementBridge) this).bridge$getName();
    }

    @Override
    public Text toText() {
        checkState(SpongeImplHooks.isMainThread());
        return ((AdvancementBridge) this).bridge$getText();
    }
}
