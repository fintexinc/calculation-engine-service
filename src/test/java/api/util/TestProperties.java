package api.util;

import api.config.ApiPropertyModel;
import api.config.RestPropertyModel;
import api.config.SmokeTestPropertyModel;
import api.config.TestCaseIndexesPropertyModel;
import api.config.TestPropertyModel;
import api.config.VersionModel;
import api.dto.tab.TestCaseIndexesModel;


import com.fintex.ce.framework.common.properties.PropertiesHolder;
import com.fintex.ce.framework.utils.YmlParser;

import java.util.Objects;

public class TestProperties {

    private static final TestPropertyModel PROPERTIES;
    private static final ApiPropertyModel API_PROPERTIES;
    private static final String TAG = "tag";
    private static final String SMOKE = "smoke";

    static {
        API_PROPERTIES = YmlParser.parseYmlToObject("/properties/testcase.yml", ApiPropertyModel.class);
        PROPERTIES = PropertiesHolder.loadProperties(TestPropertyModel.class);
    }

    private TestProperties() {
    }

    public static ApiPropertyModel getAPI() {
        return API_PROPERTIES;
    }

    public static String getFdsUrl() {
        return PROPERTIES.getManual().getFdsUrl();
    }

    public static VersionModel getCEVersions() {
        return PROPERTIES.getManual().getVersions();
    }

    public static RestPropertyModel getRest() {
        return PROPERTIES.getManual().getRest();
    }

    public static TestCaseIndexesModel getTestCaseRowIndexes(final SmokeTestPropertyModel smokeTestPropertyModel) {
        final String tag = System.getProperty(TAG);
        final boolean isSmokeTests = Objects.nonNull(tag) && tag.equalsIgnoreCase(SMOKE);
        final TestCaseIndexesPropertyModel indexes = smokeTestPropertyModel.getTestCaseRowIndexes();
        if (isSmokeTests) {
            return new TestCaseIndexesModel(indexes.getSmokeTestCaseIndexFrom(), indexes.getSmokeTestCaseIndexTo());
        } else {
            return new TestCaseIndexesModel(indexes.getDefaultCaseIndexFrom(), indexes.getDefaultCaseIndexTo());
        }
    }

}
