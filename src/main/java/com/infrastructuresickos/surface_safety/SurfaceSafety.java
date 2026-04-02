package com.infrastructuresickos.surface_safety;

import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod("surface_safety")
public class SurfaceSafety {
    public static final Logger LOGGER = LogManager.getLogger();

    public SurfaceSafety() {
        LOGGER.info("SurfaceSafety mod initialized");
    }
}
