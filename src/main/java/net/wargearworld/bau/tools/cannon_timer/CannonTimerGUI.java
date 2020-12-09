package net.wargearworld.bau.tools.cannon_timer;

import net.wargearworld.GUI_API.Executor;
import net.wargearworld.GUI_API.GUI.AnvilGUI;
import net.wargearworld.GUI_API.GUI.ArgumentList;
import net.wargearworld.GUI_API.GUI.ChestGUI;
import net.wargearworld.GUI_API.GUI.CloseArgumentList;
import net.wargearworld.GUI_API.Items.CustomHead;
import net.wargearworld.GUI_API.Items.DefaultItem;
import net.wargearworld.GUI_API.Items.HeadItem;
import net.wargearworld.GUI_API.Items.Item;
import net.wargearworld.bau.Main;
import net.wargearworld.bau.MessageHandler;
import net.wargearworld.bau.config.BauConfig;
import net.wargearworld.bau.utils.CustomHeadValues;
import net.wargearworld.bau.utils.HelperMethods;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.Map.Entry;

/**
 * Gives static access to methods to open the Block GUIs in the CannonTimer
 */
public class CannonTimerGUI {

    public static void openMain(Player p, CannonTimerBlock cannonTimerBlock, int page) {
        MessageHandler msgHandler = MessageHandler.getInstance();
        ChestGUI chestGUI = new ChestGUI(54, msgHandler.getString(p, "cannonTimer_gui_title", page + ""));
        chestGUI.onClose(s -> {
            save(p, s, cannonTimerBlock);
        });


        int maxTicks = BauConfig.getInstance().getCannonTimerMaxTicks();
        chestGUI.setMaxStackSize(maxTicks);
        int currentRow = 0; // starts with 0
        Map<Integer, CannonTimerTick> content = readContent(cannonTimerBlock.getTicks(), page);

        if (page > 1) {
            /* Set PREVIOUS Item */
            Item back = getHeadItem(p, CustomHeadValues.ARROW_LEFT.getValue(), "cannonTimer_gui_previousPage");
            back.setExecutor(s -> {
                int newpage = page - 1;
                openMain(p, cannonTimerBlock, newpage);
            });
            chestGUI.setItem(18, back);
            chestGUI.setItem(27, back);
            currentRow++;
        }

        Iterator<Entry<Integer, CannonTimerTick>> iterator = content.entrySet().iterator();
        Item plus = getHeadItem(p, CustomHeadValues.PLUS.getValue(), "cannonTimer_gui_plus").setExecutor(s -> {
            cannonTimerBlock.addTick();
            openMain(p, cannonTimerBlock, page);
        });
        for (int i = currentRow; i < 9; i++) {
            if (i == 8 && ((page == 1 && content.size() == 8) || (page > 1 && content.size() == 7)) && page != 10) {
                /* Set NEXT item */
                Item next = getHeadItem(p, CustomHeadValues.ARROW_RIGHT.getValue(), "cannonTimer_gui_nextPage");
                next.setExecutor(s -> {
                    int newpage = page + 1;
                    openMain(p, cannonTimerBlock, newpage);
                });
                chestGUI.setItem(26, next);
                chestGUI.setItem(35, next);
            } else if (iterator.hasNext()) {
                Entry<Integer, CannonTimerTick> entry = iterator.next();
                CannonTimerTick cannonTimerTick = entry.getValue();

                /* TNT */

                Item increaseTNT = getHeadItem(p, CustomHeadValues.ARROW_UP.getValue(), "cannonTimer_gui_increaseAmount").setExecutor(s -> {
                    cannonTimerTick.add(s.getClickType());
                    openMain(p, cannonTimerBlock, page);
                });
                Item decreaseTNT = getHeadItem(p, CustomHeadValues.ARROW_DOWN.getValue(), "cannonTimer_gui_decreaseAmount").setExecutor(s -> {
                    cannonTimerTick.remove(s.getClickType());
                    openMain(p, cannonTimerBlock, page);
                });
                Item tnt = new DefaultItem(Material.TNT, msgHandler.getString(p, "cannonTimer_gui_tnt", cannonTimerTick.getAmount() + ""));
                tnt.setAmount(cannonTimerTick.getAmount());
                tnt.addLore(msgHandler.getString(p, "cannonTimer_gui_tnt_lore"));
                if (cannonTimerTick.getSettings() != null) {
                    tnt.addLore(cannonTimerTick.getSettings().generateLore(p));
                }
                tnt.setExecutor(s -> {
                    openLocalSettings(p, cannonTimerTick, cannonTimerBlock, entry.getKey());
                });
                /* Ticks*/
                String tickString = msgHandler.getString(p, "cannonTimer_gui_tick", entry.getKey() + "");

                Item increaseTick = getHeadItem(p, CustomHeadValues.ARROW_UP.getValue(), "cannonTimer_gui_increaseTick").setExecutor(s -> {
                    ItemStack tickIs = s.getClickedInventory().getItem(s.getClickedIndex() + 9);
                    Integer newAmount = cannonTimerBlock.increaseTick(tickIs.getAmount(), s.getClickType());
                    if (newAmount == null)
                        return;
                    openMain(p, cannonTimerBlock, page);
                });
                increaseTick.setAmount(1);
                increaseTick.addLore(tickString);
                increaseTick.addLore(msgHandler.getString(p, "cannonTimer_gui_tick_hint_lore"));
                Item tick = new DefaultItem(Material.PAPER, tickString, entry.getKey());
                tick.setAmount(entry.getKey()).addLore(msgHandler.getString(p, "cannonTimer_gui_tick_lore"));
                tick.setExecutor(s -> {
                    cannonTimerBlock.remove(entry.getKey());
                    openMain(p, cannonTimerBlock, page);
                });
                tick.addLore(msgHandler.getString(p, "cannonTimer_gui_tick_hint_lore"));


                Item decreaseTick = getHeadItem(p, CustomHeadValues.ARROW_DOWN.getValue(), "cannonTimer_gui_decreaseTick").setExecutor(s -> {
                    ItemStack tickIs = s.getClickedInventory().getItem(s.getClickedIndex() - 9);
                    Integer newAmount = cannonTimerBlock.decreaseTick(tickIs.getAmount(), s.getClickType());
                    if (newAmount == null)
                        return;
                    openMain(p, cannonTimerBlock, page);
                });
                decreaseTick.setAmount(1);
                decreaseTick.addLore(tickString);
                decreaseTick.addLore(msgHandler.getString(p, "cannonTimer_gui_tick_hint_lore"));
                if (cannonTimerTick.getAmount() < 64) chestGUI.setItem(i, increaseTNT);
                chestGUI.setItem(i + 9, tnt);
                if (cannonTimerTick.getAmount() > 1) chestGUI.setItem(i + 18, decreaseTNT);
                if (entry.getKey() < maxTicks) {
                    chestGUI.setItem(i + 27, increaseTick);
                }
                chestGUI.setItem(i + 36, tick);
                if (entry.getKey() > 1) {
                    chestGUI.setItem(i + 45, decreaseTick);
                }
                continue;
            } else {
                if (i == 8)
                    continue;
                chestGUI.setItem(i + 27, plus);
                continue;
            }
        }
        Item settings = getHeadItem(p, CustomHeadValues.SETTINGS.getValue(), "cannonTimer_gui_settings_item");
        settings.addLore(cannonTimerBlock.getSettings().generateLore(p));
        settings.setExecutor(s -> {
            openGloalSettings(p, cannonTimerBlock);
        });
        chestGUI.setItem(8, settings);

        chestGUI.open(p);
    }

