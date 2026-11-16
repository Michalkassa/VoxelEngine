package Entity;

import World.ChunkManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Holds every entity in the world and updates, renders and cleans them up together.
// Entities can be added or removed while it is updating; the change takes effect once the update finishes
public class EntityManager {

    private final Map<Integer, Entity> entities = new LinkedHashMap<>();
    private final List<Entity> pendingAdds = new ArrayList<>();
    private boolean updating = false;

    public <T extends Entity> T add(T entity) {
        if (updating) {
            pendingAdds.add(entity);
        } else {
            entities.putIfAbsent(entity.getId(), entity);
        }
        return entity;
    }

    public void remove(Entity entity) {
        entity.remove();
        if (!updating) removeMarkedEntities();
    }

    public Entity get(int id) {
        return entities.get(id);
    }

    public Collection<Entity> getEntities() {
        return Collections.unmodifiableCollection(entities.values());
    }

    public <T extends Entity> List<T> getEntities(Class<T> type) {
        List<T> matching = new ArrayList<>();
        for (Entity entity : entities.values()) {
            if (type.isInstance(entity)) matching.add(type.cast(entity));
        }
        return matching;
    }

    public int size() {
        return entities.size();
    }

    public void update(float dt, ChunkManager chunkManager) {
        updating = true;
        try {
            for (Entity entity : entities.values()) {
                if (!entity.isRemoved()) entity.update(dt, chunkManager);
            }
        } finally {
            updating = false;
        }
        removeMarkedEntities();
        for (Entity entity : pendingAdds) {
            if (!entity.isRemoved()) entities.putIfAbsent(entity.getId(), entity);
        }
        pendingAdds.clear();
    }

    public void render() {
        for (Entity entity : entities.values()) {
            entity.render();
        }
    }

    public void cleanup() {
        for (Entity entity : entities.values()) {
            entity.cleanup();
        }
        entities.clear();
        pendingAdds.clear();
    }

    private void removeMarkedEntities() {
        entities.values().removeIf(entity -> {
            if (!entity.isRemoved()) return false;
            entity.cleanup();
            return true;
        });
    }
}
