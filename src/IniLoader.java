import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * A tiny INI loader that converts each section into a stateful map of key→value,
 * then builds typed objects via a user-supplied factory.
 * Examples:
 * - Given file: [Sword]
 *               Weight=3
 *               Value=20
 *   Expect: loadINI(file, GameItem[]::new, (name,props) -> new GameItem(...));
 *     elementFactory is called once with name="Sword" and props={"Weight":"3","Value":"20"}.
 * Template:
 * { (BufferedReader reader), (String currentName), (Map<String,String> properties), (List<T> results) }
 * Effects:
 * - Opens and reads the file (I/O).
 * - Builds and returns an array of constructed objects.
 *
 * @implSpec
 * Invariants:
 * - Each section starts with a non-null, non-empty header `[Name]`.
 * - Each key=value pair belongs to the most recent header.
 * - Properties map is reset on every new section.
 * Pre-conditions:
 * - Input file must exist and be readable.
 * - Factory function must accept valid section names and maps.
 * Post-conditions:
 * - Returns a non-null array of T constructed from all sections.
 * - Array length equals number of `[Section]` headers encountered.
 */
public final class IniLoader {
    /**
     * Reads the given INI file. For each section, it creates a stateful
     * Map of the key-value pairs within that section. It then calls the
     * elementFactory function, passing the section name (e.g.,
     * "Heavy Sword") as the first argument and the stateful Map of its
     * properties as the second.
     * Examples:
     * - Given: [A]
     *         K1=V1
     *         [B]
     *         K2=V2
     *   Expect: elementFactory("A", {"K1":"V1"}) then elementFactory("B", {"K2":"V2"});
     *          result length == 2; order == ["A","B"].
     * Design Strategy: Iteration and Case Distinction
     * Effects:
     * - Opens the file for reading (I/O).
     * - May throw {@code RuntimeException} if any error occurs.
     *
     * @param file The INI file to read.
     * @param makeArray A function to create the final array of the correct type and size.
     * @param elementFactory builds one element from (sectionName, properties)
     * @param <T> element type
     * @return a non-null array containing one element per section, in encounter order
     * @throws RuntimeException if an I/O or parsing error occurs (all failures are wrapped)
     * @implSpec
     * Invariants:
     * - Section names are taken literally between "[" and "]".
     * - Properties map is reset at each new section.
     * - Order of sections in file is preserved in output array.
     * Pre-conditions:
     * - {@code file} must be non-null and point to an existing readable file.
     * - {@code makeArray} and {@code elementFactory} must be non-null.
     * Post-conditions:
     * - Returns an array of size == number of section headers encountered.
     * - Each element corresponds to one header and its associated key-value pairs.
     */
    public static <T> T[] loadINI(File file, Function<Integer, T[]> makeArray,
                                  BiFunction<String, Map<String, String>, T> elementFactory) {
        // Initialize state container for current parsing session
        IniState<T> state = new IniState<>(elementFactory);

        // Read file line by line until end of file (line == null).
        try(var reader = new BufferedReader(new FileReader(file))) {
                parseAllLines(reader, state);
                state.commitSection();
        } catch(Exception e) {
            throw new RuntimeException(e);  // Wrap any I/O or parsing errors into RuntimeException.
        }
        // Convert the accumulated list into a typed array using makeArray factory.
        return state.results.toArray(makeArray.apply(0));
    }

