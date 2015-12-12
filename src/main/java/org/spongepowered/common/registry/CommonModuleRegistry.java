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
import org.spongepowered.api.block.tileentity.TileEntityType;
import org.spongepowered.api.block.trait.BooleanTrait;
import org.spongepowered.api.block.trait.EnumTrait;
import org.spongepowered.api.block.trait.IntegerTrait;
import org.spongepowered.api.data.meta.PatternLayer;
import org.spongepowered.api.data.type.Art;
import org.spongepowered.api.data.type.BannerPatternShape;
import org.spongepowered.api.data.type.BigMushroomType;
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
import org.spongepowered.api.data.type.Hinge;
import org.spongepowered.api.data.type.LogAxis;
import org.spongepowered.api.data.type.NotePitch;
import org.spongepowered.api.data.type.OcelotType;
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
import org.spongepowered.api.data.type.SkeletonType;
import org.spongepowered.api.data.type.SkullType;
import org.spongepowered.api.data.type.SlabType;
import org.spongepowered.api.data.type.StairShape;
import org.spongepowered.api.data.type.StoneType;
import org.spongepowered.api.data.type.TreeType;
import org.spongepowered.api.data.type.WallType;
import org.spongepowered.api.effect.particle.BlockParticle;
import org.spongepowered.api.effect.particle.ColoredParticle;
import org.spongepowered.api.effect.particle.ItemParticle;
import org.spongepowered.api.effect.particle.NoteParticle;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.particle.ParticleType;
import org.spongepowered.api.effect.particle.ResizableParticle;
import org.spongepowered.api.effect.sound.SoundType;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.ai.GoalType;
import org.spongepowered.api.entity.ai.task.AITaskType;
import org.spongepowered.api.entity.ai.task.builtin.SwimmingAITask;
import org.spongepowered.api.entity.ai.task.builtin.creature.AttackLivingAITask;
import org.spongepowered.api.entity.ai.task.builtin.creature.AvoidEntityAITask;
import org.spongepowered.api.entity.ai.task.builtin.creature.WanderAITask;
import org.spongepowered.api.entity.ai.task.builtin.creature.WatchClosestAITask;
import org.spongepowered.api.entity.ai.task.builtin.creature.horse.RunAroundLikeCrazyAITask;
import org.spongepowered.api.entity.ai.task.builtin.creature.target.FindNearestAttackableTargetAITask;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.event.cause.entity.damage.DamageModifierType;
import org.spongepowered.api.event.cause.entity.damage.DamageType;
import org.spongepowered.api.event.cause.entity.damage.source.BlockDamageSource;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSource;
import org.spongepowered.api.event.cause.entity.damage.source.EntityDamageSource;
import org.spongepowered.api.event.cause.entity.damage.source.FallingBlockDamageSource;
import org.spongepowered.api.event.cause.entity.damage.source.IndirectEntityDamageSource;
import org.spongepowered.api.item.Enchantment;
import org.spongepowered.api.item.FireworkEffect;
import org.spongepowered.api.item.FireworkShape;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.equipment.EquipmentType;
import org.spongepowered.api.item.merchant.TradeOffer;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.effect.potion.PotionEffectType;
import org.spongepowered.api.scoreboard.Scoreboard;
import org.spongepowered.api.scoreboard.Team;
import org.spongepowered.api.scoreboard.Visibility;
import org.spongepowered.api.scoreboard.critieria.Criterion;
import org.spongepowered.api.scoreboard.displayslot.DisplaySlot;
import org.spongepowered.api.scoreboard.objective.Objective;
import org.spongepowered.api.scoreboard.objective.displaymode.ObjectiveDisplayMode;
import org.spongepowered.api.statistic.Statistic;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.selector.SelectorType;
import org.spongepowered.api.util.rotation.Rotation;
import org.spongepowered.api.world.DimensionType;
import org.spongepowered.api.world.GeneratorType;
import org.spongepowered.api.world.WorldBuilder;
import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.api.world.difficulty.Difficulty;
import org.spongepowered.api.world.explosion.Explosion;
import org.spongepowered.api.world.gen.PopulatorType;
import org.spongepowered.api.world.weather.Weather;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.block.SpongeBlockSnapshotBuilder;
import org.spongepowered.common.block.SpongeBlockStateBuilder;
import org.spongepowered.common.data.builder.block.data.SpongePatternLayerBuilder;
import org.spongepowered.common.effect.particle.SpongeParticleEffectBuilder;
import org.spongepowered.common.entity.SpongeEntitySnapshotBuilder;
import org.spongepowered.common.entity.ai.SpongeAttackLivingAIBuilder;
import org.spongepowered.common.entity.ai.SpongeAvoidEntityAIBuilder;
import org.spongepowered.common.entity.ai.SpongeRunAroundLikeCrazyAIBuilder;
import org.spongepowered.common.entity.ai.SpongeSwimmingAIBuilder;
import org.spongepowered.common.entity.ai.SpongeWanderAIBuilder;
import org.spongepowered.common.entity.ai.SpongeWatchClosestAIBuilder;
import org.spongepowered.common.entity.ai.target.SpongeFindNearestAttackableTargetAIBuilder;
import org.spongepowered.common.event.SpongeBlockDamageSourceBuilder;
import org.spongepowered.common.event.SpongeDamageSourceBuilder;
import org.spongepowered.common.event.SpongeEntityDamageSourceBuilder;
import org.spongepowered.common.event.SpongeFallingBlockDamgeSourceBuilder;
import org.spongepowered.common.event.SpongeIndirectEntityDamageSourceBuilder;
import org.spongepowered.common.item.SpongeFireworkEffectBuilder;
import org.spongepowered.common.item.SpongeItemStackBuilder;
import org.spongepowered.common.item.merchant.SpongeTradeOfferBuilder;
import org.spongepowered.common.effect.potion.SpongePotionBuilder;
import org.spongepowered.common.registry.factory.MessageSinkFactoryModule;
import org.spongepowered.common.registry.factory.ResourcePackFactoryModule;
import org.spongepowered.common.registry.factory.SelectorFactoryModule;
import org.spongepowered.common.registry.factory.TextFactoryModule;
import org.spongepowered.common.registry.factory.TimingsFactoryModule;
import org.spongepowered.common.registry.type.AITaskTypeModule;
import org.spongepowered.common.registry.type.ArgumentRegistryModule;
import org.spongepowered.common.registry.type.ArtRegistryModule;
import org.spongepowered.common.registry.type.BannerPatternShapeRegistryModule;
import org.spongepowered.common.registry.type.BigMushroomRegistryModule;
import org.spongepowered.common.registry.type.BiomeTypeRegistryModule;
import org.spongepowered.common.registry.type.BlockTypeRegistryModule;
import org.spongepowered.common.registry.type.BrickTypeRegistryModule;
import org.spongepowered.common.registry.type.CareerRegistryModule;
import org.spongepowered.common.registry.type.ChatTypeRegistryModule;
import org.spongepowered.common.registry.type.CoalTypeRegistryModule;
import org.spongepowered.common.registry.type.ComparatorTypeRegistryModule;
import org.spongepowered.common.registry.type.CookedFishRegistryModule;
import org.spongepowered.common.registry.type.CriteriaRegistryModule;
import org.spongepowered.common.registry.type.DamageModifierTypeRegistryModule;
import org.spongepowered.common.registry.type.DamageSourceRegistryModule;
import org.spongepowered.common.registry.type.DamageTypeRegistryModule;
import org.spongepowered.common.registry.type.DifficultyRegistryModule;
import org.spongepowered.common.registry.type.DirtTypeRegistryModule;
import org.spongepowered.common.registry.type.DisguisedBlockTypeRegistryModule;
import org.spongepowered.common.registry.type.DisplaySlotRegistryModule;
import org.spongepowered.common.registry.type.DoublePlantTypeRegistryModule;
import org.spongepowered.common.registry.type.DyeColorRegistryModule;
import org.spongepowered.common.registry.type.EnchantmentRegistryModule;
import org.spongepowered.common.registry.type.EntityTypeRegistryModule;
import org.spongepowered.common.registry.type.EquipmentTypeRegistryModule;
import org.spongepowered.common.registry.type.FireworkShapeRegistryModule;
import org.spongepowered.common.registry.type.FishRegistryModule;
import org.spongepowered.common.registry.type.GameModeRegistryModule;
import org.spongepowered.common.registry.type.GeneratorRegistryModule;
import org.spongepowered.common.registry.type.GoalTypeModule;
import org.spongepowered.common.registry.type.GoldenAppleRegistryModule;
import org.spongepowered.common.registry.type.HingeRegistryModule;
import org.spongepowered.common.registry.type.ItemTypeRegistryModule;
import org.spongepowered.common.registry.type.LogAxisRegistryModule;
import org.spongepowered.common.registry.type.NotePitchRegistryModule;
import org.spongepowered.common.registry.type.ObjectiveDisplayModeRegistryModule;
import org.spongepowered.common.registry.type.OcelotTypeRegistryModule;
import org.spongepowered.common.registry.type.ParticleRegistryModule;
import org.spongepowered.common.registry.type.PistonTypeRegistryModule;
import org.spongepowered.common.registry.type.PlantTypeModuleRegistry;
import org.spongepowered.common.registry.type.PopulatorTypeRegistryModule;
import org.spongepowered.common.registry.type.PortionTypeRegistryModule;
import org.spongepowered.common.registry.type.PotionEffectTypeRegistryModule;
import org.spongepowered.common.registry.type.PrismarineRegistryModule;
import org.spongepowered.common.registry.type.ProfessionRegistryModule;
import org.spongepowered.common.registry.type.QuartzTypeRegistryModule;
import org.spongepowered.common.registry.type.RabbitTypeRegistryModule;
import org.spongepowered.common.registry.type.RailDirectionRegistryModule;
import org.spongepowered.common.registry.type.RotationRegistryModule;
import org.spongepowered.common.registry.type.SandTypeRegistryModule;
import org.spongepowered.common.registry.type.SandstoneTypeRegistryModule;
import org.spongepowered.common.registry.type.SelectorTypeRegistryModule;
import org.spongepowered.common.registry.type.ShrubTypeRegistryModule;
import org.spongepowered.common.registry.type.SkeletonTypeRegistryModule;
import org.spongepowered.common.registry.type.SkullTypeRegistryModule;
import org.spongepowered.common.registry.type.SlabTypeRegistryModule;
import org.spongepowered.common.registry.type.SoundRegistryModule;
import org.spongepowered.common.registry.type.StairShapeRegistryModule;
import org.spongepowered.common.registry.type.StoneTypeRegistryModule;
import org.spongepowered.common.registry.type.TextColorsRegistryModule;
import org.spongepowered.common.registry.type.TextStyleRegistryModule;
import org.spongepowered.common.registry.type.TileEntityTypeRegistryModule;
import org.spongepowered.common.registry.type.TreeTypeRegistryModule;
import org.spongepowered.common.registry.type.VisibilityRegistryModule;
import org.spongepowered.common.registry.type.WallTypeRegistryModule;
import org.spongepowered.common.registry.type.WeatherRegistryModule;
import org.spongepowered.common.registry.type.block.BooleanTraitRegistryModule;
import org.spongepowered.common.registry.type.block.EnumTraitRegistryModule;
import org.spongepowered.common.registry.type.block.IntegerTraitRegistryModule;
import org.spongepowered.common.registry.type.world.DimensionRegistryModule;
import org.spongepowered.common.scoreboard.builder.SpongeObjectiveBuilder;
import org.spongepowered.common.scoreboard.builder.SpongeScoreboardBuilder;
import org.spongepowered.common.scoreboard.builder.SpongeTeamBuilder;
import org.spongepowered.common.world.SpongeExplosionBuilder;
import org.spongepowered.common.world.SpongeWorldBuilder;

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
        factoryRegistries.add(new MessageSinkFactoryModule());
        factoryRegistries.add(new ResourcePackFactoryModule());
        factoryRegistries.add(new SelectorFactoryModule());
        factoryRegistries.add(new SelectorFactoryModule());
        factoryRegistries.add(new TextFactoryModule());
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
            .registerBuilderSupplier(Statistic.Builder.class, () -> {
                throw new UnsupportedOperationException();
            })
            .registerBuilderSupplier(DamageSource.Builder.class, SpongeDamageSourceBuilder::new)
            .registerBuilderSupplier(EntityDamageSource.Builder.class, SpongeEntityDamageSourceBuilder::new)
            .registerBuilderSupplier(IndirectEntityDamageSource.Builder.class, SpongeIndirectEntityDamageSourceBuilder::new)
            .registerBuilderSupplier(FallingBlockDamageSource.Builder.class, SpongeFallingBlockDamgeSourceBuilder::new)
            .registerBuilderSupplier(BlockDamageSource.Builder.class, SpongeBlockDamageSourceBuilder::new)
            .registerBuilderSupplier(WorldBuilder.class, SpongeWorldBuilder::new)
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
            .registerBuilderSupplier(PatternLayer.Builder.class, SpongePatternLayerBuilder::new)
            .registerBuilderSupplier(ResizableParticle.Builder.class, SpongeParticleEffectBuilder.BuilderResizable::new)
            .registerBuilderSupplier(BlockParticle.Builder.class, SpongeParticleEffectBuilder.BuilderBlock::new)
            ;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void registerCommonModules(SpongeGameRegistry registry) {
        registry.registerModule(new ArgumentRegistryModule())
            .registerModule(AITaskType.class, AITaskTypeModule.getInstance())
            .registerModule(Art.class, new ArtRegistryModule())
            .registerModule(BannerPatternShape.class, new BannerPatternShapeRegistryModule())
            .registerModule(BooleanTrait.class, BooleanTraitRegistryModule.getInstance())
            .registerModule(BigMushroomType.class, new BigMushroomRegistryModule())
            .registerModule(BiomeType.class, new BiomeTypeRegistryModule())
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
            .registerModule(Difficulty.class, new DifficultyRegistryModule())
            .registerModule(DimensionType.class, DimensionRegistryModule.getInstance())
            .registerModule(DirtType.class, new DirtTypeRegistryModule())
            .registerModule(DisguisedBlockType.class, new DisguisedBlockTypeRegistryModule())
            .registerModule(DisplaySlot.class, new DisplaySlotRegistryModule())
            .registerModule(DoublePlantType.class, new DoublePlantTypeRegistryModule())
            .registerModule(DyeColor.class, DyeColorRegistryModule.getInstance())
            .registerModule(Enchantment.class, new EnchantmentRegistryModule())
            .registerModule((Class<EnumTrait<?>>) (Class) EnumTrait.class, EnumTraitRegistryModule.getInstance())
            .registerModule(EntityType.class, EntityTypeRegistryModule.getInstance())
            .registerModule(EquipmentType.class, new EquipmentTypeRegistryModule())
            .registerModule(FireworkShape.class, new FireworkShapeRegistryModule())
            .registerModule(Fish.class, new FishRegistryModule())
            .registerModule(GameMode.class, new GameModeRegistryModule())
            .registerModule(GeneratorType.class, new GeneratorRegistryModule())
            .registerModule(GoalType.class, GoalTypeModule.getInstance())
            .registerModule(GoldenApple.class, new GoldenAppleRegistryModule())
            .registerModule(Hinge.class, new HingeRegistryModule())
            .registerModule(IntegerTrait.class, IntegerTraitRegistryModule.getInstance())
            .registerModule(ItemType.class, ItemTypeRegistryModule.getInstance())
            .registerModule(LogAxis.class, new LogAxisRegistryModule())
            .registerModule(NotePitch.class, new NotePitchRegistryModule())
            .registerModule(ObjectiveDisplayMode.class, new ObjectiveDisplayModeRegistryModule())
            .registerModule(OcelotType.class, new OcelotTypeRegistryModule())
            .registerModule(ParticleType.class, new ParticleRegistryModule())
            .registerModule(PistonType.class, new PistonTypeRegistryModule())
            .registerModule(PlantType.class, new PlantTypeModuleRegistry())
            .registerModule(PopulatorType.class, new PopulatorTypeRegistryModule())
            .registerModule(PortionType.class, new PortionTypeRegistryModule())
            .registerModule(PotionEffectType.class, new PotionEffectTypeRegistryModule())
            .registerModule(PrismarineType.class, new PrismarineRegistryModule())
            .registerModule(Profession.class, ProfessionRegistryModule.getInstance())
            .registerModule(QuartzType.class, new QuartzTypeRegistryModule())
            .registerModule(RabbitType.class, new RabbitTypeRegistryModule())
            .registerModule(RailDirection.class, new RailDirectionRegistryModule())
            .registerModule(Rotation.class, new RotationRegistryModule())
            .registerModule(SandstoneType.class, new SandstoneTypeRegistryModule())
            .registerModule(SandType.class, new SandTypeRegistryModule())
            .registerModule(SelectorType.class, new SelectorTypeRegistryModule())
            .registerModule(ShrubType.class, new ShrubTypeRegistryModule())
            .registerModule(SkeletonType.class, new SkeletonTypeRegistryModule())
            .registerModule(SkullType.class, new SkullTypeRegistryModule())
            .registerModule(SlabType.class, new SlabTypeRegistryModule())
            .registerModule(SoundType.class, new SoundRegistryModule())
            .registerModule(StairShape.class, new StairShapeRegistryModule())
            .registerModule(StoneType.class, new StoneTypeRegistryModule())
            .registerModule(TextColor.class, new TextColorsRegistryModule())
            .registerModule(new TextStyleRegistryModule())
            .registerModule(TileEntityType.class, new TileEntityTypeRegistryModule())
            .registerModule(TreeType.class, new TreeTypeRegistryModule())
            .registerModule(Visibility.class, new VisibilityRegistryModule())
            .registerModule(WallType.class, new WallTypeRegistryModule())
            .registerModule(Weather.class, new WeatherRegistryModule())
            ;
    }

    private CommonModuleRegistry() { }

    private static final class Holder {

        private static final CommonModuleRegistry INSTANCE = new CommonModuleRegistry();
    }

}

