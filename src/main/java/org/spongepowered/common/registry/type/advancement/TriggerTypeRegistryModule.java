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

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.ICriterionTrigger;
import org.spongepowered.api.advancement.criteria.trigger.Trigger;
import org.spongepowered.api.advancement.criteria.trigger.Triggers;
import org.spongepowered.api.registry.AdditionalCatalogRegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.common.mixin.core.advancements.CriteriaTriggersAccessor;
import org.spongepowered.common.registry.type.AbstractPrefixAlternateCatalogTypeRegistryModule;

@SuppressWarnings("rawtypes")
@RegisterCatalog(Triggers.class)
public class TriggerTypeRegistryModule extends AbstractPrefixAlternateCatalogTypeRegistryModule<Trigger>
        implements AdditionalCatalogRegistryModule<Trigger> {

    public static TriggerTypeRegistryModule getInstance() {
        return Holder.INSTANCE;
    }

    TriggerTypeRegistryModule() {
        super("minecraft");
    }

    @Override
    public void register(final Trigger triggerType) {
        super.register(triggerType);
    }

    @Override
    public void registerAdditionalCatalog(final Trigger triggerType) {
        // Register on CriterionTriggers, that register method will
        // delegate to the register method within this module
        CriteriaTriggersAccessor.accessor$register((ICriterionTrigger) triggerType);
    }

    @Override
    public void registerDefaults() {
        // Force the vanilla trigger types to load
        CriteriaTriggers.func_192120_a();
    }

    private static final class Holder {
        static final TriggerTypeRegistryModule INSTANCE = new TriggerTypeRegistryModule();
        static {
            // Just need to class initialize
            CriteriaTriggers.func_192120_a();
        }
    }
}
