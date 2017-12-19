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
package org.spongepowered.common.mixin.core.advancement;

import com.google.common.collect.ImmutableList;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.api.advancement.AdvancementTree;
import org.spongepowered.api.advancement.criteria.AdvancementCriterion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.advancement.SpongeAdvancementBuilder;
import org.spongepowered.common.advancement.SpongeAdvancementTree;
import org.spongepowered.common.advancement.SpongeCriterionHelper;
import org.spongepowered.common.interfaces.advancement.IMixinAdvancement;
import org.spongepowered.common.interfaces.advancement.IMixinDisplayInfo;
import org.spongepowered.common.registry.type.advancement.AdvancementRegistryModule;
import org.spongepowered.common.registry.type.advancement.AdvancementTreeRegistryModule;
import org.spongepowered.common.text.SpongeTexts;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nullable;

@Mixin(Advancement.class)
public class MixinAdvancement implements org.spongepowered.api.advancement.Advancement, IMixinAdvancement {

    @Shadow @Final private ResourceLocation id;
    @Shadow @Final @Nullable private Advancement parent;
    @Shadow @Final private String[][] requirements;
    @Shadow @Final private Map<String, Criterion> criteria;
    @Shadow @Final @Nullable private DisplayInfo display;
    @Shadow @Final private Set<Advancement> children;

    private AdvancementCriterion criterion;
    @Nullable private AdvancementTree tree;
    private String spongeId;
    private String name;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(ResourceLocation id, @Nullable Advancement parentIn, @Nullable DisplayInfo displayIn,
            AdvancementRewards rewardsIn, Map<String, Criterion> criteriaIn, String[][] requirementsIn, CallbackInfo ci) {
        if (displayIn != null) {
            ((IMixinDisplayInfo) displayIn).setAdvancement(this);
        }
        this.criteria = new HashMap<>(criteriaIn);
        this.criterion = SpongeCriterionHelper.toCriterion(this.criteria, this.requirements);
        String path = id.getResourcePath();
        this.name = path.replace('/', '_');
        this.spongeId = id.getResourceDomain() + ':' + this.name;
        if (displayIn != null) {
            this.name = SpongeTexts.toPlain(displayIn.getTitle());
        }
        AdvancementRegistryModule.getInstance().register(this);
        if (parentIn == null) {
            // Remove the root suffix from json file tree ids
            if (path.endsWith("/root")) {
                path = path.substring(0, path.lastIndexOf('/'));
            }
            path = path.replace('/', '_');
            String name = path;
            if (displayIn != null) {
                name = this.name;
            }
            path = id.getResourceDomain() + ':' + path;
            this.tree = new SpongeAdvancementTree(this, path, name);
            AdvancementTreeRegistryModule.getInstance().register(this.tree);
        } else {
            this.tree = ((org.spongepowered.api.advancement.Advancement) parentIn).getTree().orElse(null);
        }
    }

    @Override
    public Optional<AdvancementTree> getTree() {
        return Optional.ofNullable(this.tree);
    }

    @Override
    public void setTree(AdvancementTree tree) {
        this.tree = tree;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<org.spongepowered.api.advancement.Advancement> getChildren() {
        return ImmutableList.copyOf((Collection) this.children);
    }

    @Override
    public AdvancementCriterion getCriterion() {
        return this.criterion;
    }

    @Override
    public void setCriterion(AdvancementCriterion criterion) {
        this.criterion = criterion;
    }

    @Override
    public Optional<org.spongepowered.api.advancement.Advancement> getParent() {
        if (this.parent == SpongeAdvancementBuilder.DUMMY_ROOT_ADVANCEMENT) {
            return Optional.empty();
        }
        return Optional.ofNullable((org.spongepowered.api.advancement.Advancement) this.parent);
    }

    @Override
    public Optional<org.spongepowered.api.advancement.DisplayInfo> getDisplayInfo() {
        return Optional.ofNullable((org.spongepowered.api.advancement.DisplayInfo) this.display);
    }

    @Override
    public String getId() {
        return this.spongeId;
    }

    @Override
    public String getName() {
        return this.name;
    }
}
