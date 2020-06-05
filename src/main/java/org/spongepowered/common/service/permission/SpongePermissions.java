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
package org.spongepowered.common.service.permission;

import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.permission.SubjectData;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.common.entity.player.LoginPermissions;

import java.util.function.BiFunction;

public final class SpongePermissions {

    private static final String COMMAND_BLOCK_COMMAND = "";
    private static final String COMMAND_BLOCK_PERMISSION = "minecraft.commandblock";
    private static final int COMMAND_BLOCK_LEVEL = 2;
    private static final String SELECTOR_COMMAND = "@";
    private static final String SELECTOR_PERMISSION = "minecraft.selector";
    private static final int SELECTOR_LEVEL = 2;
    private static final String SPONGE_HELP_COMMAND = "sponge:help";
    static final String SPONGE_HELP_PERMISSION = "sponge.command.help";
    private static final int SPONGE_HELP_LEVEL = 0;

    private SpongePermissions() {
    }

    public static void populateMinecraftPermissions(final Subject subject) {
        // TODO: Populate command permissions - when commands are done.
    }

    public static void populateNonCommandPermissions(final SubjectData data, final BiFunction<Integer, String, Boolean> testPermission) {
        if (testPermission.apply(COMMAND_BLOCK_LEVEL, COMMAND_BLOCK_COMMAND)) {
            data.setPermission(SubjectData.GLOBAL_CONTEXT, COMMAND_BLOCK_PERMISSION, Tristate.TRUE);
        }
        if (testPermission.apply(SELECTOR_LEVEL, SELECTOR_COMMAND)) {
            data.setPermission(SubjectData.GLOBAL_CONTEXT, SELECTOR_PERMISSION, Tristate.TRUE);
        }
        if (testPermission.apply(SPONGE_HELP_LEVEL, SPONGE_HELP_COMMAND)) {
            data.setPermission(SubjectData.GLOBAL_CONTEXT, SPONGE_HELP_PERMISSION, Tristate.TRUE);
        }
        if (testPermission.apply(LoginPermissions.BYPASS_WHITELIST_LEVEL, LoginPermissions.BYPASS_WHITELIST_PERMISSION)) {
            data.setPermission(SubjectData.GLOBAL_CONTEXT, LoginPermissions.BYPASS_WHITELIST_PERMISSION, Tristate.TRUE);
        }
    }

}
