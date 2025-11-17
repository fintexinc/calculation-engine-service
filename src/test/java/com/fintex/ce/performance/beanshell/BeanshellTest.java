package com.fintex.ce.performance.beanshell;


import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertTrue;

class BeanshellTest {

    String[] mutualFunds = {"RBF460", "RBF5658", "RBF546", "RBF658", "RBF101", "RBF252", "RBF4517", "RBF517", "RBF5517", "RBF461", "RBF5657", "RBF545", "RBF657", "RBF100", "RBF254", "RBF516", "RBF4516", "RBF5516", "PMO505", "PMO2505", "PMO2405", "PMO2205", "PMO2005", "PMO605", "PMO205", "PMO405", "PMO005", "RBF1005", "RBF270", "RBF114", "RBF137", "RBF601", "TDB2921", "TDB3081", "TDB030", "TDB107", "TDB969", "TDB3065", "TDB406", "TDB2002"};
    String[] canadaEtf = {"PMIF", "PMIF.U", "XIU", "ZSP", "ZSP.U", "ZAG", "XIC", "XSP", "XBB", "ZCPB", "RIFI", "XEF", "ZCN", "ZEA", "VAB", "VFV", "XSB", "CSAV", "VUN", "PSA", "ZLB", "XCB", "VCN", "XUS", "ZFL", "HXT", "ZIC.U", "ZIC", "NUBF", "HXS", "RIIN", "ZLU.U", "ZLU", "ZUE", "ZEM", "ISIF", "ZPR", "HBB", "ZWB", "ZMU", "IGAF", "VIU", "VSB", "ZST.L", "ZST", "VSC", "ZCS.L", "ZCS", "VGRO", "XAW", "XAW.U", "ZDB", "ZCM", "XUU", "XUU.U", "XDV", "XIN", "CBO", "VBAL", "VSP", "ZEB", "XRE", "VEE", "IFRF", "XGD", "HPR", "ZWP", "XFN", "NHYB", "CPD", "ZWH.U", "ZWH", "ZFS.L", "ZFS", "XGB", "CGL", "CGL.C", "XLB", "HXCN", "XSH", "VXC", "PSB"};
    String[] usEtf = {"MLPE", "VTI", "VOO", "VXUS", "BND", "SPY", "IVV", "BNDX", "VEA", "VO", "VUG", "QQQ", "VB", "VWO", "VTV", "AGG", "VXF", "VNQ", "IEFA", "BSV", "EFA", "GLD", "VIG", "IEMG", "IWF", "BIV", "IJH", "VEU", "VTIP", "IWM", "LQD", "VYM", "IJR", "USMV", "IWD", "VCSH", "VCIT", "VGT", "VBR", "SHV", "SHY", "XLK", "VV", "MBB", "VBK", "IEF", "ITOT", "TIP", "IAU", "IVW", "EEM", "TLT", "BIL", "DIA", "IWB", "XLV", "VOE", "GOVT", "VT", "SCHF", "SCHX", "VGK", "IWR", "MUB", "XLF", "QUAL", "IXUS", "SDY", "HYG", "VOT", "BLV", "XLP", "VMBS", "IGSB", "PFF", "SCHB", "IVE", "DVY", "MINT", "MDY", "IEI", "GDX", "EMB", "RSP", "VGSH", "XLY", "EFAV", "JPST", "XLU", "VHT", "SCHD", "VGIT", "SCHP", "IWP", "ACWI", "SPLV", "EWJ", "FLOT", "DGRO", "IGIB", "IWV"};


    JSONObject addObject(JSONObject root, String key, String value) {
        root.put(key, value);
        return root;
    }

    JSONObject addArray(JSONObject root, String key, String[] values) {
        JSONArray valuesJson = new JSONArray();
        for (String pt : values) {
            valuesJson.put(pt);
        }
        root.put(key, valuesJson);
        return root;
    }

    JSONObject addDataProviders(JSONObject root, String[] dataProvider) {
        return addArray(root, "dataProviders", dataProvider);
    }

    JSONObject addParameterTypes(JSONObject root, String[] parameterType) {
        return addArray(root, "parameterTypes", parameterType);
    }

    JSONObject addCustomIntervalPerformanceStartDate(JSONObject root, String value) {
        return addObject(root, "customIntervalPerformanceStartDate", value);
    }

    JSONObject addCustomPerformanceStartDate(JSONObject root, String value) {
        return addObject(root, "customPerformanceStartDate", value);
    }

    JSONObject addCustomPerformanceEndDate(JSONObject root, String value) {
        return addObject(root, "customPerformanceEndDate", value);
    }

    JSONObject addCurrency(JSONObject root, String value) {
        return addObject(root, "currency", value);
    }

    JSONObject addTimeIntervalPeriods(JSONObject root, String[] timeIntervals) {
        return addArray(root, "timeIntervalPeriods", timeIntervals);
    }

    JSONObject addBestWorstTimeIntervalPeriods(JSONObject root, String[] timeIntervals) {
        return addArray(root, "bestWorstTimeIntervalPeriods", timeIntervals);
    }

    JSONArray addMutualFund(JSONArray holdings, String holdingIdentifier,
                                   String fundServCode, int value) {
        JSONObject holding = new JSONObject();
        holding.put("type", "CANADA_MUTUAL_FUNDS");
        holding.put("holdingIdentifier", holdingIdentifier);
        holding.put("fundServCode", fundServCode);
        holding.put("value", value);
        holdings.put(holding);

        return holdings;
    }

    JSONArray addCash(JSONArray holdings, String holdingIdentifier,
                             int value) {
        JSONObject holding = new JSONObject();
        holding.put("type", "CASH");
        holding.put("holdingIdentifier", holdingIdentifier);
        holding.put("value", value);
        holdings.put(holding);
        return holdings;
    }

    JSONArray addETF(JSONArray holdings, String type, String ticker, int value) {
        JSONObject holding = new JSONObject();
        holding.put("type", type);
        holding.put("holdingIdentifier", "TICKER");
        holding.put("ticker", ticker);
        holding.put("value", value);
        holdings.put(holding);
        return holdings;
    }

    JSONArray addStocks(JSONArray holdings, String type, String exchangeCode, String ticker, int value) {
        JSONObject holding = new JSONObject();
        holding.put("type", type);
        holding.put("holdingIdentifier", "TICKER");
        holding.put("exchangeCode", exchangeCode);
        holding.put("ticker", ticker);
        holding.put("value", value);
        holdings.put(holding);
        return holdings;
    }

    int random(int bound) {
        return new Random().nextInt(bound);
    }

    int randomNZero(int bound) {
        int val = new Random().nextInt(bound);
        if (val == 0) return 1;

        return val;
    }


