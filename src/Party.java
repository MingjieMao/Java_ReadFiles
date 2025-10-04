import java.io.*;
import java.util.*;

/**
 * Party represents an adventuring party: an ordered roster of PlayerCharacters
 * that can be loaded from simple INI files and queried for combined power.
 * Examples:
 * - Given file "tests/Parties/fellowship.ini":
 *     [Party]
 *     Name=fellowship
 *     [Members]
 *     0=Gimli
 *     1=Legolas
 *     2=Aragorn
 *   Expect:
 *     Party p = fellowship.ini // loaded by Party.loadParties(...)
 *     p.getName() -> "fellowship"
 *     names(p.getMembers()) -> ["Gimli","Legolas","Aragorn"] (in order)
 *     p.computeCombinedAttackRating() == sum(pc.computeTotalStrength() for pc in members)
 * Template:
 * { this.name, this.members }
 *
 * @implSpec
 * Invariants:
 * - {@code name} is non-null and non-empty.
 * - {@code members} is non-null, preserves insertion order, and contains no null elements.
 * Pre-conditions:
 * - When constructing a Party, caller must provide a valid {@code name} and a non-null {@code List<PlayerCharacter>}.
 * Post-conditions:
 * - A Party instance is immutable after construction (fields are {@code final}).
 * - Queries such as {@code getName()}, {@code getMembers()},
 *   and combined-stat methods always return consistent values derived from construction arguments.
 */
public class Party {
    private final String name;
    private final List<PlayerCharacter> members;

    /**
     * Creates an empty party with the given name.
     * Examples:
     * - new Party("fellowship")
     * - new Party("A")
     * - new Party("B")
     * Design Strategy: Simple Expression (field assignment).
     *
     * @param name party name (e.g., "fellowship").
     * @implSpec Post-condition: name = null, members = null
     */
    public Party(String name) {
        this.name = name;
        this.members = new ArrayList<>();
    }

    /**
     * Returns the name of the party (e.g., from the filename).
     * Examples:
     * - new Party("fellowship").getName() -> "fellowship"
     * - new Party("A").getName() -> "A"
     * Design Strategy: Simple Expression.
     *
     * @return party name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the characters who are members of this party.
     * Examples:
     *  - Given: Party p = new Party("team");
     *           PlayerCharacter g = new PlayerCharacter("Gimli", 18, 12, 30, new GameItem[0]);
     *           PlayerCharacter l = new PlayerCharacter("Legolas", 14, 13, 25, new GameItem[0]);
     *           p.addMember(g);
     *           p.addMember(l);
     *    Expect: PlayerCharacter[] a1 = p.getMembers();
     *            a1.length == 2
     *            a1[0].getName().equals("Gimli")   // a1[0]: Gimli
     *            a1[1].getName().equals("Legolas") // a1[1]: Legolas
     *            // Changing the returned array does NOT affect the party (defensive copy)
     *            a1[0] = l;
     *            PlayerCharacter[] a2 = p.getMembers();
     *            a2.length == 2
     *            a2[0].getName().equals("Gimli")   // still Gimli — proves the internal roster wasn’t affected by modifying a1
     *            a2[1].getName().equals("Legolas")
     * Design Strategy: Simple Expression (List -> array copy).
     *
     * @return snapshot array of members
     * @implSpec Post-condition: returns a new array (defensive copy);
     */
    public PlayerCharacter[] getMembers() {
        return members.toArray(new PlayerCharacter[0]);
    }

    /**
     * Returns the combined attack rating of the party, calculated as the
     * sum of every member's computeTotalStrength().
     * Examples:
     * - Given: members = [Gimli(20 total), Legolas(15 total)]
     *   Expect: 35
     * Design Strategy: Iteration
     *
     * @return combined attack rating of all members
     * @implSpec
     * Invariants: {@code members} is never null, and contains only non-null PlayerCharacters.
     * Pre-conditions: Party must have been constructed with a valid members list.
     * Post-conditions:
     *  - Result equals the sum of {@code computeTotalStrength()} of all members.
     *  - If {@code members} is empty, result == 0.
     */
    public int computeCombinedAttackRating() {
        int sum = 0;
        for (PlayerCharacter player : members) {
            sum = sum + player.computeTotalStrength();
        }
        return sum;
    }

