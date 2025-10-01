import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.StringReader;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 *
 */
public final class IniLoader {
    /**
     * Reads the given INI file. For each section, it creates a stateful
     * Map of the key-value pairs within that section. It then calls the
     * elementFactory function, passing the section name (e.g.,
     * "Heavy Sword") as the first argument and the stateful Map of its
     * properties as the second.
     *
     * @param file The INI file to read.
     * @param makeArray A function to create the final array of the correct
     * type and size.
     * @param elementFactory A function that takes a section name (String)
     * and a Map<String, String> of properties and returns a new object of
     * type T.
     * @param <T> The type of object to create for each section.
     * @return An array of objects created from the INI file.
     */
    public static <T> T[] loadINI(
            File file,
            Function<Integer, T[]> makeArray,
            BiFunction<String, Map<String, String>, T> elementFactory
    ) {
        List<T> results = new ArrayList<>();
        String currentName = null;
        Map<String, String> properties = new HashMap<>();
        try(var reader = new BufferedReader(new FileReader(file))) {
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                 if (line.startsWith("[") && line.endsWith(("]"))) {
                     if (currentName != null) {
                         T element = elementFactory.apply(currentName, properties);
                         results.add(element);
                     }
                     currentName = line.substring(1, line.length()-1);
                     properties = new LinkedHashMap<>();
                 } else if (line.contains("=")) {
                     String[] parts = line.split("=", 2);
                     if (parts.length == 2 && currentName != null) {
                         String key = parts[0];
                         String value = parts[1];
                         properties.put(key, value);
                     }
                 }
            }
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
     * The same method as from Part 1, but now implemented using loadINI.
     */
    public static GameItem[] readItems(File file) {
        Function<Integer, GameItem[]> makeArray = n -> new GameItem[n];
        BiFunction<String, Map<String,String>, GameItem> elementFactory = (name, properties) -> {
            int weight = Integer.parseInt(properties.get("Weight"));
            int value = Integer.parseInt(properties.get("Value"));
            int attackBonus = Integer.parseInt(properties.get("AttackBonus"));
            int agilityBonus = Integer.parseInt(properties.get("AgilityBonus"));
            int defenseBonus = Integer.parseInt(properties.get("DefenseBonus"));
            return new GameItem(name, weight, value, attackBonus, agilityBonus, defenseBonus);
        };
        return IniLoader.loadINI(file, makeArray, elementFactory);
    }

    /**
     * The same method as from Part 2, but now implemented using loadINI.
     */
    public static PlayerCharacter[] readCharacters(File file, GameItem[] allItems) {
        Map<String, GameItem> byName = new HashMap<>();
        for (GameItem item : allItems) {
            byName.put(item.getName(), item);
        }
        Function<Integer, PlayerCharacter[]> makeArray = n -> new PlayerCharacter[n];
        BiFunction<String, Map<String, String>, PlayerCharacter> elementFactory = (name, properties) -> {
            int strength = Integer.parseInt(properties.get("Strength"));
            int dexterity = Integer.parseInt(properties.get("Dexterity"));
            int fortitude = Integer.parseInt(properties.get("Fortitude"));
            String inventory = properties.get("Inventory");
            List<GameItem> bag = new ArrayList<>();
            for (String itemName : inventory.split(",")) {
                GameItem item = byName.get(itemName);
                bag.add(item);
            }
            return new PlayerCharacter(name, strength, dexterity, fortitude,bag.toArray(new GameItem[0]));
        };
        return IniLoader.loadINI(file, makeArray, elementFactory);
    }
}


