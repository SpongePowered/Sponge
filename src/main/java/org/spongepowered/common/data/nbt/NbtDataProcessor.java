package org.spongepowered.common.data.nbt;

import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;

import java.util.Optional;

public interface NbtDataProcessor<M extends DataManipulator<M, I>, I extends ImmutableDataManipulator<I, M>> {

    NbtDataType getTargetType();

    Optional<M> readFromCompound(NBTTagCompound compound);

    NBTTagCompound storeToCompound(NBTTagCompound compound, M manipulator);

}
