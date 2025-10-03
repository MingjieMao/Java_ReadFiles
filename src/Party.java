import java.io.*;
import java.util.*;

/**
 * Party represents an adventuring party: an ordered roster of PlayerCharacters
 * that can be loaded from simple INI files and queried for combined power.
 *
 * Examples:
 * - Given file "tests/Parties/fellowship.ini":
 *     [Party]
 *     Name=fellowship
 *
 *     [Members]
 *     0=Gimli
 *     1=Legolas
 *     2=Aragorn
 *   Expect:
 *     Party p = fellowship.ini // loaded by Party.loadParties(...)
 *     p.getName() -> "fellowship"
 *     names(p.getMembers()) -> ["Gimli","Legolas","Aragorn"] (in order)
 *     p.computeCombinedAttackRating() == sum(pc.computeTotalStrength() for pc in members)
 *
 * Template:
 * { this.name, this.members }
 *
 * @implSpec Invariants:
 *   1. {@code name} is non-null and set at construction time.
 *   2. {@code members} preserves the insertion order; {@link #getMembers()} returns a defensive copy.
 *   3. INI parsing follows the course-required pattern (BufferedReader + for(line!=null) + RuntimeException).
 *   4. No {@code continue} statements are used in parsing.
 *   5. When loading, member names are resolved via an exact String key in a provided map.
 */
public class Party {
    private final String name;
    private final List<PlayerCharacter> members;

    /**
     * Creates an empty party with the given name.
     *
     * Examples:
     * - new Party("fellowship")
     * - new Party("A")
     * - new Party("B")
     *
     * Design Strategy: Simple Expression (field assignment).
     *
     * @param name party name (e.g., "fellowship").
     * @implSpec Postcondition: name = null, members = null
     */
    public Party(String name) {
        this.name = name;
        this.members = new ArrayList<>();
    }

    /**
     * Returns the name of the party (e.g., from the filename).
     *
     * Examples:
     * - new Party("fellowship").getName() -> "fellowship"
     * - new Party("A").getName() -> "A"
     *
     * Design Strategy: Simple Expression.
     *
     * @return party name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the characters who are members of this party.
     *
     * Examples:
     *  - Given: Party p = new Party("team");
     *           PlayerCharacter g = new PlayerCharacter("Gimli", 18, 12, 30, new GameItem[0]);
     *           PlayerCharacter l = new PlayerCharacter("Legolas", 14, 13, 25, new GameItem[0]);
     *           p.addMember(g);
     *           p.addMember(l);
     *    Expect: PlayerCharacter[] a1 = p.getMembers();
     *            a1.length == 2
     *            a1[0].getName().equals("Gimli")   // a1[0]: Gimli
     *           a1[1].getName().equals("Legolas") // a1[1]: Legolas
     *
     *            // Changing the returned array does NOT affect the party (defensive copy)
     *            a1[0] = l;
     *            PlayerCharacter[] a2 = p.getMembers();
     *            a2.length == 2
     *            a2[0].getName().equals("Gimli")   // still Gimli — proves the internal roster wasn’t affected by modifying a1
     *            a2[1].getName().equals("Legolas")
     *
     * Design Strategy: Simple Expression (List -> array copy).
     *
     * @return snapshot array of members
     * @implSpec Postcondition: returns a new array (defensive copy);
     */
    public PlayerCharacter[] getMembers() {
        return members.toArray(new PlayerCharacter[0]);
    }

    /**
     * Returns the combined attack rating of the party, calculated as the
     * sum of every member's computeTotalStrength().
     *
     * Examples:
     * - Given: members = [Gimli(20 total), Legolas(15 total)]
     *   Expect: 35
     *
     * Design Strategy: Iteration
     *
     * @return combined attack rating
     * @implSpec Postcondition: result == sum(pc.computeTotalStrength() for pc in this.members).
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
     *
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
     *
     * Effects: adds element to end of list; increases size by 1.
     *
     * Design Strategy: Case Distinction
     *
     * @param character player to add.
     * @implSpec Postcondition:
     *   - If character was not already present: size increases by exactly 1 and character becomes the last element.
     *   - If character was already present: size and order remain unchanged.
     */
    public void addMember(PlayerCharacter character) {
        if (!members.contains(character)) {
            members.add(character);
        }
    }

    /**
     * Removes a character from the party.
     *
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
     *
     * Effects: removes one matching element; decreases size by 1.
     *
     * Design Strategy: Case Distinction
     *
     * @param character player to remove
     * @implSpec Postcondition:
     *  - If character was present: size decreases by exactly 1 and one occurrence is removed; relative order of remaining elements is preserved.
     *  - If character was not present: size and order remain unchanged.
     */
    public void removeMember(PlayerCharacter character) {
        members.remove(character);
    }

    /**
     * Saves the current state of the party to an appropriately named file
     * ("[name].ini") in the given directory.
     *
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
     *
     *              [Members]
     *              0=Gimli
     *              1=Legolas
     *
     * Design Strategy: Iteration
     *
     * @param directory target directory
     *
     */
    public void storeParty(File directory) {
        File file = new File(directory, name + ".ini");
        StringBuilder sb = new StringBuilder();
        for (int i=0; i < members.size(); i++) {
            PlayerCharacter pc = members.get(i);
            if (pc != null) {
                sb.append(pc.getName());
                if (i < members.size() - 1) {
                    sb.append(",");
                }
            }
        }
        String memberString = sb.toString();
    }

