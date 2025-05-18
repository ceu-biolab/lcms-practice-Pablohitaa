package adduct;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Adduct {

    /**
     * Calculate the mass to search depending on the adduct hypothesis
     *
     * @param experimentalMass mz
     * @param adduct adduct name ([M+H]+, [2M+H]+, [M+2H]2+, etc..)
     *
     * @return the mass difference within the tolerance respecting to the
     * massToSearch
     */
    private double experimentalmass;
    private String adduct;

    public Adduct(double experimentalmass, String adduct) {
        this.experimentalmass = experimentalmass;
        this.adduct = adduct;
    }

    public double getExperimentalmass() {
        return experimentalmass;
    }

    public void setExperimentalmass(double experimentalmass) {
        this.experimentalmass = experimentalmass;
    }

    public String getAdduct() {
        return adduct;
    }

    public void setAdduct(String adduct) {
        this.adduct = adduct;
    }
    /**
     * Calculate the mz of a monoisotopic mass with the corresponding adduct
     *
     * @param mz
     * @param adduct adduct name ([M+H]+, [2M+H]+, [M+2H]2+, etc..)
     *
     * @return
     */

    public static Double getMonoisotopicMassFromMZ(Double mz, String adduct) {
        // !! TODO Create the necessary regex to obtain the multimer (number before the M) and the charge (number before the + or - (if no number, the charge is 1).

        Double massToSearch;
        Double adductMass;

        if (mz == null || adduct == null || adduct.isEmpty()) {
            return null;
        }

        adductMass = AdductList.MAPMZPOSITIVEADDUCTS.getOrDefault(adduct, AdductList.MAPMZNEGATIVEADDUCTS.get(adduct));
        if (adductMass == null) {
            return null; //usar ppm para buscar el aducto
        }
        // 1. Find adduct mass shift
        if (AdductList.MAPMZPOSITIVEADDUCTS.containsKey(adduct)) {
            adductMass = AdductList.MAPMZPOSITIVEADDUCTS.get(adduct);
        } else if (AdductList.MAPMZNEGATIVEADDUCTS.containsKey(adduct)) {
            adductMass = AdductList.MAPMZNEGATIVEADDUCTS.get(adduct);
        } else {
            System.out.println("Adduct not found: " + adduct);
            return null;
        }
        // 2. Detect number of molecules (multimer)
        int multimer;
        if (adduct.contains("3M")) {
            multimer = 3;
        } else if (adduct.contains("2M")) {
            multimer = 2;
        } else if (adduct.contains("M")) {
            multimer = 1;
        } else {
            multimer = 1; // fallback por si no hay "M", aunque casi todos los aductos lo tienen
        }
        // 3. Detect charge
        int charge = 1; // Default single charge
        if (adduct.contains("2+")) {
            charge = 2;
        } else if (adduct.contains("3+")) {
            charge = 3;
        } else if (adduct.contains("2−") || adduct.contains("2-")) {
            charge = 2;
        } else if (adduct.contains("3−") || adduct.contains("3-")) {
            charge = 3;
        }
        // 4. Calculate monoisotopic mass and assign to MassToSearch
        if (multimer > 1) {
            massToSearch = (mz + adductMass) / multimer;
        } else if (charge > 1) {
            massToSearch = (mz *charge)+adductMass;
        } else {
            massToSearch = mz + adductMass;
        }
        return massToSearch;
        /*
        if Adduct is single charge the formula is M = m/z +- adductMass. Charge is 1 so it does not affect
        if Adduct is double or triple charged the formula is M =( mz - adductMass ) * charge
        if adduct is a dimer the formula is M =  (mz - adductMass) / numberOfMultimer
        return mz;
         */
    }
    /**
     * Calculate the mz of a monoisotopic mass with the corresponding adduct
     *
     * @param monoisotopicMass
     * @param adduct adduct name ([M+H]+, [2M+H]+, [M+2H]2+, etc..)
     *
     * @return
     */
    public static Double getMZFromMonoisotopicMass(Double monoisotopicMass, String adduct) {
            if (monoisotopicMass == null || adduct == null || adduct.isEmpty()) {
                return null;
            }

            Double adductMass = AdductList.MAPMZPOSITIVEADDUCTS.get(adduct);
            if (adductMass == null) {
                adductMass = AdductList.MAPMZNEGATIVEADDUCTS.get(adduct);
            }

            if (adductMass == null) {
                return null;
            }

            int multimer = 1;
            Matcher multimerMatcher = Pattern.compile("\\[(\\d*)M").matcher(adduct);
            if (multimerMatcher.find()) {
                String num = multimerMatcher.group(1);
                if (!num.isEmpty()) multimer = Integer.parseInt(num);
            }

            int charge = 1;
            Matcher chargeMatcher = Pattern.compile("(\\d*)([+-])\\]").matcher(adduct);
            if (chargeMatcher.find()) {
                String chargeStr = chargeMatcher.group(1);
                if (!chargeStr.isEmpty()) charge = Integer.parseInt(chargeStr);
            }

            double totalMass = monoisotopicMass * multimer;
            return (totalMass + adductMass) / charge;

        /*
        if Adduct is single charge the formula is m/z = M +- adductMass. Charge is 1 so it does not affect

        if Adduct is double or triple charged the formula is mz = M/charge +- adductMass

        if adduct is a dimer or multimer the formula is mz = M * numberOfMultimer +- adductMass

        return monoisotopicMass;

         */
    }
    /**
     * Returns the ppm difference between measured mass and theoretical mass
     *
     * @param experimentalMass    Mass measured by MS
     * @param theoreticalMass Theoretical mass of the compound
     */
    public static int calculatePPMIncrement(Double experimentalMass, Double theoreticalMass) {
        int ppmIncrement;
        ppmIncrement = (int) Math.round(Math.abs((experimentalMass - theoreticalMass) * 1000000
                / theoreticalMass));
        return ppmIncrement;
    }

    /**
     * Returns the ppm difference between measured mass and theoretical mass
     *
     * @param measuredMass    Mass measured by MS
     * @param ppm ppm of tolerance
     */
    public static double calculateDeltaPPM(Double measuredMass, int ppm) {
        double deltaPPM;
        deltaPPM =  Math.round(Math.abs((measuredMass * ppm) / 1000000));
        return deltaPPM;

    }
}
