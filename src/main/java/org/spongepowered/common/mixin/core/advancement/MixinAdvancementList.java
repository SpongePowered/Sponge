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

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementList;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.advancement.AdvancementTree;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.event.registry.SpongeGameRegistryRegisterEvent;
import org.spongepowered.common.event.tracking.phase.plugin.ListenerPhaseContext;
import org.spongepowered.common.event.tracking.phase.plugin.PluginPhase;
import org.spongepowered.common.interfaces.advancement.IMixinAdvancementList;
import org.spongepowered.common.registry.type.advancement.AdvancementRegistryModule;
import org.spongepowered.common.registry.type.advancement.AdvancementTreeRegistryModule;
import org.spongepowered.common.util.ServerUtils;

import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

@Mixin(AdvancementList.class)
public class MixinAdvancementList implements IMixinAdvancementList {

    @Shadow @Final private static Logger LOGGER;

    @Shadow @Final private Map<ResourceLocation, Advancement> advancements;
    @Shadow @Final private Set<Advancement> roots;
    @Shadow @Final private Set<Advancement> nonRoots;
    @Shadow @Nullable private AdvancementList.Listener listener;

    @Inject(method = "loadAdvancements", at = @At(value = "INVOKE", shift = At.Shift.BEFORE,
            target = "Ljava/util/Map;size()I"))
    private void onLoadAdvancements(Map<ResourceLocation, Advancement.Builder> advancementsIn, CallbackInfo ci) {
        // Don't post events when loading advancements on the client
        if (!ServerUtils.isCallingFromMainThread()) {
            return;
        }
        final SpongeGameRegistryRegisterEvent<org.spongepowered.api.advancement.Advancement>
            event =
            new SpongeGameRegistryRegisterEvent<>(Cause.of(EventContext.empty(), SpongeImpl.getRegistry()),
                org.spongepowered.api.advancement.Advancement.class, AdvancementRegistryModule.getInstance());
        try (ListenerPhaseContext context = PluginPhase.Listener.GENERAL_LISTENER.createPhaseContext()
            .event(event)
            .source(Sponge.getGame())) {
            context.buildAndSwitch();
            SpongeImpl.postEvent(event);
        }
    }

    @Inject(method = "loadAdvancements", at = @At(value = "RETURN"))
    private void onLoadAdvancementForTrees(Map<ResourceLocation, Advancement.Builder> advancementsIn, CallbackInfo ci) {
        // Don't post events when loading advancements on the client
        if (!ServerUtils.isCallingFromMainThread()) {
            return;
        }
        final SpongeGameRegistryRegisterEvent<AdvancementTree>
            event =
            new SpongeGameRegistryRegisterEvent<>(Cause.of(EventContext.empty(), SpongeImpl.getRegistry()),
                AdvancementTree.class, AdvancementTreeRegistryModule.getInstance());
        try (ListenerPhaseContext context = PluginPhase.Listener.GENERAL_LISTENER.createPhaseContext()
            .event(event)
            .source(Sponge.getGame())) {
            context.buildAndSwitch();
            SpongeImpl.postEvent(event);
        }
        LOGGER.info("Loaded " + this.roots.size() + " advancement trees");
    }

    @Override
    public Map<ResourceLocation, Advancement> getAdvancements() {
        return this.advancements;
    }

    @Override
    public Set<Advancement> getRootsSet() {
        return this.roots;
    }

    @Override
    public Set<Advancement> getNonRootsSet() {
        return this.nonRoots;
    }

    @Override
    public AdvancementList.Listener getListener() {
        return this.listener;
    }
}
