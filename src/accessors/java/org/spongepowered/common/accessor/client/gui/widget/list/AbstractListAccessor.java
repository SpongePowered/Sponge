package org.spongepowered.common.accessor.client.gui.widget.list;

import net.minecraft.client.gui.widget.list.AbstractList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractList.class)
public interface AbstractListAccessor {

    @Accessor("renderSelection") boolean accessor$renderSelection();
}
