/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.sql.PreparedStatement;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */
public class Amazon {

  // reference to physical database connection.
  private Connection _connection = null;

  // handling the keyboard inputs through a BufferedReader
  // This variable can be global for convenience.
  static BufferedReader in = new BufferedReader(
      new InputStreamReader(System.in));

  /**
   * Creates a new instance of Amazon store
   *
   * @param hostname the MySQL or PostgreSQL server hostname
   * @param database the name of the database
   * @param username the user name used to login to the database
   * @param password the user login password
   * @throws java.sql.SQLException when failed to make a connection.
   */
  public Amazon(String dbname, String dbport, String user, String passwd) throws SQLException {

    System.out.print("Connecting to database...");
    try {
      // constructs the connection URL
      String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
      System.out.println("Connection URL: " + url + "\n");

      // obtain a physical connection
      this._connection = DriverManager.getConnection(url, user, passwd);
      System.out.println("Done");
    } catch (Exception e) {
      System.err.println("Error - Unable to Connect to Database: " + e.getMessage());
      System.out.println("Make sure you started postgres on this machine");
      System.exit(-1);
    } // end catch
  }// end Amazon

  // Method to calculate euclidean distance between two latitude, longitude pairs.
  public double calculateDistance(double lat1, double long1, double lat2, double long2) {
    double t1 = (lat1 - lat2) * (lat1 - lat2);
    double t2 = (long1 - long2) * (long1 - long2);
    return Math.sqrt(t1 + t2);
  }

  /**
   * Method to execute an update SQL statement. Update SQL instructions
   * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
   *
   * @param sql the input SQL string
   * @throws java.sql.SQLException when update failed
   */
  public void executeUpdate(String sql) throws SQLException {
    // creates a statement object
    Statement stmt = this._connection.createStatement();

    // issues the update instruction
    stmt.executeUpdate(sql);

    // close the instruction
    stmt.close();
  }// end executeUpdate

  /**
   * Method to execute an input query SQL instruction (i.e. SELECT). This
   * method issues the query to the DBMS and outputs the results to
   * standard out.
   *
   * @param query the input query string
   * @return the number of rows returned
   * @throws java.sql.SQLException when failed to execute the query
   */
  public int executeQueryAndPrintResult(String query) throws SQLException {
    // creates a statement object
    Statement stmt = this._connection.createStatement();

    // issues the query instruction
    ResultSet rs = stmt.executeQuery(query);

    System.out.println("Executing query: " + query); // Debugging: Print the executed query

    /*
     ** obtains the metadata object for the returned result set. The metadata
     ** contains row and column info.
     */
    ResultSetMetaData rsmd = rs.getMetaData();
    int numCol = rsmd.getColumnCount();
    int rowCount = 0;

    // iterates through the result set and output them to standard out.
    boolean outputHeader = true;
    while (rs.next()) {
      if (outputHeader) {
        for (int i = 1; i <= numCol; i++) {
          System.out.print(rsmd.getColumnName(i) + "\t");
        }
        System.out.println();
        outputHeader = false;
      }
      for (int i = 1; i <= numCol; ++i)
        System.out.print(rs.getString(i) + "\t");
      System.out.println();
      ++rowCount;
    } // end while
    stmt.close();
    System.out.println("Row count: " + rowCount); // Debugging: Print the number of rows returned
    return rowCount;
  }// end executeQueryAndPrintResult

  /**
   * Method to execute an input query SQL instruction (i.e. SELECT). This
   * method issues the query to the DBMS and returns the results as
   * a list of records. Each record in turn is a list of attribute values
   *
   * @param query the input query string
   * @return the query result as a list of records
   * @throws java.sql.SQLException when failed to execute the query
   */
  public List<List<String>> executeQueryAndReturnResult(String query) throws SQLException {
    // creates a statement object
    Statement stmt = this._connection.createStatement();

    // issues the query instruction
    ResultSet rs = stmt.executeQuery(query);

    /*
     ** obtains the metadata object for the returned result set. The metadata
     ** contains row and column info.
     */
    ResultSetMetaData rsmd = rs.getMetaData();
    int numCol = rsmd.getColumnCount();
    int rowCount = 0;

    // iterates through the result set and saves the data returned by the query.
    boolean outputHeader = false;
    List<List<String>> result = new ArrayList<List<String>>();
    while (rs.next()) {
      List<String> record = new ArrayList<String>();
      for (int i = 1; i <= numCol; ++i)
        record.add(rs.getString(i));
      result.add(record);
    } // end while
    stmt.close();
    return result;
  }// end executeQueryAndReturnResult

