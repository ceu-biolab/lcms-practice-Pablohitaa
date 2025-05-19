package lipid;

import adduct.*;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class AdductDetectionTest {
    // !!TODO For the adduct detection both regular algorithms or drools can be used as far the tests are passed.


    @Before
    public void setup() {
        // !! TODO Empty by now,you can create common objects for all tests.
    }

    @Test
    public void shouldDetectAdductBasedOnMzDifference() {// test para comprobar los negativos

        // Given two peaks with ~21.98 Da difference (e.g., [M+H]+ and [M+Na]+)
        Peak mH = new Peak(700.500, 100000.0); // [M+H]+
        Peak mNa = new Peak(722.482, 80000.0);  // [M+Na]+
        Lipid lipid = new Lipid(1, "PC 34:1", "C42H82NO8P", LipidType.PC, 34, 1);

        double annotationMZ = 700.49999d;
        double annotationIntensity = 80000.0;
        double annotationRT = 6.5d;
        Annotation annotation = new Annotation(lipid, mNa.getMz(), mNa.getIntensity(), annotationRT,IonizationMode.POSITIVE, Set.of(mH, mNa));


        assertNotNull("[M+Na]+ should be detected", annotation.getAdduct());

        assertEquals( "Adduct inferred from lowest mz in group","[M+Na]+", annotation.getAdduct());

    }


    @Test
    public void shouldDetectLossOfWaterAdduct() {
        Peak mh = new Peak(700.500, 90000.0);        // [M+H]+
        Peak mhH2O = new Peak(682.4894, 70000.0);     // [M+H–H₂O]+, ~18.0106 Da less

        Lipid lipid = new Lipid(1, "PE 36:2", "C41H78NO8P", LipidType.PE, 36, 2);
        Annotation annotation = new Annotation(lipid, mh.getMz(), mh.getIntensity(), 7.5d,IonizationMode.POSITIVE, Set.of(mh, mhH2O));



        assertNotNull("[M+H]+ should be detected", annotation.getAdduct());

        assertEquals( "Adduct inferred from lowest mz in group","[M+H]+", annotation.getAdduct());
    }

    @Test
    public void shouldDetectDoublyChargedAdduct() {
        // Assume real M = (700.500 - 1.0073) = 699.4927
        // So [M+2H]2+ = (M + 2.0146) / 2 = 350.7536
        Peak singlyCharged = new Peak(700.500, 100000.0);  // [M+H]+
        Peak doublyCharged = new Peak(350.754, 85000.0);   // [M+2H]2+

        Lipid lipid = new Lipid(3, "TG 54:3", "C57H104O6", LipidType.TG, 54, 3);
        Annotation annotation = new Annotation(lipid, doublyCharged.getMz(), doublyCharged.getIntensity(), 10d,IonizationMode.POSITIVE, Set.of(singlyCharged, doublyCharged));

        assertNotNull("[M+2H]2+ should be detected", annotation.getAdduct());

        assertEquals( "Adduct inferred from lowest mz in group","[M+2H]2+", annotation.getAdduct());
    }

    @Test
    public void shouldDetectNegativeAdductBasedOnMzDifference() {

        // Given two peaks with ~36.97 Da difference (e.g., [M–H]– and [M+Cl]–)
        Peak mH = new Peak(700.500, 100000.0); // [M–H]–
        Peak mCl = new Peak(736.4767, 80000.0);  // [M+Cl]–, con ~35.9767 Da diferencia


        Lipid lipid = new Lipid(4, "PI 38:4", "C47H83O13P", LipidType.PI, 38, 4);

        double annotationMZ = 700.49999d;
        double annotationIntensity = 95000.0;
        double annotationRT = 6.8d;

        Annotation annotation = new Annotation(
                lipid,
                annotationMZ,
                annotationIntensity,
                annotationRT,
                IonizationMode.NEGATIVE,
                Set.of(mH, mCl)

        );

        // Suponiendo que detectAdductFromPeaks se llama en el constructor o se llama manualmente si no
        // annotation.detectAdductFromPeaks();

        assertNotNull("[M-H]− should be detected", annotation.getAdduct());
        assertEquals("Adduct inferred from lowest mz in group", "[M-H]−", annotation.getAdduct());
    }
    @Test
    public void shouldDetectAdductFromMultiplePeaks() {
        Peak p1 = new Peak(700.500, 100000.0);     // [M+H]+
        Peak p2 = new Peak(722.489, 80000.0);      // [M+Na]+
        Peak p3 = new Peak(350.753, 85000.0);      // [M+2H]2+
        Peak p4 = new Peak(682.489, 70000.0);      // [M+H–H₂O]+

        Lipid lipid = new Lipid(5, "PC 36:4", "C44H80NO8P", LipidType.PC, 36, 4);

        double rt = 6.0;

        Annotation annotation = new Annotation(lipid, p3.getMz(), p3.getIntensity(), rt, IonizationMode.POSITIVE,Set.of(p1, p2, p3, p4));

        assertNotNull("Adduct should be detected", annotation.getAdduct());
        assertEquals("[M+2H]2+", annotation.getAdduct());
    }

    @Test
    public void shouldDetectNegativeAdductFromMultiplePeaks() {
        double neutralMass = 699.492724;

        Peak p1 = new Peak(neutralMass - 1.0073, 85000.0);   // [M-H]−
        Peak p2 = new Peak(neutralMass - 1.0073 - 18.0106, 60000.0);   // [M-H-H2O]−
        Peak p3 = new Peak(neutralMass + 34.969, 55000.0);   // [M+Cl]−
        Peak p4 = new Peak(neutralMass + 44.998, 58000.0);   // [M+HCOOH-H]−


        Lipid lipid = new Lipid(6, "PG 34:2", "C40H74O10P", LipidType.PG, 34, 2);
        double annotationMz = p2.getMz();
        double intensity = p2.getIntensity();
        double rt = 5.8;

        Annotation annotation = new Annotation(lipid, annotationMz, intensity, rt, IonizationMode.NEGATIVE, Set.of(p1, p2, p3, p4));

        assertNotNull("Negative adduct should be detected", annotation.getAdduct());
        assertEquals("[M-H-H2O]−", annotation.getAdduct());
    }
    @Test
    public void shouldDetectDimerAdductAmongNegativeOptions() {

        Peak p1 = new Peak(349.2427, 80000.0);   // [M-H]⁻
        Peak p2 = new Peak(331.2321, 60000.0);   // [M-H-H2O]⁻
        Peak p3 = new Peak(385.2190, 55000.0);   // [M+Cl]⁻
        Peak p4 = new Peak(395.2480, 58000.0);   // [M+HCOOH-H]⁻
        Peak p5 = new Peak(699.4927, 70000.0);   // [2M-H]⁻

        Lipid lipid = new Lipid(7, "FA 18:1", "C18H34O2", LipidType.FA, 18, 1);


        Annotation annotation = new Annotation(lipid, p5.getMz(),p5.getIntensity(),10.0,IonizationMode.NEGATIVE,Set.of(p1, p2, p3, p4, p5));

        assertEquals("[2M-H]−", annotation.getAdduct());
    }
    @Test
    public void shouldReturnNullWhenNoAdductMatches() {
        Peak unrelated1 = new Peak(600.000, 50000.0);
        Peak unrelated2 = new Peak(610.000, 40000.0); // diferencia no corresponde a ningún aducto

        Lipid lipid = new Lipid(8, "PC 34:1", "C42H82NO8P", LipidType.PC, 34, 1);

        Annotation annotation = new Annotation(lipid, 600.000, 50000.0, 8.0, IonizationMode.POSITIVE, Set.of(unrelated1, unrelated2));

        assertEquals("No adduct should be assigned", null, annotation.getAdduct());
    }
    @Test
    public void shouldDetectAmmoniumAdduct() {
        Peak mNH4 = new Peak(718.5338, 85000.0);       // [M+NH4]+
        Peak mH = new Peak(701.5073, 90000.0);         // [M+H]+ (aprox)

        Lipid lipid = new Lipid(9, "PE 36:1", "C41H80NO8P", LipidType.PE, 36, 1);

        Annotation annotation = new Annotation(
                lipid,
                mNH4.getMz(),  // Simulamos que el pico anotado es el de NH4
                mNH4.getIntensity(),
                7.2,
                IonizationMode.POSITIVE,
                Set.of(mNH4, mH)
        );

        System.out.println("Detected adduct: " + annotation.getAdduct());

        assertNotNull("Ammonium adduct should be detected", annotation.getAdduct());
        assertEquals("[M+NH4]+", annotation.getAdduct());
    }
    @Test
    public void shouldIgnoreAdductIfMzDoesNotMatch() {
        Peak fake = new Peak(799.123, 70000.0); // Valor inventado, sin aducto posible

        Lipid lipid = new Lipid(13, "PC 36:4", "C44H76NO8P", LipidType.PC, 36, 4);

        Annotation annotation = new Annotation(
                lipid,
                fake.getMz(),
                fake.getIntensity(),
                6.9,
                IonizationMode.POSITIVE,
                Set.of(fake)
        );
        assertNull("No adduct should be detected from unrecognized mz", annotation.getAdduct());
    }
    /* @Test
    public void shouldDetectAdductFromDimerOnly() {
        Peak dimer = new Peak(1401.0073, 60000.0); // [2M+H]+, simulando M=700

        Lipid lipid = new Lipid(12, "TG 60:5", "C65H110O6", LipidType.TG, 60, 5);

        Annotation annotation = new Annotation(
                lipid,
                dimer.getMz(),
                dimer.getIntensity(),
                11.1,
                IonizationMode.POSITIVE,
                Set.of(dimer)
        );
        assertNotNull("Dimer adduct should be detected", annotation.getAdduct());
        assertEquals("[2M+H]+", annotation.getAdduct());
    }
    @Test
    public void shouldDetectAductWhenOnlyOnePeakPresent() {
        Peak mNa = new Peak(723.489, 80000.0); // [M+Na]+ (simulado)

        Lipid lipid = new Lipid(11, "PC 36:3", "C44H78NO8P", LipidType.PC, 36, 3);

        Annotation annotation = new Annotation(
                lipid,
                mNa.getMz(),
                mNa.getIntensity(),
                5.6,
                IonizationMode.POSITIVE,
                Set.of(mNa)
        );

        assertNotNull("Adduct should be detected from single peak", annotation.getAdduct());
        assertEquals("[M+Na]+", annotation.getAdduct());
    } */

}

