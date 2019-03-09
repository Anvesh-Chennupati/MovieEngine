import oracle.jdbc.OracleDriver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Scanner;
import java.util.TimeZone;


public class Populate {
    private File folder = new File("dat_files");
    private File[] listOfFiles = folder.listFiles();

    public static void main(String[] args) {
        Populate gen = new Populate();
//        gen.fetchFiles();
        char ask;
        System.out.println("\n Do you want to execute the insertions into database?");
        System.out.println("y for yes");
        System.out.println("n for no");
        Scanner reader = new Scanner(System.in);
        ask = reader.next().charAt(0);

        if (ask == 'Y' || ask == 'y') {
            System.out.println("Executing the insertions now");
            gen.execute();
        } else if (ask == 'n' || ask == 'N') {
            System.out.println("Continuing");
        } else {
            System.out.println("Invalid Input");
            System.exit(0);
        }

        System.out.println("Finished connection");
    }


    private void execute() {
        Connection conn = null;
        try {
            System.out.println("DB server connecting...");
            conn = openConnect();
            System.out.println("DB server connection successfully");
            publishData(conn, "dat_files/tags.dat");


            for (File listOfFile : listOfFiles) {
                if (listOfFile.isFile()) {
                    String temp = listOfFile.getName();
                    if (temp.trim().contains("user")) {
                        publishUserData(conn, "dat_files/" + temp);
                    } else {
                        if (!temp.equals("tags.dat")) {
                            publishData(conn, "dat_files/" + temp);
                        }
                    }

                }
            }
        } catch (SQLException e) {
            System.err.println("[Error]: Errors occurs when connecting to the database server: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.err.println("[Error]: Cannot find the database driver");
        } catch (IOException io) {
            io.printStackTrace();
        } finally {
            assert conn != null;
            closeConnect(conn);
        }
    }

    private Connection openConnect() throws SQLException, ClassNotFoundException {
        DriverManager.registerDriver(new oracle.jdbc.OracleDriver());
        String host = "localhost";
        String port = "1521";
        String dbName = "orcl";
        String uName = "scott";
        String pWord = "scott";

        String dbURL = "jdbc:oracle:thin:@" + host + ":" + port + ":" + dbName;
        return DriverManager.getConnection(dbURL, uName, pWord);
    }
    private void closeConnect(Connection conn) {
        try {
            conn.close();
        } catch (SQLException e) {
            System.err.println("[Error]: Cannot close Oracle DB connection: " + e.getMessage());
        }
    }

    private void publishData(Connection conn, String fName) throws SQLException, IOException {
        Statement stmt = conn.createStatement();
        FileReader fReader = new FileReader(fName);
        BufferedReader bufReader = new BufferedReader(fReader);


        String tableName = fName.replaceFirst(".dat", "");
        tableName = tableName.replaceFirst("dat_files/", "");

        System.out.println("[Info]: Deleting previous tuplies from " + tableName);
        stmt.executeUpdate("DELETE FROM " + tableName);

        System.out.println("[Info]: Inserting Data into " + tableName);
        String tuple = bufReader.readLine(); // ignore first line
        int fieldLen = tuple.split("\t").length;

        if (tableName.equals("movie_locations")) {
            fieldLen = 5;
        }

        while ((tuple = bufReader.readLine()) != null) {

            String[] fields = tuple.split("\t");
            String prefix = "";
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < fieldLen; i++) {
                String attr = " ";

                if (i < fields.length && fields[i] != null) {
                    attr = fields[i].replaceAll("'", "''");
                }
                if (attr.equals("\\N")) {
                    attr = "";
                }

                sb.append(prefix + "'" + attr + "'");
                prefix = ", ";
            }
            System.out.println("INSERT INTO " + tableName + " VALUES (" + sb.toString() + ")");
            stmt.executeUpdate("INSERT INTO " + tableName + " VALUES (" + sb.toString() + ")");
        }
        fReader.close();
        stmt.close();
    }

    private void publishUserData(Connection conn, String fName) throws SQLException, IOException {
        Statement stmt = conn.createStatement();
        FileReader fReader = new FileReader(fName);
        BufferedReader bufReader = new BufferedReader(fReader);

        String tableName = fName.replaceFirst(".dat", "");
        tableName = tableName.replaceFirst("dat_files/", "");
        boolean timestamp = false;
        if (tableName.contains("-timestamps")) {
            System.out.print("[Info]: " + tableName + "share table with ");
            timestamp = true;
            tableName = tableName.replaceFirst("-timestamps", "");
            System.out.println(tableName);
        }

        System.out.println("[Info]: Deleting previous tuplies from " + tableName);
        stmt.executeUpdate("DELETE FROM " + tableName);

        System.out.println("[Info]: Inserting Data into " + tableName);
        String tuple = bufReader.readLine();
        int fieldLen = 3;
        PreparedStatement pstmt = conn.prepareStatement("INSERT INTO " + tableName + " VALUES(?,?,?,?)");
        while ((tuple = bufReader.readLine()) != null) {
            String[] fields = tuple.split("\t");
            for (int i = 0; i < fieldLen; i++) {
                pstmt.setString(i + 1, fields[i]);
            }
            StringBuilder sb = new StringBuilder();
            if (!timestamp) {
                // yyyy-mm-dd
                sb.append(fields[5] + "-" + fields[4] + "-" + fields[3] + " ");
                // hh-mi-ss
                sb.append(fields[6] + ":" + fields[7] + ":" + fields[8]);
            } else {

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                sdf.setTimeZone(TimeZone.getTimeZone("GMT+1"));
                Date date = new Date(Long.parseLong(fields[3]));
                sb.append(sdf.format(date));
            }
            pstmt.setTimestamp(4, java.sql.Timestamp.valueOf(sb.toString()));
            pstmt.executeUpdate();
        }
        // close file and statement
        fReader.close();
        pstmt.close();
        stmt.close();
    }
}
