import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Klasse met tools om BigDecimals bij elke bewerking te laten rekenen met
 * de juiste afronding en aantal decimalen. De BigDecimal-klasse zelf vereist
 * dat dit bij elke bewerking opnieuw gedefinieerd wordt. RoundingMode kan worden
 * vastgelegd in een MathContext object, maar scale niet. Daarom deze klasse
 * gemaakt om gemakkelijker de nodige bewerkingen te uitvoeren.
 */
public class BigDecimalUtil {

    private final static int SCALE = 2;
    private final static RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    public static BigDecimal create(String input) {
        return new BigDecimal(input).setScale(SCALE,ROUNDING_MODE);
    }

    public static BigDecimal divide(BigDecimal noemer, BigDecimal deler) {
        return noemer.divide(deler,SCALE,ROUNDING_MODE);
    }

    public static BigDecimal multiply(BigDecimal bedrag1, BigDecimal bedrag2) {
        return bedrag1.multiply(bedrag2).setScale(SCALE,ROUNDING_MODE);
    }
}