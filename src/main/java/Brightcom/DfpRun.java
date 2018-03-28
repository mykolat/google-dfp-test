// Copyright 2015 Google Inc. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package Brightcom;

import com.beust.jcommander.Parameter;
import com.google.api.ads.common.lib.auth.OfflineCredentials;
import com.google.api.ads.common.lib.auth.OfflineCredentials.Api;
import com.google.api.ads.common.lib.conf.ConfigurationLoadException;
import com.google.api.ads.common.lib.exception.OAuthException;
import com.google.api.ads.common.lib.exception.ValidationException;
import com.google.api.ads.common.lib.utils.examples.CodeSampleParams;
import com.google.api.ads.dfp.axis.factory.DfpServices;
import com.google.api.ads.dfp.axis.utils.v201802.ReportDownloader;
import com.google.api.ads.dfp.axis.v201802.*;
import com.google.api.ads.dfp.lib.client.DfpSession;
import com.google.api.ads.dfp.lib.utils.examples.ArgumentNames;
import com.google.api.client.auth.oauth2.Credential;
import com.google.common.io.Files;
import com.google.common.io.Resources;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;

import static com.google.api.ads.common.lib.utils.Builder.DEFAULT_CONFIGURATION_FILENAME;

public class DfpRun {

    public static void main(String[] args) {
        DfpSession session;
        try {
            // Generate a refreshable OAuth2 credential.
            Credential oAuth2Credential =
                    new OfflineCredentials.Builder()
                            .forApi(Api.DFP)
                            .fromFile()
                            .build()
                            .generateCredential();

            // Construct a DfpSession.
            session =
                    new DfpSession.Builder().fromFile().withOAuth2Credential(oAuth2Credential).build();
        } catch (ConfigurationLoadException cle) {
            System.err.printf(
                    "Failed to load configuration from the %s file. Exception: %s%n",
                    DEFAULT_CONFIGURATION_FILENAME, cle);
            return;
        } catch (ValidationException ve) {
            System.err.printf(
                    "Invalid configuration in the %s file. Exception: %s%n",
                    DEFAULT_CONFIGURATION_FILENAME, ve);
            return;
        } catch (OAuthException oe) {
            System.err.printf(
                    "Failed to create OAuth credentials. Check OAuth settings in the %s file. "
                            + "Exception: %s%n",
                    DEFAULT_CONFIGURATION_FILENAME, oe);
            return;
        }

        DfpServices dfpServices = new DfpServices();

        RunReportWithCustomFieldsParams params = new RunReportWithCustomFieldsParams();
        if (!params.parseArguments(args)) {
            // Either pass the required parameters for this example on the command line, or insert them
            // into the code here. See the parameter class definition above for descriptions.
            params.customFieldId = Long.parseLong("123");
        }

        try {
            ArrayList<ReportQuery> reportArray = new ArrayList<>();
            reportArray.add(getFirstRep());
            //reportArray.add(getSecondRep());
            //reportArray.add(getThirdRep());
            //reportArray.add(getFourthRep());
            //reportArray.add(getFifthRep());
            //reportArray.add(getSixthRep());

            for (ReportQuery report : reportArray) {
                runQuery(dfpServices, session, report, params.customFieldId);
            }
        } catch (ApiException apiException) {
            // ApiException is the base class for most exceptions thrown by an API request. Instances
            // of this exception have a message and a collection of ApiErrors that indicate the
            // type and underlying cause of the exception. Every exception object in the dfp.axis
            // packages will return a meaningful value from toString
            //
            // ApiException extends RemoteException, so this catch block must appear before the
            // catch block for RemoteException.
            System.err.println("Request failed due to ApiException. Underlying ApiErrors:");
            if (apiException.getErrors() != null) {
                int i = 0;
                for (ApiError apiError : apiException.getErrors()) {
                    System.err.printf("  Error %d: %s%n", i++, apiError);
                }
            }
        } catch (RemoteException re) {
            System.err.printf("Request failed unexpectedly due to RemoteException: %s%n", re);
        } catch (IOException ioe) {
            System.err.printf("Example failed due to IOException: %s%n", ioe);
        } catch (InterruptedException ie) {
            System.err.printf("Thread was interrupted: %s%n", ie);
        }
    }

