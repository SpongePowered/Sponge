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
package org.spongepowered.common.mixin.api.mcp.world.level;

import net.minecraft.world.level.GameRules;
import org.spongepowered.api.world.gamerule.GameRule;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.accessor.world.level.GameRulesAccessor;

import java.lang.reflect.Type;

@Mixin(GameRules.Key.class)
public class GameRules_KeyMixin_API<T> implements GameRule<T> {

    @Shadow @Final private String id;

    @Override
    public String name() {
        return this.id;
    }

    @Override
    public Type valueType() {
        final GameRules.Type<?> type = GameRulesAccessor.accessor$GAME_RULE_TYPES().get(this);
        final GameRules.Value<?> value = type.createRule();
        if (value instanceof GameRules.BooleanValue) {
            return Boolean.class;
        }
        if (value instanceof GameRules.IntegerValue) {
            return Integer.class;
        }
        throw new IllegalStateException("Unexpected GameRule.Value implementation " + value.getClass().getName());
    }

    @Override
    @SuppressWarnings("unchecked")
    public T defaultValue() {
        final GameRules.Type<?> type = GameRulesAccessor.accessor$GAME_RULE_TYPES().get(this);
        final GameRules.Value<?> value = type.createRule();
        if (value instanceof GameRules.BooleanValue) {
            return (T) (Object)((GameRules.BooleanValue) value).get();
        }
        if (value instanceof GameRules.IntegerValue) {
            return (T)(Object)((GameRules.IntegerValue) value).get();
        }
        throw new IllegalStateException("Unexpected GameRule.Value implementation " + value.getClass().getName());
    }
}
