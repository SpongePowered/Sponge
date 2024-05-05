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


import com.google.common.collect.ImmutableList;
import net.kyori.adventure.text.Component;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.api.advancement.criteria.AdvancementCriterion;
import org.spongepowered.api.advancement.criteria.AndCriterion;
import org.spongepowered.api.advancement.criteria.OrCriterion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.advancement.criterion.DefaultedAdvancementCriterion;
import org.spongepowered.common.advancement.criterion.SpongeScoreCriterion;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.common.bridge.advancements.AdvancementBridge;
import org.spongepowered.common.bridge.advancements.CriterionBridge;
import org.spongepowered.common.bridge.advancements.DisplayInfoBridge;
import org.spongepowered.common.hooks.PlatformHooks;
import org.spongepowered.common.util.Preconditions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;


@Mixin(Advancement.class)
public abstract class AdvancementMixin implements AdvancementBridge {

    @Shadow @Final private Optional<DisplayInfo> display;
    private AdvancementCriterion impl$criterion;
    private List<Component> impl$toastText;

    @SuppressWarnings({"ConstantConditions"})
    @Inject(method = "<init>(Ljava/util/Optional;Ljava/util/Optional;Lnet/minecraft/advancements/AdvancementRewards;Ljava/util/Map;Lnet/minecraft/advancements/AdvancementRequirements;Z)V", at = @At("RETURN"))
    private void impl$setUpSpongeFields(final Optional<ResourceLocation> parent, final Optional<DisplayInfo> displayInfo, final AdvancementRewards $$2,
            final  Map<String, Criterion<?>> criteria, final AdvancementRequirements requirements, final boolean sendsTelemetryEvent, final CallbackInfo ci) {
        // Don't do anything on the client, unless we're performing registry initialization
        if (!PlatformHooks.INSTANCE.getGeneralHooks().onServerThread()) {
            return;
        }
        displayInfo.ifPresent(info -> ((DisplayInfoBridge) info).bridge$setAdvancement((org.spongepowered.api.advancement.Advancement) this));

        this.impl$toastText = this.impl$generateToastText();

        final Map<String, DefaultedAdvancementCriterion> criteriaMap = new LinkedHashMap<>();
        final Map<String, List<DefaultedAdvancementCriterion>> scoreCriteria = new HashMap<>();
        for (Map.Entry<String, Criterion<?>> entry : criteria.entrySet()) {
            final CriterionBridge mixinCriterion = (CriterionBridge) (Object) entry.getValue();
            final String groupName = mixinCriterion.bridge$getScoreCriterionName();
            if (groupName != null) {
                scoreCriteria.computeIfAbsent(groupName, k -> new ArrayList<>()).add((DefaultedAdvancementCriterion) (Object) entry.getValue());
            }

            criteriaMap.put(entry.getKey(), (DefaultedAdvancementCriterion) mixinCriterion);
            mixinCriterion.bridge$setName(entry.getKey());
        }
        for (Map.Entry<String, List<DefaultedAdvancementCriterion>> groupEntry : scoreCriteria.entrySet()) {
            criteriaMap.put(groupEntry.getKey(), new SpongeScoreCriterion(groupEntry.getKey(), groupEntry.getValue()));
            groupEntry.getValue().forEach(c -> criteriaMap.remove(c.name()));
        }

        final Set<AdvancementCriterion> andCriteria = new HashSet<>();
        for (final List<String> array : requirements.requirements()) {
            final Set<AdvancementCriterion> orCriteria = new HashSet<>();
            for (final String name : array) {
                DefaultedAdvancementCriterion criterion = criteriaMap.get(name);
                if (criterion == null && criteria.get(name) != null) { // internal removed by scoreCriterion
                    criterion = criteriaMap.get(((CriterionBridge) (Object) criteria.get(name)).bridge$getScoreCriterionName());
                }
                if (criterion != null) {
                    orCriteria.add(criterion);
                }
            }
            andCriteria.add(OrCriterion.of(orCriteria));
        }
        this.impl$criterion = AndCriterion.of(andCriteria);
    }

    private ImmutableList<Component> impl$generateToastText() {
        final ImmutableList.Builder<Component> toastText = ImmutableList.builder();
        if (this.display.isPresent()) {
            final AdvancementType frameType = this.display.get().getType();
            toastText.add(Component.translatable("advancements.toast." + frameType.getSerializedName(), SpongeAdventure.asAdventureNamed(frameType.getChatColor())));
            toastText.add(SpongeAdventure.asAdventure(this.display.get().getTitle()));
        } // else no display
        return toastText.build();
    }

    @Override
    public AdvancementCriterion bridge$getCriterion() {
        Preconditions.checkState(PlatformHooks.INSTANCE.getGeneralHooks().onServerThread());
        return this.impl$criterion;
    }

    @Override
    public void bridge$setCriterion(final AdvancementCriterion criterion) {
        // TODO this gets initially called on the "main" thread during RegisterDataPackValueEvent<AdvancementTemplate>
        //  checkState(PlatformHooks.INSTANCE.getGeneralHooks().onServerThread());
        this.impl$criterion = criterion;
    }

    @Override
    public List<Component> bridge$getToastText() {
        Preconditions.checkState(PlatformHooks.INSTANCE.getGeneralHooks().onServerThread());
        return this.impl$toastText;
    }

}
