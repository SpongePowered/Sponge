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
package org.spongepowered.common.applaunch.config.common;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Setting;

@ConfigSerializable
public final class ServicesCategory {

    public static final String UNSPECIFIED = "?";

    @Setting("plugins")
    @Comment("Services specified here can be implemented by plugins. To ensure that a"
             + "specific plugin implements a given service, set the relevant option to its"
             + "plugin ID. If you wish to use Sponge's default for a given service, use"
             + "'sponge' as the ID.\n\n"
             + "If the plugin ID is unknown, or the option is set to '?', all plugins will"
             + "be given the opportunity to register their service. If multiple plugins"
             + "attempt to register, one will be picked in an implementation dependent way."
             + "If no plugins attempt to register a service, the Sponge default will be used"
             + "if one exists.\n\n"
             + "No Sponge default service exists for the Economy service.")
    public final ServicePluginSubCategory plugins = new ServicePluginSubCategory();

    @ConfigSerializable
    public static final class ServicePluginSubCategory {

        @Setting("ban-service")
        @Comment("Specifies the plugin that will provide the ban service")
        public String banService = ServicesCategory.UNSPECIFIED;

        @Setting("context-service")
        @Comment("Specifies the plugin that will provide the context service")
        public String contextService = UNSPECIFIED;

        @Setting("economy-service")
        @Comment("Specifies the plugin that will provide the economy service")
        public String economyService = ServicesCategory.UNSPECIFIED;

        @Setting("pagination-service")
        @Comment("Specifies the plugin that will provide the pagination service")
        public String paginationService = ServicesCategory.UNSPECIFIED;

        @Setting("permission-service")
        @Comment("Specifies the plugin that will provide the permission service")
        public String permissionService = ServicesCategory.UNSPECIFIED;

        @Setting("whitelist-service")
        @Comment("Specifies the plugin that will provide the whitelist service")
        public String whitelistService = ServicesCategory.UNSPECIFIED;
    }

}
