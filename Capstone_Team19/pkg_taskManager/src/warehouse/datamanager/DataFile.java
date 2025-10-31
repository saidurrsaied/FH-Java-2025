package warehouse.datamanager;


import warehouse.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/***
 * Class: DataFile
 * Methods: exportInventoryToCSV, loadInventoryFromCSV
 * Description: Class for handling warehouse data files
 */


public class DataFile {

    private DataFile() {
    }

    /**Export inventory from InventoryDataPacket list to CSV file */
    public static void exportInventoryToCSV(List<InventoryDataPacket> data, String filePath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write("ProductID,ProductName,Quantity,ShelfID,X,Y");
            writer.newLine();

            for (InventoryDataPacket packet : data) {
                writer.write(String.format("%s,%s,%d,%s,%d,%d",
                        packet.getProductId(),
                        packet.getProductName(),
                        packet.getQuantity(),
                        packet.getShelfId(),
                        packet.getX(),
                        packet.getY()));
                writer.newLine();
            }
            System.out.println("Inventory exported to: " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /** Load inventory data from a CSV file into a list of InventoryDataPacket objects*/


    public static List<InventoryDataPacket> loadInventoryFromCSV(String filePath) {
        List<InventoryDataPacket> packets = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {

            String line;
            int currentRow = 0;

            // Skip header
            if ((line = reader.readLine()) != null) {
                currentRow++;
            }


            while ((line = reader.readLine()) != null) {
                currentRow++;
                String[] parts = line.split(",");

                if (parts.length != 6) {
                    System.out.println("Invalid row in inventory CSV file. " + "Row " + currentRow + "in file: " + filePath);
                    continue;}
                packets.add(new InventoryDataPacket(
                        parts[0], parts[1],
                        Integer.parseInt(parts[2]),
                        parts[3],
                        Integer.parseInt(parts[4]),
                        Integer.parseInt(parts[5])
                ));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return packets;
    }



    public static void initializeInventory(WarehouseManager manager, String inventoryCSVFile) throws IOException {
        File dataFile = new File(inventoryCSVFile);

        if (!dataFile.exists()) {
            // Instead of generating demo data, throw exception
            throw new FileNotFoundException("Inventory file not found: " + inventoryCSVFile);
        }

        else {
            System.out.println("Loading existing inventory from " + inventoryCSVFile + " ...");
            List<InventoryDataPacket> data = loadInventoryFromCSV(inventoryCSVFile);

            for (InventoryDataPacket packet : data) {
                manager.addProductToInventory(
                        new Product(packet.getProductName(),packet.getProductId() ),
                        packet.getQuantity(),
                        packet.getShelfId()
                );
            }

            System.out.println("Inventory loaded successfully.");
        }
    }



        /**
         * #####################################################
         * ### Floor plan export/ import methods
         * #####################################################
         * */


    /** Export warehouse floor data (objects) to CSV file */
    public static void exportFloorToCSV(List<WarehouseDataPacket> data, String filePath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write("ObjectID,ObjectType,X,Y,Available");
            writer.newLine();

            for (WarehouseDataPacket packet : data) {
                writer.write(String.format("%s,%s,%d,%d,%b",
                        packet.getId(),
                        packet.getType(),
                        packet.getX(),
                        packet.getY(),
                        packet.isAvailable()));
                writer.newLine();
            }
            System.out.println("Floor data exported to: " + filePath);
        } catch (IOException e) {
            System.err.println("Failed to export floor data: " + e.getMessage());
        }
    }

    /** Load floor layout (objects) from CSV file */
    public static List<WarehouseDataPacket> loadFloorFromCSV(String filePath) throws IOException {
        List<WarehouseDataPacket> floorData = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line = reader.readLine(); // skip header
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length < 5) continue;

                floorData.add(new WarehouseDataPacket(
                        parts[0].trim(),
                        parts[1].trim(),
                        Integer.parseInt(parts[2].trim()),
                        Integer.parseInt(parts[3].trim()),
                        Boolean.parseBoolean(parts[4].trim())
                ));
            }
        }
        return floorData;
    }

    /** Initialize warehouse floor from existing CSV */
    public static void initializeFloor(WarehouseManager manager, String floorCSVFile) throws IOException {
        File dataFile = new File(floorCSVFile);

        if (!dataFile.exists()) {
            throw new FileNotFoundException("Warehouse floor file not found: " + floorCSVFile);
        }

        System.out.println("Loading warehouse floor from " + floorCSVFile + " ...");
        List<WarehouseDataPacket> data = loadFloorFromCSV(floorCSVFile);

        for (WarehouseDataPacket packet : data) {
            try {
                WarehouseObject object;

                // Create object based on its type
                switch (packet.getType()) {
                    case "StorageShelf" -> {
                        object = new StorageShelf(packet.getId(), packet.getX(), packet.getY(), 1, 1);
                        if (!packet.isAvailable()) ((StorageShelf) object).makeOccupied();
                    }
                    case "Station" -> {
                        object = new Station(packet.getId(), packet.getId(), packet.getX(), packet.getY());
                        if (!packet.isAvailable()) ((Station) object).setOccupied();
                    }
                    default -> throw new IllegalArgumentException("Unknown object type: " + packet.getType());
                }

                manager.addObjectToFloor(object);

            } catch (Exception e) {
                System.err.println("Skipping invalid floor record: " + packet + " (" + e.getMessage() + ")");
            }
        }

        System.out.println("Floor layout loaded successfully.");
    }






}
