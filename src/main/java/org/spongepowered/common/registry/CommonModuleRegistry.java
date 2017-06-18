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

import org.spongepowered.api.Sponge;
import org.spongepowered.api.advancement.Advancement;
import org.spongepowered.api.advancement.AdvancementTree;
import org.spongepowered.api.advancement.AdvancementType;
import org.spongepowered.api.advancement.DisplayInfo;
import org.spongepowered.api.advancement.criteria.AdvancementCriterion;
import org.spongepowered.api.advancement.criteria.ScoreAdvancementCriterion;
import org.spongepowered.api.advancement.criteria.trigger.FilteredTrigger;
import org.spongepowered.api.advancement.criteria.trigger.Trigger;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.tileentity.TileEntityArchetype;
import org.spongepowered.api.block.tileentity.TileEntityType;
import org.spongepowered.api.block.trait.BooleanTrait;
import org.spongepowered.api.block.trait.EnumTrait;
import org.spongepowered.api.block.trait.IntegerTrait;
import org.spongepowered.api.boss.BossBarColor;
import org.spongepowered.api.boss.BossBarOverlay;
import org.spongepowered.api.boss.ServerBossBar;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.format.CommandMessageFormat;
import org.spongepowered.api.command.managed.ChildExceptionBehavior;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.command.parameter.flag.Flags;
import org.spongepowered.api.command.parameter.flag.UnknownFlagBehavior;
import org.spongepowered.api.command.parameter.managed.standard.CatalogedSelectorParser;
import org.spongepowered.api.command.parameter.managed.standard.CatalogedValueParameter;
import org.spongepowered.api.command.parameter.managed.standard.CatalogedValueParameterModifier;
import org.spongepowered.api.command.parameter.managed.standard.VariableValueParameterModifiers;
import org.spongepowered.api.command.parameter.managed.standard.VariableValueParameters;
import org.spongepowered.api.command.parameter.token.InputTokenizer;
import org.spongepowered.api.data.DataRegistration;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.meta.PatternLayer;
import org.spongepowered.api.data.persistence.DataFormat;
import org.spongepowered.api.data.persistence.DataTranslator;
import org.spongepowered.api.data.type.ArmorType;
import org.spongepowered.api.data.type.Art;
import org.spongepowered.api.data.type.BannerPatternShape;
import org.spongepowered.api.data.type.BigMushroomType;
import org.spongepowered.api.data.type.BodyPart;
import org.spongepowered.api.data.type.BrickType;
import org.spongepowered.api.data.type.Career;
import org.spongepowered.api.data.type.CoalType;
import org.spongepowered.api.data.type.ComparatorType;
import org.spongepowered.api.data.type.CookedFish;
import org.spongepowered.api.data.type.DirtType;
import org.spongepowered.api.data.type.DisguisedBlockType;
import org.spongepowered.api.data.type.DoublePlantType;
import org.spongepowered.api.data.type.DyeColor;
import org.spongepowered.api.data.type.Fish;
import org.spongepowered.api.data.type.GoldenApple;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.data.type.Hinge;
import org.spongepowered.api.data.type.HorseColor;
import org.spongepowered.api.data.type.HorseStyle;
import org.spongepowered.api.data.type.InstrumentType;
import org.spongepowered.api.data.type.LogAxis;
import org.spongepowered.api.data.type.NotePitch;
import org.spongepowered.api.data.type.OcelotType;
import org.spongepowered.api.data.type.ParrotVariant;
import org.spongepowered.api.data.type.PickupRule;
import org.spongepowered.api.data.type.PistonType;
import org.spongepowered.api.data.type.PlantType;
import org.spongepowered.api.data.type.PortionType;
import org.spongepowered.api.data.type.PrismarineType;
import org.spongepowered.api.data.type.Profession;
import org.spongepowered.api.data.type.QuartzType;
import org.spongepowered.api.data.type.RabbitType;
import org.spongepowered.api.data.type.RailDirection;
import org.spongepowered.api.data.type.SandType;
import org.spongepowered.api.data.type.SandstoneType;
import org.spongepowered.api.data.type.ShrubType;
import org.spongepowered.api.data.type.SkinPart;
import org.spongepowered.api.data.type.SkullType;
import org.spongepowered.api.data.type.SlabType;
import org.spongepowered.api.data.type.StairShape;
import org.spongepowered.api.data.type.StoneType;
import org.spongepowered.api.data.type.StructureMode;
import org.spongepowered.api.data.type.ToolType;
import org.spongepowered.api.data.type.TreeType;
import org.spongepowered.api.data.type.WallType;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.particle.ParticleOption;
import org.spongepowered.api.effect.particle.ParticleType;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.effect.potion.PotionEffectType;
import org.spongepowered.api.effect.sound.SoundCategory;
import org.spongepowered.api.effect.sound.SoundType;
import org.spongepowered.api.effect.sound.record.RecordType;
import org.spongepowered.api.entity.EntityArchetype;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.ai.GoalType;
import org.spongepowered.api.entity.ai.task.AITaskType;
import org.spongepowered.api.entity.ai.task.builtin.LookIdleAITask;
import org.spongepowered.api.entity.ai.task.builtin.SwimmingAITask;
import org.spongepowered.api.entity.ai.task.builtin.WatchClosestAITask;
import org.spongepowered.api.entity.ai.task.builtin.creature.AttackLivingAITask;
import org.spongepowered.api.entity.ai.task.builtin.creature.AvoidEntityAITask;
import org.spongepowered.api.entity.ai.task.builtin.creature.RangeAgentAITask;
import org.spongepowered.api.entity.ai.task.builtin.creature.WanderAITask;
import org.spongepowered.api.entity.ai.task.builtin.creature.horse.RunAroundLikeCrazyAITask;
import org.spongepowered.api.entity.ai.task.builtin.creature.target.FindNearestAttackableTargetAITask;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.entity.living.player.tab.TabListEntry;
import org.spongepowered.api.event.cause.EventContextKey;
import org.spongepowered.api.event.cause.entity.damage.DamageModifierType;
import org.spongepowered.api.event.cause.entity.damage.DamageType;
import org.spongepowered.api.event.cause.entity.damage.source.BlockDamageSource;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSource;
import org.spongepowered.api.event.cause.entity.damage.source.EntityDamageSource;
import org.spongepowered.api.event.cause.entity.damage.source.FallingBlockDamageSource;
import org.spongepowered.api.event.cause.entity.damage.source.IndirectEntityDamageSource;
import org.spongepowered.api.event.cause.entity.dismount.DismountType;
import org.spongepowered.api.event.cause.entity.spawn.SpawnType;
import org.spongepowered.api.event.cause.entity.teleport.TeleportType;
import org.spongepowered.api.extra.fluid.FluidStack;
import org.spongepowered.api.extra.fluid.FluidStackSnapshot;
import org.spongepowered.api.extra.fluid.FluidType;
import org.spongepowered.api.item.FireworkEffect;
import org.spongepowered.api.item.FireworkShape;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.enchantment.Enchantment;
import org.spongepowered.api.item.enchantment.EnchantmentType;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.InventoryArchetype;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackGenerator;
import org.spongepowered.api.item.inventory.equipment.EquipmentType;
import org.spongepowered.api.item.inventory.property.GuiId;
import org.spongepowered.api.item.inventory.query.QueryOperationType;
import org.spongepowered.api.item.merchant.TradeOffer;
import org.spongepowered.api.item.merchant.TradeOfferGenerator;
import org.spongepowered.api.item.recipe.crafting.CraftingRecipe;
import org.spongepowered.api.item.recipe.crafting.Ingredient;
import org.spongepowered.api.item.recipe.crafting.ShapedCraftingRecipe;
import org.spongepowered.api.item.recipe.crafting.ShapelessCraftingRecipe;
import org.spongepowered.api.item.recipe.smelting.SmeltingRecipe;
import org.spongepowered.api.registry.FactoryRegistry;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.scoreboard.CollisionRule;
import org.spongepowered.api.scoreboard.Scoreboard;
import org.spongepowered.api.scoreboard.Team;
import org.spongepowered.api.scoreboard.Visibility;
import org.spongepowered.api.scoreboard.critieria.Criterion;
import org.spongepowered.api.scoreboard.displayslot.DisplaySlot;
import org.spongepowered.api.scoreboard.objective.Objective;
import org.spongepowered.api.scoreboard.objective.displaymode.ObjectiveDisplayMode;
import org.spongepowered.api.service.economy.transaction.TransactionType;
import org.spongepowered.api.statistic.Statistic;
import org.spongepowered.api.statistic.StatisticType;
import org.spongepowered.api.text.chat.ChatVisibility;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextStyle;
import org.spongepowered.api.text.selector.SelectorType;
import org.spongepowered.api.text.serializer.TextSerializer;
import org.spongepowered.api.util.ban.Ban;
import org.spongepowered.api.util.ban.BanType;
import org.spongepowered.api.util.rotation.Rotation;
import org.spongepowered.api.world.DimensionType;
import org.spongepowered.api.world.GeneratorType;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.api.world.PortalAgentType;
import org.spongepowered.api.world.SerializationBehavior;
import org.spongepowered.api.world.WorldArchetype;
import org.spongepowered.api.world.WorldBorder;
import org.spongepowered.api.world.biome.BiomeGenerationSettings;
import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.api.world.biome.VirtualBiomeType;
import org.spongepowered.api.world.difficulty.Difficulty;
import org.spongepowered.api.world.explosion.Explosion;
import org.spongepowered.api.world.gen.PopulatorObject;
import org.spongepowered.api.world.gen.PopulatorType;
import org.spongepowered.api.world.gen.WorldGeneratorModifier;
import org.spongepowered.api.world.gen.populator.BigMushroom;
import org.spongepowered.api.world.gen.populator.BlockBlob;
import org.spongepowered.api.world.gen.populator.Cactus;
import org.spongepowered.api.world.gen.populator.ChorusFlower;
import org.spongepowered.api.world.gen.populator.DeadBush;
import org.spongepowered.api.world.gen.populator.DesertWell;
import org.spongepowered.api.world.gen.populator.DoublePlant;
import org.spongepowered.api.world.gen.populator.Dungeon;
import org.spongepowered.api.world.gen.populator.EndIsland;
import org.spongepowered.api.world.gen.populator.Flower;
import org.spongepowered.api.world.gen.populator.Forest;
import org.spongepowered.api.world.gen.populator.Fossil;
import org.spongepowered.api.world.gen.populator.Glowstone;
import org.spongepowered.api.world.gen.populator.IcePath;
import org.spongepowered.api.world.gen.populator.IceSpike;
import org.spongepowered.api.world.gen.populator.Lake;
import org.spongepowered.api.world.gen.populator.Melon;
import org.spongepowered.api.world.gen.populator.Mushroom;
import org.spongepowered.api.world.gen.populator.NetherFire;
import org.spongepowered.api.world.gen.populator.Ore;
import org.spongepowered.api.world.gen.populator.Pumpkin;
import org.spongepowered.api.world.gen.populator.RandomBlock;
import org.spongepowered.api.world.gen.populator.RandomObject;
import org.spongepowered.api.world.gen.populator.Reed;
import org.spongepowered.api.world.gen.populator.SeaFloor;
import org.spongepowered.api.world.gen.populator.Shrub;
import org.spongepowered.api.world.gen.populator.Vine;
import org.spongepowered.api.world.gen.populator.WaterLily;
import org.spongepowered.api.world.gen.type.BiomeTreeType;
import org.spongepowered.api.world.gen.type.MushroomType;
import org.spongepowered.api.world.schematic.BlockPaletteType;
import org.spongepowered.api.world.schematic.Schematic;
import org.spongepowered.api.world.teleport.TeleportHelperFilter;
import org.spongepowered.api.world.weather.Weather;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.advancement.SpongeAdvancementBuilder;
import org.spongepowered.common.advancement.SpongeAdvancementTreeBuilder;
import org.spongepowered.common.advancement.SpongeCriterionBuilder;
import org.spongepowered.common.advancement.SpongeDisplayInfoBuilder;
import org.spongepowered.common.advancement.SpongeFilteredTriggerBuilder;
import org.spongepowered.common.advancement.SpongeScoreCriterionBuilder;
import org.spongepowered.common.advancement.SpongeTriggerBuilder;
import org.spongepowered.common.ban.SpongeBanBuilder;
import org.spongepowered.common.block.SpongeBlockSnapshotBuilder;
import org.spongepowered.common.block.SpongeBlockStateBuilder;
import org.spongepowered.common.block.SpongeTileEntityArchetypeBuilder;
import org.spongepowered.common.boss.ServerBossBarBuilder;
import org.spongepowered.common.command.managed.SpongeCommandBuilder;
import org.spongepowered.common.command.parameter.SpongeParameterBuilder;
import org.spongepowered.common.command.parameter.SpongeParameterFirstOfBuilder;
import org.spongepowered.common.command.parameter.SpongeParameterSequenceBuilder;
import org.spongepowered.common.command.parameter.flag.SpongeFlagsBuilder;
import org.spongepowered.common.command.parameter.modifier.builder.SpongeDefaultValueModifierBuilder;
import org.spongepowered.common.command.parameter.modifier.builder.SpongeRepeatedValueModifierBuilder;
import org.spongepowered.common.command.parameter.value.builder.SpongeCatalogTypeValueParameterBuilder;
import org.spongepowered.common.command.parameter.value.builder.SpongeDynamicChoicesValueParameterBuilder;
import org.spongepowered.common.command.parameter.value.builder.SpongeEnumValueParameterBuilder;
import org.spongepowered.common.command.parameter.value.builder.SpongeLiteralValueParameterBuilder;
import org.spongepowered.common.command.parameter.value.builder.SpongeStaticChoicesValueParameterBuilder;
import org.spongepowered.common.command.parameter.value.builder.SpongeTextValueParameterBuilder;
import org.spongepowered.common.command.result.SpongeCommandResultBuilder;
import org.spongepowered.common.data.SpongeDataRegistrationBuilder;
import org.spongepowered.common.data.SpongeKeyBuilder;
import org.spongepowered.common.data.SpongeManipulatorRegistry;
import org.spongepowered.common.data.builder.meta.SpongePatternLayerBuilder;
import org.spongepowered.common.effect.particle.SpongeParticleEffectBuilder;
import org.spongepowered.common.effect.potion.SpongePotionBuilder;
import org.spongepowered.common.effect.sound.SpongeSoundBuilder;
import org.spongepowered.common.entity.SpongeEntityArchetypeBuilder;
import org.spongepowered.common.entity.SpongeEntitySnapshotBuilder;
import org.spongepowered.common.entity.ai.SpongeAttackLivingAIBuilder;
import org.spongepowered.common.entity.ai.SpongeAvoidEntityAIBuilder;
import org.spongepowered.common.entity.ai.SpongeLookIdleAIBuilder;
import org.spongepowered.common.entity.ai.SpongeRangeAgentAIBuilder;
import org.spongepowered.common.entity.ai.SpongeRunAroundLikeCrazyAIBuilder;
import org.spongepowered.common.entity.ai.SpongeSwimmingAIBuilder;
import org.spongepowered.common.entity.ai.SpongeWanderAIBuilder;
import org.spongepowered.common.entity.ai.SpongeWatchClosestAIBuilder;
import org.spongepowered.common.entity.ai.target.SpongeFindNearestAttackableTargetAIBuilder;
import org.spongepowered.common.entity.player.tab.TabListEntryBuilder;
import org.spongepowered.common.event.SpongeEventContextKeyBuilder;
import org.spongepowered.common.event.damage.SpongeBlockDamageSourceBuilder;
import org.spongepowered.common.event.damage.SpongeDamageSourceBuilder;
import org.spongepowered.common.event.damage.SpongeEntityDamageSourceBuilder;
import org.spongepowered.common.event.damage.SpongeFallingBlockDamgeSourceBuilder;
import org.spongepowered.common.event.damage.SpongeIndirectEntityDamageSourceBuilder;
import org.spongepowered.common.extra.fluid.SpongeFluidStackBuilder;
import org.spongepowered.common.extra.fluid.SpongeFluidStackSnapshotBuilder;
import org.spongepowered.common.item.SpongeFireworkEffectBuilder;
import org.spongepowered.common.item.enchantment.SpongeEnchantmentBuilder;
import org.spongepowered.common.item.inventory.SpongeInventoryBuilder;
import org.spongepowered.common.item.inventory.SpongeItemStackBuilder;
import org.spongepowered.common.item.inventory.archetype.SpongeInventoryArchetypeBuilder;
import org.spongepowered.common.item.inventory.generation.SpongeItemStackGenerator;
import org.spongepowered.common.item.merchant.SpongeTradeOfferBuilder;
import org.spongepowered.common.item.merchant.SpongeTradeOfferGenerator;
import org.spongepowered.common.item.recipe.crafting.SpongeCraftingRecipeRegistry;
import org.spongepowered.common.item.recipe.crafting.SpongeIngredientBuilder;
import org.spongepowered.common.item.recipe.crafting.SpongeShapedCraftingRecipeBuilder;
import org.spongepowered.common.item.recipe.crafting.SpongeShapelessCraftingRecipeBuilder;
import org.spongepowered.common.item.recipe.smelting.SpongeSmeltingRecipeBuilder;
import org.spongepowered.common.registry.factory.ResourcePackFactoryModule;
import org.spongepowered.common.registry.factory.TimingsFactoryModule;
import org.spongepowered.common.registry.type.BanTypeRegistryModule;
import org.spongepowered.common.registry.type.BannerPatternShapeRegistryModule;
import org.spongepowered.common.registry.type.BlockStateRegistryModule;
import org.spongepowered.common.registry.type.BlockTypeRegistryModule;
import org.spongepowered.common.registry.type.DyeColorRegistryModule;
import org.spongepowered.common.registry.type.ItemTypeRegistryModule;
import org.spongepowered.common.registry.type.NotePitchRegistryModule;
import org.spongepowered.common.registry.type.advancement.AdvancementRegistryModule;
import org.spongepowered.common.registry.type.advancement.AdvancementTreeRegistryModule;
import org.spongepowered.common.registry.type.advancement.AdvancementTypeRegistryModule;
import org.spongepowered.common.registry.type.advancement.CriterionRegistryModule;
import org.spongepowered.common.registry.type.advancement.TriggerTypeRegistryModule;
import org.spongepowered.common.registry.type.block.BigMushroomRegistryModule;
import org.spongepowered.common.registry.type.block.BooleanTraitRegistryModule;
import org.spongepowered.common.registry.type.block.BrickTypeRegistryModule;
import org.spongepowered.common.registry.type.block.ComparatorTypeRegistryModule;
import org.spongepowered.common.registry.type.block.DirtTypeRegistryModule;
import org.spongepowered.common.registry.type.block.DisguisedBlockTypeRegistryModule;
import org.spongepowered.common.registry.type.block.DoublePlantTypeRegistryModule;
import org.spongepowered.common.registry.type.block.EnumTraitRegistryModule;
import org.spongepowered.common.registry.type.block.HingeRegistryModule;
import org.spongepowered.common.registry.type.block.IntegerTraitRegistryModule;
import org.spongepowered.common.registry.type.block.LogAxisRegistryModule;
import org.spongepowered.common.registry.type.block.MushroomTypeRegistryModule;
import org.spongepowered.common.registry.type.block.PistonTypeRegistryModule;
import org.spongepowered.common.registry.type.block.PlantTypeModuleRegistry;
import org.spongepowered.common.registry.type.block.PortionTypeRegistryModule;
import org.spongepowered.common.registry.type.block.PrismarineRegistryModule;
import org.spongepowered.common.registry.type.block.QuartzTypeRegistryModule;
import org.spongepowered.common.registry.type.block.RailDirectionRegistryModule;
import org.spongepowered.common.registry.type.block.RotationRegistryModule;
import org.spongepowered.common.registry.type.block.SandTypeRegistryModule;
import org.spongepowered.common.registry.type.block.SandstoneTypeRegistryModule;
import org.spongepowered.common.registry.type.block.ShrubTypeRegistryModule;
import org.spongepowered.common.registry.type.block.SkullTypeRegistryModule;
import org.spongepowered.common.registry.type.block.SlabTypeRegistryModule;
import org.spongepowered.common.registry.type.block.StairShapeRegistryModule;
import org.spongepowered.common.registry.type.block.StoneTypeRegistryModule;
import org.spongepowered.common.registry.type.block.TileEntityTypeRegistryModule;
import org.spongepowered.common.registry.type.block.TreeTypeRegistryModule;
import org.spongepowered.common.registry.type.block.WallTypeRegistryModule;
import org.spongepowered.common.registry.type.boss.BossBarColorRegistryModule;
import org.spongepowered.common.registry.type.boss.BossBarOverlayRegistryModule;
import org.spongepowered.common.registry.type.command.CatalogedSelectorParserRegistryModule;
import org.spongepowered.common.registry.type.command.CatalogedValueParameterModifiersRegistryModule;
import org.spongepowered.common.registry.type.command.CatalogedValueParametersRegistryModule;
import org.spongepowered.common.registry.type.command.ChildExceptionBehaviorRegistryModule;
import org.spongepowered.common.registry.type.command.CommandMessageFormatRegistryModule;
import org.spongepowered.common.registry.type.command.InputTokenizerRegistryModule;
import org.spongepowered.common.registry.type.command.UnknownFlagBehaviorRegistryModule;
import org.spongepowered.common.registry.type.data.DataFormatRegistryModule;
import org.spongepowered.common.registry.type.data.DataTranslatorRegistryModule;
import org.spongepowered.common.registry.type.data.HandTypeRegistryModule;
import org.spongepowered.common.registry.type.data.InstrumentTypeRegistryModule;
import org.spongepowered.common.registry.type.data.KeyRegistryModule;
import org.spongepowered.common.registry.type.economy.TransactionTypeRegistryModule;
import org.spongepowered.common.registry.type.effect.ParticleOptionRegistryModule;
import org.spongepowered.common.registry.type.effect.ParticleRegistryModule;
import org.spongepowered.common.registry.type.effect.PotionEffectTypeRegistryModule;
import org.spongepowered.common.registry.type.effect.RecordTypeRegistryModule;
import org.spongepowered.common.registry.type.effect.SoundCategoryRegistryModule;
import org.spongepowered.common.registry.type.effect.SoundRegistryModule;
import org.spongepowered.common.registry.type.entity.AITaskTypeModule;
import org.spongepowered.common.registry.type.entity.ArtRegistryModule;
import org.spongepowered.common.registry.type.entity.BodyPartRegistryModule;
import org.spongepowered.common.registry.type.entity.CareerRegistryModule;
import org.spongepowered.common.registry.type.entity.EntityTypeRegistryModule;
import org.spongepowered.common.registry.type.entity.GameModeRegistryModule;
import org.spongepowered.common.registry.type.entity.GoalTypeModule;
import org.spongepowered.common.registry.type.entity.HorseColorRegistryModule;
import org.spongepowered.common.registry.type.entity.HorseStyleRegistryModule;
import org.spongepowered.common.registry.type.entity.OcelotTypeRegistryModule;
import org.spongepowered.common.registry.type.entity.ParrotVariantRegistryModule;
import org.spongepowered.common.registry.type.entity.PickupRuleRegistryModule;
import org.spongepowered.common.registry.type.entity.ProfessionRegistryModule;
import org.spongepowered.common.registry.type.entity.RabbitTypeRegistryModule;
import org.spongepowered.common.registry.type.entity.SkinPartRegistryModule;
import org.spongepowered.common.registry.type.event.DamageModifierTypeRegistryModule;
import org.spongepowered.common.registry.type.event.DamageSourceRegistryModule;
import org.spongepowered.common.registry.type.event.DamageTypeRegistryModule;
import org.spongepowered.common.registry.type.event.DismountTypeRegistryModule;
import org.spongepowered.common.registry.type.event.EventContextKeysModule;
import org.spongepowered.common.registry.type.event.SpawnTypeRegistryModule;
import org.spongepowered.common.registry.type.event.TeleportTypeRegistryModule;
import org.spongepowered.common.registry.type.extra.FluidTypeRegistryModule;
import org.spongepowered.common.registry.type.item.ArmorTypeRegistryModule;
import org.spongepowered.common.registry.type.item.CoalTypeRegistryModule;
import org.spongepowered.common.registry.type.item.CookedFishRegistryModule;
import org.spongepowered.common.registry.type.item.EnchantmentRegistryModule;
import org.spongepowered.common.registry.type.item.EquipmentTypeRegistryModule;
import org.spongepowered.common.registry.type.item.FireworkShapeRegistryModule;
import org.spongepowered.common.registry.type.item.FishRegistryModule;
import org.spongepowered.common.registry.type.item.GoldenAppleRegistryModule;
import org.spongepowered.common.registry.type.item.GuiIdRegistryModule;
import org.spongepowered.common.registry.type.item.InventoryArchetypeRegistryModule;
import org.spongepowered.common.registry.type.item.QueryOperationRegistryModule;
import org.spongepowered.common.registry.type.item.ToolTypeRegistryModule;
import org.spongepowered.common.registry.type.scoreboard.CollisionRuleRegistryModule;
import org.spongepowered.common.registry.type.scoreboard.CriteriaRegistryModule;
import org.spongepowered.common.registry.type.scoreboard.DisplaySlotRegistryModule;
import org.spongepowered.common.registry.type.scoreboard.ObjectiveDisplayModeRegistryModule;
import org.spongepowered.common.registry.type.scoreboard.VisibilityRegistryModule;
import org.spongepowered.common.registry.type.statistic.StatisticRegistryModule;
import org.spongepowered.common.registry.type.statistic.StatisticTypeRegistryModule;
import org.spongepowered.common.registry.type.text.ArgumentRegistryModule;
import org.spongepowered.common.registry.type.text.ChatTypeRegistryModule;
import org.spongepowered.common.registry.type.text.ChatVisibilityRegistryModule;
import org.spongepowered.common.registry.type.text.LocaleRegistryModule;
import org.spongepowered.common.registry.type.text.SelectorTypeRegistryModule;
import org.spongepowered.common.registry.type.text.TextColorRegistryModule;
import org.spongepowered.common.registry.type.text.TextSerializerRegistryModule;
import org.spongepowered.common.registry.type.text.TextStyleRegistryModule;
import org.spongepowered.common.registry.type.tileentity.StructureModeRegistryModule;
import org.spongepowered.common.registry.type.world.BlockChangeFlagRegistryModule;
import org.spongepowered.common.registry.type.world.DifficultyRegistryModule;
import org.spongepowered.common.registry.type.world.DimensionTypeRegistryModule;
import org.spongepowered.common.registry.type.world.GeneratorTypeRegistryModule;
import org.spongepowered.common.registry.type.world.PaletteTypeRegistryModule;
import org.spongepowered.common.registry.type.world.PortalAgentRegistryModule;
import org.spongepowered.common.registry.type.world.SerializationBehaviorRegistryModule;
import org.spongepowered.common.registry.type.world.TeleportHelperFilterRegistryModule;
import org.spongepowered.common.registry.type.world.WeatherRegistryModule;
import org.spongepowered.common.registry.type.world.WorldArchetypeRegistryModule;
import org.spongepowered.common.registry.type.world.WorldGeneratorModifierRegistryModule;
import org.spongepowered.common.registry.type.world.gen.BiomeTreeTypeRegistryModule;
import org.spongepowered.common.registry.type.world.gen.BiomeTypeRegistryModule;
import org.spongepowered.common.registry.type.world.gen.DungeonMobRegistryModule;
import org.spongepowered.common.registry.type.world.gen.PopulatorObjectRegistryModule;
import org.spongepowered.common.registry.type.world.gen.PopulatorTypeRegistryModule;
import org.spongepowered.common.scoreboard.builder.SpongeObjectiveBuilder;
import org.spongepowered.common.scoreboard.builder.SpongeScoreboardBuilder;
import org.spongepowered.common.scoreboard.builder.SpongeTeamBuilder;
import org.spongepowered.common.world.SpongeExplosionBuilder;
import org.spongepowered.common.world.SpongeLocatableBlockBuilder;
import org.spongepowered.common.world.SpongeWorldArchetypeBuilder;
import org.spongepowered.common.world.biome.SpongeBiomeGenerationSettingsBuilder;
import org.spongepowered.common.world.biome.SpongeVirtualBiomeTypeBuilder;
import org.spongepowered.common.world.border.SpongeWorldBorderBuilder;
import org.spongepowered.common.world.gen.builders.BigMushroomBuilder;
import org.spongepowered.common.world.gen.builders.BlockBlobBuilder;
import org.spongepowered.common.world.gen.builders.CactusBuilder;
import org.spongepowered.common.world.gen.builders.ChorusFlowerBuilder;
import org.spongepowered.common.world.gen.builders.DeadBushBuilder;
import org.spongepowered.common.world.gen.builders.DesertWellBuilder;
import org.spongepowered.common.world.gen.builders.DoublePlantBuilder;
import org.spongepowered.common.world.gen.builders.DungeonBuilder;
import org.spongepowered.common.world.gen.builders.EndIslandBuilder;
import org.spongepowered.common.world.gen.builders.FlowerBuilder;
import org.spongepowered.common.world.gen.builders.ForestBuilder;
import org.spongepowered.common.world.gen.builders.FossilBuilder;
import org.spongepowered.common.world.gen.builders.GlowstoneBuilder;
import org.spongepowered.common.world.gen.builders.IcePathBuilder;
import org.spongepowered.common.world.gen.builders.IceSpikeBuilder;
import org.spongepowered.common.world.gen.builders.LakeBuilder;
import org.spongepowered.common.world.gen.builders.MelonBuilder;
import org.spongepowered.common.world.gen.builders.MushroomBuilder;
import org.spongepowered.common.world.gen.builders.NetherFireBuilder;
import org.spongepowered.common.world.gen.builders.OreBuilder;
import org.spongepowered.common.world.gen.builders.PumpkinBuilder;
import org.spongepowered.common.world.gen.builders.RandomBlockBuilder;
import org.spongepowered.common.world.gen.builders.RandomObjectBuilder;
import org.spongepowered.common.world.gen.builders.ReedBuilder;
import org.spongepowered.common.world.gen.builders.SeaFloorBuilder;
import org.spongepowered.common.world.gen.builders.ShrubBuilder;
import org.spongepowered.common.world.gen.builders.VineBuilder;
import org.spongepowered.common.world.gen.builders.WaterLilyBuilder;
import org.spongepowered.common.world.schematic.SpongeSchematicBuilder;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public final class CommonModuleRegistry {

    public static CommonModuleRegistry getInstance() {
        return Holder.INSTANCE;
    }

    public void registerDefaultModules() {
        SpongeGameRegistry registry = SpongeImpl.getRegistry();
        registerFactories();
        registerDefaultSuppliers(registry);
        registerCommonModules(registry);
    }

    private void registerFactories() {
        final List<FactoryRegistry<?, ?>> factoryRegistries = new ArrayList<>();
        factoryRegistries.add(new ResourcePackFactoryModule());
        factoryRegistries.add(new TimingsFactoryModule());

        try {
            Field modifierField = Field.class.getDeclaredField("modifiers");
            modifierField.setAccessible(true);
            for (FactoryRegistry<?, ?> registry : factoryRegistries) {
                RegistryHelper.setFactory(registry.getFactoryOwner(), registry.provideFactory());
                registry.initialize();
            }
        } catch (Exception e) {
            SpongeImpl.getLogger().error("Could not initialize a factory!", e);
        }
    }

    private void registerDefaultSuppliers(SpongeGameRegistry registry) {
        registry.registerBuilderSupplier(ItemStack.Builder.class, SpongeItemStackBuilder::new)
            .registerBuilderSupplier(TradeOffer.Builder.class, SpongeTradeOfferBuilder::new)
            .registerBuilderSupplier(FireworkEffect.Builder.class, SpongeFireworkEffectBuilder::new)
            .registerBuilderSupplier(PotionEffect.Builder.class, SpongePotionBuilder::new)
            .registerBuilderSupplier(Objective.Builder.class, SpongeObjectiveBuilder::new)
            .registerBuilderSupplier(Team.Builder.class, SpongeTeamBuilder::new)
            .registerBuilderSupplier(Scoreboard.Builder.class, SpongeScoreboardBuilder::new)
            .registerBuilderSupplier(DamageSource.Builder.class, SpongeDamageSourceBuilder::new)
            .registerBuilderSupplier(EntityDamageSource.Builder.class, SpongeEntityDamageSourceBuilder::new)
            .registerBuilderSupplier(IndirectEntityDamageSource.Builder.class, SpongeIndirectEntityDamageSourceBuilder::new)
            .registerBuilderSupplier(FallingBlockDamageSource.Builder.class, SpongeFallingBlockDamgeSourceBuilder::new)
            .registerBuilderSupplier(BlockDamageSource.Builder.class, SpongeBlockDamageSourceBuilder::new)
            .registerBuilderSupplier(WorldArchetype.Builder.class, SpongeWorldArchetypeBuilder::new)
            .registerBuilderSupplier(Explosion.Builder.class, SpongeExplosionBuilder::new)
            .registerBuilderSupplier(BlockState.Builder.class, SpongeBlockStateBuilder::new)
            .registerBuilderSupplier(BlockSnapshot.Builder.class, SpongeBlockSnapshotBuilder::new)
            .registerBuilderSupplier(EntitySnapshot.Builder.class, SpongeEntitySnapshotBuilder::new)
            .registerBuilderSupplier(ParticleEffect.Builder.class, SpongeParticleEffectBuilder::new)
            .registerBuilderSupplier(WanderAITask.Builder.class, SpongeWanderAIBuilder::new)
            .registerBuilderSupplier(AvoidEntityAITask.Builder.class, SpongeAvoidEntityAIBuilder::new)
            .registerBuilderSupplier(RunAroundLikeCrazyAITask.Builder.class, SpongeRunAroundLikeCrazyAIBuilder::new)
            .registerBuilderSupplier(SwimmingAITask.Builder.class, SpongeSwimmingAIBuilder::new)
            .registerBuilderSupplier(WatchClosestAITask.Builder.class, SpongeWatchClosestAIBuilder::new)
            .registerBuilderSupplier(FindNearestAttackableTargetAITask.Builder.class, SpongeFindNearestAttackableTargetAIBuilder::new)
            .registerBuilderSupplier(AttackLivingAITask.Builder.class, SpongeAttackLivingAIBuilder::new)
            .registerBuilderSupplier(RangeAgentAITask.Builder.class, SpongeRangeAgentAIBuilder::new)
            .registerBuilderSupplier(LookIdleAITask.Builder.class, SpongeLookIdleAIBuilder::new)
            .registerBuilderSupplier(PatternLayer.Builder.class, SpongePatternLayerBuilder::new)
            .registerBuilderSupplier(Task.Builder.class, () -> Sponge.getScheduler().createTaskBuilder())
            .registerBuilderSupplier(BigMushroom.Builder.class, BigMushroomBuilder::new)
            .registerBuilderSupplier(BlockBlob.Builder.class, BlockBlobBuilder::new)
            .registerBuilderSupplier(ChorusFlower.Builder.class, ChorusFlowerBuilder::new)
            .registerBuilderSupplier(Cactus.Builder.class, CactusBuilder::new)
            .registerBuilderSupplier(DeadBush.Builder.class, DeadBushBuilder::new)
            .registerBuilderSupplier(DesertWell.Builder.class, DesertWellBuilder::new)
            .registerBuilderSupplier(DoublePlant.Builder.class, DoublePlantBuilder::new)
            .registerBuilderSupplier(Dungeon.Builder.class, DungeonBuilder::new)
            .registerBuilderSupplier(EndIsland.Builder.class, EndIslandBuilder::new)
            .registerBuilderSupplier(Flower.Builder.class, FlowerBuilder::new)
            .registerBuilderSupplier(Forest.Builder.class, ForestBuilder::new)
            .registerBuilderSupplier(Glowstone.Builder.class, GlowstoneBuilder::new)
            .registerBuilderSupplier(IcePath.Builder.class, IcePathBuilder::new)
            .registerBuilderSupplier(IceSpike.Builder.class, IceSpikeBuilder::new)
            .registerBuilderSupplier(Lake.Builder.class, LakeBuilder::new)
            .registerBuilderSupplier(Melon.Builder.class, MelonBuilder::new)
            .registerBuilderSupplier(Mushroom.Builder.class, MushroomBuilder::new)
            .registerBuilderSupplier(NetherFire.Builder.class, NetherFireBuilder::new)
            .registerBuilderSupplier(Ore.Builder.class, OreBuilder::new)
            .registerBuilderSupplier(Pumpkin.Builder.class, PumpkinBuilder::new)
            .registerBuilderSupplier(RandomBlock.Builder.class, RandomBlockBuilder::new)
            .registerBuilderSupplier(RandomObject.Builder.class, RandomObjectBuilder::new)
            .registerBuilderSupplier(Reed.Builder.class, ReedBuilder::new)
            .registerBuilderSupplier(SeaFloor.Builder.class, SeaFloorBuilder::new)
            .registerBuilderSupplier(Shrub.Builder.class, ShrubBuilder::new)
            .registerBuilderSupplier(Vine.Builder.class, VineBuilder::new)
            .registerBuilderSupplier(WaterLily.Builder.class, WaterLilyBuilder::new)
            .registerBuilderSupplier(Ban.Builder.class, SpongeBanBuilder::new)
            .registerBuilderSupplier(FluidStack.Builder.class, SpongeFluidStackBuilder::new)
            .registerBuilderSupplier(FluidStackSnapshot.Builder.class, SpongeFluidStackSnapshotBuilder::new)
            .registerBuilderSupplier(TabListEntry.Builder.class, TabListEntryBuilder::new)
            .registerBuilderSupplier(TradeOfferGenerator.Builder.class, SpongeTradeOfferGenerator.Builder::new)
            .registerBuilderSupplier(ItemStackGenerator.Builder.class, SpongeItemStackGenerator.Builder::new)
            .registerBuilderSupplier(ServerBossBar.Builder.class, ServerBossBarBuilder::new)
            .registerBuilderSupplier(EntityArchetype.Builder.class, SpongeEntityArchetypeBuilder::new)
            .registerBuilderSupplier(TileEntityArchetype.Builder.class, SpongeTileEntityArchetypeBuilder::new)
            .registerBuilderSupplier(Schematic.Builder.class, SpongeSchematicBuilder::new)
            .registerBuilderSupplier(VirtualBiomeType.Builder.class, SpongeVirtualBiomeTypeBuilder::new)
            .registerBuilderSupplier(BiomeGenerationSettings.Builder.class, SpongeBiomeGenerationSettingsBuilder::new)
            .registerBuilderSupplier(InventoryArchetype.Builder.class, SpongeInventoryArchetypeBuilder::new)
            .registerBuilderSupplier(Inventory.Builder.class, SpongeInventoryBuilder::new)
            .registerBuilderSupplier(SoundType.Builder.class, SpongeSoundBuilder::new)
            .registerBuilderSupplier(LocatableBlock.Builder.class, SpongeLocatableBlockBuilder::new)
            .registerBuilderSupplier(Fossil.Builder.class, FossilBuilder::new)
            .registerBuilderSupplier(DataRegistration.Builder.class, SpongeDataRegistrationBuilder::new)
            .registerBuilderSupplier(WorldBorder.Builder.class, SpongeWorldBorderBuilder::new)
            .registerBuilderSupplier(Ingredient.Builder.class, SpongeIngredientBuilder::new)
            .registerBuilderSupplier(ShapedCraftingRecipe.Builder.class, SpongeShapedCraftingRecipeBuilder::new)
            .registerBuilderSupplier(ShapelessCraftingRecipe.Builder.class, SpongeShapelessCraftingRecipeBuilder::new)
            .registerBuilderSupplier(SmeltingRecipe.Builder.class, SpongeSmeltingRecipeBuilder::new)
            .registerBuilderSupplier(EventContextKey.Builder.class, SpongeEventContextKeyBuilder::new)
            .registerBuilderSupplier(Enchantment.Builder.class, SpongeEnchantmentBuilder::new)
            .registerBuilderSupplier(Key.Builder.class, SpongeKeyBuilder::new)
            .registerBuilderSupplier(Advancement.Builder.class, SpongeAdvancementBuilder::new)
            .registerBuilderSupplier(AdvancementTree.Builder.class, SpongeAdvancementTreeBuilder::new)
            .registerBuilderSupplier(DisplayInfo.Builder.class, SpongeDisplayInfoBuilder::new)
            .registerBuilderSupplier(AdvancementCriterion.Builder.class, SpongeCriterionBuilder::new)
            .registerBuilderSupplier(ScoreAdvancementCriterion.Builder.class, SpongeScoreCriterionBuilder::new)
            .registerBuilderSupplier(FilteredTrigger.Builder.class, SpongeFilteredTriggerBuilder::new)
            .registerBuilderSupplier(Trigger.Builder.class, SpongeTriggerBuilder::new)
            .registerBuilderSupplier(Parameter.Builder.class, SpongeParameterBuilder::new)
            .registerBuilderSupplier(Parameter.SequenceBuilder.class, SpongeParameterSequenceBuilder::new)
            .registerBuilderSupplier(Parameter.FirstOfBuilder.class, SpongeParameterFirstOfBuilder::new)
            .registerBuilderSupplier(Flags.Builder.class, SpongeFlagsBuilder::new)
            .registerBuilderSupplier(CommandResult.Builder.class, SpongeCommandResultBuilder::new)
            .registerBuilderSupplier(Command.Builder.class, SpongeCommandBuilder::new)
            .registerBuilderSupplier(VariableValueParameterModifiers.RepeatedValueModifierBuilder.class, SpongeRepeatedValueModifierBuilder::new)
            .registerBuilderSupplier(VariableValueParameterModifiers.DefaultValueModifierBuilder.class, SpongeDefaultValueModifierBuilder::new)
            .registerBuilderSupplier(VariableValueParameters.DynamicChoicesBuilder.class, SpongeDynamicChoicesValueParameterBuilder::new)
            .registerBuilderSupplier(VariableValueParameters.StaticChoicesBuilder.class, SpongeStaticChoicesValueParameterBuilder::new)
            .registerBuilderSupplier(VariableValueParameters.LiteralBuilder.class, SpongeLiteralValueParameterBuilder::new)
            .registerBuilderSupplier(VariableValueParameters.CatalogedTypeBuilder.class, SpongeCatalogTypeValueParameterBuilder::new)
            .registerBuilderSupplier(VariableValueParameters.EnumBuilder.class, SpongeEnumValueParameterBuilder::new)
            .registerBuilderSupplier(VariableValueParameters.TextBuilder.class, SpongeTextValueParameterBuilder::new)
        ;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void registerCommonModules(SpongeGameRegistry registry) {
        registry.registerModule(new ArgumentRegistryModule())
            .registerModule(BlockChangeFlagRegistryModule.getInstance())
            .registerModule(AITaskType.class, AITaskTypeModule.getInstance())
            .registerModule(ArmorType.class, new ArmorTypeRegistryModule())
            .registerModule(Art.class, new ArtRegistryModule())
            .registerModule(BanType.class, new BanTypeRegistryModule())
            .registerModule(BannerPatternShape.class, new BannerPatternShapeRegistryModule())
            .registerModule(BodyPart.class, new BodyPartRegistryModule())
            .registerModule(BooleanTrait.class, BooleanTraitRegistryModule.getInstance())
            .registerModule(BigMushroomType.class, new BigMushroomRegistryModule())
            .registerModule(BiomeTreeType.class, new BiomeTreeTypeRegistryModule())
            .registerModule(BiomeType.class, new BiomeTypeRegistryModule())
            .registerModule(BlockState.class, BlockStateRegistryModule.getInstance())
            .registerModule(BlockType.class, BlockTypeRegistryModule.getInstance())
            .registerModule(BrickType.class, new BrickTypeRegistryModule())
            .registerModule(Career.class, CareerRegistryModule.getInstance())
            .registerModule(new ChatTypeRegistryModule())
            .registerModule(CoalType.class, new CoalTypeRegistryModule())
            .registerModule(ComparatorType.class, new ComparatorTypeRegistryModule())
            .registerModule(CookedFish.class, new CookedFishRegistryModule())
            .registerModule(Criterion.class, new CriteriaRegistryModule())
            .registerModule(DamageModifierType.class, new DamageModifierTypeRegistryModule())
            .registerModule(new DamageSourceRegistryModule())
            .registerModule(DamageType.class, new DamageTypeRegistryModule())
            .registerModule(DataFormat.class, new DataFormatRegistryModule())
            .registerModule(DataTranslator.class, DataTranslatorRegistryModule.getInstance())
            .registerModule(Difficulty.class, new DifficultyRegistryModule())
            .registerModule(DimensionType.class, DimensionTypeRegistryModule.getInstance())
            .registerModule(DirtType.class, new DirtTypeRegistryModule())
            .registerModule(DisguisedBlockType.class, new DisguisedBlockTypeRegistryModule())
            .registerModule(DisplaySlot.class, DisplaySlotRegistryModule.getInstance())
            .registerModule(DoublePlantType.class, new DoublePlantTypeRegistryModule())
            .registerModule(DyeColor.class, DyeColorRegistryModule.getInstance())
            .registerModule(EnchantmentType.class, EnchantmentRegistryModule.getInstance())
            .registerModule((Class<EnumTrait<?>>) (Class) EnumTrait.class, EnumTraitRegistryModule.getInstance())
            .registerModule(EntityType.class, EntityTypeRegistryModule.getInstance())
            .registerModule(EquipmentType.class, new EquipmentTypeRegistryModule())
            .registerModule(FireworkShape.class, new FireworkShapeRegistryModule())
            .registerModule(Fish.class, new FishRegistryModule())
            .registerModule(FluidType.class, FluidTypeRegistryModule.getInstance())
            .registerModule(GameMode.class, new GameModeRegistryModule())
            .registerModule(GeneratorType.class, GeneratorTypeRegistryModule.getInstance())
            .registerModule(GoalType.class, GoalTypeModule.getInstance())
            .registerModule(GoldenApple.class, new GoldenAppleRegistryModule())
            .registerModule(Hinge.class, new HingeRegistryModule())
            .registerModule(IntegerTrait.class, IntegerTraitRegistryModule.getInstance())
            .registerModule(ItemType.class, ItemTypeRegistryModule.getInstance())
            .registerModule(new LocaleRegistryModule())
            .registerModule(LogAxis.class, new LogAxisRegistryModule())
            .registerModule(MushroomType.class, new MushroomTypeRegistryModule())
            .registerModule(NotePitch.class, new NotePitchRegistryModule())
            .registerModule(ObjectiveDisplayMode.class, new ObjectiveDisplayModeRegistryModule())
            .registerModule(OcelotType.class, new OcelotTypeRegistryModule())
            .registerModule(ParrotVariant.class, new ParrotVariantRegistryModule())
            .registerModule(ParticleType.class, ParticleRegistryModule.getInstance())
            .registerModule((Class<ParticleOption<?>>) (Class<?>) ParticleOption.class, new ParticleOptionRegistryModule())
            .registerModule(PistonType.class, new PistonTypeRegistryModule())
            .registerModule(PlantType.class, new PlantTypeModuleRegistry())
            .registerModule(PopulatorObject.class, new PopulatorObjectRegistryModule())
            .registerModule(PopulatorType.class, PopulatorTypeRegistryModule.getInstance())
            .registerModule(PortionType.class, new PortionTypeRegistryModule())
            .registerModule(PotionEffectType.class, PotionEffectTypeRegistryModule.getInstance())
            .registerModule(PrismarineType.class, new PrismarineRegistryModule())
            .registerModule(Profession.class, ProfessionRegistryModule.getInstance())
            .registerModule(QuartzType.class, new QuartzTypeRegistryModule())
            .registerModule(QueryOperationType.class, new QueryOperationRegistryModule())
            .registerModule(RabbitType.class, new RabbitTypeRegistryModule())
            .registerModule(RailDirection.class, new RailDirectionRegistryModule())
            .registerModule(Rotation.class, RotationRegistryModule.getInstance())
            .registerModule(SandstoneType.class, new SandstoneTypeRegistryModule())
            .registerModule(SandType.class, new SandTypeRegistryModule())
            .registerModule(SelectorType.class, new SelectorTypeRegistryModule())
            .registerModule(SerializationBehavior.class, new SerializationBehaviorRegistryModule())
            .registerModule(ShrubType.class, new ShrubTypeRegistryModule())
            .registerModule(SkullType.class, new SkullTypeRegistryModule())
            .registerModule(SlabType.class, new SlabTypeRegistryModule())
            .registerModule(SoundType.class, new SoundRegistryModule())
            .registerModule(SpawnType.class, new SpawnTypeRegistryModule())
            .registerModule(SoundCategory.class, new SoundCategoryRegistryModule())
            .registerModule(StairShape.class, new StairShapeRegistryModule())
            .registerModule(StoneType.class, new StoneTypeRegistryModule())
            .registerModule(TeleportHelperFilter.class, new TeleportHelperFilterRegistryModule())
            .registerModule(TeleportType.class, TeleportTypeRegistryModule.getInstance())
            .registerModule(TextColor.class, new TextColorRegistryModule())
            .registerModule(TextSerializer.class, new TextSerializerRegistryModule())
            .registerModule(TextStyle.Base.class, new TextStyleRegistryModule())
            .registerModule(TileEntityType.class, TileEntityTypeRegistryModule.getInstance())
            .registerModule(ToolType.class, new ToolTypeRegistryModule())
            .registerModule(TreeType.class, new TreeTypeRegistryModule())
            .registerModule(Visibility.class, new VisibilityRegistryModule())
            .registerModule(Statistic.class, StatisticRegistryModule.getInstance())
            .registerModule(StatisticType.class, new StatisticTypeRegistryModule())
            .registerModule(WallType.class, new WallTypeRegistryModule())
            .registerModule(Weather.class, new WeatherRegistryModule())
            .registerModule(WorldGeneratorModifier.class, WorldGeneratorModifierRegistryModule.getInstance())
            .registerModule(TransactionType.class, new TransactionTypeRegistryModule())
            .registerModule(ChatVisibility.class, new ChatVisibilityRegistryModule())
            .registerModule(SkinPart.class, new SkinPartRegistryModule())
            .registerModule(WorldArchetype.class, WorldArchetypeRegistryModule.getInstance())
            .registerModule(BossBarColor.class, new BossBarColorRegistryModule())
            .registerModule(BossBarOverlay.class, new BossBarOverlayRegistryModule())
            .registerModule(PortalAgentType.class, PortalAgentRegistryModule.getInstance())
            .registerModule(HandType.class, HandTypeRegistryModule.getInstance())
            .registerModule(PickupRule.class, new PickupRuleRegistryModule())
            .registerModule(BlockPaletteType.class, new PaletteTypeRegistryModule())
            .registerModule(CollisionRule.class, new CollisionRuleRegistryModule())
            .registerModule(DismountType.class, new DismountTypeRegistryModule())
            .registerModule((Class<Key<?>>) (Class<?>) Key.class, KeyRegistryModule.getInstance())
            .registerModule(GuiId.class, GuiIdRegistryModule.getInstance())
            .registerModule(InventoryArchetype.class, InventoryArchetypeRegistryModule.getInstance())
            .registerModule(StructureMode.class, new StructureModeRegistryModule())
            .registerModule(CraftingRecipe.class, SpongeCraftingRecipeRegistry.getInstance())
            .registerModule(EventContextKey.class, EventContextKeysModule.getInstance())
            .registerModule(RecordType.class, RecordTypeRegistryModule.getInstance())
            .registerModule(HorseStyle.class, HorseStyleRegistryModule.getInstance())
            .registerModule(HorseColor.class, HorseColorRegistryModule.getInstance())
            .registerModule(InstrumentType.class, InstrumentTypeRegistryModule.getInstance())
            .registerModule(Advancement.class, AdvancementRegistryModule.getInstance())
            .registerModule(AdvancementTree.class, AdvancementTreeRegistryModule.getInstance())
            .registerModule(AdvancementType.class, new AdvancementTypeRegistryModule())
            .registerModule(Trigger.class, TriggerTypeRegistryModule.getInstance())
            .registerModule(new CriterionRegistryModule())
            .registerModule(((Class<DataRegistration<?, ?>>) (Class<?>) DataRegistration.class), SpongeManipulatorRegistry.getInstance())

            // Command Modules
            .registerModule(ChildExceptionBehavior.class, new ChildExceptionBehaviorRegistryModule())
            .registerModule(InputTokenizer.class, new InputTokenizerRegistryModule())

            // Must be registered before parameters
            .registerModule(CatalogedSelectorParser.class, new CatalogedSelectorParserRegistryModule())

            .registerModule(CatalogedValueParameterModifier.class, new CatalogedValueParameterModifiersRegistryModule())
            .registerModule(CatalogedValueParameter.class, new CatalogedValueParametersRegistryModule())

            // Must be registered after Text related classes.
            .registerModule(CommandMessageFormat.class, new CommandMessageFormatRegistryModule())
            .registerModule(UnknownFlagBehavior.class, new UnknownFlagBehaviorRegistryModule())

            // Miscellaneous Registries
            .registerModule(DungeonMobRegistryModule.getInstance())
            ;
    }

    CommonModuleRegistry() { }

    private static final class Holder {

        static final CommonModuleRegistry INSTANCE = new CommonModuleRegistry();
    }

}
