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
package org.spongepowered.common.mixin.invalid.core.advancements;

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
import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.advancement.AdvancementTree;
import org.spongepowered.api.advancement.AdvancementType;
import org.spongepowered.api.advancement.criteria.AdvancementCriterion;
import org.spongepowered.api.advancement.criteria.AndCriterion;
import org.spongepowered.api.advancement.criteria.OrCriterion;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.translation.FixedTranslation;
import org.spongepowered.api.text.translation.Translation;
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
import org.spongepowered.common.bridge.advancements.CriterionBridge;
import org.spongepowered.common.bridge.advancements.DisplayInfoBridge;
import org.spongepowered.common.bridge.util.text.TextComponentBridge;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.phase.plugin.ListenerPhaseContext;
import org.spongepowered.common.registry.type.advancement.AdvancementRegistryModule;
import org.spongepowered.common.registry.type.advancement.AdvancementTreeRegistryModule;
import org.spongepowered.common.text.SpongeTexts;
import org.spongepowered.common.text.translation.SpongeTranslation;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Mixin(Advancement.class)
public abstract class AdvancementMixin implements AdvancementBridge {

    @Shadow @Final @Mutable @Nullable private Advancement parent;
    @Shadow @Final @Mutable private String[][] requirements;
    @Shadow @Final @Mutable private Map<String, Criterion> criteria;
    @Shadow @Final @Nullable private DisplayInfo display;
    @Shadow @Final private ITextComponent displayText;

    private CatalogKey impl$key;
    private AdvancementCriterion impl$criterion;
    @Nullable private AdvancementTree impl$tree;
    private List<Text> impl$toastText;
    private Text impl$text;
    private Translation impl$translation;
    @Nullable private Advancement impl$tempParent;

    @SuppressWarnings({"ConstantConditions"})
    @Inject(method = "<init>", at = @At("RETURN"))
    private void impl$setUpSpongeFields(ResourceLocation location, @Nullable Advancement parent, @Nullable DisplayInfo displayInfo,
            AdvancementRewards rewards, Map<String, Criterion> criteria, String[][] requirements, CallbackInfo ci) {
        // Don't do anything on the client, unless we're performing registry initialization
        if (!SpongeImplHooks.onServerThread()) {
            return;
        }
        this.impl$key = (CatalogKey) (Object) location;
        if (displayInfo != null) {
            ((DisplayInfoBridge) displayInfo).bridge$setAdvancement((org.spongepowered.api.advancement.Advancement) this);
        }
        this.impl$translation = new SpongeTranslation(location.getPath().replace('/', '_'));
        if (displayInfo != null) {
            this.impl$translation = new FixedTranslation(SpongeTexts.toPlain(displayInfo.getTitle()));
        }
        if (PhaseTracker.getInstance().getCurrentState().isEvent()) {
            final Object event = ((ListenerPhaseContext<?>) PhaseTracker.getInstance().getCurrentContext()).getEvent();
            if (event instanceof GameRegistryEvent.Register) {
                final Class<? extends CatalogType> catalogType = ((GameRegistryEvent.Register<?>) event).getCatalogType();
                if (catalogType.equals(org.spongepowered.api.advancement.Advancement.class) || catalogType.equals(AdvancementTree.class)) {
                    // Wait to set the parent until the advancement is registered
                    this.impl$tempParent = parent;
                    this.parent = AdvancementRegistryModule.DUMMY_ROOT_ADVANCEMENT;
                }
            }
        }

        String fixedPath = this.impl$key.getValue();
        // This is only possible when REGISTER_ADVANCEMENTS_ON_CONSTRUCT is true
        if (parent == null) {
            // Remove the root suffix from json file tree ids
            if (fixedPath.endsWith("/root")) {
                fixedPath = fixedPath.substring(0, fixedPath.lastIndexOf('/'));
            }
            fixedPath = fixedPath.replace('/', '_');
            this.impl$tree = new SpongeAdvancementTree((org.spongepowered.api.advancement.Advancement) this, CatalogKey.of(this.impl$key
                    .getNamespace(), fixedPath), displayInfo != null ? this.impl$translation : new FixedTranslation(fixedPath));
            AdvancementTreeRegistryModule.getInstance().registerAdditionalCatalog(this.impl$tree);
        } else {
            this.impl$tree = ((org.spongepowered.api.advancement.Advancement) parent).getTree().orElse(null);
        }
        this.impl$text = SpongeTexts.toText(this.displayText);
        final ImmutableList.Builder<Text> toastText = ImmutableList.builder();
        if (this.display != null) {
            final FrameType frameType = this.display.getFrame();
            toastText.add(Text.builder(new SpongeTranslation("advancements.toast." + frameType.getName()))
                    .format(((AdvancementType) (Object) frameType).getTextFormat())
                    .build());
            toastText.add(((TextComponentBridge) this.display.getTitle()).bridge$toText());
        } else {
            toastText.add(Text.of("Unlocked advancement"));
            toastText.add(Text.of(this.impl$key));
        }
        this.impl$toastText = toastText.build();
        final Set<String> scoreCriteria = new HashSet<>();
        final Map<String, DefaultedAdvancementCriterion> criterionMap = new HashMap<>();
        for (final Map.Entry<String, Criterion> entry : new HashMap<>(criteria).entrySet()) {
            final CriterionBridge mixinCriterion = (CriterionBridge) entry.getValue();
            final DefaultedAdvancementCriterion criterion;
            if (mixinCriterion.bridge$getScoreGoal() != null) {
                criterion = new SpongeScoreCriterion(entry.getKey(), mixinCriterion.bridge$getScoreGoal(),
                        entry.getValue().getCriterionInstance());
                scoreCriteria.add(entry.getKey());
                ((SpongeScoreCriterion) criterion).internalCriteria.forEach(
                        criterion1 -> criteria.put(criterion1.getName(), (Criterion) criterion1));
            } else {
                criterion = (DefaultedAdvancementCriterion) mixinCriterion;
                ((CriterionBridge) criterion).bridge$setName(entry.getKey());
            }
            criterionMap.put(entry.getKey(), criterion);
        }
        final List<String[]> entries = new ArrayList<>();
        final List<AdvancementCriterion> andCriteria = new ArrayList<>();
        for (final String[] array : requirements) {
            final Set<AdvancementCriterion> orCriteria = new HashSet<>();
            for (final String name : array) {
                final DefaultedAdvancementCriterion criterion = criterionMap.get(name);
                if (criterion instanceof SpongeScoreCriterion) {
                    ((SpongeScoreCriterion) criterion).internalCriteria.forEach(
                            criterion1 -> entries.add(new String[]{criterion1.getName()}));
                } else {
                    entries.add(new String[]{criterion.getName()});
                }
                orCriteria.add(criterion);
            }
            andCriteria.add(OrCriterion.of(orCriteria));
        }
        this.impl$criterion = AndCriterion.of(andCriteria);
        if (!scoreCriteria.isEmpty()) {
            scoreCriteria.forEach(criteria::remove);
            this.criteria = ImmutableMap.copyOf(criteria);
            this.requirements = entries.toArray(new String[entries.size()][]);
        }
    }

