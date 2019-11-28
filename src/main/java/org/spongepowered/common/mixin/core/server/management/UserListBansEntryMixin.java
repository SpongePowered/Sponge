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
package org.spongepowered.common.mixin.core.server.management;

import com.mojang.authlib.GameProfile;
import org.spongepowered.api.util.ban.Ban;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.Date;
import net.minecraft.server.management.ProfileBanEntry;

@Mixin(ProfileBanEntry.class)
public abstract class UserListBansEntryMixin extends UserListEntryBanMixin<GameProfile> {

    /**
     * Fix {@link Ban#getCreationDate()} by passing the correct date to the
     * super constructor. (Minecraft incorrectly passes endDate as startDate)
     */
    @ModifyArgs(method = "<init>(Lcom/mojang/authlib/GameProfile;Ljava/util/Date;Ljava/lang/String;Ljava/util/Date;Ljava/lang/String;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/management/UserListEntryBan;<init>(Ljava/lang/Object;Ljava/util/Date;Ljava/lang/String;Ljava/util/Date;Ljava/lang/String;)V"))
    private static void fixCreationDate(final Args args, final com.mojang.authlib.GameProfile profile, final Date startDate, final String banner,
        final Date endDate, final String banReason) {
        //    Vanilla uses the following normally, see that we change to use the start date appropriately
        //    super(profile, endDate,   banner, endDate, banReason);
        args.setAll(profile, startDate, banner, endDate, banReason);
    }

}
