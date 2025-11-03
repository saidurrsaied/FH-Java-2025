package warehouse.datamanager;


import equipmentManager.ChargingStation;
import equipmentManager.EquipmentManager;
import equipmentManager.Robot;
import warehouse.*;
import warehouse.exceptions.DataFileException;

import java.awt.*;
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
            throw new DataFileException("Failed to export inventory to " + filePath, e);
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
                    //System.out.println("Invalid row in inventory CSV file. " + "Row " + currentRow + "in file: " + filePath);
                    throw new DataFileException("Invalid row " + currentRow + " in " + filePath);
                    }
                try {
                    packets.add(new InventoryDataPacket(
                            parts[0], parts[1],
                            Integer.parseInt(parts[2]),
                            parts[3],
                            Integer.parseInt(parts[4]),
                            Integer.parseInt(parts[5])
                    ));
                } catch (NumberFormatException nfe) {
                    throw new DataFileException("Invalid number at row " + currentRow + " in " + filePath, nfe);
                }

            }
        } catch (FileNotFoundException fnf) {
            throw new DataFileException("Inventory CSV not found: " + filePath, fnf);
        } catch (IOException ioe) {
            throw new DataFileException("I/O error reading inventory CSV: " + filePath, ioe);
        }
        return packets;
    }



    public static void initializeInventory(WarehouseManager manager, String inventoryCSVFile) throws DataFileException {
        File dataFile = new File(inventoryCSVFile);

        if (!dataFile.exists()) {

            throw new DataFileException("Inventory file not found: " + inventoryCSVFile);
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
            writer.write("ObjectID,ObjectType,X,Y");
            writer.newLine();

            for (WarehouseDataPacket packet : data) {
                writer.write(String.format("%s,%s,%d,%d",
                        packet.getId(),
                        packet.getType(),
                        packet.getX(),
                        packet.getY()
                ));
                writer.newLine();
            }
            System.out.println("Floor data exported to: " + filePath);
        } catch (IOException e) {
            throw new DataFileException("Failed to export floor data to " + filePath, e);
        }
    }

    /** Load floor layout (objects) from the CSV file */
    public static List<WarehouseDataPacket> loadFloorFromCSV(String filePath) throws DataFileException {
        List<WarehouseDataPacket> floorData = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line = reader.readLine(); // skip header
            int currentRow = 1;
            while ((line = reader.readLine()) != null) {
                currentRow++;
                String[] parts = line.split(",");
                if (parts.length < 4) {
                    System.out.println("Invalid number at row " + currentRow + " in " + filePath);
                    continue;}

                try {
                    floorData.add(new WarehouseDataPacket(
                            parts[0].trim(),
                            parts[1].trim(),
                            Integer.parseInt(parts[2].trim()),
                            Integer.parseInt(parts[3].trim())
                    ));
                } catch (NumberFormatException nfe) {
                    throw new DataFileException("Invalid number at row " + currentRow + " in " + filePath, nfe);
                }
            }
        } catch (FileNotFoundException fnf) {
            throw new DataFileException("Floor CSV not found: " + filePath, fnf);
        } catch (IOException ioe) {
            throw new DataFileException("I/O error reading floor CSV: " + filePath, ioe);
        }
        return floorData;
    }

    /** Initialize the warehouse floor from existing CSV */
    public static void initializeFloor(WarehouseManager warehouseManager, EquipmentManager equipmentManager, String floorCSVFile) throws DataFileException {
        File dataFile = new File(floorCSVFile);

        if (!dataFile.exists()) {
            throw new DataFileException("Warehouse floor file not found: " + floorCSVFile);
        }

        System.out.println("Loading warehouse floor from " + floorCSVFile + " ...");
        List<WarehouseDataPacket> floorData = loadFloorFromCSV(floorCSVFile);

        for (WarehouseDataPacket packet : floorData) {
            try {
                WarehouseObject object;

                // Create objects based on its type
                switch (packet.getType()) {
                    case "StorageShelf" -> {
                        object = new StorageShelf(packet.getId(), packet.getX(), packet.getY(), WahouseObjectType.StorageShelf);
                    }
                    case "LoadingStation" -> {
                        object = new LoadingStation(packet.getId(), packet.getX(), packet.getY(), WahouseObjectType.LoadingStation);
                    }
                    case "PackingStation" -> {
                        object = new PackingStation(packet.getId(), packet.getX(), packet.getY(), WahouseObjectType.PackingStation);
                    }
                    case "ChargingStation" -> {
                        object = new ChargingStation(packet.getId(), packet.getX(), packet.getY(), WahouseObjectType.ChargingStation);
                    }
                    case "Robot" -> {
                        object = new Robot(packet.getId(), new Point(packet.getX(), packet.getY()), equipmentManager, WahouseObjectType.Robot);
                    }

                    default -> throw new IllegalArgumentException("Unknown object type: " + packet.getType());
                }

                warehouseManager.addObjectToFloor(object);

            } catch (RuntimeException e) {
                // keep going but provide context
                System.err.println("Skipping invalid floor record: " + packet + " (" + e.getMessage() + ")");
            }
        }

        System.out.println("Floor layout loaded successfully.");
    }






}
