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

import co.aikar.timings.TimingsFactory;
import com.google.inject.Singleton;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.advancement.criteria.AdvancementCriterion;
import org.spongepowered.api.advancement.criteria.AndCriterion;
import org.spongepowered.api.advancement.criteria.OrCriterion;
import org.spongepowered.api.adventure.Audiences;
import org.spongepowered.api.adventure.SpongeComponents;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.command.parameter.managed.standard.VariableValueParameters;
import org.spongepowered.api.command.selector.Selector;
import org.spongepowered.api.data.DataManipulator;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.command.registrar.tree.CommandTreeNode;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.network.channel.ChannelExceptionHandler;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.profile.property.ProfileProperty;
import org.spongepowered.api.registry.DuplicateRegistrationException;
import org.spongepowered.api.registry.FactoryRegistry;
import org.spongepowered.api.registry.UnknownTypeException;
import org.spongepowered.api.resource.ResourcePath;
import org.spongepowered.api.resource.ResourceReloadListener;
import org.spongepowered.api.resource.pack.Pack;
import org.spongepowered.api.resourcepack.ResourcePack;
import org.spongepowered.api.state.StateMatcher;
import org.spongepowered.api.util.AABB;
import org.spongepowered.api.util.MinecraftDayTime;
import org.spongepowered.api.util.Range;
import org.spongepowered.api.util.Ticks;
import org.spongepowered.api.util.Transform;
import org.spongepowered.api.util.blockray.RayTrace;
import org.spongepowered.api.world.BlockChangeFlag;
import org.spongepowered.api.world.ServerLocation;
import org.spongepowered.common.advancement.criterion.SpongeAndCriterion;
import org.spongepowered.common.advancement.criterion.SpongeOrCriterion;
import org.spongepowered.common.adventure.AudiencesFactory;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.common.command.manager.SpongeCommandCauseFactory;
import org.spongepowered.common.command.parameter.SpongeParameterFactory;
import org.spongepowered.common.command.parameter.managed.factory.SpongeVariableValueParametersFactory;
import org.spongepowered.common.command.registrar.tree.builder.SpongeCommandTreeBuilderFactory;
import org.spongepowered.common.command.selector.SpongeSelectorFactory;
import org.spongepowered.common.data.manipulator.ImmutableDataManipulatorFactory;
import org.spongepowered.common.data.manipulator.MutableDataManipulatorFactory;
import org.spongepowered.common.data.value.SpongeValueFactory;
import org.spongepowered.common.event.tracking.BlockChangeFlagManager;
import org.spongepowered.common.item.SpongeItemStackSnapshot;
import org.spongepowered.common.network.channel.SpongeChannelExceptionHandlerFactory;
import org.spongepowered.common.profile.SpongeGameProfile;
import org.spongepowered.common.profile.SpongeProfilePropertyFactory;
import org.spongepowered.common.registry.type.advancement.SpongeAdvancementCriterionFactory;
import org.spongepowered.common.relocate.co.aikar.timings.SpongeTimingsFactory;
import org.spongepowered.common.resource.SpongeResourcePathFactory;
import org.spongepowered.common.resource.SpongeResourceReloadListenerFactory;
import org.spongepowered.common.resource.pack.SpongePackFactory;
import org.spongepowered.common.resourcepack.SpongeResourcePack;
import org.spongepowered.common.state.SpongeStateMatcherFactory;
import org.spongepowered.common.util.SpongeAABB;
import org.spongepowered.common.util.SpongeMinecraftDayTime;
import org.spongepowered.common.util.SpongeRange;
import org.spongepowered.common.util.SpongeTicks;
import org.spongepowered.common.util.SpongeTransform;
import org.spongepowered.common.util.raytrace.SpongeRayTraceFactory;
import org.spongepowered.common.world.SpongeServerLocation;

import java.util.Map;
import java.util.Objects;

@Singleton
@SuppressWarnings("unchecked")
public final class SpongeFactoryRegistry implements FactoryRegistry {