    @Test
    void test_mer() {

        JSONObject root = new JSONObject();
        addDataProviders(root, new String[]{"EAGLE", "MORNINGSTAR"});

        addParameterTypes(root, new String[]{"ABSOLUTE", "SCALED", "FORCE_REPORT_FEE"});
        JSONArray holdings = new JSONArray();
        addMutualFund(holdings, "FUNDSERV", mutualFunds[random(mutualFunds.length - 1)],
                randomNZero(100) * 100);
        addCash(holdings, "FUNDSERV", randomNZero(100) * 100);
        addETF(holdings, "CANADA_ETF", canadaEtf[random(canadaEtf.length - 1)], randomNZero(100) * 100);
        addETF(holdings, "US_ETF", usEtf[random(usEtf.length - 1)], randomNZero(100) * 100);
        addStocks(holdings, "CANADA_STOCKS", "TSE", "T", randomNZero(100) * 100);
        addStocks(holdings, "US_STOCKS", "NYS", "H", randomNZero(100) * 100);

        root.put("holdings", holdings);

        Assertions.assertNotNull(root);

        // vars.putObject("test_mer",root);
    }


    void test_total_trailing_returns() {

        JSONObject root = new JSONObject();
        //addDataProviders(root, new String[]{"EAGLE", "MORNINGSTAR"});
        //addParameterTypes(root, new String[]{"ABSOLUTE", "SCALED", "FORCE_REPORT_FEE"});

        addCustomIntervalPerformanceStartDate(root, "2019-01-31");
        addCustomPerformanceEndDate(root, "2019-06-30");
        this.addCurrency(root, "CAD");
        addTimeIntervalPeriods(root, new String[]{"1", "3", "6", "12", "36", "60", "120", "43", "100", "YEAR_TO_DATE", "SINCE_PERFORMANCE_START_DATE"});
        JSONArray holdings = new JSONArray();
        addMutualFund(holdings, "FUNDSERV", mutualFunds[random(mutualFunds.length - 1)],
                randomNZero(100) * 100);
        addCash(holdings, "FUNDSERV", randomNZero(100) * 100);
        addETF(holdings, "CANADA_ETF", canadaEtf[random(canadaEtf.length - 1)], randomNZero(100) * 100);
        addETF(holdings, "US_ETF", usEtf[random(usEtf.length - 1)], randomNZero(100) * 100);
        addStocks(holdings, "CANADA_STOCKS", "TSE", "T", randomNZero(100) * 100);
        addStocks(holdings, "US_STOCKS", "NYS", "H", randomNZero(100) * 100);

        root.put("holdings", holdings);

        // vars.putObject("test_total_trailing_returns",root);
    }


    void test_upsideCaptures() {

        JSONObject root = new JSONObject();
        //addDataProviders(root, new String[]{"EAGLE", "MORNINGSTAR"});
        //addParameterTypes(root, new String[]{"ABSOLUTE", "SCALED", "FORCE_REPORT_FEE"});

        addCustomIntervalPerformanceStartDate(root, "2018-01-31");
        addCustomPerformanceEndDate(root, "2019-06-30");
        this.addCurrency(root, "CAD");
        addTimeIntervalPeriods(root, new String[]{"12", "36", "60", "120", "43", "100", "SINCE_PERFORMANCE_START_DATE"});
        JSONArray holdings = new JSONArray();
        addMutualFund(holdings, "FUNDSERV", mutualFunds[random(mutualFunds.length - 1)],
                randomNZero(100) * 100);
        addCash(holdings, "FUNDSERV", randomNZero(100) * 100);
        addETF(holdings, "CANADA_ETF", canadaEtf[random(canadaEtf.length - 1)], randomNZero(100) * 100);
        addETF(holdings, "US_ETF", usEtf[random(usEtf.length - 1)], randomNZero(100) * 100);
        addStocks(holdings, "CANADA_STOCKS", "TSE", "T", randomNZero(100) * 100);
        addStocks(holdings, "US_STOCKS", "NYS", "H", randomNZero(100) * 100);

        root.put("holdings", holdings);

        JSONArray benchmarkHoldings = new JSONArray();

        addMutualFund(benchmarkHoldings, "FUNDSERV", mutualFunds[random(mutualFunds.length - 1)],
                randomNZero(100) * 100);

        root.put("benchmarkHoldings", benchmarkHoldings);


        // vars.putObject("test_upsideCaptures",root);
    }


    void test_assetAllocation() {

        JSONObject root = new JSONObject();
        addDataProviders(root, new String[]{"EAGLE", "MORNINGSTAR"});
        //addParameterTypes(root, new String[]{"ABSOLUTE", "SCALED", "FORCE_REPORT_FEE"});

        //addCustomIntervalPerformanceStartDate(root,"2018-01-31");
        //addCustomPerformanceEndDate(root,"2019-06-30");
        //this.addCurrency(root,"CAD");
        //addTimeIntervalPeriods(root,new String[]{"12","36","60","120","43","100","SINCE_PERFORMANCE_START_DATE"});
        JSONArray holdings = new JSONArray();
        addMutualFund(holdings, "FUNDSERV", mutualFunds[random(mutualFunds.length - 1)],
                randomNZero(100) * 100);
        addCash(holdings, "FUNDSERV", randomNZero(100) * 100);
        addETF(holdings, "CANADA_ETF", canadaEtf[random(canadaEtf.length - 1)], randomNZero(100) * 100);
        addETF(holdings, "US_ETF", usEtf[random(usEtf.length - 1)], randomNZero(100) * 100);
        addStocks(holdings, "CANADA_STOCKS", "TSE", "T", randomNZero(100) * 100);
        addStocks(holdings, "US_STOCKS", "NYS", "H", randomNZero(100) * 100);

        root.put("holdings", holdings);

        JSONArray benchmarkHoldings = new JSONArray();

        addMutualFund(benchmarkHoldings, "FUNDSERV", mutualFunds[random(mutualFunds.length - 1)],
                randomNZero(100) * 100);

        root.put("benchmarkHoldings", benchmarkHoldings);


        // vars.putObject("test_assetAllocation",root);
    }

