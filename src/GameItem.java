import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 */
public class GameItem {
    // Fields
    private final String name;
    private final int value;
    private final int weight;
    private final int attackBonus;
    private final int agilityBonus;
    private final int defenseBonus;

    /**
     * Constructs a new GameItem.
     */
    public GameItem(String name, int value, int weight, int attackBonus,
                    int agilityBonus, int defenseBonus) {
        this.name = name;
        this.value = value;
        this.weight = weight;
        this.attackBonus = attackBonus;
        this.agilityBonus = agilityBonus;
        this.defenseBonus = defenseBonus;
    }

    /**
     * Returns the name of the item (derived from the INI section header).
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the value of the item in gold pieces.
     */
    public int getValue() {
        return value;
    }

    /**
     * Returns the weight of the item in kilograms.
     */
    public int getWeight() {
        return weight;
    }

    /**
     * Returns the attack bonus for this item.
     */
    public int getAttackBonus() {
        return attackBonus;
    }

    /**
     * Returns the agility bonus for this item.
     */
    public int getAgilityBonus() {
        return agilityBonus;
    }

    /**
     * Returns the defense bonus for this item.
     */
    public int getDefenseBonus() {
        return defenseBonus;
    }

    /**
     * Given a valid and non-empty INI file of the format described above,
     * reads all item definitions from the sections and returns an array
     * containing them.
     */
    public static GameItem[] readItems(File file) {
        List<GameItem> items = new ArrayList<>();
        String name = null;
        int value = 0, weight = 0, attackBonus = 0, agilityBonus = 0, defenseBonus = 0;

        try (var reader = new BufferedReader(new FileReader(file))) {
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                if (line.startsWith("[") && line.endsWith("]")) {
                    items.add(new GameItem(name, value, weight, attackBonus, agilityBonus, defenseBonus));
                    name = line.substring(1, line.length()-1);
                    value = weight = attackBonus = agilityBonus = defenseBonus = 0;
                } else if (line.contains("=")) {
                    String[] parts = line.split("=");
                    String key = parts[0];
                    int val = Integer.parseInt(parts[1]);
                    switch (key) {
                        case "Value" -> value = val;
                        case "Weight" -> weight = val;
                        case "AttackBonus" -> attackBonus = val;
                        case "AgilityBonus" -> agilityBonus = val;
                        case "DefenseBonus" -> defenseBonus = val;
                    }
                }
            }

            if (name != null) {
                items.add(new GameItem(name, value, weight, attackBonus, agilityBonus, defenseBonus));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return items.toArray(new GameItem[0]);
    }
}
