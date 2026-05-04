package com.fightura.fightura.client;

import com.fightura.fightura.FighturaMod;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.figuramc.figura.lua.api.event.LuaEvent;
import org.luaj.vm2.LuaValue;
import yesman.epicfight.api.animation.types.EntityState;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

/**
 * Watches every tracked avatar's Epic Fight state and fires Lua events on
 * transitions (attack start/end, hurt, knockdown, motion change, mode change).
 * Avoids mixin-injecting into Epic Fight's animation system by polling once
 * per client tick.
 */
public final class FighturaEventBus {
    private static final Map<UUID, FighturaLuaAPI> INSTANCES = new ConcurrentHashMap<>();
    private static final Map<UUID, Snapshot> LAST_STATE = new ConcurrentHashMap<>();

    private FighturaEventBus() {
    }

    public static void register(UUID owner, FighturaLuaAPI api) {
        if (owner != null && api != null) {
            INSTANCES.put(owner, api);
        }
    }

    public static void clear() {
        INSTANCES.clear();
        LAST_STATE.clear();
    }

    public static void tick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || INSTANCES.isEmpty()) {
            return;
        }

        for (Map.Entry<UUID, FighturaLuaAPI> entry : INSTANCES.entrySet()) {
            UUID owner = entry.getKey();
            FighturaLuaAPI api = entry.getValue();
            Player player = mc.level.getPlayerByUUID(owner);
            if (player == null) {
                LAST_STATE.remove(owner);
                continue;
            }
            try {
                tickPlayer(owner, player, api);
            } catch (RuntimeException exception) {
                FighturaMod.LOGGER.debug("Fightura event tick failed for {}", owner, exception);
            }
        }
    }

    private static void tickPlayer(UUID owner, Player player, FighturaLuaAPI api) {
        PlayerPatch<?> playerPatch = EpicFightCapabilities.getPlayerPatch(player);
        LivingEntityPatch<?> livingPatch = EpicFightCapabilities.getEntityPatch((LivingEntity) player, LivingEntityPatch.class);
        if (livingPatch == null) {
            return;
        }

        EntityState state = livingPatch.getEntityState();
        boolean attacking = state != null && state.attacking();
        boolean hurt = state != null && state.hurt();
        boolean knockDown = state != null && state.knockDown();
        String mode = playerPatch == null ? null : playerPatch.getPlayerMode().name();
        String motion = livingPatch.getCurrentLivingMotion() == null
                ? null
                : livingPatch.getCurrentLivingMotion().toString();

        Snapshot prev = LAST_STATE.get(owner);
        Snapshot now = new Snapshot(attacking, hurt, knockDown, mode, motion);
        LAST_STATE.put(owner, now);
        if (prev == null) {
            return;
        }

        if (attacking && !prev.attacking()) {
            fire(api.attackStart, str(motion));
        } else if (!attacking && prev.attacking()) {
            fire(api.attackEnd, LuaValue.NIL);
        }

        if (hurt && !prev.hurt()) {
            fire(api.hurtStart, LuaValue.NIL);
        }
        if (knockDown && !prev.knockDown()) {
            fire(api.knockDown, LuaValue.NIL);
        }

        if (mode != null && !mode.equals(prev.mode())) {
            fire(api.modeChange, str(mode), str(prev.mode()));
        }
        if (motion != null && !motion.equals(prev.motion())) {
            fire(api.motionChange, str(motion), str(prev.motion()));
        }
    }

    private static void fire(LuaEvent event, LuaValue... args) {
        if (event == null) {
            return;
        }
        try {
            event.call(LuaValue.varargsOf(args));
        } catch (RuntimeException exception) {
            FighturaMod.LOGGER.debug("Fightura event handler raised", exception);
        }
    }

    private static LuaValue str(String value) {
        return value == null ? LuaValue.NIL : LuaValue.valueOf(value);
    }

    public static Entity findOwnerEntity(UUID owner) {
        Minecraft mc = Minecraft.getInstance();
        return mc.level == null || owner == null ? null : mc.level.getPlayerByUUID(owner);
    }

    private record Snapshot(boolean attacking, boolean hurt, boolean knockDown, String mode, String motion) {
    }
}
