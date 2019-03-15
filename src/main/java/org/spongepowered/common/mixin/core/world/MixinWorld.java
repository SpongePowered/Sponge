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

import com.flowpowered.math.vector.Vector3i;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.profiler.Profiler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.EnumLightType;
import net.minecraft.world.GameRules;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.block.SpongeBlockSnapshotBuilder;
import org.spongepowered.common.data.type.SpongeTileEntityType;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.phase.block.BlockPhase;
import org.spongepowered.common.interfaces.block.tile.IMixinTileEntity;
import org.spongepowered.common.interfaces.entity.IMixinEntity;
import org.spongepowered.common.interfaces.entity.player.IMixinEntityPlayer;
import org.spongepowered.common.interfaces.util.math.IMixinBlockPos;
import org.spongepowered.common.interfaces.world.IMixinDimension;
import org.spongepowered.common.interfaces.world.IMixinWorld;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;
import org.spongepowered.common.interfaces.world.gen.IMixinChunkProviderServer;
import org.spongepowered.common.mixin.tileentityactivation.MixinWorldServer_TileEntityActivation;
import org.spongepowered.common.util.SpongeHooks;
import org.spongepowered.common.world.SpongeDimension;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.function.Predicate;

import javax.annotation.Nullable;

@NonnullByDefault
@Mixin(net.minecraft.world.World.class)
public abstract class MixinWorld implements World, IMixinWorld {

    private static final Vector3i BLOCK_MIN = new Vector3i(-30000000, 0, -30000000);
    private static final Vector3i BLOCK_MAX = new Vector3i(30000000, 256, 30000000).sub(Vector3i.ONE);
    private static final Vector3i BLOCK_SIZE = BLOCK_MAX.sub(BLOCK_MIN).add(Vector3i.ONE);
    private static final Vector3i BIOME_MIN = new Vector3i(BLOCK_MIN.getX(), 0, BLOCK_MIN.getZ());
    private static final Vector3i BIOME_MAX = new Vector3i(BLOCK_MAX.getX(), 256, BLOCK_MAX.getZ());
    private static final Vector3i BIOME_SIZE = BIOME_MAX.sub(BIOME_MIN).add(Vector3i.ONE);
    private static final String
            CHECK_NO_ENTITY_COLLISION =
            "checkNoEntityCollision(Lnet/minecraft/util/math/AxisAlignedBB;Lnet/minecraft/entity/Entity;)Z";
    private static final String
            GET_ENTITIES_WITHIN_AABB =
            "Lnet/minecraft/world/World;getEntitiesWithinAABBExcludingEntity(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/AxisAlignedBB;)Ljava/util/List;";
    public SpongeBlockSnapshotBuilder builder = new SpongeBlockSnapshotBuilder();

    @Nullable private Context worldContext;
    protected boolean processingExplosion = false;
    protected boolean isDefinitelyFake = false;
    protected boolean hasChecked = false;
    protected SpongeDimension spongeDimensionWrapper;

    // @formatter:off
    @Shadow @Final public boolean isRemote;
    @Shadow @Final public net.minecraft.world.dimension.Dimension dimension;
    @Shadow @Final public Random rand;
    @Shadow @Final public Profiler profiler;
    @Shadow @Final public List<EntityPlayer> playerEntities;
    @Shadow @Final public List<net.minecraft.entity.Entity> loadedEntityList;
    @Shadow @Final public List<net.minecraft.entity.Entity> weatherEffects;
    @Shadow @Final public List<net.minecraft.entity.Entity> unloadedEntityList;
    @Shadow @Final public List<net.minecraft.tileentity.TileEntity> loadedTileEntityList;
    @Shadow @Final public List<net.minecraft.tileentity.TileEntity> tickableTileEntities;
    @Shadow @Final public List<net.minecraft.tileentity.TileEntity> tileEntitiesToBeRemoved;
    @Shadow @Final private List<net.minecraft.tileentity.TileEntity> addedTileEntityList;
    @Shadow @Final protected ISaveHandler saveHandler;
    @Shadow public int[] lightUpdateBlockList;
    @Shadow public int seaLevel;

