package lipid;

import java.util.List;
import java.util.Objects;

import org.drools.ruleunits.api.DataSource;
import org.drools.ruleunits.api.DataStore;
import org.drools.ruleunits.api.RuleUnitData;

public class LipidScoreUnit implements RuleUnitData{

    // !TODO insert here the code to store the data structures containing the facts where the rules will be applied


    private final DataStore<Annotation> annotations;

    public LipidScoreUnit() {
        this(DataSource.createStore());
    }

    public LipidScoreUnit(DataStore<Annotation> annotations) {
        this.annotations = annotations;

    }

    public DataStore<Annotation> getAnnotations() {
        return annotations;
    }



    /*
    private final Lipid lipid;

    private final double score;
    private final double matchedMz;
    private final String adductName;
    private final double intensity; // Intensity of the peak
    private static final double DEFAULT_TOLERANCE = 0.01;

    public LipidScoreUnit(Lipid lipid, double score, double matchedMz, String adductName, double intensity) {
        this.lipid = lipid;
        this.score = score;
        this.matchedMz = matchedMz;
        this.adductName = adductName;
        this.intensity = intensity;
    }

    public Lipid getLipid() {
        return lipid;
    }

    public double getScore() {
        return score;
    }

    public double getMatchedMz() {
        return matchedMz;
    }

    public String getAdductName() {
        return adductName;
    }

    public double getIntensity() {
        return intensity;
    }
    public static double calculateScore(double measuredMz, double expectedMz) {
        double error = Math.abs(measuredMz - expectedMz);
        double normalizedScore = 1.0 - (error / DEFAULT_TOLERANCE);
        if (normalizedScore >= 0.0 && normalizedScore <= 1.0) {
            return normalizedScore;
        } else {
            throw new IllegalArgumentException("Score calculation error: normalized score out of bounds: " + normalizedScore);
        }
    }

    @Override
    public String toString() {
        return "LipidScoreUnit{" +
                "lipid=" + lipid.getName() +
                ", score=" + score +
                ", matchedMz=" + matchedMz +
                ", adduct='" + adductName + '\'' +
                ", intensity=" + intensity +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LipidScoreUnit that = (LipidScoreUnit) o;
        return Double.compare(that.matchedMz, matchedMz) == 0 && Objects.equals(lipid, that.lipid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lipid, matchedMz);
    }
    */

}
