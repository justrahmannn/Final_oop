package com.mazegame.puzzles; // New package for puzzle-related logic

import com.mazegame.core.Door;
import com.mazegame.items.Lever; // Assuming Lever is in core or items

import java.util.ArrayList;
import java.util.List;

public class PuzzleController {
    private List<Lever> levers;
    private Door controlledDoor;
    private boolean isSolved;

    public PuzzleController(Door doorToControl) {
        this.levers = new ArrayList<>();
        this.controlledDoor = doorToControl;
        this.isSolved = false;

        // Ensure the door is initially locked if this puzzle controls it
        if (this.controlledDoor != null && !this.controlledDoor.isLocked()) {
            // This is tricky. If the door is unlocked by default, this puzzle won't lock it.
            // The door should be created as locked if a puzzle controller manages it.
            // For now, we assume the door is already created as locked.
            System.out.println("PuzzleController created for door: " + doorToControl.getName() +
                               ". Ensure this door is initially locked.");
        } else if (this.controlledDoor == null) {
            System.err.println("PuzzleController created with a NULL door to control!");
        }
    }

    public void registerLever(Lever lever) {
        if (lever != null && !levers.contains(lever)) {
            levers.add(lever);
        }
    }

    public void checkPuzzleState() {
        if (isSolved || controlledDoor == null || levers.isEmpty()) {
            return; // Puzzle already solved, no door, or no levers to check
        }

        boolean allLeversActive = true;
        for (Lever lever : levers) {
            if (!lever.isActive()) {
                allLeversActive = false;
                break;
            }
        }

        if (allLeversActive) {
            System.out.println("PUZZLE SOLVED! All levers are active.");
            if (controlledDoor.isLocked()) {
                controlledDoor.unlock(); // Unlock the door
                controlledDoor.open();   // And open it
                System.out.println(controlledDoor.getName() + " has been unlocked and opened by the puzzle!");
            }
            isSolved = true;
        } else {
            System.out.println("Puzzle not yet solved. Not all levers are active.");
            // Optional: If the door was opened by the puzzle and a lever is turned off, re-lock it?
            // For "all levers on" this usually means it stays unlocked.
        }
    }
}