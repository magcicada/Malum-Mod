package com.sammy.malum.common.item.curiosities.weapons.scythe;

import com.google.common.collect.*;
import com.sammy.malum.common.enchantment.scythe.*;
import com.sammy.malum.common.item.*;
import com.sammy.malum.core.helpers.*;
import com.sammy.malum.registry.common.*;
import com.sammy.malum.registry.common.item.*;
import net.minecraft.nbt.*;
import net.minecraft.sounds.*;
import net.minecraft.util.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.*;
import net.minecraft.world.level.*;
import net.minecraftforge.event.*;
import net.minecraftforge.event.entity.living.*;
import team.lodestar.lodestone.helpers.*;
import team.lodestar.lodestone.systems.item.*;

public class MalumScytheItem extends ModCombatItem implements IMalumEventResponderItem {

    public MalumScytheItem(Tier tier, float damage, float speed, Properties builderIn) {
        super(tier, damage + 3 + tier.getAttackDamageBonus(), speed - 3.2f, builderIn);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        if (stack.getEnchantmentLevel(EnchantmentRegistry.REBOUND.get()) > 0) {
            ReboundEnchantment.throwScythe(level, player, hand, stack);
            return InteractionResultHolder.success(stack);
        }
        if (stack.getEnchantmentLevel(EnchantmentRegistry.ASCENSION.get()) > 0) {
            AscensionEnchantment.triggerAscension(level, player, hand, stack);
            return InteractionResultHolder.success(stack);
        }
        return super.use(level, player, hand);
    }

    @Override
    public void hurtEvent(LivingHurtEvent event, LivingEntity attacker, LivingEntity target, ItemStack stack) {
        var level = attacker.level();
        if (level.isClientSide()) {
            return;
        }
        if (!event.getSource().is(DamageTypeRegistry.SCYTHE_MELEE)) {
            return;
        }
        boolean canSweep = canSweep(attacker);
        var particle = ParticleHelper.createSlashingEffect(ParticleEffectTypeRegistry.SCYTHE_SLASH);
        if (stack.getItem() instanceof ISpiritAffiliatedItem spiritAffiliatedItem) {
            particle.setSpiritType(spiritAffiliatedItem);
        }
        if (!canSweep) {
            SoundHelper.playSound(attacker, getScytheSound(false), 1, 0.75f);
            particle.setVertical().spawnForwardSlashingParticle(attacker);
            return;
        }
        SoundHelper.playSound(attacker, getScytheSound(true), 1, 1);
        particle.mirrorRandomly(attacker.getRandom()).spawnForwardSlashingParticle(attacker);

        int sweeping = EnchantmentHelper.getEnchantmentLevel(Enchantments.SWEEPING_EDGE, attacker);
        float damage = event.getAmount() * (0.5f + EnchantmentHelper.getSweepingDamageRatio(attacker));
        float radius = 1 + sweeping * 0.25f;
        level.getEntities(attacker, target.getBoundingBox().inflate(radius)).forEach(e -> {
            if (e instanceof LivingEntity livingEntity) {
                if (livingEntity.isAlive()) {
                    livingEntity.hurt((DamageTypeHelper.create(level, DamageTypeRegistry.SCYTHE_SWEEP, attacker)), damage);
                    livingEntity.knockback(0.4F,
                            Mth.sin(attacker.getYRot() * ((float) Math.PI / 180F)),
                            (-Mth.cos(attacker.getYRot() * ((float) Math.PI / 180F))));
                }
            }
        });
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        if (enchantment.equals(Enchantments.SWEEPING_EDGE)) {
            return true;
        }
        return super.canApplyAtEnchantingTable(stack, enchantment);
    }

    public SoundEvent getScytheSound(boolean canSweep) {
        return canSweep ? SoundRegistry.SCYTHE_SWEEP.get() : SoundRegistry.SCYTHE_CUT.get();
    }

    public static boolean canSweep(LivingEntity attacker) {
        return !CurioHelper.hasCurioEquipped(attacker, ItemRegistry.NECKLACE_OF_THE_NARROW_EDGE.get()) &&
                !CurioHelper.hasCurioEquipped(attacker, ItemRegistry.NECKLACE_OF_THE_HIDDEN_BLADE.get());
    }
}