    /**
     * This example runs a report that includes a custom field. To determine which
     * custom fields exist, run GetAllCustomFields.java.
     * <p>
     * Credentials and properties in {@code fromFile()} are pulled from the
     * "ads.properties" file. See README for more info.
     */

    private static class RunReportWithCustomFieldsParams extends CodeSampleParams {
        @Parameter(names = ArgumentNames.CUSTOM_FIELD_ID, required = true,
                description = "The ID of the custom field to include in the report.")
        private Long customFieldId;
    }

    /**
     * Runs the example.
     *
     * @param dfpServices   the services factory.
     * @param session       the session.
     * @param customFieldId the ID of the custom field to include in the report.
     * @throws ApiException         if the API request failed with one or more service errors.
     * @throws RemoteException      if the API request failed due to other errors.
     * @throws IOException          if unable to write the response to a file.
     * @throws InterruptedException if the thread is interrupted while waiting for the report to
     *                              complete.
     */
    public static void runQuery(DfpServices dfpServices, DfpSession session, ReportQuery reportQuery, long customFieldId)
            throws IOException, InterruptedException {
        // Get the ReportService.
        ReportServiceInterface reportService = dfpServices.get(session, ReportServiceInterface.class);

        // Create report job.
        ReportJob reportJob = new ReportJob();
        reportJob.setReportQuery(reportQuery);

        // Run report job.
        reportJob = reportService.runReportJob(reportJob);

        // Create report downloader.
        ReportDownloader reportDownloader = new ReportDownloader(reportService, reportJob.getId());

        // Wait for the report to be ready.
        reportDownloader.waitForReportReady();

        // Change to your file location.
        File file = File.createTempFile("custom-field-report-", ".csv");

        System.out.printf("Downloading report to %s ...", file.toString());

        // Download the report.
        ReportDownloadOptions options = new ReportDownloadOptions();
        options.setExportFormat(ExportFormat.CSV_DUMP);
        options.setUseGzipCompression(false);
        URL url = reportDownloader.getDownloadUrl(options);

        Resources.asByteSource(url).copyTo(Files.asByteSink(file));

        System.out.println("done.");
    }


