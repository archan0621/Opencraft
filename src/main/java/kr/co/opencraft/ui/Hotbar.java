package kr.co.opencraft.ui;

import java.util.List;
import kr.co.opencraft.world.BlockDefinition;
import kr.co.opencraft.world.BlockRegistry;
import kr.co.opencraft.world.BlockTypes;

public final class Hotbar {
    private final List<BlockDefinition> slots;
    private int selectedIndex;

    public Hotbar(BlockRegistry blocks, int initiallySelectedBlockType) {
        slots = blocks.values().stream()
            .filter(block -> block.typeId() != BlockTypes.AIR)
            .toList();
        if (slots.isEmpty()) {
            throw new IllegalArgumentException("hotbar requires at least one placeable block");
        }

        for (int i = 0; i < slots.size(); i++) {
            if (slots.get(i).typeId() == initiallySelectedBlockType) {
                selectedIndex = i;
                break;
            }
        }
    }

    public int size() {
        return slots.size();
    }

    public BlockDefinition getSlot(int index) {
        return slots.get(index);
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public BlockDefinition getSelectedBlock() {
        return slots.get(selectedIndex);
    }

    public void selectSlot(int index) {
        if (index >= 0 && index < slots.size()) {
            selectedIndex = index;
        }
    }

    public void scroll(int amount) {
        selectedIndex = Math.floorMod(selectedIndex + amount, slots.size());
    }
}
