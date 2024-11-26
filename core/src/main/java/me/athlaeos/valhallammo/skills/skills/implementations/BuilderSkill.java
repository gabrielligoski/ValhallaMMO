package me.athlaeos.valhallammo.skills.skills.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.configuration.ConfigManager;
import me.athlaeos.valhallammo.event.PlayerSkillExperienceGainEvent;
import me.athlaeos.valhallammo.hooks.WorldGuardHook;
import me.athlaeos.valhallammo.listeners.LootListener;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatManager;
import me.athlaeos.valhallammo.playerstats.profiles.Profile;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileCache;
import me.athlaeos.valhallammo.playerstats.profiles.implementations.BuildingProfile;
import me.athlaeos.valhallammo.playerstats.profiles.implementations.MiningDiggingProfile;
import me.athlaeos.valhallammo.skills.skills.Skill;
import me.athlaeos.valhallammo.utility.*;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class BuilderSkill extends Skill implements Listener {
    private final Map<Material, Double> dropsExpValues = new HashMap<>();

    private boolean forgivingDropMultipliers = true; // if false, depending on drop multiplier, drops may be reduced to 0. If true, this will be at least 1

    public BuilderSkill(String type) {
        super(type);
    }

    @Override
    public void loadConfiguration() {
        ValhallaMMO.getInstance().save("skills/builder_progression.yml");
        ValhallaMMO.getInstance().save("skills/builder.yml");

        YamlConfiguration skillConfig = ConfigManager.getConfig("skills/builder.yml").get();
        YamlConfiguration progressionConfig = ConfigManager.getConfig("skills/builder_progression.yml").get();

        loadCommonConfig(skillConfig, progressionConfig);

        forgivingDropMultipliers = skillConfig.getBoolean("forgiving_multipliers");

        ValhallaMMO.getInstance().getServer().getPluginManager().registerEvents(this, ValhallaMMO.getInstance());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void lootTableDrops(BlockBreakEvent e){
        double dropMultiplier = AccumulativeStatManager.getCachedStats("BUILDING_DROP_MULTIPLIER", e.getPlayer(), 10000, true);
        // multiply any applicable prepared drops and grant exp for them. After the extra drops from a BlockBreakEvent the drops are cleared
        ItemUtils.multiplyItems(LootListener.getPreparedExtraDrops(e.getBlock()), 1 + dropMultiplier, forgivingDropMultipliers, (i) -> dropsExpValues.containsKey(i.getType()));

        double expQuantity = 0;
        for (ItemStack i : LootListener.getPreparedExtraDrops(e.getBlock())){
            if (ItemUtils.isEmpty(i)) continue;
            expQuantity += dropsExpValues.getOrDefault(i.getType(), 0D) * i.getAmount();
        }
        addEXP(e.getPlayer(), expQuantity, false, PlayerSkillExperienceGainEvent.ExperienceGainReason.SKILL_ACTION);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onItemsDropped(BlockDropItemEvent e){
        double dropMultiplier = AccumulativeStatManager.getCachedStats("BUILDING_DROP_MULTIPLIER", e.getPlayer(), 10000, true);
        // multiply the item drops from the event itself and grant exp for the initial items and extra drops
        List<ItemStack> extraDrops = ItemUtils.multiplyDrops(e.getItems(), 1 + dropMultiplier, forgivingDropMultipliers, (i) -> dropsExpValues.containsKey(i.getItemStack().getType()));
        if (!extraDrops.isEmpty()) LootListener.prepareBlockDrops(e.getBlock(), extraDrops);

        double expQuantity = 0;
        for (Item i : e.getItems()){
            if (ItemUtils.isEmpty(i.getItemStack())) continue;
            expQuantity += dropsExpValues.getOrDefault(i.getItemStack().getType(), 0D) * i.getItemStack().getAmount();
        }
        for (ItemStack i : extraDrops){
            if (ItemUtils.isEmpty(i)) continue;
            expQuantity += dropsExpValues.getOrDefault(i.getType(), 0D) * i.getAmount();
        }
        addEXP(e.getPlayer(), expQuantity, false, PlayerSkillExperienceGainEvent.ExperienceGainReason.SKILL_ACTION);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPlaced(BlockPlaceEvent e) {
        addEXP(e.getPlayer(), 8, false, PlayerSkillExperienceGainEvent.ExperienceGainReason.SKILL_ACTION);

        BuildingProfile profile = ProfileCache.getOrCache(e.getPlayer(), BuildingProfile.class);
        if (!hasPermissionAccess(e.getPlayer())) return;

        int experience = Utils.randomAverage(profile.getBlockExperienceRate());
        e.getPlayer().giveExp(experience);
    }

    @Override
    public boolean isLevelableSkill() {
        return true;
    }

    @Override
    public Class<? extends Profile> getProfileType() {
        return BuildingProfile.class;
    }

    @Override
    public int getSkillTreeMenuOrderPriority() {
        return 25;
    }

    @Override
    public void addEXP(Player p, double amount, boolean silent, PlayerSkillExperienceGainEvent.ExperienceGainReason reason) {
        if (WorldGuardHook.inDisabledRegion(p.getLocation(), p, WorldGuardHook.VMMO_SKILL_BUILDING)) return;
        if (reason == PlayerSkillExperienceGainEvent.ExperienceGainReason.SKILL_ACTION) {
            amount *= (1 + AccumulativeStatManager.getStats("BUILDING_EXP_GAIN", p, true));
        }
        super.addEXP(p, amount, silent, reason);
    }

    public Map<Material, Double> getDropsExpValues() {
        return dropsExpValues;
    }
}
