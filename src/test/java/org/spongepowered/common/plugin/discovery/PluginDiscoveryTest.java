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
package org.spongepowered.common.plugin.discovery;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.spongepowered.common.applaunch.AppLaunch;
import org.spongepowered.common.applaunch.plugin.discovery.PluginDiscovery;
import org.spongepowered.common.plugin.discovery.spi.DummyMetadataReader;
import org.spongepowered.plugin.PluginService;
import org.spongepowered.plugin.discovery.ResourceLoading;

import java.util.List;
import java.util.Optional;

public class PluginDiscoveryTest {

    @Test
    public void verifyDummyCandidate() {
        // locator
        Optional<PluginDiscovery.Candidate> candidate = AppLaunch.pluginPlatform().discovery().candidates().stream()
            .filter(c -> c.resource().property("dummy_key").isPresent()).findFirst();
        assertTrue(candidate.isPresent());
        assertEquals("dummy_value", candidate.get().resource().property("dummy_key").orElse(null));
        assertIterableEquals(List.of("dummy_locator"), candidate.get().locators().stream().map(PluginService::name).toList());

        // reader
        assertEquals(ResourceLoading.GAME_LIBRARY, candidate.get().loading());
        assertIterableEquals(List.of(DummyMetadataReader.DUMMY_METADATA), candidate.get().metadata());
    }
}
