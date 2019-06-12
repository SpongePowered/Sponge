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
import org.spongepowered.common.advancement.CriterionBridge;
import org.spongepowered.common.advancement.SpongeAdvancementTree;
import org.spongepowered.common.advancement.SpongeScoreCriterion;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.phase.plugin.ListenerPhaseContext;
import org.spongepowered.common.interfaces.advancement.IMixinAdvancement;
import org.spongepowered.common.interfaces.advancement.IMixinCriterion;
import org.spongepowered.common.interfaces.advancement.IMixinDisplayInfo;
import org.spongepowered.common.registry.type.advancement.AdvancementRegistryModule;
import org.spongepowered.common.registry.type.advancement.AdvancementTreeRegistryModule;
import org.spongepowered.common.text.SpongeTexts;
import org.spongepowered.common.text.translation.SpongeTranslation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nullable;

@Mixin(Advancement.class)
public class MixinAdvancement implements org.spongepowered.api.advancement.Advancement, IMixinAdvancement {

    @Shadow @Final @Mutable @Nullable private Advancement parent;
    @Shadow @Final @Mutable private String[][] requirements;
    @Shadow @Final @Mutable private Map<String, Criterion> criteria;
    @Shadow @Final @Nullable private DisplayInfo display;
    @Shadow @Final private Set<Advancement> children;
    @Shadow @Final private ITextComponent displayText;

    private AdvancementCriterion criterion;
    @Nullable private AdvancementTree tree;
    private List<Text> toastText;
    private Text text;
    private String spongeId;
    private String name;

    @Nullable private Advancement tempParent;

    private void checkServer() {
        checkState(this.isMainThread());
    }

    private boolean isMainThread() {
        return SpongeImplHooks.isMainThread();
    }

