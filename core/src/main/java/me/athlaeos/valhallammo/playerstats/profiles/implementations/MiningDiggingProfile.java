package me.athlaeos.valhallammo.playerstats.profiles.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.item.MiningSpeed;
import me.athlaeos.valhallammo.playerstats.format.StatFormat;
import me.athlaeos.valhallammo.playerstats.profiles.Profile;
import me.athlaeos.valhallammo.playerstats.profiles.properties.BooleanProperties;
import me.athlaeos.valhallammo.playerstats.profiles.properties.PropertyBuilder;
import me.athlaeos.valhallammo.skills.skills.Skill;
import me.athlaeos.valhallammo.skills.skills.implementations.MiningDiggingSkill;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.version.EnchantmentMappings;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;

import java.util.Collection;

@SuppressWarnings("unused")
public class MiningDiggingProfile extends Profile {
    {
        floatStat("miningDrops", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("miningLuck", new PropertyBuilder().format(StatFormat.FLOAT_P2).perkReward().create());
        floatStat("blastingDrops", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("blastingLuck", new PropertyBuilder().format(StatFormat.FLOAT_P2).perkReward().create());
        floatStat("blockExperienceRate", new PropertyBuilder().format(StatFormat.FLOAT_P2).perkReward().create());
        floatStat("blockExperienceMultiplier", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("tntBlastRadius", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("tntDamageReduction", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        floatStat("miningSpeedBonus", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        stringSetStat("unbreakableBlocks");
        intStat("blastFortuneLevel", new PropertyBuilder().format(StatFormat.INT).perkReward().create());
        booleanStat("blastingInstantPickup", new BooleanProperties(true, true));

        booleanStat("veinMiningUnlocked", new BooleanProperties(true, true));
        booleanStat("veinMiningInstantPickup", new BooleanProperties(true, true));
        intStat("veinMiningCooldown", new PropertyBuilder().format(StatFormat.TIME_SECONDS_BASE_20_P1).perkReward().create());
        stringSetStat("veinMinerValidBlocks");

        booleanStat("drillingUnlocked", new BooleanProperties(true, true));
        floatStat("drillingSpeedBonus", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());
        intStat("drillingCooldown", new PropertyBuilder().format(StatFormat.TIME_SECONDS_BASE_20_P1).perkReward().create());
        intStat("drillingDuration", new PropertyBuilder().format(StatFormat.TIME_SECONDS_BASE_20_P1).perkReward().create());

        doubleStat("miningEXPMultiplier", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P2).perkReward().create());

        stringSetStat("emptyHandToolMaterial");
        intStat("emptyHandToolFortune", new PropertyBuilder().format(StatFormat.INT).perkReward().create());
        floatStat("emptyHandToolMiningStrength", new PropertyBuilder().format(StatFormat.FLOAT_P1).perkReward().create());

        floatStat("diggingSpeedBonus", new PropertyBuilder().format(StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).perkReward().create());

        floatStat("archaeologyRepeatChance", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P1).perkReward().create()); // chance for suspicious block to regenerate after brushing
        floatStat("archaeologyLuck", new PropertyBuilder().format(StatFormat.FLOAT_P1).perkReward().create()); // extra luck for archaeology loot tables
        floatStat("archaeologySandGenerationChance", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P6).perkReward().create()); // chance for adjacent blocks to a mined block to turn into suspicious sand
        floatStat("archaeologyGravelGenerationChance", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P6).perkReward().create()); // same with gravel
        floatStat("archaeologySandNearStructureGenerationChance", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P6).perkReward().create()); // same as generation chance, but only if block is near a structure
        floatStat("archaeologyGravelNearStructureGenerationChance", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P6).perkReward().create()); // same with gravel
        floatStat("archaeologyDefaultRareLootChance", new PropertyBuilder().format(StatFormat.PERCENTILE_BASE_1_P1).perkReward().create()); // chance for generated brushable block to contain a rare loot table if no other is specified
    }

