import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Klasse berekent de hoogte van de WAO-uitkering bij gegeven verdiensten.
 */
public class WaoUitkering extends Uitkering {
    private BigDecimal rekendagloon;
    private BigDecimal maatmanloon; // dit is het maatmanloon per standaarddatum 1 januari 2015, mag leeg zijn.
    private WaoKlasse klasse;
    private BigDecimal uitkeringspercentage;
    private ArrayList<CbsIndex> indexData;

    private final BigDecimal VT_DELER = BigDecimalUtil.create("1.08"); // WAO specifiek
    // VT is standaard 8%, dus een totaal incl. VT delen door 1.08 geeft basis bedrag zonder VT.
    // maatmanloon, rekendagloon en opgegeven verdiensten voor WAO zijn inclusief VT.

    public WaoUitkering(String rekendagloon, int uitkeringsklasse) {
        super();
        setRekendagloon(rekendagloon);
        setKlasse(uitkeringsklasse);

        uitkeringspercentage = klasse.getUitkeringspercentage();

        // inlezen CBS-index gegevens uit bronbestand
        indexData = getCbsIndexLijst();

        maatmanloon = null;
    }

    public void setRekendagloon(String rekendagloon) {
        // rekendagloon kan niet hoger zijn dan maximaal uitkeringsdagloon, en moet positief zijn.
        // in GUI implementatie is deze controle niet meer nodig
        if (Uitkering.acceptedDagloon(rekendagloon)) {
            this.rekendagloon = BigDecimalUtil.create(rekendagloon);
        }
        else {
            // opgegeven rekendagloon is te hoog, 0, of negatief
            this.rekendagloon = null;
        }
    }

    public BigDecimal getRekendagloon() {
        return rekendagloon;
    }

    public void setKlasse(int uitkeringsklasse) {
        switch (uitkeringsklasse) {
            case 1:
                klasse = WaoKlasse.KLASSE_1;
                break;
            case 2:
                klasse = WaoKlasse.KLASSE_2;
                break;
            case 3:
                klasse = WaoKlasse.KLASSE_3;
                break;
            case 4:
                klasse = WaoKlasse.KLASSE_4;
                break;
            case 5:
                klasse = WaoKlasse.KLASSE_5;
                break;
            case 6:
                klasse = WaoKlasse.KLASSE_6;
                break;
            case 7:
                klasse = WaoKlasse.KLASSE_7;
                break;
            default:
                klasse = null;
                System.out.println("Uw WAO-klasse is een geheel getal van minimaal 1 en maximaal 7.");
                break;
        }
    }

    public BigDecimal getMaatmanloon() {
        return maatmanloon;
    }

    public void setMaatmanloon(String maatmanloon) {
        if (GuiUtil.isPositive(maatmanloon)) { // dit geeft ook false als input null is, niet nodig in GUI implementatie
            this.maatmanloon = BigDecimalUtil.create(maatmanloon);
        }
        else {
            this.maatmanloon = null; // was het al, maar voor de duidelijkheid, dat blijft dus zo
        }
    }

    /**
     * Methode om het WaoUitkering object te beschrijven met de opgeslagen eigenschappen.
     * @return beschrijving van het object als String.
     */
    public String toString() {
        String beschrijving = "WAO-uitkering met een rekendagloon van " + bedragFormat(rekendagloon)
                + " en een maximaal uitkeringspercentage van " + getalFormat(klasse.getUitkeringspercentage()) +"%.";
        return beschrijving;
    }

    /**
     * Zoekt in de indexData lijst naar de index die hoort bij de opgegeven maand.
     * @param actueleMaand is de maand die is opgegeven om mee te rekenen, in het format abx-xx (bijv. jan-24).
     * @return De CBS-index die hoort bij de maand waarnaar is gezocht. Geeft null terug als de opgegeven
     * maand niet beschikbaar is.
     */
    public BigDecimal findIndex(String actueleMaand) {
        String zoekterm = actueleMaand.toLowerCase();

        for (CbsIndex c : indexData) {
            if (c.getMaand().contentEquals(zoekterm)) {
                return c.getIndex();
            }
        }
        return null;
    }

    /**
     * Methode rekent het maatmanloon dat hoort bij de uitkering om naar de maand waarover de uitkering berekend
     * moet worden. Dat is nodig om het te kunnen vergelijken met de inkomsten over die maand.
     * @param actueleMaand de maand waarnaar het maatmanloon moet worden omgezet.
     * @return Het bedrag van het maatmanloon in de actueleMaand.
     */
    private BigDecimal getActueelMaatmanloon(String actueleMaand) {
        BigDecimal nieuweIndex = findIndex(actueleMaand);
        if (nieuweIndex == null) {
            throw new RuntimeException("CBS-index niet gevonden, kan maatmanloon niet correct berekenen.");
        }
        // Indexeren: actueelMaatmanloon = maatmanloonOud*nieuwIndex/oudeIndex (ga voor oude index altijd uit van jan15 en dus 105.6)
        BigDecimal actueelMaatmanloon = BigDecimalUtil.divide(BigDecimalUtil.multiply(maatmanloon,nieuweIndex),BigDecimalUtil.create("105.6"));
        return actueelMaatmanloon;
    }

