package org.spongepowered.common.data.manipulators.entities;

import static com.google.common.base.Preconditions.checkArgument;
import static org.spongepowered.api.data.DataQuery.of;

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.manipulators.entities.FoodData;
import org.spongepowered.common.data.manipulators.SpongeAbstractData;

public class SpongeFoodData extends SpongeAbstractData<FoodData> implements FoodData {

    private double exhaustion;
    private double saturation;
    private double foodLevel;

    public SpongeFoodData() {
        super(FoodData.class);
    }

    @Override
    public DataContainer toContainer() {
        return new MemoryDataContainer()
                .set(of("FoodLevel"), this.foodLevel)
                .set(of("Saturation"), this.saturation)
                .set(of("Exhaustion"), this.exhaustion);
    }

    @Override
    public double getExhaustion() {
        return this.exhaustion;
    }

    @Override
    public FoodData setExhaustion(double exhaustion) {
        checkArgument(exhaustion >= 0 && exhaustion <= 20);
        this.exhaustion = exhaustion;
        return this;
    }

    @Override
    public double getSaturation() {
        return this.saturation;
    }

    @Override
    public FoodData setSaturation(double saturation) {
        this.saturation = saturation;
        return this;
    }

    @Override
    public double getFoodLevel() {
        return this.foodLevel;
    }

    @Override
    public FoodData setFoodLevel(double foodLevel) {
        this.foodLevel = foodLevel;
        return this;
    }

    @Override
    public int compareTo(FoodData o) {
        return (int) Math.floor((o.getFoodLevel() - this.foodLevel) - (o.getExhaustion() - this.exhaustion) - (o.getSaturation() - this.saturation));
    }
}
