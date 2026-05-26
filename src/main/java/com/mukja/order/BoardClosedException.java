// BoardClosedException.java — 마감된 주문판에 제출 시도 시
package com.mukja.order;
public class BoardClosedException extends RuntimeException {
    public BoardClosedException(String message) { super(message); }
}
