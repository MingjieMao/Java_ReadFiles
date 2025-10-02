import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * GameItem represents a single RPG item defined by an INI section.
 *
 * Examples:
 * - Given: [Heavy Sword]
 *          Type=Weapon
 *          Weight=15
 *          AgilityBonus=-5
 *          AttackBonus=10
 *          Value=250
 *   Expect: new GameItem("Heavy Sword", 250, 15, 10, -5, 0)
 * - Given: [Elven Cloak]
 *          AgilityBonus=3
 *          Value=120
 *          DefenseBonus=1
 *          Weight=3
 *   Expect: new GameItem("Elven Cloak", 120, 3, 0, 3, 1)
 *
 * Template:
 * { this.name, this.value, this.weight, this.attackBonus, this.agilityBonus, this.defenseBonus }
 *
 * @implSpec Invariants:
 *   1. {@code name} is non-null and non-empty.
 *   2. All numeric fields are defined integers (defaults allowed per assignment spec).
 *   3. Instances are immutable after construction (all fields are {@code final}).
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
     *
     * Design Strategy: Simple Expression (field assignment).
     *
     * @param name item name
     * @param value the value of item
     * @param weight the weight of item
     * @param attackBonus the attack bonus of item
     * @param agilityBonus the agility bonus of item
     * @param defenseBonus the defense bonus of item
     * @implSpec Pre: parameters are provided as parsed integers; name is expected non-empty.
     * @implSpec Post: all fields equal to provided arguments; object is immutable.
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
     *
     * Examples:
     * - Given: new GameItem("Heavy Sword", 250, 15, 10, -5, 0)
     *   Expect: "Heavy Sword"
     * - Given: new GameItem("Elven Cloak", 120, 3, 0, 3, 1)
     *   Expect: "Elven Cloak"
     *
     * Design Strategy: Simple Expression.
     *
     * @return item name
     */

    public String getName() {
        return name;
    }

    /**
     * Returns the value of the item in gold pieces.
     *
     * Examples:
     * - Given: new GameItem("Heavy Sword", 250, 15, 10, -5, 0)
     *   Expect: 250
     * - Given: new GameItem("Elven Cloak", 120, 3, 0, 3, 1)
     * - Expect: 120
     *
     * Design Strategy: Simple Expression.
     *
     * @return the value of item
     */
    public int getValue() {
        return value;
    }

    /**
     * Returns the weight of the item in kilograms.
     *
     * Examples:
     * - Given: new GameItem("Heavy Sword", 250, 15, 10, -5, 0)
     *   Expect: 15
     * - Given: new GameItem("Elven Cloak", 120, 3, 0, 3, 1)
     *   Expect: 3
     *
     * Design Strategy: Simple Expression.
     *
     * @return the weight of item
     */
    public int getWeight() {
        return weight;
    }

    /**
     * Returns the attack bonus for this item.
     *
     * Examples:
     * - Given: new GameItem("Heavy Sword", 250, 15, 10, -5, 0)
     *   Expect: 10
     * - Given: new GameItem("Elven Cloak", 120, 3, 0, 3, 1)
     *   Expect: 0
     *
     * Design Strategy: Simple Expression.
     *
     * @return the attack bonus of item
     */
    public int getAttackBonus() {
        return attackBonus;
    }

    /**
     * Returns the agility bonus for this item.
     *
     * Examples:
     * - Given: new GameItem("Heavy Sword", 250, 15, 10, -5, 0)
     *   Expect: 10
     * - Given: new GameItem("Elven Cloak", 120, 3, 0, 3, 1)
     *   Expect: 0
     *
     * Design Strategy: Simple Expression.
     *
     * @return the agility bonus of item
     */
    public int getAgilityBonus() {
        return agilityBonus;
    }

    /**
     * Returns the defense bonus for this item.
     *
     * Examples:
     * - Given: new GameItem("Heavy Sword", 250, 15, 10, -5, 0)
     *   Expect: 10
     * - Given: new GameItem("Elven Cloak", 120, 3, 0, 3, 1)
     *   Expect: 0
     *
     * Design Strategy: Simple Expression.
     *
     * @return the defense bonus of item
     */
    public int getDefenseBonus() {
        return defenseBonus;
    }

    /**
     * Given a valid and non-empty INI file of the format described above,
     * reads all item definitions from the sections and returns an array
     * containing them.
     * Examples:
     * - Given: File content (single section) — returns length 1：
     *          [Health Potion]
     *          Weight=1
     *          AttackBonus=0
     *          AgilityBonus=0
     *          DefenseBonus=0
     *   Expect: GameItem[] length == 1
     *           index 0: name="Health Potion", value=50, weight=1, attackBonus=0, agilityBonus=0, defenseBonus=0
     *
     * Design Strategy: Iteration
     *
     * Effects: reads from the provided file (I/O).
     *
     * @param file The INI file to read from.
     * @return An array of GameItem objects found in the file.
     * @implSpec Pre: file is a valid INI per assignment description (non-empty, well-formed).
     * @implSpec Post: returns an array containing one GameItem per section encountered.
     * @throws RuntimeException if an IOException or a parsing error occurs.
     */
    public static GameItem[] readItems(File file) {
        List<GameItem> items = new ArrayList<>();
        ItemProps currentProps = new ItemProps();

        try (var reader = new BufferedReader(new FileReader(file))) {
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                parseItemLine(line, items, currentProps);
            }
            if (currentProps.name != null) {
                items.add(new GameItem(currentProps.name, currentProps.value, currentProps.weight,
                        currentProps.attackBonus, currentProps.agilityBonus, currentProps.defenseBonus));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return items.toArray(new GameItem[0]);
    }

    /**
     * Helper class to hold the mutable state (properties) of the GameItem currently being parsed.
     * This avoids passing multiple separate mutable variables through method parameters.
     *
     * Examples:
     * - ItemProps(name="Elven Cloak", value=100, Weight=3, AttackBonus=10, AgilityBonus=3,  DefenseBonus=1)
     *
     * @implSpec Invariants:
     * 1. If name is not null, the remaining fields hold the stats for that item.
     * 2. All numeric fields are initialized to 0.
     */
    private static class ItemProps {
        String name = null;
        int value = 0;
        int weight = 0;
        int attackBonus = 0;
        int agilityBonus = 0;
        int defenseBonus = 0;

        void resetStats() {
            value = weight = attackBonus = agilityBonus = defenseBonus = 0;
        }
    }

    /**
     * Parses a single line from the INI file and updates the item properties,
     * adding a completed GameItem to the list when a new section is encountered.
     *
     * Examples:
     * - Before: items = [ GameItem("Health Potion", 50, 1, 0, 0, 0) ]
     *           props = { name="Wooden Shield", value=0, weight=10, attackBonus=0, agilityBonus=0, defenseBonus=0 }
     *           line  = "DefenseBonus=5"
     *   After: items = [ GameItem("Health Potion", 50, 1, 0, 0, 0) ] // unchanged
     *          props = { name="Wooden Shield", value=0, weight=10, attackBonus=0, agilityBonus=0, defenseBonus=5 }
     *
     * Design Strategy: Case Distinction.
     *
     * Effects:
     * - May append exactly one new GameItem to {@code items} when a new section header is encountered.
     * - Updates one numeric field in {@code props} when a recognized key is parsed.
     *
     * @param line The line read from the INI file.
     * @param items The list to add completed GameItem objects to.
     * @param props The mutable state object holding the properties of the item being built.
     * @implSpec Postcondition: If line is a section header, one item is added to items (unless it was the very first line).
     * @throws NumberFormatException if a property value cannot be parsed to an integer.
     */
    private static void parseItemLine(String line, List<GameItem> items, ItemProps props) {
        if (line.startsWith("[") && line.endsWith("]")) {
            if (props.name != null) {
                items.add(new GameItem(props.name, props.value, props.weight, props.attackBonus,
                                       props.agilityBonus, props.defenseBonus));
            }
            props.name = line.substring(1, line.length()-1);
            props.resetStats();
        } else if (line.contains("=")) {
            String[] parts = line.split("=",2);
            String key = parts[0];
            String val = parts[1];
            switch (key) {
                case "Value" -> props.value = Integer.parseInt(val);
                case "Weight" -> props.weight = Integer.parseInt(val);
                case "AttackBonus" -> props.attackBonus = Integer.parseInt(val);
                case "AgilityBonus" -> props.agilityBonus = Integer.parseInt(val);
                case "DefenseBonus" -> props.defenseBonus = Integer.parseInt(val);
                default -> {}
            }
        }
    }
}

