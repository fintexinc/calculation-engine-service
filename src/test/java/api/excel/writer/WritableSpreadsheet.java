package api.excel.writer;

import api.dto.RequestParamsSupplier;
import api.exception.TestException;
import com.fintex.ce.dto.holding.Holding;
import org.apache.poi.ss.usermodel.Workbook;

import java.util.List;

public interface WritableSpreadsheet {

    default void write(final List<Holding> holdings, final RequestParamsSupplier params, final Workbook workbook) {
        throw new TestException("Not Implemented");
    }

}
