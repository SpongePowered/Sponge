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
package org.spongepowered.forge.mixin.core.minecraftforge.registries;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.GameData;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.Sponge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.entity.SpongeEntityTypes;
import org.spongepowered.common.launch.Launch;
import org.spongepowered.common.launch.Lifecycle;
import org.spongepowered.common.network.channel.SpongeChannelManager;
import org.spongepowered.common.network.packet.SpongePacketHandler;

@Mixin(GameData.class)
public class GameDataMixin_Forge {

    @Inject(method = "postRegisterEvents", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/fml/ModLoader;postEventWrapContainerInModOrder(Lnet/minecraftforge/eventbus/api/Event;)V", shift = At.Shift.AFTER))
    private static void forge$registerSpongeTypesLast(final CallbackInfo ci, @Nullable @Local final Registry<?> vanillaRegistry) {
        if (vanillaRegistry != null && Registries.ENTITY_TYPE.equals(vanillaRegistry.key())) {
            SpongeEntityTypes.register((Registry<EntityType<?>>) vanillaRegistry);
        }
    }

    @Inject(method = "postRegisterEvents", at = @At("HEAD"))
    private static void forge$onRegisterEvents(final CallbackInfo ci) {
        final Lifecycle lifecycle = Launch.instance().lifecycle();
        lifecycle.callConstructEvent();
        lifecycle.callRegisterFactoryEvent();
        lifecycle.callRegisterBuilderEvent();
        lifecycle.callRegisterChannelEvent();
        lifecycle.establishGameServices();
        lifecycle.establishDataKeyListeners();

        SpongePacketHandler.init((SpongeChannelManager) Sponge.channelManager());

        lifecycle.establishDataProviders();
        lifecycle.callRegisterDataEvent();
        lifecycle.establishEarlyGlobalRegistries();

        Launch.instance().lifecycle().establishGlobalRegistries();
    }

    @Inject(method = "freezeData", at = @At("TAIL"))
    private static void forge$onFreezeData(final CallbackInfo ci) {
        Launch.instance().lifecycle().endEstablishGlobalRegistries();
    }
}
