package Brightcom.reports.data;

import com.opencsv.bean.CsvBindByPosition;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class OverallTraffic extends ReportData {
    @CsvBindByPosition(position = 0)
    private String ad_unit_name;

    @CsvBindByPosition(position = 1)
    private String advertiser_name;

    @CsvBindByPosition(position = 2)
    private String county_name;

    @CsvBindByPosition(position = 3)
    private String date;

    public String getDate() {
        return date;
    }

    public String getCountryName() {
        return county_name;
    }

    public String getAdUnitName() {
        return ad_unit_name;
    }

//    public String getAdUnitId() {
//        return ad_unit_id;
////    }

    public String getSqlString() {
        return "insert into advertisers(date, country_name, ad_unit_name) values (?,?,?)";
    }

    public void updateSQLStatement(PreparedStatement prepStmt) throws SQLException {
        prepStmt.setString(1, this.getDate());
        prepStmt.setString(2, this.getCountryName());
        prepStmt.setString(3, this.getAdUnitName());
    }
}