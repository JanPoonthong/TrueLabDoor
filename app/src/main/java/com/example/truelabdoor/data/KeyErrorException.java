package com.example.truelabdoor.data;

/**
 * Created by cardlan on 18-6-9.
 */

public class KeyErrorException extends Exception {
    public KeyErrorException() {
        super("Key create error!");
    }

    public KeyErrorException(String message) {
        super(message);
    }
}
