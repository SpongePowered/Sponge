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
package org.spongepowered.common.test.gametest.registry;

import com.mojang.serialization.DynamicOps;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryDataLoader;
import org.spongepowered.common.test.gametest.GameTest;
import org.spongepowered.common.test.gametest.GameTestGroup;

/**
 * Encodes every synchronized registry the way the configuration phase does
 * for a joining client. A mixin that replaces an accessor a registry codec
 * reads only shows up there, as a "Network Protocol Error" on login.
 */
@GameTestGroup("registry")
public final class RegistrySyncGameTests {

    @GameTest(maxTicks = 1)
    void synchronizedRegistriesEncode(final GameTestHelper helper) {
        final RegistryAccess registries = helper.getLevel().getServer().registryAccess();
        final DynamicOps<Tag> ops = registries.createSerializationContext(NbtOps.INSTANCE);
        for (final RegistryDataLoader.RegistryData<?> data : RegistryDataLoader.SYNCHRONIZED_REGISTRIES) {
            // Class based test environments and their tests have no serialized form
            if (data.key() == Registries.TEST_ENVIRONMENT || data.key() == Registries.TEST_INSTANCE) {
                continue;
            }
            RegistrySyncGameTests.encode(ops, data, registries);
        }
        helper.succeed();
    }

    private static <T> void encode(final DynamicOps<Tag> ops, final RegistryDataLoader.RegistryData<T> data, final RegistryAccess registries) {
        registries.lookup(data.key()).ifPresent(registry -> registry.listElements().forEach(element ->
            data.elementCodec().encodeStart(ops, element.value())
                .getOrThrow(error -> new IllegalStateException("Failed to serialize " + element.key() + ": " + error))));
    }
}
