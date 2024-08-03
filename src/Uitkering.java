import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

/** Klasse met algemene eigenschappen en methoden die nodig zijn voor elke
 * uitkering. Klasse is abstract omdat berekening van de uitkering per dag
 * pas specifiek geïmplementeerd kan worden als bekend is over welke uitkering
 * het gaat, en deze klasse voor berekening van de maanduitkering dus afhankelijk
 * is van de implementatie in de subklassen.
 */
public abstract class Uitkering {
    // Constanten voor correcte output
    private static final NumberFormat nf = NumberFormat.getInstance(Locale.GERMANY);
    // decimaal naar komma voor String output, niet voor currency instance gekozen omdat
    // het euro-teken dan aan het bedrag wordt vastgeplakt.
    public static final BigDecimal UK_DAGEN_IN_MND = BigDecimalUtil.create("21.75");
    // Gemiddeld aantal uitkeringsdagen per maand. (261 per jaar, gedeeld door 12, is 21.75)
    public static final BigDecimal MAXIMUM_DAGLOON = BigDecimalUtil.create("274.44"); // per jan-24
    /* maximum uitkeringsdagloon, update 2x per jaar op 1 januari en 1 juli.
    Wajong gebruikt deze niet dus in dit programma enkel relevant voor WAO, maar omdat het
    voor andere uitkeringen wel relevant kan zijn toch in de abstracte klasse gezet. */
    public static final BigDecimal ZERO = BigDecimalUtil.create("0");
    // controle voor waarden die positief moeten zijn.
    public static final BigDecimal HONDERD = BigDecimalUtil.create("100");
    // deler om standaard representatie van percentage naar feitelijke waarde te krijgen (fractie van 1)
    public static final BigDecimal WEKEN_IN_JAAR = BigDecimalUtil.create("52.2");
    // nodig om weeklonen om te zetten naar een bedrag per maand
    public static final BigDecimal MAANDEN_IN_JAAR = BigDecimalUtil.create("12");
    // nodig om weeklonen om te zetten naar een bedrag per maand
    public static final BigDecimal FOUR = BigDecimalUtil.create("4");
    // nodig om vierwekenlonen om te zetten naar een bedrag per maand

    public Uitkering() {
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
    }

    /**
     * Berekent de maanduitkering als er een verdiensten zijn. Werkt in deze implementatie alleen voor
     * WAO-uitkeringen, omdat in dat geval enkel het rekendagloon wordt gebruikt, en geen onderliggende
     * datum wordt meegenomen. Voor Wajong-uitkeringen is altijd een rekenmaand nodig, omdat de grondslag
     * daarvan afhankelijk is.
     * @return bruto maanduitkering in euro
     */
    public BigDecimal getMaanduitkering() {
        return BigDecimalUtil.multiply(getDaguitkering(),UK_DAGEN_IN_MND);
    }

    /**
     * Berekent de maanduitkering zonder verdiensten over de gegeven rekenmaand.
     * @param rekenmaand is de maand waarover gerekend moet worden, format mnd-jr als String.
     * @return bruto maanduitkering in euro
     */
    public BigDecimal getMaanduitkering(String rekenmaand) {
        return BigDecimalUtil.multiply(getDaguitkering(rekenmaand),UK_DAGEN_IN_MND);
    }

    /**
     * Berekening maanduitkering als er verdiensten zijn.
     * @param verdiensten is het brutobedrag van de inkomsten waarmee rekening wordt gehouden.
     * @param rekenmaand is de maand waarover gerekend moet worden, format mnd-jr als String.
     * @return bruto maanduitkering in euro
     */
    public BigDecimal getMaanduitkering(String verdiensten, String rekenmaand) {
        return BigDecimalUtil.multiply(getDaguitkering(verdiensten, rekenmaand),UK_DAGEN_IN_MND);
    }

    public abstract BigDecimal getDaguitkering();

    public abstract BigDecimal getDaguitkering(String rekenmaand);
    public abstract BigDecimal getDaguitkering(String verdiensten, String rekenmaand);

    public static BigDecimal getMaximumDagloon() { // nodig om afgeleide klasse toegang tot deze constante te geven
        return MAXIMUM_DAGLOON;
    }

    /**
     * Methode controleert of het opgegeven uitkeringsdagloon een positieve waarde is van
     * maximaal het wettelijk vastgestelde uitkeringsdagloon, de in deze klasse opgeslagen
     * constante MAXIMUM_DAGLOON.
     * @param uitkeringsdagloon is het ingevulde bedrag als String
     * @return true als waarde geaccepteerd is, anders false.
     */
    public static Boolean acceptedDagloon(String uitkeringsdagloon) {
        if (uitkeringsdagloon == null) {
            return false;
        }
        else {
            BigDecimal dagloon = BigDecimalUtil.create(uitkeringsdagloon);
            if ((dagloon.compareTo(getMaximumDagloon()) <= 0) && (dagloon.compareTo(Uitkering.ZERO) > 0)) {
                return true;
            } else { // opgegeven uitkeringsdagloon is te hoog, 0, of negatief
                return false;
            }
        }
    }

    /**
     * Methode om inkomsten per week of per 4 weken om te rekenen naar een bedrag per maand.
     * Omdat de rest van de applicatie doorrekent vanuit een input als String, wordt de uitkomst
     * van de berekening weer omgezet naar een String.
     * @param wekenbedrag het bedrag aan inkomsten
     * @param aantalweken het aantal weken waarover het wekenbedrag wordt verdiend, mag enkel 1 of 4 zijn.
     * @return Het omgerekende inkomen per maand als String.
     */
    public static String inkomstenWeekNaarMaand(String wekenbedrag, int aantalweken) { // 1 of 4 weken
        // 52.2 weken per jaar. Maandloon = weekloon x 52.2 / 12.
        // Conversie terug naar String omdat methoden voor berekening maanduitkering String input gebruiken
        BigDecimal inkomen = BigDecimalUtil.create(wekenbedrag);

        if (aantalweken == 1) {
            inkomen = BigDecimalUtil.divide(BigDecimalUtil.multiply(inkomen,WEKEN_IN_JAAR),MAANDEN_IN_JAAR);
            return inkomen.toPlainString();
        }
        else if (aantalweken == 4) {
            inkomen = BigDecimalUtil.divide(BigDecimalUtil.divide(BigDecimalUtil.multiply(inkomen,WEKEN_IN_JAAR),MAANDEN_IN_JAAR),FOUR);
            return inkomen.toPlainString();
        }
        else { // andere input dan 1 of 4 voor aantalweken is niet mogelijk in GUI-implementatie
            return inkomen.toPlainString();
        }
    }

    /**
     * Methode om een bedrag weer te geven als String van het format € x.xxx,xx
     * @param bedrag is de waarde die moet worden omgezet naar een String.
     * @return Het ingevoerde getal als String van het format "€ x.xxx,xx".
     */
    public static String bedragFormat(BigDecimal bedrag) {
        return "€ " + nf.format(bedrag);
    }

    /**
     * Methode om een getal weer te geven als String van het format x.xxx,xx.
     * Wordt in deze applicatie enkel gebruikt voor percentages.
     * @param getal dat moet worden omgezet naar een String.
     * @return het ingevoerde getal als String van het format "x.xxx,xx".
     */
    public static String getalFormat(BigDecimal getal) {
        return nf.format(getal);
    }
}
