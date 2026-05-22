// JsonStore.java — 제네릭 JSON 파일 저장소 (RWLock + atomic move)
package dev.sybaek.mukja.common.store;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import java.util.function.Supplier;

public class JsonStore<T> {
    private final Path file;
    private final Class<T> type;
    private final ObjectMapper mapper;
    private final Supplier<T> defaultValue;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    // file 경로의 JSON을 type으로 직렬화/역직렬화한다. 파일이 없으면 defaultValue를 반환한다
    public JsonStore(Path file, Class<T> type, ObjectMapper mapper, Supplier<T> defaultValue) {
        this.file = file;
        this.type = type;
        this.mapper = mapper;
        this.defaultValue = defaultValue;
    }

    // 읽기 락으로 현재 값을 읽는다 (없으면 기본값)
    public T read() {
        lock.readLock().lock();
        try { return readUnlocked(); }
        finally { lock.readLock().unlock(); }
    }

    // 쓰기 락으로 값을 저장한다 (tmp 파일 작성 후 atomic move)
    public void write(T data) {
        lock.writeLock().lock();
        try { writeUnlocked(data); }
        finally { lock.writeLock().unlock(); }
    }

    // 쓰기 락으로 read-modify-write를 원자적으로 수행한다
    public <R> R update(Function<T, R> fn) {
        lock.writeLock().lock();
        try { return fn.apply(readUnlocked()); }
        finally { lock.writeLock().unlock(); }
    }

    // 락 없이 읽는다 (update 콜백 내부 전용)
    public T readUnlocked() {
        if (Files.notExists(file)) return defaultValue.get();
        try { return mapper.readValue(file.toFile(), type); }
        catch (IOException e) { throw new StoreException("read failed: " + file, e); }
    }

    // 락 없이 쓴다 (update 콜백 내부 전용): tmp → ATOMIC_MOVE
    public void writeUnlocked(T data) {
        try {
            Files.createDirectories(file.getParent());
            Path tmp = Files.createTempFile(file.getParent(), "tmp-", ".json");
            mapper.writerWithDefaultPrettyPrinter().writeValue(tmp.toFile(), data);
            try { Files.move(tmp, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING); }
            catch (AtomicMoveNotSupportedException e) { Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING); }
        } catch (IOException e) { throw new StoreException("write failed: " + file, e); }
    }
}
