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
package org.spongepowered.common.mixin.core.world.chunk.storage;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.collect.Maps;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraft.world.chunk.storage.RegionFileCache;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.cause.entity.spawn.SpawnCause;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.event.entity.ConstructEntityEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.entity.PlayerTracker;
import org.spongepowered.common.interfaces.IMixinChunk;
import org.spongepowered.common.interfaces.world.IMixinAnvilChunkLoader;
import org.spongepowered.common.registry.type.entity.EntityTypeRegistryModule;

import java.io.File;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

@Mixin(AnvilChunkLoader.class)
@Implements(@Interface(iface = IMixinAnvilChunkLoader.class, prefix = "loader$"))
public abstract class MixinAnvilChunkLoader implements IMixinAnvilChunkLoader {

    private static final String ENTITY_LIST_CREATE_FROM_NBT =
            "Lnet/minecraft/entity/EntityList;createEntityFromNBT(Lnet/minecraft/nbt/NBTTagCompound;Lnet/minecraft/world/World;)Lnet/minecraft/entity/Entity;";

    @Shadow @Final private Set<ChunkPos> pendingAnvilChunksCoordinates;
    @Shadow @Final private Map<ChunkPos, NBTTagCompound> chunksToRemove;
    @Shadow @Final private File chunkSaveLocation;

    @Inject(method = "writeChunkToNBT", at = @At(value = "RETURN"))
    public void onWriteChunkToNBT(net.minecraft.world.chunk.Chunk chunkIn, World worldIn, NBTTagCompound compound, CallbackInfo ci) {
        IMixinChunk chunk = (IMixinChunk) chunkIn;

        // Add tracked block positions
        if (chunk.getTrackedShortPlayerPositions().size() > 0 || chunk.getTrackedIntPlayerPositions().size() > 0) {
            NBTTagCompound trackedNbt = new NBTTagCompound();
            NBTTagList positions = new NBTTagList();
            trackedNbt.setTag(NbtDataUtil.SPONGE_BLOCK_POS_TABLE, positions);
            compound.setTag(NbtDataUtil.SPONGE_DATA, trackedNbt);

            for (Map.Entry<Short, PlayerTracker> mapEntry : chunk.getTrackedShortPlayerPositions().entrySet()) {
                Short pos = mapEntry.getKey();
                Integer ownerUniqueIdIndex = mapEntry.getValue().ownerIndex;
                Integer notifierUniqueIdIndex = mapEntry.getValue().notifierIndex;
                NBTTagCompound valueNbt = new NBTTagCompound();
                valueNbt.setInteger("owner", ownerUniqueIdIndex);
                valueNbt.setInteger("notifier", notifierUniqueIdIndex);
                valueNbt.setShort("pos", pos);
                positions.appendTag(valueNbt);
            }

            for (Map.Entry<Integer, PlayerTracker> mapEntry : chunk.getTrackedIntPlayerPositions().entrySet()) {
                Integer pos = mapEntry.getKey();
                Integer ownerUniqueIdIndex = mapEntry.getValue().ownerIndex;
                Integer notifierUniqueIdIndex = mapEntry.getValue().notifierIndex;
                NBTTagCompound valueNbt = new NBTTagCompound();
                valueNbt.setInteger("owner", ownerUniqueIdIndex);
                valueNbt.setInteger("notifier", notifierUniqueIdIndex);
                valueNbt.setInteger("ipos", pos);
                positions.appendTag(valueNbt);
            }
        }
    }