  /**
   * Method to execute an input query SQL instruction (i.e. SELECT). This
   * method issues the query to the DBMS and returns the number of results
   *
   * @param query the input query string
   * @return the number of rows returned
   * @throws java.sql.SQLException when failed to execute the query
   */
  public int executeQuery(String query) throws SQLException {
    // creates a statement object
    Statement stmt = this._connection.createStatement();

    // issues the query instruction
    ResultSet rs = stmt.executeQuery(query);

    int rowCount = 0;

    // iterates through the result set and count nuber of results.
    while (rs.next()) {
      rowCount++;
    } // end while
    stmt.close();
    return rowCount;
  }

  /**
   * Method to fetch the last value from sequence. This
   * method issues the query to the DBMS and returns the current
   * value of sequence used for autogenerated keys
   *
   * @param sequence name of the DB sequence
   * @return current value of a sequence
   * @throws java.sql.SQLException when failed to execute the query
   */
  public int getCurrSeqVal(String sequence) throws SQLException {
    Statement stmt = this._connection.createStatement();

    ResultSet rs = stmt.executeQuery(String.format("Select currval('%s')", sequence));
    if (rs.next())
      return rs.getInt(1);
    return -1;
  }

  /**
   * Method to close the physical connection if it is open.
   */
  public void cleanup() {
    try {
      if (this._connection != null) {
        this._connection.close();
      } // end if
    } catch (SQLException e) {
      // ignored.
    } // end try
  }// end cleanup

  /**
   * The main execution method
   *
   * @param args the command line arguments this inclues the <mysql|pgsql> <login
   *             file>
   */
  public static void main(String[] args) {
    if (args.length != 3) {
      System.err.println(
          "Usage: " +
              "java [-classpath <classpath>] " +
              Amazon.class.getName() +
              " <dbname> <port> <user>");
      return;
    } // end if

    Greeting();
    Amazon esql = null;
    try {
      // use postgres JDBC driver.
      Class.forName("org.postgresql.Driver").newInstance();
      // instantiate the Amazon object and creates a physical
      // connection.
      String dbname = args[0];
      String dbport = args[1];
      String user = args[2];
      esql = new Amazon(dbname, dbport, user, "");

      boolean keepon = true;
      while (keepon) {
        // These are sample SQL statements
        System.out.println("MAIN MENU");
        System.out.println("---------");
        System.out.println("1. Create user");
        System.out.println("2. Log in");
        System.out.println("9. < EXIT");
        String authorisedUser = null;
        switch (readChoice()) {
          case 1:
            CreateUser(esql);
            break;
          case 2:
            authorisedUser = LogIn(esql);
            break;
          case 9:
            keepon = false;
            break;
          default:
            System.out.println("Unrecognized choice!");
            break;
        }// end switch
        if (authorisedUser != null) {
          boolean usermenu = true;
          while (usermenu) {
            System.out.println("MAIN MENU");
            System.out.println("---------");
            System.out.println("1. View Stores within 30 miles");
            System.out.println("2. View Product List");
            System.out.println("3. Place a Order");
            System.out.println("4. View 5 recent orders");

            // the following functionalities basically used by managers
            System.out.println("5. Update Product");
            System.out.println("6. View 5 recent Product Updates Info");
            System.out.println("7. View 5 Popular Items");
            System.out.println("8. View 5 Popular Customers");
            System.out.println("9. Place Product Supply Request to Warehouse");
            System.out.println(".........................");
            System.out.println("20. Log out");
            switch (readChoice()) {
              case 1:
                viewStores(esql);
                break;
              case 2:
                viewProducts(esql);
                break;
              case 3:
                placeOrder(esql);
                break;
              case 4:
                viewRecentOrders(esql);
                break;
              case 5:
                updateProduct(esql);
                break;
              case 6:
                viewRecentUpdates(esql);
                break;
              case 7:
                viewPopularProducts(esql);
                break;
              case 8:
                viewPopularCustomers(esql);
                break;
              case 9:
                placeProductSupplyRequests(esql);
                break;

              case 20:
                usermenu = false;
                break;
              default:
                System.out.println("Unrecognized choice!");
                break;
            }
          }
        }
      } // end while
    } catch (Exception e) {
      System.err.println(e.getMessage());
    } finally {
      // make sure to cleanup the created table and close the connection.
      try {
        if (esql != null) {
          System.out.print("Disconnecting from database...");
          esql.cleanup();
          System.out.println("Done\n\nBye !");
        } // end if
      } catch (Exception e) {
        // ignored.
      } // end try
    } // end try
  }// end main

