package org.spongepowered.common.interfaces.entity;

import com.flowpowered.math.vector.Vector3d;

public interface IMixinEntityEnderEye {

    Vector3d getTargetLoc();

    void setTargetLoc(double x, double y, double z);

}
