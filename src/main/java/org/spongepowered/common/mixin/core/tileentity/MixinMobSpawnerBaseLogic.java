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
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.MobSpawnerBaseLogic;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import org.apache.logging.log4j.Level;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.event.entity.ConstructEntityEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.util.PrettyPrinter;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.registry.type.entity.EntityTypeRegistryModule;

@Mixin(MobSpawnerBaseLogic.class)
public abstract class MixinMobSpawnerBaseLogic {

    private static final String
            ANVIL_CHUNK_LOADER_READ_ENTITY =
            "Lnet/minecraft/world/chunk/storage/AnvilChunkLoader;readWorldEntityPos(Lnet/minecraft/nbt/NBTTagCompound;Lnet/minecraft/world/World;DDDZ)Lnet/minecraft/entity/Entity;";

    @Shadow private int spawnRange;

    @Shadow public abstract BlockPos getSpawnerPosition();
    @Shadow public abstract World getSpawnerWorld();

    /**
     * @author gabizou - January 30th, 2016
     * @author gabizou - Updated April 10th, 2016 - Update for 1.9 since it's passed to the AnvilChunkLoader
     *
     * Redirects to throw a ConstructEntityEvent.PRE
     * @param world
     * @return
     */
    @Redirect(method = "updateSpawner", at = @At(value = "INVOKE", target = ANVIL_CHUNK_LOADER_READ_ENTITY))
    private Entity onConstruct(NBTTagCompound compound, World world, double x, double y, double z, boolean doesNotForceSpawn) {
        return readEntityFromCompoundAtWorld(compound, world, x, y, z, doesNotForceSpawn);

    }

    /**
     * @author gabizou - April 10th, 2016
     *
     * This is close to a verbatim copy of {@link AnvilChunkLoader#readWorldEntityPos(NBTTagCompound, World, double, double, double, boolean)}
     * with the added bonus of throwing events before entities are constructed with appropriate causes.
     *
     * @param compound The compound of the entity to spawn with
     * @param world The world to spawn at
     * @param x The x position
     * @param y The y position
     * @param z The z position
     * @param attemptToSpawn If false, the entity is not going to be spawned into the world yet
     * @return The entity, if successfully created
     */
    private static Entity readEntityFromCompoundAtWorld(NBTTagCompound compound, World world, double x, double y, double z, boolean attemptToSpawn) {
        final String entityTypeString = compound.getString(NbtDataUtil.ENTITY_TYPE_ID);
        final Class<? extends Entity> clazz = SpongeImplHooks.getEntityClass(new ResourceLocation(entityTypeString));
        if (clazz == null) {
            final PrettyPrinter printer = new PrettyPrinter(60).add("Unknown Entity for MobSpawners").centre().hr()
                .addWrapped(60, "Sponge has found a MobSpawner attempting to locate potentially"
                                + "a foreign entity type for a MobSpawner, unfortunately, there isn't a"
                                + "way to get around the deserialization process looking up unregistered"
                                + "entity types. This may be a bug with a mod or sponge.")
                .add("%s : %s", "Entity Name", entityTypeString)
                .add();
            PhaseTracker.getInstance().generateVersionInfo(printer);
            printer.trace(System.err, SpongeImpl.getLogger(), Level.WARN);
            return null;
        }
        EntityType type = EntityTypeRegistryModule.getInstance().getForClass(clazz);
        if (type == null) {
            return null;
        }
        try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            Sponge.getCauseStackManager().addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.MOB_SPAWNER);
            Transform<org.spongepowered.api.world.World> transform = new Transform<>(
                    ((org.spongepowered.api.world.World) world), new Vector3d(x, y, z));
            ConstructEntityEvent.Pre event = SpongeEventFactory.createConstructEntityEventPre(Sponge.getCauseStackManager().getCurrentCause(), type, transform);
            SpongeImpl.postEvent(event);
            if (event.isCancelled()) {
                return null;
            }
        }
        Entity entity;
        try {
            entity = EntityList.createEntityFromNBT(compound, world);
        } catch (Exception e) {
            return null;
        }

        if (entity == null) {
            return null;
        }

        entity.setLocationAndAngles(x, y, z, entity.rotationYaw, entity.rotationPitch);

        if (attemptToSpawn && !world.spawnEntity(entity)) {
            return null;
        }


        if (compound.hasKey(NbtDataUtil.Minecraft.PASSENGERS, NbtDataUtil.TAG_LIST)) {
            final NBTTagList passengerList = compound.getTagList(NbtDataUtil.Minecraft.PASSENGERS, NbtDataUtil.TAG_COMPOUND);

            for (int i = 0; i < passengerList.tagCount(); i++) {
                final Entity passenger = readEntityFromCompoundAtWorld(passengerList.getCompoundTagAt(i), world, x, y, z, attemptToSpawn);
                if (passenger != null) {
                    passenger.startRiding(entity, true);
                }
            }
        }
        return entity;
    }

}