    public boolean isVeinMiningUnlocked() { return getBoolean("veinMiningUnlocked"); }
    public void setVeinMiningUnlocked(boolean value) { setBoolean("veinMiningUnlocked", value); }
    public boolean isDrillingUnlocked() { return getBoolean("drillingUnlocked"); }
    public void setDrillingUnlocked(boolean value) { setBoolean("drillingUnlocked", value); }
    public boolean isBlastingInstantPickup() { return getBoolean("blastingInstantPickup"); }
    public void setBlastingInstantPickup(boolean value) { setBoolean("blastingInstantPickup", value); }
    public boolean isVeinMiningInstantPickup() { return getBoolean("veinMiningInstantPickup"); }
    public void setVeinMiningInstantPickup(boolean value) { setBoolean("veinMiningInstantPickup", value); }

    public int getBlastFortuneLevel() { return getInt("blastFortuneLevel"); }
    public void setBlastFortuneLevel(int value) { setInt("blastFortuneLevel", value); }
    public int getVeinMiningCooldown() { return getInt("veinMiningCooldown"); }
    public void setVeinMiningCooldown(int value) { setInt("veinMiningCooldown", value); }
    public int getDrillingCooldown() { return getInt("drillingCooldown"); }
    public void setDrillingCooldown(int value) { setInt("drillingCooldown", value); }
    public int getDrillingDuration() { return getInt("drillingDuration"); }
    public void setDrillingDuration(int value) { setInt("drillingDuration", value); }
    public int getEmptyHandToolFortune() { return getInt("emptyHandToolFortune"); }
    public void setEmptyHandToolFortune(int value) { setInt("emptyHandToolFortune", value); }

    public Collection<String> getUnbreakableBlocks() { return getStringSet("unbreakableBlocks"); }
    public void setUnbreakableBlocks(Collection<String> value) { setStringSet("unbreakableBlocks", value); }
    public Collection<String> getVeinMinerValidBlocks() { return getStringSet("veinMinerValidBlocks"); }
    public void setVeinMinerValidBlocks(Collection<String> value) { setStringSet("veinMinerValidBlocks", value); }
    public Collection<String> getEmptyHandToolMaterial() { return getStringSet("emptyHandToolMaterial"); }
    public void setEmptyHandToolMaterial(Collection<String> value) { setStringSet("emptyHandToolMaterial", value); }

    public float getMiningDrops() { return getFloat("miningDrops"); }
    public void setMiningDrops(float value) { setFloat("miningDrops", value); }
    public float getMiningLuck() { return getFloat("miningLuck"); }
    public void setMiningLuck(float value) { setFloat("miningLuck", value); }
    public float getBlastingDrops() { return getFloat("blastingDrops"); }
    public void setBlastingDrops(float value) { setFloat("blastingDrops", value); }
    public float getBlastingLuck() { return getFloat("blastingLuck"); }
    public void setBlastingLuck(float value) { setFloat("blastingLuck", value); }
    public float getTntBlastRadius() { return getFloat("tntBlastRadius"); }
    public void setTntBlastRadius(float value) { setFloat("tntBlastRadius", value); }
    public float getTntDamageReduction() { return getFloat("tntDamageReduction"); }
    public void setTntDamageReduction(float value) { setFloat("tntDamageReduction", value); }
    public float getMiningSpeedBonus() { return getFloat("miningSpeedBonus"); }
    public void setMiningSpeedBonus(float value) { setFloat("miningSpeedBonus", value); }
    public float getDrillingSpeedBonus() { return getFloat("drillingSpeedBonus"); }
    public void setDrillingSpeedBonus(float value) { setFloat("drillingSpeedBonus", value); }
    public float getBlockExperienceRate() { return getFloat("blockExperienceRate"); }
    public void setBlockExperienceRate(float value) { setFloat("blockExperienceRate", value); }
    public float getBlockExperienceMultiplier() { return getFloat("blockExperienceMultiplier"); }
    public void setBlockExperienceMultiplier(float value) { setFloat("blockExperienceMultiplier", value); }
    public float getEmptyHandToolMiningStrength() { return getFloat("emptyHandToolMiningStrength"); }
    public void setEmptyHandToolMiningStrength(float value) { setFloat("emptyHandToolMiningStrength", value); }

