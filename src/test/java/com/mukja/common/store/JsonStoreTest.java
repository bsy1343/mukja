// JsonStoreTest.java — JsonStore 동작 및 동시성 검증
package com.mukja.common.store;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

class JsonStoreTest {
    record Box(int value) {}

    @Test
    void readReturnsDefaultWhenFileMissing(@TempDir Path dir) {
        JsonStore<Box> store = new JsonStore<>(dir.resolve("box.json"), Box.class,
                new ObjectMapper(), () -> new Box(0));
        assertEquals(0, store.read().value());
    }

    @Test
    void writeThenReadRoundTrips(@TempDir Path dir) {
        JsonStore<Box> store = new JsonStore<>(dir.resolve("box.json"), Box.class,
                new ObjectMapper(), () -> new Box(0));
        store.write(new Box(42));
        assertEquals(42, store.read().value());
    }

    @Test
    void concurrentUpdatesDoNotLoseData(@TempDir Path dir) throws Exception {
        JsonStore<Box> store = new JsonStore<>(dir.resolve("box.json"), Box.class,
                new ObjectMapper(), () -> new Box(0));
        int n = 200;
        ExecutorService pool = Executors.newFixedThreadPool(16);
        java.util.List<Future<?>> futures = new java.util.ArrayList<>();
        for (int i = 0; i < n; i++) {
            futures.add(pool.submit(() -> store.mutate(cur -> new Box(cur.value() + 1))));
        }
        for (Future<?> f : futures) f.get();
        pool.shutdown();
        assertEquals(n, store.read().value());
    }
}
