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
package org.spongepowered.common.mixin.api.mcp.world.storage;

import net.minecraft.world.GameType;
import net.minecraft.world.storage.IServerWorldInfo;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.entity.living.trader.WanderingTrader;
import org.spongepowered.api.util.MinecraftDayTime;
import org.spongepowered.api.util.Ticks;
import org.spongepowered.api.world.SerializationBehavior;
import org.spongepowered.api.world.WorldBorder;
import org.spongepowered.api.world.dimension.DimensionType;
import org.spongepowered.api.world.gen.MutableWorldGenerationSettings;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.api.world.server.ServerWorldProperties;
import org.spongepowered.api.world.weather.Weather;
import org.spongepowered.api.world.weather.Weathers;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.ResourceKeyBridge;
import org.spongepowered.common.bridge.world.storage.IServerWorldInfoBridge;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.SpongeTicks;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@SuppressWarnings("ConstantConditions")
@Mixin(IServerWorldInfo.class)
@Implements(@Interface(iface = ServerWorldProperties.class, prefix = "serverWorldProperties$"))
public interface IServerWorldInfoMixin_API extends ISpawnWorldInfoMixin_API, ServerWorldProperties {

    // @formatter:off
    @Shadow void shadow$setThundering(boolean p_76069_1_);
    @Shadow int shadow$getRainTime();
    @Shadow void shadow$setRainTime(int p_76080_1_);
    @Shadow void shadow$setThunderTime(int p_76090_1_);
    @Shadow int shadow$getThunderTime();
    @Shadow int shadow$getClearWeatherTime();
    @Shadow void shadow$setClearWeatherTime(int p_230391_1_);
    @Shadow int shadow$getWanderingTraderSpawnDelay();
    @Shadow void shadow$setWanderingTraderSpawnDelay(int p_230396_1_);
    @Shadow int shadow$getWanderingTraderSpawnChance();
    @Shadow void shadow$setWanderingTraderSpawnChance(int p_230397_1_);
    @Shadow void shadow$setWanderingTraderId(UUID p_230394_1_);
    @Shadow GameType shadow$getGameType();
    @Shadow net.minecraft.world.border.WorldBorder.Serializer shadow$getWorldBorder();
    @Shadow boolean shadow$isInitialized();
    @Shadow boolean shadow$getAllowCommands();
    @Shadow void shadow$setGameType(GameType p_230392_1_);
    @Shadow void shadow$setDayTime(long p_76068_1_);
    // @formatter:on

    @Override
    default ResourceKey getKey() {
        return ((ResourceKeyBridge) this).bridge$getKey();
    }

    @Override
    default Optional<ServerWorld> getWorld() {
        return Optional.ofNullable((ServerWorld) ((IServerWorldInfoBridge) this).bridge$getWorld());
    }

    @Override
    default void setDayTime(final MinecraftDayTime dayTime) {
        this.shadow$setDayTime(dayTime.asTicks().getTicks());
    }

    @Override
    default DimensionType getDimensionType() {
        return (DimensionType) ((IServerWorldInfoBridge) this).bridge$getDimensionType();
    }

    @Override
    default void setDimensionType(final DimensionType dimensionType) {
        ((IServerWorldInfoBridge) this).bridge$setDimensionType((net.minecraft.world.DimensionType) dimensionType, true);
    }

    @Override
    default GameMode getGameMode() {
        return (GameMode) (Object) this.shadow$getGameType();
    }

    @Override
    default void setGameMode(final GameMode gamemode) {
        this.shadow$setGameType((GameType) (Object) gamemode);
    }

    @Override
    default boolean areCommandsEnabled() {
        return this.shadow$getAllowCommands();
    }

    @Intrinsic
    default boolean serverWorldProperties$isInitialized() {
        return this.shadow$isInitialized();
    }

    @Override
    default boolean isPVPEnabled() {
        return ((IServerWorldInfoBridge) this).bridge$isPVPEnabled();
    }

    @Override
    default void setPVPEnabled(final boolean state) {
        ((IServerWorldInfoBridge) this).bridge$setPVPEnabled(state);
    }

    @Override
    default UUID getUniqueId() {
        return ((IServerWorldInfoBridge) this).bridge$getUniqueId();
    }

    @Override
    default boolean isEnabled() {
        return ((IServerWorldInfoBridge) this).bridge$isEnabled();
    }

    @Override
    default void setEnabled(final boolean state) {
        ((IServerWorldInfoBridge) this).bridge$setEnabled(state);
    }

    @Override
    default boolean doesLoadOnStartup() {
        return ((IServerWorldInfoBridge) this).bridge$doesLoadOnStartup();
    }

    @Override
    default void setLoadOnStartup(final boolean state) {
        ((IServerWorldInfoBridge) this).bridge$setLoadOnStartup(state);
    }

