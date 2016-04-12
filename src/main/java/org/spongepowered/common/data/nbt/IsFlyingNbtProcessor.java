package org.spongepowered.common.data.nbt;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableFlyingData;
import org.spongepowered.api.data.manipulator.mutable.entity.FlyingData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeFlyingData;
import org.spongepowered.common.data.util.NbtDataUtil;

import java.util.Optional;

public class IsFlyingNbtProcessor implements NbtDataProcessor<FlyingData, ImmutableFlyingData> {

    @Override
    public NbtDataType getTargetType() {
        return null;
    }

    @Override
    public Optional<FlyingData> readFromCompound(NBTTagCompound compound) {
        final NBTBase tag = compound.getTag(NbtDataUtil.Minecraft.IS_FLYING);
        if (tag != null) {
            return Optional.of(new SpongeFlyingData(((NBTTagByte) tag).getByte() != 0));
        }
        return Optional.empty();
    }

    @Override
    public NBTTagCompound storeToCompound(NBTTagCompound compound, FlyingData manipulator) {
        compound.setBoolean(NbtDataUtil.Minecraft.IS_FLYING, manipulator.flying().get());
        return compound;
    }
}
