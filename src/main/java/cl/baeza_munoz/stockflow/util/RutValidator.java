package cl.baeza_munoz.stockflow.util;

public class RutValidator {

    /**
     * Valida si un RUT chileno es válido según el algoritmo Módulo 11.
     * Acepta formatos con o sin puntos y guion (ej: 12.345.678-5, 12345678-5, 123456785).
     */
    public static boolean esValido(String rut) {
        if (rut == null) {
            return false;
        }

        String limpio = rut.replace(".", "").replace("-", "").trim().toUpperCase();
        if (limpio.length() < 2 || limpio.length() > 9) {
            return false;
        }

        String cuerpo = limpio.substring(0, limpio.length() - 1);
        char dvIngresado = limpio.charAt(limpio.length() - 1);

        // El cuerpo debe ser estrictamente numérico
        if (!cuerpo.matches("\\d+")) {
            return false;
        }

        // Algoritmo Módulo 11
        int suma = 0;
        int multiplicador = 2;

        for (int i = cuerpo.length() - 1; i >= 0; i--) {
            int digito = Character.getNumericValue(cuerpo.charAt(i));
            suma += digito * multiplicador;
            multiplicador = (multiplicador == 7) ? 2 : multiplicador + 1;
        }

        int resto = 11 - (suma % 11);
        char dvEsperado;
        if (resto == 11) {
            dvEsperado = '0';
        } else if (resto == 10) {
            dvEsperado = 'K';
        } else {
            dvEsperado = Character.forDigit(resto, 10);
        }

        return Character.toUpperCase(dvEsperado) == dvIngresado;
    }

    /**
     * Formatea un RUT a estructura estándar XX.XXX.XXX-X
     */
    public static String formatear(String rut) {
        if (rut == null) {
            return null;
        }
        String limpio = rut.replace(".", "").replace("-", "").trim().toUpperCase();
        if (limpio.length() < 2) {
            return rut;
        }
        String dv = limpio.substring(limpio.length() - 1);
        String cuerpo = limpio.substring(0, limpio.length() - 1);

        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (int i = cuerpo.length() - 1; i >= 0; i--) {
            sb.insert(0, cuerpo.charAt(i));
            count++;
            if (count % 3 == 0 && i > 0) {
                sb.insert(0, ".");
            }
        }
        sb.append("-").append(dv);
        return sb.toString();
    }
}
