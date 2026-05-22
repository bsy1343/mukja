// BoardClosedException.java — 마감된 주문판에 제출 시도 시
package dev.sybaek.mukja.order;
public class BoardClosedException extends RuntimeException {
    public BoardClosedException(String message) { super(message); }
}
