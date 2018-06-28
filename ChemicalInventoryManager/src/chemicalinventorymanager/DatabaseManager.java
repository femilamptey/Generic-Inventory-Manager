package chemicalinventorymanager;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.text.SimpleDateFormat;  
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This is a static helper class that manages all the logic for the SQLite backend of this application
 */
public final class DatabaseManager {
    private DatabaseManager(){} //This class is static; it shouldn't be able to be instanced
    
    private static Connection databaseConenction;
    private static String DATABADE_PATH = "src\\chemicalinventorymanager\\Databases\\ShopDatabase.db";
    private static String DATABADE_NAME = "ShopDatabase.db";
    private static int DEFAULT_ID_LENGTH = 7;
    
    public static enum tableTypes{
        customer,
        item,
        transaction,
        supplier
    }
    
    private static void processError(Exception e){
        System.out.println(e);
        HelperClass.alertError(e);
    }
    
    private static void connect() throws SQLException{
        try {
            if (databaseConenction != null) {
                return;
            }
            String url = "jdbc:sqlite:" + DATABADE_PATH;
            databaseConenction = DriverManager.getConnection(url);
        } catch (SQLException sQLException) {
            System.out.println(sQLException);
        }    
    }
    
    /**
     * This class is designed for integration with ListViews with search-by-name capabilities
     * @param query
     * @return A list of Customers without their array of debts (for performance reasons)
     */
    public static List<Customer> getCustomersWithName(String query) throws SQLException{
        ArrayList<Customer> resultList = new ArrayList <Customer>();
        try {
            connect();
            Statement statement = databaseConenction.createStatement();
            
            query = query.toLowerCase();
            String command = "select ID, full name, GENDER, TOTAL DEBT from " + DATABADE_NAME + ".Customers"
                    + "where lower(FULL NAME) contains " + query;
            
            ResultSet results = statement.executeQuery(command);
            
            while (results.next()) {
                Customer customer = new Customer(results.getString("ID"));
                customer.fullName = results.getString("FULL NAME");
                Customer.Gender gender = (results.getString("GENDER")).equals("Male") ? Customer.Gender.male : Customer.Gender.female;
                customer.gender = gender;
                customer.totalDebt = results.getDouble("TOTAL DEBT");
                resultList.add(customer);
            }
            statement.close();
            return resultList;

        } catch (SQLException sQLException) {
            System.out.println(sQLException);
            return null;
        }
    }
    
    public static void addCustomer (Customer customer) throws SQLException{      
        try {
            connect();
            Statement statement = databaseConenction.createStatement();
            String command = String.format("INSERT INTO `Customers` (`ID`, `Full Name`, `Gender`, `Total Debt`)"
                    + " VALUES ('%s', '%s', '%s', '%f')", customer.getID(), customer.fullName, customer.getGender(), customer.totalDebt);
            statement.executeUpdate(command);
            statement.close(); 
            HelperClass.showSuccess(customer.fullName + " was successfully added to the database!");
        } catch (Exception e) {
            processError(e);
        }    
    }
    
    /**
     * Should only be called if such an id is guaranteed to exist
     * @param id
     * @return A whole customer object, including the customer's array of debts
     * @throws SQLException 
     */
    public static Customer getCustomerWithId(String id) throws SQLException{
        try {
            connect();
            Statement statement = databaseConenction.createStatement();
            
            String command = "select * from " + DATABADE_NAME + ".Customers"
                    + "where ID = " + id;
            
            ResultSet results = statement.executeQuery(command);
            Customer customer = null;
            
            if (results.next()) {
                customer = new Customer(results.getString("ID"));
                customer.fullName = results.getString("FULL NAME");
                Customer.Gender gender = (results.getString("GENDER")).equals("Male") ? Customer.Gender.male : Customer.Gender.female;
                customer.gender = gender;
                customer.totalDebt = results.getDouble("TOTAL DEBT");
                
                Blob debtsBytes = results.getBlob("ARRAY OF CREDITS");
                InputStream binaryInput = null;
                if (null != debtsBytes && debtsBytes.length() > 0) {
                    binaryInput = debtsBytes.getBinaryStream();
                    ObjectInputStream inputStream = new ObjectInputStream(binaryInput);
                    customer.debts = (Map<String, Double>)inputStream.readObject();
                }else {
                    customer.debts = null;
                }
            }
            statement.close();
            return customer;

        } catch (Exception e) {
            processError(e);
            return  null;
        } 
    }
    
    
    
    
    
