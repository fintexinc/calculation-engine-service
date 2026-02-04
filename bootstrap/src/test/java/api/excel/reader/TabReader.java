package api.excel.reader;

import org.apache.poi.ss.usermodel.Workbook;

public interface TabReader<T> {

  T read(final Workbook workbook);

}
