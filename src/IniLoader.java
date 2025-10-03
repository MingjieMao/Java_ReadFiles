import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.StringReader;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * A tiny INI loader that converts each section into a stateful map of key→value,
 * then builds typed objects via a user-supplied factory.
 *
 * Examples:
 * - Given file: [Sword]
 *               Weight=3
 *               Value=20
 *   Expect: loadINI(file, GameItem[]::new, (name,props) -> new GameItem(...));
 *     elementFactory is called once with name="Sword" and props={"Weight":"3","Value":"20"}.
 *
 * Template:
 * { (BufferedReader reader), (String currentName), (Map<String,String> properties), (List<T> results) }
 *
 * @implSpec Invariants:
 *   1. Iteration pattern is: {@code for (line = reader.readLine(); line != null; line = reader.readLine())}.
 *   2. A section begins with a header "[Name]" on a dedicated line.
 *   3. Key-value lines have the first '=' split into key and value (unaltered, no trimming).
 *   4. No {@code continue} statements are used in the parsing loop.
 *   5. On EOF, the in-progress section (if any) is committed exactly once.
 */
public final class IniLoader {
    /**
     * Reads the given INI file. For each section, it creates a stateful
     * Map of the key-value pairs within that section. It then calls the
     * elementFactory function, passing the section name (e.g.,
     * "Heavy Sword") as the first argument and the stateful Map of its
     * properties as the second.
     *
     * Examples:
     *- Given: [A]
     *         K1=V1
     *         [B]
     *         K2=V2
     *  Expect: elementFactory("A", {"K1":"V1"}) then elementFactory("B", {"K2":"V2"});
     *          result length == 2; order == ["A","B"].
     *
     * Design Strategy: Iteration
     *
     * Effects: opens the file for reading; performs file I/O; may throw a RuntimeException on failure.
     *
     * @param file The INI file to read.
     * @param makeArray A function to create the final array of the correct type and size.
     * @param elementFactory builds one element from (sectionName, properties)
     * @param <T> element type
     * @return a non-null array containing one element per section, in encounter order
     * @throws RuntimeException if an I/O or parsing error occurs (all failures are wrapped)
     * @implSpec Precondition: {@code file != null} and points to a readable INI file.
     *           Postcondition: returns a non-null array whose length equals the number of sections.
     *           Postcondition: property insertion order is preserved (uses LinkedHashMap per section).
     */
    public static <T> T[] loadINI(File file, Function<Integer, T[]> makeArray,
                                  BiFunction<String, Map<String, String>, T> elementFactory) {
        List<T> results = new ArrayList<>();
        String currentName = null;
        Map<String, String> properties = new LinkedHashMap<>();
        try(var reader = new BufferedReader(new FileReader(file))) {
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                 if (line.startsWith("[") && line.endsWith(("]"))) {
                     // Commit previous section (if any)
                     if (currentName != null) {
                         T element = elementFactory.apply(currentName, properties);
                         results.add(element);
                     }
                     // Start a new section
                     currentName = line.substring(1, line.length()-1);
                     properties = new LinkedHashMap<>();
                 } else if (line.contains("=")) {
                     // Key=value within current section
                     String[] parts = line.split("=", 2);
                     if (parts.length == 2 && currentName != null) {
                         String key = parts[0];
                         String value = parts[1];
                         properties.put(key, value);
                     }
                 }
            }
            // Commit the last section at end Of File
            if (currentName != null) {
                T element = elementFactory.apply(currentName, properties);
                results.add(element);
            }
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
        return results.toArray(makeArray.apply(0));
    }

    /**
     * Parses the given string as a base-10 integer; returns the provided default when
     * the string is {@code null} or empty.
     *
     * Examples:
     * parseIntOrDefault(null, 0)        == 0
     * parseIntOrDefault("", 42)         == 42
     * parseIntOrDefault("0", 99)        == 0
     * parseIntOrDefault("-7", 0)        == -7
     * parseIntOrDefault(" 5", 0)        -> NumberFormatException  // no trimming
     * parseIntOrDefault("+3", 0)        -> NumberFormatException  // strict parseInt
     *
     * Design Strategy: Case distinction
     *
     * @param s   input string to parse; may be {@code null} or empty
     * @param def default value to return when {@code s} is {@code null} or empty
     * @return the parsed integer, or {@code def} when {@code s} is {@code null}/empty
     * @throws NumberFormatException if {@code s} is non-empty but not a valid base-10 integer
     */
    private static int parseIntOrDefault(String s, int def) {
        if (s == null || s.isEmpty()) return def;
        return Integer.parseInt(s);
    }

