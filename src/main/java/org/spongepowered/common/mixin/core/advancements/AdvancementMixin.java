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

import static com.google.common.base.Preconditions.checkState;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.advancements.FrameType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.advancement.AdvancementTree;
import org.spongepowered.api.advancement.AdvancementType;
import org.spongepowered.api.advancement.criteria.AdvancementCriterion;
import org.spongepowered.api.advancement.criteria.AndCriterion;
import org.spongepowered.api.advancement.criteria.OrCriterion;
import org.spongepowered.api.event.game.GameRegistryEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.translation.FixedTranslation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.advancement.DefaultedAdvancementCriterion;
import org.spongepowered.common.advancement.SpongeAdvancementTree;
import org.spongepowered.common.advancement.SpongeScoreCriterion;
import org.spongepowered.common.bridge.advancements.AdvancementBridge;
import org.spongepowered.common.bridge.util.text.ITextComponentBridge;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.phase.plugin.EventListenerPhaseContext;
import org.spongepowered.common.bridge.advancements.CriterionBridge;
import org.spongepowered.common.bridge.advancements.DisplayInfoBridge;
import org.spongepowered.common.registry.type.advancement.AdvancementRegistryModule;
import org.spongepowered.common.registry.type.advancement.AdvancementTreeRegistryModule;
import org.spongepowered.common.text.SpongeTexts;
import org.spongepowered.common.text.translation.SpongeTranslation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nullable;

@Mixin(Advancement.class)
public class AdvancementMixin implements AdvancementBridge {

    @Shadow @Final @Mutable @Nullable private Advancement parent;
    @Shadow @Final @Mutable private String[][] requirements;
    @Shadow @Final @Mutable private Map<String, Criterion> criteria;
    @Shadow @Final @Nullable private DisplayInfo display;
    @Shadow @Final private ITextComponent displayText;

    private AdvancementCriterion impl$criterion;
    @Nullable private AdvancementTree impl$tree;
    private List<Text> impl$toastText;
    private Text impl$text;
    private String impl$spongeId;
    private String impl$name;
    @Nullable private Advancement impl$tempParent;


    @SuppressWarnings({"ConstantConditions"})
    @Inject(method = "<init>", at = @At("RETURN"))
    private void impl$setUpSpongeFields(final ResourceLocation id, @Nullable final Advancement parentIn, @Nullable final DisplayInfo displayIn,
            final AdvancementRewards rewardsIn, final Map<String, Criterion> criteriaIn, final String[][] requirementsIn, final CallbackInfo ci) {
        // Don't do anything on the client, unless we're performing registry initialization
        if (!SpongeImplHooks.isMainThread()) {
            return;
        }
        if (displayIn != null) {
            ((DisplayInfoBridge) displayIn).bridge$setAdvancement((org.spongepowered.api.advancement.Advancement) this);
        }
        String path = id.func_110623_a();
        this.impl$name = path.replace('/', '_');
        this.impl$spongeId = id.func_110624_b() + ':' + this.impl$name;
        if (displayIn != null) {
            this.impl$name = SpongeTexts.toPlain(displayIn.func_192297_a());
        }
        if (PhaseTracker.getInstance().getCurrentState().isEvent()) {
            final Object event = ((EventListenerPhaseContext) PhaseTracker.getInstance().getCurrentContext()).getEvent();
            if (event instanceof GameRegistryEvent.Register) {
                final Class<? extends CatalogType> catalogType = ((GameRegistryEvent.Register<?>) event).getCatalogType();
                if (catalogType.equals(org.spongepowered.api.advancement.Advancement.class) || catalogType.equals(AdvancementTree.class)) {
                    // Wait to set the parent until the advancement is registered
                    this.impl$tempParent = parentIn;
                    this.parent = AdvancementRegistryModule.DUMMY_ROOT_ADVANCEMENT;
                }
            }
        }

        // This is only possible when REGISTER_ADVANCEMENTS_ON_CONSTRUCT is true
        if (parentIn == null) {
            // Remove the root suffix from json file tree ids
            if (path.endsWith("/root")) {
                path = path.substring(0, path.lastIndexOf('/'));
            }
            path = path.replace('/', '_');
            String name = path;
            if (displayIn != null) {
                name = this.impl$name;
            }
            path = id.func_110624_b() + ':' + path;
            this.impl$tree = new SpongeAdvancementTree((org.spongepowered.api.advancement.Advancement) this, path, new FixedTranslation(name));
            AdvancementTreeRegistryModule.getInstance().registerAdditionalCatalog(this.impl$tree);
        } else {
            this.impl$tree = ((org.spongepowered.api.advancement.Advancement) parentIn).getTree().orElse(null);
        }
        this.impl$text = SpongeTexts.toText(this.displayText);
        final ImmutableList.Builder<Text> toastText = ImmutableList.builder();
        if (this.display != null) {
            final FrameType frameType = this.display.func_192291_d();
            toastText.add(Text.builder(new SpongeTranslation("advancements.toast." + frameType.func_192307_a()))
                    .format(((AdvancementType) (Object) frameType).getTextFormat())
                    .build());
            toastText.add(((ITextComponentBridge) this.display.func_192297_a()).bridge$toText());
        } else {
            toastText.add(Text.of("Unlocked advancement"));
            toastText.add(Text.of(this.impl$spongeId));
        }
        this.impl$toastText = toastText.build();
        final Set<String> scoreCriteria = new HashSet<>();
        final Map<String, DefaultedAdvancementCriterion> criterionMap = new HashMap<>();
        for (final Map.Entry<String, Criterion> entry : new HashMap<>(criteriaIn).entrySet()) {
            final CriterionBridge mixinCriterion = (CriterionBridge) entry.getValue();
            final DefaultedAdvancementCriterion criterion;
            if (mixinCriterion.bridge$getScoreGoal() != null) {
                criterion = new SpongeScoreCriterion(entry.getKey(), mixinCriterion.bridge$getScoreGoal(),
                        entry.getValue().func_192143_a());
                scoreCriteria.add(entry.getKey());
                ((SpongeScoreCriterion) criterion).internalCriteria.forEach(
                        criterion1 -> criteriaIn.put(criterion1.getName(), (Criterion) criterion1));
            } else {
                criterion = (DefaultedAdvancementCriterion) mixinCriterion;
                ((CriterionBridge) criterion).bridge$setName(entry.getKey());
            }
            criterionMap.put(entry.getKey(), criterion);
        }
        final List<String[]> entries = new ArrayList<>();
        final List<AdvancementCriterion> andCriteria = new ArrayList<>();
        for (final String[] array : requirementsIn) {
            final Set<AdvancementCriterion> orCriteria = new HashSet<>();
            for (final String name : array) {
                final DefaultedAdvancementCriterion criterion = criterionMap.get(name);
                if (criterion instanceof SpongeScoreCriterion) {
                    ((SpongeScoreCriterion) criterion).internalCriteria.forEach(
                            criterion1 -> entries.add(new String[] { criterion1.getName() }));
                } else {
                    entries.add(new String[] { criterion.getName() });
                }
                orCriteria.add(criterion);
            }
            andCriteria.add(OrCriterion.of(orCriteria));
        }
        this.impl$criterion = AndCriterion.of(andCriteria);
        if (!scoreCriteria.isEmpty()) {
            scoreCriteria.forEach(criteriaIn::remove);
            this.criteria = ImmutableMap.copyOf(criteriaIn);
            this.requirements = entries.toArray(new String[entries.size()][]);
        }
    }

