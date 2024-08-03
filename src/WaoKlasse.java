import java.math.BigDecimal;

/**
 * Klasse waarin de toegestane waarden voor een betaalpercentage van de WAO zijn vastgelegd.
 * Elke WAO-uitkering is ingedeeld in 1 van deze uitkeringsklassen.
 * Klasse bevat ook een aantal methoden om te berekenen in welke klasse iemand valt met een fictief
 * arbeidsongeschiktheidspercentage (dat door de klasse WaoUitkering wordt vastgesteld op basis van
 * feitelijke verdiensten).
 */
public enum WaoKlasse {
    KLASSE_1 ("14.00","15","25"),
    KLASSE_2 ("21.00","25","35"),
    KLASSE_3 ("28.00","35","45"),
    KLASSE_4 ("35.00","45","55"),
    KLASSE_5 ("42.00","55","65"),
    KLASSE_6 ("50.75","65","80"),
    KLASSE_7 ("75.00","80","100");

    private BigDecimal percentage;
    private BigDecimal ondergrens;
    private BigDecimal bovengrens;

    WaoKlasse(String percentage, String ondergrens, String bovengrens) {
        this.percentage = BigDecimalUtil.create(percentage);
        this.ondergrens = BigDecimalUtil.create(ondergrens);
        this.bovengrens = BigDecimalUtil.create(bovengrens);
    }
    public BigDecimal getOndergrens() {
        return ondergrens;
    }

    public BigDecimal getBovengrens() {
        return bovengrens;
    }

    /** Berekent de fictieve uitkeringsklasse, ofwel de klasse waarnaar betaald wordt.
     * Dit wordt gebaseerd op het aoPercentage, en is maximaal gelijk aan het percentage
     * van de gegeven WaoKlasse.
     * @param aoPercentage Positief getal, maximaal 100. Het percentage inkomensverlies ten
     * opzichte van het (actuele) maatmanloon.
     * @return WaoKlasse waar cliënt (maximaal) in valt met gegeven aoPercentage.
     * Als aoPercentage lager is dan de ondergrens van KLASSE_1, returns null.
     */
    private static WaoKlasse getUitkeringsKlasse(BigDecimal aoPercentage) {
        for (WaoKlasse u : values()) {
            if (aoPercentage.compareTo(u.getOndergrens()) >= 0) { // gelijk aan of groter dan ondergrens
                if (aoPercentage.compareTo(u.getBovengrens()) < 0) { // kleiner dan bovengrens
                    return u;
                }
            }
        }
        return null;
    }

    /**
     * Methode die het uitkeringspercentage teruggeeft dat hoort bij de klasse.
     * Dat is niet (altijd) gelijk aan het betaalpercentage.
     * @return Het percentage dat hoort bij het WaoKlasse object. Dit is het maximale percentage
     * van het rekendagloon dat wordt uitgekeerd voor een uitkering in deze klasse.
     */
    public BigDecimal getUitkeringspercentage() {
        return percentage;
    }

    /**
     * Geeft het uitkeringspercentage dat hoort bij het gegeven aoPercentage. Dit is de hoogte
     * van de betaling bij de verdiensten die gebruikt zijn om aoPercentage te berekenen.
     * @param aoPercentage Positief getal, maximaal 100. Het percentage inkomensverlies ten
     * opzichte van het (actuele) maatmanloon.
     * @return BigDecimal uitkeringspercentage horende bij klasse. Als er geen passende
     * klasse is, returns 0.00.
     */
    public static BigDecimal getBetaalPercentage(BigDecimal aoPercentage) {
        WaoKlasse uitkeringsklasse = getUitkeringsKlasse(aoPercentage);
        if (uitkeringsklasse == null) {
            return BigDecimalUtil.create("0.00");
        }
        return uitkeringsklasse.getUitkeringspercentage();
    }
}
