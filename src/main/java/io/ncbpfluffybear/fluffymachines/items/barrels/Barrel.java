package io.ncbpfluffybear.fluffymachines.items.barrels;

import io.github.thebusybiscuit.slimefun4.implementation.SlimefunPlugin;
import io.github.thebusybiscuit.slimefun4.utils.holograms.SimpleHologram;
import io.ncbpfluffybear.fluffymachines.utils.Utils;
import me.mrCookieSlime.CSCoreLibPlugin.Configuration.Config;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.Item.CustomItem;
import me.mrCookieSlime.Slimefun.Lists.RecipeType;
import me.mrCookieSlime.Slimefun.Objects.Category;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.SlimefunItem;
import me.mrCookieSlime.Slimefun.Objects.handlers.BlockTicker;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import me.mrCookieSlime.Slimefun.api.SlimefunItemStack;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import me.mrCookieSlime.Slimefun.api.inventory.DirtyChestMenu;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import me.mrCookieSlime.Slimefun.cscorelib2.protection.ProtectableAction;
import org.apache.commons.lang.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * A Remake of Barrels by John000708
 *
 * @author NCBPFluffyBear
 */

public class Barrel extends SlimefunItem {

    private final int[] inputBorder = {0, 1, 2, 3, 9, 12, 18, 21, 27, 30, 36, 37, 38, 39};
    private final int[] outputBorder = {5, 6, 7 ,8, 14, 17, 23, 26, 32, 35, 41, 42, 43, 44};
    private final int[] plainBorder = {4, 13, 40};

    private final int[] INPUT_SLOTS = {10, 11, 19, 20, 28, 29};
    private final int[] OUTPUT_SLOTS = {15, 16, 24, 25, 33, 34};

    private final int STATUS_SLOT = 22;
    private final int DISPLAY_SLOT = 31;

    public static final int SMALL_BARREL_SIZE = 17280; // 5 Double chests
    public static final int MEDIUM_BARREL_SIZE = 34560; // 10 Double chests
    public static final int BIG_BARREL_SIZE = 69120; // 20 Double chests
    public static final int LARGE_BARREL_SIZE = 138240; // 40 Double chests
    public static final int MASSIVE_BARREL_SIZE = 276480; // 80 Double chests
    public static final int BOTTOMLESS_BARREL_SIZE = 1728000; // 500 Double chests

    private final int MAX_STORAGE;

    public Barrel(Category category, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe, String name, int MAX_STORAGE) {
        super(category, item, recipeType, recipe);

        this.MAX_STORAGE = MAX_STORAGE;

        new BlockMenuPreset(getID(), name) {

            @Override
            public void init() {
                constructMenu(this);
            }

            @Override
            public void newInstance(BlockMenu menu, Block b) {

                // Essentially convert to onPlace itemhandler
                if (BlockStorage.getLocationInfo(b.getLocation(), "stored") == null) {
                    menu.replaceExistingItem(STATUS_SLOT, new CustomItem(
                        Material.LIME_STAINED_GLASS_PANE, "&6Items Stored: &e0" + " / " + MAX_STORAGE, "&70%"));
                    menu.addMenuClickHandler(STATUS_SLOT, (p, slot, item, action) -> false);

                    menu.replaceExistingItem(DISPLAY_SLOT, new CustomItem(Material.BARRIER, "&cEmpty"));
                    menu.addMenuClickHandler(DISPLAY_SLOT, (p, slot, item, action) -> false);

                    BlockStorage.addBlockInfo(b, "stored", "0");

                    SimpleHologram.update(b, "&cEmpty");

                    // We still need the click handlers though
                } else {
                    menu.addMenuClickHandler(STATUS_SLOT, (p, slot, item, action) -> false);
                    menu.addMenuClickHandler(DISPLAY_SLOT, (p, slot, item, action) -> false);
                }
            }

            @Override
            public boolean canOpen(Block b, Player p) {
                return (p.hasPermission("slimefun.inventory.bypass")
                    || SlimefunPlugin.getProtectionManager().hasPermission(
                    p, b.getLocation(), ProtectableAction.ACCESS_INVENTORIES));
            }

            @Override
            public int[] getSlotsAccessedByItemTransport(ItemTransportFlow itemTransportFlow) {
                return new int[0];
            }

            @Override
            public int[] getSlotsAccessedByItemTransport(DirtyChestMenu menu, ItemTransportFlow flow, ItemStack item) {
                if (flow == ItemTransportFlow.INSERT) {
                    return INPUT_SLOTS;
                } else if (flow == ItemTransportFlow.WITHDRAW) {
                    return OUTPUT_SLOTS;
                } else {
                    return new int[0];
                }
            }
        };

        registerBlockHandler(getID(), (p, b, stack, reason) -> {
            BlockMenu inv = BlockStorage.getInventory(b);
            String storedString = BlockStorage.getLocationInfo(b.getLocation(), "stored");
            int stored = Integer.parseInt(storedString);

            if (inv != null) {

                inv.dropItems(b.getLocation(), INPUT_SLOTS);
                inv.dropItems(b.getLocation(), OUTPUT_SLOTS);


                if (stored > 0) {
                    int stackSize = inv.getItemInSlot(DISPLAY_SLOT).getMaxStackSize();
                    ItemStack unKeyed = Utils.unKeyItem(inv.getItemInSlot(DISPLAY_SLOT));

                    // Everything greater than 1 stack
                    while (stored >= stackSize) {

                        b.getWorld().dropItemNaturally(b.getLocation(), new CustomItem(unKeyed, stackSize));

                        stored = stored - stackSize;
                    }

                    // Drop remaining, if there is any
                    if (stored > 0) {
                        b.getWorld().dropItemNaturally(b.getLocation(), new CustomItem(unKeyed, stored));
                    }
                }

            }
            // In case they use an explosive pick
            BlockStorage.addBlockInfo(b, "stored", "0");
            updateMenu(b, inv);
            SimpleHologram.remove(b);
            return true;
        });
    }

