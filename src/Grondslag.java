import java.math.BigDecimal;

/**
 * Klasse om data van een regel uit de Wajong-grondslag bron opslaan
 * en beschikbaar maken voor verdere toepassingen. Omdat de grondslag
 * van toepassing is over een half jaar dat begint met de maand in het
 * bronbestand wordt in de constructor een eindmaand toegevoegd die
 * wordt gebruikt voor feedback aan de gebruiker over de beschikbare
 * range.
 */
public class Grondslag {

    private String startmaand;
    private String eindmaand;
    private BigDecimal grondslag;

    public Grondslag(String startmaand, BigDecimal grondslag) {
        this.startmaand = startmaand;
        this.grondslag = grondslag;
        if (startmaand.substring(0,3).contentEquals("jan")) {
            this.eindmaand = "jun" + startmaand.substring(3);
        }
        else {
            this.eindmaand = "dec" + startmaand.substring(3);
        }
    }

    public String getStartmaand() {
        return startmaand;
    }

    public String getEindmaand() {
        return eindmaand;
    }

    public BigDecimal getGrondslag() {
        return grondslag;
    }

    public String toString() {
        String grondslag = "De Wajong-grondslag voor " + getStartmaand() + " is € " + getGrondslag() + ".";
        return grondslag;
    }
}