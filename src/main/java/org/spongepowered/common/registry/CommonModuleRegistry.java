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
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.meta.PatternLayer;
import org.spongepowered.api.data.persistence.DataFormat;
import org.spongepowered.api.data.persistence.DataTranslator;
import org.spongepowered.api.data.type.*;
import org.spongepowered.api.effect.particle.*;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.effect.potion.PotionEffectType;
import org.spongepowered.api.effect.sound.SoundCategory;
import org.spongepowered.api.effect.sound.SoundType;
import org.spongepowered.api.entity.EntityArchetype;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.ai.GoalType;
import org.spongepowered.api.entity.ai.task.AITaskType;
import org.spongepowered.api.entity.ai.task.builtin.SwimmingAITask;
import org.spongepowered.api.entity.ai.task.builtin.creature.AttackLivingAITask;
import org.spongepowered.api.entity.ai.task.builtin.creature.AvoidEntityAITask;
import org.spongepowered.api.entity.ai.task.builtin.LookIdleAITask;
import org.spongepowered.api.entity.ai.task.builtin.creature.RangeAgentAITask;
import org.spongepowered.api.entity.ai.task.builtin.creature.WanderAITask;
import org.spongepowered.api.entity.ai.task.builtin.WatchClosestAITask;
import org.spongepowered.api.entity.ai.task.builtin.creature.horse.RunAroundLikeCrazyAITask;
import org.spongepowered.api.entity.ai.task.builtin.creature.target.FindNearestAttackableTargetAITask;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.entity.living.player.tab.TabListEntry;
import org.spongepowered.api.event.cause.entity.damage.DamageModifierType;
import org.spongepowered.api.event.cause.entity.damage.DamageType;
import org.spongepowered.api.event.cause.entity.damage.source.*;
import org.spongepowered.api.event.cause.entity.spawn.*;
import org.spongepowered.api.event.cause.entity.teleport.EntityTeleportCause;
import org.spongepowered.api.event.cause.entity.teleport.PortalTeleportCause;
import org.spongepowered.api.event.cause.entity.teleport.TeleportCause;
import org.spongepowered.api.event.cause.entity.teleport.TeleportType;
import org.spongepowered.api.extra.fluid.FluidStack;
import org.spongepowered.api.extra.fluid.FluidStackSnapshot;
import org.spongepowered.api.extra.fluid.FluidType;
import org.spongepowered.api.item.Enchantment;
import org.spongepowered.api.item.FireworkEffect;
import org.spongepowered.api.item.FireworkShape;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.InventoryArchetype;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackGenerator;
import org.spongepowered.api.item.inventory.equipment.EquipmentType;
import org.spongepowered.api.item.merchant.TradeOffer;
import org.spongepowered.api.item.merchant.TradeOfferGenerator;
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
import org.spongepowered.api.text.chat.ChatVisibility;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextStyle;
import org.spongepowered.api.text.selector.SelectorType;
import org.spongepowered.api.util.ban.Ban;
import org.spongepowered.api.util.ban.BanType;
import org.spongepowered.api.util.rotation.Rotation;
import org.spongepowered.api.world.DimensionType;
import org.spongepowered.api.world.GeneratorType;
import org.spongepowered.api.world.SerializationBehavior;
import org.spongepowered.api.world.WorldArchetype;
import org.spongepowered.api.world.PortalAgentType;
import org.spongepowered.api.world.biome.BiomeGenerationSettings;
import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.api.world.biome.VirtualBiomeType;
import org.spongepowered.api.world.difficulty.Difficulty;
import org.spongepowered.api.world.explosion.Explosion;
import org.spongepowered.api.world.gen.PopulatorObject;
import org.spongepowered.api.world.gen.PopulatorType;
import org.spongepowered.api.world.gen.WorldGeneratorModifier;
import org.spongepowered.api.world.gen.populator.*;
import org.spongepowered.api.world.gen.type.BiomeTreeType;
import org.spongepowered.api.world.gen.type.MushroomType;
import org.spongepowered.api.world.schematic.BlockPaletteType;
import org.spongepowered.api.world.schematic.Schematic;
import org.spongepowered.api.world.weather.Weather;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.ban.SpongeBanBuilder;
import org.spongepowered.common.block.SpongeBlockSnapshotBuilder;
import org.spongepowered.common.block.SpongeBlockStateBuilder;
import org.spongepowered.common.block.SpongeTileEntityArchetypeBuilder;
import org.spongepowered.common.boss.ServerBossBarBuilder;
import org.spongepowered.common.data.builder.data.meta.SpongePatternLayerBuilder;
import org.spongepowered.common.effect.particle.SpongeParticleEffectBuilder;
import org.spongepowered.common.effect.potion.SpongePotionBuilder;
import org.spongepowered.common.entity.SpongeEntityArchetypeBuilder;
import org.spongepowered.common.entity.SpongeEntitySnapshotBuilder;
import org.spongepowered.common.entity.ai.*;
import org.spongepowered.common.entity.ai.target.SpongeFindNearestAttackableTargetAIBuilder;
import org.spongepowered.common.entity.player.tab.TabListEntryBuilder;
import org.spongepowered.common.event.damage.*;
import org.spongepowered.common.event.entity.teleport.SpongeEntityTeleportCauseBuilder;
import org.spongepowered.common.event.entity.teleport.SpongePortalTeleportCauseBuilder;
import org.spongepowered.common.event.entity.teleport.SpongeTeleportCauseBuilder;
import org.spongepowered.common.event.spawn.SpongeBlockSpawnCauseBuilder;
import org.spongepowered.common.event.spawn.SpongeBreedingSpawnCauseBuilder;
import org.spongepowered.common.event.spawn.SpongeEntitySpawnCauseBuilder;
import org.spongepowered.common.event.spawn.SpongeMobSpawnerSpawnCauseBuilder;
import org.spongepowered.common.event.spawn.SpongeSpawnCauseBuilder;
import org.spongepowered.common.event.spawn.SpongeWeatherSpawnCauseBuilder;
import org.spongepowered.common.extra.fluid.SpongeFluidStackBuilder;
import org.spongepowered.common.extra.fluid.SpongeFluidStackSnapshotBuilder;
import org.spongepowered.common.item.SpongeFireworkEffectBuilder;
import org.spongepowered.common.item.inventory.SpongeInventoryBuilder;
import org.spongepowered.common.item.inventory.SpongeItemStackBuilder;
import org.spongepowered.common.item.inventory.archetype.SpongeInventoryArchetypeBuilder;
import org.spongepowered.common.item.inventory.generation.SpongeItemStackGenerator;
import org.spongepowered.common.item.merchant.SpongeTradeOfferBuilder;
import org.spongepowered.common.item.merchant.SpongeTradeOfferGenerator;
import org.spongepowered.common.registry.factory.ResourcePackFactoryModule;
import org.spongepowered.common.registry.factory.TimingsFactoryModule;
import org.spongepowered.common.registry.type.*;
import org.spongepowered.common.registry.type.block.*;
import org.spongepowered.common.registry.type.boss.BossBarColorRegistryModule;
import org.spongepowered.common.registry.type.boss.BossBarOverlayRegistryModule;
import org.spongepowered.common.registry.type.data.DataFormatRegistryModule;
import org.spongepowered.common.registry.type.data.DataTranslatorRegistryModule;
import org.spongepowered.common.registry.type.data.HandTypeRegistryModule;
import org.spongepowered.common.registry.type.data.KeyRegistryModule;
import org.spongepowered.common.registry.type.economy.TransactionTypeRegistryModule;
import org.spongepowered.common.registry.type.effect.ParticleRegistryModule;
import org.spongepowered.common.registry.type.effect.PotionEffectTypeRegistryModule;
import org.spongepowered.common.registry.type.effect.SoundCategoryRegistryModule;
import org.spongepowered.common.registry.type.effect.SoundRegistryModule;
import org.spongepowered.common.registry.type.entity.*;
import org.spongepowered.common.registry.type.event.DamageModifierTypeRegistryModule;
import org.spongepowered.common.registry.type.event.DamageSourceRegistryModule;
import org.spongepowered.common.registry.type.event.DamageTypeRegistryModule;
import org.spongepowered.common.registry.type.event.SpawnTypeRegistryModule;
import org.spongepowered.common.registry.type.event.TeleportTypeRegistryModule;
import org.spongepowered.common.registry.type.extra.FluidTypeRegistryModule;
import org.spongepowered.common.registry.type.item.*;
import org.spongepowered.common.registry.type.scoreboard.CollisionRuleRegistryModule;
import org.spongepowered.common.registry.type.scoreboard.CriteriaRegistryModule;
import org.spongepowered.common.registry.type.scoreboard.DisplaySlotRegistryModule;
import org.spongepowered.common.registry.type.scoreboard.ObjectiveDisplayModeRegistryModule;
import org.spongepowered.common.registry.type.scoreboard.VisibilityRegistryModule;
import org.spongepowered.common.registry.type.text.*;
import org.spongepowered.common.registry.type.world.*;
import org.spongepowered.common.registry.type.world.gen.BiomeTreeTypeRegistryModule;
import org.spongepowered.common.registry.type.world.gen.BiomeTypeRegistryModule;
import org.spongepowered.common.registry.type.world.gen.PopulatorObjectRegistryModule;
import org.spongepowered.common.registry.type.world.gen.PopulatorTypeRegistryModule;
import org.spongepowered.common.scheduler.SpongeTaskBuilder;
import org.spongepowered.common.scoreboard.builder.SpongeObjectiveBuilder;
import org.spongepowered.common.scoreboard.builder.SpongeScoreboardBuilder;
import org.spongepowered.common.scoreboard.builder.SpongeTeamBuilder;
import org.spongepowered.common.world.SpongeExplosionBuilder;
import org.spongepowered.common.world.SpongeWorldArchetypeBuilder;
import org.spongepowered.common.world.biome.SpongeBiomeGenerationSettingsBuilder;
import org.spongepowered.common.world.biome.SpongeVirtualBiomeTypeBuilder;
import org.spongepowered.common.world.schematic.SpongeSchematicBuilder;
import org.spongepowered.common.world.gen.builders.*;

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
            .registerBuilderSupplier(ColoredParticle.Builder.class, SpongeParticleEffectBuilder.BuilderColorable::new)
            .registerBuilderSupplier(NoteParticle.Builder.class, SpongeParticleEffectBuilder.BuilderNote::new)
            .registerBuilderSupplier(ItemParticle.Builder.class, SpongeParticleEffectBuilder.BuilderMaterial::new)
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
            .registerBuilderSupplier(ResizableParticle.Builder.class, SpongeParticleEffectBuilder.BuilderResizable::new)
            .registerBuilderSupplier(BlockParticle.Builder.class, SpongeParticleEffectBuilder.BuilderBlock::new)
            .registerBuilderSupplier(Task.Builder.class, SpongeTaskBuilder::new)
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
            .registerBuilderSupplier(SpawnCause.Builder.class, SpongeSpawnCauseBuilder::new)
            .registerBuilderSupplier(EntitySpawnCause.Builder.class, SpongeEntitySpawnCauseBuilder::new)
            .registerBuilderSupplier(BreedingSpawnCause.Builder.class, SpongeBreedingSpawnCauseBuilder::new)
            .registerBuilderSupplier(BlockSpawnCause.Builder.class, SpongeBlockSpawnCauseBuilder::new)
            .registerBuilderSupplier(MobSpawnerSpawnCause.Builder.class, SpongeMobSpawnerSpawnCauseBuilder::new)
            .registerBuilderSupplier(FluidStack.Builder.class, SpongeFluidStackBuilder::new)
            .registerBuilderSupplier(FluidStackSnapshot.Builder.class, SpongeFluidStackSnapshotBuilder::new)
            .registerBuilderSupplier(TabListEntry.Builder.class, TabListEntryBuilder::new)
            .registerBuilderSupplier(TradeOfferGenerator.Builder.class, SpongeTradeOfferGenerator.Builder::new)
            .registerBuilderSupplier(ItemStackGenerator.Builder.class, SpongeItemStackGenerator.Builder::new)
            .registerBuilderSupplier(WeatherSpawnCause.Builder.class, SpongeWeatherSpawnCauseBuilder::new)
            .registerBuilderSupplier(TeleportCause.Builder.class, SpongeTeleportCauseBuilder::new)
            .registerBuilderSupplier(EntityTeleportCause.Builder.class, SpongeEntityTeleportCauseBuilder::new)
            .registerBuilderSupplier(PortalTeleportCause.Builder.class, SpongePortalTeleportCauseBuilder::new)
            .registerBuilderSupplier(ServerBossBar.Builder.class, ServerBossBarBuilder::new)
            .registerBuilderSupplier(EntityArchetype.Builder.class, SpongeEntityArchetypeBuilder::new)
            .registerBuilderSupplier(TileEntityArchetype.Builder.class, SpongeTileEntityArchetypeBuilder::new)
            .registerBuilderSupplier(Schematic.Builder.class, SpongeSchematicBuilder::new)
            .registerBuilderSupplier(VirtualBiomeType.Builder.class, SpongeVirtualBiomeTypeBuilder::new)
            .registerBuilderSupplier(BiomeGenerationSettings.Builder.class, SpongeBiomeGenerationSettingsBuilder::new)
            .registerBuilderSupplier(InventoryArchetype.Builder.class, SpongeInventoryArchetypeBuilder::new)
            .registerBuilderSupplier(Inventory.Builder.class, SpongeInventoryBuilder::new)
        ;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void registerCommonModules(SpongeGameRegistry registry) {
        registry.registerModule(new ArgumentRegistryModule())
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
            .registerModule(Enchantment.class, EnchantmentRegistryModule.getInstance())
            .registerModule((Class<EnumTrait<?>>) (Class) EnumTrait.class, EnumTraitRegistryModule.getInstance())
            .registerModule(EntityType.class, EntityTypeRegistryModule.getInstance())
            .registerModule(EquipmentType.class, new EquipmentTypeRegistryModule())
            .registerModule(FireworkShape.class, new FireworkShapeRegistryModule())
            .registerModule(Fish.class, new FishRegistryModule())
            .registerModule(FluidType.class, FluidTypeRegistryModule.getInstance())
            .registerModule(GameMode.class, new GameModeRegistryModule())
            .registerModule(GeneratorType.class, new GeneratorTypeRegistryModule())
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
            .registerModule(ParticleType.class, new ParticleRegistryModule())
            .registerModule(PistonType.class, new PistonTypeRegistryModule())
            .registerModule(PlantType.class, new PlantTypeModuleRegistry())
            .registerModule(PopulatorObject.class, new PopulatorObjectRegistryModule())
            .registerModule(PopulatorType.class, PopulatorTypeRegistryModule.getInstance())
            .registerModule(PortionType.class, new PortionTypeRegistryModule())
            .registerModule(PotionEffectType.class, PotionEffectTypeRegistryModule.getInstance())
            .registerModule(PrismarineType.class, new PrismarineRegistryModule())
            .registerModule(Profession.class, ProfessionRegistryModule.getInstance())
            .registerModule(QuartzType.class, new QuartzTypeRegistryModule())
            .registerModule(RabbitType.class, new RabbitTypeRegistryModule())
            .registerModule(RailDirection.class, new RailDirectionRegistryModule())
            .registerModule(Rotation.class, new RotationRegistryModule())
            .registerModule(SandstoneType.class, new SandstoneTypeRegistryModule())
            .registerModule(SandType.class, new SandTypeRegistryModule())
            .registerModule(SelectorType.class, new SelectorTypeRegistryModule())
            .registerModule(SerializationBehavior.class, new SerializationBehaviorRegistryModule())
            .registerModule(ShrubType.class, new ShrubTypeRegistryModule())
            .registerModule(SkeletonType.class, new SkeletonTypeRegistryModule())
            .registerModule(SkullType.class, new SkullTypeRegistryModule())
            .registerModule(SlabType.class, new SlabTypeRegistryModule())
            .registerModule(SoundType.class, new SoundRegistryModule())
            .registerModule(SpawnType.class, new SpawnTypeRegistryModule())
            .registerModule(SoundCategory.class, new SoundCategoryRegistryModule())
            .registerModule(StairShape.class, new StairShapeRegistryModule())
            .registerModule(StoneType.class, new StoneTypeRegistryModule())
            .registerModule(TeleportType.class, TeleportTypeRegistryModule.getInstance())
            .registerModule(TextColor.class, new TextColorRegistryModule())
            .registerModule(new TextSerializerRegistryModule())
            .registerModule(TextStyle.Base.class, new TextStyleRegistryModule())
            .registerModule(TileEntityType.class, TileEntityTypeRegistryModule.getInstance())
            .registerModule(ToolType.class, new ToolTypeRegistryModule())
            .registerModule(TreeType.class, new TreeTypeRegistryModule())
            .registerModule(Visibility.class, new VisibilityRegistryModule())
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
            .registerModule((Class<Key<?>>) (Class<?>) Key.class, KeyRegistryModule.getInstance())
            .registerModule(ZombieType.class, ZombieTypeRegistryModule.getInstance())
            .registerModule(InventoryArchetype.class, InventoryArchetypeRegistryModule.getInstance())
            ;
    }

    CommonModuleRegistry() { }

    private static final class Holder {

        static final CommonModuleRegistry INSTANCE = new CommonModuleRegistry();
    }

}
