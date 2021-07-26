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
package org.spongepowered.fabric.applaunch.plugin;

import org.apache.logging.log4j.Logger;
import org.spongepowered.common.applaunch.plugin.PluginPlatform;
import org.spongepowered.plugin.PluginCandidate;
import org.spongepowered.plugin.PluginEnvironment;
import org.spongepowered.plugin.PluginKeys;
import org.spongepowered.plugin.PluginLanguageService;
import org.spongepowered.plugin.PluginResource;
import org.spongepowered.plugin.PluginResourceLocatorService;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class FabricPluginPlatform implements PluginPlatform {

	private final PluginEnvironment pluginEnvironment;
	private final Map<String, PluginResourceLocatorService<PluginResource>> locatorServices;
	private final Map<String, PluginLanguageService<PluginResource>> languageServices;

	private final Map<String, Set<PluginResource>> locatorResources;
	private final Map<PluginLanguageService<PluginResource>, List<PluginCandidate<PluginResource>>> pluginCandidates;

	public FabricPluginPlatform(PluginEnvironment pluginEnvironment) {
		this.pluginEnvironment = pluginEnvironment;
		this.locatorServices = new HashMap<>();
		this.languageServices = new HashMap<>();
		this.locatorResources = new HashMap<>();
		this.pluginCandidates = new IdentityHashMap<>();
	}

	@Override
	public String version() {
		return this.pluginEnvironment.blackboard().get(PluginKeys.VERSION).orElse("dev");
	}

	@Override
	public void setVersion(String version) {
		this.pluginEnvironment.blackboard().getOrCreate(PluginKeys.VERSION, () -> version);
	}

	@Override
	public Logger logger() {
		return this.pluginEnvironment.logger();
	}

	@Override
	public Path baseDirectory() {
		// TODO: change orElse part
		return this.pluginEnvironment.blackboard().get(PluginKeys.BASE_DIRECTORY).orElse(Paths.get("."));
	}

	@Override
	public void setBaseDirectory(Path baseDirectory) {
		this.pluginEnvironment.blackboard().getOrCreate(PluginKeys.BASE_DIRECTORY, () -> baseDirectory);
	}

	@Override
	public List<Path> pluginDirectories() {
		return this.pluginEnvironment.blackboard().get(PluginKeys.PLUGIN_DIRECTORIES).orElseThrow(() -> new IllegalStateException("No plugin "
				+ "directories have been specified!"));
	}

	@Override
	public void setPluginDirectories(List<Path> pluginDirectories) {
		this.pluginEnvironment.blackboard().getOrCreate(PluginKeys.PLUGIN_DIRECTORIES, () -> pluginDirectories);
	}

	public PluginEnvironment getPluginEnvironment() {
		return this.pluginEnvironment;
	}

	public Map<String, PluginResourceLocatorService<PluginResource>> getLocatorServices() {
		return Collections.unmodifiableMap(this.locatorServices);
	}

	public Map<String, PluginLanguageService<PluginResource>> getLanguageServices() {
		return Collections.unmodifiableMap(this.languageServices);
	}

	public Map<String, Set<PluginResource>> getResources() {
		return Collections.unmodifiableMap(this.locatorResources);
	}

	public Map<PluginLanguageService<PluginResource>, List<PluginCandidate<PluginResource>>> getCandidates() {
		return this.pluginCandidates;
	}

	private void initialize() {
		for (final Map.Entry<String, PluginLanguageService<PluginResource>> entry : this.languageServices.entrySet()) {
			entry.getValue().initialize(this.pluginEnvironment);
		}
	}

	private void discoverLocatorServices() {
		@SuppressWarnings("unchecked")
		final ServiceLoader<PluginResourceLocatorService<PluginResource>> serviceLoader = (ServiceLoader<PluginResourceLocatorService<PluginResource>>) (Object) ServiceLoader.load(
				PluginResourceLocatorService.class, FabricPluginPlatform.class.getClassLoader());

		for (final Iterator<PluginResourceLocatorService<PluginResource>> it = serviceLoader.iterator(); it.hasNext(); ) {
			final PluginResourceLocatorService<PluginResource> next;

			try {
				next = it.next();
			} catch (final ServiceConfigurationError e) {
				this.pluginEnvironment.logger().error("Error encountered initializing plugin resource locator!", e);
				continue;
			}

			this.locatorServices.put(next.name(), next);
		}
	}

	private void discoverLanguageServices() {
		@SuppressWarnings("unchecked")
		final ServiceLoader<PluginLanguageService<PluginResource>> serviceLoader = (ServiceLoader<PluginLanguageService<PluginResource>>) (Object) ServiceLoader.load(
				PluginLanguageService.class, FabricPluginPlatform.class.getClassLoader());

		for (final Iterator<PluginLanguageService<PluginResource>> it = serviceLoader.iterator(); it.hasNext(); ) {
			final PluginLanguageService<PluginResource> next;

			try {
				next = it.next();
			} catch (final ServiceConfigurationError e) {
				this.pluginEnvironment.logger().error("Error encountered initializing plugin language service!", e);
				continue;
			}

			this.languageServices.put(next.name(), next);
		}
	}

	private void locatePluginResources() {
		for (final Map.Entry<String, PluginResourceLocatorService<PluginResource>> locatorEntry : this.locatorServices.entrySet()) {
			final PluginResourceLocatorService<PluginResource> locatorService = locatorEntry.getValue();
			final Set<PluginResource> resources = locatorService.locatePluginResources(this.pluginEnvironment);
			if (!resources.isEmpty()) {
				this.locatorResources.put(locatorEntry.getKey(), resources);
			}
		}
	}

	private void createPluginCandidates() {
		for (final Map.Entry<String, PluginLanguageService<PluginResource>> languageEntry : this.languageServices.entrySet()) {
			final PluginLanguageService<PluginResource> languageService = languageEntry.getValue();
			for (final Map.Entry<String, Set<PluginResource>> resourcesEntry : this.locatorResources.entrySet()) {

				for (final PluginResource pluginResource : resourcesEntry.getValue()) {
					try {
						final List<PluginCandidate<PluginResource>> candidates =
								languageService.createPluginCandidates(this.pluginEnvironment, pluginResource);
						if (candidates.isEmpty()) {
							continue;
						}

						this.pluginCandidates.computeIfAbsent(languageService, k -> new LinkedList<>()).addAll(candidates);
					} catch (ClassCastException ignored) {
					}
				}
			}
		}
	}

	private String codeSource() {
		try {
			return this.getClass().getProtectionDomain().getCodeSource().getLocation().toString();
		} catch (final Throwable th) {
			return "Unknown";
		}
	}

	public void configure() {
		this.logger().info("SpongePowered PLUGIN Subsystem Version={} Source={}",
				this.version(), this.codeSource());

		this.discoverLocatorServices();
		this.getLocatorServices().forEach((k, v) -> this.pluginEnvironment
				.logger().info("Plugin resource locator '{}' found.", k));
		this.discoverLanguageServices();
		this.getLanguageServices().forEach((k, v) -> this.pluginEnvironment
				.logger().info("Plugin language loader '{}' found.", k));

		this.initialize();

		this.locatePluginResources();
		this.createPluginCandidates();

		// TODO: process plugins
	}
}