    @Override
    default boolean doesKeepSpawnLoaded() {
        return ((IServerWorldInfoBridge) this).bridge$doesKeepSpawnLoaded();
    }

    @Override
    default void setKeepSpawnLoaded(final boolean state) {
        ((IServerWorldInfoBridge) this).bridge$setKeepSpawnLoaded(state);
    }

    @Override
    default boolean doesGenerateSpawnOnLoad() {
        return ((IServerWorldInfoBridge) this).bridge$doesGenerateSpawnOnLoad();
    }

    @Override
    default void setGenerateSpawnOnLoad(final boolean state) {
        ((IServerWorldInfoBridge) this).bridge$setGenerateSpawnOnLoad(state);
    }

    @Override
    default MutableWorldGenerationSettings getWorldGenerationSettings() {
        throw new UnsupportedOperationException("Only vanilla implemented server world properties are supported!");
    }

    @Override
    default SerializationBehavior getSerializationBehavior() {
        return ((IServerWorldInfoBridge) this).bridge$getSerializationBehavior();
    }

    @Override
    default void setSerializationBehavior(final SerializationBehavior behavior) {
        ((IServerWorldInfoBridge) this).bridge$setSerializationBehavior(Objects.requireNonNull(behavior));
    }

    @Intrinsic
    default int serverWorldProperties$getWanderingTraderSpawnDelay() {
        return this.shadow$getWanderingTraderSpawnDelay();
    }

    @Intrinsic
    default void serverWorldProperties$setWanderingTraderSpawnDelay(final int delay) {
        this.shadow$setWanderingTraderSpawnDelay(delay);
    }

    @Intrinsic
    default int serverWorldProperties$getWanderingTraderSpawnChance() {
        return this.shadow$getWanderingTraderSpawnChance();
    }

    @Intrinsic
    default void serverWorldProperties$setWanderingTraderSpawnChance(final int chance) {
        this.shadow$setWanderingTraderSpawnChance(chance);
    }

    @Override
    default void setWanderingTrader(@Nullable final WanderingTrader trader) {
        this.shadow$setWanderingTraderId(trader == null ? null : trader.getUniqueId());
    }

    @Override
    default Weather getWeather() {
        if (this.shadow$isRaining()) {
            return Weathers.RAIN.get();
        } else if (this.shadow$isThundering()) {
            return Weathers.THUNDER.get();
        }
        return Weathers.CLEAR.get();
    }

    @Override
    default Ticks getRemainingWeatherDuration() {
        if (this.shadow$isRaining()) {
            return new SpongeTicks(this.shadow$getRainTime());
        } else if (this.shadow$isThundering()) {
            return new SpongeTicks(this.shadow$getThunderTime());
        }
        return new SpongeTicks(this.shadow$getClearWeatherTime());
    }

    @Override
    default Ticks getRunningWeatherDuration() {
        if (this.shadow$isRaining()) {
            return new SpongeTicks(6000 - this.shadow$getRainTime());
        } else if (this.shadow$isThundering()) {
            return new SpongeTicks(6000 - this.shadow$getThunderTime());
        } else {
            return new SpongeTicks(6000 - this.shadow$getClearWeatherTime());
        }
    }

    @Override
    default void setWeather(final Weather weather) {
        this.setWeather(weather, new SpongeTicks(6000 / Constants.TickConversions.TICK_DURATION_MS));
    }

    @Override
    default void setWeather(final Weather weather, final Ticks ticks) {
        if (weather == Weathers.CLEAR.get()) {
            this.shadow$setClearWeatherTime((int) ticks.getTicks());
            this.shadow$setRaining(false);
            this.shadow$setRainTime(0);
            this.shadow$setThundering(false);
            this.shadow$setThunderTime(0);
        } else if (weather == Weathers.RAIN.get()) {
            this.shadow$setRaining(true);
            this.shadow$setRainTime((int) ticks.getTicks());
            this.shadow$setThundering(false);
            this.shadow$setThunderTime(0);
            this.shadow$setClearWeatherTime(0);
        } else if (weather == Weathers.THUNDER.get()) {
            this.shadow$setRaining(true);
            this.shadow$setRainTime((int) ticks.getTicks());
            this.shadow$setThundering(true);
            this.shadow$setThunderTime((int) ticks.getTicks());
            this.shadow$setClearWeatherTime(0);
        }
    }

    @Override
    default WorldBorder getWorldBorder() {
        final net.minecraft.world.border.WorldBorder.Serializer settings = this.shadow$getWorldBorder();

        final net.minecraft.world.border.WorldBorder mcBorder = new net.minecraft.world.border.WorldBorder();
        mcBorder.applySettings(settings);
        return (WorldBorder) mcBorder;
    }

}