    /**
     * Parses all lines from the INI file and updates parsing state accordingly.
     * Examples:
     * - Given file:
     *      [A]
     *      k1=v1
     *      [B]
     *      k2=v2
     *   Expect:
     *      - Line "[A]"  -> state.currentName = "A"
     *      - Line "k1=v1" -> state.properties = {"k1":"v1"}
     *      - Line "[B]"  -> commitSection("A"), start new section "B"
     *      - Line "k2=v2" -> state.properties = {"k2":"v2"}
     * Design Strategy: Iteration and Case Distinction
     * Effects:
     * - Mutates {@code state}: may update {@code currentName}, {@code properties}, and
     *   append committed sections into {@code results}.
     * - Does not close {@code reader} (caller manages resource).
     *
     * @param reader BufferedReader positioned at the start of the INI file
     * @param state mutable parsing state (tracks current section and accumulated results)
     * @param <T> element type returned by elementFactory
     * @throws IOException if an error occurs during line reading
     * @implSpec
     * Invariant:
     * - {@code state.currentName} is the most recent section header (if any);
     * - {@code state.properties} stores key→value pairs for that section.
     * Pre-condition:
     * {@code reader} and {@code state} are non-null.
     * Post-condition:
     * - At end-of-file, the last section is not automatically committed;
     * - caller (e.g., {@code loadINI}) must call {@code state.commitSection()}.
     */
    private static <T> void parseAllLines(BufferedReader reader, IniState<T> state) throws IOException {
        for (String line = reader.readLine(); line != null; line = reader.readLine()) {
            // Case 1: Section header like [SectionName]
            if (line.startsWith("[") && line.endsWith("]")) {
                // If we were already in a section, finalize it by calling elementFactory.
                state.commitSection();
                // Start new section
                state.currentName = line.substring(1, line.length() - 1);

            // Case 2: Key=Value inside section
            } else if (line.contains("=")) {
                String[] parts = line.split("=", 2);
                if (parts.length == 2 && state.currentName != null) {
                    String key = parts[0];
                    String value = parts[1];
                    state.properties.put(key, value);  // Add (key,value) to current section’s map.
                }
            }
        }
    }

    /**
     * State holder for parsing one INI file.
     * Example:
     * - Start parsing: {@code currentName = null}, {@code properties = {}}.
     * - After reading "[Sword]": {@code currentName = "Sword"}.
     * - After reading "Weight=10": {@code properties = {"Weight":"10"}}.
     * - When calling {@code commitSection()}, a new T is constructed and added
     *   to {@code results}, then state resets for the next section.
     * Design Strategy: Simple Expression
     *
     * @param <T> element type constructed from each section
     * @implSpec
     * Invariants:
     * - {@code currentName} is either null or the last seen section header.
     * - {@code properties} contains key→value pairs only for the current section.
     * - {@code results} grows monotonically (elements only added, never removed).
     * Pre-conditions:
     * - {@code elementFactory} must be non-null and accept valid input.
     * Post-conditions:
     * - After {@code commitSection()}, if {@code currentName != null}, one new
     *   element is appended to {@code results} and state resets.
     */
    private static class IniState<T> {
        final BiFunction<String, Map<String, String>, T> elementFactory;
        final List<T> results = new ArrayList<>();
        String currentName = null;
        Map<String, String> properties = new LinkedHashMap<>();

        IniState(BiFunction<String, Map<String, String>, T> elementFactory) {
            this.elementFactory = elementFactory;
        }

        /**
         * Commits the current section: build element and reset state.
         * Effects:
         * - If {@code currentName != null}, constructs one element and appends to {@code results}.
         * - Resets {@code currentName = null} and {@code properties = new LinkedHashMap<>}.
         *
         * @implSpec
         * - Pre-condition: {@code elementFactory} must not be null.
         * - Post-condition: If {@code currentName} was non-null, then
         *   {@code results.size()} increases by 1.
         */
        void commitSection() {
            if (currentName != null) {
                T element = elementFactory.apply(currentName, properties);  // Build element and add to results
                results.add(element);
                properties = new LinkedHashMap<>(); // Reset properties map for the next section
            }
        }
    }

    /**
     * Parses the given string as a base-10 integer; returns the provided default when
     * the string is {@code null} or empty.
     * Examples:
     * parseIntOrDefault(null, 0)        == 0
     * parseIntOrDefault("", 42)         == 42
     * parseIntOrDefault("0", 99)        == 0
     * parseIntOrDefault("-7", 0)        == -7
     * parseIntOrDefault(" 5", 0)        -> NumberFormatException
     * parseIntOrDefault("+3", 0)        -> NumberFormatException
     * Design Strategy: Case distinction
     *
     * @param s   input string to parse; may be {@code null} or empty
     * @return the parsed integer, or {@code 0} when {@code s} is {@code null}empty
     * @throws NumberFormatException if {@code s} is non-empty but not a valid base-10 integer
     */
    private static int parseIntOrDefault(String s) {
        // Case 1: If input is null or empty, return default.
        if (s == null || s.isEmpty()) {
            return 0;
        }
        // Case 2: Otherwise, parse the string as an integer.
        return Integer.parseInt(s);
    }

