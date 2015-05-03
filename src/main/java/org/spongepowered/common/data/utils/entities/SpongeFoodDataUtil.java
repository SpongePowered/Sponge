package org.spongepowered.common.data.utils.entities;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.spongepowered.common.data.DataTransactionBuilder.builder;
import static org.spongepowered.common.data.DataTransactionBuilder.fail;

import com.google.common.base.Optional;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.FoodStats;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataPriority;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.manipulators.entities.FoodData;
import org.spongepowered.api.service.persistence.InvalidDataException;
import org.spongepowered.common.data.SpongeDataUtil;
import org.spongepowered.common.data.manipulators.entities.SpongeFoodData;

public class SpongeFoodDataUtil implements SpongeDataUtil<FoodData> {

    @Override
    public Optional<FoodData> fillData(DataHolder holder, FoodData manipulator, DataPriority priority) {
        if (holder instanceof EntityPlayer) {
            switch (checkNotNull(priority)) {
                case DATA_HOLDER:
                case PRE_MERGE:
                    final FoodStats foodStats = ((EntityPlayer) holder).getFoodStats();
                    manipulator.setExhaustion(foodStats.foodExhaustionLevel);
                    manipulator.setFoodLevel(foodStats.getFoodLevel());
                    manipulator.setSaturation(foodStats.getSaturationLevel());
                    return Optional.of(manipulator);
                default:
                    return Optional.absent();
            }
        }
        return Optional.absent();
    }

    @Override
    public DataTransactionResult setData(DataHolder dataHolder, FoodData manipulator, DataPriority priority) {
        if (!(dataHolder instanceof EntityPlayer)) {
            return fail(manipulator);
        } else {
            switch (checkNotNull(priority)) {
                case DATA_HOLDER:
                    return builder().reject(manipulator).result(DataTransactionResult.Type.SUCCESS).build();
                case DATA_MANIPULATOR:
                    final FoodStats foodStats = ((EntityPlayer) dataHolder).getFoodStats();
                    final FoodData oldData = createFrom(dataHolder).get();
                    foodStats.setFoodLevel(((int) Math.floor(manipulator.getFoodLevel())));
                    foodStats.foodExhaustionLevel = ((float) manipulator.getExhaustion());
                    foodStats.setFoodSaturationLevel(((float) manipulator.getSaturation()));
                    return builder().replace(oldData).result(DataTransactionResult.Type.SUCCESS).build();
                default:
                    return fail(manipulator);
            }
        }
    }

    @Override
    public boolean remove(DataHolder dataHolder) {
        if (!(dataHolder instanceof EntityPlayer)) {
            return false;
        } else {
            final FoodStats foodStats = ((EntityPlayer) dataHolder).getFoodStats();
            foodStats.foodExhaustionLevel = 0;
            foodStats.setFoodLevel(20);
            foodStats.setFoodSaturationLevel(20);
            return true;
        }
    }

    @Override
    public Optional<FoodData> build(DataView container) throws InvalidDataException {
        return Optional.absent();
    }

    @Override
    public FoodData create() {
        return new SpongeFoodData();
    }

    @Override
    public Optional<FoodData> createFrom(DataHolder dataHolder) {
        if (!(dataHolder instanceof EntityPlayer)) {
            return Optional.absent();
        }
        final FoodStats foodStats = ((EntityPlayer) dataHolder).getFoodStats();
        final FoodData foodData = create();
        foodData.setExhaustion(foodStats.foodExhaustionLevel);
        foodData.setFoodLevel(foodStats.getFoodLevel());
        foodData.setSaturation(foodStats.getSaturationLevel());
        return Optional.of(foodData);
    }
}
