package api.util.excel;

import com.google.common.base.Strings;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Workbook;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class ExcelFormatUtils {
    public static final String DATA_FORMAT_1 = "yyyy-MM-dd";
    public static final String DATA_FORMAT_2 = "MM/dd/yyyy";
    public static final String DATA_FORMAT_3 = "M/dd/yyyy";

    public static final SimpleDateFormat DATE_FORMAT_YYYY_MM_DD = new SimpleDateFormat(DATA_FORMAT_1);
    public static final SimpleDateFormat DATE_FORMAT_MM_DD_YYYY = new SimpleDateFormat(DATA_FORMAT_2);

    public static final DateTimeFormatter DATA_TIME_FORMATTER = DateTimeFormatter.ofPattern(DATA_FORMAT_2);
    public static final DateTimeFormatter DATA_TIME_FORMATTER_2 = DateTimeFormatter.ofPattern(DATA_FORMAT_3);

    private ExcelFormatUtils() {
    }

    public static CellStyle createDateFormat(final Workbook wb, final String dateFormat) {
        final CreationHelper creationHelper = wb.getCreationHelper();

        // Create Cell Style for formatting Date
        CellStyle dateCellStyle = wb.createCellStyle();
        dateCellStyle.setDataFormat(creationHelper.createDataFormat().getFormat(Strings.isNullOrEmpty(dateFormat) ? DATA_FORMAT_2 : dateFormat));

        return dateCellStyle;
    }

    public static LocalDate convertToLocalDateViaInstant(Date dateToConvert) {
        return dateToConvert.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

    public static Date convertToDateViaInstant(LocalDate dateToConvert) {
        return java.util.Date.from(dateToConvert.atStartOfDay()
                .atZone(ZoneId.systemDefault())
                .toInstant());
    }

}
