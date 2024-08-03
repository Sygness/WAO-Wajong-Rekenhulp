import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Klasse berekent de hoogte van de Wajong-uitkering bij gegeven verdiensten.
 * De berekening geldt alleen voor cliënten die 21 jaar of ouder zijn, en vanaf
 * 1 januari 2021.
 */
public class WajongUitkering extends Uitkering {

    private ArrayList<Grondslag> grondslagData;
    private Boolean arbeidsvermogen = true;
    private BigDecimal PERCENTAGE_ARBEIDSVERMOGEN = BigDecimalUtil.create("0.7");
    private BigDecimal PERCENTAGE_GEEN_ARBEIDSVERMOGEN = BigDecimalUtil.create("0.75");
    private BigDecimal garantiebedrag = null; // niet iedereen heeft een garantiebedrag
    private BigDecimal loonwaarde = null; // alleen van toepassing bij loondispensatie
    private BigDecimal overigeInkomstenPerDag = Uitkering.ZERO; // kan ingesteld worden als er bijv. een andere uitkering is.
    // nog toevoegen aan berekening daguitkering als er tijd is (12 maart 2024)

    // constructor
    public WajongUitkering() {
        super();

        // inlezen Grondslag-gegevens uit bronbestand
        grondslagData = getGrondslagLijst();
    }

    // setters
    public void setArbeidsvermogen(Boolean arbeidsvermogen) {
        this.arbeidsvermogen = arbeidsvermogen;
    }

    public void setGarantiebedrag(String garantiebedrag) {
        this.garantiebedrag = BigDecimalUtil.create(garantiebedrag);
    }

    public void setLoondispensatie(String loonwaardePercentage) {
        this.loonwaarde = BigDecimalUtil.create(loonwaardePercentage);
    }

    public void setOverigeInkomstenPerDag(String overigeInkomstenMnd) { //input als String en per maand
        this.overigeInkomstenPerDag = BigDecimalUtil.divide(BigDecimalUtil.create(overigeInkomstenMnd),UK_DAGEN_IN_MND);
    }

    // getters
    public Boolean getArbeidsvermogen() {
        return arbeidsvermogen;
    }

    public BigDecimal getGarantiebedrag() {
        return garantiebedrag;
    }

    public BigDecimal getLoonwaarde() {
        return loonwaarde;
    }

    public BigDecimal getOverigeInkomstenPerDag() {
        return overigeInkomstenPerDag;
    }

    /**
     * Grondslag die in bronbestand staat is een half jaar geldig met die maand als startmaand.
     * Een berekening over maart 2023 maakt dus gebruik van de grondslag geldig van jan-23 t/m jun-23.
     * @param zoekterm is de te berekenen maand in het format abc-xx (Bijvoorbeeld jan-21).
     * @return De Grondslag voor die maand in euro, als BigDecimal waarde. Geeft null terug
     * als gekozen maand niet beschikbaar is, en BigDecimal 0.00 als input van het verkeerde format is.
     */
    public BigDecimal getGrondslag(String zoekterm) { //zoekterm moet format "jan-24" hebben
        switch (zoekterm.substring(0,3)) {
            case "jan","feb","mrt","apr","mei","jun":
                zoekterm = "jan" + zoekterm.substring(3);
                break;
            case "jul","aug","sep","okt","nov","dec":
                zoekterm = "jul" + zoekterm.substring(3);
                break;
            default:
                // dit komt nooit voor als GUI input goed controleert
                return Uitkering.ZERO; // niet null om te onderscheiden van niet op lijst
        }

        for (Grondslag g : grondslagData) {
            if (g.getStartmaand().contentEquals(zoekterm)) {
                return g.getGrondslag();
            }
        }
        System.out.println("Grondslag niet gevonden voor opgegeven maand " + zoekterm + ".");
        return null;
    }

