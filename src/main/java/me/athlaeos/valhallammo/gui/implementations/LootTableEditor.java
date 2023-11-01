package me.athlaeos.valhallammo.gui.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.crafting.CustomRecipeRegistry;
import me.athlaeos.valhallammo.crafting.ToolRequirementType;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.ingredientconfiguration.IngredientChoice;
import me.athlaeos.valhallammo.crafting.ingredientconfiguration.RecipeOption;
import me.athlaeos.valhallammo.crafting.ingredientconfiguration.SlotEntry;
import me.athlaeos.valhallammo.crafting.ingredientconfiguration.implementations.MaterialChoice;
import me.athlaeos.valhallammo.crafting.recipetypes.DynamicBrewingRecipe;
import me.athlaeos.valhallammo.dom.Action;
import me.athlaeos.valhallammo.dom.Question;
import me.athlaeos.valhallammo.dom.Questionnaire;
import me.athlaeos.valhallammo.gui.Menu;
import me.athlaeos.valhallammo.gui.PlayerMenuUtility;
import me.athlaeos.valhallammo.gui.SetModifiersMenu;
import me.athlaeos.valhallammo.gui.SetRecipeOptionMenu;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.loot.LootPool;
import me.athlaeos.valhallammo.loot.LootTable;
import me.athlaeos.valhallammo.loot.LootTableRegistry;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.StringUtils;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

import static me.athlaeos.valhallammo.gui.implementations.RecipeOptionMenu.KEY_OPTION_ID;

public class LootTableEditor extends Menu {
    private static final NamespacedKey BUTTON_ACTION_KEY = new NamespacedKey(ValhallaMMO.getInstance(), "button_action");
    private static final NamespacedKey BUTTON_DATA = new NamespacedKey(ValhallaMMO.getInstance(), "button_data");
    private static final int iconIndex = 4;
    private static final int preservationTypeIndex = 10;
    private static final int poolDescriptionIndex = 16;
    private static final int previousPageIndex = 27;
    private static final int nextPageIndex = 35;
    private static final int deleteIndex = 45;
    private static final int backToMenuIndex = 53;
    private static final int[] poolIndexes = new int[]{
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };

