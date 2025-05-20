package com.mazegame.interfaces;

public interface Activatable {
    void open();
    void close();
    boolean isLocked();
    // Consider adding: boolean canBeForcedOpen(); // For crowbars
    // Consider adding: boolean requiresKey();
    // Consider adding: String getKeyId(); // If specific keys are needed
}