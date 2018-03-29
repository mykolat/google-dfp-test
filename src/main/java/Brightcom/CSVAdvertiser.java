package Brightcom;

import com.opencsv.bean.CsvBindByPosition;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class CSVAdvertiser {
    @CsvBindByPosition(position = 0)
    private String ad_unit_name;

    @CsvBindByPosition(position = 1)
    private String county_name;

    @CsvBindByPosition(position = 2)
    private String date;

    @CsvBindByPosition(position = 3)
    private String ad_unit_id;

    public String getDate() {
        return date;
    }

    public String getCountryName() {
        return county_name;
    }

    public String getAdUnitName() {
        return ad_unit_name;
    }

    public String getAdUnitId() {
        return ad_unit_id;
    }

    public static String getSqlString() {
        return "insert into advertisers(ad_unit_name, country_name, date) values (?,?,?)";
    }

    public void getSQLStatement(PreparedStatement prepStmt) throws SQLException {
        prepStmt.setString(1, this.getAdUnitName());
        prepStmt.setString(2, this.getCountryName());
        prepStmt.setString(3, this.getDate());
    }

    // Getters and Setters (Omitted for brevity)    
}