    /**
     * Het uitkeringspercentage wordt als eigenschap opgeslagen als getal tussen 0 en 100. Voor gebruik in
     * de verdere berekeningen moet het een getal tussen 0 en 1 zijn. Deze methode converteert de opgeslagen
     * waarde naar de versie waarmee gerekend kan worden door te delen door 100.
     * Omdat in het geval van 50,75% de uitkomst meer dan 2 decimalen nodig heeft om te kloppen, wordt
     * in dit geval geen gebruik gemaakt van BigDecimalUtil.
     * @return het uitkeringspercentage als getal tussen 0 en 1.
     */
    public BigDecimal getUitkeringspercentage() {
        return uitkeringspercentage.divide(HONDERD);
    }

    /**
     * Bereken de WAO-uitkering per dag met het vastgelegde rekendagloon en uitkeringspercentage horend
     * bij de klasse van deze uitkering. Gaat er vanuit dat er geen inkomsten zijn.
     * @return De hoogte van de volledige WAO-uitkering in euro per maand.
     */
    public BigDecimal getDaguitkering() {
        BigDecimal rekendagloonExclVT = BigDecimalUtil.divide(rekendagloon,VT_DELER);
        BigDecimal daguitkering = BigDecimalUtil.multiply(rekendagloonExclVT,getUitkeringspercentage());
        return daguitkering;
    }

    /**
     * Bereken de WAO-uitkering per dag in maand rekenmaand bij de opgegeven inkomsten.
     * @param verdiensten de inkomsten in de maand waarover wordt gerekend.
     * @param rekenmaand de maand waarover de inkomsten zijn genoten en de uitkering moet worden berekend.
     * @return BigDecimal met het bedrag aan WAO-uitkering per dag in euro bij de opgegeven inkomsten.
     */
    public BigDecimal getDaguitkering(String verdiensten, String rekenmaand) {
        BigDecimal inkomsten = BigDecimalUtil.create(verdiensten);
        BigDecimal maatmanloon = getActueelMaatmanloon(rekenmaand);

        // aoPercentage = (maatmanloon - inkomsten) / maatmanloon * 100%.
        BigDecimal aoPercentage = BigDecimalUtil.multiply(BigDecimalUtil.divide(maatmanloon.subtract(inkomsten),maatmanloon),HONDERD);

        // uitkeringspercentage ophalen
        BigDecimal betaalPercentage = klasse.getBetaalPercentage(aoPercentage);
        if (klasse.getBetaalPercentage(aoPercentage).compareTo(uitkeringspercentage) > 0)
            // ao-berekening geeft hoger percentage dan maximale horend bij dit WaoUitkering object.
        {
            betaalPercentage = uitkeringspercentage; // zet naar maximaal.
        }

        BigDecimal rekendagloonExclVT = BigDecimalUtil.divide(rekendagloon,VT_DELER);
        // rekendagloon exclusief VT * betaalpercentage/100 (betaalpercentage wordt niet opgeslagen als fractie van 1, dus nu omzetten)
        BigDecimal daguitkering = BigDecimalUtil.multiply(rekendagloonExclVT,BigDecimalUtil.divide(betaalPercentage,HONDERD));
        return daguitkering;
    }

    /**
     * Methode wordt in deze implementatie niet gebruikt, kan niet worden aangeroepen vanuit de GUI.
     * Is nodig omdat de klasse Wajong-uitkering deze wel gebruikt en er dus ook in deze klasse een versie
     * van moet worden geschreven.
     * In een betere versie van deze applicatie kan deze methode de getDaguitkering() methode vervangen,
     * omdat ook rekendagloon in feite gekoppeld is aan een datum. (Dat is voor elke uitkering het geval,
     * dus Uitkering.getMaanduitkering() vervalt dan ook.
     * Placeholder versie van methode.
     * @param rekenmaand de maand waarover gerekend moet worden
     * @return De hoogte van de uitkering per dag met het rekendagloon van deze WAO-uitkering.
     * Parameter rekenmaand wordt niet gebruikt.
     */
    public BigDecimal getDaguitkering(String rekenmaand) {
        return getDaguitkering(); // placeholder
    }

    /**
     * Haalt de periode op waarover de CBS-index is opgeslagen in indexData, voor informatie
     * aan de gebruiker.
     * @return String met de periode waarover de CBS-index is ingelezen uit het bronbestand. Gaat
     * er vanuit dat er geen waarde ontbreken, geeft range als maand van eerste waarde t/m maand
     * van laatste waarde.
     */
    public String getCbsRange() {
        return "jan-21 t/m " + indexData.get(indexData.size()-1).getMaand();
        // return "jan-21 t/m " + indexData.getLast().getMaand(); // getLast() niet bruikbaar voor JDK 21
    }

    /**
     * Inlezen van de CBS-index gegevens die beschikbaar zijn in het bronbestand CbsIndex.txt.
     * Deze methode wordt alleen gebruikt in de constructor en is daarom private.
     * @return ArrayList met alle beschikbare indexgegevens als CbsIndex objecten.
     */
    private ArrayList<CbsIndex> getCbsIndexLijst() {
        Scanner inputStream = null;
        indexData = new ArrayList<>();

        try
        {
            inputStream = new Scanner(new FileInputStream("src/resources/CbsIndex.txt"));
        }
        catch (FileNotFoundException e)
        {
            System.out.println("Bestand CbsIndex.txt niet gevonden");
            System.out.println("of niet kunnen openen.");
            System.exit(0);
        }

        // uitlezen van het bestand en data in een ArrayList met CbsIndex objecten zetten.
        while (inputStream.hasNextLine()) {
            CbsIndex item = new CbsIndex(inputStream.next(),BigDecimalUtil.create(inputStream.next()));
            //System.out.println(item); // testregel, later verwijderen
            indexData.add(item);
        }
        inputStream.close();
        return indexData;
    }
}