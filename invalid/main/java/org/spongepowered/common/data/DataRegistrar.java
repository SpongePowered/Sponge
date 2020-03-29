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
package org.spongepowered.common.data;

import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.entity.Sign;
import org.spongepowered.api.block.entity.Skull;
import org.spongepowered.api.block.entity.carrier.furnace.FurnaceBlockEntity;
import org.spongepowered.api.data.DataManipulator.Mutable;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.entity.EntityArchetype;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.fluid.FluidStackSnapshot;
import org.spongepowered.api.item.FireworkEffect;
import org.spongepowered.api.item.enchantment.Enchantment;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.merchant.TradeOffer;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.text.BookView;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Color;
import org.spongepowered.api.util.weighted.VariableAmount;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.api.world.Location;
import org.spongepowered.common.block.SpongeBlockSnapshotBuilder;
import org.spongepowered.common.block.SpongeBlockStateBuilder;
import org.spongepowered.common.data.builder.authlib.SpongeGameProfileBuilder;
import org.spongepowered.common.data.builder.block.state.SpongeBlockStateMetaContentUpdater;
import org.spongepowered.common.data.builder.block.tileentity.SpongeBannerBuilder;
import org.spongepowered.common.data.builder.block.tileentity.SpongeBeaconBuilder;
import org.spongepowered.common.data.builder.block.tileentity.SpongeBrewingStandBuilder;
import org.spongepowered.common.data.builder.block.tileentity.SpongeChestBuilder;
import org.spongepowered.common.data.builder.block.tileentity.SpongeCommandBlockBuilder;
import org.spongepowered.common.data.builder.block.tileentity.SpongeComparatorBuilder;
import org.spongepowered.common.data.builder.block.tileentity.SpongeDaylightBuilder;
import org.spongepowered.common.data.builder.block.tileentity.SpongeDispenserBuilder;
import org.spongepowered.common.data.builder.block.tileentity.SpongeDropperBuilder;
import org.spongepowered.common.data.builder.block.tileentity.SpongeEnchantmentTableBuilder;
import org.spongepowered.common.data.builder.block.tileentity.SpongeEndPortalBuilder;
import org.spongepowered.common.data.builder.block.tileentity.SpongeEnderChestBuilder;
import org.spongepowered.common.data.builder.block.tileentity.SpongeFurnaceBuilder;
import org.spongepowered.common.data.builder.block.tileentity.SpongeHopperBuilder;
import org.spongepowered.common.data.builder.block.tileentity.SpongeMobSpawnerBuilder;
import org.spongepowered.common.data.builder.block.tileentity.SpongeSignBuilder;
import org.spongepowered.common.data.builder.block.tileentity.SpongeSkullBuilder;
import org.spongepowered.common.data.builder.item.SpongeFireworkEffectDataBuilder;
import org.spongepowered.common.data.builder.item.SpongeItemStackSnapshotBuilder;
import org.spongepowered.common.data.builder.manipulator.InvisibilityDataAddVanishUpdater;
import org.spongepowered.common.data.builder.manipulator.immutable.item.ImmutableItemEnchantmentDataBuilder;
import org.spongepowered.common.data.builder.meta.SpongePatternLayerBuilder;
import org.spongepowered.common.data.builder.util.weighted.BaseAndAdditionBuilder;
import org.spongepowered.common.data.builder.util.weighted.BaseAndVarianceBuilder;
import org.spongepowered.common.data.builder.util.weighted.FixedBuilder;
import org.spongepowered.common.data.builder.util.weighted.OptionalVarianceBuilder;
import org.spongepowered.common.data.builder.world.LocationBuilder;
import org.spongepowered.common.data.util.LegacyCustomDataClassContentUpdater;
import org.spongepowered.common.effect.particle.SpongeParticleEffectBuilder;
import org.spongepowered.common.effect.potion.PotionEffectContentUpdater;
import org.spongepowered.common.effect.potion.SpongePotionBuilder;
import org.spongepowered.common.entity.SpongeEntityArchetypeBuilder;
import org.spongepowered.common.entity.SpongeEntitySnapshotBuilder;
import org.spongepowered.common.extra.fluid.SpongeFluidStackSnapshotBuilder;
import org.spongepowered.common.item.ItemStackSnapshotDuplicateManipulatorUpdater;
import org.spongepowered.common.item.SpongeItemStackBuilder;
import org.spongepowered.common.item.enchantment.SpongeEnchantmentBuilder;
import org.spongepowered.common.item.merchant.SpongeTradeOfferBuilder;
import org.spongepowered.common.world.SpongeLocatableBlockBuilder;
import org.spongepowered.common.world.storage.SpongePlayerData;

public final class DataRegistrar {

