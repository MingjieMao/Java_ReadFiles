import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;


public final class PlayerCharacter {
    private final String name;
    private final int strength;
    private final int dexterity;
    private final int fortitude;
    private final GameItem[] inventory;

    /**
     * Creates a new character.
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
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the character's base Strength stat (without item bonuses).
     */
    public int getStrength() {
        return strength;
    }

    /**
     * Returns the character's base Dexterity stat (without item bonuses).
     */
    public int getDexterity() {
        return dexterity;
    }

    /**
     * Returns the character's base Fortitude stat (without item bonuses).
     */
    public int getFortitude() {
        return fortitude;
    }

    /**
     * Returns an array of GameItem objects in the character's inventory.
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

        String name = null;
        int strength = 0, dexterity = 0, fortitude = 0;
        String inventoryString = null;

        try(var reader = new BufferedReader(new FileReader(file))) {
            for(String line = reader.readLine(); line != null; line = reader.readLine()) {
                if (line.startsWith("[") && line.endsWith("]")) {
                    characters.add(createCharacterFromProps(name, strength, dexterity, fortitude, inventoryString, itemLookup));
                    name = line.substring(1, line.length() - 1);
                    strength = dexterity = fortitude = 0;
                    inventoryString = null;
                } else if (line.contains("=")) {
                    String[] parts = line.split("=", 2);
                    String key = parts[0];
                    String value = parts[1];

                    switch (key) {
                        case "Strength" -> strength = Integer.parseInt(value);
                        case "Dexterity" -> dexterity = Integer.parseInt(value);
                        case "Fortitude" -> fortitude = Integer.parseInt(value);
                        case "Inventory" -> inventoryString = value;
                        default -> {}
                    }
                }
            }
            if (name != null) {
                characters.add(createCharacterFromProps(name, strength, dexterity, fortitude, inventoryString, itemLookup));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return characters.toArray(new PlayerCharacter[0]);
    }

}