    /**
     * The same method as from Part 1, but now implemented using loadINI.
     *
     * Examples:
     * - Given: [Heavy Sword]
     *          Weight=10
     *          Value=50
     *          AttackBonus=7
     *          AgilityBonus=0
     *          DefenseBonus=0
     *   Expect: Creates one {@code GameItem("Heavy Sword", 50, 10, 7, 0, 0)}.
     *
     * Design Strategy: Combining Functions
     *
     * Effects: reads the file; numeric fields are parsed via {@code parseIntOrDefault}.
     *
     * @param file items INI file
     * @return array of {@code GameItem}
     * @throws RuntimeException if an I/O or parsing error occurs (wrapped)
     * @implSpec Precondition: every section contains the keys:
     *           {@code Weight, Value, AttackBonus, AgilityBonus, DefenseBonus} with integer values.
     *           Postcondition: returns a non-null array; one element per section.
     */
    public static GameItem[] readItems(File file) {
        Function<Integer, GameItem[]> makeArray = n -> new GameItem[n];
        BiFunction<String, Map<String,String>, GameItem> elementFactory = (name, properties) -> {
            int weight = parseIntOrDefault(properties.get("Weight"), 0);
            int value = parseIntOrDefault(properties.get("Value"), 0);
            int attackBonus = parseIntOrDefault(properties.get("AttackBonus"), 0);
            int agilityBonus = parseIntOrDefault(properties.get("AgilityBonus"), 0);
            int defenseBonus = parseIntOrDefault(properties.get("DefenseBonus"), 0);
            return new GameItem(name, value, weight, attackBonus, agilityBonus, defenseBonus);
        };
        return IniLoader.loadINI(file, makeArray, elementFactory);
    }

    /**
     * The same method as from Part 2, but now implemented using loadINI.
     *
     * Examples:
     * - Given items: Sword(Attack+3), Cloak(Agility+2)
     *   Characters:
     *     [Alice]
     *     Strength=12
     *     Dexterity=5
     *     Fortitude=3
     *     Inventory=Sword,Cloak
     *   Expect:
     *     new PlayerCharacter("Alice", 12, 5, 3, [Sword, Cloak])
     *     Alice.computeTotalStrength() == 12 + 3
     *     Alice.computeTotalDexterity() == 5 + 2
     *
     * Design Strategy: Iteration
     *
     * Effects: reads the file; splits the inventory string by comma and resolves items by name.
     *
     * @param file     characters INI file
     * @param allItems all possible items that inventories may reference
     * @return array of {@code PlayerCharacter}
     * @throws RuntimeException if an I/O or parsing error occurs (wrapped)
     * @implSpec Precondition: {@code allItems} contains every item name referenced by {@code Inventory}.
     *           Precondition: each section contains integer keys {@code Strength, Dexterity, Fortitude};
     *           if {@code Inventory} is present and non-empty, names are separated by literal commas (no trimming).
     *           Postcondition: returns a non-null array; one element per section; the inventory array is a defensive copy.
     */
    public static PlayerCharacter[] readCharacters(File file, GameItem[] allItems) {
        Map<String, GameItem> byName = new HashMap<>();
        for (GameItem item : allItems) {
            if (item != null && item.getName() != null){
                byName.put(item.getName(), item);
            }
        }
        Function<Integer, PlayerCharacter[]> makeArray = n -> new PlayerCharacter[n];
        BiFunction<String, Map<String, String>, PlayerCharacter> elementFactory = (name, properties) -> {
            int strength = parseIntOrDefault(properties.get("Strength"), 0);
            int dexterity = parseIntOrDefault(properties.get("Dexterity"), 0);
            int fortitude = parseIntOrDefault(properties.get("Fortitude"), 0);
            String inventory = properties.get("Inventory");
            List<GameItem> bag = new ArrayList<>();
            if (inventory != null && !inventory.isEmpty()) {
                for (String itemName : inventory.split(",")) {
                    if (itemName.isEmpty()) {
                        continue;
                    }
                    GameItem item = byName.get(itemName);
                    if (item != null) {
                        bag.add(item);
                    }
                }
            }
            return new PlayerCharacter(name, strength, dexterity, fortitude,bag.toArray(new GameItem[0]));
        };
        return IniLoader.loadINI(file, makeArray, elementFactory);
    }
}

