package su.nightexpress.excellentenchants.enchantment.tool;

import org.bukkit.block.BlockFace;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class TunnelEnchantTest {

    private static Set<List<Integer>> asSet(int[][] offsets) {
        Set<List<Integer>> set = new HashSet<>();
        for (int[] o : offsets) set.add(Arrays.stream(o).boxed().toList());
        return set;
    }

    @Test
    void levelThreeIsFullSquareOnEveryFace() {
        for (BlockFace face : new BlockFace[]{BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST}) {
            int[][] offsets = TunnelEnchant.areaOffsets(face, TunnelEnchant.blocksByLevel(3));
            assertEquals(8, offsets.length, face.name());
            Set<List<Integer>> set = asSet(offsets);
            assertEquals(8, set.size(), face.name());
            assertFalse(set.contains(List.of(0, 0, 0)), face.name());
            for (int[] o : offsets) {
                // Ни шага вглубь: по оси удара смещение всегда ноль.
                int depth = face.getModX() != 0 ? o[0] : face.getModY() != 0 ? o[1] : o[2];
                assertEquals(0, depth, face.name());
                for (int c : o) assertTrue(c >= -1 && c <= 1, face.name());
            }
        }
    }

    @Test
    void levelsGiveTwoFiveNine() {
        assertEquals(1, TunnelEnchant.areaOffsets(BlockFace.NORTH, TunnelEnchant.blocksByLevel(1)).length);
        assertEquals(4, TunnelEnchant.areaOffsets(BlockFace.NORTH, TunnelEnchant.blocksByLevel(2)).length);
        assertEquals(8, TunnelEnchant.areaOffsets(BlockFace.NORTH, TunnelEnchant.blocksByLevel(5)).length);
        // I уровень в стене — блок под ударом: тоннель 1×2.
        assertArrayEquals(new int[]{0, -1, 0}, TunnelEnchant.areaOffsets(BlockFace.EAST, 2)[0]);
    }

    @Test
    void lookFaceMatchesGodPickaxeThresholds() {
        assertEquals(BlockFace.UP, TunnelEnchant.lookFace(0F, 60F));
        assertEquals(BlockFace.DOWN, TunnelEnchant.lookFace(0F, -60F));
        assertEquals(BlockFace.NORTH, TunnelEnchant.lookFace(0F, 0F));    // смотрит на юг
        assertEquals(BlockFace.EAST, TunnelEnchant.lookFace(90F, 10F));   // на запад
        assertEquals(BlockFace.SOUTH, TunnelEnchant.lookFace(180F, 0F));  // на север
        assertEquals(BlockFace.WEST, TunnelEnchant.lookFace(-90F, 0F));   // на восток
    }
}
