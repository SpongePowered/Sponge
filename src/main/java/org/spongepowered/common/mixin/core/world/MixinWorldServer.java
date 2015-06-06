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
package org.spongepowered.common.mixin.core.world;

import net.minecraft.scoreboard.ScoreboardSaveData;
import net.minecraft.util.BlockPos;
import net.minecraft.village.VillageCollection;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldSettings;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.GeneratorType;
import org.spongepowered.api.world.GeneratorTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.interfaces.IMixinScoreboardSaveData;
import org.spongepowered.common.interfaces.IMixinWorld;

@NonnullByDefault
@Mixin(WorldServer.class)
public abstract class MixinWorldServer extends MixinWorld {

    @Inject(method = "createSpawnPosition(Lnet/minecraft/world/WorldSettings;)V", at = @At("HEAD"), cancellable = true)
    public void onCreateSpawnPosition(WorldSettings settings, CallbackInfo ci) {
        GeneratorType generatorType = (GeneratorType) settings.getTerrainType();
        if (generatorType != null && generatorType.equals(GeneratorTypes.END)) {
            this.worldInfo.setSpawn(new BlockPos(55, 60, 0));
            ci.cancel();
        }
    }

    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/scoreboard/ScoreboardSaveData;setScoreboard(Lnet/minecraft/scoreboard/Scoreboard;)V", shift = At.Shift.BEFORE), locals = LocalCapture.CAPTURE_FAILHARD)
    public void onInit(CallbackInfoReturnable<World> ci, String s, VillageCollection villagecollection, ScoreboardSaveData scoreboardsavedata) {
        ((IMixinScoreboardSaveData) scoreboardsavedata).setSpongeScoreboard(this.spongeScoreboard);
        this.spongeScoreboard.getScoreboards().add(this.worldScoreboard);
    }

    @Inject(method = "init", at = @At("RETURN"))
    public void onPostInit(CallbackInfoReturnable<World> ci) {
        // Run the world generator modifiers in the init method
        // (the "init" method, not the "<init>" constructor)
        IMixinWorld world = (IMixinWorld) ci.getReturnValue();
        world.updateWorldGenerator();
    }
}