    private static void save(Player p, CloseArgumentList s, CannonTimerBlock cannonTimerBlock) {
        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
            if (p.getOpenInventory().getTopInventory().getType() == InventoryType.CRAFTING) {
                MessageHandler.getInstance().send(p, "cannonTimer_saved");
                cannonTimerBlock.setActive(cannonTimerBlock.isActive(), p.getWorld());
                cannonTimerBlock.sort();
            }
        }, 1);

    }

    private static Map<Integer, CannonTimerTick> readContent(Map<Integer, CannonTimerTick> map, int page) {
        Map<Integer, CannonTimerTick> out = new LinkedHashMap<>();
        List<Integer> list = new ArrayList<>(map.keySet());
        Collection<Integer> outList = HelperMethods.getPage(list,page);
        for (int i : outList) {
            out.put(i, map.get(i));
        }
        return out;

    }

    private static Item getHeadItem(Player p, String id, String key, String... args) {
        HeadItem headItem = new HeadItem(new CustomHead(id), s -> {
        });
        headItem.setName(MessageHandler.getInstance().getString(p, key, args));
        return headItem;
    }

    public static void openGloalSettings(Player p, CannonTimerBlock cannonTimerBlock) {
        MessageHandler msgHandler = MessageHandler.getInstance();

        CannonTimerSettings settings = cannonTimerBlock.getSettings();

        ChestGUI chestGUI = new ChestGUI(9, msgHandler.getString(p, "cannonTimer_global_settings_gui_title"));
        chestGUI.onClose(s -> {
            Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
                if (p.getOpenInventory().getTopInventory().getType() == InventoryType.CRAFTING)
                    openMain(p, cannonTimerBlock, 1);
            }, 1);
        });
        Item setXOffset = getHeadItem(p, CustomHeadValues.X.getValue(), "cannonTimer_settings_xOffset").setExecutor(s -> {
            openOffset(p, settings, "X", list -> {
                openGloalSettings(p, cannonTimerBlock);
            });
        }).addLore(List.of(msgHandler.getString(p, "cannonTimer_gui_settings_lore_xOffset", settings.getxOffset() + "")));
        Item setZOffset = getHeadItem(p, CustomHeadValues.Z.getValue(), "cannonTimer_settings_zOffset").setExecutor(s -> {
            openOffset(p, settings, "Z", list -> {
                openGloalSettings(p, cannonTimerBlock);
            });
        }).addLore(msgHandler.getString(p, "cannonTimer_gui_settings_lore_zOffset", settings.getzOffset() + ""));
        Item setRandom = getRandomItem(p, settings).setExecutor(s -> {
            System.out.println("clicked");
            boolean newVelocity = !settings.isVelocity();
            settings.setVelocity(newVelocity);
            openGloalSettings(p, cannonTimerBlock);
        });

        chestGUI.setItem(1, setXOffset);
        chestGUI.setItem(4, setZOffset);
        chestGUI.setItem(7, setRandom);
        chestGUI.open(p);
    }

    public static void openLocalSettings(Player p, CannonTimerTick cannonTimerTick, CannonTimerBlock cannonTimerBlock, int tick) {
        MessageHandler msgHandler = MessageHandler.getInstance();

        CannonTimerSettings settings = cannonTimerTick.getSettings();
        if (settings == null) {
            settings = new CannonTimerSettings();
            cannonTimerTick.setSettings(settings);
        }
        CannonTimerSettings finalSettings = settings;

        ChestGUI chestGUI = new ChestGUI(9, msgHandler.getString(p, "cannonTimer_local_settings_gui_title", tick + ""));
        chestGUI.onClose(s -> {
            Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
                if (p.getOpenInventory().getTopInventory().getType() == InventoryType.CRAFTING)
                    openMain(p, cannonTimerBlock, 1);
            }, 1);
        });
        Item setXOffset = getHeadItem(p, CustomHeadValues.X.getValue(), "cannonTimer_settings_xOffset").setExecutor(s -> {
            openOffset(p, finalSettings, "X", list -> {
                openLocalSettings(p, cannonTimerTick, cannonTimerBlock, tick);
            });
        }).addLore(List.of(msgHandler.getString(p, "cannonTimer_gui_settings_lore_xOffset", settings.getxOffset() + "")));
        Item setZOffset = getHeadItem(p, CustomHeadValues.Z.getValue(), "cannonTimer_settings_zOffset").setExecutor(s -> {
            openOffset(p, finalSettings, "Z", list -> {
                openLocalSettings(p, cannonTimerTick, cannonTimerBlock, tick);
            });
        }).addLore(List.of(msgHandler.getString(p, "cannonTimer_gui_settings_lore_zOffset", settings.getzOffset() + "")));
        Item setRandom = getRandomItem(p, finalSettings).setExecutor(s -> {
            boolean newVelocity = !finalSettings.isVelocity();
            finalSettings.setVelocity(newVelocity);
            Bukkit.getScheduler().runTaskLater(Main.getPlugin(),()->  openLocalSettings(p, cannonTimerTick, cannonTimerBlock, tick),1);
        });

        chestGUI.setItem(1, setXOffset);
        chestGUI.setItem(4, setZOffset);
        chestGUI.setItem(7, setRandom);
        chestGUI.open(p);
    }

    private static void openOffset(Player p, CannonTimerSettings settings, String arg, Executor<ArgumentList> onClick) {
        AnvilGUI anvilGUI = null;
        if (arg.equalsIgnoreCase("x")) {
            anvilGUI = new AnvilGUI(MessageHandler.getInstance().getString(p, "cannonTimer_gui_settings_xOffset"), settings.getxOffset() + "");
        } else if (arg.equalsIgnoreCase("z")) {
            anvilGUI = new AnvilGUI(MessageHandler.getInstance().getString(p, "cannonTimer_gui_settings_zOffset"), settings.getzOffset() + "");

        }
        anvilGUI.setExecutor(s -> {
            String result = s.getClicked().getItemMeta().getDisplayName();
            result = result.replace(" ", "").replace(",", ".");
            try {
                Double offset = Double.parseDouble(result);
            } catch (NumberFormatException ex) {
                p.closeInventory();
                return;
            }
            if (arg.equalsIgnoreCase("x")) {
                settings.setxOffset(Double.parseDouble(result));
            } else if (arg.equalsIgnoreCase("z")) {
                settings.setzOffset(Double.parseDouble(result));
            }
            onClick.call(s);
        });
        anvilGUI.open(p);
    }

    private static Item getRandomItem(Player p, CannonTimerSettings settings) {
        Item item;
        if (settings.isVelocity()) {
            item = new DefaultItem(Material.LIME_DYE, MessageHandler.getInstance().getString(p, "cannonTimer_settings_random_deactivate"));
        } else {
            item = new DefaultItem(Material.RED_DYE, MessageHandler.getInstance().getString(p, "cannonTimer_settings_random_activate"));
        }
        return item;
    }
}
