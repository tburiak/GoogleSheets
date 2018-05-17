import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SheetsQuickstart {

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

    public static void main(String... args) throws IOException, GeneralSecurityException {

        String conUrl = "jdbc:sqlserver://DP996\\MSSQLSERVER16;databaseName=ResultsData;user=sa;password=1234567";
        Sheets service = new GoogleClient().buildClientService();
        List<List<String>> body = new SQLClient(conUrl).extractDataFromDB();

        final String spreadsheetId = "1ouf2wg3PthmREGQQ5vP_6Jz0zBykOXQaHOW5Ig5pjjc";
        final String range = "Sheet1!A1:P2670";

        writeDataFromDB(body, service, spreadsheetId, range);


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