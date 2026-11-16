package Entity;

import Core.Transform;
import World.ChunkManager;
import org.joml.Vector3f;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

class EntityManagerTest {

    private static final float DT = 1f / 60f;

    // Records what the manager does to it instead of simulating physics
    private static class TestEntity extends Entity {
        int updates = 0;
        int renders = 0;
        int cleanups = 0;
        Consumer<TestEntity> onUpdate = entity -> {};

        TestEntity() {
            super(new Transform(new Vector3f(), new Vector3f(1, 1, 1)));
        }

        @Override
        public void update(float dt, ChunkManager chunkManager) {
            updates++;
            onUpdate.accept(this);
        }

        @Override
        public void render() {
            renders++;
        }

        @Override
        public void cleanup() {
            cleanups++;
        }
    }

    private static class OtherEntity extends TestEntity {
    }

    private EntityManager manager;

    @BeforeEach
    void setUp() {
        manager = new EntityManager();
    }

    @Test
    void everyEntityGetsADifferentId() {
        TestEntity a = new TestEntity();
        TestEntity b = new TestEntity();
        TestEntity c = new TestEntity();

        assertNotEquals(a.getId(), b.getId());
        assertNotEquals(b.getId(), c.getId());
        assertNotEquals(a.getId(), c.getId());
    }

    @Test
    void addedEntitiesCanBeFoundById() {
        TestEntity a = manager.add(new TestEntity());
        TestEntity b = manager.add(new TestEntity());

        assertSame(a, manager.get(a.getId()));
        assertSame(b, manager.get(b.getId()));
        assertEquals(2, manager.size());
        assertNull(manager.get(-1));
    }

    @Test
    void addingTheSameEntityTwiceKeepsOneCopy() {
        TestEntity a = new TestEntity();

        manager.add(a);
        manager.add(a);
        manager.update(DT, null);

        assertEquals(1, manager.size());
        assertEquals(1, a.updates);
    }

    @Test
    void entityListCannotBeChangedFromOutside() {
        manager.add(new TestEntity());

        assertThrows(UnsupportedOperationException.class, () -> manager.getEntities().clear());
    }

    @Test
    void entitiesCanBeListedByType() {
        TestEntity a = manager.add(new TestEntity());
        OtherEntity b = manager.add(new OtherEntity());

        assertEquals(List.of(b), manager.getEntities(OtherEntity.class));
        assertEquals(List.of(a, b), manager.getEntities(TestEntity.class));
    }

    @Test
    void updateUpdatesEveryEntityOnce() {
        TestEntity a = manager.add(new TestEntity());
        TestEntity b = manager.add(new TestEntity());

        manager.update(DT, null);
        manager.update(DT, null);

        assertEquals(2, a.updates);
        assertEquals(2, b.updates);
    }

    @Test
    void entitiesAreUpdatedInTheOrderTheyWereAdded() {
        List<Integer> order = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            TestEntity entity = manager.add(new TestEntity());
            entity.onUpdate = e -> order.add(e.getId());
        }

        manager.update(DT, null);

        List<Integer> expected = manager.getEntities().stream().map(Entity::getId).toList();
        assertEquals(expected, order);
    }

    @Test
    void removedEntityIsCleanedUpAndNoLongerUpdated() {
        TestEntity a = manager.add(new TestEntity());
        TestEntity b = manager.add(new TestEntity());

        manager.remove(a);
        manager.update(DT, null);

        assertNull(manager.get(a.getId()));
        assertTrue(a.isRemoved());
        assertEquals(0, a.updates);
        assertEquals(1, a.cleanups);
        assertEquals(1, b.updates);
    }

    @Test
    void entityMarkedRemovedIsDroppedOnTheNextUpdate() {
        TestEntity a = manager.add(new TestEntity());

        a.remove();
        manager.update(DT, null);

        assertEquals(0, manager.size());
        assertEquals(0, a.updates);
        assertEquals(1, a.cleanups);
    }

    @Test
    void entityCanRemoveItselfDuringUpdate() {
        TestEntity a = manager.add(new TestEntity());
        TestEntity b = manager.add(new TestEntity());
        a.onUpdate = e -> manager.remove(e);

        assertDoesNotThrow(() -> manager.update(DT, null));

        assertNull(manager.get(a.getId()));
        assertEquals(1, a.cleanups);
        assertEquals(1, b.updates, "later entities should still update that frame");
    }

    @Test
    void entityCanSpawnAnotherDuringUpdate() {
        TestEntity parent = manager.add(new TestEntity());
        List<TestEntity> spawned = new ArrayList<>();
        parent.onUpdate = e -> {
            if (spawned.isEmpty()) spawned.add(manager.add(new TestEntity()));
        };

        assertDoesNotThrow(() -> manager.update(DT, null));
        TestEntity child = spawned.get(0);
        assertSame(child, manager.get(child.getId()));
        assertEquals(0, child.updates, "new entity should start updating next frame");

        manager.update(DT, null);
        assertEquals(1, child.updates);
    }

    @Test
    void entitySpawnedAndRemovedInTheSameUpdateIsNeverAdded() {
        TestEntity parent = manager.add(new TestEntity());
        TestEntity child = new TestEntity();
        parent.onUpdate = e -> {
            manager.add(child);
            manager.remove(child);
        };

        manager.update(DT, null);

        assertNull(manager.get(child.getId()));
        assertEquals(1, manager.size());
    }

    @Test
    void renderDrawsEveryEntity() {
        TestEntity a = manager.add(new TestEntity());
        TestEntity b = manager.add(new TestEntity());

        manager.render();

        assertEquals(1, a.renders);
        assertEquals(1, b.renders);
    }

    @Test
    void cleanupCleansEveryEntityAndEmptiesTheManager() {
        TestEntity a = manager.add(new TestEntity());
        TestEntity b = manager.add(new TestEntity());

        manager.cleanup();

        assertEquals(1, a.cleanups);
        assertEquals(1, b.cleanups);
        assertEquals(0, manager.size());
    }
}
