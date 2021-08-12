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
package org.spongepowered.forge.mixin.core.minecraftforge.fml;

import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.loading.moddiscovery.ModInfo;
import net.minecraftforge.forgespi.language.IModInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.launch.Launch;
import org.spongepowered.forge.bridge.minecraftforge.fml.ModContainerBridge;
import org.spongepowered.forge.launch.ForgeLaunch;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.metadata.PluginMetadata;

import java.util.function.Supplier;

@Mixin(value = ModContainer.class, remap = false)
public abstract class ModContainerMixin_Forge implements ModContainerBridge, PluginContainer {

    // @formatter:off
    @Shadow @Final protected IModInfo modInfo;
    @Shadow @Final protected String modId;
    // @formatter:on

    @Shadow protected Supplier<?> contextExtension;
    private Logger forge$logger;
    private PluginMetadata forge$pluginMetadata;

    @Override
    public PluginMetadata metadata() {
        if (this.forge$pluginMetadata == null) {
            this.forge$pluginMetadata = ((ForgeLaunch) Launch.instance()).metadataForMod((ModInfo) this.modInfo);
        }

        return this.forge$pluginMetadata;
    }

    @Override
    public void bridge$setPluginMetadata(final PluginMetadata metadata) {
        this.forge$pluginMetadata = metadata;
    }

    @Override
    public Logger logger() {
        if (this.forge$logger == null) {
            this.forge$logger = LogManager.getLogger(this.modId);
        }

        return this.forge$logger;
    }

    @Override
    public Object instance() {
        return this.contextExtension.get();
    }
}