    protected void constructMenu(BlockMenuPreset preset) {
        for (int i : outputBorder) {
            preset.addItem(i, new CustomItem(new ItemStack(Material.ORANGE_STAINED_GLASS_PANE), " "), (p, slot, item, action) -> false);
        }

        for (int i : inputBorder) {
            preset.addItem(i, new CustomItem(new ItemStack(Material.CYAN_STAINED_GLASS_PANE), " "), (p, slot, item, action) -> false);
        }

        for (int i : plainBorder) {
            preset.addItem(i, new CustomItem(new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE), " "), (p, slot, item, action) -> false);
        }
    }

    @Override
    public void preRegister() {
        addItemHandler(new BlockTicker() {

            @Override
            public void tick(Block b, SlimefunItem sf, Config data) {
                Barrel.this.tick(b);
            }

            @Override
            public boolean isSynchronized() {
                return false;
            }
        });
    }

    protected void tick(Block b) {
        BlockMenu inv = BlockStorage.getInventory(b);
        Location l = b.getLocation();

        // These have to be in separate lines or code goes ree

        for (int slot : INPUT_SLOTS) {
            if (inv.getItemInSlot(slot) != null && inv.getItemInSlot(slot).getType() != Material.AIR) {

                String storedString = BlockStorage.getLocationInfo(l, "stored");
                int stored = Integer.parseInt(storedString);
                ItemStack item = inv.getItemInSlot(slot);

                if (stored == 0) {
                    registerItem(b, inv, slot, item, stored);
                }


                else if (stored > 0 && inv.getItemInSlot(DISPLAY_SLOT) != null
                    && matchMeta(Utils.unKeyItem(inv.getItemInSlot(DISPLAY_SLOT)), item)
                    && stored < MAX_STORAGE) {

                    // Can fit entire itemstack
                    if (stored + item.getAmount() <= MAX_STORAGE) {
                        storeItem(b, inv, slot, item, stored);

                    // Split itemstack
                    } else {
                        int amount = MAX_STORAGE - stored;
                        inv.consumeItem(slot, amount);

                        BlockStorage.addBlockInfo(b, "stored", String.valueOf((stored + amount)));
                        updateMenu(b, inv);
                    }
                }
            }
        }

        for (int i = 0; i < OUTPUT_SLOTS.length; i++) {
            if (inv.getItemInSlot(DISPLAY_SLOT) != null && inv.getItemInSlot(DISPLAY_SLOT).getType() != Material.BARRIER) {

                int freeSlot = 0;

                for (int outputSlot : OUTPUT_SLOTS) {
                    if (inv.getItemInSlot(outputSlot) == null) {
                        freeSlot = outputSlot;
                        break;
                    } else if (outputSlot == OUTPUT_SLOTS[5]) {
                        return;
                    }
                }

                String storedString = BlockStorage.getLocationInfo(l, "stored");
                int stored = Integer.parseInt(storedString);
                ItemStack item = inv.getItemInSlot(DISPLAY_SLOT);

                if (stored > inv.getItemInSlot(DISPLAY_SLOT).getMaxStackSize()) {

                    ItemStack clone = new CustomItem(Utils.unKeyItem(item), item.getMaxStackSize());

                    int amount = clone.getMaxStackSize();
                    BlockStorage.addBlockInfo(b, "stored", String.valueOf((stored - amount)));
                    inv.pushItem(clone, freeSlot);
                    updateMenu(b, inv);

                } else if (stored != 0) {

                    ItemStack clone = new CustomItem(Utils.unKeyItem(item), stored);

                    BlockStorage.addBlockInfo(b, "stored", "0");
                    inv.pushItem(clone, freeSlot);
                    updateMenu(b, inv);
                }
            }
        }
    }

    private void registerItem(Block b, BlockMenu inv, int slot, ItemStack item, int stored) {
        int amount = item.getAmount();
        inv.replaceExistingItem(DISPLAY_SLOT, new CustomItem(Utils.keyItem(item), 1));

        inv.consumeItem(slot, amount);

        BlockStorage.addBlockInfo(b, "stored", String.valueOf((stored + amount)));
        updateMenu(b, inv);
    }

    private void storeItem(Block b, BlockMenu inv, int slot, ItemStack item, int stored) {
        int amount = item.getAmount();
        inv.consumeItem(slot, amount);

        BlockStorage.addBlockInfo(b, "stored", String.valueOf((stored + amount)));
        updateMenu(b, inv);
    }

    private boolean matchMeta(ItemStack item1, ItemStack item2) {
        return item1.getType().equals(item2.getType()) && item1.getItemMeta().equals(item2.getItemMeta());
    }

    private void updateMenu(Block b, BlockMenu inv) {
        String storedString = BlockStorage.getLocationInfo(b.getLocation(), "stored");
        int stored = Integer.parseInt(storedString);
        String itemName;

        if (inv.getItemInSlot(DISPLAY_SLOT) != null && inv.getItemInSlot(DISPLAY_SLOT).getItemMeta().hasDisplayName()) {
            itemName = inv.getItemInSlot(DISPLAY_SLOT).getItemMeta().getDisplayName();
        } else {
            itemName = WordUtils.capitalizeFully(inv.getItemInSlot(DISPLAY_SLOT).getType().name().replace("_", " "));
        }

        String storedPercent = doubleRoundAndFade((double) stored / (double) MAX_STORAGE * 100);
        String storedStacks = doubleRoundAndFade((double) stored / (double) inv.getItemInSlot(DISPLAY_SLOT).getMaxStackSize());

        inv.replaceExistingItem(STATUS_SLOT, new CustomItem(
            Material.LIME_STAINED_GLASS_PANE, "&6Items Stored: &e" + stored + " / " + MAX_STORAGE,
            "&b" + storedStacks + " Stacks &8| &7" + storedPercent + "&7%"));
        SimpleHologram.update(b, itemName + " &9x" + stored + " &7(" + storedPercent + "&7%)");

        if (stored == 0) {
            inv.replaceExistingItem(DISPLAY_SLOT, new CustomItem(Material.BARRIER, "&cEmpty"));
            SimpleHologram.update(b, "&cEmpty");
        }
    }

    public static String doubleRoundAndFade(double num) {
        // Using same format that is used on lore power
        String formattedString = Utils.powerFormat.format(num);
        if (formattedString.indexOf('.') != -1) {
            return formattedString.substring(0, formattedString.indexOf('.')) + ChatColor.DARK_GRAY
                + formattedString.substring(formattedString.indexOf('.')) + ChatColor.GRAY;
        } else {
            return formattedString;
        }
    }
}
