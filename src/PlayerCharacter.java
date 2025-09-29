import java.io.File;


public class PlayerCharacter {
    private final String name;
    private final int strength;
    private final int dexterity;
    private final int fortitude;
    private final GameItem[] inventory;

    /**
     * Creates a new character.
     */
    public static PlayerCharacter(String name, int strength, int dexterity,
                                  int fortitude, GameItem[] inventory) {
        this.name = name;
        this.strength = strength;
        this.dexterity = dexterity;
        this.fortitude = fortitude;
        this.inventory = inventory;
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
        return inventory;
    }

    /**
     * Returns the character's total strength, calculated as base Strength
     * plus the sum of AttackBonus from all items in their inventory.
     */
    public int computeTotalStrength() {
        int totalBonus = 0;
        for (GameItem item : inventory) {
            totalBonus = totalBonus + item.getDefenseBonus();
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
}



/**
 * Given a valid and non-empty character INI file and a complete array
 * of all possible GameItems that exist in the game (that is, you can
 * assume that every item in a character's inventory is present in
 * allItems), this method loads the characters from the file.
 */
public static PlayerCharacter[] readCharacters(File file, GameItem[] allItems) {
    List<PlayerCharacter> characters = new ArrayList<>();
}

void main() {
}
