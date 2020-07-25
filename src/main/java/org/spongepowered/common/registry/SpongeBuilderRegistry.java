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
package org.spongepowered.common.registry;

import com.google.common.base.Preconditions;
import com.google.inject.Singleton;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.advancement.AdvancementTree;
import org.spongepowered.api.advancement.DisplayInfo;
import org.spongepowered.api.advancement.criteria.AdvancementCriterion;
import org.spongepowered.api.advancement.criteria.ScoreAdvancementCriterion;
import org.spongepowered.api.advancement.criteria.trigger.FilteredTrigger;
import org.spongepowered.api.advancement.criteria.trigger.Trigger;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.command.parameter.managed.Flag;
import org.spongepowered.api.command.parameter.managed.standard.VariableValueParameters;
import org.spongepowered.api.command.selector.Selector;
import org.spongepowered.api.data.DataRegistration;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.meta.BannerPatternLayer;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.effect.sound.SoundType;
import org.spongepowered.api.entity.ai.goal.builtin.LookAtGoal;
import org.spongepowered.api.entity.ai.goal.builtin.LookRandomlyGoal;
import org.spongepowered.api.entity.ai.goal.builtin.SwimGoal;
import org.spongepowered.api.entity.ai.goal.builtin.creature.AttackLivingGoal;
import org.spongepowered.api.entity.ai.goal.builtin.creature.AvoidLivingGoal;
import org.spongepowered.api.entity.ai.goal.builtin.creature.RandomWalkingGoal;
import org.spongepowered.api.entity.ai.goal.builtin.creature.RangedAttackAgainstAgentGoal;
import org.spongepowered.api.entity.ai.goal.builtin.creature.horse.RunAroundLikeCrazyGoal;
import org.spongepowered.api.entity.ai.goal.builtin.creature.target.FindNearestAttackableTargetGoal;
import org.spongepowered.api.entity.attribute.AttributeModifier;
import org.spongepowered.api.entity.living.player.tab.TabListEntry;
import org.spongepowered.api.event.cause.EventContextKey;
import org.spongepowered.api.event.cause.entity.damage.source.BlockDamageSource;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSource;
import org.spongepowered.api.event.cause.entity.damage.source.EntityDamageSource;
import org.spongepowered.api.event.cause.entity.damage.source.FallingBlockDamageSource;
import org.spongepowered.api.event.cause.entity.damage.source.IndirectEntityDamageSource;
import org.spongepowered.api.fluid.FluidStack;
import org.spongepowered.api.fluid.FluidStackSnapshot;
import org.spongepowered.api.item.FireworkEffect;
import org.spongepowered.api.item.enchantment.Enchantment;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackGenerator;
import org.spongepowered.api.item.inventory.transaction.InventoryTransactionResult;
import org.spongepowered.api.item.inventory.type.ViewableInventory;
import org.spongepowered.api.item.merchant.TradeOffer;
import org.spongepowered.api.item.merchant.TradeOfferGenerator;
import org.spongepowered.api.item.recipe.crafting.Ingredient;
import org.spongepowered.api.item.recipe.crafting.ShapedCraftingRecipe;
import org.spongepowered.api.item.recipe.crafting.ShapelessCraftingRecipe;
import org.spongepowered.api.item.recipe.crafting.SpecialCraftingRecipe;
import org.spongepowered.api.item.recipe.single.StoneCutterRecipe;
import org.spongepowered.api.item.recipe.smelting.SmeltingRecipe;
import org.spongepowered.api.registry.BuilderRegistry;
import org.spongepowered.api.registry.DuplicateRegistrationException;
import org.spongepowered.api.registry.UnknownTypeException;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.scoreboard.Scoreboard;
import org.spongepowered.api.scoreboard.Team;
import org.spongepowered.api.scoreboard.objective.Objective;
import org.spongepowered.api.util.ResettableBuilder;
import org.spongepowered.api.util.ban.Ban;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.api.world.WorldArchetype;
import org.spongepowered.api.world.WorldBorder;
import org.spongepowered.api.world.biome.VirtualBiomeType;
import org.spongepowered.api.world.explosion.Explosion;
import org.spongepowered.common.advancement.SpongeAdvancementTreeBuilder;
import org.spongepowered.common.advancement.SpongeCriterionBuilder;
import org.spongepowered.common.advancement.SpongeDisplayInfoBuilder;
import org.spongepowered.common.advancement.SpongeFilteredTriggerBuilder;
import org.spongepowered.common.advancement.SpongeScoreCriterionBuilder;
import org.spongepowered.common.advancement.SpongeTriggerBuilder;
import org.spongepowered.common.ban.SpongeBanBuilder;
import org.spongepowered.common.block.SpongeBlockSnapshotBuilder;
import org.spongepowered.common.block.SpongeBlockStateBuilder;
import org.spongepowered.common.command.SpongeParameterizedCommandBuilder;
import org.spongepowered.common.command.parameter.SpongeParameterKeyBuilder;
import org.spongepowered.common.command.parameter.flag.SpongeFlagBuilder;
import org.spongepowered.common.command.parameter.managed.builder.SpongeTextParameterBuilder;
import org.spongepowered.common.command.parameter.multi.SpongeFirstOfParameterBuilder;
import org.spongepowered.common.command.parameter.multi.SpongeSequenceParameterBuilder;
import org.spongepowered.common.command.parameter.subcommand.SpongeSubcommandParameterBuilder;
import org.spongepowered.common.command.result.SpongeCommandResultBuilder;
import org.spongepowered.common.command.selector.SpongeSelectorFactory;
import org.spongepowered.common.data.SpongeDataRegistrationBuilder;
import org.spongepowered.common.data.builder.meta.SpongePatternLayerBuilder;
import org.spongepowered.common.data.key.SpongeKeyBuilder;
import org.spongepowered.common.effect.potion.SpongePotionBuilder;
import org.spongepowered.common.effect.sound.SpongeSoundBuilder;
import org.spongepowered.common.entity.ai.SpongeWatchClosestAIBuilder;
import org.spongepowered.common.entity.ai.goal.builtin.SpongeLookRandomlyGoalBuilder;
import org.spongepowered.common.entity.ai.goal.builtin.SpongeSwimGoalBuilder;
import org.spongepowered.common.entity.ai.goal.builtin.creature.SpongeAttackLivingGoalBuilder;
import org.spongepowered.common.entity.ai.goal.builtin.creature.SpongeAvoidLivingGoalBuilder;
import org.spongepowered.common.entity.ai.goal.builtin.creature.SpongeRandomWalkingGoalBuilder;
import org.spongepowered.common.entity.ai.goal.builtin.creature.SpongeRangedAttackAgainstAgentGoalBuilder;
import org.spongepowered.common.entity.ai.goal.builtin.creature.horse.SpongeRunAroundLikeCrazyAIBuilder;
import org.spongepowered.common.entity.ai.goal.builtin.creature.target.SpongeFindNearestAttackableTargetGoalBuilder;
import org.spongepowered.common.entity.attribute.SpongeAttributeModifierBuilder;
import org.spongepowered.common.entity.player.tab.TabListEntryBuilder;
import org.spongepowered.common.event.SpongeEventContextKeyBuilder;
import org.spongepowered.common.event.damage.SpongeBlockDamageSourceBuilder;
import org.spongepowered.common.event.damage.SpongeDamageSourceBuilder;
import org.spongepowered.common.event.damage.SpongeEntityDamageSourceBuilder;
import org.spongepowered.common.event.damage.SpongeFallingBlockDamgeSourceBuilder;
import org.spongepowered.common.event.damage.SpongeIndirectEntityDamageSourceBuilder;
import org.spongepowered.common.fluid.SpongeFluidStackBuilder;
import org.spongepowered.common.fluid.SpongeFluidStackSnapshotBuilder;
import org.spongepowered.common.inventory.InventoryTransactionResultImpl;
import org.spongepowered.common.inventory.SpongeInventoryBuilder;
import org.spongepowered.common.inventory.custom.SpongeViewableInventoryBuilder;
import org.spongepowered.common.item.SpongeFireworkEffectBuilder;
import org.spongepowered.common.item.SpongeItemStackBuilder;
import org.spongepowered.common.item.enchantment.SpongeEnchantmentBuilder;
import org.spongepowered.common.item.enchantment.SpongeRandomEnchantmentListBuilder;
import org.spongepowered.common.item.generation.SpongeItemStackGenerator;
import org.spongepowered.common.item.merchant.SpongeTradeOfferBuilder;
import org.spongepowered.common.item.merchant.SpongeTradeOfferGenerator;
import org.spongepowered.common.item.recipe.crafting.SpongeIngredientBuilder;
import org.spongepowered.common.item.recipe.crafting.SpongeShapedCraftingRecipeBuilder;
import org.spongepowered.common.item.recipe.crafting.SpongeShapelessCraftingRecipeBuilder;
import org.spongepowered.common.item.recipe.crafting.SpongeSpecialCraftingRecipeBuilder;
import org.spongepowered.common.item.recipe.crafting.SpongeStoneCutterRecipeBuilder;
import org.spongepowered.common.item.recipe.smelting.SpongeSmeltingRecipeBuilder;
import org.spongepowered.common.scheduler.SpongeTaskBuilder;
import org.spongepowered.common.scoreboard.builder.SpongeObjectiveBuilder;
import org.spongepowered.common.scoreboard.builder.SpongeScoreboardBuilder;
import org.spongepowered.common.scoreboard.builder.SpongeTeamBuilder;
import org.spongepowered.common.world.SpongeExplosionBuilder;
import org.spongepowered.common.world.SpongeLocatableBlockBuilder;
import org.spongepowered.common.world.SpongeWorldArchetypeBuilder;
import org.spongepowered.common.world.biome.SpongeVirtualBiomeTypeBuilder;
import org.spongepowered.common.world.border.SpongeWorldBorderBuilder;