    /**
     * Adds a character to the party.
     * Examples:
     *  - Given: GameItem axe   = new GameItem("Battle Axe", 3, 0, 0);
     *           GameItem bow   = new GameItem("Longbow", 2, 1, 0);
     *           PlayerCharacter gimli = new PlayerCharacter("Gimli",   18, 12, 30, new GameItem[]{axe});
     *           PlayerCharacter legolas = new PlayerCharacter("Legolas",14, 13, 25, new GameItem[]{bow});
     *           Party p = new Party("team");
     *           p.addMember(gimli);
     *           p.addMember(legolas);
     *    Expect: p.getMembers().length == 2
     *            p.getMembers()[0].getName().equals("Gimli")
     *            p.getMembers()[1].getName().equals("Legolas")
     * Design Strategy: Case Distinction
     * Effects:
     * - Adds the new character to the end of the list if not already contained.
     * - Increases size by 1 if character was absent, otherwise no change.
     *
     * @param character player to add.
     * @implSpec
     * Invariants: {@code members} is never null and contains no duplicates.
     * Pre-conditions: {@code character} must not be null.
     * Post-condition:
     * - If {@code character} was not already in {@code members}, it is appended at the end.
     * - If {@code character} was already present, list remains unchanged.
     * - Size of {@code members} increases by at most 1.
     */
    public void addMember(PlayerCharacter character) {
        if (!members.contains(character)) {
            members.add(character);
        }
    }

    /**
     * Removes a character from the party.
     * Examples:
     *  - Given:
     *      GameItem axe   = new GameItem("Battle Axe", 3, 0, 0);
     *      GameItem bow   = new GameItem("Longbow", 2, 1, 0);
     *      PlayerCharacter gimli   = new PlayerCharacter("Gimli",   18, 12, 30, new GameItem[]{axe});
     *      PlayerCharacter legolas = new PlayerCharacter("Legolas", 14, 13, 25, new GameItem[]{bow});
     *      Party p = new Party("team");
     *      p.addMember(gimli);            // roster = [Gimli]
     *      p.addMember(legolas);          // roster = [Gimli, Legolas]
     *      p.removeMember(gimli);         // roster = [Legolas]
     *      p.removeMember(gimli);         // no-op; Gimli already removed
     *    Expect:
     *      p.getMembers().length == 1
     *      p.getMembers()[0].getName().equals("Legolas")
     * Effects: removes one matching element; decreases size by 1.
     * Design Strategy: Case Distinction
     *
     * @param character player to remove
     * @implSpec
     * Invariants: {@code members} is never null; contains no duplicate references.
     * Pre-conditions: None beyond non-null {@code members}.
     * Post-condition:
     *  - If {@code character} was present, it is removed once, and size decreases by 1.
     *  - If {@code character} was not present, no change occurs.
     *  - {@code members} remains consistent (no nulls introduced).
     */
    public void removeMember(PlayerCharacter character) {
        members.remove(character);
    }

