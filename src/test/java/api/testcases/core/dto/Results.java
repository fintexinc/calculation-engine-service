package api.testcases.core.dto;

import lombok.Data;

/**
 * Contains expected result from EXCEL and actual result from APP
 *
 * @param <E> expected result type
 */
@Data
public class Results<E> {

    // from EXCEL
    private E expected;
    // from APP
    private E actual;

    // if TRUE - then response from APP was 200 OK
    // if FALSE - then response from APP wasn't 200 OK and we can ignore values inside {@link expected} and {@link actual} fields as they are default
    private boolean is2xx;

    public Results() {
        is2xx = false;
    }

    public Results(E expected, E actual) {
        this.expected = expected;
        this.actual = actual;
        this.is2xx = true;
    }

    public Results(E expected, E actual, boolean is2xx) {
        this.expected = expected;
        this.actual = actual;
        this.is2xx = is2xx;
    }
}
