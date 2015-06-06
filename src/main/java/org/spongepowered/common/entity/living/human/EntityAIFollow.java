package org.spongepowered.common.entity.living.human;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class EntityAIFollow extends EntityAIBase {
    private EntityHuman theFollower;
    World theWorld;
    private double field_75336_f;
    private PathNavigate petPathfinder;
    private int field_75343_h;
    float maxDist;
    float minDist;
    private boolean field_75344_i;
    private static final String __OBFID = "CL_00001585";

    public EntityAIFollow(EntityHuman theFollower, double p_i1625_2_, float minDistIn, float maxDistIn) {
        this.theFollower = theFollower;
        this.theWorld = theFollower.worldObj;
        this.field_75336_f = p_i1625_2_;
        this.petPathfinder = theFollower.getNavigator();
        this.minDist = minDistIn;
        this.maxDist = maxDistIn;
        this.setMutexBits(3);

        if (!(theFollower.getNavigator() instanceof PathNavigateGround)) {
            throw new IllegalArgumentException("Unsupported mob type for FollowOwnerGoal");
        }
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    public boolean shouldExecute() {
        return theFollower.getOwner() != null;
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    public boolean continueExecuting() {
        return !this.petPathfinder.noPath() && this.theFollower.getDistanceSqToEntity(this.theFollower.getOwner()) > (double)(this.maxDist * this
                .maxDist);
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void startExecuting() {
        this.field_75343_h = 0;
        this.field_75344_i = ((PathNavigateGround)this.theFollower.getNavigator()).getAvoidsWater();
        ((PathNavigateGround)this.theFollower.getNavigator()).setAvoidsWater(false);
    }

    /**
     * Resets the task
     */
    public void resetTask() {
        //this.theFollower.setOwner(null);
        this.petPathfinder.clearPathEntity();
        ((PathNavigateGround)this.theFollower.getNavigator()).setAvoidsWater(true);
    }


    /**
     * Updates the task
     */
    public void updateTask() {
        this.theFollower.getLookHelper().setLookPositionWithEntity(this.theFollower.getOwner(), 10.0F, (float)this.theFollower.getVerticalFaceSpeed());

        if (--this.field_75343_h <= 0) {
            this.field_75343_h = 10;

            if (!this.petPathfinder.tryMoveToEntityLiving(this.theFollower.getOwner(), this.field_75336_f)) {
                if (this.theFollower.getDistanceSqToEntity(this.theFollower.getOwner()) >= 144.0D) {
                    int i = MathHelper.floor_double(this.theFollower.getOwner().posX) - 4;
                    int j = MathHelper.floor_double(this.theFollower.getOwner().posZ) - 4;
                    int k = MathHelper.floor_double(this.theFollower.getOwner().getEntityBoundingBox().minY);

                    for (int l = 0; l <= 4; ++l) {
                        for (int i1 = 0; i1 <= 4; ++i1) {
                            if ((l < 1 || i1 < 1 || l > 3 || i1 > 3) && World.doesBlockHaveSolidTopSurface(this.theWorld, new BlockPos(i + l, k - 1, j + i1)) && !this.theWorld.getBlockState(new BlockPos(i + l, k, j + i1)).getBlock().isFullCube() && !this.theWorld.getBlockState(new BlockPos(i + l, k + 1, j + i1)).getBlock().isFullCube()) {
                                this.theFollower.setLocationAndAngles((double)((float)(i + l) + 0.5F), (double)k, (double)((float)(j + i1) + 0.5F), this.theFollower.rotationYaw, this.theFollower.rotationPitch);
                                this.petPathfinder.clearPathEntity();
                                return;
                            }
                        }
                    }
                }
            }
        }
    }
}