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

import com.mojang.serialization.MapCodec;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Unit;
import net.minecraft.world.level.gamerules.GameRuleMap;
import org.spongepowered.api.world.gamerule.GameRule;
import org.spongepowered.api.world.server.ServerWorld;

import java.util.Map;

/**
 * Turns a {@link GameTestEnvironment} into the vanilla test environment its tests run in. A
 * {@link GameRulesEnvironment} becomes vanilla's own {@code minecraft:game_rules} environment; vanilla has no
 * equivalent for any other, so it is run as it is.
 */
public final class TestEnvironments {

    public static TestEnvironmentDefinition<?> definition(final Class<? extends GameTestEnvironment> type) {
        final GameTestEnvironment environment = SpongeGameTests.instantiate(type, "Gametest environment");
        if (environment instanceof final GameRulesEnvironment rules) {
            return TestEnvironments.gameRules(rules.rules());
        }
        return new Sponge(environment);
    }

    @SuppressWarnings("unchecked")
    private static TestEnvironmentDefinition<?> gameRules(final Map<GameRule<?>, Object> rules) {
        final GameRuleMap.Builder map = new GameRuleMap.Builder();
        // Sponge's game rules are vanilla's
        rules.forEach((rule, value) -> map.set((net.minecraft.world.level.gamerules.GameRule<Object>) (Object) rule, value));
        return new TestEnvironmentDefinition.SetGameRules(map.build());
    }

    /**
     * An environment run as it is. It is registered from code and never read from or written to a datapack, so it has
     * no codec of its own to be found by.
     */
    private record Sponge(GameTestEnvironment environment) implements TestEnvironmentDefinition<Unit> {

        @Override
        public Unit setup(final ServerLevel level) {
            this.environment.setup((ServerWorld) level);
            return Unit.INSTANCE;
        }

        @Override
        public void teardown(final ServerLevel level, final Unit saved) {
            this.environment.teardown((ServerWorld) level);
        }

        @Override
        public MapCodec<Sponge> codec() {
            return MapCodec.unit(this);
        }
    }

    private TestEnvironments() {
    }
}
