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

import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.bridge.world.GameRulesBridge;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.world.WorldManager;

import java.util.TreeMap;

@Mixin(GameRules.class)
public abstract class GameRulesMixin implements GameRulesBridge {

    @Shadow @Final private TreeMap<String, Object> rules;
    @Shadow public abstract void shadow$setOrCreateGameRule(String key, String ruleValue);
    
    private boolean impl$adjustAllWorlds = false;

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/GameRules;addGameRule(Ljava/lang/String;"
        + "Ljava/lang/String;Lnet/minecraft/world/GameRules$ValueType;)V", ordinal = 0))
    private void impl$toggleAddFlagOff(GameRules gameRules, String key, String value, GameRules.ValueType type) {
        this.impl$adjustAllWorlds = false;
        gameRules.addGameRule(key, value, type);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void impl$toggleAddFlagOn(CallbackInfo ci) {
        this.impl$adjustAllWorlds = true;
    }

    @Redirect(method = "setOrCreateGameRule", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/GameRules$Value;setValue(Ljava/lang/String;)V"))
    private void impl$adjustWorldsForAddSetGameRule(GameRules.Value source, String value, String key, String ruleValue) {
        if (!this.impl$adjustAllWorlds) {
            source.setValue(value);
            return;
        }

        WorldManager.getWorlds()
                .stream()
                .map(World::getGameRules)
                .forEach(gameRules -> {
                    GameRules.Value otherValue = ((GameRulesAccessor) gameRules).accessor$getRules().get(key);
                    if (otherValue != null) {
                        otherValue.setValue(value);
                    }
                });
    }

    @Inject(method = "addGameRule", at = @At("HEAD"), cancellable = true)
    private void impl$adjustWorldsForAddGameRule(String key, String value, GameRules.ValueType type, CallbackInfo ci) {
        ci.cancel();

        if (!this.impl$adjustAllWorlds) {
            this.rules.put(key, new GameRules.Value(value, type));
            return;
        }

        WorldManager.getWorlds()
            .stream()
            .map(World::getGameRules)
            .forEach(gameRules -> ((GameRulesAccessor) gameRules).accessor$getRules().put(key, new GameRules.Value(value, type)));
    }

    @Redirect(method = "readFromNBT",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/GameRules;setOrCreateGameRule(Ljava/lang/String;Ljava/lang/String;)V"))
    private void impl$setGameRuleFromNBT(GameRules gameRules, String key, String value) {
        this.bridge$setOrCreateGameRule(key, value);
    }

    @Override
    public void bridge$setOrCreateGameRule(String gameRule, String value) {
        this.impl$adjustAllWorlds = false;
        this.shadow$setOrCreateGameRule(gameRule, value);
        this.impl$adjustAllWorlds = true;
    }

    @Override
    public boolean bridge$removeGameRule(String gameRule) {
        // Cannot remove default gamerule
        if (Constants.Sponge.DEFAULT_GAME_RULES.hasRule(gameRule)) {
            this.shadow$setOrCreateGameRule(gameRule, Constants.Sponge.DEFAULT_GAME_RULES.getString(gameRule));
            return true;
        }

        return this.rules.remove(gameRule) != null;
    }
}
