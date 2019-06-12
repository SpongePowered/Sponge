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
package org.spongepowered.common.mixin.core.world;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.CaseFormat;
import com.google.common.base.MoreObjects;
import com.google.gson.JsonParseException;
import net.minecraft.world.WorldType;
import net.minecraft.world.gen.ChunkGeneratorSettings;
import net.minecraft.world.gen.FlatGeneratorInfo;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.GeneratorType;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.gen.WorldGenerator;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.data.persistence.JsonDataFormat;
import org.spongepowered.common.data.util.DataQueries;
import org.spongepowered.common.interfaces.world.ServerWorldBridge;
import org.spongepowered.common.registry.type.world.GeneratorTypeRegistryModule;

import java.io.IOException;

@NonnullByDefault
@Mixin(WorldType.class)
public abstract class MixinWorldType {

    @Shadow @Final private String name;
    @Shadow @Final private int id;

    @Inject(method = "<init>(ILjava/lang/String;)V", at = @At("RETURN"))
    private void onConstructSpongeRegister(int id, String name, CallbackInfo callbackInfo) {
        // Ensures that new world types are automatically registered with the registry module
        GeneratorTypeRegistryModule.getInstance().registerAdditionalCatalog(this);
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.name.hashCode();
        result = prime * result + this.id;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof WorldType)) {
            return false;
        }

        final WorldType other = (WorldType) obj;
        return this.name.equals(other.getName()) && this.id == other.getId();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", ((GeneratorType) this).getId())
                .add("name", this.name)
                .add("settings", ((GeneratorType) this).getGeneratorSettings())
                .toString();
    }
}