    /**
     * The same method as from Part 1, but now implemented using loadINI.
     * Examples:
     * - Given: [Heavy Sword]
     *          Weight=10
     *          Value=50
     *          AttackBonus=7
     *          AgilityBonus=0
     *          DefenseBonus=0
     *   Expect: Creates one {@code GameItem("Heavy Sword", 50, 10, 7, 0, 0)}.
     * Design Strategy: Combining Functions
     * Effects:
     * - Opens and reads the file (I/O).
     * - Parses numeric fields with {@code parseIntOrDefault}.
     * - May throw {@code RuntimeException} if reading/parsing fails.
     *
     * @param file items INI file
     * @return array of {@code GameItem}
     * @throws RuntimeException if an I/O or parsing error occurs (wrapped)
     * @implSpec
     * Invariants:
     * - Every section header corresponds to one {@code GameItem}.
     * - All numeric fields default to 0 if missing or empty.
     * Pre-conditions:
     * - {@code file} is non-null, exists, and is a valid non-empty INI with item sections.
     * Post-conditions:
     * - Returns a non-null array of {@code GameItem}.
     * - Order of array matches order of sections in the file.
     */
    public static GameItem[] readItems(File file) {
        // Function to create the result array of correct type and size (e.g. new GameItem[n]).
        Function<Integer, GameItem[]> makeArray = GameItem[]::new;
        // Build one GameItem from (sectionName, properties)
        BiFunction<String, Map<String,String>, GameItem> elementFactory = (name, properties) -> {
            int weight = parseIntOrDefault(properties.get("Weight"));
            int value = parseIntOrDefault(properties.get("Value"));
            int attackBonus = parseIntOrDefault(properties.get("AttackBonus"));
            int agilityBonus = parseIntOrDefault(properties.get("AgilityBonus"));
            int defenseBonus = parseIntOrDefault(properties.get("DefenseBonus"));
            return new GameItem(name, value, weight, attackBonus, agilityBonus, defenseBonus);
        };
        // Delegate parsing and object creation to IniLoader
        return IniLoader.loadINI(file, makeArray, elementFactory);
    }

    /**
     * The same method as from Part 2, but now implemented using loadINI.
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
     * Design Strategy: Iteration
     * Effects:
     * - Opens and reads the file (I/O).
     * - Splits the inventory string, resolves each name to a {@code GameItem}.
     * - Missing/unknown items are skipped silently.
     * - May throw {@code RuntimeException} if reading/parsing fails.
     *
     * @param file     characters INI file
     * @param allItems all possible items that inventories may reference
     * @return array of {@code PlayerCharacter}
     * @throws RuntimeException if an I/O or parsing error occurs (wrapped)
     * @implSpec
     * Invariants:
     * - Every section header corresponds to one {@code PlayerCharacter}.
     * - Inventory entries reference existing item names (if not found, skipped).
     * - Numeric attributes default to 0 if missing/empty.
     * Pre-conditions:
     * - {@code file} is non-null, exists, and is a valid non-empty characters INI.
     * - {@code allItems} is non-null and contains all items that inventories may reference.
     * Post-conditions:
     * - Returns a non-null array of {@code PlayerCharacter}.
     * - Array order matches section order in file.
     */
    public static PlayerCharacter[] readCharacters(File file, GameItem[] allItems) {
        // Step 1: Build a lookup map from item name to GameItem
        Map<String, GameItem> byName = new HashMap<>();
        for (GameItem item : allItems) {
            if (item != null && item.getName() != null){
                byName.put(item.getName(), item);
            }
        }
        // Step 2: Array constructor function for PlayerCharacter[]
        Function<Integer, PlayerCharacter[]> makeArray = PlayerCharacter[]::new;

        // Step 3: Factory function to build one PlayerCharacter from section
        BiFunction<String, Map<String, String>, PlayerCharacter> elementFactory = (name, properties) -> {
            int strength = parseIntOrDefault(properties.get("Strength"));
            int dexterity = parseIntOrDefault(properties.get("Dexterity"));
            int fortitude = parseIntOrDefault(properties.get("Fortitude"));
            String inventory = properties.get("Inventory");
            // Build inventory list (resolve item names into GameItem objects)
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
        // Step 4: Delegate parsing to IniLoader
        return IniLoader.loadINI(file, makeArray, elementFactory);
    }
}