    /**
     * Bereken de uitkering per dag van deze Wajong-uitkering bij nihil inkomsten.
     * Omdat er ook geen maand is opgegeven waarover gerekend kan worden, haalt
     * de methode de laatste bekende grondslag op uit de lijst.
     * @return Wajong-uitkering per dag in euro bij meest recente grondslag en zonder inkomsten.
     */
    public BigDecimal getDaguitkering() {
        // Rekenmaand is nodig om grondslag op te halen.
        // In geval dat deze niet gegeven wordt, uitgaan van meest recente
        BigDecimal rekenGrondslag = grondslagData.get(grondslagData.size()-1).getGrondslag();
        // BigDecimal rekenGrondslag = grondslagData.getLast.getGrondslag(); // getLast() niet bruikbaar voor JDK 21.
        BigDecimal dagbedrag;

        if (arbeidsvermogen) {
            dagbedrag = BigDecimalUtil.multiply(rekenGrondslag,PERCENTAGE_ARBEIDSVERMOGEN);
        }
        else {
            dagbedrag = BigDecimalUtil.multiply(rekenGrondslag,PERCENTAGE_GEEN_ARBEIDSVERMOGEN);
        }

        if (getGarantiebedrag() == null) {
            return dagbedrag;
        }
        else {
            if (getGarantiebedrag().compareTo(dagbedrag) > 0) {  // GAR is hoger dan berekend bedrag
                return getGarantiebedrag();
            }
            else {
                return dagbedrag;
            }
        }
    }

    /**
     * Bereken de Wajong-uitkering per dag in maand rekenmaand.
     * @param rekenmaand = maand waarover de uitkering berekend moet worden
     * @return BigDecimal met het bedrag aan Wajong-uitkering per dag in euro met de
     * grondslag in de opgegeven maand en zonder inkomsten.
     */
    public BigDecimal getDaguitkering(String rekenmaand) {
        // Get grondslag voor rekenmaand
        BigDecimal rekenGrondslag = getGrondslag(rekenmaand);
        if (rekenGrondslag == null) {
            return null;
        }

        BigDecimal dagbedrag;

        if (arbeidsvermogen) {
            dagbedrag = BigDecimalUtil.multiply(rekenGrondslag,PERCENTAGE_ARBEIDSVERMOGEN);
        }
        else {
            dagbedrag = BigDecimalUtil.multiply(rekenGrondslag,PERCENTAGE_GEEN_ARBEIDSVERMOGEN);
        }

        if (garantiebedrag == null) {
            return dagbedrag;
        }
        else {
            if (garantiebedrag.compareTo(dagbedrag) > 0) {  // GAR is hoger dan berekend bedrag
                return garantiebedrag;
            }
            else {
                return dagbedrag;
            }
        }
    }

