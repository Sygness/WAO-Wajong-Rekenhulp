import javax.swing.*;
import java.awt.*;

import static java.lang.Double.parseDouble;

/**
 * Klasse met tools voor het controleren en opschonen van gebruikersinput, en grafische feedback
 * voor diverse typen velden.
 */
public class GuiUtil {

    private static Color REJECTED = Color.RED;

    // input lezen en oppoetsen tools

    /**
     * Methode om een String input te controleren op geschiktheid om omgezet
     * te worden naar een bedrag. Veel voorkomende variaties zoals gebruik van een
     * euroteken of extra spaties worden verwijderd. Een komma als scheidingsteken
     * wordt vervangen door een punt.
     * @param input String die later omgezet moet worden naar een bedrag
     * @return De inputstring in een geschikt format (xxxx.xx, waarbij x cijfers zijn van 0 tot 9).
     * Returns null als input null is of ook na bovengenoemde bewerkingen niet als
     * Double geïnterpreteerd kan worden.
     */
    public static String bedragCleanup(String input) {
        if (input == null) {
            return null;
        }

        String outputString = input.replace('€',' '); // evt euroteken weg
        outputString = outputString.strip(); // spaties e.d. voor en achter weg
        outputString = outputString.replace(',','.'); //decimaalteken moet een . zijn

        try {
            /* test voor een aantal andere mogelijkheden voor foute invoer,
            zoals alleen spaties, letters, andere munteenheden of
            een scheidingsteken voor duizendtallen.
            */
            parseDouble(outputString);
            //test geslaagd
            return outputString;
        }
        catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Maakt van 2 Strings voor maand en jaar één String van het format mnd-jr (jan-24).
     * @param maandinput maand volledig uitgeschreven als String
     * @param jaarinput jaartal uitgeschreven als String
     * @return String die weergave is van de ingegeven maand in het format mnd-jr (bijv. jan-24).
     */
    public static String getRekenmaand(String maandinput, String jaarinput) {
        String maanddeel = maandinput.substring(0,3);
        String jaardeel = jaarinput.substring(2,4);
        if (maanddeel.equals("maa")) {
            maanddeel = "mrt";
        }
        return maanddeel + "-" + jaardeel;
    }

    /**
     * Controle of ingevulde waarde een positieve BigDecimal oplevert.
     * @param getal als String
     * @return true als waarde groter dan 0 is, anders false.
     */
    public static Boolean isPositive(String getal) {
        if (getal == null) {
            return false;
        }
        else {
            if (BigDecimalUtil.create(getal).compareTo(Uitkering.ZERO) > 0) {
                return true;
            }
            else {
                return false;
            }
        }
    }

    /**
     * Methode om te controleren of gegeven input (te lezen als getal) een waarde heeft die als
     * percentage gelezen kan worden, positief getal van maximaal 100.
     * @param getal als String
     * @return true als waarde > 0 en maximaal 100.00 is
     */
    public static Boolean isPercentage(String getal) {
        if (isPositive(getal)) {
            if (BigDecimalUtil.create(getal).compareTo(Uitkering.HONDERD) < 1) {
                return true;
            }
            else {
                return false;
            }
        }
        else {
            return false; // dit triggert ook als input leeg is door controle in isPositive().
        }
    }

    // JTextField tools
    public static void inputAccepted(JTextField textfield) {
        textfield.setBorder(BorderFactory.createLoweredSoftBevelBorder());
        textfield.setEditable(false);
    }

    public static void inputRejected(JTextField textfield) {
        textfield.setBorder(BorderFactory.createLineBorder(REJECTED, 2));
        textfield.setEditable(true);
    }

    public static void reset(JTextField textfield) {
        textfield.setBorder(BorderFactory.createLoweredSoftBevelBorder());
        textfield.setEditable(true);
        textfield.setText("");
    }

    // JComboBox tools
    public static void inputAccepted(JComboBox combobox) {
        combobox.setBorder(BorderFactory.createEmptyBorder());
        combobox.setEnabled(false);
    }

    public static void inputRejected(JComboBox combobox) {
        combobox.setBorder(BorderFactory.createLineBorder(REJECTED,2));
        combobox.setEnabled(true);
    }

    public static void reset(JComboBox combobox) {
        combobox.setBorder(BorderFactory.createEmptyBorder());
        combobox.setEnabled(true);
        combobox.setSelectedIndex(0);
    }

    // JCheckBox tools
    public static void inputAccepted(JCheckBox checkbox) {
        checkbox.setEnabled(false);
    }

    public static void reset(JCheckBox checkbox) {
        checkbox.setEnabled(true);
        checkbox.setSelected(false);
    }
}