package me.athlaeos.valhallammo.playerstats.profiles.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.playerstats.format.StatFormat;
import me.athlaeos.valhallammo.playerstats.profiles.Profile;
import me.athlaeos.valhallammo.playerstats.profiles.properties.PropertyBuilder;
import me.athlaeos.valhallammo.skills.skills.Skill;
import me.athlaeos.valhallammo.skills.skills.implementations.BuilderSkill;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

@SuppressWarnings("unused")
public class BuildingProfile extends Profile {
    {
        doubleStat("buildingEXPMultiplier", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P2).perkReward().create());
        floatStat("blockExperienceRate", new PropertyBuilder().format(StatFormat.FLOAT_P2).perkReward().create());
    }

    public double getBuildingEXPMultiplier(){ return getDouble("buildingEXPMultiplier");}
    public void setBuildingEXPMultiplier(double value){ setDouble("buildingEXPMultiplier", value);}
    public float getBlockExperienceRate() { return getFloat("blockExperienceRate"); }
    public void setBlockExperienceRate(float value) { setFloat("blockExperienceRate", value); }

    public BuildingProfile(Player owner) {
        super(owner);
    }

    @Override
    public String getTableName() {
        return "profiles_building";
    }

    private static final NamespacedKey key = new NamespacedKey(ValhallaMMO.getInstance(), "profile_building");

    @Override
    public BuildingProfile getBlankProfile(Player owner) {
        return new BuildingProfile(owner);
    }

    @Override
    public Class<? extends Skill> getSkillType() {
        return BuilderSkill.class;
    }
}