    private final Map<Class<?>, Object> factories;

    public SpongeFactoryRegistry() {
        this.factories = new Object2ObjectArrayMap<>();
    }

    @Override
    public <T> T provideFactory(final Class<T> clazz) throws UnknownTypeException {
        final Object duck = this.factories.get(clazz);
        if (duck == null) {
            throw new UnknownTypeException(String.format("Type '%s' has no factory registered!", clazz));
        }

        return (T) duck;
    }

    public <T> SpongeFactoryRegistry registerFactory(Class<T> factoryClass, T factory) {
        Objects.requireNonNull(factory);

        if (this.factories.containsKey(factoryClass)) {
            throw new DuplicateRegistrationException(String.format("Type '%s' has already been registered as a factory!", factoryClass));
        }

        this.factories.put(factoryClass, factory);
        return this;
    }

    public void registerDefaultFactories() {
        this
            .registerFactory(ResourceKey.Factory.class, new SpongeResourceKeyFactory())
            .registerFactory(Audiences.Factory.class, new AudiencesFactory())
            .registerFactory(AABB.Factory.class, new SpongeAABB.FactoryImpl())
            .registerFactory(AdvancementCriterion.Factory.class, new SpongeAdvancementCriterionFactory())
            .registerFactory(ResourceReloadListener.Factory.class, SpongeResourceReloadListenerFactory.INSTANCE)
            .registerFactory(Pack.Factory.class, SpongePackFactory.INSTANCE)
            .registerFactory(CommandCause.Factory.class, new SpongeCommandCauseFactory())
            .registerFactory(CommandTreeNode.NodeFactory.class, new SpongeCommandTreeBuilderFactory())
            .registerFactory(ItemStackSnapshot.Factory.class, () -> SpongeItemStackSnapshot.EMPTY)
            .registerFactory(Parameter.Value.Factory.class, new SpongeParameterFactory())
            .registerFactory(ResourcePack.Factory.class, new SpongeResourcePack.Factory())
            .registerFactory(ResourcePath.Factory.class, new SpongeResourcePathFactory())
            .registerFactory(ServerLocation.Factory.class, new SpongeServerLocation.Factory())
            .registerFactory(SpongeComponents.Factory.class, new SpongeAdventure.Factory())
            .registerFactory(TimingsFactory.class, new SpongeTimingsFactory())
            .registerFactory(Transform.Factory.class, new SpongeTransform.Factory())
            .registerFactory(VariableValueParameters.Factory.class, new SpongeVariableValueParametersFactory())
            .registerFactory(ChannelExceptionHandler.Factory.class, new SpongeChannelExceptionHandlerFactory())
            .registerFactory(Selector.Factory.class, new SpongeSelectorFactory())
            .registerFactory(Range.Factory.class, new SpongeRange.Factory())
            .registerFactory(Value.Factory.class, new SpongeValueFactory())
            .registerFactory(DataManipulator.Mutable.Factory.class, new MutableDataManipulatorFactory())
            .registerFactory(DataManipulator.Immutable.Factory.class, new ImmutableDataManipulatorFactory())
            .registerFactory(BlockChangeFlag.Factory.class, new BlockChangeFlagManager.Factory())
            .registerFactory(OrCriterion.Factory.class, new SpongeOrCriterion.Factory())
            .registerFactory(AndCriterion.Factory.class, new SpongeAndCriterion.Factory())
            .registerFactory(Ticks.Factory.class, new SpongeTicks.Factory())
            .registerFactory(MinecraftDayTime.Factory.class, new SpongeMinecraftDayTime.Factory())
            .registerFactory(GameProfile.Factory.class, new SpongeGameProfile.Factory())
            .registerFactory(ProfileProperty.Factory.class, new SpongeProfilePropertyFactory())
            .registerFactory(RayTrace.Factory.class, new SpongeRayTraceFactory())
            .registerFactory(StateMatcher.Factory.class, new SpongeStateMatcherFactory())
        ;
    }

}
