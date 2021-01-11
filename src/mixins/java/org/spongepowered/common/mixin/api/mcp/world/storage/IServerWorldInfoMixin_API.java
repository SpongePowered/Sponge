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

import net.kyori.adventure.text.Component;
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
import org.spongepowered.api.world.WorldType;
import org.spongepowered.api.world.generation.config.WorldGenerationConfig;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.api.world.server.storage.ServerWorldProperties;
import org.spongepowered.api.world.weather.Weather;
import org.spongepowered.api.world.weather.WeatherType;
import org.spongepowered.api.world.weather.WeatherTypes;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.SpongeTicks;
import org.spongepowered.common.world.weather.SpongeWeather;
import org.spongepowered.common.world.weather.SpongeWeatherType;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@SuppressWarnings("ConstantConditions")
@Mixin(IServerWorldInfo.class)
@Implements(@Interface(iface = ServerWorldProperties.class, prefix = "serverWorldProperties$"))
public interface IServerWorldInfoMixin_API extends ServerWorldProperties {

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
        throw new UnsupportedOperationException("Only vanilla implemented server world properties are supported!");
    }

    @Override
    default Optional<ServerWorld> world() {
        throw new UnsupportedOperationException("Only vanilla implemented server world properties are supported!");
    }

    @Override
    default Optional<Component> displayName() {
        throw new UnsupportedOperationException("Only vanilla implemented server world properties are supported!");
    }

    @Override
    default void setDisplayName(final @Nullable Component name) {
        throw new UnsupportedOperationException("Only vanilla implemented server world properties are supported!");
    }

    @Override
    default void setDayTime(final MinecraftDayTime dayTime) {
        this.shadow$setDayTime(dayTime.asTicks().getTicks());
    }

    @Override
    default void setWorldType(final WorldType worldType) {
        throw new UnsupportedOperationException("Only vanilla implemented server world properties are supported!");
    }

    @Override
    default GameMode gameMode() {
        return (GameMode) (Object) this.shadow$getGameType();
    }

    @Override
    default void setGameMode(final GameMode gamemode) {
        this.shadow$setGameType((GameType) (Object) gamemode);
    }

    @Override
    default boolean commands() {
        return this.shadow$getAllowCommands();
    }

    @Intrinsic
    default boolean serverWorldProperties$initialized() {
        return this.shadow$isInitialized();
    }

    @Override
    default boolean pvp() {
        throw new UnsupportedOperationException("Only vanilla implemented server world properties are supported!");
    }

    @Override
    default void setPvp(final boolean pvp) {
        throw new UnsupportedOperationException("Only vanilla implemented server world properties are supported!");
    }

    @Override
    default UUID getUniqueId() {
        throw new UnsupportedOperationException("Only vanilla implemented server world properties are supported!");
    }

    @Override
    default boolean enabled() {
        throw new UnsupportedOperationException("Only vanilla implemented server world properties are supported!");
    }

    @Override
    default void setEnabled(final boolean enabled) {
        throw new UnsupportedOperationException("Only vanilla implemented server world properties are supported!");
    }

    @Override
    default boolean loadOnStartup() {
        throw new UnsupportedOperationException("Only vanilla implemented server world properties are supported!");
    }

    @Override
    default void setLoadOnStartup(final boolean loadOnStartup) {
        throw new UnsupportedOperationException("Only vanilla implemented server world properties are supported!");
    }

    @Override
    default boolean performsSpawnLogic() {
        throw new UnsupportedOperationException("Only vanilla implemented server world properties are supported!");
    }

    @Override
    default void setPerformsSpawnLogic(final boolean performsSpawnLogic) {
        throw new UnsupportedOperationException("Only vanilla implemented server world properties are supported!");
    }

    @Override
    default WorldGenerationConfig.Mutable worldGenerationConfig() {
        throw new UnsupportedOperationException("Only vanilla implemented server world properties are supported!");
    }

    @Override
    default SerializationBehavior serializationBehavior() {
        throw new UnsupportedOperationException("Only vanilla implemented server world properties are supported!");
    }

    @Override
    default void setSerializationBehavior(final SerializationBehavior behavior) {
        throw new UnsupportedOperationException("Only vanilla implemented server world properties are supported!");
    }

    @Override
    default int wanderingTraderSpawnDelay() {
        return this.shadow$getWanderingTraderSpawnDelay();
    }

    @Override
    default void setWanderingTraderSpawnDelay(final int delay) {
        this.shadow$setWanderingTraderSpawnDelay(delay);
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
        this.shadow$setWanderingTraderId(trader == null ? null : trader.getUniqueId());
    }

    @Override
    default Weather weather() {
        if (((IServerWorldInfo) this).isRaining()) {
            return new SpongeWeather((SpongeWeatherType) WeatherTypes.RAIN.get(), new SpongeTicks(this.shadow$getRainTime()), new SpongeTicks(6000 - this.shadow$getRainTime()));
        } else if (((IServerWorldInfo) this).isThundering()) {
            return new SpongeWeather((SpongeWeatherType) WeatherTypes.THUNDER.get(), new SpongeTicks(this.shadow$getThunderTime()), new SpongeTicks(6000 - this.shadow$getThunderTime()));
        }
        return new SpongeWeather((SpongeWeatherType) WeatherTypes.CLEAR.get(), new SpongeTicks(this.shadow$getClearWeatherTime()), new SpongeTicks(6000 - this.shadow$getClearWeatherTime()));
    }

    @Override
    default void setWeather(final WeatherType type) {
        this.setWeather(Objects.requireNonNull(type, "type"), new SpongeTicks(6000 / Constants.TickConversions.TICK_DURATION_MS));
    }

    @Override
    default void setWeather(final WeatherType type, final Ticks ticks) {
        Objects.requireNonNull(type, "type");
        final long time = Objects.requireNonNull(ticks, "ticks").getTicks();

        if (type == WeatherTypes.CLEAR.get()) {
            this.shadow$setClearWeatherTime((int) time);
            ((IServerWorldInfo) this).setRaining(false);
            this.shadow$setRainTime(0);
            this.shadow$setThundering(false);
            this.shadow$setThunderTime(0);
        } else if (type == WeatherTypes.RAIN.get()) {
            ((IServerWorldInfo) this).setRaining(true);
            this.shadow$setRainTime((int) time);
            this.shadow$setThundering(false);
            this.shadow$setThunderTime(0);
            this.shadow$setClearWeatherTime(0);
        } else if (type == WeatherTypes.THUNDER.get()) {
            ((IServerWorldInfo) this).setRaining(true);
            this.shadow$setRainTime((int) time);
            this.shadow$setThundering(true);
            this.shadow$setThunderTime((int) time);
            this.shadow$setClearWeatherTime(0);
        }
    }

    @Override
    default WorldBorder worldBorder() {
        final net.minecraft.world.border.WorldBorder.Serializer settings = this.shadow$getWorldBorder();

        final net.minecraft.world.border.WorldBorder mcBorder = new net.minecraft.world.border.WorldBorder();
        mcBorder.applySettings(settings);
        return (WorldBorder) mcBorder;
    }

}
