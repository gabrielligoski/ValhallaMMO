package me.athlaeos.valhallammo.utility;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.animations.Animation;
import me.athlaeos.valhallammo.animations.AnimationRegistry;
import me.athlaeos.valhallammo.dom.Catch;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatManager;
import me.athlaeos.valhallammo.potioneffects.CustomPotionEffect;
import me.athlaeos.valhallammo.potioneffects.PotionEffectRegistry;
import me.athlaeos.valhallammo.potioneffects.PotionEffectWrapper;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.potion.PotionEffect;

import java.util.Collection;
import java.util.HashSet;

public class Parryer {
    private static final Collection<PotionEffectWrapper> parryEnemyDebuffs = new HashSet<>();
    private static final Collection<PotionEffectWrapper> parrySelfDebuffs = new HashSet<>();
    private static final boolean parrySparks = ValhallaMMO.getPluginConfig().getBoolean("parry_sparks");
    private static final Sound parryActivationSound = Catch.catchOrElse(() -> Sound.valueOf(ValhallaMMO.getPluginConfig().getString("parry_sound")), Sound.BLOCK_IRON_TRAPDOOR_OPEN, "Invalid parry activation sound given, used default");
    private static final Sound parrySuccessSound = Catch.catchOrElse(() -> Sound.valueOf(ValhallaMMO.getPluginConfig().getString("parry_success_sound")), Sound.ENTITY_ITEM_BREAK, "Invalid parry success sound given, used default");
    private static final Sound parryFailedSound = Catch.catchOrElse(() -> Sound.valueOf(ValhallaMMO.getPluginConfig().getString("parry_failed_sound")), Sound.ITEM_SHIELD_BREAK, "Invalid parry fail sound given, used default");
    private static Animation parrySuccessAnimation = AnimationRegistry.getAnimation(AnimationRegistry.ENTITY_SPARK_FLASH.id());
    private static Animation parryFailAnimation = null;

    static {
        YamlConfiguration c = ValhallaMMO.getPluginConfig();
        ConfigurationSection enemySection = c.getConfigurationSection("parry_enemy_debuffs");
        if (enemySection != null) {
            for (String effect : enemySection.getKeys(false)) {
                parryEnemyDebuffs.add(PotionEffectRegistry.getEffect(effect).setAmplifier(c.getDouble("parry_enemy_debuffs." + effect)));
            }
        }
        ConfigurationSection selfSection = c.getConfigurationSection("parry_failed_debuffs");
        if (selfSection != null) {
            for (String effect : selfSection.getKeys(false)) {
                parrySelfDebuffs.add(PotionEffectRegistry.getEffect(effect).setAmplifier(c.getDouble("parry_failed_debuffs." + effect)));
            }
        }
    }

    /**
     * Activates a parry effect, all times should be given in game ticks
     * @param e the entity to activate the parry for
     * @param activeFor how long the entity should be parrying for, in game ticks. If attacked during this time period, the damage is reduced and the attacker is debuffed
     * @param vulnerableFor how long the entity should be vulnerable for, in game ticks. Should be longer than activeFor. If attacked during the time period,
     *                      and activeFor hasn't expired yet, the entity is debuffed instead.
     * @param cooldown the cooldown of the ability, in game ticks.
     */
    public static void forceParry(LivingEntity e, int activeFor, int vulnerableFor, int cooldown) {
        Timer.setCooldown(e.getUniqueId(), activeFor * 50, "parry_effective");
        Timer.setCooldown(e.getUniqueId(), vulnerableFor * 50, "parry_vulnerable");
        Timer.setCooldownIgnoreIfPermission(e, cooldown * 50, "parry_cooldown");
        e.getWorld().playSound(e.getLocation(), parryActivationSound, 1F, 1F);
    }

    public static void attemptParry(LivingEntity e){
        if (!Timer.isCooldownPassed(e.getUniqueId(), "parry_cooldown")) return;
        int cooldown = (int) AccumulativeStatManager.getCachedStats("PARRY_COOLDOWN", e, 10000, true) - 1;
        if (cooldown < 0) return;
        int activeDuration = (int) AccumulativeStatManager.getCachedStats("PARRY_EFFECTIVENESS_DURATION", e, 10000, true);
        int vulnerableDuration = (int) AccumulativeStatManager.getCachedStats("PARRY_VULNERABLE_DURATION", e, 10000, true);
        forceParry(e, activeDuration, vulnerableDuration, cooldown);
    }

