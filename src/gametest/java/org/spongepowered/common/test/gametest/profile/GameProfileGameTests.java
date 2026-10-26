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
package org.spongepowered.common.test.gametest.profile;

import net.minecraft.gametest.framework.GameTestHelper;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.profile.GameProfileCache;
import org.spongepowered.common.test.gametest.GameTest;
import org.spongepowered.common.test.gametest.GameTestGroup;

import java.util.UUID;

/**
 * The game test server's services hold a mock name to id resolver rather than
 * the cached one Sponge mixes into, so the profile manager must still have a
 * cache there for plugins that look up users.
 */
@GameTestGroup("profile")
public final class GameProfileGameTests {

    @GameTest(maxTicks = 1)
    void cacheAvailable(final GameTestHelper helper) {
        final GameProfileCache cache = Sponge.server().gameProfileManager().cache();
        helper.assertTrue(cache.findById(UUID.randomUUID()).isEmpty(), "An unknown id was found in the profile cache");
        // How plugins hit it first: the user manager reads the profile cache
        Sponge.server().userManager().streamAll().count();
        helper.succeed();
    }
}