     /**
     * This class is designed for integration with ListViews with search-by-name capabilities
     * @param query
     * @return A list of Items
     */
    public static List<InventoryItem> getItemsWithName(String query) throws SQLException{
        ArrayList<InventoryItem> resultList = new ArrayList <InventoryItem>();
        try {
            connect();
            Statement statement = databaseConenction.createStatement();
            
            query = query.toLowerCase();
            String command = "select * from `Inventory Items` where lower(`NAME`) like '%" + query + "%'";
            
            ResultSet results = statement.executeQuery(command);
            
            while (results.next()) {
                InventoryItem item = new InventoryItem(results.getString("ID"));
                item.name = results.getString("NAME");
                item.price = results.getFloat("PRICE");
                item.description = results.getString("DESCRIPTION");
                item.amountAvailable = results.getInt("AMOUNT AVAILABLE");
                item.stillSold = (results.getInt("STILL SOLD")) == 1;
                item.supplierId = results.getString("SUPPLIERS");
                resultList.add(item);
            }
            statement.close();
            return resultList;

        } catch (Exception e) {
            processError(e);
            return null;
        }
    }
    
    public static void addItem (InventoryItem item) throws SQLException{      
        try {
            connect();
            Statement statement = databaseConenction.createStatement();
            String command = String.format("INSERT INTO `Inventory Items`"
                    + " VALUES ('%s', '%s','%f', '%s', '%d', '%d', '%s', '%s')", 
                    item.getID(), item.name, item.price ,item.description, (item.stillSold) ? 1 : 0, item.amountAvailable, item.imageName, item.supplierId);
            statement.executeUpdate(command);
            statement.close();    
            HelperClass.showSuccess(item.name + " was successfully added to the database!");
        } catch (Exception e) {
            processError(e);
        }
      
    }
    
     /**
     * Should only be called if such an id is guaranteed to exist
     * @param id
     * @return
     * @throws SQLException 
     */
    public static InventoryItem getItemWithId(String id) throws SQLException{
        try {
            connect();
            Statement statement = databaseConenction.createStatement();
            
            String command = "select * from `Inventory Items` where `ID` = '" + id + "'";
            
            ResultSet results = statement.executeQuery(command);
            InventoryItem item = null;
            
            if (results.next()) {
                item = new InventoryItem(results.getString("ID"));
                item.name = results.getString("NAME");
                item.price = results.getFloat("PRICE");
                item.description = results.getString("DESCRIPTION");
                item.amountAvailable = results.getInt("AMOUNT AVAILABLE");
                item.stillSold = (results.getInt("STILL SOLD")) == 1;
                item.supplierId = results.getString("SUPPLIERS");
                item.imageName = results.getString("PICTURE NAME");                
            }
            statement.close();
            return item;

        } catch (Exception e) {
            processError(e);
            return  null;
        }
    }
    
    
    
    
    
     /**
     * This class is designed for integration with ListViews with search-by-name capabilities
     * @param query
     * @return A list of Suppliers
     */
    public static List<Supplier> getSuppliersWithName(String query) throws SQLException{
        ArrayList<Supplier> resultList = new ArrayList <Supplier>();
        try {
            connect();
            Statement statement = databaseConenction.createStatement();
            
            query = query.toLowerCase();
            String command = "select * " + DATABADE_NAME + ".Suppliers"
                    + "where lower(NAME) contains " + query;
            
            ResultSet results = statement.executeQuery(command);
            
            while (results.next()) {
                Supplier supplier = new Supplier(results.getString("ID"));
                supplier.name = results.getString("NAME");
                supplier.email = results.getString("EMAIL");
                supplier.phone = results.getString("PHONE");
                
                String stringofItemIds = results.getString("ITEMS SUPPLIED");
                List<String> items = Arrays.asList(stringofItemIds.split("\\s*,\\s*"));
                ArrayList<String> idArray = new ArrayList<>(items);
                supplier.itemsSupplied = idArray;
                
                resultList.add(supplier);
            }
            statement.close();
            return resultList;

        } catch (Exception e) {
            processError(e);
            return null;
        }
    }
    
