// StoreException.java — JSON 저장소 I/O 예외
package dev.sybaek.mukja.common.store;

public class StoreException extends RuntimeException {
    // 저장소 읽기/쓰기 실패 시 던진다
    public StoreException(String message, Throwable cause) { super(message, cause); }
}
