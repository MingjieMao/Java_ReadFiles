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
     *  plus the sum of AttackBonus from all items in their inventory.
     *
     * Examples:
     * - Given items:
     *     Sword: AttackBonus=+3, AgilityBonus=0,  DefenseBonus=0
     *     Dagger: AttackBonus=+1, AgilityBonus=+1, DefenseBonus=0
     *     PC("Alice", strength=12, ..., inventory=[Sword, Dagger])
     *   Expect: 12 + (3 + 1) = 16
     *
     * Design Strategy: Iteration
     *
     * @return total strength as an int
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
     *
     * Examples:
     * - Items:
     *     Elven Cloak: AgilityBonus=+3
     *     Boots:       AgilityBonus=+2
     *     PC("Rin", dexterity=5, inventory=[Elven Cloak, Boots])
     *   Expect: 5 + (3 + 2) = 10
     *
     * Design Strategy: Iteration
     *
     * @return total dexterity as an int
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
     *
     * Examples:
     * - Given Items: Wooden Shield: DefenseBonus=+5
     *          Iron Helm:     DefenseBonus=+2
     *          PC("Baldur", fortitude=4, inventory=[Wooden Shield, Iron Helm])
     *   Expect: 4 + (5 + 2) = 11
     *
     * Design Strategy: Iteration
     *
     * @return total fortitude as an int
     */
    public int computeTotalFortitude() {
        int totalBonus = 0;
        for (GameItem item : inventory) {
            totalBonus = totalBonus + item.getDefenseBonus();
        }
        return this.fortitude + totalBonus;
    }

    /**
     * Creates a PlayerCharacter from parsed properties and an item lookup.
     *
     * Examples:
     * - Given: name="Alice", strength=12, dex=5, fort=3, inventoryString="Sword,Dagger"
     *            itemLookup: {"Sword"->SwordItem, "Dagger"->DaggerItem}
     *   Expect: returns new PlayerCharacter("Alice", 12, 5, 3, [SwordItem, DaggerItem])
     *
     * - Given: name="Lonely", strength=1, dex=1, fort=1, inventoryString=null
     *   Expect: returns new PlayerCharacter("Lonely", 1, 1, 1, [])
     *
     * Design Strategy: Iteration
     *
     * @param name             character name parsed from section header
     * @param strength         parsed base Strength
     * @param dexterity        parsed base Dexterity
     * @param fortitude        parsed base Fortitude
     * @param inventoryString  comma-separated item names, e.g., "Sword,Dagger"
     * @param itemLookup       map from item name to {@code GameItem}
     * @return a new PlayerCharacter built from the given properties
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
     *
     * Examples:
     * - Given: File content with two sections:
     *    [Alice]
     *    Strength=12
     *    Dexterity=5
     *    Fortitude=3
     *    Inventory=Sword,Dagger
     *
     *    [Bob]
     *    Strength=8
     *    Dexterity=9
     *    Fortitude=10
     *    Inventory=Wooden Shield
     *
     *    Expect: returns PlayerCharacter[] length == 2:
     *            [0]: name="Alice", strength=12, dexterity=5, fortitude=3, inventory=[Sword, Dagger]
     *            [1]: name="Bob",   strength=8,  dexterity=9, fortitude=10, inventory=[Wooden Shield]
     *
     * Design Strategy: Iteration
     *
     * Effects: opens and reads the file line-by-line (file input).
     *
     * @param file     character INI file to load
     * @param allItems complete list of all possible items in the game
     * @return array of PlayerCharacter, one per section in the file
     * @throws RuntimeException wraps any exception during I/O or parsing
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

    /**
     * Mutable holder for the properties of the character currently being parsed.
     *
     * Examples:
     * - Given: [Alice]
     *          Strength=12
     *          Inventory=Sword,Dagger
     *   Expect: props == { name="Alice", strength=12, dexterity=0, fortitude=0, inventoryString="Sword,Dagger" }
     *
     * @implSpec Invariants:
     *   1. If {@code name != null}, fields represent the in-progress section.
     *   2. {@link #resetStats()} sets strength, dexterity, fortitude to 0 (name unchanged).
     */
    private static final class CharProps {
        String name = null;
        int strength = 0, dexterity = 0, fortitude = 0;
        String inventoryString = null;

        void resetStats() { strength = dexterity = fortitude = 0; }
    }

    /**
     * Parses a single line of the character INI; on a new section header, commits the previous character.
     *
     * Examples:
     * - Given: characters=[], props={name=null, strength=0, dexterity=0, fortitude=0, inventoryString=null}
     *          line="[Alice]"
     *   Expect: characters=[]
     *           props={name="Alice", strength=0, dexterity=0, fortitude=0, inventoryString=null}
     *
     * - Given: Key=value line "Strength=12":
     *          props={name="Alice", strength=0, dexterity=0, fortitude=0, inventoryString=null}
     *   Expect: props={name="Alice", strength=12, dexterity=0, fortitude=0, inventoryString=null}
     *
     * - Given: Key=value line "Inventory=Sword,Dagger":
     *          props={name="Alice", strength=12, dexterity=5, fortitude=3, inventoryString=null}
     *   Expect: props={name="Alice", strength=12, dexterity=5, fortitude=3, inventoryString="Sword,Dagger"}
     *
     * - Given: characters=[], props={name="Alice", strength=12, dexterity=5, fortitude=3, inventoryString="Sword,Dagger"}
     *          line="[Bob]"
     *   Expect: characters=[ createCharacterFromProps("Alice",12,5,3,"Sword,Dagger", itemLookup) ]
     *           props={name="Bob", strength=0, dexterity=0, fortitude=0, inventoryString=null}
     *
     * Design Strategy: Case Distinction
     *
     * Effects:
     * - On header: may append exactly one PlayerCharacter to {@code characters}.
     * - On key=value: updates exactly one field in {@code props}.
     *
     * @param line        one raw line from the INI, e.g., "[Alice]" or "Strength=12"
     * @param characters  output list to receive committed characters
     * @param itemLookup  item lookup for building inventories
     * @param props       mutable in-progress state for the current section
     * @throws NumberFormatException if a numeric value cannot be parsed
     */
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