  public static void Greeting() {
    System.out.println(
        "\n\n*******************************************************\n" +
            "              User Interface      	               \n" +
            "*******************************************************\n");
  }// end Greeting

  /*
   * Reads the users choice given from the keyboard
   * 
   * @int
   **/
  public static int readChoice() {
    int input;
    // returns only if a correct value is given.
    do {
      System.out.print("Please make your choice: ");
      try { // read the integer, parse it and break.
        input = Integer.parseInt(in.readLine());
        break;
      } catch (Exception e) {
        System.out.println("Your input is invalid!");
        continue;
      } // end try
    } while (true);
    return input;
  }// end readChoice

  /*
   * Creates a new user
   **/
  public static void CreateUser(Amazon esql) {
    try {
      System.out.print("\tEnter name: ");
      String name = in.readLine();
      System.out.print("\tEnter password: ");
      String password = in.readLine();
      System.out.print("\tEnter latitude: ");
      String latitude = in.readLine(); // enter lat value between [0.0, 100.0]
      System.out.print("\tEnter longitude: "); // enter long value between [0.0, 100.0]
      String longitude = in.readLine();

      String type = "Customer";

      String query = String.format(
          "INSERT INTO USERS (name, password, latitude, longitude, type) VALUES ('%s','%s', %s, %s,'%s')", name,
          password, latitude, longitude, type);

      esql.executeUpdate(query);
      System.out.println("User successfully created!");
    } catch (Exception e) {
      System.err.println(e.getMessage());
    }
  }// end CreateUser

  /*
   * Check log in credentials for an existing user
   * 
   * @return User login or null is the user does not exist
   **/
  private static String currentUserName = null;
  private static int currentUserID = 0;

  public static String LogIn(Amazon esql) {
    try {
      System.out.print("\tEnter name: ");
      String name = in.readLine();
      System.out.print("\tEnter password: ");
      String password = in.readLine();
      String query = String.format("SELECT * FROM USERS WHERE name = '%s' AND password = '%s'", name, password);
      int userNum = esql.executeQuery(query);
      if (userNum > 0) {
        currentUserName = name;
        String query1 = String.format("SELECT userID FROM users WHERE name = '%s' AND password = '%s'", name, password);
        List<List<String>> result = esql.executeQueryAndReturnResult(query1);
        if (!result.isEmpty() && !result.get(0).isEmpty()) {
          String userIDStr = result.get(0).get(0);
          currentUserID = Integer.parseInt(userIDStr);
        }

        return name;
      }
      return null;
    } catch (Exception e) {
      System.err.println(e.getMessage());
      return null;
    }
  }// end

  // Rest of the functions definition go in here
  // Shreya complete
  public static void viewStores(Amazon esql) {
    try {
        String getUserLocationQuery = "SELECT U.latitude, U.longitude FROM Users U WHERE U.name = '" + currentUserName + "'"; 
        List<List<String>> userLocationResult = esql.executeQueryAndReturnResult(getUserLocationQuery);
        if (userLocationResult.isEmpty()){
          System.out.println("No user location found");
          return;
        }
        double userLatitude = Double.parseDouble(userLocationResult.get(0).get(0));
        double userLongitude = Double.parseDouble(userLocationResult.get(0).get(1));

        String getStoresLocation = "SELECT S.storeID, S.latitude, S.longitude FROM Store S"; 
        List<List<String>> storesLocationResult = esql.executeQueryAndReturnResult(getStoresLocation);
        List<String> nearbyStores = new ArrayList<>();
        for (int i = 0; i < storesLocationResult.size(); i++){
          String storeName = storesLocationResult.get(i).get(0);
          double storeLatitude = Double.parseDouble(storesLocationResult.get(i).get(1));
          double storeLongitude= Double.parseDouble(storesLocationResult.get(i).get(2));
          double distance = esql.calculateDistance(userLatitude,userLongitude,storeLatitude,userLongitude);
          if (distance <= 30){
            nearbyStores.add(storeName);
          }
        }
        System.out.println("Stores within 30 miles of you: ");
        if (nearbyStores.isEmpty()){
          System.out.println("No stores near you ");
        }else{
          for (String storeName : nearbyStores){
            System.out.println(storeName);
          }
        }

    } catch (Exception e) {
        System.err.println("Error: " + e.getMessage());
    }
  };

