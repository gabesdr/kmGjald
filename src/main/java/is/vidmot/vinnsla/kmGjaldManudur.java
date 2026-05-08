package is.vidmot;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Manudur - data class sem geymir eina kílómetraskráningu.
 *
 * Klasi: einföld gögn án logikar. Notað til að halda utan um sögu skráninga
 * í kmGjaldController. Eitt stak í lista = einn skráður mánuður.
 *
 * Höfum gildin "final" til að koma í veg fyrir að einhver breyti þeim eftir á - þetta
 * gerir klasann immutable (óbreytanlegan), sem er öruggara og einfaldara í notkun.
 *
 * author: Gabríel del Rosario
 */
public class kmGjaldManudur {
    private final LocalDate dagsetning;   // dagurinn sem skráningin var gerð
    private final String flokkur;          // "A", "B" eða "C"
    private final int thyngd;              // þyngd í kg (0 ef A eða B)
    private final int upphaf;              // upphafsstaða km
    private final int lokKm;               // lokastaða km
    private final int eknirKm;             // ekið á tímabilinu
    private final double gjald;            // heildargjald fyrir tímabilið (kr)
    private final double gjaldPerKm;       // gjald á km á tímabilinu (kr/km)

    public kmGjaldManudur(LocalDate dagsetning, String flokkur, int thyngd,
                   int upphaf, int lokKm, double gjaldPerKm) {
        this.dagsetning = dagsetning;
        this.flokkur = flokkur;
        this.thyngd = thyngd;
        this.upphaf = upphaf;
        this.lokKm = lokKm;
        this.eknirKm = lokKm - upphaf;
        this.gjaldPerKm = gjaldPerKm;
        this.gjald = eknirKm * gjaldPerKm;
    }

    // Getters
    public LocalDate getDagsetning() { return dagsetning; }
    public String getFlokkur()       { return flokkur; }
    public int getThyngd()           { return thyngd; }
    public int getUpphaf()           { return upphaf; }
    public int getLokKm()            { return lokKm; }
    public int getEknirKm()          { return eknirKm; }
    public double getGjald()         { return gjald; }
    public double getGjaldPerKm()    { return gjaldPerKm; }

    /**
     * Snýr þessari skráningu í læsilegan streng fyrir ListView.
     * ListView notar toString() sjálfkrafa þegar engin custom cell factory er sett.
     *
     * Dæmi: "2026-05-08 · B  ·  42,000 → 43,200 (1,200 km)  ·  8,340 kr"
     */
    @Override
    public String toString() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        // Ef flokkur er C, sýnum þyngd með í sviga
        String flokkurTexti = flokkur.equals("C")
                ? "C (" + String.format("%,d", thyngd) + " kg)"
                : flokkur;

        return String.format("%s · %s  ·  %,d → %,d (%,d km)  ·  %,.2f kr",
                dtf.format(dagsetning), flokkurTexti, upphaf, lokKm, eknirKm, gjald);
    }
}
