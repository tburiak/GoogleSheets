import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

public class PassRateDailyReport {

    public static void main(String... args) throws IOException, GeneralSecurityException {

        final String conUrl = "jdbc:sqlserver://DP996\\MSSQLSERVER16;databaseName=ResultsData;user=sa;password=1234567";
        final String spreadsheetId = "1ouf2wg3PthmREGQQ5vP_6Jz0zBykOXQaHOW5Ig5pjjc";

        String startRangeTitleFail;
        String startRangeTitleUpdate;
        String endRangeFail;
        String startRangeFail;
        String rangeForFailed;
        String rangeForFailedTitle;

        String startRangeUpdate;
        String endRangeUpdate;
        String rangeForUpdate;
        String rangeForUpdateTitle;

        GoogleClient googleClient = new GoogleClient();
        SQLClient sqlClient = new SQLClient(conUrl);
        String sheetTitle = LocalDate.now().format(DateTimeFormatter.ofPattern("MM/dd"));
        googleClient.insertNewSheet(googleClient.getService(), spreadsheetId, sheetTitle);

        List<List<String>> bodyFail = sqlClient.extractDataFromDB("fail");
        startRangeTitleFail = "A1";
        rangeForFailedTitle = googleClient.getRange(sheetTitle, startRangeTitleFail);
        googleClient.setTextColor(googleClient.getService(), spreadsheetId, sheetTitle, googleClient.getNumberFromRange(startRangeTitleFail));
        googleClient.writeData(Arrays.asList(Arrays.asList("FAILED/BLOCKED")), spreadsheetId, rangeForFailedTitle);

        startRangeFail = googleClient.getLetterFromRange(startRangeTitleFail) + (googleClient.getNumberFromRange(startRangeTitleFail) + 1);
        googleClient.setEndRange(startRangeFail, sqlClient.getCountOfColumns(), sqlClient.getCountOfRows());
        endRangeFail = googleClient.getEndRange();
        rangeForFailed = googleClient.getRange(sheetTitle, startRangeFail, endRangeFail);
        googleClient.writeData(bodyFail, spreadsheetId, rangeForFailed);

        List<List<String>> bodyUpd = sqlClient.extractDataFromDB("upd");
        startRangeTitleUpdate = googleClient.getLetterFromRange(startRangeTitleFail) + (googleClient.getNumberFromRange(endRangeFail) + 2);
        rangeForUpdateTitle = googleClient.getRange(sheetTitle, startRangeTitleUpdate);
        googleClient.setTextColor(googleClient.getService(), spreadsheetId, sheetTitle, googleClient.getNumberFromRange(startRangeTitleUpdate));
        googleClient.writeData(Arrays.asList(Arrays.asList("NEEDS UPDATE")), spreadsheetId, rangeForUpdateTitle);

        startRangeUpdate = googleClient.getLetterFromRange(startRangeTitleUpdate) + (googleClient.getNumberFromRange(startRangeTitleUpdate) + 1 );
        googleClient.setEndRange(startRangeUpdate, sqlClient.getCountOfColumns(), sqlClient.getCountOfRows());
        endRangeUpdate = googleClient.getEndRange();
        rangeForUpdate = googleClient.getRange(sheetTitle, startRangeUpdate, endRangeUpdate);
        googleClient.writeData(bodyUpd, spreadsheetId, rangeForUpdate);

    }
}