    public double getMiningEXPMultiplier(){ return getDouble("miningEXPMultiplier");}
    public void setMiningEXPMultiplier(double value){ setDouble("miningEXPMultiplier", value);}


    public float getDiggingSpeedBonus() { return getFloat("diggingSpeedBonus"); }
    public void setDiggingSpeedBonus(float value) { setFloat("diggingSpeedBonus", value); }

    public float getArchaeologyRepeatChance() { return getFloat("archaeologyRepeatChance"); }
    public void setArchaeologyRepeatChance(float value) { setFloat("archaeologyRepeatChance", value); }
    public float getArchaeologyLuck() { return getFloat("archaeologyLuck"); }
    public void setArchaeologyLuck(float value) { setFloat("archaeologyLuck", value); }
    public float getArchaeologySandGenerationChance() { return getFloat("archaeologySandGenerationChance"); }
    public void setArchaeologySandGenerationChance(float value) { setFloat("archaeologySandGenerationChance", value); }
    public float getArchaeologyGravelGenerationChance() { return getFloat("archaeologyGravelGenerationChance"); }
    public void setArchaeologyGravelGenerationChance(float value) { setFloat("archaeologyGravelGenerationChance", value); }
    public float getArchaeologySandNearStructureGenerationChance() { return getFloat("archaeologySandNearStructureGenerationChance"); }
    public void setArchaeologySandNearStructureGenerationChance(float value) { setFloat("archaeologySandNearStructureGenerationChance", value); }
    public float getArchaeologyGravelNearStructureGenerationChance() { return getFloat("archaeologyGravelNearStructureGenerationChance"); }
    public void setArchaeologyGravelNearStructureGenerationChance(float value) { setFloat("archaeologyGravelNearStructureGenerationChance", value); }
    public float getArchaeologyDefaultRareLootChance() { return getFloat("archaeologyDefaultRareLootChance"); }
    public void setArchaeologyDefaultRareLootChance(float value) { setFloat("archaeologyDefaultRareLootChance", value); }

    private ItemBuilder emptyHandTool = null;

    public ItemBuilder getEmptyHandTool() {
        return emptyHandTool;
    }

    @Override
    public void onCacheRefresh() {
        String value = getEmptyHandToolMaterial().stream().findFirst().orElse(null);
        if (value == null) {
            emptyHandTool = null;
            return;
        }
        Material material = ItemUtils.stringToMaterial(value, null);
        if (material == null) {
            emptyHandTool = null;
            return;
        }
        int level = getEmptyHandToolFortune();
        ItemBuilder item = new ItemBuilder(material);
        if (level != 0) item.enchant(level < 0 ? Enchantment.SILK_TOUCH : EnchantmentMappings.FORTUNE.getEnchantment(), level < 0 ? 1 : level);
        if (getEmptyHandToolMiningStrength() > 0) MiningSpeed.setMultiplier(item.getMeta(), getEmptyHandToolMiningStrength());
        emptyHandTool = new ItemBuilder(item.get());
    }

    public MiningDiggingProfile(Player owner) {
        super(owner);
    }

    @Override
    public String getTableName() {
        return "profiles_mining_digging";
    }

    private static final NamespacedKey key = new NamespacedKey(ValhallaMMO.getInstance(), "profiles_mining_digging");

    @Override
    public MiningDiggingProfile getBlankProfile(Player owner) {
        return new MiningDiggingProfile(owner);
    }

    @Override
    public Class<? extends Skill> getSkillType() {
        return MiningDiggingSkill.class;
    }
}
