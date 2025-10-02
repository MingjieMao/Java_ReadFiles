import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;

/**
 * PlayerCharacter represents a single player-controlled character described by an INI section.
 *
 * Examples:
 * - Given: [Gimli]
 *          Class=Dwarf Warrior
 *          Strength=18
 *          Dexterity=12
 *          Fortitude=30
 *          Inventory=Battle Axe,Chainmail,Health Potion
 *   Expect: new PlayerCharacter("Gimli", 18, 12, 30, [Battle Axe,Chainmail,Health Potion])
 * - Given: [Legolas]
 *          Class=Elf Ranger
 *          Strength=14
 *          Fortitude=25
 *          Location=Mirkwood
 *          Inventory=Longbow,Leather Armor,Arrows
 *   Expect: new PlayerCharacter("Legolas", 14, 25, 0, [Longbow,Leather Armor,Arrows])
 *
 * Template:
 * { this.name, this.strength, this.dexterity, this.fortitude, this.inventory }
 *
 * @implSpec Invariants:
 *   1. {@code name} is non-null and non-empty.
 *   2. {@code inventory} is an immutable defensive copy of the provided array.
 *   3. Base stats ({@code strength, dexterity, fortitude}) are integers as parsed from file.
 */
public final class PlayerCharacter {
    private final String name;
    private final int strength;
    private final int dexterity;
    private final int fortitude;
    private final GameItem[] inventory;

    /**
     * Creates a new character.
     *
     * Design Strategy: Simple Expression (field assignment).
     *
     * Examples:
     * - new PlayerCharacter("Alice", 12, 5, 3, new GameItem[]{sword, dagger})
     * - new PlayerCharacter("Bob", 8, 9, 10, new GameItem[]{shield})
     *
     * Effects: stores a defensive copy of {@code inventory}.
     *
     * @param name character name (e.g., "Alice")
     * @param strength base Strength (e.g., 12)
     * @param dexterity base Dexterity (e.g., 5)
     * @param fortitude base Fortitude (e.g., 3)
     * @param inventory array of items the character holds
     * @implSpec Post: fields equal to provided arguments; {@code this.inventory} is a copy.
     */
    public PlayerCharacter(String name, int strength, int dexterity,
                                  int fortitude, GameItem[] inventory) {
        this.name = name;
        this.strength = strength;
        this.dexterity = dexterity;
        this.fortitude = fortitude;
        this.inventory = Arrays.copyOf(inventory, inventory.length);
    }

    /**
     * Returns the character's name (from the INI section header).
     *
     * Examples:
     * - Given: new PlayerCharacter("Alice", 12, 5, 3, new GameItem[0])
     *   Expect: "Alice"
     * - Given: new PlayerCharacter("Bob", 8, 9, 10, new GameItem[0])
     *   Expect: "Bob"
     *
     * Design Strategy: Simple Expression.
     *
     * @return character name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the character's base Strength stat (without item bonuses).
     *
     * Examples:
     * - Given: new PlayerCharacter("Alice", 12, 5, 3, new GameItem[0])
     *   Expect: 12
     * - Given: new PlayerCharacter("Bob", 8, 9, 10, new GameItem[0])
     *   Expect: 8
     *
     * Design Strategy: Simple Expression.
     *
     * @return base Strength
     */
    public int getStrength() {
        return strength;
    }

    /**
     * Returns the character's base Dexterity stat (without item bonuses).
     */
    /**
     * Returns the character's base Dexterity stat (without item bonuses).
     *
     * Examples:
     * - Given: new PlayerCharacter("Alice", 12, 5, 3, new GameItem[0])
     *   Expect: 5
     * - Given: new PlayerCharacter("Bob", 8, 9, 10, new GameItem[0])
     *   Expect: 9
     *
     * Design Strategy: Simple Expression.
     *
     * @return base Dexterity
     */
    public int getDexterity() {
        return dexterity;
    }

    /**
     * Returns the character's base Fortitude stat (without item bonuses).
     *
     * Examples:
     * - Given: new PlayerCharacter("Alice", 12, 5, 3, new GameItem[0])
     *   Expect: 3
     * - Given: new PlayerCharacter("Bob", 8, 9, 10, new GameItem[0])
     *   Expect: 10
     *
     * Design Strategy: Simple Expression.
     *
     * @return base Fortitude
     */
    public int getFortitude() {
        return fortitude;
    }