    /**
     * Saves the current state of the party to an appropriately named file
     * ("[name].ini") in the given directory.
     * Examples:
     *  - Given: Party p = new Party("fellowship");
     *           GameItem axe = new GameItem("Battle Axe", 3, 0, 0);
     *           GameItem bow = new GameItem("Longbow", 2, 1, 0);
     *           PlayerCharacter gimli = new PlayerCharacter("Gimli", 18, 12, 30, new GameItem[]{axe});
     *           PlayerCharacter legolas = new PlayerCharacter("Legolas", 14, 13, 25, new GameItem[]{bow});
     *           p.addMember(gimli);
     *           p.addMember(legolas);
     *    Expect: File path: <directory>/fellowship.ini
     *            Content:
     *              [Party]
     *              Name=fellowship
     *              [Members]
     *              0=Gimli
     *              1=Legolas
     * Design Strategy: Iteration
     * Effects:
     * - Side effect: creates or overwrites a file in the given directory.
     * - Performs I/O, may throw exceptions if writing fails.
     *
     * @param directory target directory.
     * @throws RuntimeException if an I/O error occurs during file writing.
     * @implSpec
     * Invariants:
     * - {@code name} is non-null and non-empty.
     * - {@code members} is non-null, contains no nulls.
     * Pre-conditions:
     * - {@code directory} must be a valid writable directory, not null.
     * Post-conditions:
     * - Creates/overwrites file named {@code name + ".ini"} inside {@code directory}.
     * - File content reflects current party state (name and ordered members).
     */
    public void storeParty(File directory) {
        File file = new File(directory, name + ".ini");

        try (var writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("[Party]");
            writer.newLine();
            writer.write("Name=" + name);
            writer.newLine();

            writer.write("[Members]");
            writer.newLine();
            for (int i = 0; i < members.size(); i++) {
                PlayerCharacter pc = members.get(i);
                if (pc != null) {
                    writer.write(i + "=" + pc.getName());
                    writer.newLine();
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to store party " + name, e);
        }
    }

    /**
     * Given a directory containing party files and a list of all available
     * characters in the game, loads all parties from the INI files in the
     * given directory. Assume that all INI files in the directory are
     * non-empty and valid INI Party files. Moreover, assume that all
     * characters in the Party files are contained in allCharacters.
     * Examples:
     * - Given: Directory "tests/Parties" contains:
     *          fellowship.ini:
     *          [Party]
     *          Name=fellowship
     *          [Members]
     *          0=Gimli
     *          1=Legolas
     *          2=Aragorn
     *          hunters.ini:
     *          [Party]
     *          Name=hunters
     *          [Members]
     *          0=Aragorn
     *          1=Legolas
     *          allCharacters = [Aragorn, Legolas, Gimli, Frodo, Sam]
     *   Expect: loadParties(allCharacters, dir) returns Party[] length 2:
     *           [0]: name="fellowship", members=["Gimli","Legolas","Aragorn"]
     *           [1]: name="hunters",    members=["Aragorn","Legolas"]
     * Design Strategy: Iteration
     * Effects:
     *  - Opens files for reading.
     *  - Allocates new Party objects.
     *  - May throw {@code RuntimeException} if directory reading/parsing fails.
     *
     * @param allCharacters An array of all PlayerCharacters in the game, must include every character referenced in INI files.
     * @param directory The File object representing the directory containing the party INI files.
     * @return non-null array of Party objects loaded from the directory.
     * @throws RuntimeException wraps any exception as per course policy
     * @implSpec
     * Invariants:
     *  - Returned array is non-null and contains no null Party.
     *  - Each Party has a non-null name and non-null members list.
     * Pre-conditions:
     * - {@code directory} is non-null.
     * - Every {@code *.ini} file in {@code directory} is non-empty, valid, and refers only to known characters.
     * Post-conditions:
     * - Returns a Party[] whose length equals the number of *.ini files.
     * - Each Party preserves the order of members according to numeric keys in its INI.
     */
    public static Party[] loadParties(PlayerCharacter[] allCharacters, File directory){
        // 1. Build lookup from character name to PlayerCharacter
        HashMap<String, PlayerCharacter> byName = new HashMap<>();
        for (PlayerCharacter pc : allCharacters) {
            if (pc != null && pc.getName() != null) {
                byName.put(pc.getName(), pc);
            }
        }

        // 2. Enumerate .ini files in the directory
        File[] files = directory.listFiles((_, name) -> name.toLowerCase().endsWith(".ini"));
        if (files == null) {
            return new Party[0];
        }

        // 3. Parse each file into a Party and collect
        List<Party> out = new ArrayList<>();
        for (File f : files) {
            Party p = loadOnePartyFromIniFile(f, byName);
            out.add(p);
        }

        // Convert list to array
        return out.toArray(new Party[0]);
    }

    /**
     * Loads exactly one Party from a single INI file.
     * Examples:
     * - Given file fellowship.ini:
     *     [Party]
     *     Name=fellowship
     *     [Members]
     *     0=Gimli
     *     1=Legolas
     *     2=Aragorn
     *   Expect: Party with name="fellowship" and members ["Gimli","Legolas","Aragorn"] in order.
     * Design Strategy: Iteration
     * Effects:
     * - Opens and reads the given INI file.
     * - Allocates and returns a new Party object.
     * - May throw {@code RuntimeException} if file reading/parsing fails.
     *
     * @param ini    INI file for one party
     * @param byName lookup map from character name -> PlayerCharacter
     * @return Party built from the file contents
     * @throws RuntimeException wraps any exception as per course policy
     * @implSpec
     * Invariants:
     * - Returned Party is non-null, with non-null name and non-null member list.
     * Pre-conditions:
     * - {@code ini} is a valid, non-empty Party INI file.
     * - {@code byName} maps every referenced member name to a PlayerCharacter.
     * Post-conditions:
     * - Constructs and returns a Party whose name equals the [Party] Name.
     * - Members are inserted in ascending numeric key order (0,1,2,…).
     */
    private static Party loadOnePartyFromIniFile(File ini, HashMap<String, PlayerCharacter> byName) {
        // Temporary holder for parsed values
        PartyDataHolder holder = new PartyDataHolder();

        // Read file line by line and parse each line
        try (BufferedReader reader = new BufferedReader(new FileReader(ini))) {
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                parsePartyLine(line, holder);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Extract parsed data
        String partyNameFromFile = holder.partyNameFromFile;
        Map<Integer, String> memberMap = holder.memberMap;

        // Construct Party with parsed name
        Party p = new Party(partyNameFromFile);

        // Preserve declared order by sorting numeric keys
        List<Integer> keys = new ArrayList<>(memberMap.keySet());
        Collections.sort(keys);
        for (Integer idx : keys) {
            String name = memberMap.get(idx);
            PlayerCharacter pc = byName.get(name);
            if (pc != null) {
                p.members.add(pc);  // Add mapped PlayerCharacter to Party
            }
        }
        // Return fully constructed Party
        return p;
    }

    /**
     * Parses a single line of a Party INI file and updates parsing state.
     * Examples (state transitions):
     * - Line "[Party]"    : holder.currentSection -> "Party"
     * - Line "Name=foo"   : if currentSection == "Party", set holder.partyNameFromFile = "foo"
     * - Line "[Members]"  : holder.currentSection -> "Members"
     * - Line "0=Gimli"    : if currentSection == "Members", memberMap.put(0,"Gimli")
     * Design Strategy: Case distinction
     * Effects:
     * - Mutates {@code holder} by changing currentSection, partyNameFromFile, or memberMap.
     *
     * @param line   raw line (already read from file)
     * @param holder mutable parsing state to update
     * @throws NumberFormatException if member index is not a valid integer
     * @implSpec
     * Invariants:
     * - {@code holder} remains non-null after method execution.
     * Precondition:
     * - {@code line != null}, {@code holder != null}.
     * Post-conditions:
     * - If {@code line} is a section header, updates {@code holder.currentSection}.
     * - If {@code line} is "Name=..." in [Party], sets {@code holder.partyNameFromFile}.
     * - If {@code line} is "i=name" in [Members], adds entry to {@code holder.memberMap}.
     */
    private static void parsePartyLine(String line, PartyDataHolder holder) {
        String currentSection = holder.currentSection;

        // Case 1: Section header like [Party] or [Members]
        if (line.startsWith("[") && (line.endsWith("]"))) {
            holder.currentSection = (line.substring(1, (line.length() - 1)));
        } else {
            // Case 2: key=value line
            int eq = line.indexOf('=');
            if (eq >= 0) {
                String key = line.substring(0, eq);
                String val = line.substring(eq + 1);
                // If in [Party] section
                if ("Party".equalsIgnoreCase(currentSection)) {
                    if ("Name".equalsIgnoreCase(key)) {
                        holder.partyNameFromFile = val;  // set party name
                    }
                // If in [Members] section
                } else if ("Members".equalsIgnoreCase(currentSection)) {
                    int idx = Integer.parseInt(key);  // Assume key is numeric index (0,1,2,…)
                    holder.memberMap.put(idx, val);   // add mapping index -> name
                }
            }
        }
    }

    /**
     * Holds intermediate parsing state while reading a single Party INI file.
     * Not exposed outside the loader; only used to accumulate data line-by-line.
     * Examples:
     * - After reading "[Party]":
     *     holder.currentSection == "Party"
     *     holder.partyNameFromFile == null
     *     holder.memberMap.isEmpty() == true
     * - After reading "Name=fellowship" (and currentSection == "Party"):
     *     holder.partyNameFromFile == "fellowship"
     * - After reading "[Members]":
     *     holder.currentSection == "Members"
     * - After reading "0=Gimli", "1=Legolas", "2=Aragorn" (and currentSection == "Members"):
     *     holder.memberMap.get(0).equals("Gimli")
     *     holder.memberMap.get(1).equals("Legolas")
     *     holder.memberMap.get(2).equals("Aragorn")
     * Design Strategy: Simple Expression (field assignment).
     *
     * @implSpec
     * Invariants:
     * - {@code currentSection} is always one of "", "Party", or "Members".
     * - If {@code currentSection == "Party"} and line is "Name=X", then {@code partyNameFromFile == X}.
     * - If {@code currentSection == "Members"} and line is "i=Name", then {@code memberMap.get(i).equals(Name)}.
     * Pre-conditions:
     * - Used only during Party INI parsing; not exposed outside loader.
     * Post-conditions:
     * - After parsing completes, {@code partyNameFromFile} holds the Party name,
     *   and {@code memberMap} holds the index-to-name mapping of all members.
     */
    private static class PartyDataHolder {
        // Party name read from the [Party] section; may remain null until discovered.
        String partyNameFromFile = null;

        // Maps member index (0,1,2,...) to the member's character name from the [Members] section.
        final Map<Integer, String> memberMap = new HashMap<>();

        // Tracks the name of the current section header being parsed
        String currentSection = "";
    }
}
