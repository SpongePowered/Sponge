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
package org.spongepowered.common.test.gametest;

import org.spongepowered.api.world.gamerule.GameRule;
import org.spongepowered.api.world.gamerule.GameRuleHolder;
import org.spongepowered.api.world.server.ServerWorld;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Game rules set for the tests, declared in the constructor with {@link #rule}:
 *
 * <pre>{@code
 * public final class NoMobGriefing extends GameRulesEnvironment {
 *     public NoMobGriefing() {
 *         this.rule(GameRules.MOB_GRIEFING, false);
 *     }
 * }
 * }</pre>
 *
 * <p>Teardown resets each rule to its default, as vanilla's game rules environment does, so the rules are the same
 * after the tests whichever way the environment was applied.</p>
 */
public abstract class GameRulesEnvironment implements GameTestEnvironment {

    // Environments are made before the game rules are registered, so a rule is only looked up once it's needed
    private final Map<Supplier<? extends GameRule<?>>, Object> rules = new LinkedHashMap<>();

    /** Sets the rule to the value for the tests; a registry reference such as {@code GameRules.MOB_GRIEFING} is a supplier */
    protected final <V> void rule(final Supplier<? extends GameRule<V>> rule, final V value) {
        this.rules.put(rule, value);
    }

    /** The rules and the values the tests run with, for a platform that applies them through vanilla */
    public final Map<GameRule<?>, Object> rules() {
        final Map<GameRule<?>, Object> rules = new LinkedHashMap<>();
        this.rules.forEach((rule, value) -> rules.put(rule.get(), value));
        return Collections.unmodifiableMap(rules);
    }

    @Override
    public final void setup(final ServerWorld world) {
        this.rules().forEach((rule, value) -> GameRulesEnvironment.set(world.properties(), rule, value));
    }

    @Override
    public final void teardown(final ServerWorld world) {
        this.rules().keySet().forEach(rule -> GameRulesEnvironment.set(world.properties(), rule, rule.defaultValue()));
    }

    @SuppressWarnings("unchecked")
    private static <V> void set(final GameRuleHolder holder, final GameRule<V> rule, final Object value) {
        holder.setGameRule(rule, (V) value);
    }
}
