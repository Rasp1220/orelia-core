package rpg.status.combat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DamageFormulaTest {

    private static final double DELTA = 1e-9;

    @Test
    void mitigateHalvesDamageAtEqualDefense() {
        // defense == damage-ish reference point: def/(def+100) reduction curve
        assertEquals(50.0, DamageFormula.mitigate(100.0, 100.0), DELTA);
    }

    @Test
    void mitigateWithZeroDefenseAppliesNoReduction() {
        assertEquals(100.0, DamageFormula.mitigate(100.0, 0.0), DELTA);
    }

    @Test
    void applyAttackBonusScalesDamageByAtkPercent() {
        assertEquals(150.0, DamageFormula.applyAttackBonus(100.0, 50.0), DELTA);
    }

    @Test
    void applyAttackBonusWithZeroAtkIsNoOp() {
        assertEquals(100.0, DamageFormula.applyAttackBonus(100.0, 0.0), DELTA);
    }

    @Test
    void criticalMultiplierAddsCritDmgAsPercentBonus() {
        assertEquals(1.7, DamageFormula.criticalMultiplier(1.5, 20.0), DELTA);
    }

    @Test
    void criticalMultiplierWithZeroCritDmgReturnsBaseMultiplier() {
        assertEquals(1.5, DamageFormula.criticalMultiplier(1.5, 0.0), DELTA);
    }
}
