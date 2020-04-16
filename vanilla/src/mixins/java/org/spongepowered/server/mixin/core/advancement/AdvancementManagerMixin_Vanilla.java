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
package org.spongepowered.server.mixin.core.advancement;

import com.google.gson.Gson;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementList;
import net.minecraft.advancements.AdvancementManager;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.FilenameUtils;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.SpongeImpl;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

@Mixin(AdvancementManager.class)
public class AdvancementManagerMixin_Vanilla {

    @Shadow @Final private static Gson GSON;

    @Redirect(method = "reload", at = @At(value = "INVOKE", target =
            "Lnet/minecraft/advancements/AdvancementList;loadAdvancements(Ljava/util/Map;)V"))
    private void vanilla$LoadUpAdvancements(AdvancementList advancementList,
            Map<ResourceLocation, Advancement.Builder> advancements) {
        if (!SpongeImpl.isInitialized()) {
            return;
        }
        for (PluginContainer pluginContainer : Sponge.getPluginManager().getPlugins()) {
            // Skip the minecraft advancements
            if (pluginContainer.getId().equals("minecraft")) {
                continue;
            }
            final Optional<Path> optSource = pluginContainer.getSource();
            if (optSource.isPresent()) {
                final Path source = optSource.get();
                final String base = "assets/" + pluginContainer.getId() + "/advancements";
                Path root;
                if (Files.isDirectory(source)) {
                    root = source.resolve(base);
                } else {
                    try {
                        final FileSystem fileSystem = FileSystems.newFileSystem(source, null);
                        root = fileSystem.getPath("/" + base);
                    } catch (IOException e) {
                        SpongeImpl.getLogger().error("Error loading FileSystem from jar: ", e);
                        continue;
                    }
                }
                if (!Files.exists(root)) {
                    continue;
                }
                try {
                    Files.walk(root).forEach(path -> {
                        if (!FilenameUtils.getExtension(path.getFileName().toString()).equals("json")) {
                            return;
                        }
                        final Path relPath = root.relativize(path);
                        final String id = FilenameUtils.removeExtension(relPath.toString())
                                .replaceAll("\\\\", "/");
                        final ResourceLocation resourceLocation = new ResourceLocation(
                                pluginContainer.getId(), id);
                        if (!advancements.containsKey(resourceLocation)) {
                            try (BufferedReader reader = Files.newBufferedReader(path)) {
                                final Advancement.Builder advancementBuilder =
                                        JsonUtils.fromJson(GSON, reader, Advancement.Builder.class);
                                advancements.put(resourceLocation, advancementBuilder);
                            } catch (IOException e) {
                                SpongeImpl.getLogger().error("Failed to read advancement "
                                        + resourceLocation + " from path " + path, e);
                            }
                        }
                    });
                } catch (IOException e) {
                    SpongeImpl.getLogger().error("Failed to walk path: " + root, e);
                }
            }
        }
        advancementList.loadAdvancements(advancements);
    }
}
