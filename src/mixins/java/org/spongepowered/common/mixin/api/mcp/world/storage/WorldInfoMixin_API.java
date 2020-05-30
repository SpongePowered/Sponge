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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.gson.JsonParseException;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameRules;
import net.minecraft.world.GameType;
import net.minecraft.world.WorldType;
import net.minecraft.world.storage.WorldInfo;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.boss.BossBar;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.entity.living.trader.WanderingTrader;
import org.spongepowered.api.world.SerializationBehavior;
import org.spongepowered.api.world.WorldBorder;
import org.spongepowered.api.world.dimension.DimensionType;
import org.spongepowered.api.world.gamerule.GameRule;
import org.spongepowered.api.world.gen.GeneratorType;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.api.world.teleport.PortalAgentType;
import org.spongepowered.api.world.weather.Weather;
import org.spongepowered.api.world.weather.Weathers;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.accessor.world.GameRulesAccessor;
import org.spongepowered.common.accessor.world.GameRules_RuleValueAccessor;
import org.spongepowered.common.bridge.server.management.PlayerChunkMapBridge;
import org.spongepowered.common.bridge.world.dimension.DimensionTypeBridge;
import org.spongepowered.common.bridge.world.storage.WorldInfoBridge;
import org.spongepowered.common.config.SpongeConfig;
import org.spongepowered.common.config.type.WorldConfig;
import org.spongepowered.common.data.persistence.NbtTranslator;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.math.vector.Vector3i;

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@SuppressWarnings("ConstantConditions")
@Mixin(WorldInfo.class)
@Implements(@Interface(iface = WorldProperties.class, prefix = "worldproperties$"))
public abstract class WorldInfoMixin_API implements WorldProperties {

    @Shadow private long randomSeed;
    @Shadow @Nullable private String legacyCustomOptions;
    @Shadow private UUID wanderingTraderId;
    @Shadow public abstract int shadow$getSpawnX();
    @Shadow public abstract int shadow$getSpawnY();
    @Shadow public abstract int shadow$getSpawnZ();
    @Shadow public abstract void shadow$setSpawn(BlockPos pos);
    @Shadow public abstract WorldType shadow$getGenerator();
    @Shadow public abstract void shadow$setGenerator(WorldType worldType);
    @Shadow public abstract long shadow$getSeed();
    @Shadow public abstract long shadow$getGameTime();
    @Shadow public abstract long shadow$getDayTime();
    @Shadow public abstract void shadow$setDayTime(long time);
    @Shadow public abstract GameType shadow$getGameType();
    @Shadow public abstract void shadow$setGameType(GameType gameType);
    @Shadow public abstract boolean shadow$isMapFeaturesEnabled();
    @Shadow public abstract void shadow$setMapFeaturesEnabled(boolean state);
    @Shadow public abstract boolean shadow$isHardcore();
    @Shadow public abstract void shadow$setHardcore(boolean state);
    @Shadow public abstract boolean shadow$areCommandsAllowed();
    @Shadow public abstract void shadow$setAllowCommands(boolean state);
    @Shadow public abstract boolean shadow$isInitialized();
    @Shadow public abstract Difficulty shadow$getDifficulty();
    @Shadow public abstract void shadow$setDifficulty(Difficulty difficulty);
    @Shadow public abstract CompoundNBT shadow$getGeneratorOptions();
    @Shadow public abstract void shadow$setGeneratorOptions(CompoundNBT compound);
    @Shadow public abstract GameRules shadow$getGameRulesInstance();
    @Shadow public abstract int shadow$getWanderingTraderSpawnChance();
    @Shadow public abstract void shadow$setWanderingTraderSpawnChance(int chance);
    @Shadow public abstract int shadow$getWanderingTraderSpawnDelay();
    @Shadow public abstract void shadow$setWanderingTraderSpawnDelay(int delay);
    @Shadow public abstract void shadow$setWanderingTraderId(UUID uniqueId);
    @Shadow public abstract String shadow$getWorldName();
    @Shadow public abstract boolean shadow$isRaining();
    @Shadow public abstract void shadow$setRaining(boolean state);
    @Shadow public abstract int shadow$getRainTime();
    @Shadow public abstract void shadow$setRainTime(int time);
    @Shadow public abstract boolean shadow$isThundering();
    @Shadow public abstract void shadow$setThundering(boolean state);
    @Shadow public abstract int shadow$getThunderTime();
    @Shadow public abstract void shadow$setThunderTime(int time);
    @Shadow public abstract void shadow$setClearWeatherTime(int time);
    @Shadow public abstract int shadow$getClearWeatherTime();

