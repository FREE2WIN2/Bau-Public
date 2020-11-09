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
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.Map.Entry;

/**
 * Gives static access to methods to open the Block GUIs in the CannonTimer
 */
public class CannonTimerGUI {
    private static final String ARROW_DOWN = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzI0MzE5MTFmNDE3OGI0ZDJiNDEzYWE3ZjVjNzhhZTQ0NDdmZTkyNDY5NDNjMzFkZjMxMTYzYzBlMDQzZTBkNiJ9fX0=";
    private static final String ARROW_UP = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmNjYmY5ODgzZGQzNTlmZGYyMzg1YzkwYTQ1OWQ3Mzc3NjUzODJlYzQxMTdiMDQ4OTVhYzRkYzRiNjBmYyJ9fX0=";
    private static final String ARROW_LEFT = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2RjOWU0ZGNmYTQyMjFhMWZhZGMxYjViMmIxMWQ4YmVlYjU3ODc5YWYxYzQyMzYyMTQyYmFlMWVkZDUifX19";
    private static final String ARROW_RIGHT = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTU2YTM2MTg0NTllNDNiMjg3YjIyYjdlMjM1ZWM2OTk1OTQ1NDZjNmZjZDZkYzg0YmZjYTRjZjMwYWI5MzExIn19fQ==";
    private static final String PLUS = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWEyZDg5MWM2YWU5ZjZiYWEwNDBkNzM2YWI4NGQ0ODM0NGJiNmI3MGQ3ZjFhMjgwZGQxMmNiYWM0ZDc3NyJ9fX0=";
    private static final String MINUS = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTM1ZTRlMjZlYWZjMTFiNTJjMTE2NjhlMWQ2NjM0ZTdkMWQwZDIxYzQxMWNiMDg1ZjkzOTQyNjhlYjRjZGZiYSJ9fX0=";
    private static final String X = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzM4YWIxNDU3NDdiNGJkMDljZTAzNTQzNTQ5NDhjZTY5ZmY2ZjQxZDllMDk4YzY4NDhiODBlMTg3ZTkxOSJ9fX0=";
    private static final String Z = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzk5MmM3NTNiZjljNjI1ODUzY2UyYTBiN2IxNzRiODlhNmVjMjZiYjVjM2NjYjQ3M2I2YTIwMTI0OTYzMTIifX19";
    private static final String SETTINGS = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTRkNDliYWU5NWM3OTBjM2IxZmY1YjJmMDEwNTJhNzE0ZDYxODU0ODFkNWIxYzg1OTMwYjNmOTlkMjMyMTY3NCJ9fX0=";

