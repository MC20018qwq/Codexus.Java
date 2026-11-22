package codexus.base1206.plugin.utils.patch;

import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
public final class Teams {
    private final Map<String, Set<String>> teamMembers = new LinkedHashMap<>();
    public synchronized void createTeam(String teamName) { teamMembers.putIfAbsent(teamName, new HashSet<>()); }
    public synchronized void removeTeam(String teamName) { teamMembers.remove(teamName); }
    public synchronized boolean containsTeam(String teamName) { return teamMembers.containsKey(teamName); }
    public synchronized boolean addPlayerToTeam(String teamName, String playerName) { var members = teamMembers.get(teamName); return members != null && members.add(playerName); }
    public synchronized boolean removePlayerFromTeam(String teamName, String playerName) { var members = teamMembers.get(teamName); return members != null && members.remove(playerName); }
    public synchronized String findPlayerTeam(String playerName) { for(var entry : teamMembers.entrySet()) if(entry.getValue().contains(playerName)) return entry.getKey(); return null; }
    public synchronized void clear() { teamMembers.clear(); }
}
