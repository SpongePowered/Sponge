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
package org.spongepowered.common.modlauncher.service;

import cpw.mods.modlauncher.api.IEnvironment;
import cpw.mods.modlauncher.api.ITransformationService;
import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.api.IncompatibleEnvironmentException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.spongepowered.common.launch.IExitHandler;
import org.spongepowered.common.launch.InternalLaunchService;
import org.spongepowered.common.launch.SpongeLaunch;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public class SuperClassTransformationService implements ITransformationService, InternalLaunchService {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final Marker SPONGE = MarkerManager.getMarker("sponge");
    private boolean hasForge = false;

    @Override
    public String name() {
        return "sponge";
    }

    @Override
    public void initialize(final IEnvironment environment) {
        LOGGER.debug(SPONGE, "Setting up Sponge Implementation");

        if (environment.findLaunchPlugin("fml").isPresent()) {
            this.hasForge = true;
        }
        LOGGER.debug(SPONGE, "Configuring Mixins for Sponge (Core Implementation)");
        SpongeLaunch.setupMixinEnvironment();
    }

    @Override
    public void beginScanning(final IEnvironment environment) {
        throw new UnsupportedOperationException("lolwut");
    }

    @Override
    public void onLoad(final IEnvironment env, final Set<String> otherServices) throws IncompatibleEnvironmentException {
        LOGGER.debug(SPONGE, "Loading Sponge Launch Service");
        SpongeLaunch.initializeLaunchService(this);

    }

    @Override
    public List<ITransformer> transformers() {
        return null;
    }

    @Override
    public void addJreExtensionsToClassPath() {

    }

    @Override
    public void registerSuperclassModification(final String targetClass, final String newSuperClass) {

    }

    @Override
    public Supplier<? extends IExitHandler> getExitHandler() {
        throw new UnsupportedOperationException("IMPLEMENT ME");
    }

    @Override
    public Supplier<? extends Logger> getLaunchLogger() {
        throw new UnsupportedOperationException("IMPLEMENT ME");
    }

    @Override
    public boolean isVanilla() {
        return !this.hasForge; // maybe?
    }
}
