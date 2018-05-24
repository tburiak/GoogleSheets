import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SQLClient {

    private ResultSet rs;
    private String connectionUrl;
    private String sql;
    private int countOfRows;
    private int countOfColumns;

    private List<List<String>> mData;

    public SQLClient(String connectionUrl) {
        this.connectionUrl = connectionUrl;
    }

    public List<List<String>> extractDataFromDB(String queryType) {
        mData = new ArrayList<>();
        List<String> columnNames = new ArrayList<>();
        System.out.print("Connecting to SQL Server ... ");
        try (Connection connection = DriverManager.getConnection(connectionUrl);
             CallableStatement cstmt = connection.prepareCall("{call PassRateDailyReport(?, ?)}")) {
            System.out.println("Done.");
            cstmt.setInt("qtydays", 5);
            cstmt.setString("typeQuery", queryType);
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

    public int getCountOfRows() {
        countOfRows = this.mData.size();
        return countOfRows;
    }

    public int getCountOfColumns() {
        countOfColumns = this.mData.get(0).size();
        return countOfColumns;
    }
}
