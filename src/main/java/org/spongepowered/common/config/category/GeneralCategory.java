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
package org.spongepowered.common.config.category;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class GeneralCategory extends ConfigCategory {

    @Setting(value = "plugins-dir", comment = ""
            + "Additional directory to search for plugins, relative to the\n"
            + "execution root or specified as an absolute path.\n"
            + "Note that the default: '${CANONICAL_MODS_DIR}/plugins'\n"
            + "is going to search for a plugins folder in the mods directory.\n"
            + "If you wish for the plugins folder to reside in the root game\n"
            + "directory, change the value to '${CANONICAL_GAME_DIR}/plugins'.")
    private String pluginsDir = "${CANONICAL_MODS_DIR}/plugins";
    @Setting(value = "config-dir", comment = ""
            + "The directory for Sponge plugin configurations, relative to the\n"
            + "execution root or specified as an absolute path.\n"
            + "Note that the default: '${CANONICAL_GAME_DIR}/config'\n"
            + "is going to use the 'config' directory in the root game directory.\n"
            + "If you wish for plugin configs to reside within a child of the configuration\n"
            + "directory, change the value to, for example, '${CANONICAL_CONFIG_DIR}/sponge/plugins'.\n"
            + "Note: It is not recommended to set this to '${CANONICAL_CONFIG_DIR}/sponge', as there is\n"
            + "a possibility that plugin configurations can conflict the Sponge core configurations.\n")
    private String configDir = "${CANONICAL_GAME_DIR}/config";

    public String pluginsDir() {
        return this.pluginsDir;
    }

    public void setPluginsDir(String pluginsDir) {
        this.pluginsDir = pluginsDir;
    }

    public String configDir() {
        return this.configDir;
    }

    public void setConfigDir(String configDir) {
        this.configDir = configDir;
    }

}
