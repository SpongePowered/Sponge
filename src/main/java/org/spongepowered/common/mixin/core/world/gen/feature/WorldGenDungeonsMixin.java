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
package org.spongepowered.common.mixin.core.world.gen.feature;

import com.google.common.base.MoreObjects;
import net.minecraft.tileentity.MobSpawnerBaseLogic;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.gen.feature.WorldGenDungeons;
import org.spongepowered.api.entity.EntityArchetype;
import org.spongepowered.api.util.weighted.WeightedSerializableObject;
import org.spongepowered.api.util.weighted.WeightedTable;
import org.spongepowered.api.world.gen.populator.Dungeon;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.data.processor.common.SpawnerUtils;

@Mixin(WorldGenDungeons.class)
public abstract class WorldGenDungeonsMixin extends WorldGeneratorMixin {

    @Redirect(method = "generate", at = @At(value = "INVOKE", target = "Lnet/minecraft/tileentity/MobSpawnerBaseLogic;setEntityId(Lnet/minecraft/util/ResourceLocation;)V"))
    private void impl$updateDataAndChoices(final MobSpawnerBaseLogic logic, final ResourceLocation mobName) {
        if (((Dungeon) this).getMobSpawnerData().isPresent()) {
            // Use custom spawner data
            SpawnerUtils.applyData(logic, ((Dungeon) this).getMobSpawnerData().get());
            return;
        }

        if (((Dungeon) this).getChoices().isPresent()) {
            // Use custom choices
            final WeightedTable<EntityArchetype> choices = ((Dungeon) this).getChoices().get();
            final EntityArchetype entity = choices.get(logic.func_98271_a().field_73012_v).stream().findFirst().orElse(null);
            if (entity == null) {
                return; // No choices to choose from? Use default instead.
            }
            SpawnerUtils.setNextEntity(logic, new WeightedSerializableObject<>(entity, 1));
            return;
        }

        // Just use the given mobName
        logic.func_190894_a(mobName);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                      .add("Type", "Dungeon")
                      .add("PerChunk", ((Dungeon) this).getAttemptsPerChunk())
                      .add("Data", ((Dungeon) this).getMobSpawnerData())
                      .add("Choices", ((Dungeon) this).getChoices())
                      .toString();
    }

}