    @SuppressWarnings("unchecked")
    public static void setupSerialization() {
        SpongeDataManager dataManager = SpongeDataManager.getInstance();

        // TileEntities
        dataManager.registerBuilder(Banner.class, new SpongeBannerBuilder());
        dataManager.registerBuilder(PatternLayer.class, new SpongePatternLayerBuilder());
        dataManager.registerBuilder(org.spongepowered.api.block.entity.carrier.BrewingStand.class, new SpongeBrewingStandBuilder());
        dataManager.registerBuilder(org.spongepowered.api.block.entity.carrier.chest.Chest.class, new SpongeChestBuilder());
        dataManager.registerBuilder(CommandBlock.class, new SpongeCommandBlockBuilder());
        dataManager.registerBuilder(org.spongepowered.api.block.entity.Comparator.class, new SpongeComparatorBuilder());
        dataManager.registerBuilder(org.spongepowered.api.block.entity.DaylightDetector.class, new SpongeDaylightBuilder());
        dataManager.registerBuilder(org.spongepowered.api.block.entity.carrier.Dispenser.class, new SpongeDispenserBuilder());
        dataManager.registerBuilder(org.spongepowered.api.block.entity.carrier.Dropper.class, new SpongeDropperBuilder());
        dataManager.registerBuilder(org.spongepowered.api.block.entity.EnchantmentTable.class, new SpongeEnchantmentTableBuilder());
        dataManager.registerBuilder(org.spongepowered.api.block.entity.EnderChest.class, new SpongeEnderChestBuilder());
        dataManager.registerBuilder(org.spongepowered.api.block.entity.EndPortal.class, new SpongeEndPortalBuilder());
        dataManager.registerBuilder(FurnaceBlockEntity.class, new SpongeFurnaceBuilder());
        dataManager.registerBuilder(org.spongepowered.api.block.entity.carrier.Hopper.class, new SpongeHopperBuilder());
        dataManager.registerBuilder(org.spongepowered.api.block.entity.MobSpawner.class, new SpongeMobSpawnerBuilder());
        dataManager.registerBuilder(Sign.class, new SpongeSignBuilder());
        dataManager.registerBuilder(Skull.class, new SpongeSkullBuilder());
        dataManager.registerBuilder(org.spongepowered.api.block.entity.carrier.Beacon.class, new SpongeBeaconBuilder());
        dataManager.registerBuilder(LocatableBlock.class, new SpongeLocatableBlockBuilder());

        // Block stuff
        dataManager.registerBuilder(BlockSnapshot.class, SpongeBlockSnapshotBuilder.pooled());
        dataManager.registerBuilder(BlockState.class, new SpongeBlockStateBuilder());
        dataManager.registerBuilderAndImpl(ImmutableTreeData.class, ImmutableSpongeTreeData.class, new ImmutableSpongeTreeDataBuilder());

        // Entity stuff
        dataManager.registerBuilder(EntitySnapshot.class, new SpongeEntitySnapshotBuilder());
        dataManager.registerBuilder(EntityArchetype.class, new SpongeEntityArchetypeBuilder());

        // ItemStack stuff
        dataManager.registerBuilder(ItemStack.class, new SpongeItemStackBuilder());
        dataManager.registerBuilder(ItemStackSnapshot.class, new SpongeItemStackSnapshotBuilder());
        dataManager.registerBuilder(Enchantment.class, new SpongeEnchantmentBuilder());
        dataManager.registerBuilderAndImpl(ImmutableEnchantmentData.class, ImmutableSpongeEnchantmentData.class,
                new ImmutableItemEnchantmentDataBuilder());
        dataManager.registerBuilder(FireworkEffect.class, new SpongeFireworkEffectDataBuilder());

        // Text stuff
        dataManager.registerBuilder(Text.class, new TextConfigSerializer());
        dataManager.registerBuilder(BookView.class, new BookViewDataBuilder());
        dataManager.registerBuilder(FluidStackSnapshot.class, new SpongeFluidStackSnapshotBuilder());

        // Effects stuff
        dataManager.registerBuilder(ParticleEffect.class, new SpongeParticleEffectBuilder());

        // Util stuff
        dataManager.registerBuilder(VariableAmount.BaseAndAddition.class, new BaseAndAdditionBuilder());
        dataManager.registerBuilder(VariableAmount.BaseAndVariance.class, new BaseAndVarianceBuilder());
        dataManager.registerBuilder(VariableAmount.Fixed.class, new FixedBuilder());
        dataManager.registerBuilder(VariableAmount.OptionalAmount.class, new OptionalVarianceBuilder());

        dataManager.registerBuilder(Location.class, new LocationBuilder());
        dataManager.registerBuilder(SpongePlayerData.class, new SpongePlayerData.Builder());

        dataManager.registerBuilder(GameProfile.class, new SpongeGameProfileBuilder());

        dataManager.registerBuilder(Color.class, new Color.Builder());
        dataManager.registerBuilder(TradeOffer.class, new SpongeTradeOfferBuilder());

        dataManager.registerBuilder(PotionEffect.class, new SpongePotionBuilder());

        // Content Updaters
        dataManager.registerContentUpdater(BlockState.class, new SpongeBlockStateMetaContentUpdater());
        final InvisibilityDataAddVanishUpdater invisibilityUpdater = new InvisibilityDataAddVanishUpdater();
        dataManager.registerContentUpdater(InvisibilityData.class, invisibilityUpdater);
        dataManager.registerContentUpdater(ImmutableInvisibilityData.class, invisibilityUpdater);
        dataManager.registerContentUpdater(SpongeInvisibilityData.class, invisibilityUpdater);
        dataManager.registerContentUpdater(ImmutableSpongeInvisibilityData.class, invisibilityUpdater);
        final PotionEffectContentUpdater potionUpdater = new PotionEffectContentUpdater();
        dataManager.registerContentUpdater(PotionEffect.class, potionUpdater);
        dataManager.registerContentUpdater(PotionEffectData.class, potionUpdater);
        dataManager.registerContentUpdater(ImmutablePotionEffectData.class, potionUpdater);
        dataManager.registerContentUpdater(SpongePotionEffectData.class, potionUpdater);
        dataManager.registerContentUpdater(ImmutableSpongePotionEffectData.class, potionUpdater);
        dataManager.registerContentUpdater(ItemStackSnapshot.class, new ItemStackSnapshotDuplicateManipulatorUpdater());

        // Content Updaters for Custom Data
        dataManager.registerContentUpdater(Mutable.class, new LegacyCustomDataClassContentUpdater());

    }


}
