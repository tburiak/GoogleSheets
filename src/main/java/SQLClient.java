import java.sql.*;
import java.util.ArrayList;
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
        try (Connection connection = DriverManager.getConnection(connectionUrl);
             CallableStatement cstmt = connection.prepareCall("{call PassRateDailyReport(?, ?)}")) {
            System.out.println("Done.");
            cstmt.setInt("qtydays", 5);
            cstmt.setString("typeQuery", "failed");
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
                while (rs.next()) {
                    List<String> dataRow = new ArrayList<>();
                    columnNames.forEach(column -> {
                        try {
                            dataRow.add(rs.getString(column).trim());
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    });
                    mData.add(dataRow);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        mData.add(0, columnNames);
        return mData;
    }
}