  // Shreya complete
  public static void placeOrder(Amazon esql) {
    try {
        System.out.print("\tEnter store id: ");
        int storeId = Integer.parseInt(in.readLine());
        System.out.print("\tEnter product name: ");
        String productName = in.readLine();
        System.out.print("\tEnter the number of units you wish to purchase: ");
        int selectednumberofUnits = Integer.parseInt(in.readLine());

      String getUserLocationQuery = "SELECT U.latitude, U.longitude, U.userID FROM Users U WHERE U.name = '" + currentUserName + "'"; 
      List<List<String>> userLocationResult = esql.executeQueryAndReturnResult(getUserLocationQuery);
      double userLatitude = Double.parseDouble(userLocationResult.get(0).get(0));
      double userLongitude = Double.parseDouble(userLocationResult.get(0).get(1));
      int userId = Integer.parseInt(userLocationResult.get(0).get(2));
      if (userLocationResult.isEmpty()){
        System.out.println("No user location found");
        return;
      }
      String getStoreLocationInfo = "SELECT S.latitude, S.longitude FROM Store S WHERE S.storeId = " + storeId; 
      List<List<String>> storeLocationResult = esql.executeQueryAndReturnResult(getStoreLocationInfo);
      double storeLatitude = Double.parseDouble(storeLocationResult.get(0).get(0));
      double storeLongitude = Double.parseDouble(storeLocationResult.get(0).get(1));

      double distanceFromStore = esql.calculateDistance(userLatitude,userLongitude,storeLatitude,storeLongitude);
      if (distanceFromStore > 30){
        System.out.println("Store is not within 30 miles of you.");
        return;
      }
         String getNumberofUnitsProduct = "SELECT numberOfUnits FROM Product P WHERE P.productName ='" + productName + "'";
        List<List<String>> queryResult = esql.executeQueryAndReturnResult(getNumberofUnitsProduct);
        int availableUnits = Integer.parseInt(queryResult.get(0).get(0));
        if (availableUnits < selectednumberofUnits){
        System.out.println("Not enough units of this products" + "There are only: " + availableUnits + " available units" );
        return;
        }
        //INSERT INTO ORDERS
      String query2 = "INSERT INTO Orders (customerID, storeID, productName, unitsOrdered, orderTime) VALUES ("
            + userId + ", " + storeId + ", '" + productName + "', " + selectednumberofUnits + ", current_timestamp)";
      esql.executeUpdate(query2);
      System.out.println("Order successful");

        // UPDATE PRODUCTS TABLE
        String updateProducts = "UPDATE Product SET numberOfUnits = numberOfUnits - " + selectednumberofUnits +
                                " WHERE storeID = " + storeId + " AND productName = '" + productName + "'";
        esql.executeUpdate(updateProducts);
        System.out.println(currentUserName + " ordered " + selectednumberofUnits + " units of " + productName + " from store " + storeId);

    } catch (Exception e) {
        System.err.println("Error: " + e.getMessage());
    }
  };

