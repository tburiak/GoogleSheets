import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SQLClient {

    private ResultSet rs;
    private String connectionUrl;
    private String sql;

    public SQLClient(String connectionUrl) {
        this.connectionUrl = connectionUrl;
    }

    public List<List<String>> extractDataFromDB() {
        List<List<String>> mData = new ArrayList<>();
        List<String> columnNames = new ArrayList<>();
        try {
            // Load SQL Server JDBC driver and establish connection.
//            try (Connection connection = DriverManager.getConnection(connectionUrl)) {
//                System.out.println("Done.");
//
//                // READ demo
//                System.out.print("Reading data from table, press ENTER to continue...");
//                sql = "SELECT * FROM dbo.AreaPath;";
//                try (Statement statement = connection.createStatement();
//                     ResultSet resultSet = statement.executeQuery(sql)) {
//                    while (resultSet.next()) {
//                        System.out.println(
//                                resultSet.getInt(1) + " " + resultSet.getString(2) + " " + resultSet.getString(3));
//                    }
//                }
//                connection.close();
//                System.out.println("All done.");
//            }

            System.out.print("Connecting to SQL Server ... ");
            try (Connection connection = DriverManager.getConnection(connectionUrl)) {
                System.out.println("Done.");
                CallableStatement cstmt;
                cstmt = null;
                try {

                    cstmt = connection.prepareCall("{call JavaIterationStats(?)}");
                    cstmt.setInt("qtydays", 4);
                    cstmt.execute();

                    boolean results = cstmt.execute();
                    int rowsAffected = 0;

                    while (results || rowsAffected != -1) {
                        if (results) {
                            rs = cstmt.getResultSet();
                            break;
                        } else {
                            rowsAffected = cstmt.getUpdateCount();
                        }
                        results = cstmt.getMoreResults();
                    }

                    if (rs != null) {
                        ResultSetMetaData columns = rs.getMetaData();
                        int i = 0;
                        while (i < columns.getColumnCount()) {
                            i++;
                            columnNames.add(columns.getColumnName(i));
                        }
                        System.out.print("\n");
//                        while (rs.next()) {
//                            for (i = 0; i < columnNames.size(); i++) {
//                                System.out.print(rs.getString(columnNames.get(i))
//                                        + "\t");
//
//                            }
//                            System.out.print("\n");
//                        }
                    }


                    while (rs.next()) {
                        List<String> dataRow = new ArrayList<>();
                        for (int i = 0; i < columnNames.size(); i++) {
                            dataRow.add(rs.getString(columnNames.get(i)));
                        }
//                        dataRow.add(rs.getString("ManualTestID"));
//                        dataRow.add(rs.getString("Name"));
                        mData.add(dataRow);
                    }

                    rs.close();
                    cstmt.close();
                    connection.close();

                    System.out.println("All done.");
                } catch (Exception e) {
                    System.out.println();
                    e.printStackTrace();
                }
            }

        } catch (Exception e) {
            System.out.println();
            e.printStackTrace();
        }
        mData.add(0, columnNames);
//        mData.add(0, Arrays.asList("FAILED", null));
        return mData;
    }
}