    @Override
    public CatalogKey bridge$getKey() {
        return this.impl$key;
    }

    @Override
    public void bridge$setTranslation(Translation name) {
        checkState(SpongeImplHooks.onServerThread());
        this.impl$translation = name;
    }

    @Override
    public Optional<Advancement> bridge$getParent() {
        checkState(SpongeImplHooks.onServerThread());
        if (this.impl$tempParent != null) {
            return Optional.of(this.impl$tempParent);
        }
        if (this.parent == AdvancementRegistryModule.DUMMY_ROOT_ADVANCEMENT) {
            return Optional.empty();
        }
        return Optional.ofNullable(this.parent);
    }

    @Override
    public void bridge$setParent(@Nullable final Advancement advancement) {
        checkState(SpongeImplHooks.onServerThread());
        this.parent = advancement;
    }

    @Override
    public Optional<AdvancementTree> bridge$getTree() {
        checkState(SpongeImplHooks.onServerThread());
        return Optional.ofNullable(this.impl$tree);
    }

    @Override
    public void bridge$setTree(final AdvancementTree tree) {
        checkState(SpongeImplHooks.onServerThread());
        this.impl$tree = tree;
    }

    @Override
    public AdvancementCriterion bridge$getCriterion() {
        checkState(SpongeImplHooks.onServerThread());
        return this.impl$criterion;
    }

    @Override
    public void bridge$setCriterion(final AdvancementCriterion criterion) {
        checkState(SpongeImplHooks.onServerThread());
        this.impl$criterion = criterion;
    }

    @Override
    public boolean bridge$isRegistered() {
        checkState(SpongeImplHooks.onServerThread());
        return this.impl$tempParent == null;
    }

    @Override
    public void bridge$setRegistered() {
        checkState(SpongeImplHooks.onServerThread());
        if (this.impl$tempParent == null) {
            return;
        }
        this.parent = this.impl$tempParent;
        // The child didn't get added yet to it's actual parent
        this.parent.addChild((Advancement) (Object) this);
        this.impl$tempParent = null;
    }

    @Override
    public Text bridge$getText() {
        checkState(SpongeImplHooks.onServerThread());
        return this.impl$text;
    }

    @Override
    public List<Text> bridge$getToastText() {
        checkState(SpongeImplHooks.onServerThread());
        return this.impl$toastText;
    }
}
