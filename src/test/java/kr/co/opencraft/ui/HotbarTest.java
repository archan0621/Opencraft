package kr.co.opencraft.ui;

import kr.co.opencraft.world.BlockTypes;
import kr.co.opencraft.world.OpenCraftBlockRegistry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HotbarTest {
    @Test
    void selectsRegisteredBlocksAndWrapsWhenScrolling() {
        Hotbar hotbar = new Hotbar(OpenCraftBlockRegistry.blocks(), BlockTypes.ORIGIN_STONE);

        assertEquals(6, hotbar.size());
        assertEquals(BlockTypes.ORIGIN_STONE, hotbar.getSelectedBlock().typeId());

        hotbar.selectSlot(hotbar.size() - 1);
        hotbar.scroll(1);
        assertEquals(BlockTypes.MY_STONE, hotbar.getSelectedBlock().typeId());

        hotbar.scroll(-1);
        assertEquals(BlockTypes.BEDROCK, hotbar.getSelectedBlock().typeId());
    }
}
