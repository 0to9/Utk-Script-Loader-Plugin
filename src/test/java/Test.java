import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@SuppressWarnings({"rawtypes", "unchecked"})
public class Test {
    public static void main(String[] args) {
    }

    ChatColor WARNING_TEXT_COLOR = ChatColor.GOLD;

    MultiValuedMap<Objective, Objective> objectiveMap = new HashSetValuedHashMap<>();
    Map settings = new HashMap();
    DisplaySlot[] displaySlots = {DisplaySlot.BELOW_NAME, DisplaySlot.PLAYER_LIST, DisplaySlot.SIDEBAR};

    public void updatePlayerScoreboard(Player player) {
        ScoreboardManager manager = Objects.requireNonNull(Bukkit.getScoreboardManager());
        Scoreboard currentBoard = player.getScoreboard();
        Scoreboard mainBoard = manager.getMainScoreboard();
        if (currentBoard == mainBoard) {
            currentBoard = manager.getNewScoreboard();
            player.setScoreboard(currentBoard);
        }

        String[] config = (String[]) settings.get(player.getName());
        for (int i = 0; i < 3; i++) {
            Objective mainObjective = null;

            String conf = config[i];
            if ("".equals(conf))
                config[i] = null;
            else if (conf != null) {
                Objective oldObj = currentBoard.getObjective(displaySlots[i]);
                if (oldObj != null) {
                    if (conf.equals(oldObj.getName()))
                        continue; // skip slot if already set correctly

                    Objective mainObj = mainBoard.getObjective(oldObj.getName());
                    objectiveMap.removeMapping(mainObj, oldObj);
                    currentBoard.clearSlot(displaySlots[i]);
                }
                if (conf.equals("disable_slot"))
                    continue; // disable this scoreboard display slot

                mainObjective = mainBoard.getObjective(conf);
                if (mainObjective == null) {
                    player.sendMessage(WARNING_TEXT_COLOR + "Unable to find objective: " + conf);
                    player.sendMessage(WARNING_TEXT_COLOR + "Loading in default server scoreboard");
                    config[i] = null;
                }
            }

            if (mainObjective == null)
                mainObjective = mainBoard.getObjective(displaySlots[i]);

            if (mainObjective != null) {
                String objectiveName = mainObjective.getName();

                Objective newObjective = currentBoard.getObjective(objectiveName);
                if (newObjective == null) {
                    String criteria = mainObjective.getCriteria(), displayName = mainObjective.getDisplayName();
                    newObjective = currentBoard.registerNewObjective(objectiveName, criteria, displayName);
                }

                restockScores(mainObjective, newObjective);
                objectiveMap.put(mainObjective, newObjective);
                newObjective.setDisplaySlot(displaySlots[i]);
            }
        }
    }

    public void restockScores(Objective oldObjective, Objective newObjective) {
//        for (OfflinePlayer player : $ALL_PLAYERS) {
//            String name = player.getName();
//            if (name != null)
//                newObjective.getScore(name).setScore(oldObjective.getScore(name).getScore());
//        }
    }

    public void updateScores() {
        for (Map.Entry e : objectiveMap.entries())
            restockScores((Objective) e.getKey(), (Objective) e.getValue());
    }
}
