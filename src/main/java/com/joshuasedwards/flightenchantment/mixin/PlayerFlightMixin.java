package com.joshuasedwards.flightenchantment.mixin;

import com.joshuasedwards.flightenchantment.FlightEnchantmentMod;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerFlightMixin {
    @Unique private boolean flight_enchantment$wasFlying;
    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
    private void flight_enchantment$controlledClientDescent(net.minecraft.world.phys.Vec3 input, CallbackInfo ci) {
        Player player = (Player)(Object)this;
        if (player.level().isClientSide() && player.getAbilities().flying && FlightEnchantmentMod.needsLanding(player)) {
            player.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
            player.move(net.minecraft.world.entity.MoverType.SELF, new net.minecraft.world.phys.Vec3(0, -0.08, 0));
            player.resetFallDistance();
            ci.cancel();
        }
    }

    @Inject(method = "hurtServer", at = @At("HEAD"))
    private void flight_enchantment$beforeDamage(ServerLevel level, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        Player player = (Player)(Object)this;
        flight_enchantment$wasFlying = player.getAbilities().flying && FlightEnchantmentMod.equipped(player);
    }
    @Inject(method = "hurtServer", at = @At("RETURN"))
    private void flight_enchantment$afterDamage(ServerLevel level, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        Player player = (Player)(Object)this;
        if (flight_enchantment$wasFlying && player.isAlive() && FlightEnchantmentMod.equipped(player)) {
            player.getAbilities().mayfly = true;
            player.getAbilities().flying = true;
            player.resetFallDistance();
            if (player instanceof ServerPlayer serverPlayer) serverPlayer.onUpdateAbilities();
        }
    }
}