    @Override
    public String getDirectoryName() {
        return this.shadow$getWorldName();
    }

    @Override
    public Vector3i getSpawnPosition() {
        return new Vector3i(this.shadow$getSpawnX(), this.shadow$getSpawnY(), this.shadow$getSpawnZ());
    }

    @Override
    public void setSpawnPosition(Vector3i position) {
        checkNotNull(position);
        this.shadow$setSpawn(VecHelper.toBlockPos(position));
    }

    @Override
    public GeneratorType getGeneratorType() {
        return (GeneratorType) this.shadow$getGenerator();
    }

    @Override
    public void setGeneratorType(final GeneratorType type) {
        checkNotNull(type);
        this.shadow$setGenerator((WorldType) type);
    }

    @Intrinsic
    public long worldproperties$getSeed() {
        return this.shadow$getSeed();
    }

    @Override
    public void setSeed(long seed) {
        this.randomSeed = seed;
    }

    @Override
    public Duration getGameTime() {
        return Duration.ofMillis(this.shadow$getGameTime());
    }

    @Override
    public Duration getDayTime() {
        return Duration.ofMillis(this.shadow$getDayTime());
    }

    @Override
    public void setDayTime(Duration time) {
        this.shadow$setDayTime(time.toMillis());
    }

    @Override
    public DimensionType getDimensionType() {
        return ((DimensionTypeBridge) ((WorldInfoBridge) this).bridge$getDimensionType()).bridge$getSpongeDimensionType();
    }

    @Override
    public PortalAgentType getPortalAgentType() {
        return ((WorldInfoBridge) this).bridge$getPortalAgent();
    }

    @Override
    public GameMode getGameMode() {
        return (GameMode) (Object) this.shadow$getGameType();
    }

    @Override
    public void setGameMode(GameMode gamemode) {
        this.shadow$setGameType((GameType) (Object) gamemode);
    }

    @Override
    public boolean areStructuresEnabled() {
        return this.shadow$isMapFeaturesEnabled();
    }

    @Override
    public void setStructuresEnabled(boolean state) {
        this.shadow$setMapFeaturesEnabled(state);
    }

    @Intrinsic
    public boolean worldproperties$isHardcore() {
        return this.shadow$isHardcore();
    }

    @Intrinsic
    public void worldproperties$setHardcore(boolean state) {
        this.shadow$setHardcore(state);
    }

    @Override
    public boolean areCommandsEnabled() {
        return this.shadow$areCommandsAllowed();
    }

    @Override
    public void setCommandsEnabled(boolean state) {
        this.shadow$setAllowCommands(state);
    }

    @Intrinsic
    public boolean worldproperties$isInitialized() {
        return this.shadow$isInitialized();
    }

    @Override
    public org.spongepowered.api.world.difficulty.Difficulty getDifficulty() {
        return (org.spongepowered.api.world.difficulty.Difficulty) (Object) this.shadow$getDifficulty();
    }

    @Override
    public void setDifficulty(org.spongepowered.api.world.difficulty.Difficulty difficulty) {
        this.shadow$setDifficulty((Difficulty) (Object) difficulty);
    }

    @Override
    public boolean isPVPEnabled() {
        return ((WorldInfoBridge) this).bridge$isPVPEnabled();
    }

    @Override
    public void setPVPEnabled(boolean state) {
        ((WorldInfoBridge) this).bridge$setPVPEnabled(state);
    }

    @Override
    public boolean doesGenerateBonusChest() {
        return ((WorldInfoBridge) this).bridge$doesGenerateBonusChest();
    }

    @Override
    public void setGenerateBonusChest(boolean state) {
        ((WorldInfoBridge) this).bridge$setGenerateBonusChest(state);
    }

    @Override
    public UUID getUniqueId() {
        return ((WorldInfoBridge) this).bridge$getUniqueId();
    }

    @Override
    public boolean isEnabled() {
        return ((WorldInfoBridge) this).bridge$isEnabled();
    }

    @Override
    public void setEnabled(boolean state) {
        ((WorldInfoBridge) this).bridge$setEnabled(state);
    }

    @Override
    public boolean doesLoadOnStartup() {
        return ((WorldInfoBridge) this).bridge$doesLoadOnStartup();
    }