  //Shreya complete 
  public static void updateProduct(Amazon esql) {
    try {
        if (currentUserName == null){
            System.out.println("Error: No user logged in.");
            return;
        }
          String getManagerID = "SELECT userID FROM Users WHERE name = '" + currentUserName + "'";
          List<List<String>> managerIDs = esql.executeQueryAndReturnResult(getManagerID);
          int managerID = Integer.parseInt(managerIDs.get(0).get(0));


          String userTypeQuery = "SELECT type FROM Users WHERE name = '" + currentUserName + "'";
          List<List<String>> userTypeResult = esql.executeQueryAndReturnResult(userTypeQuery);
          if (userTypeResult.isEmpty()) {
            System.out.println("No user with this name exists.");
            return;
          }
            String userType = userTypeResult.get(0).get(0);
            if (!userType.trim().equals("manager")) {
              System.out.println("Error: Access denied. Only managers can update product information.");
              return;
            }
              System.out.print("\tEnter store id: ");
              int storeId = Integer.parseInt(in.readLine());

              String query = "SELECT * FROM Store "  + " WHERE storeID = " + storeId + " AND managerID = " + managerID;
              List<List<String>> managerOutput = esql.executeQueryAndReturnResult(query);
              if (managerOutput.isEmpty()){
                System.out.println( "You does not manage this store. Please try another store");
                return;
              }
              System.out.print("\tEnter product you wish to update: ");
              String product = in.readLine();

              System.out.print("\tEnter number of units you wish to update it to: ");
              int numUnits = Integer.parseInt(in.readLine());

              System.out.print("\tWhat price would you like to change it to?: ");
              double price = Double.parseDouble(in.readLine());

              String updateProducts = "UPDATE Product SET numberOfUnits =  " + numUnits + " , pricePerUnit = " + price +
                                " WHERE storeID = " + storeId + " AND productName = '" + product + "'";
              esql.executeUpdate(updateProducts);

              String insertUpdate = "INSERT INTO ProductUpdates (managerID, storeID, productName, updatedOn) VALUES ("
                    + managerID + ", " + storeId + ", '" + product + "', "  + " CURRENT_TIMESTAMP)";
              esql.executeUpdate(insertUpdate);
              System.out.println("Insert into ProductUpdates successful");
              String output5RecentUpdates = "SELECT * " +
                "FROM ProductUpdates " +
                "WHERE managerID = " + managerID + 
                "ORDER BY updateNumber DESC " +
                "LIMIT 5";
                esql.executeQueryAndPrintResult(output5RecentUpdates);

      } catch (Exception e) {
            System.err.println(e.getMessage());
          }
    };


  // Shreya complete
  public static void viewPopularProducts(Amazon esql) {
    try {
        if (currentUserName == null) {
            System.out.println("Error: No user logged in.");
        }
        String getManagerID = "SELECT userID FROM Users WHERE name = '" + currentUserName + "'";

        List<List<String>> managerIDs = esql.executeQueryAndReturnResult(getManagerID);
        int managerID = Integer.parseInt(managerIDs.get(0).get(0));

        String userTypeQuery = "SELECT type FROM Users WHERE name = '" + currentUserName + "'";

        List<List<String>> userTypeResult = esql.executeQueryAndReturnResult(userTypeQuery);
          if (userTypeResult.isEmpty()) {
            System.out.println("Error: User not found.");
          }

          String userType = userTypeResult.get(0).get(0);
          if (!"manager".equals(userType.trim())) {
            System.out.println("Error: Access denied. Only managers can execute this query.");
            return;

          }

          String popularProducts = "SELECT P.productName, COUNT(O.orderNumber) AS total_orders " +
                          "FROM Orders O " +
                          "JOIN Product P ON O.storeID = P.storeID AND O.productName = P.productName " +
                          "JOIN Store S ON O.storeID = S.storeID " +
                          "WHERE managerID = " + managerID + 
                          "GROUP BY P.productName " +
                          "ORDER BY total_orders DESC " +
                          "LIMIT 5";
          esql.executeQueryAndPrintResult(popularProducts);

    } catch (Exception e) {
        System.err.println("Error executing query: " + e.getMessage());
    }
  };

  // Khushi complete
  public static void viewProducts(Amazon esql) {
    try {
      System.out.print("\tEnter storeID: ");
      int storeID = Integer.parseInt(in.readLine());
      String query = "SELECT productName AS Product_Name, numberOfUnits AS Number_of_Units, pricePerUnit AS Price_per_Unit FROM Product WHERE storeID = " + storeID;
      esql.executeQueryAndPrintResult(query);

    } catch (Exception e) {
      System.err.println(e.getMessage());
    }
  };

