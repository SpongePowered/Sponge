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
public class GlobalGeneralCategory extends GeneralCategory {

    @Setting(value = "file-io-thread-sleep",
            comment = "If 'true', sleeping between chunk saves will be enabled, beware of memory issues.")
    private boolean fileIOThreadSleep = false;

    @Setting(value = "check-file-when-saving-sponge-data-file", comment = ""
            + "If 'true', Sponge will try to ensure that its data is on disk\n"
            + "when saving the 'level_sponge.dat' file,\n"
            + "but this may cause a slight performance hit.\n\n"
            + "Setting this to 'false' will mean Sponge does not\n"
            + "perform any consistency checks, but you may end up\n"
            + "with corrupt data if an unexpected failure occurs on your server,\n"
            + "requiring restoring this file from backup.")
    private boolean checkFileWhenSavingSpongeDataFile = true;

    public GlobalGeneralCategory() {
    }

    public boolean getFileIOThreadSleep() {
        return this.fileIOThreadSleep;
    }

    public boolean isCheckFileWhenSavingSpongeDataFile() {
        return this.checkFileWhenSavingSpongeDataFile;
    }

}
