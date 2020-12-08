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
package org.spongepowered.common.advancement.criterion;

import com.google.gson.JsonObject;
import net.minecraft.advancements.criterion.AbstractCriterionTrigger;
import net.minecraft.advancements.criterion.CriterionInstance;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.loot.ConditionArrayParser;
import net.minecraft.loot.ConditionArraySerializer;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.common.accessor.advancements.CriteriaTriggersAccessor;

public final class SpongeScoreTrigger extends AbstractCriterionTrigger<SpongeScoreTrigger.Instance> {

    public static final SpongeScoreTrigger SCORE_TRIGGER = CriteriaTriggersAccessor.accessor$register(new SpongeScoreTrigger(new ResourceLocation("sponge:score")));

    private final ResourceLocation resourceLocation;

    private SpongeScoreTrigger(final ResourceLocation resourceLocation) {
        this.resourceLocation = resourceLocation;
    }

    @Override
    public ResourceLocation getId() {
        return this.resourceLocation;
    }

    @Override
    protected Instance createInstance(final JsonObject jsonObject, final EntityPredicate.AndPredicate andPredicate,
            final ConditionArrayParser conditionArrayParser) {
        return new SpongeScoreTrigger.Instance(this.resourceLocation, andPredicate, -1);
    }

    public static class Instance extends CriterionInstance {

        private final int triggerTimes;

        public Instance(final ResourceLocation criterionIn, final EntityPredicate.AndPredicate andPredicate, final int triggerTimes) {
            super(criterionIn, andPredicate);
            this.triggerTimes = triggerTimes;
        }

        public static Instance of(int triggerTimes) {
            return new Instance(SpongeScoreTrigger.SCORE_TRIGGER.getId(), EntityPredicate.AndPredicate.ANY, triggerTimes);
        }

        @Override
        public JsonObject serializeToJson(final ConditionArraySerializer arraySerializer) {
            return new JsonObject();
        }

        public int getTriggerTimes() {
            return this.triggerTimes;
        }
    }
}