    private static ReportQuery getFirstRep() {//Advertisers
        // Create report query.
        ReportQuery reportQuery = new ReportQuery();
        reportQuery.setDimensions(new Dimension[]{Dimension.DATE, Dimension.COUNTRY_NAME, Dimension.AD_UNIT_NAME,
                Dimension.AD_UNIT_ID, Dimension.ADVERTISER_ID, Dimension.ADVERTISER_NAME});
        reportQuery.setColumns(new Column[]{Column.TOTAL_CODE_SERVED_COUNT, Column.TOTAL_LINE_ITEM_LEVEL_IMPRESSIONS, Column.TOTAL_LINE_ITEM_LEVEL_CLICKS,
                Column.TOTAL_LINE_ITEM_LEVEL_CPM_AND_CPC_REVENUE, Column.TOTAL_LINE_ITEM_LEVEL_WITHOUT_CPD_AVERAGE_ECPM, Column.TOTAL_LINE_ITEM_LEVEL_CTR,
                Column.TOTAL_ACTIVE_VIEW_ELIGIBLE_IMPRESSIONS, Column.TOTAL_ACTIVE_VIEW_MEASURABLE_IMPRESSIONS, Column.TOTAL_ACTIVE_VIEW_VIEWABLE_IMPRESSIONS,
                Column.TOTAL_ACTIVE_VIEW_MEASURABLE_IMPRESSIONS_RATE, Column.TOTAL_ACTIVE_VIEW_VIEWABLE_IMPRESSIONS_RATE, Column.AD_EXCHANGE_LINE_ITEM_LEVEL_IMPRESSIONS,
                Column.AD_EXCHANGE_LINE_ITEM_LEVEL_TARGETED_IMPRESSIONS, Column.AD_EXCHANGE_LINE_ITEM_LEVEL_CLICKS, Column.AD_EXCHANGE_LINE_ITEM_LEVEL_TARGETED_CLICKS,
                Column.AD_EXCHANGE_LINE_ITEM_LEVEL_CTR, Column.AD_EXCHANGE_LINE_ITEM_LEVEL_REVENUE, Column.AD_EXCHANGE_LINE_ITEM_LEVEL_AVERAGE_ECPM,
                Column.AD_EXCHANGE_LINE_ITEM_LEVEL_PERCENT_IMPRESSIONS, Column.AD_EXCHANGE_LINE_ITEM_LEVEL_PERCENT_CLICKS, Column.AD_EXCHANGE_LINE_ITEM_LEVEL_WITHOUT_CPD_PERCENT_REVENUE,
                Column.AD_EXCHANGE_ACTIVE_VIEW_ELIGIBLE_IMPRESSIONS, Column.AD_EXCHANGE_ACTIVE_VIEW_MEASURABLE_IMPRESSIONS, Column.AD_EXCHANGE_ACTIVE_VIEW_VIEWABLE_IMPRESSIONS,
                Column.AD_EXCHANGE_ACTIVE_VIEW_MEASURABLE_IMPRESSIONS_RATE, Column.AD_EXCHANGE_ACTIVE_VIEW_VIEWABLE_IMPRESSIONS_RATE, Column.VIDEO_VIEWERSHIP_START,
                Column.VIDEO_VIEWERSHIP_FIRST_QUARTILE, Column.VIDEO_VIEWERSHIP_MIDPOINT, Column.VIDEO_VIEWERSHIP_THIRD_QUARTILE,
                Column.VIDEO_VIEWERSHIP_COMPLETE, Column.VIDEO_VIEWERSHIP_AVERAGE_VIEW_RATE, Column.VIDEO_VIEWERSHIP_AVERAGE_VIEW_TIME,
                Column.VIDEO_VIEWERSHIP_COMPLETION_RATE, Column.VIDEO_VIEWERSHIP_TOTAL_ERROR_COUNT,
                Column.VIDEO_VIEWERSHIP_VIDEO_LENGTH, Column.VIDEO_VIEWERSHIP_AUTO_PLAYS, Column.VIDEO_VIEWERSHIP_CLICK_TO_PLAYS,
                Column.VIDEO_ERRORS_VAST_ERROR_100_COUNT, Column.VIDEO_ERRORS_VAST_ERROR_101_COUNT, Column.VIDEO_ERRORS_VAST_ERROR_102_COUNT,
                Column.VIDEO_ERRORS_VAST_ERROR_200_COUNT, Column.VIDEO_ERRORS_VAST_ERROR_201_COUNT, Column.VIDEO_ERRORS_VAST_ERROR_202_COUNT,
                Column.VIDEO_ERRORS_VAST_ERROR_203_COUNT, Column.VIDEO_ERRORS_VAST_ERROR_300_COUNT, Column.VIDEO_ERRORS_VAST_ERROR_301_COUNT,
                Column.VIDEO_ERRORS_VAST_ERROR_302_COUNT, Column.VIDEO_ERRORS_VAST_ERROR_303_COUNT, Column.VIDEO_ERRORS_VAST_ERROR_400_COUNT,
                Column.VIDEO_ERRORS_VAST_ERROR_401_COUNT, Column.VIDEO_ERRORS_VAST_ERROR_402_COUNT, Column.VIDEO_ERRORS_VAST_ERROR_403_COUNT,
                Column.VIDEO_ERRORS_VAST_ERROR_405_COUNT, Column.VIDEO_ERRORS_VAST_ERROR_500_COUNT, Column.VIDEO_ERRORS_VAST_ERROR_501_COUNT,
                Column.VIDEO_ERRORS_VAST_ERROR_502_COUNT, Column.VIDEO_ERRORS_VAST_ERROR_503_COUNT, Column.VIDEO_ERRORS_VAST_ERROR_600_COUNT,
                Column.VIDEO_ERRORS_VAST_ERROR_601_COUNT, Column.VIDEO_ERRORS_VAST_ERROR_602_COUNT, Column.VIDEO_ERRORS_VAST_ERROR_603_COUNT,
                Column.VIDEO_ERRORS_VAST_ERROR_604_COUNT, Column.VIDEO_ERRORS_VAST_ERROR_900_COUNT, Column.VIDEO_ERRORS_VAST_ERROR_901_COUNT});

        reportQuery.setDimensionAttributes(new DimensionAttribute[]{});

        // Set the dynamic date range type or a custom start and end date.
        reportQuery.setDateRangeType(DateRangeType.YESTERDAY);
        return reportQuery;
//    reportQuery.setStartDate(
//            DateTimes.toDateTime("2018-03-24T00:00:00", "America/New_York").getDate());
//    reportQuery.setEndDate(
//            DateTimes.toDateTime("2018-03-24T23:59:59", "America/New_York").getDate());
//

    }

