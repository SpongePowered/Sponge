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

import static com.google.common.base.Preconditions.checkState;

import com.google.common.collect.ImmutableMap;
import net.minecraft.advancements.AdvancementList;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ImpossibleTrigger;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.api.advancement.Advancement;
import org.spongepowered.api.registry.AdditionalCatalogRegistryModule;
import org.spongepowered.api.registry.util.RegistrationDependency;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.bridge.advancements.AdvancementBridge;
import org.spongepowered.common.bridge.advancements.AdvancementListBridge;
import org.spongepowered.common.mixin.core.advancements.AdvancementManagerAccessor;
import org.spongepowered.common.registry.CustomRegistrationPhase;
import org.spongepowered.common.registry.type.AbstractPrefixCheckCatalogRegistryModule;

import java.util.Map;

@SuppressWarnings("unchecked")
@CustomRegistrationPhase
@RegistrationDependency(CriterionRegistryModule.class)
public class AdvancementRegistryModule extends AbstractPrefixCheckCatalogRegistryModule<Advancement>
        implements AdditionalCatalogRegistryModule<Advancement> {

    private static final Criterion dummyCriterion = new Criterion(new ImpossibleTrigger.Instance());
    private static final String dummyCriterionName = "dummy_root_criterion";
    private static final Map<String, Criterion> dummyCriteria = ImmutableMap.of(dummyCriterionName, dummyCriterion);
    private static final String[][] dummyRequirements = {{ dummyCriterionName }};

    // Putting it here to make sure that initialized outside any of the events to prevent it from being registered
    public static net.minecraft.advancements.Advancement DUMMY_ROOT_ADVANCEMENT;

    public static AdvancementRegistryModule getInstance() {
        return Holder.INSTANCE;
    }

    AdvancementRegistryModule() {
        super("minecraft");
    }

    private static AdvancementListBridge getAdvancementList() {
        return (AdvancementListBridge) AdvancementManagerAccessor.accessor$getAdvancementList();
    }

    @Override
    public void registerDefaults() {
        DUMMY_ROOT_ADVANCEMENT = new net.minecraft.advancements.Advancement(
                new ResourceLocation("sponge", "dummy_root"), null, null,
                AdvancementRewards.field_192114_a, dummyCriteria, dummyRequirements) {
            @Override
            public void func_192071_a(net.minecraft.advancements.Advancement child) {
                // Prevent children to be added so that there
                // aren't any leftover references from this instance
            }
        };
    }

    @Override
    public void registerAdditionalCatalog(Advancement advancement) {
        checkState(SpongeImplHooks.isMainThread());
        ((AdvancementBridge) advancement).bridge$setRegistered();
        final net.minecraft.advancements.Advancement mcAdv = (net.minecraft.advancements.Advancement) advancement;
        final AdvancementListBridge advList = getAdvancementList();
        advList.bridge$getAdvancements().put(mcAdv.func_192067_g(), mcAdv);
        // If the parent != null, that means that its not a root advancement
        if (mcAdv.func_192070_b() != null && mcAdv.func_192070_b() != DUMMY_ROOT_ADVANCEMENT &&
                advList.bridge$getNonRootsSet().add(mcAdv)) { // Only update if the root wasn't already present for some reason
            final AdvancementList.Listener listener = advList.bridge$getListener();
            if (listener != null) {
                listener.func_191932_c(mcAdv);
            }
        }
    }

    void registerSilently(net.minecraft.advancements.Advancement advancement) {
        super.register((Advancement) advancement);
    }

    void remove(net.minecraft.advancements.Advancement advancement) {
        this.catalogTypeMap.remove(advancement.func_192067_g().toString());
    }

    void clear() {
        this.catalogTypeMap.clear();
    }

    private static final class Holder {
        static final AdvancementRegistryModule INSTANCE = new AdvancementRegistryModule();
        static {
            try {
                Class.forName("net.minecraft.advancements.AdvancementManager");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
