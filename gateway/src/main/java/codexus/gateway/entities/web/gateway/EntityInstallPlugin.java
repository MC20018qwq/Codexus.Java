package codexus.gateway.entities.web.gateway;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Gateway/Codexus/Gateway/Entities/Web/Gateway/EntityInstallPlugin.cs. */
@JsonPropertyOrder({"id","dependencies","downloadUrl","fileHash","fileSize","name","version"})
public class EntityInstallPlugin {
    @JsonProperty("id")
    public String Id = "";

    @JsonProperty("dependencies")
    public java.util.List<codexus.gateway.entities.web.gateway.EntityInstallPlugin> Dependencies = new java.util.ArrayList<>();

    @JsonProperty("downloadUrl")
    public String DownloadUrl = "";

    @JsonProperty("fileHash")
    public String FileHash = "";

    @JsonProperty("fileSize")
    public int FileSize;

    @JsonProperty("name")
    public String Name = "";

    @JsonProperty("version")
    public String Version = "";


    public java.util.List<EntityInstallPlugin> getAllDownloadPlugins() {
        var result = new java.util.LinkedHashMap<String, EntityInstallPlugin>();
        collect(this, result, new java.util.HashSet<>(), 0);
        return java.util.List.copyOf(result.values());
    }
    private static void collect(EntityInstallPlugin plugin, java.util.Map<String, EntityInstallPlugin> result, java.util.Set<String> visiting, int depth) {
        if (plugin == null || plugin.Id == null || plugin.Id.isBlank()) throw new IllegalArgumentException("Invalid plugin dependency");
        if (depth > 128 || result.size() > 1024) throw new IllegalArgumentException("Plugin dependency graph is too large");
        String id = plugin.Id.toUpperCase(java.util.Locale.ROOT);
        if (visiting.contains(id)) throw new IllegalArgumentException("Cyclic plugin dependency: " + plugin.Id);
        var previous = result.get(id);
        if (previous != null) {
            if (!java.util.Objects.equals(previous.Version, plugin.Version) || !java.util.Objects.equals(previous.FileHash, plugin.FileHash))
                throw new IllegalArgumentException("Conflicting plugin dependency: " + plugin.Id);
            return;
        }
        result.put(id, plugin); visiting.add(id);
        if (plugin.Dependencies != null) for (var dependency : plugin.Dependencies) collect(dependency, result, visiting, depth + 1);
        visiting.remove(id);
    }
}