    private static ReportQuery getSecondRep() {//Overall_Traffic
        // Create report query.
        ReportQuery reportQuery = new ReportQuery();
        reportQuery.setDimensions(new Dimension[]{Dimension.DATE, Dimension.COUNTRY_NAME, Dimension.AD_UNIT_NAME,
                Dimension.AD_UNIT_ID});
        reportQuery.setColumns(new Column[]{Column.TOTAL_CODE_SERVED_COUNT, Column.TOTAL_INVENTORY_LEVEL_UNFILLED_IMPRESSIONS, Column.TOTAL_LINE_ITEM_LEVEL_IMPRESSIONS,
                Column.TOTAL_LINE_ITEM_LEVEL_CLICKS, Column.TOTAL_LINE_ITEM_LEVEL_CPM_AND_CPC_REVENUE, Column.TOTAL_LINE_ITEM_LEVEL_WITHOUT_CPD_AVERAGE_ECPM,
                Column.TOTAL_LINE_ITEM_LEVEL_CTR, Column.TOTAL_ACTIVE_VIEW_ELIGIBLE_IMPRESSIONS, Column.TOTAL_ACTIVE_VIEW_MEASURABLE_IMPRESSIONS,
                Column.TOTAL_ACTIVE_VIEW_VIEWABLE_IMPRESSIONS, Column.TOTAL_ACTIVE_VIEW_MEASURABLE_IMPRESSIONS_RATE, Column.TOTAL_ACTIVE_VIEW_VIEWABLE_IMPRESSIONS_RATE,
                Column.AD_SERVER_LINE_ITEM_LEVEL_PERCENT_IMPRESSIONS, Column.AD_SERVER_IMPRESSIONS, Column.AD_SERVER_DOWNLOADED_IMPRESSIONS,
                Column.AD_SERVER_CLICKS, Column.AD_SERVER_WITHOUT_CPD_AVERAGE_ECPM, Column.AD_SERVER_CTR,
                Column.AD_SERVER_CPM_AND_CPC_REVENUE, Column.AD_SERVER_LINE_ITEM_LEVEL_PERCENT_CLICKS,
                Column.AD_SERVER_LINE_ITEM_LEVEL_WITHOUT_CPD_PERCENT_REVENUE, Column.AD_SERVER_ACTIVE_VIEW_MEASURABLE_IMPRESSIONS,
                Column.AD_SERVER_ACTIVE_VIEW_VIEWABLE_IMPRESSIONS, Column.AD_SERVER_ACTIVE_VIEW_MEASURABLE_IMPRESSIONS_RATE,
                Column.AD_SERVER_ACTIVE_VIEW_VIEWABLE_IMPRESSIONS_RATE, Column.AD_SERVER_ACTIVE_VIEW_ELIGIBLE_IMPRESSIONS,
                Column.AD_EXCHANGE_LINE_ITEM_LEVEL_IMPRESSIONS, Column.AD_EXCHANGE_LINE_ITEM_LEVEL_TARGETED_IMPRESSIONS, Column.AD_EXCHANGE_LINE_ITEM_LEVEL_CLICKS,
                Column.AD_EXCHANGE_LINE_ITEM_LEVEL_TARGETED_CLICKS, Column.AD_EXCHANGE_LINE_ITEM_LEVEL_CTR, Column.AD_EXCHANGE_LINE_ITEM_LEVEL_REVENUE,
                Column.AD_EXCHANGE_LINE_ITEM_LEVEL_AVERAGE_ECPM, Column.AD_EXCHANGE_LINE_ITEM_LEVEL_PERCENT_IMPRESSIONS,
                Column.AD_EXCHANGE_LINE_ITEM_LEVEL_PERCENT_CLICKS, Column.AD_EXCHANGE_LINE_ITEM_LEVEL_WITHOUT_CPD_PERCENT_REVENUE,
                Column.AD_EXCHANGE_ACTIVE_VIEW_ELIGIBLE_IMPRESSIONS, Column.AD_EXCHANGE_ACTIVE_VIEW_MEASURABLE_IMPRESSIONS, Column.AD_EXCHANGE_ACTIVE_VIEW_VIEWABLE_IMPRESSIONS,
                Column.AD_EXCHANGE_ACTIVE_VIEW_MEASURABLE_IMPRESSIONS_RATE, Column.AD_EXCHANGE_ACTIVE_VIEW_VIEWABLE_IMPRESSIONS_RATE,
                Column.VIDEO_VIEWERSHIP_START, Column.VIDEO_VIEWERSHIP_FIRST_QUARTILE, Column.VIDEO_VIEWERSHIP_MIDPOINT,
                Column.VIDEO_VIEWERSHIP_THIRD_QUARTILE, Column.VIDEO_VIEWERSHIP_COMPLETE,
                Column.VIDEO_VIEWERSHIP_AVERAGE_VIEW_RATE, Column.VIDEO_VIEWERSHIP_AVERAGE_VIEW_TIME,
                Column.VIDEO_VIEWERSHIP_COMPLETION_RATE, Column.VIDEO_VIEWERSHIP_TOTAL_ERROR_COUNT, Column.VIDEO_VIEWERSHIP_TOTAL_ERROR_RATE,
                Column.VIDEO_VIEWERSHIP_VIDEO_LENGTH, Column.VIDEO_VIEWERSHIP_AUTO_PLAYS, Column.VIDEO_VIEWERSHIP_CLICK_TO_PLAYS,
                Column.VIDEO_ERRORS_VAST_ERROR_100_COUNT, Column.VIDEO_ERRORS_VAST_ERROR_101_COUNT, Column.VIDEO_ERRORS_VAST_ERROR_102_COUNT,
                Column.VIDEO_ERRORS_VAST_ERROR_200_COUNT, Column.VIDEO_ERRORS_VAST_ERROR_201_COUNT, Column.VIDEO_ERRORS_VAST_ERROR_202_COUNT,
                Column.VIDEO_ERRORS_VAST_ERROR_203_COUNT, Column.VIDEO_ERRORS_VAST_ERROR_300_COUNT, Column.VIDEO_ERRORS_VAST_ERROR_301_COUNT,
                Column.VIDEO_ERRORS_VAST_ERROR_302_COUNT, Column.VIDEO_ERRORS_VAST_ERROR_303_COUNT, Column.VIDEO_ERRORS_VAST_ERROR_400_COUNT,
                Column.VIDEO_ERRORS_VAST_ERROR_401_COUNT, Column.VIDEO_ERRORS_VAST_ERROR_402_COUNT, Column.VIDEO_ERRORS_VAST_ERROR_403_COUNT,
                Column.VIDEO_ERRORS_VAST_ERROR_405_COUNT, Column.VIDEO_ERRORS_VAST_ERROR_500_COUNT, Column.VIDEO_ERRORS_VAST_ERROR_501_COUNT,
                Column.VIDEO_ERRORS_VAST_ERROR_502_COUNT, Column.VIDEO_ERRORS_VAST_ERROR_503_COUNT, Column.VIDEO_ERRORS_VAST_ERROR_600_COUNT,
                Column.VIDEO_ERRORS_VAST_ERROR_601_COUNT, Column.VIDEO_ERRORS_VAST_ERROR_602_COUNT, Column.VIDEO_ERRORS_VAST_ERROR_603_COUNT,
                Column.VIDEO_ERRORS_VAST_ERROR_604_COUNT, Column.VIDEO_ERRORS_VAST_ERROR_900_COUNT, Column.VIDEO_ERRORS_VAST_ERROR_901_COUNT});

        reportQuery.setDimensionAttributes(new DimensionAttribute[]{});

        // Set the dynamic date range type or a custom start and end date.
        reportQuery.setDateRangeType(DateRangeType.YESTERDAY);
        return reportQuery;

    }