    /**
     * Handles the parry effect on an EntityDamageByEntityEvent, returning the damage multiplier after a parry
     * @param e the event
     * @return the damage multiplier of the taken damage
     */
    public static double handleParry(EntityDamageByEntityEvent e){
        if (e.getDamager() instanceof LivingEntity a && e.getEntity() instanceof LivingEntity v){
            if (!Timer.isCooldownPassed(v.getUniqueId(), "parry_effective")){
                // parry successful
                int debuffDuration = (int) AccumulativeStatManager.getCachedRelationalStats("PARRY_ENEMY_DEBUFF_DURATION", v, a, 10000, true);
                double damageReduction = (int) AccumulativeStatManager.getCachedRelationalStats("PARRY_DAMAGE_REDUCTION", v, a, 10000, true);
                for (PotionEffectWrapper wrapper : parryEnemyDebuffs){
                    PotionEffectWrapper copy = PotionEffectRegistry.getEffect(wrapper.getEffect()).setAmplifier(wrapper.getAmplifier()).setDuration(debuffDuration);
                    if (!wrapper.isVanilla()) PotionEffectRegistry.addEffect(a, v, new CustomPotionEffect(copy, debuffDuration, copy.getAmplifier()), false, 1, EntityPotionEffectEvent.Cause.ATTACK);
                    else a.addPotionEffect(new PotionEffect(copy.getVanillaEffect(), debuffDuration, (int) copy.getAmplifier()));
                }
                if (parrySparks && parrySuccessAnimation != null) parrySuccessAnimation.animate(v, v.getLocation(), v.getEyeLocation().getDirection(), 0);
                e.setDamage(e.getDamage() * (1 - damageReduction));
                v.getWorld().playSound(v.getLocation(), parrySuccessSound, 1F, 1F);

                double cooldownReduction = AccumulativeStatManager.getCachedRelationalStats("PARRY_SUCCESS_COOLDOWN_REDUCTION", v, a, 10000, true);
                long cooldown = Timer.getCooldown(v.getUniqueId(), "parry_cooldown");
                if (cooldown > 0) Timer.setCooldown(v.getUniqueId(), (int) (cooldown * (1 - cooldownReduction)), "parry_cooldown");
                Timer.setCooldown(v.getUniqueId(), 0, "parry_vulnerable");
                Timer.setCooldown(v.getUniqueId(), 0, "parry_effective");
                return 1 - damageReduction;
            } else if (!Timer.isCooldownPassed(v.getUniqueId(), "parry_vulnerable")){
                // parry failed
                int debuffDuration = (int) AccumulativeStatManager.getCachedRelationalStats("PARRY_SELF_DEBUFF_DURATION", v, a, 10000, true);
                for (PotionEffectWrapper wrapper : parrySelfDebuffs){
                    PotionEffectWrapper copy = PotionEffectRegistry.getEffect(wrapper.getEffect()).setAmplifier(wrapper.getAmplifier()).setDuration(debuffDuration);
                    if (!wrapper.isVanilla()) PotionEffectRegistry.addEffect(v, a, new CustomPotionEffect(copy, debuffDuration, copy.getAmplifier()), false, 1, EntityPotionEffectEvent.Cause.ATTACK);
                    else v.addPotionEffect(new PotionEffect(copy.getVanillaEffect(), debuffDuration, (int) copy.getAmplifier()));
                }
                if (parryFailAnimation != null) parryFailAnimation.animate(v, v.getLocation(), v.getEyeLocation().getDirection(), 0);
                v.getWorld().playSound(v.getLocation(), parryFailedSound, 1F, 1F);
            }
            Timer.setCooldown(v.getUniqueId(), 0, "parry_vulnerable");
            Timer.setCooldown(v.getUniqueId(), 0, "parry_effective");
        }
        return 1;
    }

    public static void setParrySuccessAnimation(Animation parrySuccessAnimation) {
        Parryer.parrySuccessAnimation = parrySuccessAnimation;
    }

    public static Animation getParrySuccessAnimation() {
        return parrySuccessAnimation;
    }

    public static void setParryFailAnimation(Animation parryFailAnimation) {
        Parryer.parryFailAnimation = parryFailAnimation;
    }

    public static Animation getParryFailAnimation() {
        return parryFailAnimation;
    }
}
