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
package org.spongepowered.common.advancement;

import com.google.gson.JsonObject;
import net.minecraft.util.SharedConstants;
import net.minecraft.util.registry.Registry;
import org.apache.commons.io.FileUtils;
import org.spongepowered.api.advancement.Advancement;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class SpongeAdvancementProvider {

    public static void registerAdvancements(final Registry<Advancement> advancements) {
        final Path datapackPluginAdvancements = Paths.get("world").resolve("datapacks").resolve("plugin-advancements");
        try {
            FileUtils.deleteDirectory(datapackPluginAdvancements.toFile());
        } catch (IOException e) {
            throw new IllegalStateException("Could not clear plugin-advancements datapack.", e);
        }
        for (Advancement advancement : advancements) {
            final net.minecraft.advancements.Advancement mcAdvancement = (net.minecraft.advancements.Advancement) advancement;
            SpongeAdvancementProvider.save(datapackPluginAdvancements, mcAdvancement);
        }
        if (!advancements.keySet().isEmpty()) {
            final Path packMeta = datapackPluginAdvancements.resolve("pack.mcmeta");
            final JsonObject packDataRoot = new JsonObject();
            final JsonObject packData = new JsonObject();
            packDataRoot.add("pack", packData);
            packData.addProperty("pack_format", SharedConstants.getCurrentVersion().getPackVersion());
            packData.addProperty("description", "Sponge Plugin provided Advancements");
            SpongeAdvancementProvider.saveToFile(packDataRoot, packMeta);
        }
    }

    private static void save(final Path datapackPath, final net.minecraft.advancements.Advancement advancement) {
        final Path namespacedData = datapackPath.resolve("data").resolve(advancement.getId().getNamespace());
        final Path advancementFile = namespacedData.resolve("advancements").resolve(advancement.getId().getPath() + ".json");
        SpongeAdvancementProvider.saveToFile(advancement.deconstruct().serializeToJson(), advancementFile);
    }

    private static void saveToFile(final JsonObject json, final Path pathIn) {
        try {
            Files.createDirectories(pathIn.getParent());
            try (BufferedWriter bufferedwriter = Files.newBufferedWriter(pathIn)) {
                bufferedwriter.write(json.toString());
            }
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
    }

}