    private static ReportQuery getThirdRep() {// Ad_Exchange_Ad_Units
        // Create report query.
        ReportQuery reportQuery = new ReportQuery();
        reportQuery.setDimensions(new Dimension[]{Dimension.AD_EXCHANGE_DFP_AD_UNIT, Dimension.AD_EXCHANGE_DFP_AD_UNIT_ID,
                Dimension.AD_EXCHANGE_COUNTRY_NAME});
        reportQuery.setColumns(new Column[]{Column.AD_EXCHANGE_MATCHED_REQUESTS, Column.AD_EXCHANGE_COVERAGE,
                Column.AD_EXCHANGE_CLICKS, Column.AD_EXCHANGE_CTR, Column.AD_EXCHANGE_LIFT, Column.AD_EXCHANGE_ESTIMATED_REVENUE});

        reportQuery.setDimensionAttributes(new DimensionAttribute[]{});

        // Set the dynamic date range type or a custom start and end date.
        reportQuery.setDateRangeType(DateRangeType.YESTERDAY);
        return reportQuery;
//    reportQuery.setStartDate(
//            DateTimes.toDateTime("2018-03-24T00:00:00", "America/New_York").getDate());
//    reportQuery.setEndDate(
//            DateTimes.toDateTime("2018-03-24T23:59:59", "America/New_York").getDate());
//

    }

