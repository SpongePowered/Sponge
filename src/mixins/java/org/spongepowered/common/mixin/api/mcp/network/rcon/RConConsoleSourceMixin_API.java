package org.spongepowered.common.mixin.api.mcp.network.rcon;

import net.minecraft.network.rcon.RConConsoleSource;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(RConConsoleSource.class)
public abstract class RConConsoleSourceMixin_API implements Subject {

    @Override
    public String getIdentifier() {
        // RCon no longer has an identifier on the class, but it passes this to its CommandSource
        return "Recon";
    }

}
