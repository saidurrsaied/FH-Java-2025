package equipments;

public class ChargingStation {
    private boolean busy = false;

    // synchronized ensures only one shuttle can charge at a time
    public synchronized void charge(Robot robot) {
        if (busy) {
            System.out.println("Station busy! " + robot.getName() + " waiting...");
        }

        while (busy) {
            try {
                wait(); // wait until station is free
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        busy = true;
        System.out.println(robot.getName() + " started charging...");

        try {
            Thread.sleep(3000); // simulate charging for 3 seconds
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        robot.setBattery(100);
        System.out.println(robot.getName() + " fully charged!");

        busy = false;
        notifyAll(); // wake up waiting shuttles
    }
}