  // Khushi complete
  public static void viewRecentOrders(Amazon esql) {
    try {
      String currentUser = currentUserName;
      if (currentUser != null) {
        String userTypeQuery = "SELECT type FROM Users WHERE name = '" + currentUser + "'";
        List<List<String>> userTypeResult = esql.executeQueryAndReturnResult(userTypeQuery);
        if (!userTypeResult.isEmpty()) {
          String Utype = userTypeResult.get(0).get(0);
          String query;
          if (Utype.trim().equals("customer")) {
            query = "SELECT O.storeID AS Store_ID, O.productName AS product_name, O.unitsOrdered AS units_ordered, O.orderTime AS time_of_order FROM Orders O, Users U WHERE U.userID = O.customerID AND U.name = '" + currentUser + "' ORDER BY O.orderTime DESC LIMIT 5;";
          } else if (Utype.trim().equals("manager")) {
            query = "SELECT O.orderNumber AS order_number, U.name AS customer_name, O.storeID AS store_ID, O.productName AS product_name, O.orderTime AS time_of_order FROM Orders O, Users U, Store S WHERE U.userID = O.customerID AND S.storeID = O.storeID AND S.managerID = " + currentUserID + "ORDER BY O.orderTime DESC LIMIT 5;";
          } else if (Utype.trim().equals("admin")) {
            query = "SELECT O.orderNumber AS order_number, U.name AS customer_name, O.storeID AS store_ID, O.productName AS product_name, O.orderTime AS time_of_order FROM Orders O, Users U, Store S WHERE U.userID = O.customerID AND S.storeID = O.storeID ORDER BY O.orderTime DESC LIMIT 5";
          }
          else {
            System.out.println("Invalid user.");
            return;
          }
          esql.executeQueryAndPrintResult(query);
        } else {
          System.out.println("No user logged in.");
        }
      }
    } catch (Exception e) {
      System.err.println(e.getMessage());
    }
  };

