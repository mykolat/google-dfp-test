package Brightcom;


import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Iterator;

public class ImportCsv {
    public static int BATCH_LIMIT = 1000;

    public static void main(String[] args) throws IOException {
        try (
                Reader reader = Files.newBufferedReader(Paths.get("/tmp/custom-field-report-3904046905745273187.csv"));
                Connection connection = DBConnection.getConnection();
        ) {
            CsvToBean<CSVAdvertiser> csvToBean = new CsvToBeanBuilder(reader)
                    .withType(CSVAdvertiser.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();

            Iterator<CSVAdvertiser> csvUserIterator = csvToBean.iterator();
            csvUserIterator.next();

            PreparedStatement prepStmt = connection.prepareStatement(CSVAdvertiser.getSqlString());

            int i = 1;
            while (csvUserIterator.hasNext() && i++ > 0) {

                CSVAdvertiser advertiser = csvUserIterator.next();

                advertiser.getSQLStatement(prepStmt);
                prepStmt.addBatch();

                if (i % BATCH_LIMIT == 0) {
                    System.out.println("Writing " + i + " rows to DB");
                    prepStmt.executeBatch();
                }
            }
            prepStmt.executeBatch();
            System.out.println(i + " rows added to DB");
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