    /**
     * Returns an array of GameItem objects in the character's inventory.
     *
     * Examples:
     * - Given: new PlayerCharacter("Alice", 12, 5, 3, [sword, dagger]).getInventory()
     *   Expect: new GameItem[]{sword, dagger}
     *
     * Design Strategy: Simple Expression
     *
     * @return copied array of items; modifying the result does not affect the character
     */
    public GameItem[] getInventory() {
        return Arrays.copyOf(inventory, inventory.length);
    }

    /**
     * Returns the character's total strength, calculated as base Strength
     * plus the sum of AttackBonus from all items in their inventory.
     */
    public int computeTotalStrength() {
        int totalBonus = 0;
        for (GameItem item : inventory) {
            totalBonus = totalBonus + item.getAttackBonus();
        }
        return strength + totalBonus;
    }

    /**
     * Returns the character's total dexterity calculated as base Dexterity
     * plus the sum of AgilityBonus from all items in their inventory.
     */
    public int computeTotalDexterity() {
        int totalBonus = 0;
        for (GameItem item : inventory) {
            totalBonus = totalBonus + item.getAgilityBonus();
        }
        return this.dexterity + totalBonus;
    }

    /**
     * Returns the character's total fortitude calculated as base Fortitude
     * plus the sum of DefenseBonus from all items in their inventory.
     */
    public int computeTotalFortitude() {
        int totalBonus = 0;
        for (GameItem item : inventory) {
            totalBonus = totalBonus + item.getDefenseBonus();
        }
        return this.fortitude + totalBonus;
    }

    /**
     *
     * @param name
     * @param strength
     * @param dexterity
     * @param fortitude
     * @param inventoryString
     * @param itemLookup
     * @return
     */
    private static PlayerCharacter createCharacterFromProps(String name, int strength, int dexterity, int fortitude,
                                                            String inventoryString, Map<String, GameItem> itemLookup) {
        List<GameItem> items = new ArrayList<>();
        if (inventoryString != null && !inventoryString.isEmpty()) {
            for (String itemName : inventoryString.split(",")) {
                GameItem item = itemLookup.get(itemName);
                items.add(item);
            }
        }
        return new PlayerCharacter(name, strength, dexterity, fortitude, items.toArray(new GameItem[0]));
    }

    /**
     * Given a valid and non-empty character INI file and a complete array
     * of all possible GameItems that exist in the game (that is, you can
     * assume that every item in a character's inventory is present in
     * allItems), this method loads the characters from the file.
     */
    public static PlayerCharacter[] readCharacters(File file, GameItem[] allItems) {
        List<PlayerCharacter> characters = new ArrayList<>();
        Map<String, GameItem> itemLookup = new HashMap<>();
        for (GameItem item : allItems) {
            itemLookup.put(item.getName(), item);
        }

        CharProps props = new CharProps();

        try(var reader = new BufferedReader(new FileReader(file))) {
            for(String line = reader.readLine(); line != null; line = reader.readLine()) {
                parseCharacterLine(line, characters, itemLookup, props);
            }
            if (props.name != null) {
                characters.add(createCharacterFromProps(
                        props.name, props.strength, props.dexterity, props.fortitude, props.inventoryString, itemLookup));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return characters.toArray(new PlayerCharacter[0]);
    }

    private static final class CharProps {
        String name = null;
        int strength = 0, dexterity = 0, fortitude = 0;
        String inventoryString = null;

        void resetStats() { strength = dexterity = fortitude = 0; }
    }

    private static void parseCharacterLine(String line, List<PlayerCharacter> characters,
                                           Map<String, GameItem> itemLookup, CharProps props) {
        if (line.startsWith("[") && line.endsWith("]")) {
            if (props.name != null) {
                characters.add(createCharacterFromProps(
                        props.name, props.strength, props.dexterity, props.fortitude, props.inventoryString, itemLookup
                ));
            }
            props.name = line.substring(1, line.length() - 1);
            props.resetStats();
            props.inventoryString = null;
        } else if (line.contains("=")) {
            String[] parts = line.split("=", 2);
            String key = parts[0];
            String value = parts[1];
            switch (key) {
                case "Strength"  -> props.strength = Integer.parseInt(value);
                case "Dexterity" -> props.dexterity = Integer.parseInt(value);
                case "Fortitude" -> props.fortitude = Integer.parseInt(value);
                case "Inventory" -> props.inventoryString = value;
                default -> {}
            }
        }
    }

    }
