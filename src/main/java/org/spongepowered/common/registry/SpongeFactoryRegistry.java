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

import co.aikar.timings.TimingsFactory;
import com.google.inject.Singleton;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import org.spongepowered.api.advancement.criteria.AdvancementCriterion;
import org.spongepowered.api.adventure.Audiences;
import org.spongepowered.api.adventure.SpongeComponents;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.command.parameter.managed.standard.VariableValueParameters;
import org.spongepowered.api.command.registrar.tree.CommandTreeBuilder;
import org.spongepowered.api.command.selector.Selector;
import org.spongepowered.common.command.selector.SpongeSelectorFactory;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.network.channel.ChannelExceptionHandler;
import org.spongepowered.api.registry.DuplicateRegistrationException;
import org.spongepowered.api.registry.FactoryRegistry;
import org.spongepowered.api.registry.UnknownTypeException;
import org.spongepowered.api.resourcepack.ResourcePack;
import org.spongepowered.api.util.Range;
import org.spongepowered.api.util.Transform;
import org.spongepowered.common.adventure.AudienceFactory;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.api.world.ServerLocation;
import org.spongepowered.common.command.parameter.SpongeParameterValueFactory;
import org.spongepowered.common.command.parameter.managed.factory.SpongeVariableValueParameterBuilderFactory;
import org.spongepowered.common.command.registrar.tree.SpongeRootCommandTreeBuilderFactory;
import org.spongepowered.common.item.SpongeItemStackSnapshot;
import org.spongepowered.common.network.channel.SpongeChannelExceptionHandlers;
import org.spongepowered.common.registry.type.advancement.SpongeAdvancementCriterionFactory;
import org.spongepowered.common.relocate.co.aikar.timings.SpongeTimingsFactory;
import org.spongepowered.common.resourcepack.SpongeResourcePackFactory;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.common.command.manager.SpongeCommandCauseFactory;
import org.spongepowered.common.util.SpongeRange;
import org.spongepowered.common.util.SpongeTransformFactory;
import org.spongepowered.common.world.SpongeServerLocationFactory;

import java.util.Map;

@Singleton
@SuppressWarnings("unchecked")
public final class SpongeFactoryRegistry implements FactoryRegistry {

    private final Map<Class<?>, Object> factories;

    public SpongeFactoryRegistry() {
        this.factories = new Object2ObjectArrayMap<>();
    }

    @Override
    public <T> T provideFactory(Class<T> clazz) throws UnknownTypeException {
        checkNotNull(clazz);

        final Object duck = this.factories.get(clazz);
        if (duck == null) {
            throw new UnknownTypeException(String.format("Type '%s' has no factory registered!", clazz));
        }

        return (T) duck;
    }

    public <T> SpongeFactoryRegistry registerFactory(Class<T> factoryClass, T factory) {
        checkNotNull(factoryClass);
        checkNotNull(factory);

        if (this.factories.containsKey(factoryClass)) {
            throw new DuplicateRegistrationException(String.format("Type '%s' has already been registered as a factory!", factoryClass));
        }

        this.factories.put(factoryClass, factory);
        return this;
    }

    public void registerDefaultFactories() {
        this
            .registerFactory(Audiences.Factory.class, new AudienceFactory())
            .registerFactory(AdvancementCriterion.Factory.class, SpongeAdvancementCriterionFactory.INSTANCE)
            .registerFactory(CommandCause.Factory.class, SpongeCommandCauseFactory.INSTANCE)
            .registerFactory(CommandTreeBuilder.RootNodeFactory.class, SpongeRootCommandTreeBuilderFactory.INSTANCE)
            .registerFactory(ItemStackSnapshot.Factory.class, () -> SpongeItemStackSnapshot.EMPTY)
            .registerFactory(Parameter.Value.Factory.class, SpongeParameterValueFactory.INSTANCE)
            .registerFactory(ResourcePack.Factory.class, SpongeResourcePackFactory.INSTANCE)
            .registerFactory(ServerLocation.Factory.class, SpongeServerLocationFactory.INSTANCE)
            .registerFactory(SpongeComponents.Factory.class, new SpongeAdventure.Factory())
            .registerFactory(TimingsFactory.class, SpongeTimingsFactory.INSTANCE)
            .registerFactory(Transform.Factory.class, SpongeTransformFactory.INSTANCE)
            .registerFactory(VariableValueParameters.Factory.class, SpongeVariableValueParameterBuilderFactory.INSTANCE)
            .registerFactory(ChannelExceptionHandler.Factory.class, SpongeChannelExceptionHandlers.INSTANCE)
            .registerFactory(Selector.Factory.class, SpongeSelectorFactory.INSTANCE)
            .registerFactory(Range.Factory.class, SpongeRange.FACTORY_INSTANCE)
        ;
    }

}