    @Shadow public boolean processingLoadedTiles;
    @Shadow protected WorldInfo worldInfo;
    @Shadow protected IChunkProvider chunkProvider;
    @Shadow @Final public net.minecraft.world.border.WorldBorder worldBorder;

    @Shadow protected int updateLCG;

    @Shadow protected abstract void tickPlayers();


    // To be overridden in MixinWorldServer_Lighting
    @Shadow public abstract int getRawLight(BlockPos pos, EnumLightType lightType);
    @Shadow public abstract int getSkylightSubtracted();
    @Shadow public abstract net.minecraft.world.chunk.Chunk getChunk(BlockPos pos);
    @Shadow public abstract WorldInfo getWorldInfo();
    @Shadow public abstract boolean addTileEntity(net.minecraft.tileentity.TileEntity tile);
    @Shadow public abstract void onEntityAdded(net.minecraft.entity.Entity entityIn);
    @Shadow public abstract void onEntityRemoved(net.minecraft.entity.Entity entityIn);
    @Shadow public void markChunkDirty(BlockPos pos, net.minecraft.tileentity.TileEntity unusedTileEntity){};
   // @Shadow public abstract List<Entity> getEntitiesInAABBexcluding(@Nullable net.minecraft.entity.Entity entityIn, AxisAlignedBB boundingBox, @Nullable Predicate <? super net.minecraft.entity.Entity > predicate);
    @Shadow public abstract boolean addWeatherEffect(net.minecraft.entity.Entity entityIn);
    @Shadow public abstract Biome getBiome(BlockPos pos);
    @Shadow public abstract net.minecraft.world.chunk.Chunk getChunk(int chunkX, int chunkZ);
    @Shadow public abstract <T extends net.minecraft.entity.Entity> List<T> getEntitiesWithinAABB(Class<? extends T> clazz, AxisAlignedBB aabb, @Nullable Predicate<? super T> filter);
    @Shadow public abstract MinecraftServer getServer();
    // Methods needed for MixinWorldServer & Tracking
    @Shadow public abstract boolean spawnEntity(net.minecraft.entity.Entity entity); // This is overridden in MixinWorldServer
    @Shadow public abstract void updateAllPlayersSleepingFlag();
    @Shadow public abstract boolean setBlockState(BlockPos pos, IBlockState state);
    @Shadow public abstract boolean setBlockState(BlockPos pos, IBlockState state, int flags);
    @Shadow public abstract void updateComparatorOutputLevel(BlockPos pos, Block blockIn);
    @Shadow public abstract void notifyBlockUpdate(BlockPos pos, IBlockState oldState, IBlockState newState, int flags);
    @Shadow public abstract void playSound(EntityPlayer p_184148_1_, double p_184148_2_, double p_184148_4_, double p_184148_6_, SoundEvent p_184148_8_, net.minecraft.util.SoundCategory p_184148_9_, float p_184148_10_, float p_184148_11_);
    @Shadow public abstract GameRules shadow$getGameRules();
    @Shadow public abstract boolean isRaining();
    @Shadow public abstract boolean isThundering();
    @Shadow public abstract boolean isRainingAt(BlockPos strikePosition);
    @Shadow public abstract DifficultyInstance getDifficultyForLocation(BlockPos pos);
    @Shadow public abstract void notifyLightSet(BlockPos pos);
    @Shadow @Nullable private net.minecraft.tileentity.TileEntity getPendingTileEntityAt(BlockPos p_189508_1_) {
        return null; // Shadowed
    }
    @Shadow protected abstract void playEvent(int i, BlockPos pos, int stateId);

    // @formatter:on

    @Shadow public abstract void tickEntity(final net.minecraft.entity.Entity entity);

    @Shadow
    public boolean isBlockModifiable(EntityPlayer player, BlockPos pos) {
        return true; // shadowed so we can call from MixinWorldServer in spongeforge.
    }