    private static ReportQuery getFourthRep() {//Adx_Buyers
        // Create report query.
        ReportQuery reportQuery = new ReportQuery();
        reportQuery.setDimensions(new Dimension[]{Dimension.AD_EXCHANGE_DFP_AD_UNIT, Dimension.AD_EXCHANGE_DFP_AD_UNIT_ID, Dimension.AD_EXCHANGE_BUYER_NETWORK_NAME,
                Dimension.AD_EXCHANGE_ADVERTISER_NAME, Dimension.AD_EXCHANGE_COUNTRY_NAME});
        reportQuery.setColumns(new Column[]{Column.AD_EXCHANGE_COVERAGE,
                Column.AD_EXCHANGE_CLICKS, Column.AD_EXCHANGE_CTR, Column.AD_EXCHANGE_LIFT, Column.AD_EXCHANGE_ESTIMATED_REVENUE});

        reportQuery.setDimensionAttributes(new DimensionAttribute[]{});

        // Set the dynamic date range type or a custom start and end date.
        reportQuery.setDateRangeType(DateRangeType.YESTERDAY);
        return reportQuery;
//    reportQuery.setStartDate(
//            DateTimes.toDateTime("2018-03-24T00:00:00", "America/New_York").getDate());
//    reportQuery.setEndDate(
//            DateTimes.toDateTime("2018-03-24T23:59:59", "America/New_York").getDate());
//

    }

    private static ReportQuery getFifthRep() {//Adx_Pricing_Rules
        // Create report query.
        ReportQuery reportQuery = new ReportQuery();
        reportQuery.setDimensions(new Dimension[]{Dimension.AD_EXCHANGE_DFP_AD_UNIT, Dimension.AD_EXCHANGE_DFP_AD_UNIT_ID,
                Dimension.AD_UNIT_ID, Dimension.AD_EXCHANGE_COUNTRY_NAME, Dimension.AD_EXCHANGE_PRICING_RULE_NAME, Dimension.AD_EXCHANGE_BRANDING_TYPE,
                Dimension.AD_EXCHANGE_BRANDING_TYPE_CODE});
        reportQuery.setColumns(new Column[]{Column.AD_EXCHANGE_COVERAGE, Column.AD_EXCHANGE_CLICKS, Column.AD_EXCHANGE_CTR, Column.AD_EXCHANGE_LIFT,
                Column.AD_EXCHANGE_ESTIMATED_REVENUE});

        reportQuery.setDimensionAttributes(new DimensionAttribute[]{});

        // Set the dynamic date range type or a custom start and end date.
        reportQuery.setDateRangeType(DateRangeType.YESTERDAY);
        return reportQuery;
//    reportQuery.setStartDate(
//            DateTimes.toDateTime("2018-03-24T00:00:00", "America/New_York").getDate());
//    reportQuery.setEndDate(
//            DateTimes.toDateTime("2018-03-24T23:59:59", "America/New_York").getDate());
//

    }

    private static ReportQuery getSixthRep() {//AdX_Bid_requests
        // Create report query.
        ReportQuery reportQuery = new ReportQuery();
        reportQuery.setDimensions(new Dimension[]{Dimension.AD_EXCHANGE_COUNTRY_NAME, Dimension.AD_EXCHANGE_BID_TYPE_CODE,
                Dimension.AD_EXCHANGE_CREATIVE_SIZES});
        reportQuery.setColumns(new Column[]{Column.AD_EXCHANGE_AD_ECPM});

        reportQuery.setDimensionAttributes(new DimensionAttribute[]{});

        // Set the dynamic date range type or a custom start and end date.
        reportQuery.setDateRangeType(DateRangeType.YESTERDAY);
        return reportQuery;
    }
}
