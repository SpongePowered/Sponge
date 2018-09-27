package org.spongepowered.test;

import com.google.inject.Inject;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.plugin.Plugin;

import java.util.UUID;

@Plugin(id = "cause-stack-test", name = "Cause Stack Test", description = "A plugin testing the cause stack", version = "0.0.0")
public class CauseStackTest {
    
    @Inject private Logger logger;
    
    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onGameStartedServer(GameStartedServerEvent event) {
        UUID randomUUID = UUID.randomUUID();
        
        // Initial Cause
        Sponge.getCauseStackManager().pushCause(randomUUID);
        
        // old_min_depth: 0 (Gets incremented to 2 after creating StackFrame).
        // lastCauseSize: 0.
        try (CauseStackManager.StackFrame stackFrameFirst = Sponge.getCauseStackManager().pushCauseFrame()) {
            // These have to be the same in order to increase the duplicateCauses count.
            String duplicateCause = "Duplicate Cause Test";
            
            // Initial Cause
            stackFrameFirst.pushCause(duplicateCause);
            
            // Duplicate Causes
            stackFrameFirst.pushCause(duplicateCause);
            stackFrameFirst.pushCause(duplicateCause);
            stackFrameFirst.pushCause(duplicateCause);
            stackFrameFirst.pushCause(duplicateCause);
            stackFrameFirst.pushCause(duplicateCause);
            
            // old_min_depth: 2 (Plugin, UUID) (Gets incremented to 3 after creating StackFrame).
            // lastCauseSize: 5.
            try (CauseStackManager.StackFrame stackFrameSecond = Sponge.getCauseStackManager().pushCauseFrame()) {
                // This will result in index 2 (Which will be our randomUUID) of the duplicateCauses in the SpongeCauseStackManager to be set to 5.
            }
        }
        
        // Pop initial push.
        Sponge.getCauseStackManager().popCause();
        
        // Check for duplication.
        int count = 0;
        while (Sponge.getCauseStackManager().peekCause() == randomUUID) {
            Sponge.getCauseStackManager().popCause();
            count++;
        }
        
        if (count > 0) {
            this.logger.error("===================================================================");
            this.logger.error("CauseStack Duplication detected.");
            this.logger.error(count + " Objects were found when 0 was expected.");
            this.logger.error("===================================================================");
        }
    }
}