    public static void addSupplier (Supplier supplier) throws SQLException{      
        try {
            connect();
            Statement statement = databaseConenction.createStatement();
            
            String command = String.format("INSERT INTO `Suppliers`"
                    + " VALUES ('%s', '%s', '%s', '%s', '%s')", 
                    supplier.getID(), supplier.name, supplier.email, supplier.phone, String.join(",", supplier.itemsSupplied));
            statement.executeUpdate(command);
            statement.close(); 
            HelperClass.showSuccess(supplier.name + " was successfully added to the database!");
        } catch (Exception e) {
            processError(e);
        }
      
    }
    
     /**
     * Should only be called if such an id is guaranteed to exist
     * @param id
     * @return
     * @throws SQLException 
     */
    public static Supplier getSupplierWithId(String id) throws SQLException{
        try {
            connect();
            Statement statement = databaseConenction.createStatement();
            
            String command = "select * from " + DATABADE_NAME + ".Inventory Items"
                    + "where ID = " + id;
            
            ResultSet results = statement.executeQuery(command);
            Supplier supplier = null;
            
            if (results.next()) {
                supplier = new Supplier(results.getString("ID"));
                supplier.name = results.getString("NAME");
                supplier.email = results.getString("EMAIL");
                supplier.phone = results.getString("PHONE");
                
                String stringofItemIds = results.getString("ITEMS SUPPLIED");
                List<String> items = Arrays.asList(stringofItemIds.split("\\s*,\\s*"));
                ArrayList<String> idArray = new ArrayList<>(items);
                supplier.itemsSupplied = idArray;           
            }
            statement.close();
            return supplier;

        } catch (Exception e) {
            processError(e);
            return null;
        }
    }
    
    
    
    
        
     /**
     * This class is designed for integration with ListViews with search-by-name capabilities
     * @param query
     * @return A list of Suppliers
     */
    public static List<Transaction> getTransactionWithDate(String query) throws SQLException{
        ArrayList<Transaction> resultList = new ArrayList <Transaction>();
        try {
            connect();
            Statement statement = databaseConenction.createStatement();
            
            query = query.toLowerCase();
            String command = "select * " + DATABADE_NAME + ".Transactions"
                    + "where lower(DATE) contains " + query;
            
            ResultSet results = statement.executeQuery(command);
            SimpleDateFormat formatter = new SimpleDateFormat("E, MMM dd yyyy");  
            
            while (results.next()) {
                Transaction tran = new Transaction(results.getString("ID"), results.getString("CUSTOMERID"));
                tran.date = formatter.parse(results.getString("DATE"));
                tran.mode = (results.getInt("DEBIT OR CREDIT") == 0) ? Transaction.transactionMode.debit : Transaction.transactionMode.credit;
                tran.creditAmount = results.getDouble("CREDIT AMOUNT");
                
                List<String> itemIDs = Arrays.asList(results.getString("ITEMS SOLD").split("\\s*,\\s*"));
                
                List<String> quantities = Arrays.asList(results.getString("QUANTITIES SOLD").split("\\s*,\\s*"));
                
                Map<String, Integer> transactions = new HashMap<>();
                 
                 for (int i = 0; i < itemIDs.size(); i++){
                    transactions.put(itemIDs.get(i), Integer.parseInt(quantities.get(i)));
                 }
                 
                tran.transactions = transactions;
                
                resultList.add(tran);
            }
            statement.close();
            return resultList;

        } catch (Exception e) {
            processError(e);
            return null;
        }
    }
    
