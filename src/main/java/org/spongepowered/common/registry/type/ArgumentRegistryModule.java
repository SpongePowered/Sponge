package org.spongepowered.common.registry.type;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.text.selector.ArgumentHolder;
import org.spongepowered.api.text.selector.ArgumentType;
import org.spongepowered.api.text.selector.ArgumentTypes;
import org.spongepowered.common.registry.RegisterCatalog;
import org.spongepowered.common.registry.RegistrationDependency;
import org.spongepowered.common.registry.RegistryModule;
import org.spongepowered.common.registry.factory.SelectorFactoryModule;
import org.spongepowered.common.text.selector.SpongeArgumentHolder;
import org.spongepowered.common.text.selector.SpongeSelectorFactory;

import java.util.HashMap;
import java.util.Map;

@RegistrationDependency({SelectorFactoryModule.class, SelectorTypeRegistryModule.class})
public class ArgumentRegistryModule implements RegistryModule {

    @RegisterCatalog(ArgumentTypes.class)
    private final Map<String, ArgumentHolder<?>> argumentTypeMap = new HashMap<>();

    @Override
    public void registerDefaults() {
        final SpongeSelectorFactory factory = SelectorFactoryModule.INSTANCE;
        // POSITION
        ArgumentType<Integer> x = factory.createArgumentType("x", Integer.class);
        ArgumentType<Integer> y = factory.createArgumentType("y", Integer.class);
        ArgumentType<Integer> z = factory.createArgumentType("z", Integer.class);
        ArgumentHolder.Vector3<Vector3i, Integer> position = new SpongeArgumentHolder.SpongeVector3<>(x, y, z, Vector3i.class);
        this.argumentTypeMap.put("position", position);

        // RADIUS
        ArgumentType<Integer> rmin = factory.createArgumentType("rm", Integer.class);
        ArgumentType<Integer> rmax = factory.createArgumentType("r", Integer.class);
        ArgumentHolder.Limit<ArgumentType<Integer>> radius = new SpongeArgumentHolder.SpongeLimit<>(rmin, rmax);
        this.argumentTypeMap.put("radius", radius);

        // GAME_MODE
        this.argumentTypeMap.put("game_mode", factory.createArgumentType("m", GameMode.class));

        // COUNT
        this.argumentTypeMap.put("count", factory.createArgumentType("c", Integer.class));

        // LEVEL
        ArgumentType<Integer> lmin = factory.createArgumentType("lm", Integer.class);
        ArgumentType<Integer> lmax = factory.createArgumentType("l", Integer.class);
        ArgumentHolder.Limit<ArgumentType<Integer>> level = new SpongeArgumentHolder.SpongeLimit<>(lmin, lmax);
        this.argumentTypeMap.put("level", level);

        // TEAM
        this.argumentTypeMap.put("team", factory.createInvertibleArgumentType("team", Integer.class,
                                                                              org.spongepowered.api.scoreboard.Team.class.getName()));

        // NAME
        this.argumentTypeMap.put("name", factory.createInvertibleArgumentType("name", String.class));

        // DIMENSION
        ArgumentType<Integer> dx = factory.createArgumentType("dx", Integer.class);
        ArgumentType<Integer> dy = factory.createArgumentType("dy", Integer.class);
        ArgumentType<Integer> dz = factory.createArgumentType("dz", Integer.class);
        ArgumentHolder.Vector3<Vector3i, Integer> dimension =
            new SpongeArgumentHolder.SpongeVector3<>(dx, dy, dz, Vector3i.class);
        this.argumentTypeMap.put("dimension", dimension);

        // ROTATION
        ArgumentType<Double> rotxmin = factory.createArgumentType("rxm", Double.class);
        ArgumentType<Double> rotymin = factory.createArgumentType("rym", Double.class);
        ArgumentType<Double> rotzmin = factory.createArgumentType("rzm", Double.class);
        ArgumentHolder.Vector3<Vector3d, Double> rotmin =
            new SpongeArgumentHolder.SpongeVector3<>(rotxmin, rotymin, rotzmin, Vector3d.class);
        ArgumentType<Double> rotxmax = factory.createArgumentType("rx", Double.class);
        ArgumentType<Double> rotymax = factory.createArgumentType("ry", Double.class);
        ArgumentType<Double> rotzmax = factory.createArgumentType("rz", Double.class);
        ArgumentHolder.Vector3<Vector3d, Double> rotmax =
            new SpongeArgumentHolder.SpongeVector3<>(rotxmax, rotymax, rotzmax, Vector3d.class);
        ArgumentHolder.Limit<ArgumentHolder.Vector3<Vector3d, Double>> rot =
            new SpongeArgumentHolder.SpongeLimit<>(rotmin, rotmax);
        this.argumentTypeMap.put("rotation", rot);

        // ENTITY_TYPE
        this.argumentTypeMap.put("entity_type", factory.createInvertibleArgumentType("type", EntityType.class));
    }
}
