package org.spongepowered.common.mixin.core.world.gen.populators;

import static com.google.common.base.Preconditions.checkNotNull;

import com.flowpowered.math.vector.Vector3i;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.gen.NoiseGeneratorSimplex;
import net.minecraft.world.gen.feature.WorldGenEndIsland;
import net.minecraft.world.gen.feature.WorldGenerator;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.util.weighted.VariableAmount;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.gen.PopulatorType;
import org.spongepowered.api.world.gen.PopulatorTypes;
import org.spongepowered.api.world.gen.populator.EndIsland;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(WorldGenEndIsland.class)
public abstract class MixinWorldGenEndIsland extends WorldGenerator implements EndIsland {

    private NoiseGeneratorSimplex field_185973_o;
    private long lastSeed = -1;

    private VariableAmount initial;
    private VariableAmount decrement;
    private BlockState state;

    @Inject(method = "<init>", at = @At("RETURN") )
    public void onConstructed(CallbackInfo ci) {
        this.initial = VariableAmount.baseWithRandomAddition(4, 3);
        this.decrement = VariableAmount.baseWithRandomAddition(0.5, 2);
        this.state = BlockTypes.END_STONE.getDefaultState();
    }

    @Override
    public PopulatorType getType() {
        return PopulatorTypes.END_ISLAND;
    }

    @Override
    public VariableAmount getStartingRadius() {
        return this.initial;
    }

    @Override
    public void setStartingRadius(VariableAmount radius) {
        this.initial = checkNotNull(radius);
    }

    @Override
    public VariableAmount getRadiusDecrement() {
        return this.decrement;
    }

    @Override
    public void setRadiusDecrement(VariableAmount decrement) {
        this.decrement = checkNotNull(decrement);
    }

    @Override
    public BlockState getIslandBlock() {
        return this.state;
    }

    @Override
    public void setIslandBlock(BlockState state) {
        this.state = checkNotNull(state);
    }

    @Override
    public void populate(Chunk chunk, Random rand) {
        if (this.field_185973_o == null || chunk.getWorld().getProperties().getSeed() != this.lastSeed) {
            this.lastSeed = chunk.getWorld().getProperties().getSeed();
            this.field_185973_o = new NoiseGeneratorSimplex(new Random(this.lastSeed));
        }
        Vector3i min = chunk.getBlockMin();
        BlockPos chunkPos = new BlockPos(min.getX(), min.getY(), min.getZ());
        float f = this.func_185960_a(min.getX() / 16, min.getZ() / 16, 1, 1);

        if (f < -20.0F && rand.nextInt(14) == 0) {
            generate((World) chunk.getWorld(), rand, chunkPos.add(rand.nextInt(16) + 8, 55 + rand.nextInt(16), rand.nextInt(16) + 8));

            if (rand.nextInt(4) == 0) {
                generate((World) chunk.getWorld(), rand, chunkPos.add(rand.nextInt(16) + 8, 55 + rand.nextInt(16), rand.nextInt(16) + 8));
            }
        }
    }

    /*
     * Author: Deamon
     * 
     * Purpose: it use the initial radius, radius decrement, and block type fields
     */
    @Override
    @Overwrite
    public boolean generate(World worldIn, Random rand, BlockPos position) {
//        int i = rand.nextInt(3) + 4;
        int i = this.initial.getFlooredAmount(rand);
        float f = i;

        for (int j = 0; f > 0.5F; --j) {
            for (int k = MathHelper.floor_float(-f); k <= MathHelper.ceiling_float_int(f); ++k) {
                for (int l = MathHelper.floor_float(-f); l <= MathHelper.ceiling_float_int(f); ++l) {
                    if (k * k + l * l <= (f + 1.0F) * (f + 1.0F)) {
//                        this.setBlockAndNotifyAdequately(worldIn, position.add(k, j, l), Blocks.end_stone.getDefaultState());
                        this.setBlockAndNotifyAdequately(worldIn, position.add(k, j, l), (IBlockState) this.state);
                    }
                }
            }

            f = (float) (f - this.decrement.getAmount(rand));
//            f = (float)(f - (rand.nextInt(2) + 0.5D));
        }

        return true;
    }

    private float func_185960_a(int p_185960_1_, int p_185960_2_, int p_185960_3_, int p_185960_4_) {
        float f = p_185960_1_ * 2 + p_185960_3_;
        float f1 = p_185960_2_ * 2 + p_185960_4_;
        float f2 = 100.0F - MathHelper.sqrt_float(f * f + f1 * f1) * 8.0F;

        if (f2 > 80.0F) {
            f2 = 80.0F;
        }

        if (f2 < -100.0F) {
            f2 = -100.0F;
        }

        for (int i = -12; i <= 12; ++i) {
            for (int j = -12; j <= 12; ++j) {
                long k = p_185960_1_ + i;
                long l = p_185960_2_ + j;

                if (k * k + l * l > 4096L && this.field_185973_o.func_151605_a(k, l) < -0.8999999761581421D) {
                    float f3 = (MathHelper.abs(k) * 3439.0F + MathHelper.abs(l) * 147.0F) % 13.0F + 9.0F;
                    f = p_185960_3_ - i * 2;
                    f1 = p_185960_4_ - j * 2;
                    float f4 = 100.0F - MathHelper.sqrt_float(f * f + f1 * f1) * f3;

                    if (f4 > 80.0F) {
                        f4 = 80.0F;
                    }

                    if (f4 < -100.0F) {
                        f4 = -100.0F;
                    }

                    if (f4 > f2) {
                        f2 = f4;
                    }
                }
            }
        }

        return f2;
    }

}