    /**
     * Bereken de Wajong-uitkering per dag in maand rekenmaand bij de opgegeven inkomsten.
     * @param verdiensten de inkomsten in de maand waarover wordt gerekend.
     * @param rekenmaand de maand waarover de inkomsten zijn genoten en de uitkering moet worden berekend.
     * @return BigDecimal met het bedrag aan Wajong-uitkering per dag in euro met de opgegeven inkomsten
     * bij de grondslag in de opgegeven maand.
     */
    public BigDecimal getDaguitkering(String verdiensten, String rekenmaand) {
        // Get grondslag voor rekenmaand
        BigDecimal rekenGrondslag = getGrondslag(rekenmaand);
        if (rekenGrondslag == null) {
            return null;
        }

        BigDecimal dagbedrag; // tijdelijke opslag voor berekend bedrag uitkering per dag
        // verdiensten per dag zijn verdiensten per maand / 21.75
        BigDecimal verdienstenPerDag = BigDecimalUtil.divide(BigDecimalUtil.create(verdiensten),Uitkering.UK_DAGEN_IN_MND);

        if (arbeidsvermogen) { // hier controleren op loondispensatie (niet toegestaan bij geen arbeidsvermogen)
            if (loonwaarde == null) { // geen loondispensatie
                // in dit geval niet nodig verschil te maken tussen verdiensten en overige inkomsten, optellen
                verdienstenPerDag = verdienstenPerDag.add(overigeInkomstenPerDag);
                // 0.7 * (grondslag - verdiensten)
                dagbedrag = BigDecimalUtil.multiply(rekenGrondslag.subtract(verdienstenPerDag),PERCENTAGE_ARBEIDSVERMOGEN);
            }
            else { // wel loondispensatie
                dagbedrag = berekeningLoondispensatie(rekenGrondslag,verdienstenPerDag);
            }
        }
        else { // geen arbeidsvermogen
            // ook in dit geval niet nodig verschil te maken tussen verdiensten en overige inkomsten, optellen
            verdienstenPerDag = verdienstenPerDag.add(overigeInkomstenPerDag);
            // 0.75 * (grondslag - verdiensten)
            dagbedrag = BigDecimalUtil.multiply(rekenGrondslag.subtract(verdienstenPerDag),PERCENTAGE_GEEN_ARBEIDSVERMOGEN);
        }

        // nu nog vergelijken met het garantiebedrag (dat niet 0 mag zijn), en of bedrag lager is dan nul.
        if (garantiebedrag == null) {
            if (dagbedrag.compareTo(Uitkering.ZERO) < 0) { // dagbedrag is negatief
                return Uitkering.ZERO;
            }
            else {
                return dagbedrag;
            }
        }
        else {
            if (garantiebedrag.compareTo(dagbedrag) > 0) {  // GAR is hoger dan berekend bedrag
                return garantiebedrag;
            }
            else {
                if (dagbedrag.compareTo(Uitkering.ZERO) < 0) { // dagbedrag is negatief
                    return Uitkering.ZERO;
                }
                else {
                    return dagbedrag;
                }
            }
        }
    }

    /**
     * Berekent de daguitkering volgens de 2 methoden die gelden bij loondispensatie, en geeft het hoogste
     * van de 2 bedragen terug. Berekening maakt verschil tussen gedispenseerd loon en overige inkomsten.
     * Overige inkomsten zijn geen parameter, maar zijn opgeslagen als eigenschap van het WajongUitkering object.
     * Deze berekening is kloppend vanaf 1-1-22. Over de periode 1-1-21 tot 1-1-22 gold een andere rekenwijze.
     * @param grondslag De grondslag die al is opgehaald door bovenliggende methode
     * @param inkomstenPerDag Inkomsten al omgezet naar een bedrag per dag door bovenliggende methode
     * @return Dagbedrag aan uitkering berekend met methoden voor loondispensatie
     */
    private BigDecimal berekeningLoondispensatie(BigDecimal grondslag,BigDecimal inkomstenPerDag) {
        BigDecimal rekenLoonwaarde = loonwaarde.divide(HONDERD,4,RoundingMode.HALF_UP); // scale naar 4 anders dataverlies
        /* TOELICHTING BEREKENING:
        - compensatiefactor = (loonwaarde - 0.3) / (0.7 * loonwaarde) --> Dit mag niet worden afgerond, niet aanmaken via Util.
        1. Berekening LD1 = 0.7 * (grondslag - (compensatiefactor * inkomstenPerDag) --> we korten minder dan de normale 70%
        2. Berekening LD2 = (inkomstenPerDag / loonwaarde) - inkomstenPerDag --> aanvullen tot normloon
        3. Hoogste waarde komt tot betaling.

        Stap 1 heeft een uitgebreidere versie voor het geval er naast het gedispenseerd inkomen ook andere inkomsten zijn.
        Dat is het geval bij samenloop van uitkeringen, of meerdere dienstverbanden waarbij cliënt niet overal LD heeft.
        Stap 1 wordt dan: 0.7 * (grondslag - ((cf - gedispenseerd inkomen) + overig inkomen))

        Stap 2 heeft in theorie ook een andere berekening bij overige inkomsten, maar die komt op hetzelfde neer
        als de basisberekening. Overige inkomsten worden buiten beschouwing gelaten. */

        BigDecimal bedragLoondispensatie1 = BigDecimalUtil.multiply(PERCENTAGE_ARBEIDSVERMOGEN,(grondslag.
                subtract((getCompensatiefactor(rekenLoonwaarde).multiply(inkomstenPerDag)).add(overigeInkomstenPerDag))));
        // compensatiefactor-berekening

        BigDecimal bedragLoondispensatie2 = BigDecimalUtil.divide(inkomstenPerDag,rekenLoonwaarde).subtract(inkomstenPerDag);
        // aanvulling tot normloon

        if (bedragLoondispensatie1.compareTo(bedragLoondispensatie2) > 0) { // LD1 > LD2
            return bedragLoondispensatie1;
        }
        else {
            return bedragLoondispensatie2;
        }
    }

