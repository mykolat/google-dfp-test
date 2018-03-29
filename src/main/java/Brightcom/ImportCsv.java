package Brightcom;


import Brightcom.reports.data.Advertiser;
import Brightcom.reports.data.ReportData;
import com.google.common.io.ByteSource;
import com.google.common.io.Resources;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Iterator;


public class ImportCsv {
    public static int BATCH_LIMIT = 1000;

    public static <T extends ReportData> void exportToDb(URL url) {
        ByteSource byteSource = Resources.asByteSource(url);

        try (

                InputStream targetStream = byteSource.openStream();
                Reader reader = new InputStreamReader(targetStream);
                Connection connection = DBConnection.getConnection();
        ) {
            CsvToBean<Advertiser> csvToBean = new CsvToBeanBuilder(reader)
                    .withType(Advertiser.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();

            //exportAdvertisers(csvToBean.iterator(), connection);
            Iterator<T> csvUserIterator = (Iterator<T>) csvToBean.iterator();

            //Cant use static method with generic type
            T title = csvUserIterator.next();
            PreparedStatement prepStmt = connection.prepareStatement(title.getSqlString());

            int i = 1;
            while (csvUserIterator.hasNext() && i++ > 0) {

                T advertiser = csvUserIterator.next();

                advertiser.updateSQLStatement(prepStmt);
                prepStmt.addBatch();

                if (i % BATCH_LIMIT == 0) {
                    System.out.println("Writing " + i + " rows to DB");
                    prepStmt.executeBatch();
                }
            }
            prepStmt.executeBatch();
            System.out.println(i + " rows added to DB");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
//
//    private static void exportAdvertisers(Iterator<T> csvUserIterator, Connection connection) throws SQLException {
//        csvUserIterator.next();
//
//        PreparedStatement prepStmt = connection.prepareStatement(Advertiser.getSqlString());
//        int i = 1;
//        while (csvUserIterator.hasNext() && i++ > 0) {
//
//            Advertiser advertiser = csvUserIterator.next();
//
//            advertiser.updateSQLStatement(prepStmt);
//            prepStmt.addBatch();
//
//            if (i % BATCH_LIMIT == 0) {
//                System.out.println("Writing " + i + " rows to DB");
//                prepStmt.executeBatch();
//            }
//        }
//        prepStmt.executeBatch();
//        System.out.println(i + " rows added to DB");
//    }
}
