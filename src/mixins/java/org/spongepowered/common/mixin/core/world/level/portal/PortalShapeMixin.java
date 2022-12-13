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
package org.spongepowered.common.mixin.core.world.level.portal;

import net.minecraft.BlockUtil;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.level.portal.PortalShape;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.api.world.portal.Portal;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.world.portal.NetherPortalType;
import org.spongepowered.common.world.portal.SpongePortalInfo;

@Mixin(PortalShape.class)
public abstract class PortalShapeMixin {

    @Redirect(method = "createPortalInfo", at = @At(value = "NEW", target = "net/minecraft/world/level/portal/PortalInfo"))
    private static PortalInfo impl$createSpongePortalInfo(final Vec3 var1, final Vec3 var2, final float var3, final float var4,
        final ServerLevel $$0, final BlockUtil.FoundRectangle $$1, final Direction.Axis $$2,
        final Vec3 $$3, final Entity $$4, final Vec3 $$5, final float $$6, final float $$7) {
        final Portal portal = NetherPortalType.portalObjectFromRectangle((ServerWorld) $$0, $$1);
        return new SpongePortalInfo(var1, var2, var3, var4, portal);
    }

}