    /**
     * Bereken de compensatiefactor die nodig is voor berekening loondispensatie regel 1.
     * @param loonwaarde als decimale waarden tussen 0 en 1.
     * @return Compensatiefactor als BigDecimal die niet is afgerond.
     */
    private BigDecimal getCompensatiefactor(BigDecimal loonwaarde) { // loonwaarde als fractie van 1
        // compensatiefactor = (loonwaarde - 0.3) / (0.7 * loonwaarde)
        // Dit is de enige waarde die niet mag worden afgerond
        BigDecimal noemer = loonwaarde.subtract(BigDecimalUtil.create("0.3"));
        BigDecimal deler = BigDecimalUtil.create("0.7").multiply(loonwaarde);

        return noemer.divide(deler,12, RoundingMode.HALF_UP); // slaat op tilt zonder scale en rounding mode.
        // voor deze toepassing is dit voldoende "niet afgerond".
    }

    /**
     * Inlezen van de grondslag-gegevens die beschikbaar zijn in het bronbestand WajongGrondslag.txt.
     * Deze methode wordt alleen gebruikt in de constructor en is daarom private.
     * @return ArrayList met alle beschikbare grondslag gegevens opgeslagen als Grondslag-objecten.
     */
    private ArrayList<Grondslag> getGrondslagLijst() {
        Scanner inputStream = null;
        grondslagData = new ArrayList<>();

        try
        {
            inputStream = new Scanner(new FileInputStream("src/resources/WajongGrondslag.txt"));
        }
        catch (FileNotFoundException e)
        {
            System.out.println("Bestand WajongGrondslag.txt niet gevonden");
            System.out.println("of niet kunnen openen.");
            System.exit(0);
        }

        // uitlezen van het bestand en data in een ArrayList met Grondslag objecten zetten.
        while (inputStream.hasNextLine()) {
            Grondslag item = new Grondslag(inputStream.next(),BigDecimalUtil.create(inputStream.next()));
            // System.out.println(item); // testregel, later verwijderen
            grondslagData.add(item);
        }
        inputStream.close();
        return grondslagData;
    }

    /**
     * Haalt de periode op waarover grondslagwaarden bekend zijn, voor informatie
     * aan gebruiker.
     * @return String met de periode waarover grondslag (ofwel het Wettelijk Minimumloon excl. VT per dag)
     * is ingelezen uit het bronbestand.
     */
    public String getGrondslagRange() {
        return "jan-21 t/m " + grondslagData.get(grondslagData.size()-1).getEindmaand();
        // return "jan-21 t/m " + grondslagData.getLast().getEindmaand(); // getLast() niet bruikbaar vóór JDK 21.
    }

    /**
     * Methode om het WajongUitkering object te beschrijven met de opgeslagen eigenschappen.
     * @return beschrijving van het object als String.
     */
    public String toString() {
        String beschrijving = "Wajong-uitkering ";
        if (arbeidsvermogen) {
            beschrijving += "met arbeidsvermogen";
            if (loonwaarde != null) {
                beschrijving += " en loondispensatie met een loonwaarde van " + getalFormat(loonwaarde) + "%";
            }
        }
        else {
            beschrijving += "zonder arbeidsvermogen";
        }
        beschrijving += ".";

        if (garantiebedrag != null) {
            beschrijving += " Garantiebedrag van " + bedragFormat(BigDecimalUtil.multiply(garantiebedrag,UK_DAGEN_IN_MND)) + " per maand.";
        }
        return beschrijving;
    }
}
