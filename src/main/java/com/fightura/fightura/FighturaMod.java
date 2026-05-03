package com.fightura.fightura;

import com.fightura.fightura.client.FighturaForgeEvents;
import com.mojang.logging.LogUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.slf4j.Logger;

@Mod(FighturaMod.MOD_ID)
public final class FighturaMod {
    public static final String MOD_ID = "fightura";
    public static final Logger LOGGER = LogUtils.getLogger();

    public FighturaMod() {
        LOGGER.info("Fightura starting");

        if (FMLEnvironment.dist != Dist.CLIENT) {
            return;
        }
        if (!ModList.get().isLoaded("epicfight") || !ModList.get().isLoaded("figura")) {
            LOGGER.warn("Fightura requires both Figura and Epic Fight on the client. The mod will idle.");
            return;
        }

        try {
            FighturaForgeEvents.register(FMLJavaModLoadingContext.get().getModEventBus());
        } catch (LinkageError | RuntimeException exception) {
            LOGGER.error("Fightura failed to initialize. Check Figura/Epic Fight versions.", exception);
        }
    }
}
