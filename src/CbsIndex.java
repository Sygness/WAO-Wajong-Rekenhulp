import java.math.BigDecimal;

/**
 * Klasse om data van een regel uit de cbs-index bron opslaan
 * en beschikbaar maken voor verdere toepassingen.
 *
 * De index opgeslagen in een object van deze klasse is niet exact gelijk
 * aan de CBS-index. Die heeft immers maar een precisie van 1 decimaal.
 * Hij wordt in deze klasse opgeslagen alsof hij 2 decimalen heeft (en dus exacter
 * dan de werkelijkheid) omdat dat nodig is om er mee door te rekenen. Het is
 * makkelijker in 1x te converteren dan opnieuw in elke bewerking.
 */
public class CbsIndex {

    private String maand;
    private BigDecimal index;

    public CbsIndex(String maand, BigDecimal index) {
        this.maand = maand;
        this.index = index;
    }

    public String getMaand() {
        return maand;
    }

    public BigDecimal getIndex() {
        return index;
    }

    public String toString() {
        String cbsindex = "De CBS-index voor " + getMaand() + " is " + getIndex() + ".";
        return cbsindex;
    }
}