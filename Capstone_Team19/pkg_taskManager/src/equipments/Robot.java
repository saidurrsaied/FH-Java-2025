package equipments;

import taskManager.Task;

import java.awt.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Robot implements Runnable {
    private final String name;
    private double battery = 100;
    private final ChargingStation station;
    private Point location = new Point(0, 0);
    private String status = "Idle";
    private final BlockingQueue<Task> taskQueue = new LinkedBlockingQueue<>();

    public Robot(String name, ChargingStation station) {
        this.name = name;
        this.station = station;
    }

    public String getName() { return name; }
    public void setStatus(String status) { this.status = status; }
    public String getStatus() { return status; }
    public Point getLocation() { return location; }

    public void moveTo(Point target) {
        System.out.println(name + " moving from " + location + " to " + target);
        try {
            Thread.sleep(1000); // simulate travel
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        location = target;
        battery -= 5;
        System.out.println(name + " arrived at " + location + " (Battery: " + battery + "%)");
    }

    public void addTask(Task task) {
        taskQueue.add(task);
        System.out.println(name + " received task: " + task.getDescription());
    }

    @Override
    public void run() {
        System.out.println(name + " thread started.");
        while (true) {
            try {
                Task task = taskQueue.take();
                task.execute(this);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void chargeIfNeeded() {
        if (battery < 30) {
            station.charge(this);
        }
    }

    public void setBattery(double value) {
        battery = value;
    }



}

