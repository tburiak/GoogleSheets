import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.microsoft.sqlserver.jdbc.SQLServerDriver;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class SheetsQuickstart {
    private static final String APPLICATION_NAME = "Google Sheets API Java Quickstart";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String CREDENTIALS_FOLDER = "credentials"; // Directory to store user credentials.

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved credentials/ folder.
     */
    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.DRIVE);
    private static final String CLIENT_SECRET_DIR = "client_secret.json";

    /**
     * Creates an authorized Credential object.
     *
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If there is no client_secret.
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        InputStream in = SheetsQuickstart.class.getResourceAsStream(CLIENT_SECRET_DIR);
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(CREDENTIALS_FOLDER)))
                .setAccessType("offline")
                .build();
        return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
    }

    public static void writeDataFromDB(List<List<String>> myData, Sheets service, String spreadsheetId, String writeRange) {

        try {
            List<List<Object>> writeData = new ArrayList<>();
            for (List<String> someData : myData) {
                List<Object> dataRow = new ArrayList<>();
                someData.stream().collect(Collectors.toCollection(() -> dataRow));
                writeData.add(dataRow);
            }

            ValueRange vr = new ValueRange().setValues(writeData).setMajorDimension("ROWS");
            service.spreadsheets().values()
                    .update(spreadsheetId, writeRange, vr)
                    .setValueInputOption("RAW")
                    .execute();
        } catch (Exception e) {
            // handle exception
        }
    }

    /**
     * Prints the names and majors of students in a sample spreadsheet:
     * https://docs.google.com/spreadsheets/d/1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms/edit
     */
    public static void main(String... args) throws IOException, GeneralSecurityException {
        // Build a new authorized API client service.
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        final String spreadsheetId = "1ouf2wg3PthmREGQQ5vP_6Jz0zBykOXQaHOW5Ig5pjjc";
        final String range = "Sheet1!A1:B2670";


        Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();

        List<List<String>> mData = new ArrayList<>();
        String connectionUrl = "jdbc:sqlserver://DP996\\MSSQLSERVER16;databaseName=ResultsData;user=sa;password=1234567";
        String sql;

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

                CallableStatement cstmt = null;
                ResultSet rs = null;
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
                    while (rs.next()) {
                        List<String> dataRow = new ArrayList<>();
                        dataRow.add(rs.getString("ManualTestID"));
                        dataRow.add(rs.getString("Name"));
                        mData.add(dataRow);
                    }
                    mData.add(0, Arrays.asList("FAILED", null));
                    mData.add(1, Arrays.asList("ManualTestID", "Name"));
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


        writeDataFromDB(mData, service, spreadsheetId, range);


//        ValueRange response = service.spreadsheets().values()
//                .get(spreadsheetId, range)
//                .execute();
//        List<List<Object>> values = response.getValues();
//        if (values == null || values.isEmpty()) {
//            System.out.println("No data found.");
//        } else {
//            System.out.println("ManualTestID, FunctionalArea");
//            for (List row : values) {
//                // Print columns A and E, which correspond to indices 0 and 4.
//                System.out.printf("%s, %s\n", row.get(0), row.get(2));
//            }
//        }


    }
}