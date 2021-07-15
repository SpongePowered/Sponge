package org.spongepowered.common.launch.mapping;

/**
 * A mapping manager provide the necessary tool to convert
 * from Mojang mapping to the current runtime mapping. This
 * is intended to facility the reflection across different
 * mapping.
 */
public interface SpongeMappingManager {
	String toRuntimeClassName(String srcName);

	String toRuntimeFieldName(Class<?> owner, String srcName);

	String toRuntimeMethodName(Class<?> owner, String srcName, Class<?> ...params);
}
