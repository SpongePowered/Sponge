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
package org.spongepowered.common.mixin.api.minecraft.world.level.storage;

import net.minecraft.world.level.storage.ServerLevelData;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.living.trader.WanderingTrader;
import org.spongepowered.api.util.MinecraftDayTime;
import org.spongepowered.api.util.Ticks;
import org.spongepowered.api.world.server.storage.ServerWorldProperties;
import org.spongepowered.api.world.weather.Weather;
import org.spongepowered.api.world.weather.WeatherType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.SpongeTicks;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@SuppressWarnings("ConstantConditions")
@Mixin(ServerLevelData.class)
public interface ServerLevelDataMixin_API extends ServerWorldProperties {

    // @formatter:off
    @Shadow int shadow$getWanderingTraderSpawnDelay();
    @Shadow void shadow$setWanderingTraderSpawnDelay(int p_230396_1_);
    @Shadow int shadow$getWanderingTraderSpawnChance();
    @Shadow void shadow$setWanderingTraderSpawnChance(int p_230397_1_);
    @Shadow void shadow$setWanderingTraderId(UUID p_230394_1_);
    @Shadow @Nullable UUID shadow$getWanderingTraderId();
    @Shadow void shadow$setDayTime(long p_76068_1_);
    // @formatter:on

    @Override
    default void setDayTime(final MinecraftDayTime dayTime) {
        this.shadow$setDayTime(dayTime.asTicks().ticks());
    }

    @Override
    default Ticks wanderingTraderSpawnDelay() {
        return SpongeTicks.ticksOrInfinite(this.shadow$getWanderingTraderSpawnDelay());
    }

    @Override
    default void setWanderingTraderSpawnDelay(final Ticks delay) {
        this.shadow$setWanderingTraderSpawnDelay(SpongeTicks.toSaturatedIntOrInfinite(delay));
    }

    @Override
    default int wanderingTraderSpawnChance() {
        return this.shadow$getWanderingTraderSpawnChance();
    }

    @Override
    default void setWanderingTraderSpawnChance(final int chance) {
        this.shadow$setWanderingTraderSpawnChance(chance);
    }

    @Override
    default void setWanderingTrader(@Nullable final WanderingTrader trader) {
        this.shadow$setWanderingTraderId(trader == null ? null : trader.uniqueId());
    }

    @Override
    default Optional<UUID> wanderTraderUniqueId() {
        return Optional.ofNullable(this.shadow$getWanderingTraderId());
    }

    @Override
    default void setWeather(final WeatherType type) {
        this.offer(Keys.WEATHER, Weather.of(Objects.requireNonNull(type, "type"), 6000 / Constants.TickConversions.TICK_DURATION_MS));
    }

    @Override
    default void setWeather(final WeatherType type, final Ticks ticks) {
        this.offer(Keys.WEATHER, Weather.of(Objects.requireNonNull(type, "type"), Objects.requireNonNull(ticks, "ticks")));
    }

}