    @Override
    public void setLoadOnStartup(boolean state) {
        ((WorldInfoBridge) this).bridge$setLoadOnStartup(state);
    }

    @Override
    public boolean doesKeepSpawnLoaded() {
        return ((WorldInfoBridge) this).bridge$doesKeepSpawnLoaded();
    }

    @Override
    public void setKeepSpawnLoaded(boolean state) {
        ((WorldInfoBridge) this).bridge$setKeepSpawnLoaded(state);
    }

    @Override
    public boolean doesGenerateSpawnOnLoad() {
        return ((WorldInfoBridge) this).bridge$doesGenerateSpawnOnLoad();
    }

    @Override
    public void setGenerateSpawnOnLoad(boolean state) {
        ((WorldInfoBridge) this).bridge$setGenerateSpawnOnLoad(state);
    }

    @Override
    public DataContainer getGeneratorSettings() {
        // TODO 1.14 - This may not be correct...
        if (this.legacyCustomOptions != null) {
            try {
                return DataContainer.createNew().set(Constants.Sponge.World.WORLD_CUSTOM_SETTINGS, DataFormats.JSON.get().read(this.legacyCustomOptions));
            } catch (JsonParseException | IOException ignored) {
                return DataContainer.createNew();
            }
        } else {
            return DataContainer.createNew().set(Constants.Sponge.World.WORLD_CUSTOM_SETTINGS,
                NbtTranslator.getInstance().translateFrom(this.shadow$getGeneratorOptions()));
        }
    }

    @Override
    public void setGeneratorSettings(DataContainer generatorSettings) {
        this.shadow$setGeneratorOptions(NbtTranslator.getInstance().translate(generatorSettings));
    }

    @Override
    public WorldBorder getWorldBorder() {
        // TODO 1.14 - Fetch the WorldBorder if a live world instance, return a dummy if it isn't?
        return null;
    }

    @Override
    public SerializationBehavior getSerializationBehavior() {
        return ((WorldInfoBridge) this).bridge$getSerializationBehavior();
    }

    @Override
    public void setSerializationBehavior(SerializationBehavior behavior) {
        ((WorldInfoBridge) this).bridge$setSerializationBehavior(checkNotNull(behavior));
    }

    @Intrinsic
    public int worldproperties$getWanderingTraderSpawnDelay() {
        return this.shadow$getWanderingTraderSpawnDelay();
    }

    @Intrinsic
    public void worldproperties$setWanderingTraderSpawnDelay(int delay) {
        this.shadow$setWanderingTraderSpawnDelay(delay);
    }

    @Intrinsic
    public int worldproperties$getWanderingTraderSpawnChance() {
        return this.shadow$getWanderingTraderSpawnChance();
    }

    @Intrinsic
    public void worldproperties$setWanderingTraderSpawnChance(int chance) {
        this.shadow$setWanderingTraderSpawnChance(chance);
    }

    @Override
    public Optional<UUID> getWanderTraderUniqueId() {
        return Optional.ofNullable(this.wanderingTraderId);
    }

    @Override
    public void setWanderingTrader(@Nullable WanderingTrader trader) {
        this.shadow$setWanderingTraderId(trader == null ? null : trader.getUniqueId());
    }

    @Override
    public List<BossBar> getCustomBossBars() {
        // TODO 1.14 - Fetch the boss bars if a live world instance, return dummies if they aren't?
        return null;
    }

    @Override
    public void setCustomBossBars(@Nullable List<BossBar> bars) {

    }

    @Override
    public Weather getWeather() {
        if (this.shadow$isRaining()) {
            return Weathers.RAIN.get();
        } else if (this.shadow$isThundering()) {
            return Weathers.THUNDER_STORM.get();
        }
        return Weathers.CLEAR.get();
    }

    @Override
    public Duration getRemainingWeatherDuration() {
        if (this.shadow$isRaining()) {
            return Duration.ofSeconds(this.shadow$getRainTime());
        } else if (this.shadow$isThundering()) {
            return Duration.ofSeconds(this.shadow$getThunderTime());
        }
        return Duration.ofSeconds(this.shadow$getClearWeatherTime());
    }

