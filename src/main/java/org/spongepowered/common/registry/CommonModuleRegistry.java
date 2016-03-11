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

import org.spongepowered.api.block.*;
import org.spongepowered.api.block.tileentity.*;
import org.spongepowered.api.block.trait.*;
import org.spongepowered.api.data.meta.*;
import org.spongepowered.api.data.persistence.*;
import org.spongepowered.api.data.type.*;
import org.spongepowered.api.effect.particle.*;
import org.spongepowered.api.effect.potion.*;
import org.spongepowered.api.effect.sound.*;
import org.spongepowered.api.entity.*;
import org.spongepowered.api.entity.ai.*;
import org.spongepowered.api.entity.ai.task.*;
import org.spongepowered.api.entity.ai.task.builtin.*;
import org.spongepowered.api.entity.ai.task.builtin.creature.*;
import org.spongepowered.api.entity.ai.task.builtin.creature.horse.*;
import org.spongepowered.api.entity.ai.task.builtin.creature.target.*;
import org.spongepowered.api.entity.living.player.gamemode.*;
import org.spongepowered.api.entity.living.player.tab.*;
import org.spongepowered.api.event.cause.entity.damage.*;
import org.spongepowered.api.event.cause.entity.damage.source.*;
import org.spongepowered.api.event.cause.entity.spawn.*;
import org.spongepowered.api.extra.fluid.*;
import org.spongepowered.api.item.*;
import org.spongepowered.api.item.inventory.*;
import org.spongepowered.api.item.inventory.equipment.*;
import org.spongepowered.api.item.merchant.*;
import org.spongepowered.api.registry.*;
import org.spongepowered.api.scheduler.*;
import org.spongepowered.api.scoreboard.*;
import org.spongepowered.api.scoreboard.critieria.*;
import org.spongepowered.api.scoreboard.displayslot.*;
import org.spongepowered.api.scoreboard.objective.*;
import org.spongepowered.api.scoreboard.objective.displaymode.*;
import org.spongepowered.api.service.economy.transaction.*;
import org.spongepowered.api.statistic.*;
import org.spongepowered.api.text.chat.*;
import org.spongepowered.api.text.format.*;
import org.spongepowered.api.text.selector.*;
import org.spongepowered.api.util.ban.*;
import org.spongepowered.api.util.rotation.*;
import org.spongepowered.api.world.*;
import org.spongepowered.api.world.biome.*;
import org.spongepowered.api.world.difficulty.*;
import org.spongepowered.api.world.explosion.*;
import org.spongepowered.api.world.gen.*;
import org.spongepowered.api.world.gen.populator.*;
import org.spongepowered.api.world.gen.type.*;
import org.spongepowered.api.world.weather.*;
import org.spongepowered.common.*;
import org.spongepowered.common.ban.*;
import org.spongepowered.common.block.*;
import org.spongepowered.common.data.builder.data.meta.*;
import org.spongepowered.common.effect.particle.*;
import org.spongepowered.common.effect.potion.*;
import org.spongepowered.common.entity.*;
import org.spongepowered.common.entity.ai.*;
import org.spongepowered.common.entity.ai.target.*;
import org.spongepowered.common.entity.player.tab.*;
import org.spongepowered.common.event.*;
import org.spongepowered.common.event.spawn.*;
import org.spongepowered.common.extra.fluid.*;
import org.spongepowered.common.item.*;
import org.spongepowered.common.item.inventory.*;
import org.spongepowered.common.item.inventory.generation.*;
import org.spongepowered.common.item.merchant.*;
import org.spongepowered.common.registry.factory.*;
import org.spongepowered.common.registry.type.*;
import org.spongepowered.common.registry.type.block.*;
import org.spongepowered.common.registry.type.data.*;
import org.spongepowered.common.registry.type.economy.*;
import org.spongepowered.common.registry.type.effect.*;
import org.spongepowered.common.registry.type.entity.*;
import org.spongepowered.common.registry.type.event.*;
import org.spongepowered.common.registry.type.extra.*;
import org.spongepowered.common.registry.type.item.*;
import org.spongepowered.common.registry.type.scoreboard.*;
import org.spongepowered.common.registry.type.text.*;
import org.spongepowered.common.registry.type.world.*;
import org.spongepowered.common.registry.type.world.gen.*;
import org.spongepowered.common.scheduler.*;
import org.spongepowered.common.scoreboard.builder.*;
import org.spongepowered.common.world.*;
import org.spongepowered.common.world.gen.builders.*;

