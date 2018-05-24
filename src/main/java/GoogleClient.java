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
import com.google.api.services.sheets.v4.model.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class GoogleClient {

    private static final String APPLICATION_NAME = "Google Sheets API Java Quickstart";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String CREDENTIALS_FOLDER = "credentials"; // Directory to store user credentials.
    private Sheets service;
    private String endRange;
    private String range;
    private BatchUpdateSpreadsheetResponse response;


    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved credentials/ folder.
     */
    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.DRIVE);
    private static final String CLIENT_SECRET_DIR = "client_secret.json";

    public GoogleClient() throws IOException, GeneralSecurityException {
        this.buildClientService();
    }

    public Sheets getService() {
        return service;
    }

    /**
     * Creates an authorized Credential object.
     *
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If there is no client_secret.
     */

    private Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        InputStream in = PassRateDailyReport.class.getResourceAsStream(CLIENT_SECRET_DIR);
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(CREDENTIALS_FOLDER)))
                .setAccessType("offline")
                .build();
        return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
    }

    Sheets buildClientService() throws IOException, GeneralSecurityException {
        // Build a new authorized API client service.
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        this.service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();
        return this.service;
    }

    public void writeData(List<List<String>> myData, String spreadsheetId, String writeRange) {

        try {
            List<List<Object>> writeData = new ArrayList<>();
            for (List<String> someData : myData) {
                List<Object> dataRow = new ArrayList<>();
                someData.stream().collect(Collectors.toCollection(() -> dataRow));
                writeData.add(dataRow);
            }

            ValueRange vr = new ValueRange().setValues(writeData).setMajorDimension("ROWS");
            this.service.spreadsheets().values()
                    .update(spreadsheetId, writeRange, vr)
                    .setValueInputOption("USER_ENTERED")
                    .execute();
        } catch (Exception e) {
            // handle exception
        }
    }

    public String getEndRange() {
        return this.endRange;
    }

    public String getLetterFromRange(String range) {
        String letterPart;
        letterPart = range.split("\\d+")[0];
        return letterPart;
    }

    public int getNumberFromRange(String range) {
        int letterPart;
        letterPart = Integer.parseInt(range.split("\\D")[1]);
        return letterPart;
    }

    public void setEndRange(String startRange, int countOfColumns, int countOfRows) {
        String erLetterPart = null;
        int srNumberPart;
        srNumberPart = getNumberFromRange(startRange);
        String endRange = null;
        char c = getLetterFromRange(startRange).toCharArray()[0];
        for (int i = 0; i < countOfColumns; i++) {
            erLetterPart = String.valueOf(c++);
        }
        countOfRows += srNumberPart;
        this.endRange = erLetterPart + countOfRows;
    }

    public String getRange(String sheetName, String startRange, String endRange) {
        return this.range = sheetName + "!" + startRange + ":" + endRange;
    }

    public String getRange(String sheetName, String startRange) {
        return this.range = sheetName + "!" + startRange;
    }

    public void setSpreadsheetName(Spreadsheet spreadsheet, String title) {
        SpreadsheetProperties spreadsheetProperties = new SpreadsheetProperties();
        spreadsheetProperties.setTitle(title);
        spreadsheet.setProperties(spreadsheetProperties);
    }


    public void setSheetName(Sheets service, String spreadsheetId, String title) throws IOException {
        List<Request> requests = new ArrayList<>();
        requests.add(new Request().setUpdateSheetProperties(new UpdateSheetPropertiesRequest()
                .setProperties(new SheetProperties().setTitle(title)).setFields("title")));
        requests.add(new Request().setAddSheet(new AddSheetRequest().setProperties(new SheetProperties().setTitle("fff"))));

        BatchUpdateSpreadsheetRequest body = new BatchUpdateSpreadsheetRequest().setRequests(requests);
        BatchUpdateSpreadsheetResponse response = service.spreadsheets().batchUpdate(spreadsheetId, body).execute();
        System.out.println(response.getReplies().get(0));
    }

    public void insertNewSheet(Sheets service, String spreadsheetId, String title) throws IOException {
        Spreadsheet sheetMetaData = service.spreadsheets().get(spreadsheetId).execute();
        List<Sheet> sheets = sheetMetaData.getSheets();
        boolean isNameDuplicate = false;
        for (Sheet sheet : sheets) {
            if (sheet.getProperties().getTitle().contains(title)) {
                isNameDuplicate = true;
                System.out.println(title + " sheet name is already exists.");
                break;
            }
        }
        if (!isNameDuplicate) {
            List<Request> requests = new ArrayList<>();
            requests.add(new Request().setAddSheet(new AddSheetRequest().setProperties(new SheetProperties().setTitle(title).setIndex(0))));
            BatchUpdateSpreadsheetRequest body = new BatchUpdateSpreadsheetRequest().setRequests(requests);
            response = service.spreadsheets().batchUpdate(spreadsheetId, body).execute();
            System.out.println(response.getReplies().get(0));
        }
    }

    public void setTextColor(Sheets service, String spreadsheetId, String sheetTitle, int rowIndex) throws IOException {

        int sheetID = getSheetIDBySheetTitle(service, spreadsheetId, sheetTitle);
        List<Request> requests = new ArrayList<>();
        requests.add(new Request()
                .setRepeatCell(new RepeatCellRequest()
                        .setCell(new CellData()
                                .setUserEnteredFormat(new CellFormat()
                                        .setTextFormat(new TextFormat()
                                                .setForegroundColor(new Color()
                                                        .setRed(Float.valueOf("1"))
                                                        .setGreen(Float.valueOf("0"))
                                                        .setBlue(Float.valueOf("0"))
                                                )
                                                .setFontSize(14)
                                                .setBold(Boolean.TRUE)
                                        )
                                )
                        )
                        .setRange(new GridRange()
                                .setSheetId(sheetID)
                                .setStartRowIndex(rowIndex-1)
                                .setEndRowIndex(rowIndex)
                                .setStartColumnIndex(0)
                                .setEndColumnIndex(1)
                        )
                        .setFields("*")
                )
        );

        BatchUpdateSpreadsheetRequest body = new BatchUpdateSpreadsheetRequest().setRequests(requests);
        BatchUpdateSpreadsheetResponse response = service.spreadsheets().batchUpdate(spreadsheetId, body).execute();
        System.out.println(response.getReplies().get(0));
    }

    public void setNumberFormat(Sheets service, String spreadsheetId, String sheetTitle, int columnIndex, int startRowIndex, int endRowIndex) throws IOException {

        int sheetID = getSheetIDBySheetTitle(service, spreadsheetId, sheetTitle);
        List<Request> requests = new ArrayList<>();
        requests.add(new Request()
                .setRepeatCell(new RepeatCellRequest()
                        .setCell(new CellData()
                                .setUserEnteredFormat(new CellFormat()
                                            .setNumberFormat(new NumberFormat()
                                                .setType("NUMBER")
                                                .setPattern("###0")
                                        )
                                )
                        )
                        .setRange(new GridRange()
                                .setSheetId(sheetID)
                                .setStartRowIndex(startRowIndex)
                                .setEndRowIndex(endRowIndex)
                                .setStartColumnIndex(columnIndex-1)
                                .setEndColumnIndex(columnIndex)
                        )
                        .setFields("*")
                )
        );

        BatchUpdateSpreadsheetRequest body = new BatchUpdateSpreadsheetRequest().setRequests(requests);
        BatchUpdateSpreadsheetResponse response = service.spreadsheets().batchUpdate(spreadsheetId, body).execute();
        System.out.println(response.getReplies().get(0));
    }

    public int getSheetIDBySheetTitle(Sheets service, String spreadsheetId, String sheetTitle) throws IOException {
        Spreadsheet sheetMetaData = service.spreadsheets().get(spreadsheetId).execute();
        List<Sheet> sheets = sheetMetaData.getSheets();
        int sheetID = 0;
        for (Sheet sheet : sheets) {
            if (sheet.getProperties().getTitle().contains(sheetTitle)) {
                sheetID = sheet.getProperties().getSheetId();
            }
        }
        return sheetID;
    }





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
