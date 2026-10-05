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
package org.spongepowered.common.data.provider.entity;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.registry.DefaultedRegistryValue;
import org.spongepowered.common.data.provider.DataProviderRegistrator;
import org.spongepowered.common.data.provider.DataProviderRegistratorBuilder;

import java.util.List;

public final class EntityDataProviders extends DataProviderRegistratorBuilder {

    public sealed interface KeyComponentProvider<A extends DefaultedRegistryValue, MC> {
        <T extends Entity> void applyToRegistrator(DataProviderRegistrator.MutableRegistrator<T> registrator);
    }

    public static List<KeyComponentProvider<?, ?>> of(KeyComponentProvider<?, ?>... providers) {
        return List.of(providers);
    }

    public static <A extends DefaultedRegistryValue, MC> KeyComponentProvider<A, MC> holderOf(
        Key<Value<A>> apiKey,
        DataComponentType<Holder<MC>> componentType,
        ResourceKey<Registry<MC>> resourceKey
    ) {
        return new HolderProvider<>(apiKey, componentType, resourceKey);
    }

    public static <A extends DefaultedRegistryValue, MC extends Enum<MC>> KeyComponentProvider<A, MC> enumOf(
        Key<Value<A>> apiKey,
        DataComponentType<MC> componentType
    ) {
        return new EnumProvider<>(apiKey, componentType);
    }

    @SuppressWarnings("unchecked")
    record HolderProvider<A extends DefaultedRegistryValue, MC>(
        Key<Value<A>> apiKey,
        DataComponentType<Holder<MC>> componentType,
        ResourceKey<Registry<MC>> resourceKey
    ) implements KeyComponentProvider<A, MC> {
        public <T extends Entity> void applyToRegistrator(DataProviderRegistrator.MutableRegistrator<T> registrator) {
            registrator.create(this.apiKey)
                .get(h -> (A) h.get(this.componentType).value())
                .setAnd((h, v) -> {
                    final var holder = h.level().registryAccess().lookup(this.resourceKey)
                        .map(r -> r.wrapAsHolder((MC) (Object) v));
                    if (holder.isEmpty()) {
                        return false;
                    }
                    h.setComponent(this.componentType, holder.get());
                    return true;
                });
        }
    }

    @SuppressWarnings("unchecked")
    record EnumProvider<A extends DefaultedRegistryValue, MC extends Enum<MC>>(
        Key<Value<A>> apiKey,
        DataComponentType<MC> componentType
    ) implements KeyComponentProvider<A, MC> {
        public <T extends Entity> void applyToRegistrator(DataProviderRegistrator.MutableRegistrator<T> registrator) {
            registrator.create(this.apiKey)
                .get(h -> (A) h.get(this.componentType))
                .set((h, v) -> h.setComponent(this.componentType, (MC) (Object) v));
        }
    }

    @Override
    public void registerProviders() {
        AbstractArrowData.register(this.registrator);
        AbstractChestedHorseData.register(this.registrator);
        AbstractHorseData.register(this.registrator);
        AbstractMinecartData.register(this.registrator);
        AbstractRaiderData.register(this.registrator);
        AbstractVillagerData.register(this.registrator);
        AgeableData.register(this.registrator);
        AggressiveData.register(this.registrator);
        AnimalData.register(this.registrator);
        AreaEffectCloudData.register(this.registrator);
        ArmorStandData.register(this.registrator);
        ArrowData.register(this.registrator);
        AxolotlData.register(this.registrator);
        BatData.register(this.registrator);
        BedData.register(this.registrator);
        BlazeData.register(this.registrator);
        BoatData.register(this.registrator);
        CatData.register(this.registrator);
        ChickenData.register(this.registrator);
        CommandBlockMinecartData.register(this.registrator);
        CowData.register(this.registrator);
        CreakingData.register(this.registrator);
        CreeperData.register(this.registrator);
        DamagingProjectileData.register(this.registrator);
        DolphinData.register(this.registrator);
        DisplayEntityData.register(this.registrator);
        EnderCrystalData.register(this.registrator);
        EnderDragonData.register(this.registrator);
        EndermanData.register(this.registrator);
        EndermiteData.register(this.registrator);
        EntityData.register(this.registrator);
        EvokerData.register(this.registrator);
        ExperienceOrbData.register(this.registrator);
        ExplosiveData.register(this.registrator);
        EyeOfEnderData.register(this.registrator);
        FallingBlockData.register(this.registrator);
        FireworkRocketData.register(this.registrator);
        FishingBobberData.register(this.registrator);
        FoxData.register(this.registrator);
        FrogData.register(this.registrator);
        FurnaceMinecartData.register(this.registrator);
        FusedExplosiveData.register(this.registrator);
        GrieferData.register(this.registrator);
        GuardianData.register(this.registrator);
        HangingData.register(this.registrator);
        HorseData.register(this.registrator);
        HumanData.register(this.registrator);
        IdentifiableData.register(this.registrator);
        InteractionData.register(this.registrator);
        InvulnerableData.register(this.registrator);
        IronGolemData.register(this.registrator);
        ItemData.register(this.registrator);
        ItemFrameData.register(this.registrator);
        LeashableData.register(this.registrator);
        LightningBoltData.register(this.registrator);
        LivingData.register(this.registrator);
        LlamaData.register(this.registrator);
        LocationTargetingData.register(this.registrator);
        MannequinData.register(this.registrator);
        MobData.register(this.registrator);
        MooshroomData.register(this.registrator);
        OcelotData.register(this.registrator);
        PaintingData.register(this.registrator);
        PandaData.register(this.registrator);
        ParrotData.register(this.registrator);
        PatrollerData.register(this.registrator);
        PhantomData.register(this.registrator);
        PigData.register(this.registrator);
        PillagerData.register(this.registrator);
        PlayerData.register(this.registrator);
        PolarBearData.register(this.registrator);
        PotionData.register(this.registrator);
        ProjectileData.register(this.registrator);
        PufferfishData.register(this.registrator);
        RabbitData.register(this.registrator);
        RavagerData.register(this.registrator);
        SalmonData.register(this.registrator);
        ServerPlayerData.register(this.registrator);
        SheepData.register(this.registrator);
        ShulkerBulletData.register(this.registrator);
        ShulkerData.register(this.registrator);
        SlimeData.register(this.registrator);
        SpellcastingIllagerData.register(this.registrator);
        SpiderData.register(this.registrator);
        StriderData.register(this.registrator);
        TadpoleData.register(this.registrator);
        TameableData.register(this.registrator);
        ThrowableItemProjectileData.register(this.registrator);
        TNTData.register(this.registrator);
        TraderLlamaData.register(this.registrator);
        TransientData.register(this.registrator);
        TropicalFishData.register(this.registrator);
        TurtleData.register(this.registrator);
        VanishableData.register(this.registrator);
        VexData.register(this.registrator);
        VillagerData.register(this.registrator);
        VindicatorData.register(this.registrator);
        WitherData.register(this.registrator);
        WolfData.register(this.registrator);
        ZombieData.register(this.registrator);
        ZombifiedPiglinData.register(this.registrator);
        ZombieVillagerData.register(this.registrator);
    }
}
