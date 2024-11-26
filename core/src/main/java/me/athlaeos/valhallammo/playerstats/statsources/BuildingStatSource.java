package me.athlaeos.valhallammo.playerstats.statsources;

import me.athlaeos.valhallammo.playerstats.AccumulativeStatSource;
import me.athlaeos.valhallammo.playerstats.profiles.Profile;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileCache;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileRegistry;
import me.athlaeos.valhallammo.playerstats.profiles.implementations.BuildingProfile;
import me.athlaeos.valhallammo.skills.skills.SkillRegistry;
import me.athlaeos.valhallammo.skills.skills.implementations.BuilderSkill;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class BuildingStatSource implements AccumulativeStatSource {
    private final String stat;
    private final Class<? extends Number> numberType;
    private final double def;

    /**
     * Grants stats only if the player is directly looking at a block the MiningSkill can grant exp for
     */
    public BuildingStatSource(String stat){
        this.stat = stat;

        Profile baseProfile = ProfileRegistry.getBlankProfile(null, BuildingProfile.class);
        if (baseProfile.intStatNames().contains(stat)) {
            def = baseProfile.getDefaultInt(stat);
            numberType = Integer.class;
        } else if (baseProfile.floatStatNames().contains(stat)) {
            def = baseProfile.getDefaultFloat(stat);
            numberType = Float.class;
        } else if (baseProfile.doubleStatNames().contains(stat)) {
            def = baseProfile.getDefaultDouble(stat);
            numberType = Double.class;
        } else {
            def = 0;
            numberType = null;
        }

        if (numberType == null) {
            throw new IllegalArgumentException("BuildingStatSource:" + BuildingProfile.class.getSimpleName() + " with stat " + stat +
                    " was initialized, but this profile type does not have such a stat");
        }
    }

    @Override
    public double fetch(Entity statPossessor, boolean use) {
        BuilderSkill buildingSkill = SkillRegistry.isRegistered(BuilderSkill.class) ? (BuilderSkill) SkillRegistry.getSkill(BuilderSkill.class) : null;
        if (statPossessor instanceof Player p && buildingSkill != null){
            Block b = p.getTargetBlockExact(8);
            if (b == null || !buildingSkill.getDropsExpValues().containsKey(b.getType())) return def;

            BuildingProfile profile = ProfileCache.getOrCache(p, BuildingProfile.class);
            if (numberType.equals(Integer.class)) return profile.getInt(stat);
            if (numberType.equals(Float.class)) return profile.getFloat(stat);
            return profile.getDouble(stat);
        }
        return def;
    }
}
