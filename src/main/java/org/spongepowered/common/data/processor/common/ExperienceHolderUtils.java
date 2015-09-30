package org.spongepowered.common.data.processor.common;


public class ExperienceHolderUtils {
    
    public static int getExpBetweenLevels(int level) {
        return level >= 30 ? 112 + (level - 30) * 9 : (level >= 15 ? 37 + (level - 15) * 5 : 7 + level * 2);
    }

}
