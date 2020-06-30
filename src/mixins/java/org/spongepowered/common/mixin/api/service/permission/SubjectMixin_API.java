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
package org.spongepowered.common.mixin.api.service.permission;

import net.minecraft.entity.item.minecart.MinecartCommandBlockEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.rcon.RConConsoleSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.CommandBlockTileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.service.permission.SpongeBridgeSubject;

/**
 * Mixin to provide a common implementation of subject that refers to the
 * installed permissions service for a subject.
 */
@Mixin(value = {ServerPlayerEntity.class, CommandBlockTileEntity.class, MinecartCommandBlockEntity.class, MinecraftServer.class, RConConsoleSource.class},
        targets = {"net/minecraft/tileentity/SignTileEntity$1", "net/minecraft/tileentity/SignTileEntity$2"})
public abstract class SubjectMixin_API implements SpongeBridgeSubject {
}
