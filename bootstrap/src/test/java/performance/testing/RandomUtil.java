package performance.testing;

import com.fintex.ce.domain.enumeration.Currency;

import java.util.Random;

import static com.fintex.ce.domain.enumeration.Currency.CAD;
import static com.fintex.ce.domain.enumeration.Currency.USD;

public class RandomUtil {

  public static int getRandomInt(final int lowerBound, final int upperBound) {
    return new Random().nextInt(upperBound - lowerBound) + lowerBound;
  }

  public static Currency getCurrency() {
    return getRandomInt(0, 1) == 0 ? CAD : USD;
  }

}