    void test_equityCountryExposure() {

        JSONObject root = new JSONObject();
        //addDataProviders(root, new String[]{"EAGLE", "MORNINGSTAR"});
        //addParameterTypes(root, new String[]{"ABSOLUTE", "SCALED", "FORCE_REPORT_FEE"});

        //addCustomIntervalPerformanceStartDate(root,"2018-01-31");
        //addCustomPerformanceEndDate(root,"2019-06-30");
        //this.addCurrency(root,"CAD");
        //addTimeIntervalPeriods(root,new String[]{"12","36","60","120","43","100","SINCE_PERFORMANCE_START_DATE"});
        JSONArray holdings = new JSONArray();
        addMutualFund(holdings, "FUNDSERV", mutualFunds[random(mutualFunds.length - 1)],
                randomNZero(100) * 100);
        addCash(holdings, "FUNDSERV", randomNZero(100) * 100);
        addETF(holdings, "CANADA_ETF", canadaEtf[random(canadaEtf.length - 1)], randomNZero(100) * 100);
        addETF(holdings, "US_ETF", usEtf[random(usEtf.length - 1)], randomNZero(100) * 100);
        addStocks(holdings, "CANADA_STOCKS", "TSE", "T", randomNZero(100) * 100);
        addStocks(holdings, "US_STOCKS", "NYS", "H", randomNZero(100) * 100);

        root.put("holdings", holdings);

        /*
        JSONArray benchmarkHoldings = new JSONArray();

        addMutualFund(benchmarkHoldings, "FUNDSERV", mutualFunds[random(mutualFunds.length-1)],
                randomNZero(100)*100);

        root.put("benchmarkHoldings", benchmarkHoldings);

         */


        // vars.putObject("test_equityCountryExposure",root);
    }


    void test_assetAllocationWithEmergingMarkets() {

        JSONObject root = new JSONObject();
        addDataProviders(root, new String[]{"EAGLE", "MORNINGSTAR"});
        //addParameterTypes(root, new String[]{"ABSOLUTE", "SCALED", "FORCE_REPORT_FEE"});

        //addCustomIntervalPerformanceStartDate(root,"2018-01-31");
        //addCustomPerformanceEndDate(root,"2019-06-30");
        //this.addCurrency(root,"CAD");
        //addTimeIntervalPeriods(root,new String[]{"12","36","60","120","43","100","SINCE_PERFORMANCE_START_DATE"});
        JSONArray holdings = new JSONArray();
        addMutualFund(holdings, "FUNDSERV", mutualFunds[random(mutualFunds.length - 1)],
                randomNZero(100) * 100);
        addCash(holdings, "FUNDSERV", randomNZero(100) * 100);
        addETF(holdings, "CANADA_ETF", canadaEtf[random(canadaEtf.length - 1)], randomNZero(100) * 100);
        addETF(holdings, "US_ETF", usEtf[random(usEtf.length - 1)], randomNZero(100) * 100);
        addStocks(holdings, "CANADA_STOCKS", "TSE", "T", randomNZero(100) * 100);
        addStocks(holdings, "US_STOCKS", "NYS", "H", randomNZero(100) * 100);

        root.put("holdings", holdings);

        /*
        JSONArray benchmarkHoldings = new JSONArray();

        addMutualFund(benchmarkHoldings, "FUNDSERV", mutualFunds[random(mutualFunds.length-1)],
                randomNZero(100)*100);

        root.put("benchmarkHoldings", benchmarkHoldings);

         */


        // vars.putObject("test_assetAllocationWithEmergingMarkets",root);
    }


    void test_fixedIncomeCreditQuality() {

        JSONObject root = new JSONObject();
        //addDataProviders(root, new String[]{"EAGLE", "MORNINGSTAR"});
        //addParameterTypes(root, new String[]{"ABSOLUTE", "SCALED", "FORCE_REPORT_FEE"});

        //addCustomIntervalPerformanceStartDate(root,"2018-01-31");
        //addCustomPerformanceEndDate(root,"2019-06-30");
        //this.addCurrency(root,"CAD");
        //addTimeIntervalPeriods(root,new String[]{"12","36","60","120","43","100","SINCE_PERFORMANCE_START_DATE"});
        JSONArray holdings = new JSONArray();
        addMutualFund(holdings, "FUNDSERV", mutualFunds[random(mutualFunds.length - 1)],
                randomNZero(100) * 100);
        addCash(holdings, "FUNDSERV", randomNZero(100) * 100);
        addETF(holdings, "CANADA_ETF", canadaEtf[random(canadaEtf.length - 1)], randomNZero(100) * 100);
        addETF(holdings, "US_ETF", usEtf[random(usEtf.length - 1)], randomNZero(100) * 100);
        addStocks(holdings, "CANADA_STOCKS", "TSE", "T", randomNZero(100) * 100);
        addStocks(holdings, "US_STOCKS", "NYS", "H", randomNZero(100) * 100);

        root.put("holdings", holdings);

        /*
        JSONArray benchmarkHoldings = new JSONArray();

        addMutualFund(benchmarkHoldings, "FUNDSERV", mutualFunds[random(mutualFunds.length-1)],
                randomNZero(100)*100);

        root.put("benchmarkHoldings", benchmarkHoldings);

         */


        // vars.putObject("test_fixedIncomeCreditQuality",root);
    }


    void test_fixedIncomeCountryExposure() {

        JSONObject root = new JSONObject();
        //addDataProviders(root, new String[]{"EAGLE", "MORNINGSTAR"});
        //addParameterTypes(root, new String[]{"ABSOLUTE", "SCALED", "FORCE_REPORT_FEE"});

        //addCustomIntervalPerformanceStartDate(root,"2018-01-31");
        //addCustomPerformanceEndDate(root,"2019-06-30");
        //this.addCurrency(root,"CAD");
        //addTimeIntervalPeriods(root,new String[]{"12","36","60","120","43","100","SINCE_PERFORMANCE_START_DATE"});
        JSONArray holdings = new JSONArray();
        addMutualFund(holdings, "FUNDSERV", mutualFunds[random(mutualFunds.length - 1)],
                randomNZero(100) * 100);
        addCash(holdings, "FUNDSERV", randomNZero(100) * 100);
        addETF(holdings, "CANADA_ETF", canadaEtf[random(canadaEtf.length - 1)], randomNZero(100) * 100);
        addETF(holdings, "US_ETF", usEtf[random(usEtf.length - 1)], randomNZero(100) * 100);
        addStocks(holdings, "CANADA_STOCKS", "TSE", "T", randomNZero(100) * 100);
        addStocks(holdings, "US_STOCKS", "NYS", "H", randomNZero(100) * 100);

        root.put("holdings", holdings);

        /*
        JSONArray benchmarkHoldings = new JSONArray();

        addMutualFund(benchmarkHoldings, "FUNDSERV", mutualFunds[random(mutualFunds.length-1)],
                randomNZero(100)*100);

        root.put("benchmarkHoldings", benchmarkHoldings);

         */


        // vars.putObject("test_fixedIncomeCountryExposure",root);
    }


