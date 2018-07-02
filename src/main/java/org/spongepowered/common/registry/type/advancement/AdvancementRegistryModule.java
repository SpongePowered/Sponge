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
package org.spongepowered.common.registry.type.advancement;

import com.google.common.collect.ImmutableMap;
import net.minecraft.advancements.AdvancementList;
import net.minecraft.advancements.AdvancementManager;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ImpossibleTrigger;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.api.advancement.Advancement;
import org.spongepowered.api.registry.AdditionalCatalogRegistryModule;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.interfaces.advancement.IMixinAdvancement;
import org.spongepowered.common.interfaces.advancement.IMixinAdvancementList;
import org.spongepowered.common.registry.CustomRegistrationPhase;
import org.spongepowered.common.registry.type.AbstractPrefixCheckCatalogRegistryModule;
import org.spongepowered.common.util.ServerUtils;

import java.util.Map;

@CustomRegistrationPhase
public class AdvancementRegistryModule extends AbstractPrefixCheckCatalogRegistryModule<Advancement>
        implements AdditionalCatalogRegistryModule<Advancement> {

    private static final Criterion dummyCriterion = new Criterion(new ImpossibleTrigger.Instance());
    private static final String dummyCriterionName = "dummy_root_criterion";
    private static final Map<String, Criterion> dummyCriteria = ImmutableMap.of(dummyCriterionName, dummyCriterion);
    private static final String[][] dummyRequirements = {{ dummyCriterionName }};

    // Putting it here to make sure that initialized outside any of the events to prevent it from being registered
    public static final net.minecraft.advancements.Advancement DUMMY_ROOT_ADVANCEMENT = new net.minecraft.advancements.Advancement(
            new ResourceLocation("sponge", "dummy_root"), null, null,
            AdvancementRewards.EMPTY, dummyCriteria, dummyRequirements) {
        @Override
        public void addChild(net.minecraft.advancements.Advancement child) {
            // Prevent children to be added so that there
            // aren't any leftover references from this instance
        }
    };

    public static AdvancementRegistryModule getInstance() {
        return Holder.INSTANCE;
    }

    AdvancementRegistryModule() {
        super("minecraft");
    }

    public void clear() {
        this.catalogTypeMap.clear();
    }

    @Override
    public void registerAdditionalCatalog(Advancement advancement) {
        super.register(advancement);
        ((IMixinAdvancement) advancement).setRegistered();
        if (ServerUtils.isCallingFromMainThread() && PhaseTracker.getInstance().getCurrentState().isEvent()) {
            final net.minecraft.advancements.Advancement mcAdv = (net.minecraft.advancements.Advancement) advancement;
            final IMixinAdvancementList advancementList = (IMixinAdvancementList) AdvancementManager.ADVANCEMENT_LIST;
            advancementList.getAdvancements().put(mcAdv.getId(), mcAdv);
            // If the parent != null, that means that its not a root advancement
            if (mcAdv.getParent() != null && mcAdv.getParent() != DUMMY_ROOT_ADVANCEMENT &&
                    advancementList.getNonRootsSet().add(mcAdv)) { // Only update if the root wasn't already present for some reason
                final AdvancementList.Listener listener = advancementList.getListener();
                if (listener != null) {
                    listener.nonRootAdvancementAdded(mcAdv);
                }
            }
        }
    }

    private static final class Holder {
        static final AdvancementRegistryModule INSTANCE = new AdvancementRegistryModule();
    }
}
