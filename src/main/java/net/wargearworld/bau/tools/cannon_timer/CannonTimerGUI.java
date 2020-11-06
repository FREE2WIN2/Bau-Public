package net.wargearworld.bau.tools.cannon_timer;

import net.wargearworld.GUI_API.GUI.ChestGUI;
import net.wargearworld.GUI_API.Items.CustomHead;
import net.wargearworld.GUI_API.Items.HeadItem;
import net.wargearworld.GUI_API.Items.Item;
import net.wargearworld.bau.MessageHandler;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.Map.Entry;

/**
 * Gives static access to methods to open the Block GUIs in the CannonTimer
 */
public class CannonTimerGUI {
    private static final int ARROW_DOWN = 8793;
    private static final int ARROW_UP = 8786;
    private static final int ARROW_LEFT = 8790;
    private static final int ARROW_RIGHT = 8787;

    public static void open(Player p, CannonTimerBlock cannonTimerBlock, int page) {
        ChestGUI chestGUI = new ChestGUI(54, "title here");

        int currentRow = 0; // starts with 0
        Map<Integer, CannonTimerTick> content = readContent(cannonTimerBlock.getTicks(), page);

        if (page > 1) {
            /* Set PREVIOUS Item */
            Item back = getHeadItem(p, ARROW_LEFT, "cannonTimer_gui_previousPage");
            back.setExecutor(s -> {
                int newpage = page - 1;
                open(p, cannonTimerBlock, newpage);
            });
            chestGUI.setItem(18, back);
            chestGUI.setItem(27, back);
            currentRow++;
        }

        Iterator<Entry<Integer, CannonTimerTick>> iterator = content.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<Integer, CannonTimerTick> entry = iterator.next();




            if (!iterator.hasNext()) {
                /* Set NEXT item */
                Item next = getHeadItem(p, ARROW_RIGHT, "cannonTimer_gui_nextPage");
                next.setExecutor(s -> {
                    int newpage = page + 1;
                    open(p, cannonTimerBlock, newpage);
                });
                chestGUI.setItem(26, next);
                chestGUI.setItem(35, next);
                currentRow++;
            }
        }
    }

    private static Map<Integer, CannonTimerTick> readContent(Map<Integer, CannonTimerTick> map, int page) {
        int mapSize = map.size();
        int begin = 0;
        int pageSize = 7;
        if (page == 1) {
            pageSize++;
        } else {
            begin += 8;
            begin += (page - 2) * 7;
        }
        if (pageSize >= mapSize) {
            return map;
        }
        TreeMap<Integer, CannonTimerTick> out = new TreeMap<>();
        List<Integer> list = new ArrayList<>(map.keySet());
        if (list.size() < begin - 1) {
            return out;
        }
        if (list.size() < begin + pageSize - 1) {
            list = list.subList(begin, list.size() - 1);
        } else {
            list = list.subList(begin, begin + pageSize);
        }
        for (int i : list) {
            out.put(i, map.get(i));
        }
        return out;

    }

    private static Item getHeadItem(Player p, int id, String key, String... args) {
        HeadItem headItem = new HeadItem(id, s -> {
        });
        headItem.setName(MessageHandler.getInstance().getString(p, key, args));
        return headItem;
    }
}