  // Khushi complete
  public static void viewRecentUpdates(Amazon esql) {
    try {
      String currentUser = currentUserName;
      int currUserID = currentUserID;
      if (currentUser != null) {
        String userTypeQ = "SELECT type FROM Users WHERE name = '" + currentUser + "'";
        List<List<String>> userTypeR = esql.executeQueryAndReturnResult(userTypeQ);
        if (!userTypeR.isEmpty()) {
          String Utype = userTypeR.get(0).get(0);
          String query;
          if (Utype.trim().equals("admin")) {
            System.out.print("\nPlease choose an option: View recent product updates, view user information, or update?(view/user/update):");
            String input_updates = in.readLine();
            if (input_updates.equals("view")) {
              query = "SELECT p.updateNumber AS update_number, p.managerID AS manager_ID, p.storeID AS store_ID, p.productName AS product_name, p.updatedOn AS updated_on FROM ProductUpdates p ORDER BY p.updateNumber DESC LIMIT 5;";
              esql.executeQueryAndPrintResult(query);
            }else if (input_updates.equals("user")) {
              System.out.print("\nWould you like to see a list of all users or a certain user? (all/one):");
              String cust_input = in.readLine();
              if (cust_input.equals("all")) {
                query = "SELECT * FROM Users";
                esql.executeQueryAndPrintResult(query);
              } else if (cust_input.equals("one")) {
                System.out.print("Please enter the user ID: ");
                String cust_id_input = in.readLine();
                query = "SELECT * FROM Users WHERE userID = " + cust_id_input;
                esql.executeQueryAndPrintResult(query);
              } else {
                System.out.println("Invalid input. Please type in one of the options.");
              }
            } else if (input_updates.equals("update")) {
              System.out.print("\nWould you like to update product information or user information? (products/users):");
              String update_input = in.readLine();
              if (update_input.equals("products")) {
                System.out.print("Please enter the store ID of the product that you would like to update: ");
                int storeid_inp = Integer.parseInt(in.readLine());
                System.out.print("Please enter the name of the product that you would like to update: ");
                String productname_inp = in.readLine();
                System.out.print("\nPlease enter new information for this product (press enter to skip any field) \nNew StoreID:");
                String storeIDInput = in.readLine();
                System.out.print("New product name: ");
                String productNameInput = in.readLine();
                System.out.print("New number of units: ");
                String numOfUnitsInput = in.readLine();
                System.out.print("New price per unit: ");
                String pricePUnitInput = in.readLine();

                System.out.print("Updating " + productname_inp + " from " + storeid_inp + "...");
                StringBuilder update_query = new StringBuilder("UPDATE Product SET ");
                String update_product_table_query;
                boolean isFirst = true;
                if (!storeIDInput.isEmpty()){
                  update_query.append("storeID = ").append(Integer.parseInt(storeIDInput));
                  isFirst = false;
                }
                if (!productNameInput.isEmpty()) {
                  if (!isFirst) {
                    update_query.append(", ");
                  }
                  update_query.append("productName = '").append(productNameInput).append("'");
                  isFirst = false;
                }
                if (!numOfUnitsInput.isEmpty()) {
                  if (!isFirst) {
                    update_query.append(", ");
                  }
                  update_query.append("numberOfUnits = '").append(numOfUnitsInput).append("'");
                  isFirst = false;
                }
                if (!pricePUnitInput.isEmpty()) {
                  if (!isFirst) {
                    update_query.append(", ");
                  }
                  update_query.append("pricePerUnit = ").append(Integer.parseInt(pricePUnitInput));
                  isFirst = false;
                }
                update_product_table_query = "INSERT INTO productUpdates (managerID,storeID,productName,updatedOn) VALUES (" + currentUserID + "," + storeid_inp + ",'" + productname_inp + "', CURRENT_TIMESTAMP);";
                update_query.append(" WHERE storeID = ").append(storeid_inp).append(" AND productName = '").append(productname_inp).append("'");
                esql.executeUpdate(update_query.toString());
                System.out.print("Product updated! \nLogging...");
                esql.executeUpdate(update_product_table_query.toString());
                System.out.print("Logged. Request completed!\n");
              }else if (update_input.trim().equals("users")) {
                System.out.print("Please enter the user ID that you would like to update: ");
                String user_id_inp = in.readLine();
                System.out.print("Please enter new information for this user (press enter to skip any field) \nNew User ID:");
                String userIdInput = in.readLine();
                System.out.print("New name: ");
                String nameInput = in.readLine();
                System.out.print("New password: ");
                String passwordInput = in.readLine();
                System.out.print("New latitude: ");                
                String latitudeInput = in.readLine();
                System.out.print("New longitude: ");
                String longitudeInput = in.readLine();
                System.out.print("New user type: ");
                String typeInput = in.readLine();
                StringBuilder update_query = new StringBuilder("UPDATE Users SET ");
                boolean isFirst = true;
                if (!userIdInput.isEmpty()) {
                  update_query.append("userID = ").append(Integer.parseInt(userIdInput));
                  isFirst = false;
                }
                if (!nameInput.isEmpty()) {
                  if (!isFirst) {
                    update_query.append(", ");
                  }
                  update_query.append("name = '").append(nameInput).append("'");
                  isFirst = false;
                }
                if (!passwordInput.isEmpty()) {
                  if (!isFirst) {
                    update_query.append(", ");
                  }
                  update_query.append("password = '").append(passwordInput).append("'");
                  isFirst = false;
                }
                if (!latitudeInput.isEmpty()) {
                  if (!isFirst) {
                    update_query.append(", ");
                  }
                  update_query.append("latitude = ").append(Double.parseDouble(latitudeInput));
                  isFirst = false;
                }
                if (!longitudeInput.isEmpty()) {
                  if (!isFirst) {
                    update_query.append(", ");
                  }
                  update_query.append("longitude = ").append(Double.parseDouble(longitudeInput));
                  isFirst = false;
                }
                if (!typeInput.isEmpty()) {
                  if (!isFirst) {
                    update_query.append(", ");
                  }
                  update_query.append("type = '").append(typeInput).append("'");
                  isFirst = false;
                }
                update_query.append(" WHERE userID = ").append(user_id_inp);
                System.out.print("Updating user " + user_id_inp + "...");
                esql.executeUpdate(update_query.toString());
                System.out.println("User information has been updated!");
              }
            }else {
              System.out.println("Invalid choice. Please select one of the shown options.");
            }
          }
          else {
            System.out.println("Invalid user. You do not have permission to view recent updates.");
          }
        }
        else {
          System.out.println("No user logged in. Please log in!");
        }
      }
    } catch (Exception e) {
      System.err.println(e.getMessage());
    }
  };

