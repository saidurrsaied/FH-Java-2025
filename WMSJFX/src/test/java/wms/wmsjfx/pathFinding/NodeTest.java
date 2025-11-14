package wms.wmsjfx.pathFinding;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.Point;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class NodeTest {

    @Test
    @DisplayName("fCost equals gCost + hCost and walkable flag is respected")
    void fCostAndWalkable() {
        Node n = new Node(NodeType.None, true, new Point(1, 2));
        n.gCost = 10;
        n.hCost = 7;
        assertEquals(17, n.fCost());
        assertTrue(n.getWalkable());
    }

    @Test
    @DisplayName("Parent linkage can be assigned and read")
    void parentLink() {
        Node a = new Node(NodeType.Shelf, false, new Point(0, 0));
        Node b = new Node(NodeType.None, true, new Point(1, 0));
        b.parent = a;
        assertSame(a, b.parent);
        assertEquals(new Point(0,0), b.parent.position);
    }
}