    public static void openMain(Player p, CannonTimerBlock cannonTimerBlock, int page) {
        MessageHandler msgHandler = MessageHandler.getInstance();
        ChestGUI chestGUI = new ChestGUI(54, msgHandler.getString(p, "cannonTimer_gui_title", page + ""));
        chestGUI.onClose(s -> {
            save(p, s, cannonTimerBlock);
        });


        chestGUI.setMaxStackSize(80);
        int currentRow = 0; // starts with 0
        Map<Integer, CannonTimerTick> content = readContent(cannonTimerBlock.getTicks(), page);

        if (page > 1) {
            /* Set PREVIOUS Item */
            Item back = getHeadItem(p, ARROW_LEFT, "cannonTimer_gui_previousPage");
            back.setExecutor(s -> {
                int newpage = page - 1;
                openMain(p, cannonTimerBlock, newpage);
            });
            chestGUI.setItem(18, back);
            chestGUI.setItem(27, back);
            currentRow++;
        }

        Iterator<Entry<Integer, CannonTimerTick>> iterator = content.entrySet().iterator();
        Item plus = getHeadItem(p, PLUS, "cannonTimer_gui_plus").setExecutor(s -> {
            cannonTimerBlock.addTick();
            openMain(p, cannonTimerBlock, page);
        });
        for (int i = currentRow; i < 9; i++) {
            if (i == 8 && ((page == 1 && content.size() == 8) || (page > 1 && content.size() == 7)) && page != 10) {
                /* Set NEXT item */
                Item next = getHeadItem(p, ARROW_RIGHT, "cannonTimer_gui_nextPage");
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

                Item increaseTNT = getHeadItem(p, ARROW_UP, "cannonTimer_gui_increaseAmount").setExecutor(s -> {
                    cannonTimerTick.add(s.getClickType());
                    openMain(p, cannonTimerBlock, page);
                });
                Item decreaseTNT = getHeadItem(p, ARROW_DOWN, "cannonTimer_gui_decreaseAmount").setExecutor(s -> {
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

                Item increaseTick = getHeadItem(p, ARROW_UP, "cannonTimer_gui_increaseTick").setExecutor(s -> {
                    ItemStack tickIs = s.getClickedInventory().getItem(s.getClickedIndex() + 9);
                    Integer newAmount = cannonTimerBlock.increaseTick(tickIs.getAmount(), s.getClickType());
                    if (newAmount == null)
                        return;
                    openMain(p, cannonTimerBlock, page);
//                    openMain(p, cannonTimerBlock, page);
                });
                increaseTick.setAmount(1);
                Item tick = new DefaultItem(Material.PAPER, msgHandler.getString(p, "cannonTimer_gui_tick", entry.getKey() + ""), entry.getKey());
                tick.setAmount(entry.getKey()).addLore(msgHandler.getString(p, "cannonTimer_gui_tick_lore"));
                tick.setExecutor(s -> {
                    cannonTimerBlock.remove(entry.getKey());
                    openMain(p, cannonTimerBlock, page);
                });
                Item decreaseTick = getHeadItem(p, ARROW_DOWN, "cannonTimer_gui_decreaseTick").setExecutor(s -> {
                    ItemStack tickIs = s.getClickedInventory().getItem(s.getClickedIndex() - 9);
                    Integer newAmount = cannonTimerBlock.decreaseTick(tickIs.getAmount(), s.getClickType());
                    if (newAmount == null)
                        return;
                    openMain(p, cannonTimerBlock, page);
                });
                decreaseTick.setAmount(1);

                if (cannonTimerTick.getAmount() < 64) chestGUI.setItem(i, increaseTNT);
                chestGUI.setItem(i + 9, tnt);
                if (cannonTimerTick.getAmount() > 1) chestGUI.setItem(i + 18, decreaseTNT);
                if (entry.getKey() < 81) {
                    chestGUI.setItem(i + 27, increaseTick);
                }
                chestGUI.setItem(i + 36, tick);
                if (entry.getKey() > 1) {
                    chestGUI.setItem(i + 45, decreaseTick);
                }
                continue;
            } else{
                if(i == 8)
                    continue;
                chestGUI.setItem(i + 27, plus);
                continue;
            }
        }
        Item settings = getHeadItem(p, SETTINGS, "cannonTimer_gui_settings_item");
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
        int mapSize = map.size();
        int begin = 0;
        int pageSize = 7;
        if (page == 1) {
            pageSize++;
        } else{
            if(page == 10)
                pageSize++;
            begin += 8;
            begin += (page - 2) * 7;
        }
        if (pageSize >= mapSize) {
            return map;
        }
        Map<Integer, CannonTimerTick> out = new HashMap<>();
        List<Integer> list = new ArrayList<>(map.keySet());

        if (list.size() - 1 < begin) {
            return out;
        }
        if (list.size() - 1 < begin + pageSize) {
            list = list.subList(begin, list.size() - 1);
        } else {
            list = list.subList(begin, begin + pageSize);
        }
        for (int i : list) {
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
        Item setXOffset = getHeadItem(p, X, "cannonTimer_settings_xOffset").setExecutor(s -> {
            openOffset(p, settings, "X", list -> {
                openGloalSettings(p, cannonTimerBlock);
            });
        }).addLore(List.of(msgHandler.getString(p, "cannonTimer_gui_settings_lore_xOffset", settings.getxOffset() + "")));
        Item setZOffset = getHeadItem(p, Z, "cannonTimer_settings_zOffset").setExecutor(s -> {
            openOffset(p, settings, "Z", list -> {
                openGloalSettings(p, cannonTimerBlock);
            });
        }).addLore(msgHandler.getString(p, "cannonTimer_gui_settings_lore_zOffset", settings.getzOffset() + ""));
        Item setRandom = getRandomItem(p, settings).setExecutor(s -> {
            settings.setVelocity(!settings.isVelocity());
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
        Item setXOffset = getHeadItem(p, X, "cannonTimer_settings_xOffset").setExecutor(s -> {
            openOffset(p, finalSettings, "X", list -> {
                openLocalSettings(p, cannonTimerTick, cannonTimerBlock, tick);
            });
        }).addLore(List.of(msgHandler.getString(p, "cannonTimer_gui_settings_lore_xOffset", settings.getxOffset() + "")));
        ;
        Item setZOffset = getHeadItem(p, Z, "cannonTimer_settings_zOffset").setExecutor(s -> {
            openOffset(p, finalSettings, "Z", list -> {
                openLocalSettings(p, cannonTimerTick, cannonTimerBlock, tick);
            });
        }).addLore(List.of(msgHandler.getString(p, "cannonTimer_gui_settings_lore_zOffset", settings.getzOffset() + "")));
        ;
        Item setRandom = getRandomItem(p, finalSettings).setExecutor(s -> {
            finalSettings.setVelocity(!finalSettings.isVelocity());
            openLocalSettings(p, cannonTimerTick, cannonTimerBlock, tick);
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
