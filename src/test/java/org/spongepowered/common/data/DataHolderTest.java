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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.entity.BlockEntity;
import org.spongepowered.api.block.entity.BlockEntityArchetype;
import org.spongepowered.api.block.entity.BlockEntityTypes;
import org.spongepowered.api.data.DataRegistration;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.SerializableDataHolder;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.world.DefaultWorldKeys;
import org.spongepowered.api.world.chunk.WorldChunk;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.api.world.server.storage.ServerWorldProperties;
import org.spongepowered.common.util.Constants;
import org.spongepowered.math.vector.Vector3d;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class DataHolderTest {

    private static final Key<Value<String>> STRING_KEY = Key.from(ResourceKey.of("test", "string"), String.class);

    static {
        SpongeDataManager.INSTANCE.registerCustomDataRegistration((SpongeDataRegistration) DataRegistration.of(DataHolderTest.STRING_KEY, ItemStack.class, BlockEntity.class, Entity.class, WorldChunk.class, ServerWorldProperties.class));
    }

    public static Stream<Arguments> testEntity() {
        final ServerWorld world = Sponge.server().worldManager().world(DefaultWorldKeys.DEFAULT).get();

        return Stream.of(
            DataHolderTest.testEntityArguments(() -> ItemStack.of(ItemTypes.DIAMOND), c -> ItemStack.builder().fromContainer(c).build()),
            DataHolderTest.testEntityArguments(() -> BlockEntityArchetype.builder().blockEntity(BlockEntityTypes.CHEST).state(BlockTypes.CHEST.get().defaultState()).build(), c -> BlockEntityArchetype.builder().build(c).orElseThrow()),
            DataHolderTest.testEntityArguments(() -> world.createEntity(EntityTypes.CAT, Vector3d.ZERO), c -> world.createEntity(c).orElseThrow())
        );
    }

    private static <T extends SerializableDataHolder.Mutable> Arguments testEntityArguments(final Supplier<T> supplier, final Function<DataContainer, T> function) {
        return Arguments.of(supplier, function);
    }

    @ParameterizedTest
    @MethodSource
    public <T extends SerializableDataHolder.Mutable> void testEntity(final Supplier<T> supplier, final Function<DataContainer, T> function) {
        final var input = "test" + System.currentTimeMillis();

        final T holder = supplier.get();
        final var offerResult = holder.offer(DataHolderTest.STRING_KEY, input);

        Assertions.assertEquals(DataTransactionResult.successResult(Value.immutableOf(DataHolderTest.STRING_KEY, input)), offerResult);
        Assertions.assertEquals(Optional.of(input), holder.get(DataHolderTest.STRING_KEY));

        final DataContainer originalContainer = holder.toContainer();

        final T copy = function.apply(originalContainer);

        final DataContainer copyContainer = copy.toContainer();

        originalContainer.remove(Constants.Entity.V2.DATA.then(Constants.Entity.ENTITY_UUID));
        copyContainer.remove(Constants.Entity.V2.DATA.then(Constants.Entity.ENTITY_UUID));

        Assertions.assertEquals(originalContainer, copyContainer);
    }
}
