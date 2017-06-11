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
/**
 * The various mixins in this package for command sources mirror the SpongeAPI command source interfaces.
 *
 * <dl>
 * <dt>{@link org.spongepowered.api.command.CommandSource}</dt>
 * <dd>{@link org.spongepowered.common.mixin.core.command.MixinCommandSource}
 * <dt>{@link org.spongepowered.api.command.source.CommandBlockSource}</dt>
 * <dd>{@link org.spongepowered.common.mixin.core.command.MixinCommandBlockSource}
 * <dt>{@link org.spongepowered.api.command.source.RconSource}</dt>
 * <dd>{@link org.spongepowered.common.mixin.core.network.rcon.MixinRConConsoleSource}
 * <dt>{@link org.spongepowered.api.command.source.SignSource}</dt>
 * <dd>{@link org.spongepowered.common.mixin.core.command.MixinSignCommandSender}
 * <dt>{@link org.spongepowered.api.command.source.ProxySource} via /execute
 * <dd>{@link org.spongepowered.common.mixin.core.command.MixinCommandExecuteAtSender}
 * </dl>
 *
 * In addition, {@link org.spongepowered.common.mixin.core.command.MixinBlockCommandBlockSender} and
 * {@link org.spongepowered.common.mixin.core.command.MixinMinecartCommandBlockSender} are for inner classes that are separate from the mixin that
 * actually implements their command source interfaces.
 */
@org.spongepowered.api.util.annotation.NonnullByDefault
package org.spongepowered.common.mixin.core.command;