import java.lang.reflect.*;
import java.util.*;

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
            .registerBuilderSupplier(Statistic.StatisticBuilder.class, () -> {
                throw new UnsupportedOperationException();
            })
            .registerBuilderSupplier(DamageSource.Builder.class, SpongeDamageSourceBuilder::new)
            .registerBuilderSupplier(EntityDamageSource.Builder.class, SpongeEntityDamageSourceBuilder::new)
            .registerBuilderSupplier(IndirectEntityDamageSource.Builder.class, SpongeIndirectEntityDamageSourceBuilder::new)
            .registerBuilderSupplier(FallingBlockDamageSource.Builder.class, SpongeFallingBlockDamgeSourceBuilder::new)
            .registerBuilderSupplier(BlockDamageSource.Builder.class, SpongeBlockDamageSourceBuilder::new)
            .registerBuilderSupplier(WorldCreationSettings.Builder.class, SpongeWorldCreationSettingsBuilder::new)
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
            .registerBuilderSupplier(Task.Builder.class, SpongeTaskBuilder::new)
            .registerBuilderSupplier(BigMushroom.Builder.class, BigMushroomBuilder::new)
            .registerBuilderSupplier(BlockBlob.Builder.class, BlockBlobBuilder::new)
            .registerBuilderSupplier(Cactus.Builder.class, CactusBuilder::new)
            .registerBuilderSupplier(DeadBush.Builder.class, DeadBushBuilder::new)
            .registerBuilderSupplier(DesertWell.Builder.class, DesertWellBuilder::new)
            .registerBuilderSupplier(DoublePlant.Builder.class, DoublePlantBuilder::new)
            .registerBuilderSupplier(Dungeon.Builder.class, DungeonBuilder::new)
            .registerBuilderSupplier(EnderCrystalPlatform.Builder.class, EnderCrystalPlatformBuilder::new)
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
            .registerModule(Difficulty.class, new DifficultyRegistryModule())
            .registerModule(DimensionType.class, DimensionRegistryModule.getInstance())
            .registerModule(DirtType.class, new DirtTypeRegistryModule())
            .registerModule(DisguisedBlockType.class, new DisguisedBlockTypeRegistryModule())
            .registerModule(DisplaySlot.class,DisplaySlotRegistryModule.getInstance())
            .registerModule(DoublePlantType.class, new DoublePlantTypeRegistryModule())
            .registerModule(DyeColor.class, DyeColorRegistryModule.getInstance())
            .registerModule(Enchantment.class, new EnchantmentRegistryModule())
            .registerModule((Class<EnumTrait<?>>) (Class) EnumTrait.class, EnumTraitRegistryModule.getInstance())
            .registerModule(EntityType.class, EntityTypeRegistryModule.getInstance())
            .registerModule(EquipmentType.class, new EquipmentTypeRegistryModule())
            .registerModule(FireworkShape.class, new FireworkShapeRegistryModule())
            .registerModule(Fish.class, new FishRegistryModule())
            .registerModule(FluidType.class, FluidTypeRegistryModule.getInstance())
            .registerModule(GameMode.class, new GameModeRegistryModule())
            .registerModule(GeneratorType.class, new GeneratorRegistryModule())
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
            .registerModule(ShrubType.class, new ShrubTypeRegistryModule())
            .registerModule(SkeletonType.class, new SkeletonTypeRegistryModule())
            .registerModule(SkullType.class, new SkullTypeRegistryModule())
            .registerModule(SlabType.class, new SlabTypeRegistryModule())
            .registerModule(SoundType.class, new SoundRegistryModule())
            .registerModule(SpawnType.class, new SpawnTypeRegistryModule())
            .registerModule(StairShape.class, new StairShapeRegistryModule())
            .registerModule(StoneType.class, new StoneTypeRegistryModule())
            .registerModule(TextColor.class, new TextColorRegistryModule())
            .registerModule(new TextSerializerRegistryModule())
            .registerModule(TextStyle.Base.class, new TextStyleRegistryModule())
            .registerModule(TileEntityType.class, TileEntityTypeRegistryModule.getInstance())
            .registerModule(ToolType.class, new ToolTypeRegistryModule())
            .registerModule(TreeType.class, new TreeTypeRegistryModule())
            .registerModule(Visibility.class, new VisibilityRegistryModule())
            .registerModule(WallType.class, new WallTypeRegistryModule())
            .registerModule(Weather.class, new WeatherRegistryModule())
            .registerModule(WorldGeneratorModifier.class, GeneratorModifierRegistryModule.getInstance())
            .registerModule(TransactionType.class, new TransactionTypeRegistryModule())
            .registerModule(ChatVisibility.class, new ChatVisibilityRegistryModule())
            .registerModule(SkinPart.class, new SkinPartRegistryModule())
            ;
    }

    CommonModuleRegistry() { }

    private static final class Holder {

        static final CommonModuleRegistry INSTANCE = new CommonModuleRegistry();
    }

}

