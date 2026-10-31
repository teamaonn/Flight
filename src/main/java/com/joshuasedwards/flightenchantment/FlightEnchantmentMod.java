package com.joshuasedwards.flightenchantment;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.Vec3;

/** Flight permission and exhaustion belong to the server, including single-player. */
public final class FlightEnchantmentMod implements ModInitializer {
    public static final String MOD_ID = "flight_enchantment";
    public static final ResourceKey<Enchantment> FLIGHT = ResourceKey.create(Registries.ENCHANTMENT,
            Identifier.fromNamespaceAndPath(MOD_ID, "flight"));
    public static boolean equipped(Player player) {
        var entry = player.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(FLIGHT);
        return entry.isPresent() && EnchantmentHelper.getItemEnchantmentLevel(entry.get(), player.getItemBySlot(EquipmentSlot.FEET)) > 0;
    }
    public static boolean needsLanding(Player player) {
        return !player.isCreative() && !player.isSpectator() && equipped(player)
                && player.getFoodData().getFoodLevel() <= 6 && !player.onGround();
    }
    @Override public void onInitialize() {
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STOPPED.register(server -> FlightState.STATES.clear());
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) tick(player);
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> FlightState.STATES.remove(handler.player.getUUID()));
    }
    private static void tick(ServerPlayer player) {
        var states = FlightState.STATES;
        FlightState.State state = states.get(player.getUUID());
        if (state != null && state.player != player) { states.remove(player.getUUID()); state = null; }
        if (player.isCreative() || player.isSpectator()) { states.remove(player.getUUID()); return; }
        if (!equipped(player) || !player.isAlive()) {
            if (state != null) {
                player.getAbilities().flying = false;
                player.getAbilities().mayfly = false;
                player.onUpdateAbilities();
                states.remove(player.getUUID());
            }
            return;
        }
        if (state == null) { state = new FlightState.State(player); states.put(player.getUUID(), state); }
        if (!state.dimension.equals(player.level().dimension())) { state.last = player.position(); state.dimension = player.level().dimension(); state.landing = false; }
        boolean hungry = player.getFoodData().getFoodLevel() <= 6;
        if (hungry && player.getAbilities().flying && !player.onGround()) state.landing = true;
        if (!hungry) state.landing = false;
        if (hungry && !state.landing && player.getAbilities().flying) {
            player.getAbilities().flying = false;
            player.onUpdateAbilities();
        }
        boolean allow = !hungry || state.landing;
        if (player.getAbilities().mayfly != allow) { player.getAbilities().mayfly = allow; player.onUpdateAbilities(); }
        if (state.landing) {
            player.getAbilities().flying = true;
            player.resetFallDistance();
            player.setDeltaMovement(Vec3.ZERO);
            player.move(MoverType.SELF, new Vec3(0, -0.08, 0));
            // Synchronize the collision-tested descent; player input cannot steer or ascend.
            player.connection.teleport(state.last.x, player.getY(), state.last.z, player.getYRot(), player.getXRot());
            if (player.onGround() || player.getFluidHeight(net.minecraft.tags.FluidTags.WATER) > 0) {
                state.landing = false;
                player.getAbilities().flying = false;
                player.getAbilities().mayfly = false;
                player.onUpdateAbilities();
            }
        } else if (player.getAbilities().flying) {
            player.resetFallDistance();
            double distance = player.position().distanceTo(state.last);
            // Sprinting costs 0.1 exhaustion per metre. Ignore teleports/dimension changes.
            if (state.dimension.equals(player.level().dimension()) && distance < 10)
                player.causeFoodExhaustion((float)(distance * 0.1));
        }
        state.last = player.position(); state.dimension = player.level().dimension();
    }
}
