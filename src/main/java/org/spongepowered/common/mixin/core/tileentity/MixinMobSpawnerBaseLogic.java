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
package org.spongepowered.common.mixin.core.tileentity;

import com.flowpowered.math.vector.Vector3d;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.tileentity.MobSpawnerBaseLogic;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.cause.entity.spawn.SpawnCause;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.event.entity.ConstructEntityEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.registry.type.entity.EntityTypeRegistryModule;

@Mixin(MobSpawnerBaseLogic.class)
public abstract class MixinMobSpawnerBaseLogic {

    private static final String WORLD_SPAWN_ENTITY = "Lnet/minecraft/world/World;spawnEntityInWorld(Lnet/minecraft/entity/Entity;)Z";
    private static final String ENTITY_LIST_CREATE_ENTITY =
            "Lnet/minecraft/entity/EntityList;createEntityByName(Ljava/lang/String;Lnet/minecraft/world/World;)Lnet/minecraft/entity/Entity;";

    @Shadow private int spawnRange;

    @Shadow public abstract BlockPos getSpawnerPosition();
    @Shadow public abstract World getSpawnerWorld();


    private double posX;
    private double posY;
    private double posZ;

    /**
     * @author gabizou - January 30th, 2016
     *
     * Redirects to throw a ConstructEntityEvent.PRE
     * @param entityName
     * @param world
     * @return
     */
    @Redirect(method = "updateSpawner", at = @At(value = "INVOKE", target = ENTITY_LIST_CREATE_ENTITY))
    private Entity onConstruct(String entityName, World world) {
        EntityType type = EntityTypeRegistryModule.getInstance().getForClass(EntityList.stringToClassMapping.get(entityName));
        if (type == null) {
            return null;
        }
        BlockPos blockPos = getSpawnerPosition();

        this.posX = (double)blockPos.getX() + (this.getSpawnerWorld().rand.nextDouble() - this.getSpawnerWorld().rand.nextDouble())
                                              * (double)this.spawnRange + 0.5D;
        this.posY = (double)(blockPos.getY() + this.getSpawnerWorld().rand.nextInt(3) - 1);
        this.posZ = (double)blockPos.getZ() + (this.getSpawnerWorld().rand.nextDouble() - this.getSpawnerWorld().rand.nextDouble())
                                              * (double)this.spawnRange + 0.5D;

        SpawnCause cause = SpawnCause.builder().type(SpawnTypes.MOB_SPAWNER).build(); // We can't use MobspawnerSpawnCause yet.
        Transform<org.spongepowered.api.world.World> transform = new Transform<>(
                ((org.spongepowered.api.world.World) getSpawnerWorld()), new Vector3d(this.posX, this.posY, this.posZ));
        ConstructEntityEvent.Pre event = SpongeEventFactory.createConstructEntityEventPre(Cause.of(NamedCause.source(cause)), type, transform);
        SpongeImpl.postEvent(event);
        if (event.isCancelled()) {
            return null;
        }
        return EntityList.createEntityByName(entityName, world);

    }

    @Inject(method = "updateSpawner", at = @At(value = "INVOKE", target = "Lnet/minecraft/tileentity/MobSpawnerBaseLogic;resetTimer()V"))
    private void onReset(CallbackInfo callbackInfo) {
        this.posX = 0;
        this.posY = 0;
        this.posZ = 0;
    }

    /**
     * Redirects the entity spawn to ours since we know the cause already.
     *
     * @param world
     * @param entity
     * @return
     */
    @Redirect(method = "spawnNewEntity", at = @At(value = "INVOKE", target = WORLD_SPAWN_ENTITY))
    private boolean onEntitySpawn(World world, Entity entity) {
        // TODO include the spawner data once implemented.
        SpawnCause cause = SpawnCause.builder().type(SpawnTypes.MOB_SPAWNER).build(); // We can't use MobspawnerSpawnCause yet.
        return ((org.spongepowered.api.world.World) world).spawnEntity(((org.spongepowered.api.entity.Entity) entity),
                Cause.of(NamedCause.source(cause)));
    }

}