    @Inject(method = "<init>", at = @At(value = "RETURN"))
    public void onInit(CallbackInfo ci) {
        this.spongeDimensionWrapper = new SpongeDimension(this.dimension);
    }

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/dimension/Dimension;"
                                                                     + "createWorldBorder()Lnet/minecraft/world/border/WorldBorder;"))
    private net.minecraft.world.border.WorldBorder onCreateWorldBorder(net.minecraft.world.dimension.Dimension dimension) {
        if (this.isFake()) {
            return dimension.createWorldBorder();
        }
        return ((IMixinDimension) dimension).createServerWorldBorder();
    }

    // TODO - Migrate this to MixinIWorldReaderBase with an isFake check
    @SuppressWarnings("rawtypes")
    @Inject(method = "getCollisionBoxes(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/AxisAlignedBB;)Ljava/util/List;", at = @At("HEAD"), cancellable = true)
    public void onGetCollisionBoxes(net.minecraft.entity.Entity entity, AxisAlignedBB axis, CallbackInfoReturnable<List<AxisAlignedBB>> cir) {
        if (this.isFake() || entity == null) {
            return;
        }
        if (entity.world != null && !((IMixinWorld) entity.world).isFake() && SpongeHooks.checkBoundingBoxSize(entity, axis)) {
            // Removing misbehaved living entities
            cir.setReturnValue(new ArrayList<>());
        }
    }

    @Inject(method = "onEntityAdded", at = @At("TAIL"))
    private void onEntityAddedToWorldMarkAsTracked(net.minecraft.entity.Entity entityIn, CallbackInfo ci) {
        if (!this.isFake()) { // Only set the value if the entity is not fake
            EntityUtil.toMixin(entityIn).setTrackedInWorld(true);
        }
    }

    @Inject(method = "onEntityRemoved", at = @At("TAIL"))
    private void onEntityRemovedFromWorldMarkAsUntracked(net.minecraft.entity.Entity entityIn, CallbackInfo ci) {
        if (!this.isFake() || EntityUtil.toMixin(entityIn).isTrackedInWorld()) {
            EntityUtil.toMixin(entityIn).setTrackedInWorld(false);
        }
    }

    @Nullable
    @Override
    public EntityPlayer getClosestPlayerToEntityWhoAffectsSpawning(net.minecraft.entity.Entity entity, double distance) {
        return this.getClosestPlayerWhoAffectsSpawning(entity.posX, entity.posY, entity.posZ, distance);
    }

    @Nullable
    @Override
    public EntityPlayer getClosestPlayerWhoAffectsSpawning(double x, double y, double z, double distance) {
        double bestDistance = -1.0D;
        EntityPlayer result = null;

        for (Object entity : this.playerEntities) {
            EntityPlayer player = (EntityPlayer) entity;
            if (player == null || player.removed || !((IMixinEntityPlayer) player).affectsSpawning()) {
                continue;
            }

            double playerDistance = player.getDistanceSq(x, y, z);

            if ((distance < 0.0D || playerDistance < distance * distance) && (bestDistance == -1.0D || playerDistance < bestDistance)) {
                bestDistance = playerDistance;
                result = player;
            }
        }

        return result;
    }

    @Override
    public boolean isFake() {
        if (this.hasChecked) {
            return this.isDefinitelyFake;
        }
        this.isDefinitelyFake = this.isRemote || this.worldInfo == null || !(this instanceof IMixinWorldServer);
        this.hasChecked = true;
        return this.isDefinitelyFake;
    }

    @Redirect(method = "isAnyPlayerWithinRangeAt", at = @At(value = "INVOKE", target = "Lcom/google/common/base/Predicate;apply(Ljava/lang/Object;)Z", remap = false))
    public boolean onIsAnyPlayerWithinRangePredicate(com.google.common.base.Predicate<EntityPlayer> predicate, Object object) {
        EntityPlayer player = (EntityPlayer) object;
        return !(player.removed || !((IMixinEntityPlayer) player).affectsSpawning()) && predicate.apply(player);
    }

    // For invisibility
    @Redirect(method = CHECK_NO_ENTITY_COLLISION, at = @At(value = "INVOKE", target = GET_ENTITIES_WITHIN_AABB))
    public List<net.minecraft.entity.Entity> filterInvisibile(net.minecraft.world.World world, net.minecraft.entity.Entity entityIn,
            AxisAlignedBB axisAlignedBB) {
        List<net.minecraft.entity.Entity> entities = world.getEntitiesWithinAABBExcludingEntity(entityIn, axisAlignedBB);
        Iterator<net.minecraft.entity.Entity> iterator = entities.iterator();
        while (iterator.hasNext()) {
            net.minecraft.entity.Entity entity = iterator.next();
            if (((IMixinEntity) entity).isVanished() && ((IMixinEntity) entity).ignoresCollision()) {
                iterator.remove();
            }
        }
        return entities;
    }

    @Redirect(method = "getClosestPlayer", at = @At(value = "INVOKE", target = "Ljava/util/function/Predicate;test(Ljava/lang/Object;)Z", remap = false))
    private boolean onGetClosestPlayerCheck(Predicate<net.minecraft.entity.Entity> predicate, Object entityPlayer) {
        EntityPlayer player = (EntityPlayer) entityPlayer;
        IMixinEntity mixinEntity = (IMixinEntity) player;
        return predicate.test(player) && !mixinEntity.isVanished();
    }

    @Inject(method = "playSound(Lnet/minecraft/entity/player/EntityPlayer;DDDLnet/minecraft/util/SoundEvent;Lnet/minecraft/util/SoundCategory;FF)V", at = @At("HEAD"), cancellable = true)
    private void spongePlaySoundAtEntity(EntityPlayer entity, double x, double y, double z, SoundEvent name, net.minecraft.util.SoundCategory category, float volume, float pitch, CallbackInfo callbackInfo) {
        if (entity instanceof IMixinEntity) {
            if (((IMixinEntity) entity).isVanished()) {
                callbackInfo.cancel();
            }
        }
    }

    // These are overriden in MixinWorldServer where they should be.

    @Inject(method = "destroyBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;playEvent(ILnet/minecraft/util/math/BlockPos;I)V"), cancellable = true)
    public void onDestroyBlock(BlockPos pos, boolean dropBlock, CallbackInfoReturnable<Boolean> cir) {

    }

    @Redirect(method = "tickEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;tick()V"))
    protected void onUpdateWeatherEffect(net.minecraft.entity.Entity entityIn) {
        entityIn.tick();
    }

    @Redirect(method = "tickEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/ITickable;tick()V"))
    protected void onUpdateTileEntities(ITickable tile) {
        tile.tick();
    }

    @Redirect(method = "tickEntity(Lnet/minecraft/entity/Entity;Z)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;tick()V"))
    protected void onCallEntityUpdate(net.minecraft.entity.Entity entity) {
        entity.tick();
    }

    @Redirect(method = "tickEntity(Lnet/minecraft/entity/Entity;Z)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;updateRidden()V"))
    protected void onCallEntityRidingUpdate(net.minecraft.entity.Entity entity) {
        entity.updateRidden();
    }

    @Redirect(method = "addTileEntity",
            at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", remap = false),
            slice = @Slice(from = @At(value = "FIELD", target = "Lnet/minecraft/world/World;tickableTileEntities:Ljava/util/List;"),
                           to =   @At(value = "FIELD", target = "Lnet/minecraft/world/World;isRemote:Z")))
    private boolean onAddTileEntity(List<net.minecraft.tileentity.TileEntity> list, Object tile) {
        if (!this.isFake() && !canTileUpdate((net.minecraft.tileentity.TileEntity) tile)) {
            return false;
        }

        return list.add((net.minecraft.tileentity.TileEntity) tile);
    }

    private boolean canTileUpdate(net.minecraft.tileentity.TileEntity tile) {
        TileEntity spongeTile = (TileEntity) tile;
        if (spongeTile.getType() != null && !((SpongeTileEntityType) spongeTile.getType()).canTick()) {
            return false;
        }

        return true;
    }

    @Inject(method = "getPlayerEntityByUUID", at = @At("HEAD"), cancellable = true)
    public void onGetPlayerEntityByUUID(UUID uuid, CallbackInfoReturnable<UUID> cir) {
        // avoid crashing server if passed a null UUID
        if (uuid == null) {
            cir.setReturnValue(null);
        }
    }

    /**
     * @author blood - February 20th, 2017
     * @reason Avoids loading unloaded chunk when checking for sky.
     *
     * @param pos The position to get the light for
     * @return Whether block position can see sky
     */
    @Overwrite
    public boolean canSeeSky(BlockPos pos) {
        final net.minecraft.world.chunk.Chunk chunk = ((IMixinChunkProviderServer) this.chunkProvider).getLoadedChunkWithoutMarkingActive(pos.getX() >> 4, pos.getZ() >> 4);
        if (chunk == null || chunk.unloadQueued) {
            return false;
        }

        return chunk.canSeeSky(pos);
    }

    @Override
    public int getRawBlockLight(BlockPos pos, EnumLightType lightType) {
        return this.getRawLight(pos, lightType);
    }

    /**
     * @author gabizou - July 25th, 2016
     * @author Aaron1011 - February 5th, 2018 - Update for 1.13
     * @reason Optimizes several blockstate lookups for getting raw light.
     *
     * @param pos The position to get the light for
     * @param enumSkyBlock The light type
     * @return The raw light
     */
    @Inject(method = "getRawLight", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getBlockState" +
            "(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/state/IBlockState;"), cancellable = true)
    private void onLightGetBlockState(BlockPos pos, EnumLightType enumSkyBlock, CallbackInfoReturnable<Integer> cir) {
        final net.minecraft.world.chunk.Chunk chunk;
        if (!this.isFake()) {
            chunk = ((IMixinChunkProviderServer) ((WorldServer) (Object) this).getChunkProvider()).getLoadedChunkWithoutMarkingActive(pos.getX() >> 4, pos.getZ() >> 4);
        } else {
            chunk = this.getChunk(pos);
        }
        if (chunk == null || chunk.unloadQueued) {
            cir.setReturnValue(0);
        }
    }

    /**
     * @author gabizou - August 4th, 2016
     * @reason Rewrites the check to be inlined to {@link IMixinBlockPos}.
     *
     * @param pos The position
     * @return The block state at the desired position
     */
    @Overwrite
    public IBlockState getBlockState(BlockPos pos) {
        // Sponge - Replace with inlined method
        // if (this.isOutsideBuildHeight(pos)) // Vanilla
        if (((IMixinBlockPos) pos).isInvalidYPosition()) {
            // Sponge end
            return Blocks.AIR.getDefaultState();
        }
        net.minecraft.world.chunk.Chunk chunk = this.getChunk(pos);
        return chunk.getBlockState(pos);
    }

    /**
     * @author gabizou - August 4th, 2016
     * @author bloodmc - May 10th, 2017 - Added async check
     * @reason Rewrites the check to be inlined to {@link IMixinBlockPos}.
     *
     * @param pos The position
     * @return The tile entity at the desired position, or else null
     */
    @Overwrite
    @Nullable
    public net.minecraft.tileentity.TileEntity getTileEntity(BlockPos pos) {
        // Sponge - Replace with inlined method
        //  if (this.isOutsideBuildHeight(pos)) // Vanilla
        if (((IMixinBlockPos) pos).isInvalidYPosition()) {
            return null;
            // Sponge End
        } else {
            net.minecraft.tileentity.TileEntity tileentity = null;

            // Sponge - Don't create or obtain pending tileentity async, simply check if TE exists in chunk
            // Mods such as pixelmon call this method async, so this is a temporary workaround until fixed
            if (!this.isFake() && !SpongeImpl.getServer().isCallingFromMinecraftThread()) {
                return this.getChunk(pos).getTileEntity(pos, net.minecraft.world.chunk.Chunk.EnumCreateEntityType.CHECK);
            }
            // Sponge end

            if (this.processingLoadedTiles) {
                tileentity = this.getPendingTileEntityAt(pos);
            }

            if (tileentity == null) {
                 tileentity = this.getChunk(pos).getTileEntity(pos, net.minecraft.world.chunk.Chunk.EnumCreateEntityType.IMMEDIATE);
            }

            if (tileentity == null) {
                tileentity = this.getPendingTileEntityAt(pos);
            }

            return tileentity;
        }
    }


    /**
     * @author gabizou
     * @author gabizou - March 15th, 2019 - MC 1.13.2
     * @reason Adds a redirector to use instead of an injector to avoid duplicate chunk area loaded lookups.
     * This is overridden in MixinWorldServer_Lighting.
     *
     * @param thisWorld This world
     * @param pos The block position to check light for
     * @param radius The radius, always 17
     * @param allowEmtpy Whether to allow empty chunks, always false
     * @param lightType The light type
     * @param samePosition The block position to check light for, again.
     * @return True if the area is loaded
     */
    @Redirect(method = "checkLightFor", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;isAreaLoaded(Lnet/minecraft/util/math/BlockPos;IZ)Z"))
    protected boolean spongeIsAreaLoadedForCheckingLight(net.minecraft.world.World thisWorld, BlockPos pos, int radius, boolean allowEmtpy, EnumLightType lightType, BlockPos samePosition) {
        return ((net.minecraft.world.World) (Object) this).isAreaLoaded(pos, radius, allowEmtpy);
    }

    /**
     * @author gabizou - August 4th, 2016
     * @author Aaron1011 - February 6th, 2019 - Update for 1.13
     * @reason Rewrites the check to be inlined to {@link IMixinBlockPos}.
     *
     * @param pos The position
     * @return True if the block position is valid
     *
     */
    @Overwrite
    public static boolean isValid(BlockPos pos) { // isValid
        return ((IMixinBlockPos) pos).isValidPosition();
    }

    /**
     * @author gabizou - August 4th, 2016
     * @reason Rewrites the check to be inlined to {@link IMixinBlockPos}.
     * @author Aaron1011 - February 6th, 2019 - Update for 1.13
     *
     * @param pos The position
     * @return True if the block position is outside build height
     */
    @Overwrite
    public static boolean isOutsideBuildHeight(BlockPos pos) { // isOutsideBuildHeight
        return ((IMixinBlockPos) pos).isInvalidYPosition();
    }


    /*********************** TIMINGS ***********************/

    /**
     * @author blood
     * @author gabizou - Ported to 1.9.4 - replace direct field calls to overriden methods in MixinWorldServer
     *
     * @reason Add timing hooks in various areas. This method shouldn't be touched by mods/forge alike
     */
    @Overwrite
    public void tickEntities() {
        this.profiler.startSection("entities");
        this.profiler.startSection("global");
        this.startEntityGlobalTimings(); // Sponge


        for (int i = 0; i < this.weatherEffects.size(); ++i) {
            net.minecraft.entity.Entity entity = this.weatherEffects.get(i);

            try {
                ++entity.ticksExisted;
                entity.tick();
            } catch (Throwable throwable2) {
                this.stopTimingForWeatherEntityTickCrash(entity); // Sponge - end the entity timing
                CrashReport crashreport = CrashReport.makeCrashReport(throwable2, "Ticking entity");
                CrashReportCategory crashreportcategory = crashreport.makeCategory("Entity being ticked");

                if (entity == null) {
                    crashreportcategory.addDetail("Entity", "~~NULL~~");
                } else {
                    entity.fillCrashReport(crashreportcategory);
                }

                SpongeImplHooks.onEntityError(entity, crashreport);
            }

            if (entity.removed) {
                this.weatherEffects.remove(i--);
            }
        }

        this.stopEntityTickTimingStartEntityRemovalTiming(); // Sponge
        this.profiler.endStartSection("remove");
        this.loadedEntityList.removeAll(this.unloadedEntityList);

        for (int k = 0; k < this.unloadedEntityList.size(); ++k) {
            net.minecraft.entity.Entity entity1 = this.unloadedEntityList.get(k);
            // Sponge start - use cached chunk
            // int j = entity1.chunkCoordX;
            // int k1 = entity1.chunkCoordZ;

            final net.minecraft.world.chunk.Chunk activeChunk = (net.minecraft.world.chunk.Chunk) ((IMixinEntity) entity1).getActiveChunk();
            if (activeChunk != null) {
                activeChunk.removeEntity(entity1);
            }
            // Sponge end
        }

        for (int l = 0; l < this.unloadedEntityList.size(); ++l) {
            this.onEntityRemoved(this.unloadedEntityList.get(l));
        }

        this.unloadedEntityList.clear();
        this.stopEntityRemovalTiming(); // Sponge
        this.tickPlayers();
        this.profiler.endStartSection("regular");
        this.entityActivationCheck();

        for (int i1 = 0; i1 < this.loadedEntityList.size(); ++i1) {
            net.minecraft.entity.Entity entity2 = this.loadedEntityList.get(i1);
            net.minecraft.entity.Entity entity3 = entity2.getRidingEntity();

            if (entity3 != null) {
                if (!entity3.removed && entity3.isPassenger(entity2)) {
                    continue;
                }

                entity2.stopRiding();
            }

            this.profiler.startSection("tick");
            this.startEntityTickTiming(); // Sponge

            if (!entity2.removed && !(entity2 instanceof EntityPlayerMP)) {
                try {
                    SpongeImplHooks.onEntityTickStart(entity2);
                    this.tickEntity(entity2);
                    SpongeImplHooks.onEntityTickEnd(entity2);
                } catch (Throwable throwable1) {
                    this.stopTimingTickEntityCrash(entity2); // Sponge
                    CrashReport crashreport1 = CrashReport.makeCrashReport(throwable1, "Ticking entity");
                    CrashReportCategory crashreportcategory1 = crashreport1.makeCategory("Entity being ticked");
                    entity2.fillCrashReport(crashreportcategory1);
                    SpongeImplHooks.onEntityError(entity2, crashreport1);
                }
            }

            this.stopEntityTickSectionBeforeRemove(); // Sponge
            this.profiler.endSection();
            this.profiler.startSection("remove");
            this.startEntityRemovalTick(); // Sponge

            if (entity2.removed) {
                // Sponge start - use cached chunk
                int l1 = entity2.chunkCoordX;
                int i2 = entity2.chunkCoordZ;

                final net.minecraft.world.chunk.Chunk activeChunk = (net.minecraft.world.chunk.Chunk) ((IMixinEntity) entity2).getActiveChunk();
                if (activeChunk == null) {
                    this.getChunk(l1, i2).removeEntity(entity2);
                } else {
                    activeChunk.removeEntity(entity2);
                }
                // Sponge end

                this.loadedEntityList.remove(i1--);
                this.onEntityRemoved(entity2);
            }

            this.stopEntityRemovalTiming(); // Sponge
            this.profiler.endSection();
        }

        // this.profiler.endStartSection("blockEntities"); // Sponge - Don't use the profiler
        spongeTileEntityActivation();
        this.processingLoadedTiles = true;
        Iterator<net.minecraft.tileentity.TileEntity> iterator = this.tickableTileEntities.iterator();

        while (iterator.hasNext()) {
            this.startTileTickTimer(); // Sponge
            net.minecraft.tileentity.TileEntity tileentity = iterator.next();

            if (!tileentity.isRemoved() && tileentity.hasWorld()) {
                BlockPos blockpos = tileentity.getPos();

                if (((IMixinTileEntity) tileentity).shouldTick() && this.worldBorder.contains(blockpos)) { // Sponge
                    try {
                        this.profiler.startSection(() -> String.valueOf(TileEntityType.REGISTRY.getKey(tileentity.getType())));
                        SpongeImplHooks.onTETickStart(tileentity);
                        ((ITickable) tileentity).tick();
                        //this.profiler.endSection();
                        SpongeImplHooks.onTETickEnd(tileentity);
                    } catch (Throwable throwable) {
                        this.stopTimingTickTileEntityCrash(tileentity); // Sponge
                        CrashReport crashreport2 = CrashReport.makeCrashReport(throwable, "Ticking block entity");
                        CrashReportCategory crashreportcategory2 = crashreport2.makeCategory("Block entity being ticked");
                        tileentity.addInfoToCrashReport(crashreportcategory2);
                        SpongeImplHooks.onTileEntityError(tileentity, crashreport2);
                    }
                }
            }

            this.stopTileEntityAndStartRemoval(); // Sponge

            if (tileentity.isRemoved()) {
                iterator.remove();
                this.loadedTileEntityList.remove(tileentity);
                // Sponge start - use cached chunk
                final net.minecraft.world.chunk.Chunk activeChunk = (net.minecraft.world.chunk.Chunk) ((IMixinTileEntity) tileentity).getActiveChunk();
                if (activeChunk != null) {
                    //this.getChunk(tileentity.getPos()).removeTileEntity(tileentity.getPos());
                    //Forge: Bugfix: If we set the tile entity it immediately sets it in the chunk, so we could be desynced
                    if (activeChunk.getTileEntity(tileentity.getPos(), net.minecraft.world.chunk.Chunk.EnumCreateEntityType.CHECK) == tileentity) {
                        activeChunk.removeTileEntity(tileentity.getPos());
                    }
                }
                // Sponge end
            }

            this.stopTileEntityRemovelInWhile(); // Sponge
        }

        if (!this.tileEntitiesToBeRemoved.isEmpty()) {
            // Sponge start - use forge hook
            for (Object tile : this.tileEntitiesToBeRemoved) {
               SpongeImplHooks.onTileChunkUnload(((net.minecraft.tileentity.TileEntity)tile));
            }
            // Sponge end

            // forge: faster "contains" makes this removal much more efficient
            java.util.Set<net.minecraft.tileentity.TileEntity> remove = java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>());
            remove.addAll(this.tileEntitiesToBeRemoved);
            this.tickableTileEntities.removeAll(remove);
            this.loadedTileEntityList.removeAll(remove);
            this.tileEntitiesToBeRemoved.clear();
        }

        if (!this.isFake()) {
            try (final PhaseContext<?> context = BlockPhase.State.TILE_CHUNK_UNLOAD.createPhaseContext().source(this)) {
                context.buildAndSwitch();
                this.startPendingTileEntityTimings(); // Sponge
            }
        }

        this.processingLoadedTiles = false;  //FML Move below remove to prevent CMEs
         this.profiler.endStartSection("pendingBlockEntities");

        if (!this.addedTileEntityList.isEmpty()) {
            for (int j1 = 0; j1 < this.addedTileEntityList.size(); ++j1) {
                net.minecraft.tileentity.TileEntity tileentity1 = this.addedTileEntityList.get(j1);

                if (!tileentity1.isRemoved()) {
                    if (!this.loadedTileEntityList.contains(tileentity1)) {
                        this.addTileEntity(tileentity1);
                    }

                    if (this.isBlockLoaded(tileentity1.getPos())) {
                        net.minecraft.world.chunk.Chunk chunk = this.getChunk(tileentity1.getPos());
                        IBlockState iblockstate = chunk.getBlockState(tileentity1.getPos());
                        chunk.addTileEntity(tileentity1.getPos(), tileentity1);
                        this.notifyBlockUpdate(tileentity1.getPos(), iblockstate, iblockstate, 3);
                    }
                }
            }

            this.addedTileEntityList.clear();
        }

        this.endPendingTileEntities(); // Sponge
        this.profiler.endSection();
        this.profiler.endSection();
    }

    /**
     * Overridden in {@link MixinWorldServer_TileEntityActivation}
     */
    public void spongeTileEntityActivation() {

    }

    public void entityActivationCheck() {
        // Overridden in MixinWorldServer_Activation
    }

    @Override
    public int getSeaLevel() {
        return this.seaLevel;
    }

    protected void startEntityGlobalTimings() { }

    protected void stopTimingForWeatherEntityTickCrash(net.minecraft.entity.Entity updatingEntity) { }

    protected void stopEntityTickTimingStartEntityRemovalTiming() { }

    protected void stopEntityRemovalTiming() { }

    protected void startEntityTickTiming() { }

    protected void stopTimingTickEntityCrash(net.minecraft.entity.Entity updatingEntity) { }

    protected void stopEntityTickSectionBeforeRemove() { }

    protected void startEntityRemovalTick() { }

    protected void startTileTickTimer() { }

    protected void stopTimingTickTileEntityCrash(net.minecraft.tileentity.TileEntity updatingTileEntity) { }

    protected void stopTileEntityAndStartRemoval() { }

    protected void stopTileEntityRemovelInWhile() { }

    protected void startPendingTileEntityTimings() {}

    protected void endPendingTileEntities() { }

}
