package pathFinding;

import java.awt.Point;
import java.util.*;

/**
 * Thread-safe PathFinding class using A* algorithm.
 * It does NOT modify WarehouseMap nodes directly.
 * Each call to findPath() creates local TempNodes to store gCost/hCost/parent,
 * so multiple threads (robots) can call findPath() concurrently.
 */
public class PathFinding {

    private final WarehouseMap warehouseMap;

    public PathFinding(WarehouseMap warehouseMap) {
        this.warehouseMap = warehouseMap;
    }

    /**
     * Finds a path between startPos and targetPos using A* algorithm.
     * Safe for multi-threaded calls.
     */
    public List<Point> findPath(Point startPos, Point targetPos) {
        // --- local cache to store temporary node data (per thread, per call)
        Map<Point, TempNode> localNodes = new HashMap<>();

        // Initialize start and target nodes
        TempNode startNode = new TempNode(startPos);
        TempNode targetNode = new TempNode(targetPos);
        localNodes.put(startPos, startNode);
        localNodes.put(targetPos, targetNode);

        List<TempNode> openSet = new ArrayList<>();
        Set<Point> closedSet = new HashSet<>();
        openSet.add(startNode);

        while (!openSet.isEmpty()) {
            // Find node with lowest fCost in openSet
            TempNode currentNode = openSet.get(0);
            for (int i = 1; i < openSet.size(); i++) {
                TempNode n = openSet.get(i);
                if (n.fCost() < currentNode.fCost() ||
                        (n.fCost() == currentNode.fCost() && n.hCost < currentNode.hCost)) {
                    currentNode = n;
                }
            }
            openSet.remove(currentNode);
            closedSet.add(currentNode.position);

            // --- Target reached
            if (currentNode.position.equals(targetPos)) {
                return retracePath(currentNode);
            }

            // --- Loop through neighbors from the map
            for (Node neighborNode : warehouseMap.getNeighbors(warehouseMap.getWarehouseObject(currentNode.position))) {
                if (neighborNode == null) continue;

                Point neighborPos = neighborNode.position;

                // Skip closed or blocked cells
                if (closedSet.contains(neighborPos)) continue;
                if (!neighborNode.walkable && !neighborPos.equals(targetPos)) continue;

                // Get or create local TempNode for this position
                TempNode neighbor = localNodes.get(neighborPos);
                if (neighbor == null) {
                    neighbor = new TempNode(neighborPos);
                    localNodes.put(neighborPos, neighbor);
                }

                int newCost = currentNode.gCost + getDistance(currentNode.position, neighbor.position);

                if (newCost < neighbor.gCost || !openSet.contains(neighbor)) {
                    neighbor.gCost = newCost;
                    neighbor.hCost = getDistance(neighbor.position, targetPos);
                    neighbor.parent = currentNode;

                    if (!openSet.contains(neighbor)) openSet.add(neighbor);
                }
            }
        }

        System.out.println("Cannot find path from " + startPos + " to " + targetPos);
        return new ArrayList<>();
    }

    /**
     * Retraces the path from target node back to start node.
     */
    private List<Point> retracePath(TempNode targetNode) {
        List<Point> path = new ArrayList<>();
        TempNode current = targetNode;

        while (current != null) {
            path.add(current.position);
            current = current.parent;
        }
        Collections.reverse(path);

        // Optional: remove first point (start position)
        if (!path.isEmpty()) path.remove(0);

        System.out.println("Found path (" + path.size() + " steps): " + path);
        return path;
    }

    /**
     * Returns diagonal (14) and straight (10) distance cost.
     */
    private int getDistance(Point a, Point b) {
        int dstX = Math.abs(a.x - b.x);
        int dstY = Math.abs(a.y - b.y);
        return (dstX > dstY)
                ? 14 * dstY + 10 * (dstX - dstY)
                : 14 * dstX + 10 * (dstY - dstX);
    }

    /**
     * Local temporary node for thread-safe pathfinding.
     * Each robot/thread has its own TempNode set.
     */
    private static class TempNode {
        final Point position;
        int gCost = Integer.MAX_VALUE;
        int hCost;
        TempNode parent;

        TempNode(Point position) {
            this.position = position;
            this.gCost = 0; // will be updated for start node
        }

        int fCost() {
            return gCost + hCost;
        }
    }
}
