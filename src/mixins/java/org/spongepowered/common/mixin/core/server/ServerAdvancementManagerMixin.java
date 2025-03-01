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
package org.spongepowered.common.mixin.core.server;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementNode;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.advancement.AdvancementTree;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.advancement.AdvancementTreeEvent;
import org.spongepowered.api.registry.Registry;
import org.spongepowered.api.registry.RegistryEntry;
import org.spongepowered.api.registry.RegistryHolder;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.launch.Launch;
import org.spongepowered.common.registry.RegistryHolderLogic;
import org.spongepowered.common.registry.SpongeRegistryHolder;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mixin(ServerAdvancementManager.class)
public abstract class ServerAdvancementManagerMixin {

    @SuppressWarnings({"unchecked"})
    @WrapMethod(method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V")
    private void impl$onApply(final Map<ResourceLocation, Advancement> $$0, final ResourceManager $$1, final ProfilerFiller $$2, final Operation<Void> original) {
        final RegistryHolderLogic registryHolder = ((SpongeRegistryHolder) $$1).registryHolder();
        final Registry<Advancement> registry = (Registry<Advancement>) (Object) registryHolder.registry(RegistryTypes.ADVANCEMENT);
        $$0.forEach((k, v) -> registry.register((ResourceKey) (Object) k, v));
        Launch.instance().lifecycle().processServerRegistries((RegistryHolder) $$1, Stream.of(registry));
        original.call(registry.streamEntries().collect(Collectors.toMap(RegistryEntry::key, RegistryEntry::value)), $$1, $$2);
    }

    @WrapOperation(method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/advancements/TreeNodePosition;run(Lnet/minecraft/advancements/AdvancementNode;)V"))
    private void impl$onLayout(final AdvancementNode instance, final Operation<Void> original,
            final Map<ResourceLocation, Advancement> $$0, final ResourceManager $$1, final ProfilerFiller $$2) {
        original.call(instance);

        final Cause cause = PhaseTracker.getInstance().currentCause();
        final AdvancementTreeEvent.GenerateLayout event = SpongeEventFactory.createAdvancementTreeEventGenerateLayout(
            cause, ((SpongeRegistryHolder) $$1).registryHolder(), (AdvancementTree) instance);
        SpongeCommon.post(event);
    }
}