    void test_equitySector() {

        JSONObject root = new JSONObject();
        //addDataProviders(root, new String[]{"EAGLE", "MORNINGSTAR"});
        //addParameterTypes(root, new String[]{"ABSOLUTE", "SCALED", "FORCE_REPORT_FEE"});

        //addCustomIntervalPerformanceStartDate(root,"2018-01-31");
        //addCustomPerformanceEndDate(root,"2019-06-30");
        //this.addCurrency(root,"CAD");
        //addTimeIntervalPeriods(root,new String[]{"12","36","60","120","43","100","SINCE_PERFORMANCE_START_DATE"});
        JSONArray holdings = new JSONArray();
        addMutualFund(holdings, "FUNDSERV", mutualFunds[random(mutualFunds.length - 1)],
                randomNZero(100) * 100);
        addCash(holdings, "FUNDSERV", randomNZero(100) * 100);
        addETF(holdings, "CANADA_ETF", canadaEtf[random(canadaEtf.length - 1)], randomNZero(100) * 100);
        addETF(holdings, "US_ETF", usEtf[random(usEtf.length - 1)], randomNZero(100) * 100);
        addStocks(holdings, "CANADA_STOCKS", "TSE", "T", randomNZero(100) * 100);
        addStocks(holdings, "US_STOCKS", "NYS", "H", randomNZero(100) * 100);

        root.put("holdings", holdings);

        /*
        JSONArray benchmarkHoldings = new JSONArray();

        addMutualFund(benchmarkHoldings, "FUNDSERV", mutualFunds[random(mutualFunds.length-1)],
                randomNZero(100)*100);

        root.put("benchmarkHoldings", benchmarkHoldings);

         */


        // vars.putObject("test_equitySector",root);
    }


    void test_equityMarketCapitalization() {

        JSONObject root = new JSONObject();
        //addDataProviders(root, new String[]{"EAGLE", "MORNINGSTAR"});
        //addParameterTypes(root, new String[]{"ABSOLUTE", "SCALED", "FORCE_REPORT_FEE"});

        //addCustomIntervalPerformanceStartDate(root,"2018-01-31");
        //addCustomPerformanceEndDate(root,"2019-06-30");
        //this.addCurrency(root,"CAD");
        //addTimeIntervalPeriods(root,new String[]{"12","36","60","120","43","100","SINCE_PERFORMANCE_START_DATE"});
        JSONArray holdings = new JSONArray();
        addMutualFund(holdings, "FUNDSERV", mutualFunds[random(mutualFunds.length - 1)],
                randomNZero(100) * 100);
        addCash(holdings, "FUNDSERV", randomNZero(100) * 100);
        addETF(holdings, "CANADA_ETF", canadaEtf[random(canadaEtf.length - 1)], randomNZero(100) * 100);
        addETF(holdings, "US_ETF", usEtf[random(usEtf.length - 1)], randomNZero(100) * 100);
        addStocks(holdings, "CANADA_STOCKS", "TSE", "T", randomNZero(100) * 100);
        addStocks(holdings, "US_STOCKS", "NYS", "H", randomNZero(100) * 100);

        root.put("holdings", holdings);

        /*
        JSONArray benchmarkHoldings = new JSONArray();

        addMutualFund(benchmarkHoldings, "FUNDSERV", mutualFunds[random(mutualFunds.length-1)],
                randomNZero(100)*100);

        root.put("benchmarkHoldings", benchmarkHoldings);

         */


        // vars.putObject("test_equityMarketCapitalization",root);
    }


    void test_annuralReturn() {

        JSONObject root = new JSONObject();
        //addDataProviders(root, new String[]{"EAGLE", "MORNINGSTAR"});
        //addParameterTypes(root, new String[]{"ABSOLUTE", "SCALED", "FORCE_REPORT_FEE"});

        addCustomIntervalPerformanceStartDate(root, "2018-01-31");
        addCustomPerformanceEndDate(root, "2019-06-30");
        this.addCurrency(root, "CAD");
        //addTimeIntervalPeriods(root,new String[]{"12","36","60","120","43","100","SINCE_PERFORMANCE_START_DATE"});
        JSONArray holdings = new JSONArray();
        addMutualFund(holdings, "FUNDSERV", mutualFunds[random(mutualFunds.length - 1)],
                randomNZero(100) * 100);
        addCash(holdings, "FUNDSERV", randomNZero(100) * 100);
        addETF(holdings, "CANADA_ETF", canadaEtf[random(canadaEtf.length - 1)], randomNZero(100) * 100);
        addETF(holdings, "US_ETF", usEtf[random(usEtf.length - 1)], randomNZero(100) * 100);
        addStocks(holdings, "CANADA_STOCKS", "TSE", "T", randomNZero(100) * 100);
        addStocks(holdings, "US_STOCKS", "NYS", "H", randomNZero(100) * 100);

        root.put("holdings", holdings);

       /* JSONArray benchmarkHoldings = new JSONArray();

        addMutualFund(benchmarkHoldings, "FUNDSERV", mutualFunds[random(mutualFunds.length-1)],
                randomNZero(100)*100);

        root.put("benchmarkHoldings", benchmarkHoldings);
        */


        // vars.putObject("test_annuralReturn",root);
    }


    void test_growthOf10K() {

        JSONObject root = new JSONObject();
        //addDataProviders(root, new String[]{"EAGLE", "MORNINGSTAR"});
        //addParameterTypes(root, new String[]{"ABSOLUTE", "SCALED", "FORCE_REPORT_FEE"});

        addCustomIntervalPerformanceStartDate(root, "2018-01-31");
        addCustomPerformanceEndDate(root, "2019-06-30");
        this.addCurrency(root, "CAD");
        //addTimeIntervalPeriods(root,new String[]{"12","36","60","120","43","100","SINCE_PERFORMANCE_START_DATE"});
        JSONArray holdings = new JSONArray();
        addMutualFund(holdings, "FUNDSERV", mutualFunds[random(mutualFunds.length - 1)],
                randomNZero(100) * 100);
        addCash(holdings, "FUNDSERV", randomNZero(100) * 100);
        addETF(holdings, "CANADA_ETF", canadaEtf[random(canadaEtf.length - 1)], randomNZero(100) * 100);
        addETF(holdings, "US_ETF", usEtf[random(usEtf.length - 1)], randomNZero(100) * 100);
        addStocks(holdings, "CANADA_STOCKS", "TSE", "T", randomNZero(100) * 100);
        addStocks(holdings, "US_STOCKS", "NYS", "H", randomNZero(100) * 100);

        root.put("holdings", holdings);

       /* JSONArray benchmarkHoldings = new JSONArray();

        addMutualFund(benchmarkHoldings, "FUNDSERV", mutualFunds[random(mutualFunds.length-1)],
                randomNZero(100)*100);

        root.put("benchmarkHoldings", benchmarkHoldings);
        */

        // vars.putObject("test_growthOf10K",root);
    }