  // Khushi complete
public static void viewPopularCustomers(Amazon esql) {
    try {
      String currentUser = currentUserName;
      if (currentUser != null) {
        String userTypeQuery = "SELECT type FROM Users WHERE name = '" + currentUser + "'";
        List<List<String>> userTypeResult = esql.executeQueryAndReturnResult(userTypeQuery);
        if (!userTypeResult.isEmpty()) {
          String userType = userTypeResult.get(0).get(0);
          if (userType.trim().equals("manager")) {
            int currentID = currentUserID;
            String query = "SELECT u.userID AS user_ID, u.name as customer_name, u.password, u.latitude, u.longitude, COUNT(*) AS orders_placed FROM Users u, Orders o, Store s WHERE u.userID = o.customerID AND s.storeID = o.storeID AND s.managerID = " + currentID + " GROUP BY u.userID ORDER BY orders_placed DESC LIMIT 5;";
            esql.executeQueryAndPrintResult(query);
          } else if (userType.trim().equals("admin")) {
            int currentID = currentUserID;
            String query = "SELECT u.userID AS user_ID, u.name as customer_name, u.password, u.latitude, u.longitude, COUNT(*) AS orders_placed FROM Users u, Orders o, Store s WHERE u.userID = o.customerID AND s.storeID = o.storeID GROUP BY u.userID ORDER BY orders_placed DESC LIMIT 5;";
            esql.executeQueryAndPrintResult(query);
          } else {
            System.out.println("Error: Access denied. You must be a manager or an admin to access popular customers!");
            return;
          }
        } else {
          System.out.println("Error: User not found.");
        }
      } else {
        System.out.println("Error: No user logged in.");
      }
    } catch (Exception e) {
      System.err.println("Error executing query: " + e.getMessage());
    }
  };

  // Khushi complete
  public static void placeProductSupplyRequests(Amazon esql) {
    try {
      String currentUser = currentUserName;
      if (currentUser != null) {
        String userTypeQuery = "SELECT type FROM Users WHERE name = '" + currentUser + "'";
        List<List<String>> userTypeResult = esql.executeQueryAndReturnResult(userTypeQuery);
        if (!userTypeResult.isEmpty()) {
          String productNameInput;
          int numOfUnitsInput;
          int warehouseIDInput;
          String Utype = userTypeResult.get(0).get(0);
          String product_query;
          String update_product_query;
          String supply_request_query;
          int currentID = currentUserID;
          if (Utype.trim().equals("manager")) {
            System.out.print("Please enter the storeID of the product you would like to place a supply request for: ");
            int storeIDInput = Integer.parseInt(in.readLine());
            String check_query = "SELECT managerID FROM Store WHERE storeID = " + storeIDInput;
            List<List<String>> managerIDResult = esql.executeQueryAndReturnResult(check_query);
            int storeManager = Integer.parseInt(managerIDResult.get(0).get(0));
            if (storeManager == currentID) {
              System.out.print("Please enter the name of the product you would like to place a supply request for: ");
              productNameInput = in.readLine();
              System.out.print("Please enter the number of units needed: ");
              numOfUnitsInput = Integer.parseInt(in.readLine());
              System.out.print("Please enter the warehouseID from which you would like to request the product: ");
              warehouseIDInput = Integer.parseInt(in.readLine());

              supply_request_query = "INSERT INTO productSupplyRequests (managerID,warehouseID,storeID,productName, unitsRequested) VALUES (" + currentID + "," + warehouseIDInput + "," + storeIDInput + ",'" + productNameInput + "'," + numOfUnitsInput + ");";
              product_query = "UPDATE product SET numberOfUnits = numberOfUnits + " + numOfUnitsInput + " WHERE storeID = " + storeIDInput + " AND productName = '" + productNameInput + "';";
              update_product_query = "INSERT INTO productUpdates (managerID,storeID,productName,updatedOn) VALUES (" + currentID + "," + storeIDInput + ",'" + productNameInput + "', CURRENT_TIMESTAMP);";
            } else {
              System.out.println("You do not manage this store. Please enter the storeID for the store(s) that you manage.");
              return;
            }
          } else {
            System.out.println("Invalid user. Only managers can place a supply request!");
            return;
          }

          esql.executeUpdate(supply_request_query);
          System.out.println("You have placed a supply request for " + numOfUnitsInput + " units of " 
                             + productNameInput + " at Warehouse #" + warehouseIDInput + 
                             ". \nUpdating catalog...");
          esql.executeUpdate(product_query);
          System.out.println("Product have been supplied to your store. \nLogging...");
          esql.executeUpdate(update_product_query);
          System.out.println("Request logged. Request completed!");
        } else {
          System.out.println("No user logged in.");
        }
      }
    } catch (Exception e) {
      System.err.println("Error executing query: " + e.getMessage());
    }
  };
}// end Amazon
