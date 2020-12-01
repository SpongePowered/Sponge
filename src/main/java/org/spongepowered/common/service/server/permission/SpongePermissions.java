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
package org.spongepowered.common.service.server.permission;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.permission.SubjectData;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.common.entity.player.LoginPermissions;
import org.spongepowered.common.util.Constants;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BiFunction;

public final class SpongePermissions {

    private static final Set<String> REGISTERED_PERMISSIONS = new HashSet<>();

    private SpongePermissions() {
    }

    public static void populateNonCommandPermissions(final SubjectData data, final BiFunction<Integer, String, Boolean> testPermission) {
        if (testPermission.apply(Constants.Permissions.COMMAND_BLOCK_LEVEL, Constants.Command.COMMAND_BLOCK_COMMAND)) {
            data.setPermission(SubjectData.GLOBAL_CONTEXT, Constants.Permissions.COMMAND_BLOCK_PERMISSION, Tristate.TRUE);
        }
        if (testPermission.apply(Constants.Permissions.SELECTOR_LEVEL, Constants.Command.SELECTOR_COMMAND)) {
            data.setPermission(SubjectData.GLOBAL_CONTEXT, Constants.Permissions.SELECTOR_PERMISSION, Tristate.TRUE);
        }
        if (testPermission.apply(Constants.Permissions.SPONGE_HELP_LEVEL, Constants.Command.SPONGE_HELP_COMMAND)) {
            data.setPermission(SubjectData.GLOBAL_CONTEXT, Constants.Permissions.SPONGE_HELP_PERMISSION, Tristate.TRUE);
        }
        if (testPermission.apply(LoginPermissions.BYPASS_WHITELIST_LEVEL, LoginPermissions.BYPASS_WHITELIST_PERMISSION)) {
            data.setPermission(SubjectData.GLOBAL_CONTEXT, LoginPermissions.BYPASS_WHITELIST_PERMISSION, Tristate.TRUE);
        }
    }

    public static void registerPermission(final String permissionNode, final int opLevel) {
        if (SpongePermissions.REGISTERED_PERMISSIONS.add(permissionNode)) {
            final PermissionService service = Sponge.getServer().getServiceProvider().permissionService();
            if (opLevel == 0) {
                // register as a default permission
                 service.getDefaults()
                        .getTransientSubjectData()
                        .setPermission(SubjectData.GLOBAL_CONTEXT, permissionNode, Tristate.TRUE);
            }
            if (service instanceof SpongePermissionService) {
                ((SpongePermissionService) service).getGroupForOpLevel(opLevel).getTransientSubjectData()
                        .setPermission(SubjectData.GLOBAL_CONTEXT, permissionNode, Tristate.TRUE);
            }
        }
    }

}
