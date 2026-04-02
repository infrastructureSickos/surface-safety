package com.infrastructuresickos.surface_safety;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Random;

/**
 * Implements SurfaceSafety mechanics:
 *
 *  Safe zone (Y 47–79): zombies and skeletons do not naturally spawn.
 *
 *  High altitude (Y 79+): zombies and skeletons may spawn with leather gear,
 *    chance scaling from 0% at Y 79 to 100% at build height (Y 320). Each
 *    equipment slot is rolled independently.
 *
 *  Deep underground (Y 0–47): zombies and skeletons may spawn with iron gear,
 *    chance scaling from 0% at Y 47 to 100% at Y 0. Each equipment slot is
 *    rolled independently.
 *
 *  Only natural spawns are affected (not spawn eggs or spawners).
 *
 * Registered manually on the FORGE bus — do NOT add @Mod.EventBusSubscriber.
 */
public class SurfaceSafetyEventHandler {

    // Y boundaries (inclusive)
    private static final int SAFE_ZONE_MIN = 47;
    private static final int SAFE_ZONE_MAX = 79;
    private static final int BUILD_HEIGHT   = 320;

    private static final Random RANDOM = new Random();

    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD,
            EquipmentSlot.CHEST,
            EquipmentSlot.LEGS,
            EquipmentSlot.FEET
    };

    /**
     * FinalizeSpawn fires just before a mob is added to the world. We cancel
     * zombie/skeleton spawns in the safe zone and equip them above/below it.
     */
    @SubscribeEvent
    public void onFinalizeSpawn(MobSpawnEvent.FinalizeSpawn event) {
        // Only affect natural spawns
        if (event.getSpawnType() != MobSpawnType.NATURAL) return;

        if (!(event.getEntity() instanceof Zombie) && !(event.getEntity() instanceof Skeleton)) return;

        int y = (int) Math.floor(event.getY());

        // --- Safe zone: cancel the spawn ---
        if (y >= SAFE_ZONE_MIN && y <= SAFE_ZONE_MAX) {
            event.setSpawnCancelled(true);
            return;
        }

        // --- High altitude: leather gear ---
        if (y > SAFE_ZONE_MAX) {
            double chance = Math.min(1.0, (double)(y - SAFE_ZONE_MAX) / (BUILD_HEIGHT - SAFE_ZONE_MAX));
            applyGear(event, chance, true);
            return;
        }

        // --- Deep underground: iron gear ---
        if (y < SAFE_ZONE_MIN) {
            double chance = Math.min(1.0, (double)(SAFE_ZONE_MIN - y) / SAFE_ZONE_MIN);
            applyGear(event, chance, false);
        }
    }

    /**
     * Rolls each equipment slot independently. Armor is leather or iron.
     * Weapon is wooden sword (zombie) / bow (skeleton) for leather tier, or
     * iron sword (zombie) / bow (skeleton) for iron tier.
     */
    private void applyGear(MobSpawnEvent.FinalizeSpawn event, double chance, boolean leather) {
        Mob mob = event.getEntity();

        // Armor slots
        ItemStack[] armorItems = leather
                ? new ItemStack[]{
                        new ItemStack(Items.LEATHER_HELMET),
                        new ItemStack(Items.LEATHER_CHESTPLATE),
                        new ItemStack(Items.LEATHER_LEGGINGS),
                        new ItemStack(Items.LEATHER_BOOTS)
                  }
                : new ItemStack[]{
                        new ItemStack(Items.IRON_HELMET),
                        new ItemStack(Items.IRON_CHESTPLATE),
                        new ItemStack(Items.IRON_LEGGINGS),
                        new ItemStack(Items.IRON_BOOTS)
                  };

        for (int i = 0; i < ARMOR_SLOTS.length; i++) {
            if (RANDOM.nextDouble() < chance) {
                mob.setItemSlot(ARMOR_SLOTS[i], armorItems[i]);
                mob.setDropChance(ARMOR_SLOTS[i], 0.0f); // don't drop gear on death
            }
        }

        // Weapon slot
        if (RANDOM.nextDouble() < chance) {
            ItemStack weapon;
            if (mob instanceof Zombie) {
                weapon = leather ? new ItemStack(Items.WOODEN_SWORD) : new ItemStack(Items.IRON_SWORD);
            } else {
                // Skeleton — bow regardless of tier
                weapon = new ItemStack(Items.BOW);
            }
            mob.setItemSlot(EquipmentSlot.MAINHAND, weapon);
            mob.setDropChance(EquipmentSlot.MAINHAND, 0.0f);
        }
    }
}
