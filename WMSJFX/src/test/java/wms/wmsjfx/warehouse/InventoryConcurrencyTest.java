package wms.wmsjfx.warehouse;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Concurrency stress test that demonstrates a potential data race in Inventory.
 *
 * Notes:
 * - The current Inventory implementation is not thread-safe and uses a plain HashMap with
 *   non-atomic quantity updates. This test spawns multiple threads performing interleaved
 *   increases/decreases on the same product and checks for lost updates.
 * - Because races are non-deterministic, this test is @Disabled by default and tagged "race".
 *   Run it on-demand to observe failures: mvnw.cmd -Djunit.jupiter.tags=race test
 */
@Tag("race")
@Disabled("Demonstrates non-deterministic data races in Inventory; run manually with -Djunit.jupiter.tags=race")
class InventoryConcurrencyTest {

    private StorageShelf shelf(String id, int x, int y) {
        return new StorageShelf(id, x, y, WahouseObjectType.StorageShelf);
    }

    @Test
    @DisplayName("Concurrent increments/decrements on the same product may lose updates")
    void concurrentUpdates_mayLoseUpdates() throws Exception {
        Inventory inv = new Inventory();
        Product p = new Product("ConcurrentWidget", "CW");
        StorageShelf s = shelf("S1", 1, 1);
        inv.addProduct(p, 1000, s); // start with 1000

        int threads = 16;
        int opsPerThread = 500; // each thread does +1 then -1 repeatedly
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);

        Runnable r = () -> {
            try {
                assertTrue(start.await(1, TimeUnit.SECONDS), "start not released in time");
                for (int i = 0; i < opsPerThread; i++) {
                    inv.increaseProductQuantity("CW", 1);
                    inv.decreaseProductQuantity("CW", 1);
                }
            } catch (InterruptedException e) {
                fail("Thread interrupted");
            } finally {
                done.countDown();
            }
        };

        for (int i = 0; i < threads; i++) {
            new Thread(r, "Worker-" + i).start();
        }

        start.countDown();
        assertTrue(done.await(10, TimeUnit.SECONDS), "Workers did not finish in time");

        // In a thread-safe implementation, the final quantity would remain 1000
        int finalQty = inv.getProductQuantity("CW");
        // We assert equality to the expected value; with races this may intermittently fail,
        // which is the point of this demonstration test.
        assertEquals(1000, finalQty, "Final quantity indicates lost updates due to a race");
    }
}
