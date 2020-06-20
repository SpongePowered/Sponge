/*
 * This file is part of plugin-spi, licensed under the MIT License (MIT).
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
package org.spongepowered.common.launch.plugin.config.section;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.util.List;
import java.util.Map;

@ConfigSerializable
public final class PluginSection {

    @Setting(value = "id")
    private String id;

    @Setting(value = "name")
    private String name;

    @Setting(value = "version")
    private String version;

    @Setting(value = "main-class")
    private String mainClass;

    @Setting(value = "description")
    private String description;

    @Setting(value = "links")
    private LinksSection linksSection;

    @Setting(value = "contributors")
    private List<ContributorSection> contributorSections;

    @Setting(value = "dependencies")
    private List<DependencySection> dependencySections;

    @Setting(value = "extra")
    private Map<String, String> extra;

    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getVersion() {
        return this.version;
    }

    public String getMainClass() {
        return this.mainClass;
    }

    public String getDescription() {
        return this.description;
    }

    public LinksSection getLinksSection() {
        return this.linksSection;
    }

    public List<ContributorSection> getContributorSections() {
        return this.contributorSections;
    }

    public List<DependencySection> getDependencySections() {
        return this.dependencySections;
    }

    public Map<String, String> getExtraMetadata() {
        return this.extra;
    }
}
