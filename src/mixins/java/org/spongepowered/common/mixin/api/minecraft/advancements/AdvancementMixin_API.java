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
package org.spongepowered.common.mixin.api.minecraft.advancements;

import com.google.gson.JsonElement;
import net.kyori.adventure.text.Component;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.advancement.criteria.AdvancementCriterion;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.data.persistence.Queries;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.advancement.SpongeAdvancementTemplate;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.common.bridge.advancements.AdvancementBridge;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Mixin(Advancement.class)
public abstract class AdvancementMixin_API implements org.spongepowered.api.advancement.Advancement {

   @Shadow @Final private Optional<ResourceLocation> parent;
   @Shadow @Final private Optional<DisplayInfo> display;
   @Shadow @Final private AdvancementRewards rewards;
   @Shadow @Final private Map<String, Criterion<?>> criteria;
   @Shadow @Final private AdvancementRequirements requirements;
   @Shadow @Final private Optional<net.minecraft.network.chat.Component> name;


    @Override
    public AdvancementCriterion criterion() {
        return ((AdvancementBridge) this).bridge$getCriterion();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Optional<ResourceKey> parent() {
        return this.parent.map(ResourceKey.class::cast);
    }

    @Override
    public Optional<org.spongepowered.api.advancement.DisplayInfo> displayInfo() {
        return this.display.map(org.spongepowered.api.advancement.DisplayInfo.class::cast);
    }

    @Override
    public List<Component> toToastText() {
        return ((AdvancementBridge) this).bridge$getToastText();
    }

    @Override
    public Component asComponent() {
        return SpongeAdventure.asAdventure(this.name.orElse(net.minecraft.network.chat.Component.literal("no display name"))); // TODO can we get the resource key here?
    }

    @Override
    public int contentVersion() {
        return 0;
    }

    @Override
    public DataContainer toContainer() {
        final JsonElement json = SpongeAdvancementTemplate.encode((Advancement) (Object) this);
        try {
            final DataContainer container = DataFormats.JSON.get().read(json.toString());
            container.set(Queries.CONTENT_VERSION, this.contentVersion());
            return container;
        } catch (IOException e) {
            throw new IllegalStateException("Could not read deserialized Advancement:\n" + json, e);
        }
    }
}
