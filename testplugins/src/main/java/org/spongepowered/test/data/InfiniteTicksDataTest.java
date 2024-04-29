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
package org.spongepowered.test.data;

import com.google.inject.Inject;
import io.leangen.geantyref.TypeToken;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.command.parameter.managed.ValueParser;
import org.spongepowered.api.command.parameter.managed.standard.VariableValueParameters;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.effect.potion.PotionEffectType;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.ai.goal.Goal;
import org.spongepowered.api.entity.ai.goal.GoalExecutorTypes;
import org.spongepowered.api.entity.ai.goal.GoalTypes;
import org.spongepowered.api.entity.ai.goal.builtin.creature.RangedAttackAgainstAgentGoal;
import org.spongepowered.api.entity.living.Agent;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.living.player.CooldownEvent;
import org.spongepowered.api.event.item.inventory.UseItemStackEvent;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.event.lifecycle.RegisterDataPackValueEvent;
import org.spongepowered.api.event.world.ChangeWeatherEvent;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.recipe.RecipeRegistration;
import org.spongepowered.api.item.recipe.RecipeType;
import org.spongepowered.api.item.recipe.RecipeTypes;
import org.spongepowered.api.item.recipe.cooking.CookingRecipe;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.util.Ticks;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.weather.Weather;
import org.spongepowered.api.world.weather.WeatherType;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;
import org.spongepowered.test.LoadableModule;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings({"unchecked", "rawtypes"})
@Plugin("infiniteticksdatatest")
public final class InfiniteTicksDataTest implements LoadableModule {

    private static final List<Key<@NonNull Value<Ticks>>> KEYS = List.of(
            Keys.ATTACK_TIME,
            Keys.AUTO_SPIN_ATTACK_TICKS,
            Keys.BABY_TICKS,
            Keys.BREEDING_COOLDOWN,
            Keys.COOLDOWN,
            Keys.DESPAWN_DELAY,
            Keys.DURATION,
            Keys.DURATION_ON_USE,
            Keys.EATING_TIME,
            Keys.EGG_TIME,
            Keys.END_GATEWAY_AGE,
            Keys.FALL_TIME,
            Keys.FIREWORK_FLIGHT_MODIFIER,
            Keys.FIRE_DAMAGE_DELAY,
            Keys.FIRE_TICKS,
            Keys.FROZEN_TIME,
            Keys.FUSE_DURATION,
            Keys.INTERPOLATION_DELAY,
            Keys.INTERPOLATION_DURATION,
            Keys.INVULNERABILITY_TICKS,
            Keys.LIFE_TICKS,
            Keys.MAX_BURN_TIME,
            Keys.MAX_COOK_TIME,
            Keys.MAX_FROZEN_TIME,
            Keys.MAX_SPAWN_DELAY,
            Keys.MIN_SPAWN_DELAY,
            Keys.PASSED_COOK_TIME,
            Keys.PICKUP_DELAY,
            Keys.REAPPLICATION_DELAY,
            Keys.REMAINING_BREW_TIME,
            Keys.REMAINING_SPAWN_DELAY,
            Keys.ROARING_TIME,
            Keys.SNEEZING_TIME,
            Keys.STUNNED_TIME,
            Keys.TICKS_REMAINING,
            Keys.UNHAPPY_TIME,
            Keys.WAIT_TIME
    );

    private final PluginContainer plugin;

    private boolean enabled = false;
    private boolean setItemCooldownToInfinite = false;
    private boolean setUseItemStackToInfinite = false;
    private boolean setWeatherToInfinite = false;

    @Inject
    public InfiniteTicksDataTest(final PluginContainer plugin) {
        this.plugin = plugin;
    }

    @Override
    public void enable(final CommandContext ctx) {
        this.enabled = true;
        try {
            Sponge.server().commandManager().process("reload");
        } catch (final CommandException e) {
            e.printStackTrace();
        }
        Sponge.eventManager().registerListeners(this.plugin, new InfiniteTicksDataTest.Listeners());
    }