    @Inject(method = "readChunkFromNBT", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/NBTTagCompound;getIntArray(Ljava/lang/String;)[I", shift = At.Shift.BEFORE), locals = LocalCapture.CAPTURE_FAILHARD)
    public void onReadChunkFromNBT(World worldIn, NBTTagCompound compound, CallbackInfoReturnable<net.minecraft.world.chunk.Chunk> ci, int chunkX, int chunkZ, net.minecraft.world.chunk.Chunk chunkIn) {
        if (compound.hasKey(NbtDataUtil.SPONGE_DATA)) {
            Map<Integer, PlayerTracker> trackedIntPlayerPositions = Maps.newHashMap();
            Map<Short, PlayerTracker> trackedShortPlayerPositions = Maps.newHashMap();
            NBTTagList positions = compound.getCompoundTag(NbtDataUtil.SPONGE_DATA).getTagList(NbtDataUtil.SPONGE_BLOCK_POS_TABLE, 10);
            IMixinChunk chunk = (IMixinChunk) chunkIn;
            for (int i = 0; i < positions.tagCount(); i++) {
                NBTTagCompound valueNbt = positions.getCompoundTagAt(i);
                boolean isShortPos = valueNbt.hasKey("pos");
                PlayerTracker tracker = new PlayerTracker();
                if (valueNbt.hasKey("owner")) {
                    tracker.ownerIndex = valueNbt.getInteger("owner");
                } else if (valueNbt.hasKey("uuid")) { // Migrate old data, remove in future
                    tracker.ownerIndex = valueNbt.getInteger("uuid");
                }
                if (valueNbt.hasKey("notifier")) {
                    tracker.notifierIndex = valueNbt.getInteger("notifier");
                }

                if (tracker.notifierIndex != -1 || tracker.ownerIndex != -1) {
                    if (isShortPos) {
                        trackedShortPlayerPositions.put(valueNbt.getShort("pos"), tracker);
                    } else {
                        trackedIntPlayerPositions.put(valueNbt.getInteger("ipos"), tracker);
                    }
                }
            }
            chunk.setTrackedIntPlayerPositions(trackedIntPlayerPositions);
            chunk.setTrackedShortPlayerPositions(trackedShortPlayerPositions);
        }
    }

    /**
     * @author gabizou - January 30th, 2016
     *
     * Attempts to redirect EntityList spawning an entity. Forge rewrites this method to
     * handle it in a different method, so this will not actually inject in SpongeForge.
     *
     * @param compound
     * @param world
     * @return
     */
    @Redirect(method = "readChunkFromNBT(Lnet/minecraft/world/World;Lnet/minecraft/nbt/NBTTagCompound;)Lnet/minecraft/world/chunk/Chunk;",
            at = @At(value = "INVOKE", target = ENTITY_LIST_CREATE_FROM_NBT), require = 0, expect = 0)
    private Entity onReadEntity(NBTTagCompound compound, World world) {
        if ("Minecart".equals(compound.getString(NbtDataUtil.ENTITY_TYPE_ID))) {
            compound.setString(NbtDataUtil.ENTITY_TYPE_ID,
                    EntityMinecart.Type.values()[compound.getInteger(NbtDataUtil.MINECART_TYPE)].getName());
            compound.removeTag(NbtDataUtil.MINECART_TYPE);
        }
        Class<? extends Entity> entityClass = SpongeImplHooks.getEntityClass(new ResourceLocation(compound.getString(NbtDataUtil.ENTITY_TYPE_ID)));
        if (entityClass == null) {
            return null;
        }
        EntityType type = EntityTypeRegistryModule.getInstance().getForClass(entityClass);
        if (type == null) {
            return null;
        }
        NBTTagList positionList = compound.getTagList(NbtDataUtil.ENTITY_POSITION, NbtDataUtil.TAG_DOUBLE);
        NBTTagList rotationList = compound.getTagList(NbtDataUtil.ENTITY_ROTATION, NbtDataUtil.TAG_FLOAT);
        Vector3d position = new Vector3d(positionList.getDoubleAt(0), positionList.getDoubleAt(1), positionList.getDoubleAt(2));
        Vector3d rotation = new Vector3d(rotationList.getFloatAt(0), rotationList.getFloatAt(1), 0);
        Transform<org.spongepowered.api.world.World> transform = new Transform<>((org.spongepowered.api.world.World) world, position, rotation);
        SpawnCause cause = SpawnCause.builder().type(SpawnTypes.CHUNK_LOAD).build();
        ConstructEntityEvent.Pre event = SpongeEventFactory.createConstructEntityEventPre(Cause.of(NamedCause.source(cause)), type, transform);
        SpongeImpl.postEvent(event);
        if (event.isCancelled()) {
            return null;
        }
        return EntityList.createEntityFromNBT(compound, world);
    }

    @Intrinsic // Forge method
    public boolean loader$chunkExists(World world, int x, int z) {
        ChunkPos chunkcoordintpair = new ChunkPos(x, z);

        if (this.pendingAnvilChunksCoordinates.contains(chunkcoordintpair)) {
            for (ChunkPos pendingChunkCoord : this.chunksToRemove.keySet()) {
                if (pendingChunkCoord.equals(chunkcoordintpair)) {
                    return true;
                }
            }
        }

        return RegionFileCache.getChunkInputStream(this.chunkSaveLocation, x, z) != null;
    }

    @Override
    public Path getWorldDir() {
        return this.chunkSaveLocation.toPath();
    }

}