    @SuppressWarnings("ConstantConditions")
    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(ResourceLocation id, @Nullable Advancement parentIn, @Nullable DisplayInfo displayIn,
            AdvancementRewards rewardsIn, Map<String, Criterion> criteriaIn, String[][] requirementsIn, CallbackInfo ci) {
        // Don't do anything on the client, unless we're performing registry initialization
        if (!this.isMainThread()) {
            return;
        }
        if (displayIn != null) {
            ((IMixinDisplayInfo) displayIn).setAdvancement(this);
        }
        String path = id.getPath();
        this.name = path.replace('/', '_');
        this.spongeId = id.getNamespace() + ':' + this.name;
        if (displayIn != null) {
            this.name = SpongeTexts.toPlain(displayIn.getTitle());
        }
        if (PhaseTracker.getInstance().getCurrentState().isEvent()) {
            Object event = ((ListenerPhaseContext) PhaseTracker.getInstance().getCurrentContext()).getEvent();
            if (event instanceof GameRegistryEvent.Register) {
                Class<? extends CatalogType> catalogType = ((GameRegistryEvent.Register) event).getCatalogType();
                if (catalogType.equals(org.spongepowered.api.advancement.Advancement.class) || catalogType.equals(AdvancementTree.class)) {
                    // Wait to set the parent until the advancement is registered
                    this.tempParent = parentIn;
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
                name = this.name;
            }
            path = id.getNamespace() + ':' + path;
            this.tree = new SpongeAdvancementTree(this, path, new FixedTranslation(name));
            AdvancementTreeRegistryModule.getInstance().registerAdditionalCatalog(this.tree);
        } else {
            this.tree = ((org.spongepowered.api.advancement.Advancement) parentIn).getTree().orElse(null);
        }
        this.text = SpongeTexts.toText(this.displayText);
        final ImmutableList.Builder<Text> toastText = ImmutableList.builder();
        if (this.display != null) {
            final FrameType frameType = this.display.getFrame();
            toastText.add(Text.builder(new SpongeTranslation("advancements.toast." + frameType.getName()))
                    .format(((AdvancementType) (Object) frameType).getTextFormat())
                    .build());
            toastText.add(getDisplayInfo().get().getTitle());
        } else {
            toastText.add(Text.of("Unlocked advancement"));
            toastText.add(Text.of(getId()));
        }
        this.toastText = toastText.build();
        final Set<String> scoreCriteria = new HashSet<>();
        final Map<String, CriterionBridge> criterionMap = new HashMap<>();
        for (Map.Entry<String, Criterion> entry : new HashMap<>(criteriaIn).entrySet()) {
            final IMixinCriterion mixinCriterion = (IMixinCriterion) entry.getValue();
            final CriterionBridge criterion;
            if (mixinCriterion.getScoreGoal() != null) {
                criterion = new SpongeScoreCriterion(entry.getKey(), mixinCriterion.getScoreGoal(),
                        entry.getValue().getCriterionInstance());
                scoreCriteria.add(entry.getKey());
                ((SpongeScoreCriterion) criterion).internalCriteria.forEach(
                        criterion1 -> criteriaIn.put(criterion1.getName(), (Criterion) criterion1));
            } else {
                criterion = (CriterionBridge) mixinCriterion;
                ((IMixinCriterion) criterion).setName(entry.getKey());
            }
            criterionMap.put(entry.getKey(), criterion);
        }
        final List<String[]> entries = new ArrayList<>();
        final List<AdvancementCriterion> andCriteria = new ArrayList<>();
        for (String[] array : requirementsIn) {
            final Set<AdvancementCriterion> orCriteria = new HashSet<>();
            for (String name : array) {
                final CriterionBridge criterion = criterionMap.get(name);
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
        this.criterion = AndCriterion.of(andCriteria);
        if (!scoreCriteria.isEmpty()) {
            scoreCriteria.forEach(criteriaIn::remove);
            this.criteria = ImmutableMap.copyOf(criteriaIn);
            this.requirements = entries.toArray(new String[entries.size()][]);
        }
    }

    @Override
    public Optional<AdvancementTree> getTree() {
        checkServer();
        return Optional.ofNullable(this.tree);
    }

    @Override
    public void setParent(@Nullable Advancement advancement) {
        checkServer();
        this.parent = advancement;
    }

    @Override
    public void setTree(AdvancementTree tree) {
        checkServer();
        this.tree = tree;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Collection<org.spongepowered.api.advancement.Advancement> getChildren() {
        checkServer();
        return ImmutableList.copyOf((Collection) this.children);
    }

    @Override
    public AdvancementCriterion getCriterion() {
        checkServer();
        return this.criterion;
    }

    @Override
    public void setCriterion(AdvancementCriterion criterion) {
        checkServer();
        this.criterion = criterion;
    }

    @Override
    public void setName(String name) {
        checkServer();
        this.name = name;
    }

    @Override
    public boolean isRegistered() {
        checkServer();
        return this.tempParent == null;
    }

    @Override
    public void setRegistered() {
        checkServer();
        if (this.tempParent == null) {
            return;
        }
        this.parent = this.tempParent;
        // The child didn't get added yet to it's actual parent
        this.parent.addChild((Advancement) (Object) this);
        this.tempParent = null;
    }

    @Override
    public Optional<org.spongepowered.api.advancement.Advancement> getParent() {
        checkServer();
        if (this.tempParent != null) {
            return Optional.of((org.spongepowered.api.advancement.Advancement) this.tempParent);
        }
        if (this.parent == AdvancementRegistryModule.DUMMY_ROOT_ADVANCEMENT) {
            return Optional.empty();
        }
        return Optional.ofNullable((org.spongepowered.api.advancement.Advancement) this.parent);
    }

    @Override
    public Optional<org.spongepowered.api.advancement.DisplayInfo> getDisplayInfo() {
        checkServer();
        return Optional.ofNullable((org.spongepowered.api.advancement.DisplayInfo) this.display);
    }

    @Override
    public List<Text> toToastText() {
        checkServer();
        return this.toastText;
    }

    @Override
    public String getId() {
        checkServer();
        return this.spongeId;
    }

    @Override
    public String getName() {
        checkServer();
        return this.name;
    }

    @Override
    public Text toText() {
        checkServer();
        return this.text;
    }
}