    @Override
    public void disable(final CommandContext ctx) {
        this.enabled = false;
        try {
            Sponge.server().commandManager().process("reload");
        } catch (final CommandException e) {
            e.printStackTrace();
        }
        Sponge.eventManager().registerListeners(this.plugin, new InfiniteTicksDataTest.Listeners());
    }

    @Listener
    private void onRegisterCommand(final RegisterCommandEvent<Command.Parameterized> event) {
        final Parameter.Value<Entity> entityParameter = Parameter.entity().key("entity").build();
        final Parameter.Value<ServerLocation> locationParameter = Parameter.location().key("location").build();
        final Parameter.Value<Key<@NonNull Value<Ticks>>> keyParameter = Parameter.builder(new TypeToken<Key<@NonNull Value<Ticks>>>() { })
                .addParser((ValueParser) VariableValueParameters
                        .staticChoicesBuilder(Key.class)
                        .addChoices(InfiniteTicksDataTest.KEYS.stream().collect(Collectors.toMap(k -> k.key().value(), k -> k)))
                        .build())
                .key("key")
                .build();
        final Parameter.Value<PotionEffectType> potionEffectParameter = Parameter.registryElement(TypeToken.get(PotionEffectType.class), RegistryTypes.POTION_EFFECT_TYPE, "minecraft").key("potionEffectType").build();
        final Parameter.Value<ItemType> itemTypeParameter = Parameter.registryElement(TypeToken.get(ItemType.class), RegistryTypes.ITEM_TYPE, "minecraft").key("itemType").build();
        final Parameter.Value<WeatherType> weatherTypeParameter = Parameter.registryElement(TypeToken.get(WeatherType.class), RegistryTypes.WEATHER_TYPE, "sponge").key("weatherType").build();

        event.register(this.plugin, Command.builder()
                .addParameter(entityParameter)
                .addParameter(keyParameter)
                .executor(context -> {
                    final Entity entity = context.requireOne(entityParameter);
                    final Key<@NonNull Value<Ticks>> key = context.requireOne(keyParameter);
                    if (!entity.supports(key)) {
                        return CommandResult.error(Component.text("Unsupported key for entity: " + entity.type().findKey(RegistryTypes.ENTITY_TYPE).get()));
                    }
                    final DataTransactionResult result = entity.offer(key, Ticks.infinite());
                    if (result.isSuccessful()) {
                        context.sendMessage(Component.text("Applied!", NamedTextColor.GREEN));
                        return CommandResult.success();
                    }
                    return CommandResult.error(Component.text("Failed to offer"));
                }).build(), "offerEntityInfiniteTicks");

        event.register(this.plugin, Command.builder()
                .addParameter(locationParameter)
                .addParameter(keyParameter)
                .executor(context -> {
                    final ServerLocation location = context.requireOne(locationParameter);
                    final Key<@NonNull Value<Ticks>> key = context.requireOne(keyParameter);
                    if (!location.supports(key)) {
                        return CommandResult.error(Component.text("Unsupported key for location: " + location.blockType().findKey(RegistryTypes.BLOCK_TYPE).get()));
                    }
                    final DataTransactionResult result = location.offer(key, Ticks.infinite());
                    if (result.isSuccessful()) {
                        context.sendMessage(Component.text("Applied!", NamedTextColor.GREEN));
                        return CommandResult.success();
                    }
                    return CommandResult.error(Component.text("Failed to offer"));
                }).build(), "offerLocationInfiniteTicks");

        event.register(this.plugin, Command.builder()
                .addParameter(entityParameter)
                .addParameter(potionEffectParameter)
                .executor(context -> {
                    final Entity entity = context.requireOne(entityParameter);
                    final PotionEffectType potionEffectType = context.requireOne(potionEffectParameter);
                    if (!entity.supports(Keys.POTION_EFFECTS)) {
                        return CommandResult.error(Component.text("Unsupported key for entity: " + entity.type().findKey(RegistryTypes.ENTITY_TYPE).get()));
                    }
                    final DataTransactionResult result = entity.offerSingle(Keys.POTION_EFFECTS, PotionEffect.builder()
                            .potionType(potionEffectType)
                            .duration(Ticks.infinite())
                            .build());
                    if (result.isSuccessful()) {
                        context.sendMessage(Component.text("Applied! ", NamedTextColor.GREEN)
                                .append(Component.text(result.successfulValue(Keys.POTION_EFFECTS)
                                        .map(Value::get)
                                        .stream()
                                        .flatMap(List::stream)
                                        .filter(e -> e.duration().isInfinite())
                                        .map(e -> e.type().key(RegistryTypes.POTION_EFFECT_TYPE).asString())
                                        .collect(Collectors.joining(", ")))));
                        return CommandResult.success();
                    }
                    return CommandResult.success();
                }).build(), "offerInfinitePotion");

        event.register(this.plugin, Command.builder()
                .addParameter(entityParameter)
                .executor(context -> {
                    final Entity entity = context.requireOne(entityParameter);
                    if (!(entity instanceof Agent agent)) {
                        return CommandResult.error(Component.text("Entity is not agent: " + entity.type().findKey(RegistryTypes.ENTITY_TYPE).get()));
                    }
                    final List<? super Goal<?>> goals = agent.goal(GoalExecutorTypes.NORMAL.get())
                            .map(e -> e.tasksByType(GoalTypes.RANGED_ATTACK_AGAINST_AGENT.get()))
                            .orElse(Collections.emptyList());
                    if (goals.isEmpty()) {
                        return CommandResult.error(Component.text("Entity has no RangedAttackAgainstAgentGoal: " + entity.type().findKey(RegistryTypes.ENTITY_TYPE).get()));
                    }
                    goals.forEach(g -> ((RangedAttackAgainstAgentGoal) g).setDelayBetweenAttacks(Ticks.infinite()));
                    context.sendMessage(Component.text("Applied!", NamedTextColor.GREEN));
                    return CommandResult.success();
                }).build(), "offerInfiniteRangedAttackDelay");

        event.register(this.plugin, Command.builder()
                .addParameter(itemTypeParameter)
                .executor(context -> {
                    final ServerPlayer player = context.cause().first(ServerPlayer.class).get();
                    final ItemType itemType = context.requireOne(itemTypeParameter);
                    if (player.cooldownTracker().setCooldown(itemType, Ticks.infinite())) {
                        context.sendMessage(Component.text("Applied!", NamedTextColor.GREEN));
                        return CommandResult.success();
                    } else {
                        return CommandResult.error(Component.text("Failed to apply!"));
                    }
                }).build(), "setInfiniteItemCooldown");

        event.register(this.plugin, Command.builder()
                .addParameter(weatherTypeParameter)
                .executor(context -> {
                    final ServerPlayer player = context.cause().first(ServerPlayer.class).get();
                    final WeatherType weatherType = context.requireOne(weatherTypeParameter);
                    player.world().setWeather(weatherType, Ticks.infinite());
                    final Weather weather = player.world().weather();
                    if (weather.type() == weatherType && weather.remainingDuration().isInfinite()) {
                        context.sendMessage(Component.text("Applied! ", NamedTextColor.GREEN));
                    } else {
                        return CommandResult.error(Component.text("Failed to apply!"));
                    }
                    return CommandResult.success();
                }).build(), "setInfiniteWeather");

        event.register(this.plugin, Command.builder()
                .executor(context -> {
                    final ServerPlayer player = context.cause().first(ServerPlayer.class).get();
                    player.world().properties().setWanderingTraderSpawnDelay(Ticks.infinite());
                    if (player.world().properties().wanderingTraderSpawnDelay() == Ticks.infinite()) {
                        context.sendMessage(Component.text("Applied! ", NamedTextColor.GREEN));
                    } else {
                        return CommandResult.error(Component.text("Failed to apply!"));
                    }
                    return CommandResult.success();
                }).build(), "setInfiniteWanderingTraderSpawnDelay");

        event.register(this.plugin, Command.builder()
                .executor(context -> {
                    this.setItemCooldownToInfinite = !this.setItemCooldownToInfinite;
                    final Component newState = Component.text(
                            this.setItemCooldownToInfinite ? "ON" : "OFF", this.setItemCooldownToInfinite ? NamedTextColor.GREEN : NamedTextColor.RED);
                    context.sendMessage(Identity.nil(), Component.text("Set Item Cooldown To Infinite: ").append(newState));
                    return CommandResult.success();
                })
                .build(), "toggleSetItemCooldownToInfinite"
        );

        event.register(this.plugin, Command.builder()
                .executor(context -> {
                    this.setUseItemStackToInfinite = !this.setUseItemStackToInfinite;
                    final Component newState = Component.text(
                            this.setUseItemStackToInfinite ? "ON" : "OFF", this.setUseItemStackToInfinite ? NamedTextColor.GREEN : NamedTextColor.RED);
                    context.sendMessage(Identity.nil(), Component.text("Set Use Item Stack To Infinite: ").append(newState));
                    return CommandResult.success();
                })
                .build(), "toggleSetUseItemStackToInfinite"
        );

        event.register(this.plugin, Command.builder()
                .executor(context -> {
                    this.setWeatherToInfinite = !this.setWeatherToInfinite;
                    final Component newState = Component.text(
                            this.setWeatherToInfinite ? "ON" : "OFF", this.setWeatherToInfinite ? NamedTextColor.GREEN : NamedTextColor.RED);
                    context.sendMessage(Identity.nil(), Component.text("Set Weather To Infinite: ").append(newState));
                    return CommandResult.success();
                })
                .build(), "toggleSetWeatherToInfinite"
        );
    }