    private static final ItemStack filler = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).name("").get();

    private final LootTable table;

    private static final ItemStack togglePreservationTypeButton = new ItemBuilder(getButtonData("editor_loottable_preservationmode", Material.COMPARATOR))
            .name("&eLoot Preservation Mode")
            .stringTag(BUTTON_ACTION_KEY, "togglePreservationTypeButton")
            .lore("&7Determines how vanilla loot is treated",
                    "&7with this loot table involved.",
                    "&7Vanilla loot can be",
                    "&a- Preserved entirely, generated",
                    "&aloot is added to vanilla loot",
                    "&e- Removed if loot table generated",
                    "&eloot",
                    "&c- Removed regardless of generated",
                    "&cloot",
                    "",
                    "&eClick to cycle")
            .flag(ItemFlag.HIDE_ATTRIBUTES).get();
    private final ItemStack iconLabel;
    private static final ItemStack poolDescriptionLabel = new ItemBuilder(getButtonData("editor_loottable_descriptionlabel", Material.PAPER))
            .name("&9What's a pool?")
            .lore("&fA loot pool is a subdivision of a",
                    "&floot table.",
                    "&fLoottables can have as many pools",
                    "&fas you want, each possibly",
                    "&fcontributing to drops as long as",
                    "&fits filter conditions pass. ",
                    "&fThis makes tables more structured",
                    "&fand allows for more control.")
            .flag(ItemFlag.HIDE_ATTRIBUTES).get();
    private static final ItemStack nextPageButton = new ItemBuilder(getButtonData("editor_nextpage", Material.ARROW))
            .name("&7&lNext page")
            .stringTag(BUTTON_ACTION_KEY, "nextPageButton")
            .flag(ItemFlag.HIDE_ATTRIBUTES).get();
    private static final ItemStack previousPageButton = new ItemBuilder(getButtonData("editor_prevpage", Material.ARROW))
            .name("&7&lPrevious page")
            .stringTag(BUTTON_ACTION_KEY, "previousPageButton")
            .flag(ItemFlag.HIDE_ATTRIBUTES).get();

    private static final ItemStack deleteButton = new ItemBuilder(getButtonData("editor_delete", Material.BARRIER))
            .stringTag(BUTTON_ACTION_KEY, "deleteButton")
            .name("&cDelete Recipe").get();
    private static final ItemStack deleteConfirmButton = new ItemBuilder(getButtonData("editor_deleteconfirm", Material.BARRIER))
            .name("&cDelete Recipe")
            .stringTag(BUTTON_ACTION_KEY, "deleteConfirmButton")
            .enchant(Enchantment.DURABILITY, 1)
            .lore("&aRight-click &7to confirm recipe deletion")
            .flag(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS).get();
    private static final ItemStack backToMenuButton = new ItemBuilder(getButtonData("editor_backtomenu", Material.BOOK))
            .stringTag(BUTTON_ACTION_KEY, "backToMenuButton")
            .name("&fBack to Menu").get();
    private static final ItemStack createNewButton = new ItemBuilder(getButtonData("editor_loottable_newpool", Material.LIME_STAINED_GLASS_PANE))
            .name("&b&lNew Pool")
            .stringTag(BUTTON_ACTION_KEY, "createNewButton")
            .flag(ItemFlag.HIDE_ATTRIBUTES).get();

    public LootTableEditor(PlayerMenuUtility playerMenuUtility, LootTable table) {
        super(playerMenuUtility);
        this.table = table;

        iconLabel = new ItemBuilder(table.getIcon())
                .lore("&fThe icon of the loot table.",
                        "&fNot visible to players, it's",
                        "&fjust for your own organization",
                        "&fneeds.",
                        "&6Click with item to change icon")
                .name("&eLoot Table Icon")
                .get();
    }

    @Override
    public String getMenuName() {
        return Utils.chat(ValhallaMMO.isResourcePackConfigForced() ? "&f\uF808\uF310\uF80C\uF80A\uF808\uF802&8%table%" : TranslationManager.getTranslation("editormenu_loottables")).replace("%table%", table.getKey());
    }

    @Override
    public int getSlots() {
        return 54;
    }

    private boolean confirmDeletion = false;
    private int page = 0;

    @Override
    public void handleMenu(InventoryClickEvent e) {
        e.setCancelled(!(e.getClickedInventory() instanceof PlayerInventory));

        ItemStack cursor = e.getCursor();
        ItemStack clicked = e.getCurrentItem();

        String action = ItemUtils.getPDCString(BUTTON_ACTION_KEY, clicked, "");
        if (!StringUtils.isEmpty(action)){
            switch (action){
                case "backToMenuButton" -> {
                    new LootTableOverviewMenu(playerMenuUtility).open();
                    return;
                }
                case "togglePreservationTypeButton" -> {
                    int currentMode = Arrays.asList(LootTable.VanillaLootPreservationType.values()).indexOf(table.getVanillaLootPreservationType());
                    if (e.getClick().isLeftClick()){
                        if (currentMode + 1 >= LootTable.VanillaLootPreservationType.values().length) currentMode = 0;
                        else currentMode += 1;
                    } else {
                        if (currentMode - 1 < 0) currentMode = LootTable.VanillaLootPreservationType.values().length - 1;
                        else currentMode -= 1;
                    }
                    table.setVanillaLootPreservationType(LootTable.VanillaLootPreservationType.values()[currentMode]);
                }
                case "deleteButton" -> {
                    confirmDeletion = true;
                    Utils.sendMessage(e.getWhoClicked(), "&cAre you sure you want to delete this loot table?");
                    setMenuItems();
                    return;
                }
                case "deleteConfirmButton" -> {
                    if (e.isRightClick()){
                        LootTableRegistry.getLootTables().remove(table.getKey());
                        new LootTableOverviewMenu(playerMenuUtility).open();
                        return;
                    }
                }
                case "nextPageButton" -> page++;
                case "previousPageButton" -> page = Math.max(0, page - 1);
                case "createNewButton" -> {
                    playerMenuUtility.setPreviousMenu(this); // fallback in case player doesn't wanna
                    e.getWhoClicked().closeInventory();
                    Questionnaire questionnaire = new Questionnaire((Player) e.getWhoClicked(), null, null,
                            new Question("&fWhat should the pool's key be? (type in chat, or 'cancel' to cancel)", s -> !table.getPools().containsKey(s), "&cPool with this key already exists! Try again")
                    ) {
                        @Override
                        public Action<Player> getOnFinish() {
                            if (getQuestions().isEmpty()) return super.getOnFinish();
                            Question question = getQuestions().get(0);
                            if (question.getAnswer() == null) return super.getOnFinish();
                            return (p) -> {
                                String answer = question.getAnswer().replaceAll(" ", "_").toLowerCase();
                                if (answer.contains("cancel")) playerMenuUtility.getPreviousMenu().open();
                                else if (table.getPools().containsKey(answer))
                                    Utils.sendMessage(getWho(), "&cPool key already exists!");
                                else {
                                    LootPool pool = table.addPool(answer);
                                    new LootPoolEditor(playerMenuUtility, pool).open();
                                }
                            };
                        }
                    };
                    Questionnaire.startQuestionnaire((Player) e.getWhoClicked(), questionnaire);
                }
            }
        }

        String data = ItemUtils.getPDCString(BUTTON_DATA, clicked, null);
        if (!StringUtils.isEmpty(data)){
            LootPool pool = table.getPools().get(data);
            if (pool != null){
                new LootPoolEditor(playerMenuUtility, pool).open();
                return;
            }
        }

        if (iconIndex == e.getRawSlot() && !ItemUtils.isEmpty(cursor)){
            table.setIcon(cursor.getType());
            iconLabel.setType(cursor.getType());
        }

        confirmDeletion = false;
        setMenuItems();
    }

    @Override
    public void handleMenu(InventoryDragEvent e) {
        e.setCancelled(true);
        if (e.getRawSlots().size() == 1){
            ClickType type = e.getType() == DragType.EVEN ? ClickType.LEFT : ClickType.RIGHT;
            InventoryAction action = e.getType() == DragType.EVEN ? InventoryAction.DROP_ALL_CURSOR : InventoryAction.DROP_ONE_CURSOR;
            handleMenu(new InventoryClickEvent(e.getView(), InventoryType.SlotType.CONTAINER, new ArrayList<>(e.getRawSlots()).get(0), type, action));
        }
    }

    @Override
    public void setMenuItems() {
        inventory.clear();
        if (!ValhallaMMO.getPluginConfig().getBoolean("admin_gui_filler_removal")) for (int i = 0; i < 54; i++) inventory.setItem(i, filler);
        inventory.setItem(iconIndex, iconLabel);

        List<LootPool> pools = new ArrayList<>(table.getPools().values());
        pools.sort(Comparator.comparing(LootPool::getKey));
        List<ItemStack> buttons = new ArrayList<>();
        pools.forEach(p -> {
            ItemBuilder builder = new ItemBuilder(Material.BOOK)
                    .name("&6" + p.getKey())
                    .stringTag(BUTTON_DATA, p.getKey());
            if (!p.getPredicates().isEmpty()) {
                builder.appendLore("&6" + (p.getPredicateSelection() == LootTable.PredicateSelection.ANY ? "Any" : "All") + "&e of the following");
                builder.appendLore("&econditions must pass:");
                p.getPredicates().forEach(pr -> builder.appendLore("&f> " + StringUtils.separateStringIntoLines(pr.getActiveDescription(), 40)));
            }
            if (p.getEntries().isEmpty()) builder.appendLore("&cPool has no drops");
            else if (p.isWeighted()) builder.appendLore(
                    String.format("&6Can drop up to %d%s", p.getWeightedRolls(), p.getBonusLuckRolls() > 0 ? String.format("(+%.1f/luck)", p.getBonusLuckRolls()) : ""),
                    String.format("&6items from a selection of %d", p.getEntries().size()));
            else builder.appendLore(String.format("&6Has %d potential chanced drops", p.getEntries().size()));
            buttons.add(builder.get());
        });
        buttons.add(createNewButton);
        Map<Integer, List<ItemStack>> pages = Utils.paginate(poolIndexes.length, buttons);

        page = Math.max(1, Math.min(page, pages.size()));

        if (!pages.isEmpty()){
            int index = 0;
            for (ItemStack i : pages.get(page - 1)){
                inventory.setItem(poolIndexes[index], i);
                index++;
            }
        }
        inventory.setItem(preservationTypeIndex, new ItemBuilder(togglePreservationTypeButton).name("&eLoot Mode: " +
                    switch (table.getVanillaLootPreservationType()){
                        case KEEP -> "&fAdd to Vanilla";
                        case CLEAR -> "&fClear Vanilla";
                        case CLEAR_UNLESS_EMPTY -> "&fOverwrite Vanilla";
                    }
                ).get());
        inventory.setItem(deleteIndex, confirmDeletion ? deleteConfirmButton : deleteButton);
        inventory.setItem(backToMenuIndex, backToMenuButton);
        inventory.setItem(poolDescriptionIndex, poolDescriptionLabel);
        if (page < pages.size()) inventory.setItem(nextPageIndex, nextPageButton);
        if (page > 1) inventory.setItem(previousPageIndex, previousPageButton);
    }
}
