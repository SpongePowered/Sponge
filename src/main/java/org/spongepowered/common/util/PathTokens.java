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
package org.spongepowered.common.util;

import com.google.common.collect.Maps;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.launch.SpongeLaunch;

import java.util.Map;

public final class PathTokens {

    /**
     * Token which contains the fully-qualified path to the game directory (profile root)
     */
    public static final String PATHTOKEN_CANONICAL_GAME_DIR = "CANONICAL_GAME_DIR";
    /**
     * Token which contains the fully-qualified path to FML's "mods" folder
     */
    public static final String PATHTOKEN_CANONICAL_MODS_DIR = "CANONICAL_MODS_DIR";
    /**
     * Token which contains the fully-qualified path to FML's "config" folder
     */
    public static final String PATHTOKEN_CANONICAL_CONFIG_DIR = "CANONICAL_CONFIG_DIR";
    /**
     * Token which contains the current minecraft version as a string
     */
    public static final String PATHTOKEN_MC_VERSION = "MC_VERSION";

    private PathTokens() {
    }

    public static String replace(String string) {
        final Map<String, String> tokens = getPathTokens();
        for (final Map.Entry<String, String> token : tokens.entrySet()) {
            string = string.replace(token.getKey(), token.getValue());
        }

        return string;
    }

    private static Map<String, String> getPathTokens() {
        final Map<String, String> tokens = Maps.newHashMap();
        tokens.put(formatToken(PATHTOKEN_CANONICAL_MODS_DIR), SpongeLaunch.getPluginsDir().toFile().getAbsolutePath());
        tokens.put(formatToken(PATHTOKEN_CANONICAL_GAME_DIR), SpongeLaunch.getGameDir().toFile().getAbsolutePath());
        tokens.put(formatToken(PATHTOKEN_CANONICAL_CONFIG_DIR), SpongeLaunch.getConfigDir().toFile().getAbsolutePath());
        tokens.put(formatToken(PATHTOKEN_MC_VERSION), SpongeImpl.MINECRAFT_VERSION.getName());
        return tokens;
    }

    private static String formatToken(String name) {
        return String.format("${%s}", name);
    }

}
