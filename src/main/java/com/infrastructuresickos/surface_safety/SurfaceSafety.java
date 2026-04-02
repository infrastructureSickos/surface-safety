package com.infrastructuresickos.surface_safety;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(SurfaceSafety.MOD_ID)
public class SurfaceSafety {
    public static final String MOD_ID = "surface_safety";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public SurfaceSafety() {
        MinecraftForge.EVENT_BUS.register(new SurfaceSafetyEventHandler());
        LOGGER.info("SurfaceSafety initialized");
    }
}