    void test_bestWorstPeriod() {

        JSONObject root = new JSONObject();
        //addDataProviders(root, new String[]{"EAGLE", "MORNINGSTAR"});
        //addParameterTypes(root, new String[]{"ABSOLUTE", "SCALED", "FORCE_REPORT_FEE"});

        //addCustomIntervalPerformanceStartDate(root,"2013-01-31");
        addCustomIntervalPerformanceStartDate(root, "2013-01-31");
        addCustomPerformanceEndDate(root, "2019-06-30");
        this.addCurrency(root, "CAD");
        addBestWorstTimeIntervalPeriods(root, new String[]{"12", "36", "60", "120"});
        //addTimeIntervalPeriods(root,new String[]{"12","36","60","120","43","100","SINCE_PERFORMANCE_START_DATE"});
        JSONArray holdings = new JSONArray();
        addMutualFund(holdings, "FUNDSERV", mutualFunds[random(mutualFunds.length - 1)],
                randomNZero(100) * 100);
        addCash(holdings, "FUNDSERV", randomNZero(100) * 100);
        addETF(holdings, "CANADA_ETF", canadaEtf[random(canadaEtf.length - 1)], randomNZero(100) * 100);
        addETF(holdings, "US_ETF", usEtf[random(usEtf.length - 1)], randomNZero(100) * 100);
        addStocks(holdings, "CANADA_STOCKS", "TSE", "T", randomNZero(100) * 100);
        addStocks(holdings, "US_STOCKS", "NYS", "H", randomNZero(100) * 100);

        root.put("holdings", holdings);

       /* JSONArray benchmarkHoldings = new JSONArray();

        addMutualFund(benchmarkHoldings, "FUNDSERV", mutualFunds[random(mutualFunds.length-1)],
                randomNZero(100)*100);

        root.put("benchmarkHoldings", benchmarkHoldings);
        */


        // vars.putObject("test_bestWorstPeriod",root);
    }

    void test_leadingTotalReturns() {

        JSONObject root = new JSONObject();
        //addDataProviders(root, new String[]{"EAGLE", "MORNINGSTAR"});
        //addParameterTypes(root, new String[]{"ABSOLUTE", "SCALED", "FORCE_REPORT_FEE"});

        addCustomPerformanceStartDate(root, "2013-01-31");
        //addCustomPerformanceEndDate(root,"2019-06-30");
        this.addCurrency(root, "CAD");
        //addBestWorstTimeIntervalPeriods(root,new String[]{"12","36","60","120"});
        addTimeIntervalPeriods(root, new String[]{"12", "36", "60", "120", "43", "100", "SINCE_PERFORMANCE_START_DATE"});
        JSONArray holdings = new JSONArray();
        addMutualFund(holdings, "FUNDSERV", mutualFunds[random(mutualFunds.length - 1)],
                randomNZero(100) * 100);
        addCash(holdings, "FUNDSERV", randomNZero(100) * 100);
        addETF(holdings, "CANADA_ETF", canadaEtf[random(canadaEtf.length - 1)], randomNZero(100) * 100);
        addETF(holdings, "US_ETF", usEtf[random(usEtf.length - 1)], randomNZero(100) * 100);
        addStocks(holdings, "CANADA_STOCKS", "TSE", "T", randomNZero(100) * 100);
        addStocks(holdings, "US_STOCKS", "NYS", "H", randomNZero(100) * 100);

        root.put("holdings", holdings);

       /* JSONArray benchmarkHoldings = new JSONArray();

        addMutualFund(benchmarkHoldings, "FUNDSERV", mutualFunds[random(mutualFunds.length-1)],
                randomNZero(100)*100);

        root.put("benchmarkHoldings", benchmarkHoldings);
        */


        // vars.putObject("test_leadingTotalReturns",root);
    }

    void test_standardDeviation() {

        JSONObject root = new JSONObject();
        //addDataProviders(root, new String[]{"EAGLE", "MORNINGSTAR"});
        //addParameterTypes(root, new String[]{"ABSOLUTE", "SCALED", "FORCE_REPORT_FEE"});

        //addCustomIntervalPerformanceStartDate(root,"2013-01-31");
        addCustomIntervalPerformanceStartDate(root, "2013-01-31");
        addCustomPerformanceEndDate(root, "2019-06-30");
        this.addCurrency(root, "CAD");
        //addBestWorstTimeIntervalPeriods(root,new String[]{"12","36","60","120"});
        addTimeIntervalPeriods(root, new String[]{"12", "36", "60", "120", "43", "100", "SINCE_PERFORMANCE_START_DATE"});
        JSONArray holdings = new JSONArray();
        addMutualFund(holdings, "FUNDSERV", mutualFunds[random(mutualFunds.length - 1)],
                randomNZero(100) * 100);
        addCash(holdings, "FUNDSERV", randomNZero(100) * 100);
        addETF(holdings, "CANADA_ETF", canadaEtf[random(canadaEtf.length - 1)], randomNZero(100) * 100);
        addETF(holdings, "US_ETF", usEtf[random(usEtf.length - 1)], randomNZero(100) * 100);
        addStocks(holdings, "CANADA_STOCKS", "TSE", "T", randomNZero(100) * 100);
        addStocks(holdings, "US_STOCKS", "NYS", "H", randomNZero(100) * 100);

        root.put("holdings", holdings);

       /* JSONArray benchmarkHoldings = new JSONArray();

        addMutualFund(benchmarkHoldings, "FUNDSERV", mutualFunds[random(mutualFunds.length-1)],
                randomNZero(100)*100);

        root.put("benchmarkHoldings", benchmarkHoldings);
        */


        // vars.putObject("test_standardDeviation",root);
    }

    void test_sharpeRatio() {

        JSONObject root = new JSONObject();
        //addDataProviders(root, new String[]{"EAGLE", "MORNINGSTAR"});
        //addParameterTypes(root, new String[]{"ABSOLUTE", "SCALED", "FORCE_REPORT_FEE"});

        //addCustomIntervalPerformanceStartDate(root,"2013-01-31");
        addCustomIntervalPerformanceStartDate(root, "2013-01-31");
        addCustomPerformanceEndDate(root, "2019-06-30");
        this.addCurrency(root, "CAD");
        //addBestWorstTimeIntervalPeriods(root,new String[]{"12","36","60","120"});
        addTimeIntervalPeriods(root, new String[]{"12", "36", "60", "120", "43", "100", "SINCE_PERFORMANCE_START_DATE"});
        JSONArray holdings = new JSONArray();
        addMutualFund(holdings, "FUNDSERV", mutualFunds[random(mutualFunds.length - 1)],
                randomNZero(100) * 100);
        addCash(holdings, "FUNDSERV", randomNZero(100) * 100);
        addETF(holdings, "CANADA_ETF", canadaEtf[random(canadaEtf.length - 1)], randomNZero(100) * 100);
        addETF(holdings, "US_ETF", usEtf[random(usEtf.length - 1)], randomNZero(100) * 100);
        addStocks(holdings, "CANADA_STOCKS", "TSE", "T", randomNZero(100) * 100);
        addStocks(holdings, "US_STOCKS", "NYS", "H", randomNZero(100) * 100);

        root.put("holdings", holdings);

       /* JSONArray benchmarkHoldings = new JSONArray();

        addMutualFund(benchmarkHoldings, "FUNDSERV", mutualFunds[random(mutualFunds.length-1)],
                randomNZero(100)*100);

        root.put("benchmarkHoldings", benchmarkHoldings);
        */


        // vars.putObject("test_sharpeRatio",root);
    }

