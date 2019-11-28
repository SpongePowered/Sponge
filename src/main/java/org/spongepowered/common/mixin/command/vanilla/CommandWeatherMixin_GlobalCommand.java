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
package org.spongepowered.common.mixin.command.vanilla;

import net.minecraft.command.CommandWeather;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.WorldInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.SpongeImpl;

@Mixin(CommandWeather.class)
public abstract class CommandWeatherMixin_GlobalCommand {

    // Apply weather changes to all worlds

    @Redirect(method = "execute", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/storage/WorldInfo;setCleanWeatherTime(I)V"))
    private void globalCommand$setAllWorldInfoWeathers(final WorldInfo info, final int time) {
        for (final WorldServer world : SpongeImpl.getServer().field_71305_c) {
            world.func_72912_H().func_176142_i(time);
        }
    }

    @Redirect(method = "execute", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/storage/WorldInfo;setRainTime(I)V"))
    private void globalCommand$setAllWorldInfoRainTime(final WorldInfo info, final int time) {
        for (final WorldServer world : SpongeImpl.getServer().field_71305_c) {
            world.func_72912_H().func_76080_g(time);
        }
    }

    @Redirect(method = "execute", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/storage/WorldInfo;setThunderTime(I)V"))
    private void globalCommand$setAllWorldInfoThunderTime(final WorldInfo info, final int time) {
        for (final WorldServer world : SpongeImpl.getServer().field_71305_c) {
            world.func_72912_H().func_76090_f(time);
        }
    }

    @Redirect(method = "execute", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/storage/WorldInfo;setRaining(Z)V"))
    private void globalCommand$setAllWorldInfoRainingState(final WorldInfo info, final boolean state) {
        for (final WorldServer world : SpongeImpl.getServer().field_71305_c) {
            world.func_72912_H().func_76084_b(state);
        }
    }

    @Redirect(method = "execute", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/storage/WorldInfo;setThundering(Z)V"))
    private void globalCommand$setAllWorldThunderingState(final WorldInfo info, final boolean state) {
        for (final WorldServer world : SpongeImpl.getServer().field_71305_c) {
            world.func_72912_H().func_76069_a(state);
        }
    }

}