    /**
     * Given a directory containing party files and a list of all available
     * characters in the game, loads all parties from the INI files in the
     * given directory. Assume that all INI files in the directory are
     * non-empty and valid INI Party files. Moreover, assume that all
     * characters in the Party files are contained in allCharacters.
     *
     * Effects: opens files for reading; may throw a RuntimeException on failure.
     *
     * Design Strategy: Iteration
     *
     * @param allCharacters An array of all PlayerCharacters in the game.
     * @param directory The File object representing the directory containing the party INI files.
     * @return An array of Party objects loaded from the directory.
     * @throws RuntimeException wraps any exception as per course policy
     * @implSpec Precondition: directory is non-null; every *.ini in directory is a valid non-empty Party INI;
     *  - allCharacters contains every character name referenced in those INIs.
     *  - Postcondition: returns a non-null array; for each loaded party, member order matches numeric keys in its INI.
     */
    public static Party[] loadParties(PlayerCharacter[] allCharacters, File directory){
        // 1. Build lookup
        HashMap<String, PlayerCharacter> byName = new HashMap<>();
        for (PlayerCharacter pc : allCharacters) {
            if (pc != null && pc.getName() != null) byName.put(pc.getName(), pc);
        }
        // 2. Enumerate .ini files
        File[] files = directory.listFiles((dir, name) -> name.toLowerCase().endsWith(".ini"));
        // 3. Parse each into a Party
        List<Party> out = new ArrayList<>();
        for (File f : files) {
            Party p = loadOnePartyFromIniFile(f, byName);
            if (p != null) out.add(p);
        }
        return out.toArray(new Party[0]);
    }

    /**
     * Loads exactly one Party from a single INI file.
     *
     * Design Strategy: Iteration
     *
     * Effects: opens the file, reads it fully, constructs a Party instance.
     *
     * @param ini    INI file for one party
     * @param byName name -> PlayerCharacter lookup
     * @return Party built from the file contents
     * @throws RuntimeException wraps any exception as per course policy
     * @implSpec Precondition: ini is a valid, non-empty Party INI file; byName maps every referenced member name.
     *           Postcondition: constructs a Party whose name equals the [Party].
     *           Name and whose members follow sorted numeric keys (0,1,2,...).
     */
    private static Party loadOnePartyFromIniFile(File ini, HashMap<String, PlayerCharacter> byName) {

        PartyDataHolder holder = new PartyDataHolder();

        try (BufferedReader reader = new BufferedReader(new FileReader(ini))) {
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                parsePartyLine(line, holder);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        String partyNameFromFile = holder.partyNameFromFile;
        Map<Integer, String> memberMap = holder.memberMap;

        Party p = new Party(partyNameFromFile);

        // Keep file-declared order by sorting numeric keys.
        List<Integer> keys = new ArrayList<>(memberMap.keySet());
        Collections.sort(keys);  // numeric ascending
        for (Integer idx : keys) {
            String name = memberMap.get(idx);
            PlayerCharacter pc = byName.get(name);
            if (pc != null) {
                p.members.add(pc);
            }
        }
        return p;
    }

    /**
     * Parses a single line of a Party INI file and updates parsing state.
     *
     * Examples (state transitions):
     * - Line "[Party]"    : holder.currentSection -> "Party"
     * - Line "Name=foo"   : if currentSection == "Party", set holder.partyNameFromFile = "foo"
     * - Line "[Members]"  : holder.currentSection -> "Members"
     * - Line "0=Gimli"    : if currentSection == "Members", memberMap.put(0,"Gimli")
     *
     * Design Strategy: Case distinction
     *
     * @param line   raw line (already read from file)
     * @param holder mutable parsing state to update
     * @throws NumberFormatException if member index is not a valid integer
     * @implSpec Precondition: line != null and holder != null.
     * Postcondition: updates holder.currentSection on section headers; updates holder.partyNameFromFile or holder.
     * memberMap on key=value lines according to currentSection.
     */
    private static void parsePartyLine(String line, PartyDataHolder holder) {
        String currentSection = holder.currentSection;
        // Section header like [Party] or [Members]
        if (line.startsWith("[") && (line.endsWith("]"))) {
            holder.currentSection = (line.substring(1, (line.length() - 1)));
        } else {
            int eq = line.indexOf('=');
            if (eq >= 0) {
                String key = line.substring(0, eq);
                String val = line.substring(eq + 1);
                if ("Party".equalsIgnoreCase(currentSection)) {
                    if ("Name".equalsIgnoreCase(key)) {
                        holder.partyNameFromFile = val;
                    }
                } else if ("Members".equalsIgnoreCase(currentSection)) {
                    // assume key is number（0,1,2...）
                    int idx = Integer.parseInt(key);
                    holder.memberMap.put(idx, val);
                }
            }
        }
    }

    /**
     * Holds intermediate parsing state while reading a single Party INI file.
     * Not exposed outside the loader; only used to accumulate data line-by-line.
     *
     * Examples:
     * - After reading "[Party]":
     *     holder.currentSection == "Party"
     *     holder.partyNameFromFile == null
     *     holder.memberMap.isEmpty() == true
     *
     * - After reading "Name=fellowship" (and currentSection == "Party"):
     *     holder.partyNameFromFile == "fellowship"
     *
     * - After reading "[Members]":
     *     holder.currentSection == "Members"
     *
     * - After reading "0=Gimli", "1=Legolas", "2=Aragorn" (and currentSection == "Members"):
     *     holder.memberMap.get(0).equals("Gimli")
     *     holder.memberMap.get(1).equals("Legolas")
     *     holder.memberMap.get(2).equals("Aragorn")
     *
     * Design Strategy: Simple Expression (field assignment).
     *
     * @implSpec Invariants:
     * - currentSection is "", "Party", or "Members";
     * - if currentSection == "Party" and line is "Name=X", then partyNameFromFile == X;
     * - if currentSection == "Members" and line is "i=Name", then memberMap.get(i).equals(Name).
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