    @Override
    public Optional<AdvancementTree> bridge$getTree() {
        checkState(SpongeImplHooks.isMainThread());
        return Optional.ofNullable(this.impl$tree);
    }

    @Override
    public void bridge$setParent(@Nullable final Advancement advancement) {
        checkState(SpongeImplHooks.isMainThread());
        this.parent = advancement;
    }

    @Override
    public void bridge$setTree(final AdvancementTree tree) {
        checkState(SpongeImplHooks.isMainThread());
        this.impl$tree = tree;
    }

    @Override
    public AdvancementCriterion bridge$getCriterion() {
        checkState(SpongeImplHooks.isMainThread());
        return this.impl$criterion;
    }

    @Override
    public void bridge$setCriterion(final AdvancementCriterion criterion) {
        checkState(SpongeImplHooks.isMainThread());
        this.impl$criterion = criterion;
    }

    @Override
    public void bridge$setName(final String name) {
        checkState(SpongeImplHooks.isMainThread());
        this.impl$name = name;
    }

    @Override
    public boolean bridge$isRegistered() {
        checkState(SpongeImplHooks.isMainThread());
        return this.impl$tempParent == null;
    }

    @Override
    public void bridge$setRegistered() {
        checkState(SpongeImplHooks.isMainThread());
        if (this.impl$tempParent == null) {
            return;
        }
        this.parent = this.impl$tempParent;
        // The child didn't get added yet to it's actual parent
        this.parent.func_192071_a((Advancement) (Object) this);
        this.impl$tempParent = null;
    }

    @Override
    public Optional<Advancement> bridge$getParent() {
        checkState(SpongeImplHooks.isMainThread());
        if (this.impl$tempParent != null) {
            return Optional.of(this.impl$tempParent);
        }
        if (this.parent == AdvancementRegistryModule.DUMMY_ROOT_ADVANCEMENT) {
            return Optional.empty();
        }
        return Optional.ofNullable(this.parent);
    }



    @Override
    public List<Text> bridge$getToastText() {
        checkState(SpongeImplHooks.isMainThread());
        return this.impl$toastText;
    }

    @Override
    public String bridge$getId() {
        checkState(SpongeImplHooks.isMainThread());
        return this.impl$spongeId;
    }

    @Override
    public String bridge$getName() {
        checkState(SpongeImplHooks.isMainThread());
        return this.impl$name;
    }

    @Override
    public Text bridge$getText() {
        checkState(SpongeImplHooks.isMainThread());
        return this.impl$text;
    }
}
