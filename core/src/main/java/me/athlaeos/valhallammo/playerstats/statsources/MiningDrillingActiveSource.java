package me.athlaeos.valhallammo.playerstats.statsources;

import me.athlaeos.valhallammo.playerstats.AccumulativeStatSource;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileCache;
import me.athlaeos.valhallammo.playerstats.profiles.implementations.MiningDiggingProfile;
import me.athlaeos.valhallammo.skills.skills.SkillRegistry;
import me.athlaeos.valhallammo.skills.skills.implementations.MiningDiggingSkill;
import me.athlaeos.valhallammo.utility.Timer;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class MiningDrillingActiveSource implements AccumulativeStatSource {
    @Override
    public double fetch(Entity statPossessor, boolean use) {
        MiningDiggingSkill MiningDiggingSkill = SkillRegistry.isRegistered(MiningDiggingSkill.class) ? (MiningDiggingSkill) SkillRegistry.getSkill(MiningDiggingSkill.class) : null;
        if (statPossessor instanceof Player p && MiningDiggingSkill != null && !Timer.isCooldownPassed(statPossessor.getUniqueId(), "mining_drilling_duration")){
            Block b = p.getTargetBlockExact(8);
            if (b == null || !MiningDiggingSkill.getDropsExpValues().containsKey(b.getType())) return 0;
            MiningDiggingProfile profile = ProfileCache.getOrCache(p, MiningDiggingProfile.class);
            return profile.getDrillingSpeedBonus();
        }
        return 0;
    }
}
