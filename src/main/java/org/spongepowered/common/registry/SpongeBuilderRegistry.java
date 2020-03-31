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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.inject.Singleton;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.command.parameter.managed.standard.VariableValueParameters;
import org.spongepowered.api.registry.BuilderRegistry;
import org.spongepowered.api.registry.DuplicateRegistrationException;
import org.spongepowered.api.registry.UnknownTypeException;
import org.spongepowered.api.util.ResettableBuilder;
import org.spongepowered.common.command.SpongeParameterizedCommandBuilder;
import org.spongepowered.common.command.parameter.managed.builder.SpongeTextParameterBuilder;
import org.spongepowered.common.command.parameter.multi.SpongeFirstOfParameterBuilder;
import org.spongepowered.common.command.parameter.multi.SpongeSequenceParameterBuilder;
import org.spongepowered.common.command.parameter.subcommand.SpongeSubcommandParameterBuilder;
import org.spongepowered.common.command.result.SpongeCommandResultBuilder;

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
        checkNotNull(builderClass);

        final Supplier<?> supplier = this.builders.get(builderClass);
        if (supplier == null) {
            throw new UnknownTypeException(String.format("Type '%s' has no builder registered!", builderClass));
        }

        return (T) supplier.get();
    }

    public <T> SpongeBuilderRegistry register(final Class<T> builderClass, final Supplier<? extends T> supplier) {
        checkNotNull(builderClass);
        checkNotNull(supplier);

        if (this.builders.containsKey(builderClass)) {
            throw new DuplicateRegistrationException(String.format("Type '%s' has already been registered as a builder!", builderClass));
        }

        this.builders.put(builderClass, supplier);
        return this;
    }
    
    public void registerDefaultBuilders() {
        this
            .register(ResourceKey.Builder.class, SpongeResourceKeyBuilder::new)
//            .register(ItemStack.Builder.class, SpongeItemStackBuilder::new)
//            .register(TradeOffer.Builder.class, SpongeTradeOfferBuilder::new)
//            .register(FireworkEffect.Builder.class, SpongeFireworkEffectBuilder::new)
//            .register(PotionEffect.Builder.class, SpongePotionBuilder::new)
//            .register(Objective.Builder.class, SpongeObjectiveBuilder::new)
//            .register(Team.Builder.class, SpongeTeamBuilder::new)
//            .register(Scoreboard.Builder.class, SpongeScoreboardBuilder::new)
//            .register(DamageSource.Builder.class, SpongeDamageSourceBuilder::new)
//            .register(EntityDamageSource.Builder.class, SpongeEntityDamageSourceBuilder::new)
//            .register(IndirectEntityDamageSource.Builder.class, SpongeIndirectEntityDamageSourceBuilder::new)
//            .register(FallingBlockDamageSource.Builder.class, SpongeFallingBlockDamgeSourceBuilder::new)
//            .register(BlockDamageSource.Builder.class, SpongeBlockDamageSourceBuilder::new)
//            .register(WorldArchetype.Builder.class, SpongeWorldArchetypeBuilder::new)
//            .register(Explosion.Builder.class, SpongeExplosionBuilder::new)
//            .register(BlockState.Builder.class, SpongeBlockStateBuilder::new)
//            .register(BlockSnapshot.Builder.class, SpongeBlockSnapshotBuilder::unpooled)
//            .register(EntitySnapshot.Builder.class, SpongeEntitySnapshotBuilder::new)
//            .register(ParticleEffect.Builder.class, SpongeParticleEffectBuilder::new)
//            .register(RandomWalkingGoal.Builder.class, SpongeRandomWalkingGoalBuilder::new)
//            .register(AvoidLivingGoal.Builder.class, SpongeAvoidEntityGoalBuilder::new)
//            .register(RunAroundLikeCrazyGoal.Builder.class, SpongeRunAroundLikeCrazyAIBuilder::new)
//            .register(SwimGoal.Builder.class, SpongeSwimGoalBuilder::new)
//            .register(LookAtGoal.Builder.class, SpongeWatchClosestAIBuilder::new)
//            .register(FindNearestAttackableTargetGoal.Builder.class, SpongeFindNearestAttackableTargetGoalBuilder::new)
//            .register(AttackLivingGoal.Builder.class, SpongeAttackLivingGoalBuilder::new)
//            .register(RangedAttackAgainstAgentGoal.Builder.class, SpongeRangedAttackAgainstAgentGoalBuilder::new)
//            .register(LookRandomlyGoal.Builder.class, SpongeLookRandomlyGoalBuilder::new)
//            .register(BannerPatternLayer.Builder.class, SpongePatternLayerBuilder::new)
//            .register(Task.Builder.class, SpongeTaskBuilder::new)
//            .register(Ban.Builder.class, SpongeBanBuilder::new)
//            .register(FluidStack.Builder.class, SpongeFluidStackBuilder::new)
//            .register(FluidStackSnapshot.Builder.class, SpongeFluidStackSnapshotBuilder::new)
//            .register(TabListEntry.Builder.class, TabListEntryBuilder::new)
//            .register(TradeOfferGenerator.Builder.class, SpongeTradeOfferGenerator.Builder::new)
//            .register(ItemStackGenerator.Builder.class, SpongeItemStackGenerator.Builder::new)
//            .register(ServerBossBar.Builder.class, ServerBossBarBuilder::new)
//            .register(EntityArchetype.Builder.class, SpongeEntityArchetypeBuilder::new)
//            .register(BlockEntityArchetype.Builder.class, SpongeBlockEntityArchetypeBuilder::new)
//            .register(Schematic.Builder.class, SpongeSchematicBuilder::new)
//            .register(VirtualBiomeType.Builder.class, SpongeVirtualBiomeTypeBuilder::new)
//            .register(Inventory.Builder.class, SpongeInventoryBuilder::new)
//            .register(ViewableInventory.Builder.class, SpongeViewableInventoryBuilder::new)
//            .register(SoundType.Builder.class, SpongeSoundBuilder::new)
//            .register(LocatableBlock.Builder.class, SpongeLocatableBlockBuilder::new)
//            .register(DataRegistration.Builder.class, SpongeDataRegistrationBuilder::new)
//            .register(WorldBorder.Builder.class, SpongeWorldBorderBuilder::new)
//            .register(Ingredient.Builder.class, SpongeIngredientBuilder::new)
//            .register(ShapedCraftingRecipe.Builder.class, SpongeShapedCraftingRecipeBuilder::new)
//            .register(ShapelessCraftingRecipe.Builder.class, SpongeShapelessCraftingRecipeBuilder::new)
//            .register(SpecialCraftingRecipe.Builder.class, SpongeSpecialCraftingRecipeBuilder::new)
//            .register(SmeltingRecipe.Builder.class, SpongeSmeltingRecipeBuilder::new)
//            .register(StoneCutterRecipe.Builder.class, SpongeStoneCutterRecipeBuilder::new)
//            .register(EventContextKey.Builder.class, SpongeEventContextKeyBuilder::new)
//            .register(Enchantment.Builder.class, SpongeEnchantmentBuilder::new)
//            .register(Enchantment.RandomListBuilder.class, SpongeRandomEnchantmentListBuilder::new)
//            .register(Key.Builder.class, SpongeKeyBuilder::new)
//            .register(Advancement.Builder.class, SpongeAdvancementBuilder::new)
//            .register(AdvancementTree.Builder.class, SpongeAdvancementTreeBuilder::new)
//            .register(DisplayInfo.Builder.class, SpongeDisplayInfoBuilder::new)
//            .register(AdvancementCriterion.Builder.class, SpongeCriterionBuilder::new)
//            .register(ScoreAdvancementCriterion.Builder.class, SpongeScoreCriterionBuilder::new)
//            .register(FilteredTrigger.Builder.class, SpongeFilteredTriggerBuilder::new)
//            .register(Trigger.Builder.class, SpongeTriggerBuilder::new)
//            .register(ResourceKey.Builder.class, SpongeCatalogKeyBuilder::new)
            .register(Command.Builder.class, SpongeParameterizedCommandBuilder::new)
            .register(Parameter.FirstOfBuilder.class, SpongeFirstOfParameterBuilder::new)
            .register(Parameter.SequenceBuilder.class, SpongeSequenceParameterBuilder::new)
            .register(Parameter.Subcommand.Builder.class, SpongeSubcommandParameterBuilder::new)
            .register(VariableValueParameters.TextBuilder.class, SpongeTextParameterBuilder::new)
            .register(CommandResult.Builder.class, SpongeCommandResultBuilder::new)
        ;
    }
}
