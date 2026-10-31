package com.joshuasedwards.flightenchantment;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class FlightState {
    private FlightState() {}
    public static final Map<UUID, State> STATES = new HashMap<>();
    public static final class State {
        final ServerPlayer player;
        Vec3 last;
        ResourceKey<Level> dimension;
        boolean landing;
        State(ServerPlayer player) { this.player = player; last = player.position(); dimension = player.level().dimension(); }
    }
}