    public static void addTransaction (Transaction tran) throws SQLException{      
        try {
            connect();
            Statement statement = databaseConenction.createStatement();
            SimpleDateFormat formatter = new SimpleDateFormat("E, MMM dd yyyy"); 
            
            String itemKeys = "";
            Set<String> keys = tran.transactions.keySet();
            itemKeys = String.join(",", keys);
            
            String quantities = "";
            Collection<Integer> mappings = tran.transactions.values();
            quantities = mappings.toString();
            quantities = quantities.substring(1, quantities.length()-1); 
            
            String command = String.format("INSERT INTO Suppliers"
                    + "VALUES ('%s', '%s', '%s', '%d', '%s', '%s', '%f')", 
                    tran.getID(), tran.getCustomerID(), formatter.format(tran.date), (tran.mode == Transaction.transactionMode.debit) ? 0 : 1, itemKeys, quantities, tran.creditAmount);
            statement.executeUpdate(command);
            statement.close();            
        } catch (Exception e) {
            processError(e);
        }
      
    }
    
     /**
     * Should only be called if such an id is guaranteed to exist
     * @param id
     * @return
     * @throws SQLException 
     */
    public static Transaction getTransactionWithId(String id) throws SQLException{
        try {
            connect();
            Statement statement = databaseConenction.createStatement();
            SimpleDateFormat formatter = new SimpleDateFormat("E, MMM dd yyyy");  
            
            String command = "select * from " + DATABADE_NAME + ".Inventory Items"
                    + "where ID = " + id;
            
            ResultSet results = statement.executeQuery(command);
            Transaction tran = null;
            
            if (results.next()) {
                tran = new Transaction(results.getString("ID"), results.getString("CUSTOMERID"));
                tran.date = formatter.parse(results.getString("DATE"));
                tran.mode = (results.getInt("DEBIT OR CREDIT") == 0) ? Transaction.transactionMode.debit : Transaction.transactionMode.credit;
                tran.creditAmount = results.getDouble("CREDIT AMOUNT");
                
                List<String> itemIDs = Arrays.asList(results.getString("ITEMS SOLD").split("\\s*,\\s*"));
                
                List<String> quantities = Arrays.asList(results.getString("QUANTITIES SOLD").split("\\s*,\\s*"));
                
                Map<String, Integer> transactions = new HashMap<>();
                 
                 for (int i = 0; i < itemIDs.size(); i++){
                    transactions.put(itemIDs.get(i), Integer.parseInt(quantities.get(i)));
                 }
                 
                tran.transactions = transactions;
                return tran;
            }

        } catch (Exception e) {
            processError(e);
            return null;
        }
        return  null;
    }
    
    
    public static String getTableFromEnum(tableTypes table){
        String tableName = "";
        switch (table){
            case customer:
                tableName = "`Customers`";
                break;
            case transaction:
                tableName = "`Transactions`";
                break;
            case item:
                tableName = "`Inventory Items`";
                break;
            case supplier:
                tableName = "`Suppliers`";
                break;                
        }
        return tableName;
    }
    
    
    public static HashSet<String> getIDs(tableTypes table) throws SQLException{
        connect();
        HashSet<String> idStrings = new HashSet<String>();
        Statement statement = databaseConenction.createStatement();      
        String command = "SELECT DISTINCT ID FROM " + getTableFromEnum(table);            
        ResultSet results = statement.executeQuery(command);
        while (results.next()) idStrings.add(results.getString("ID"));
        return idStrings;
    }
    
        
     /**
     * Generates a unique id for use in the entered table type.
     * This will break the whole app once the user gets a few billion entries (which probably won't happen).
     * @throws SQLException 
     */
    public static String generateUniqueId(tableTypes table) throws SQLException{
        connect();
        String id = "";
        boolean valid = false;
        HashSet<String> idStrings = getIDs(table);                   

        while (!valid){
            id = RandomString.nextString(DEFAULT_ID_LENGTH);
            valid = !idStrings.contains(id);
        }
        return id;
    }

}