    @Listener
    private void onRecipeRegistry(final RegisterDataPackValueEvent<RecipeRegistration> event) {
        if (!this.enabled) {
            return;
        }

        event.register(this.createCookingRecipe(RecipeTypes.BLASTING.get(), "infinite_blasting_sponge"));
        event.register(this.createCookingRecipe(RecipeTypes.CAMPFIRE_COOKING.get(), "infinite_campire_cooking_sponge"));
        event.register(this.createCookingRecipe(RecipeTypes.SMELTING.get(), "infinite_smelting_sponge"));
        event.register(this.createCookingRecipe(RecipeTypes.SMOKING.get(), "infinite_smoking_sponge"));
    }

    private RecipeRegistration createCookingRecipe(final RecipeType<CookingRecipe> type, final String key) {
        return CookingRecipe.builder()
                .type(type)
                .ingredient(ItemTypes.SPONGE.get())
                .result(ItemTypes.WET_SPONGE.get())
                .cookingTime(Ticks.infinite())
                .key(ResourceKey.of(this.plugin, key))
                .build();
    }

    final class Listeners {

        @Listener
        private void onSetItemCooldown(final CooldownEvent.Set event) {
            if (InfiniteTicksDataTest.this.setItemCooldownToInfinite) {
                event.setNewCooldown(Ticks.infinite());
            }
        }

        @Listener
        private void onStartUseItemStack(final UseItemStackEvent.Start event) {
            if (InfiniteTicksDataTest.this.setUseItemStackToInfinite) {
                event.setRemainingDuration(Ticks.infinite());
            }
        }

        @Listener
        private void onChangeWeather(final ChangeWeatherEvent event) {
            if (InfiniteTicksDataTest.this.setWeatherToInfinite) {
                event.setWeather(event.weather().finalReplacement().type(), Ticks.infinite());
            }
        }
    }
}
