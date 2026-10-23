package cl.baeza_munoz.stockflow;

import cl.baeza_munoz.stockflow.util.RutValidator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RutValidatorTest {

    @Test
    void esValido_RutsValidos_DebeRetornarTrue() {
        assertTrue(RutValidator.esValido("11.111.111-1"));
        assertTrue(RutValidator.esValido("11111111-1"));
        assertTrue(RutValidator.esValido("22222222-2"));
        assertTrue(RutValidator.esValido("33333333-3"));
        assertTrue(RutValidator.esValido("12.345.678-5"));
        assertTrue(RutValidator.esValido("12345678-5"));
        assertTrue(RutValidator.esValido("123456785"));
    }

    @Test
    void esValido_RutsInvalidos_DebeRetornarFalse() {
        assertFalse(RutValidator.esValido("11.111.111-2")); // DV incorrecto
        assertFalse(RutValidator.esValido("123")); // Muy corto
        assertFalse(RutValidator.esValido("texto-1")); // No numérico
        assertFalse(RutValidator.esValido(null));
        assertFalse(RutValidator.esValido(""));
    }

    @Test
    void formatear_DebeFormatearCorrectamente() {
        assertEquals("11.111.111-1", RutValidator.formatear("111111111"));
        assertEquals("12.345.678-5", RutValidator.formatear("123456785"));
        assertEquals("11.111.111-1", RutValidator.formatear("11111111-1"));
    }
}