    void test_downsideDeviation() {

        JSONObject root = new JSONObject();
        //addDataProviders(root, new String[]{"EAGLE", "MORNINGSTAR"});
        //addParameterTypes(root, new String[]{"ABSOLUTE", "SCALED", "FORCE_REPORT_FEE"});

        //addCustomIntervalPerformanceStartDate(root,"2013-01-31");
        addCustomIntervalPerformanceStartDate(root, "2013-01-31");
        addCustomPerformanceEndDate(root, "2019-06-30");
        this.addCurrency(root, "CAD");
        //addBestWorstTimeIntervalPeriods(root,new String[]{"12","36","60","120"});
        addTimeIntervalPeriods(root, new String[]{"12", "36", "60", "120", "43", "100", "SINCE_PERFORMANCE_START_DATE"});
        JSONArray holdings = new JSONArray();
        addMutualFund(holdings, "FUNDSERV", mutualFunds[random(mutualFunds.length - 1)],
                randomNZero(100) * 100);
        addCash(holdings, "FUNDSERV", randomNZero(100) * 100);
        addETF(holdings, "CANADA_ETF", canadaEtf[random(canadaEtf.length - 1)], randomNZero(100) * 100);
        addETF(holdings, "US_ETF", usEtf[random(usEtf.length - 1)], randomNZero(100) * 100);
        addStocks(holdings, "CANADA_STOCKS", "TSE", "T", randomNZero(100) * 100);
        addStocks(holdings, "US_STOCKS", "NYS", "H", randomNZero(100) * 100);

        root.put("holdings", holdings);

       /* JSONArray benchmarkHoldings = new JSONArray();

        addMutualFund(benchmarkHoldings, "FUNDSERV", mutualFunds[random(mutualFunds.length-1)],
                randomNZero(100)*100);

        root.put("benchmarkHoldings", benchmarkHoldings);
        */


        // vars.putObject("test_downsideDeviation",root);
    }

    void test_sortinoRatio() {

        JSONObject root = new JSONObject();
        //addDataProviders(root, new String[]{"EAGLE", "MORNINGSTAR"});
        //addParameterTypes(root, new String[]{"ABSOLUTE", "SCALED", "FORCE_REPORT_FEE"});

        //addCustomIntervalPerformanceStartDate(root,"2013-01-31");
        addCustomIntervalPerformanceStartDate(root, "2013-01-31");
        addCustomPerformanceEndDate(root, "2019-06-30");
        this.addCurrency(root, "CAD");
        //addBestWorstTimeIntervalPeriods(root,new String[]{"12","36","60","120"});
        addTimeIntervalPeriods(root, new String[]{"12", "36", "60", "120", "43", "100", "SINCE_PERFORMANCE_START_DATE"});
        JSONArray holdings = new JSONArray();
        addMutualFund(holdings, "FUNDSERV", mutualFunds[random(mutualFunds.length - 1)],
                randomNZero(100) * 100);
        addCash(holdings, "FUNDSERV", randomNZero(100) * 100);
        addETF(holdings, "CANADA_ETF", canadaEtf[random(canadaEtf.length - 1)], randomNZero(100) * 100);
        addETF(holdings, "US_ETF", usEtf[random(usEtf.length - 1)], randomNZero(100) * 100);
        addStocks(holdings, "CANADA_STOCKS", "TSE", "T", randomNZero(100) * 100);
        addStocks(holdings, "US_STOCKS", "NYS", "H", randomNZero(100) * 100);

        root.put("holdings", holdings);

       /* JSONArray benchmarkHoldings = new JSONArray();

        addMutualFund(benchmarkHoldings, "FUNDSERV", mutualFunds[random(mutualFunds.length-1)],
                randomNZero(100)*100);

        root.put("benchmarkHoldings", benchmarkHoldings);
        */


        // vars.putObject("test_sortinoRatio",root);
    }

    void test_maxDrawDown() {

        JSONObject root = new JSONObject();
        //addDataProviders(root, new String[]{"EAGLE", "MORNINGSTAR"});
        //addParameterTypes(root, new String[]{"ABSOLUTE", "SCALED", "FORCE_REPORT_FEE"});

        //addCustomIntervalPerformanceStartDate(root,"2013-01-31");
        addCustomIntervalPerformanceStartDate(root, "2013-01-31");
        addCustomPerformanceEndDate(root, "2019-06-30");
        this.addCurrency(root, "CAD");
        //addBestWorstTimeIntervalPeriods(root,new String[]{"12","36","60","120"});
        addTimeIntervalPeriods(root, new String[]{"12", "36", "60", "120", "43", "100", "SINCE_PERFORMANCE_START_DATE"});
        JSONArray holdings = new JSONArray();
        addMutualFund(holdings, "FUNDSERV", mutualFunds[random(mutualFunds.length - 1)],
                randomNZero(100) * 100);
        addCash(holdings, "FUNDSERV", randomNZero(100) * 100);
        addETF(holdings, "CANADA_ETF", canadaEtf[random(canadaEtf.length - 1)], randomNZero(100) * 100);
        addETF(holdings, "US_ETF", usEtf[random(usEtf.length - 1)], randomNZero(100) * 100);
        addStocks(holdings, "CANADA_STOCKS", "TSE", "T", randomNZero(100) * 100);
        addStocks(holdings, "US_STOCKS", "NYS", "H", randomNZero(100) * 100);

        root.put("holdings", holdings);

       /* JSONArray benchmarkHoldings = new JSONArray();

        addMutualFund(benchmarkHoldings, "FUNDSERV", mutualFunds[random(mutualFunds.length-1)],
                randomNZero(100)*100);

        root.put("benchmarkHoldings", benchmarkHoldings);
        */


        // vars.putObject("test_maxDrawDown",root);
    }

