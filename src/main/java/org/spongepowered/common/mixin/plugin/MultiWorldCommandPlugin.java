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
package org.spongepowered.common.mixin.plugin;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.common.SpongeImpl;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MultiWorldCommandPlugin implements IMixinConfigPlugin {

    private static final Pattern MIXIN_OPTION_EXTRACTOR = Pattern.compile("^[a-z.]+\\.Command(?<option>[A-Za-z]+)Mixin_(MultiWorld|Global)Command$");

    private boolean configState;

    @Override
    public void onLoad(String mixinPackage) {
        this.configState = mixinPackage.endsWith(".multiworld");
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        Matcher matcher = MIXIN_OPTION_EXTRACTOR.matcher(mixinClassName);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Mixin class name \"" + mixinClassName + "\" does not match form expected by MIXIN_OPTION_EXTRACTOR.");
        }
        String name = matcher.group("option").toLowerCase(Locale.ENGLISH);

        // MCP class name for the seed command is CommandShowSeed so we need to rename it manually
        if (name.equals("showseed")) {
            name = "seed";
        }

        Map<String, Boolean> patches = SpongeImpl.getGlobalConfigAdapter().getConfig().getCommands().getMultiWorldCommandPatches();
        if (!patches.containsKey(name)) {
            patches.put(name, true);
            SpongeImpl.getGlobalConfigAdapter().save();
            return this.configState;
        }

        return patches.get(name) == this.configState;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

}
