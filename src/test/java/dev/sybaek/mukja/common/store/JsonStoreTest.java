// JsonStoreTest.java — JsonStore 동작 및 동시성 검증
package dev.sybaek.mukja.common.store;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.IntStream;

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
        @SuppressWarnings("unchecked")
        List<Future<?>> futures = (List<Future<?>>) (List<?>) IntStream.range(0, n)
                .mapToObj(i -> pool.submit(() ->
                        store.update(cur -> { store.writeUnlocked(new Box(cur.value() + 1)); return null; })))
                .toList();
        for (Future<?> f : futures) f.get();
        pool.shutdown();
        assertEquals(n, store.read().value());
    }
}