    void test_upsideCaptureRequirement() {

        JSONObject root = new JSONObject();
        //addDataProviders(root, new String[]{"EAGLE", "MORNINGSTAR"});
        //addParameterTypes(root, new String[]{"ABSOLUTE", "SCALED", "FORCE_REPORT_FEE"});

        //addCustomIntervalPerformanceStartDate(root,"2013-01-31");
        addCustomIntervalPerformanceStartDate(root, "2013-01-31");
        addCustomPerformanceEndDate(root, "2019-06-30");
        this.addCurrency(root, "CAD");
        //addBestWorstTimeIntervalPeriods(root,new String[]{"12","36","60","120"});
        addTimeIntervalPeriods(root, new String[]{"12", "36", "60", "120", "43", "100", "SINCE_PERFORMANCE_START_DATE"});
        JSONArray holdings = new JSONArray();
        addMutualFund(holdings, "FUNDSERV", mutualFunds[random(mutualFunds.length - 1)],
                randomNZero(100) * 100);
        addCash(holdings, "FUNDSERV", randomNZero(100) * 100);
        addETF(holdings, "CANADA_ETF", canadaEtf[random(canadaEtf.length - 1)], randomNZero(100) * 100);
        addETF(holdings, "US_ETF", usEtf[random(usEtf.length - 1)], randomNZero(100) * 100);
        addStocks(holdings, "CANADA_STOCKS", "TSE", "T", randomNZero(100) * 100);
        addStocks(holdings, "US_STOCKS", "NYS", "H", randomNZero(100) * 100);

        root.put("holdings", holdings);

        JSONArray benchmarkHoldings = new JSONArray();

        addMutualFund(benchmarkHoldings, "FUNDSERV", mutualFunds[random(mutualFunds.length - 1)],
                randomNZero(100) * 100);

        root.put("benchmarkHoldings", benchmarkHoldings);


        // vars.putObject("test_upsideCaptureRequirement",root);
    }


    void test_downsideCaptureRequirement() {

        JSONObject root = new JSONObject();
        //addDataProviders(root, new String[]{"EAGLE", "MORNINGSTAR"});
        //addParameterTypes(root, new String[]{"ABSOLUTE", "SCALED", "FORCE_REPORT_FEE"});

        //addCustomIntervalPerformanceStartDate(root,"2013-01-31");
        addCustomIntervalPerformanceStartDate(root, "2013-01-31");
        addCustomPerformanceEndDate(root, "2019-06-30");
        this.addCurrency(root, "CAD");
        //addBestWorstTimeIntervalPeriods(root,new String[]{"12","36","60","120"});
        addTimeIntervalPeriods(root, new String[]{"12", "36", "60", "120", "43", "100", "SINCE_PERFORMANCE_START_DATE"});
        JSONArray holdings = new JSONArray();
        addMutualFund(holdings, "FUNDSERV", mutualFunds[random(mutualFunds.length - 1)],
                randomNZero(100) * 100);
        addCash(holdings, "FUNDSERV", randomNZero(100) * 100);
        addETF(holdings, "CANADA_ETF", canadaEtf[random(canadaEtf.length - 1)], randomNZero(100) * 100);
        addETF(holdings, "US_ETF", usEtf[random(usEtf.length - 1)], randomNZero(100) * 100);
        addStocks(holdings, "CANADA_STOCKS", "TSE", "T", randomNZero(100) * 100);
        addStocks(holdings, "US_STOCKS", "NYS", "H", randomNZero(100) * 100);

        root.put("holdings", holdings);

        JSONArray benchmarkHoldings = new JSONArray();

        addMutualFund(benchmarkHoldings, "FUNDSERV", mutualFunds[random(mutualFunds.length - 1)],
                randomNZero(100) * 100);

        root.put("benchmarkHoldings", benchmarkHoldings);


        // vars.putObject("test_downsideCaptureRequirement",root);
    }

    void test_trackingError() {

        JSONObject root = new JSONObject();
        //addDataProviders(root, new String[]{"EAGLE", "MORNINGSTAR"});
        //addParameterTypes(root, new String[]{"ABSOLUTE", "SCALED", "FORCE_REPORT_FEE"});

        //addCustomIntervalPerformanceStartDate(root,"2013-01-31");
        addCustomIntervalPerformanceStartDate(root, "2013-01-31");
        addCustomPerformanceEndDate(root, "2019-06-30");
        this.addCurrency(root, "CAD");
        //addBestWorstTimeIntervalPeriods(root,new String[]{"12","36","60","120"});
        addTimeIntervalPeriods(root, new String[]{"12", "36", "60", "120", "43", "100", "SINCE_PERFORMANCE_START_DATE"});
        JSONArray holdings = new JSONArray();
        addMutualFund(holdings, "FUNDSERV", mutualFunds[random(mutualFunds.length - 1)],
                randomNZero(100) * 100);
        addCash(holdings, "FUNDSERV", randomNZero(100) * 100);
        addETF(holdings, "CANADA_ETF", canadaEtf[random(canadaEtf.length - 1)], randomNZero(100) * 100);
        addETF(holdings, "US_ETF", usEtf[random(usEtf.length - 1)], randomNZero(100) * 100);
        addStocks(holdings, "CANADA_STOCKS", "TSE", "T", randomNZero(100) * 100);
        addStocks(holdings, "US_STOCKS", "NYS", "H", randomNZero(100) * 100);

        root.put("holdings", holdings);

        JSONArray benchmarkHoldings = new JSONArray();

        addMutualFund(benchmarkHoldings, "FUNDSERV", mutualFunds[random(mutualFunds.length - 1)],
                randomNZero(100) * 100);

        root.put("benchmarkHoldings", benchmarkHoldings);


        // vars.putObject("test_trackingError",root);
    }

    void test_excessReturn() {

        JSONObject root = new JSONObject();
        //addDataProviders(root, new String[]{"EAGLE", "MORNINGSTAR"});
        //addParameterTypes(root, new String[]{"ABSOLUTE", "SCALED", "FORCE_REPORT_FEE"});

        //addCustomIntervalPerformanceStartDate(root,"2013-01-31");
        addCustomIntervalPerformanceStartDate(root, "2013-01-31");
        addCustomPerformanceEndDate(root, "2019-06-30");
        this.addCurrency(root, "CAD");
        //addBestWorstTimeIntervalPeriods(root,new String[]{"12","36","60","120"});
        addTimeIntervalPeriods(root, new String[]{"12", "36", "60", "120", "43", "100", "SINCE_PERFORMANCE_START_DATE"});
        JSONArray holdings = new JSONArray();
        addMutualFund(holdings, "FUNDSERV", mutualFunds[random(mutualFunds.length - 1)],
                randomNZero(100) * 100);
        addCash(holdings, "FUNDSERV", randomNZero(100) * 100);
        addETF(holdings, "CANADA_ETF", canadaEtf[random(canadaEtf.length - 1)], randomNZero(100) * 100);
        addETF(holdings, "US_ETF", usEtf[random(usEtf.length - 1)], randomNZero(100) * 100);
        addStocks(holdings, "CANADA_STOCKS", "TSE", "T", randomNZero(100) * 100);
        addStocks(holdings, "US_STOCKS", "NYS", "H", randomNZero(100) * 100);

        root.put("holdings", holdings);

        JSONArray benchmarkHoldings = new JSONArray();

        addMutualFund(benchmarkHoldings, "FUNDSERV", mutualFunds[random(mutualFunds.length - 1)],
                randomNZero(100) * 100);

        root.put("benchmarkHoldings", benchmarkHoldings);


        // vars.putObject("test_excessReturn",root);
    }

