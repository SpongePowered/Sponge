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
package org.spongepowered.forge.applaunch.service;

import cpw.mods.modlauncher.Launcher;
import cpw.mods.modlauncher.api.IEnvironment;
import cpw.mods.modlauncher.api.ITransformationService;
import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.api.TypesafeMap;
import joptsimple.OptionSpec;
import joptsimple.OptionSpecBuilder;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.transformers.modlauncher.SuperclassChanger;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class SpongeForgeTransformationService implements ITransformationService {
    private OptionSpec<Boolean> checkHashes;
    private OptionSpec<String> librariesDirectoryName;

    @NonNull
    @Override
    public String name() {
        return "spongeforge";
    }

    @Override
    public void arguments(final BiFunction<String, String, OptionSpecBuilder> argumentBuilder) {
        this.checkHashes = argumentBuilder.apply("checkHashes", "Whether to check hashes when downloading libraries")
            .withOptionalArg()
            .ofType(Boolean.class)
            .defaultsTo(true);
        this.librariesDirectoryName = argumentBuilder.apply("librariesDir", "The directory to download SpongeForge libraries to")
            .withOptionalArg()
            .ofType(String.class)
            .defaultsTo("sponge-libraries");
    }

    @Override
    public void argumentValues(final OptionResult option) {
        Launcher.INSTANCE.environment().computePropertyIfAbsent(Keys.CHECK_LIBRARY_HASHES.get(), $ -> option.value(this.checkHashes));
        Launcher.INSTANCE.environment().computePropertyIfAbsent(Keys.LIBRARIES_DIRECTORY.get(),
            $ -> Launcher.INSTANCE.environment().getProperty(IEnvironment.Keys.GAMEDIR.get())
            .orElseThrow(() -> new IllegalStateException("No game directory was available"))
            .resolve(option.value(this.librariesDirectoryName)));
    }

    @Override
    public void initialize(final IEnvironment environment) {
        if (FMLEnvironment.production) {
            // TODO actually read this from the jar manifest
            environment.getProperty(SuperclassChanger.INSTANCE.get()).ifPresent(scc -> {
                scc.offerResource(SpongeForgeTransformationService.class.getResource("/common.superclasschange"), "SpongeForge injected");
                scc.offerResource(SpongeForgeTransformationService.class.getResource("/forge.superclasschange"), "SpongeForge injected");
            });
        }
    }

    @Override
    public void onLoad(final IEnvironment env, final Set<String> otherServices) {
    }

    @NonNull
    @Override
    @SuppressWarnings("rawtypes") // :(((((((((((((((
    public List<ITransformer> transformers() {
        return Collections.emptyList();
    }

    public static final class Keys {

        public static final Supplier<TypesafeMap.Key<Boolean>> CHECK_LIBRARY_HASHES = IEnvironment.buildKey("sponge:check_library_hashes", Boolean.class);
        public static final Supplier<TypesafeMap.Key<Path>> LIBRARIES_DIRECTORY = IEnvironment.buildKey("sponge:libraries_directory", Path.class);

        private Keys() {
        }

    }
}
