/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
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
        try (CauseStackManager.StackFrame stackFrame = Sponge.getCauseStackManager().pushCauseFrame()) {
            // These have to be the same in order to increase the duplicateCauses count.
            String duplicateCause = "Duplicate Cause Test";
            
            // Initial Cause
            stackFrame.pushCause(duplicateCause);
            
            // Duplicate Causes
            stackFrame.pushCause(duplicateCause);
            stackFrame.pushCause(duplicateCause);
            stackFrame.pushCause(duplicateCause);
            stackFrame.pushCause(duplicateCause);
            stackFrame.pushCause(duplicateCause);
            
            // old_min_depth: 2 (Plugin, UUID) (Gets incremented to 3 after creating StackFrame).
            // lastCauseSize: 5.
            // This will result in index 2 (Which will be our randomUUID) of the duplicateCauses in the SpongeCauseStackManager to be set to 5.
            Sponge.getCauseStackManager().pushCauseFrame().close();
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