    void test_Beta() {

        JSONObject root = new JSONObject();
        //addDataProviders(root, new String[]{"EAGLE", "MORNINGSTAR"});
        //addParameterTypes(root, new String[]{"ABSOLUTE", "SCALED", "FORCE_REPORT_FEE"});

        //addCustomIntervalPerformanceStartDate(root,"2013-01-31");
        addCustomIntervalPerformanceStartDate(root, "2013-01-31");
        addCustomPerformanceEndDate(root, "2019-06-30");
        this.addCurrency(root, "CAD");
        //addBestWorstTimeIntervalPeriods(root,new String[]{"12","36","60","120"});
        addTimeIntervalPeriods(root, new String[]{"12", "36", "60", "120", "43", "100", "SINCE_PERFORMANCE_START_DATE"});
        JSONArray holdings = new JSONArray();
        addMutualFund(holdings, "FUNDSERV", mutualFunds[random(mutualFunds.length - 1)],
                randomNZero(100) * 100);
        addCash(holdings, "FUNDSERV", randomNZero(100) * 100);
        addETF(holdings, "CANADA_ETF", canadaEtf[random(canadaEtf.length - 1)], randomNZero(100) * 100);
        addETF(holdings, "US_ETF", usEtf[random(usEtf.length - 1)], randomNZero(100) * 100);
        addStocks(holdings, "CANADA_STOCKS", "TSE", "T", randomNZero(100) * 100);
        addStocks(holdings, "US_STOCKS", "NYS", "H", randomNZero(100) * 100);

        root.put("holdings", holdings);

        JSONArray benchmarkHoldings = new JSONArray();

        addMutualFund(benchmarkHoldings, "FUNDSERV", mutualFunds[random(mutualFunds.length - 1)],
                randomNZero(100) * 100);

        root.put("benchmarkHoldings", benchmarkHoldings);


        // vars.putObject("test_Beta",root);
    }

    void test_Alpha() {

        JSONObject root = new JSONObject();
        //addDataProviders(root, new String[]{"EAGLE", "MORNINGSTAR"});
        //addParameterTypes(root, new String[]{"ABSOLUTE", "SCALED", "FORCE_REPORT_FEE"});

        //addCustomIntervalPerformanceStartDate(root,"2013-01-31");
        addCustomIntervalPerformanceStartDate(root, "2013-01-31");
        addCustomPerformanceEndDate(root, "2019-06-30");
        this.addCurrency(root, "CAD");
        //addBestWorstTimeIntervalPeriods(root,new String[]{"12","36","60","120"});
        addTimeIntervalPeriods(root, new String[]{"12", "36", "60", "120", "43", "100", "SINCE_PERFORMANCE_START_DATE"});
        JSONArray holdings = new JSONArray();
        addMutualFund(holdings, "FUNDSERV", mutualFunds[random(mutualFunds.length - 1)],
                randomNZero(100) * 100);
        addCash(holdings, "FUNDSERV", randomNZero(100) * 100);
        addETF(holdings, "CANADA_ETF", canadaEtf[random(canadaEtf.length - 1)], randomNZero(100) * 100);
        addETF(holdings, "US_ETF", usEtf[random(usEtf.length - 1)], randomNZero(100) * 100);
        addStocks(holdings, "CANADA_STOCKS", "TSE", "T", randomNZero(100) * 100);
        addStocks(holdings, "US_STOCKS", "NYS", "H", randomNZero(100) * 100);

        root.put("holdings", holdings);

        JSONArray benchmarkHoldings = new JSONArray();

        addMutualFund(benchmarkHoldings, "FUNDSERV", mutualFunds[random(mutualFunds.length - 1)],
                randomNZero(100) * 100);

        root.put("benchmarkHoldings", benchmarkHoldings);


        // vars.putObject("test_Alpha",root);
    }


    void test_Correlation() {

        JSONObject root = new JSONObject();
        //addDataProviders(root, new String[]{"EAGLE", "MORNINGSTAR"});
        //addParameterTypes(root, new String[]{"ABSOLUTE", "SCALED", "FORCE_REPORT_FEE"});

        //addCustomIntervalPerformanceStartDate(root,"2013-01-31");
        addCustomIntervalPerformanceStartDate(root, "2013-01-31");
        addCustomPerformanceEndDate(root, "2019-06-30");
        this.addCurrency(root, "CAD");
        //addBestWorstTimeIntervalPeriods(root,new String[]{"12","36","60","120"});
        addTimeIntervalPeriods(root, new String[]{"12", "36", "60", "120", "43", "100", "SINCE_PERFORMANCE_START_DATE"});
        JSONArray holdings = new JSONArray();
        addMutualFund(holdings, "FUNDSERV", mutualFunds[random(mutualFunds.length - 1)],
                randomNZero(100) * 100);
        addCash(holdings, "FUNDSERV", randomNZero(100) * 100);
        addETF(holdings, "CANADA_ETF", canadaEtf[random(canadaEtf.length - 1)], randomNZero(100) * 100);
        addETF(holdings, "US_ETF", usEtf[random(usEtf.length - 1)], randomNZero(100) * 100);
        addStocks(holdings, "CANADA_STOCKS", "TSE", "T", randomNZero(100) * 100);
        addStocks(holdings, "US_STOCKS", "NYS", "H", randomNZero(100) * 100);

        root.put("holdings", holdings);

        /*JSONArray benchmarkHoldings = new JSONArray();

        addMutualFund(benchmarkHoldings, "FUNDSERV", mutualFunds[random(mutualFunds.length-1)],
                randomNZero(100)*100);

        root.put("benchmarkHoldings", benchmarkHoldings);*/


        // vars.putObject("test_Correlation",root);
    }

    @Test
    void setup_part_1() {
        test_mer();
        test_total_trailing_returns();
        test_upsideCaptures();
        test_assetAllocation();
        test_equityCountryExposure();
        test_assetAllocationWithEmergingMarkets();
        test_fixedIncomeCreditQuality();
        test_fixedIncomeCountryExposure();
        test_equitySector();
        test_equityMarketCapitalization();
        test_annuralReturn();
        test_growthOf10K();
        assertTrue(Boolean.TRUE);
    }

    @Test
    void setup_part_2() {
        test_mer();
        test_total_trailing_returns();
        test_upsideCaptures();
        test_assetAllocation();
        test_equityCountryExposure();
        test_assetAllocationWithEmergingMarkets();
        test_fixedIncomeCreditQuality();
        test_fixedIncomeCountryExposure();
        test_equitySector();
        test_equityMarketCapitalization();
        test_annuralReturn();
        test_growthOf10K();
        assertTrue(Boolean.TRUE);
    }

}