    @Override
    public Duration getRunningWeatherDuration() {
        // TODO 1.14 - Weather has no finite maximum and we don't know how much it started with so....I guess I'll hardcode it? How do I implement this??
        if (this.shadow$isRaining()) {
            return Duration.ofSeconds(6000 - this.shadow$getRainTime());
        } else if (this.shadow$isThundering()) {
            return Duration.ofSeconds(6000 - this.shadow$getThunderTime());
        } else {
            return Duration.ofSeconds(6000 - this.shadow$getClearWeatherTime());
        }
    }

    @Override
    public void setWeather(Weather weather) {
        this.setWeather(weather, Duration.ofSeconds(6000));
    }

    @Override
    public void setWeather(Weather weather, Duration duration) {
        if (weather == Weathers.CLEAR.get()) {
            this.shadow$setClearWeatherTime((int) (duration.toMillis() / 1000));
            this.shadow$setRaining(false);
            this.shadow$setRainTime(0);
            this.shadow$setThundering(false);
            this.shadow$setThunderTime(0);
        } else if (weather == Weathers.RAIN.get()) {
            this.shadow$setRaining(true);
            this.shadow$setRainTime((int) (duration.toMillis() / 1000));
            this.shadow$setThundering(false);
            this.shadow$setThunderTime(0);
            this.shadow$setClearWeatherTime(0);
        } else if (weather == Weathers.THUNDER_STORM.get()) {
            this.shadow$setRaining(true);
            this.shadow$setRainTime((int) (duration.toMillis() / 1000));
            this.shadow$setThundering(true);
            this.shadow$setThunderTime((int) (duration.toMillis() / 1000));
            this.shadow$setClearWeatherTime(0);
        }
    }

    @Override
    public <V> V getGameRule(GameRule<V> gameRule) {
        // TODO 1.14 - Boy, this is baaaad....
        final GameRules.RuleValue<?> value = this.shadow$getGameRulesInstance().get((GameRules.RuleKey<?>) (Object) gameRule);
        if (value instanceof GameRules.BooleanValue) {
            return (V) Boolean.valueOf(((GameRules.BooleanValue) value).get());
        } else if (value instanceof GameRules.IntegerValue) {
            return (V) Integer.valueOf(((GameRules.IntegerValue) value).get());
        }
        return null;
    }

    @Override
    public <V> void setGameRule(GameRule<V> gameRule, V value) {
        // TODO 1.14 - Boy, this is baaaad....
        final GameRules.RuleValue<?> mValue = this.shadow$getGameRulesInstance().get((GameRules.RuleKey<?>) (Object) gameRule);
        ((GameRules_RuleValueAccessor) mValue).accessor$func_223553_a(value.toString());
    }

    @Override
    public Map<GameRule<?>, ?> getGameRules() {
        // TODO 1.14 - Boy, this is baaaad....
        final Map<GameRules.RuleKey<?>, GameRules.RuleValue<?>> rules =
            ((GameRulesAccessor) this.shadow$getGameRulesInstance()).accessor$getRules();

        final Map<GameRule<?>, Object> apiRules = new HashMap<>();
        for (Map.Entry<GameRules.RuleKey<?>, GameRules.RuleValue<?>> rule : rules.entrySet()) {
            final GameRule<?> key = (GameRule<?>) (Object) rule.getKey();
            final GameRules.RuleValue<?> mValue = rule.getValue();
            Object value = null;
            if (mValue instanceof GameRules.BooleanValue) {
                value = ((GameRules.BooleanValue) mValue).get();
            } else if (mValue instanceof GameRules.IntegerValue) {
                value = ((GameRules.IntegerValue) mValue).get();
            }

            if (value != null) {
                apiRules.put(key, value);
            }
        }

        return apiRules;
    }

    @Override
    public int getViewDistance() {
        // TODO ChunkManager#viewDistance
        // originates from ServerPlayNetHandler#getViewDistance()
    }

    @Override
    public void setViewDistance(int viewDistance) {
        final SpongeConfig<WorldConfig> configAdapter = ((WorldInfoBridge) this.getWorldInfo()).bridge$getConfigAdapter();
        // don't use the parameter, use the field that has been clamped
        configAdapter.getConfig().getWorld().setViewDistance(((PlayerChunkMapBridge) this.playerChunkMap).accessor$getViewDistance());
        configAdapter.save();
        // TODO ChunkManager#setViewDistance();
        throw new UnsupportedOperationException();
    }

    // TODO resetViewDistance? this.setViewDistance(this.server.getPlayerList().getViewDistance());

}
