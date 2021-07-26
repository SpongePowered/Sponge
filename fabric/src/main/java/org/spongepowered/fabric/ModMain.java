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
package org.spongepowered.fabric;

import java.lang.reflect.InvocationTargetException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.PreInitEntrypoint;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;

public class ModMain implements PreLaunchEntrypoint, PreInitEntrypoint {
	private static final Logger LOGGER = LogManager.getLogger(ModMain.class);

	@Override
	public void onPreInit() {
		String[] args = FabricLoader.getInstance().getLaunchArguments(false);
		LOGGER.info("Invoking SpongeFabric Installer with args {}", (Object) args);

		invokeMain("org.spongepowered.fabric.installer.InstallerMain", args);

	}

	@Override
	public void onPreLaunch() {
		EnvType environment = FabricLoader.getInstance().getEnvironmentType();
		boolean dev = FabricLoader.getInstance().isDevelopmentEnvironment();

		if (environment.equals(EnvType.CLIENT) && dev) {
			LOGGER.info("Invoking SpongeFabric ClientLaunchDev");
			invokeMain("org.spongepowered.fabric.applaunch.handler.dev.ClientDevLaunchHandler", new String[]{});
		} else if (environment.equals(EnvType.SERVER) && dev) {
			LOGGER.info("Invoking SpongeFabric ServerLaunch");
			invokeMain("org.spongepowered.fabric.applaunch.handler.dev.ServerDevLaunchHandler", new String[]{});
		} if (environment.equals(EnvType.CLIENT) && !dev) {
			LOGGER.info("Invoking SpongeFabric ClientLaunchDev");
			invokeMain("org.spongepowered.fabric.applaunch.handler.prod.ClientProdLaunchHandler", new String[]{});
		} else if (environment.equals(EnvType.SERVER) && !dev) {
			LOGGER.info("Invoking SpongeFabric ServerLaunch");
			invokeMain("org.spongepowered.fabric.applaunch.handler.prod.ServerProdLaunchHandler", new String[]{});
		} else {
			throw new RuntimeException("Unknown environment type");
		}
	}

	private static void invokeMain(final String className, final String[] args) {
		try {
			Class.forName(className)
					.getMethod("main", String[].class)
					.invoke(null, (Object) args);
		} catch (final InvocationTargetException ex) {
			LOGGER.error("Failed to invoke main class {} due to an error", className, ex.getCause());
			System.exit(1);
		} catch (final ClassNotFoundException | NoSuchMethodException | IllegalAccessException ex) {
			LOGGER.error("Failed to invoke main class {} due to an error", className, ex);
			System.exit(1);
		}
	}
}
