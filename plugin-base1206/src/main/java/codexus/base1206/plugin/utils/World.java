package codexus.base1206.plugin.utils;

import java.util.ArrayList;
import java.util.List;
public class World {
    private final List<Entity> entities = new ArrayList<>();
    public synchronized void addEntity(Entity entity) { entities.add(entity); }
    public synchronized void removeEntity(Entity entity) { entities.remove(entity); }
    public synchronized void removeEntity(int entityId) { entities.removeIf(entity -> entity.EntityId == entityId); }
    public synchronized Entity getEntity(int entityId) { return entities.stream().filter(entity -> entity.EntityId == entityId).findFirst().orElse(null); }
}