import java.util.Map;
import java.util.function.Supplier;

@Singleton
@SuppressWarnings("unchecked")
public final class SpongeBuilderRegistry implements BuilderRegistry {

    private final Map<Class<?>, Supplier<?>> builders;

    public SpongeBuilderRegistry() {
        this.builders = new Object2ObjectArrayMap<>();
    }

    @Override
    public <T extends ResettableBuilder<?, ? super T>> T provideBuilder(final Class<T> builderClass) {
        Preconditions.checkNotNull(builderClass);

        final Supplier<?> supplier = this.builders.get(builderClass);
        if (supplier == null) {
            throw new UnknownTypeException(String.format("Type '%s' has no builder registered!", builderClass));
        }

        return (T) supplier.get();
    }

    public <T> SpongeBuilderRegistry register(final Class<T> builderClass, final Supplier<? extends T> supplier) {
        Preconditions.checkNotNull(builderClass);
        Preconditions.checkNotNull(supplier);

        if (this.builders.containsKey(builderClass)) {
            throw new DuplicateRegistrationException(String.format("Type '%s' has already been registered as a builder!", builderClass));
        }

        this.builders.put(builderClass, supplier);
        return this;
    }
    
    public void registerDefaultBuilders() {
        this
            .register(ResourceKey.Builder.class, SpongeResourceKeyBuilder::new)
            .register(ItemStack.Builder.class, SpongeItemStackBuilder::new)
            .register(TradeOffer.Builder.class, SpongeTradeOfferBuilder::new)
            .register(FireworkEffect.Builder.class, SpongeFireworkEffectBuilder::new)
            .register(PotionEffect.Builder.class, SpongePotionBuilder::new)
            .register(Objective.Builder.class, SpongeObjectiveBuilder::new)
            .register(Team.Builder.class, SpongeTeamBuilder::new)
            .register(Scoreboard.Builder.class, SpongeScoreboardBuilder::new)
            .register(DamageSource.Builder.class, SpongeDamageSourceBuilder::new)
            .register(EntityDamageSource.Builder.class, SpongeEntityDamageSourceBuilder::new)
            .register(IndirectEntityDamageSource.Builder.class, SpongeIndirectEntityDamageSourceBuilder::new)
            .register(FallingBlockDamageSource.Builder.class, SpongeFallingBlockDamgeSourceBuilder::new)
            .register(BlockDamageSource.Builder.class, SpongeBlockDamageSourceBuilder::new)
            .register(WorldArchetype.Builder.class, SpongeWorldArchetypeBuilder::new)
            .register(Explosion.Builder.class, SpongeExplosionBuilder::new)
            .register(BlockState.Builder.class, SpongeBlockStateBuilder::new)
            .register(BlockSnapshot.Builder.class, SpongeBlockSnapshotBuilder::unpooled)
//            .register(EntitySnapshot.Builder.class, SpongeEntitySnapshotBuilder::new)
//            .register(ParticleEffect.Builder.class, SpongeParticleEffectBuilder::new)
            .register(RandomWalkingGoal.Builder.class, SpongeRandomWalkingGoalBuilder::new)
            .register(AvoidLivingGoal.Builder.class, SpongeAvoidLivingGoalBuilder::new)
            .register(RunAroundLikeCrazyGoal.Builder.class, SpongeRunAroundLikeCrazyAIBuilder::new)
            .register(SwimGoal.Builder.class, SpongeSwimGoalBuilder::new)
            .register(LookAtGoal.Builder.class, SpongeWatchClosestAIBuilder::new)
            .register(FindNearestAttackableTargetGoal.Builder.class, SpongeFindNearestAttackableTargetGoalBuilder::new)
            .register(AttackLivingGoal.Builder.class, SpongeAttackLivingGoalBuilder::new)
            .register(RangedAttackAgainstAgentGoal.Builder.class, SpongeRangedAttackAgainstAgentGoalBuilder::new)
            .register(LookRandomlyGoal.Builder.class, SpongeLookRandomlyGoalBuilder::new)
            .register(BannerPatternLayer.Builder.class, SpongePatternLayerBuilder::new)
            .register(Task.Builder.class, SpongeTaskBuilder::new)
            .register(Ban.Builder.class, SpongeBanBuilder::new)
            .register(FluidStack.Builder.class, SpongeFluidStackBuilder::new)
            .register(FluidStackSnapshot.Builder.class, SpongeFluidStackSnapshotBuilder::new)
            .register(TabListEntry.Builder.class, TabListEntryBuilder::new)
            .register(TradeOfferGenerator.Builder.class, SpongeTradeOfferGenerator.Builder::new)
            .register(ItemStackGenerator.Builder.class, SpongeItemStackGenerator.Builder::new)
//            .register(EntityArchetype.Builder.class, SpongeEntityArchetypeBuilder::new)
//            .register(BlockEntityArchetype.Builder.class, SpongeBlockEntityArchetypeBuilder::new)
//            .register(Schematic.Builder.class, SpongeSchematicBuilder::new)
            .register(VirtualBiomeType.Builder.class, SpongeVirtualBiomeTypeBuilder::new)
            .register(Inventory.Builder.class, SpongeInventoryBuilder::new)
            .register(ViewableInventory.Builder.class, SpongeViewableInventoryBuilder::new)
            .register(InventoryTransactionResult.Builder.class, InventoryTransactionResultImpl.Builder::new)
            .register(SoundType.Builder.class, SpongeSoundBuilder::new)
            .register(LocatableBlock.Builder.class, SpongeLocatableBlockBuilder::new)
            .register(DataRegistration.Builder.class, SpongeDataRegistrationBuilder::new)
            .register(WorldBorder.Builder.class, SpongeWorldBorderBuilder::new)
            .register(Ingredient.Builder.class, SpongeIngredientBuilder::new)
            .register(ShapedCraftingRecipe.Builder.class, SpongeShapedCraftingRecipeBuilder::new)
            .register(ShapelessCraftingRecipe.Builder.class, SpongeShapelessCraftingRecipeBuilder::new)
            .register(SpecialCraftingRecipe.Builder.class, SpongeSpecialCraftingRecipeBuilder::new)
            .register(SmeltingRecipe.Builder.class, SpongeSmeltingRecipeBuilder::new)
            .register(StoneCutterRecipe.Builder.class, SpongeStoneCutterRecipeBuilder::new)
            .register(EventContextKey.Builder.class, SpongeEventContextKeyBuilder::new)
            .register(Enchantment.Builder.class, SpongeEnchantmentBuilder::new)
            .register(Enchantment.RandomListBuilder.class, SpongeRandomEnchantmentListBuilder::new)
            .register(Key.Builder.class, SpongeKeyBuilder::new)
//            .register(Advancement.Builder.class, SpongeAdvancementBuilder::new)
            .register(AdvancementTree.Builder.class, SpongeAdvancementTreeBuilder::new)
            .register(DisplayInfo.Builder.class, SpongeDisplayInfoBuilder::new)
            .register(AdvancementCriterion.Builder.class, SpongeCriterionBuilder::new)
            .register(ScoreAdvancementCriterion.Builder.class, SpongeScoreCriterionBuilder::new)
            .register(FilteredTrigger.Builder.class, SpongeFilteredTriggerBuilder::new)
            .register(Trigger.Builder.class, SpongeTriggerBuilder::new)
            .register(AttributeModifier.Builder.class, SpongeAttributeModifierBuilder::new)
            .register(Command.Builder.class, SpongeParameterizedCommandBuilder::new)
            .register(Parameter.FirstOfBuilder.class, SpongeFirstOfParameterBuilder::new)
            .register(Parameter.SequenceBuilder.class, SpongeSequenceParameterBuilder::new)
            .register(Parameter.Subcommand.Builder.class, SpongeSubcommandParameterBuilder::new)
            .register(VariableValueParameters.TextBuilder.class, SpongeTextParameterBuilder::new)
            .register(CommandResult.Builder.class, SpongeCommandResultBuilder::new)
            .register(Parameter.Key.Builder.class, SpongeParameterKeyBuilder::new)
            .register(Flag.Builder.class, SpongeFlagBuilder::new)
            .register(Selector.Builder.class, SpongeSelectorFactory::createBuilder)
        ;
    }
}
