package lipid;

import java.util.*;

import adduct.Adduct;
import adduct.AdductList;


public class Annotation {

    private final Lipid lipid;
    private final double mz;
    private final double intensity; // intensity of the most abundant peak in the groupedPeaks
    private final double rtMin;
    private String adduct; // !!TODO The adduct will be detected based on the groupedSignals
    private final Set<Peak> groupedSignals;
    private int score;
    private final int TOLERANCE=10;
    private int totalScoresApplied;
    private IonizationMode ionization;
    /**
     * @param lipid
     * @param mz
     * @param intensity
     * @param rtMin
     * @param ionization
     */
    public Annotation(Lipid lipid, double mz, double intensity, double rtMin,IonizationMode ionization) {
        this(lipid, mz, intensity, rtMin,ionization, Collections.emptySet());
    }

    /**
     * @param lipid
     * @param mz
     * @param intensity
     * @param rtMin
     * @param groupedSignals
     * @param ionization
     */
    public Annotation(Lipid lipid, double mz, double intensity, double rtMin, IonizationMode ionization,Set<Peak> groupedSignals) {
        this.lipid = lipid;
        this.mz = mz;
        this.rtMin = rtMin;
        this.intensity = intensity;
        this.ionization= ionization;
        // !!TODO This set should be sorted according to help the program to deisotope the signals plus detect the adduct
        this.groupedSignals = new TreeSet<>(groupedSignals);
        this.score = 0;
        this.totalScoresApplied = 0;
        detectAdductFromPeaks();
    }
    public Lipid getLipid() {
        return lipid;
    }

    public double getMz() {
        return mz;
    }

    public double getRtMin() {
        return rtMin;
    }

    public String getAdduct() {
        return adduct;
    }

    public void setAdduct(String adduct) {
        this.adduct = adduct;
    }

    public double getIntensity() {
        return intensity;
    }

    public Set<Peak> getGroupedSignals() {
        return Collections.unmodifiableSet(groupedSignals);
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public IonizationMode getIonization() {
        return ionization;
    }
    public void setIonization(IonizationMode ionization) {
        this.ionization = ionization;
    }
    // !CHECK Take into account that the score should be normalized between -1 and 1
    public void addScore(int delta) {
        this.score += delta;
        this.totalScoresApplied++;
    }
    /**
     * @return The normalized score between 0 and 1 that consists on the final number divided into the times that the rule
     * has been applied.
     */
    public double getNormalizedScore() {
        if (totalScoresApplied == 0) return 0.0;
        return (double) score / totalScoresApplied;
    }
        // return Math.min(1.0, (double) this.score / this.totalScoresApplied);

    private void detectAdductFromPeaks() {
        String finalAdduct = null;
        int ppm=0;

        // Aductos positivos
        if (ionization == IonizationMode.POSITIVE) {
            for (String adduct1 : AdductList.MAPMZPOSITIVEADDUCTS.keySet()) {
                for (String adduct2 : AdductList.MAPMZPOSITIVEADDUCTS.keySet()) {
                    if (adduct1.equals(adduct2)) continue;

                    for (Peak p1 : groupedSignals) {
                        for (Peak p2 : groupedSignals) {
                            if (p1.equals(p2)) continue;

                            Double mass1 = Adduct.getMonoisotopicMassFromMZ(p1.getMz(), adduct1);
                            Double mass2 = Adduct.getMonoisotopicMassFromMZ(p2.getMz(), adduct2);
                            ppm= Adduct.calculatePPMIncrement( mass1, mass2);
                            //System.out.println(ppm + ", "+ adduct1 + "= " + mass1 + "mz1= " + p1.getMz() + ", " + adduct2 + "= "+ mass2 + "mz2= " + p2.getMz());
                            if(ppm<TOLERANCE){
                                if (mass1 != null && mass2 != null && Math.abs(mass1 - mass2) <= TOLERANCE) {
                                    if (Math.abs(p1.getMz() - this.mz) <= ppm) {
                                        finalAdduct = adduct1;
                                        this.adduct = finalAdduct;
                                        return; // Primer match encontrado, salimos
                                    } else if (Math.abs(p2.getMz() - this.mz) <= TOLERANCE) {
                                        finalAdduct = adduct2;
                                        this.adduct = finalAdduct;
                                        return; // Primer match encontrado, salimos
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }else if (ionization == IonizationMode.NEGATIVE) {

            // Aductos negativos
            for (String adduct1 : AdductList.MAPMZNEGATIVEADDUCTS.keySet()) {
                for (String adduct2 : AdductList.MAPMZNEGATIVEADDUCTS.keySet()) {
                    if (adduct1.equals(adduct2)) continue;

                    for (Peak p1 : groupedSignals) {
                        for (Peak p2 : groupedSignals) {
                            if (p1.equals(p2)) continue;
                            Double mass1 = Adduct.getMonoisotopicMassFromMZ(p1.getMz(), adduct1);
                            Double mass2 = Adduct.getMonoisotopicMassFromMZ(p2.getMz(), adduct2);
                            ppm= Adduct.calculatePPMIncrement(mass1,mass2);
                            if(ppm<TOLERANCE){
                                if (mass1 != null && mass2 != null && Math.abs(mass1 - mass2) <= TOLERANCE) {
                                    if (Math.abs(p1.getMz() - this.mz) <= TOLERANCE) {
                                        finalAdduct = adduct1;
                                        this.adduct = finalAdduct;
                                        return;
                                    } else if (Math.abs(p2.getMz() - this.mz) <= TOLERANCE) {
                                        finalAdduct = adduct2;
                                        this.adduct = finalAdduct;
                                        return;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        System.out.println(finalAdduct);
        this.adduct = finalAdduct; // Por si no se cumple ninguna condición
    }


    // !TODO Take into account that the score should be normalized between 0 and 1


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Annotation)) return false;
        Annotation that = (Annotation) o;
        return Double.compare(that.mz, mz) == 0 &&
                Double.compare(that.rtMin, rtMin) == 0 &&
                Objects.equals(lipid, that.lipid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lipid, mz, rtMin);
    }

    @Override
    public String toString() {
        return String.format("Annotation(%s, mz=%.4f, RT=%.2f, adduct=%s, intensity=%.1f, score=%d)",
                lipid.getName(), mz, rtMin, adduct, intensity, score);
    }

    // !!TODO Detect the adduct with an algorithm or with drools, up to the user.
}
