package com.smartlogix.ubicacionesservice.config;

import com.smartlogix.ubicacionesservice.model.Ubicacion;
import com.smartlogix.ubicacionesservice.repository.UbicacionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UbicacionRepository ubicacionRepository;

    @Override
    public void run(String... args) throws Exception {
        log.info("Iniciando inicialización completa de la tabla de ubicaciones...");

        // 1. Ubicaciones Estándar (P1 a P5, Zone A, B, C, Posiciones variables)
        char[] pisos = { 'A', 'B', 'C' };
        int countStandard = 0;
        Map<Integer, Integer> limitsPerFloor = Map.of(
                1, 57,
                2, 43,
                3, 28,
                4, 16,
                5, 12);

        for (int pasillo = 1; pasillo <= 5; pasillo++) {
            int maxPositions = limitsPerFloor.getOrDefault(pasillo, 60);
            for (char piso : pisos) {
                for (int numero = 1; numero <= maxPositions; numero++) {
                    String codigo = String.format("P%d-%c-%02d", pasillo, piso, numero);
                    if (!ubicacionRepository.existsByCodigoUbicacion(codigo)) {
                        Ubicacion u = new Ubicacion();
                        u.setCodigoUbicacion(codigo);
                        u.setPasillo(pasillo);
                        u.setPiso(piso);
                        u.setNumero(numero);
                        u.setEsEstante(false);
                        ubicacionRepository.save(u);
                        countStandard++;
                    }
                }
            }
        }
        if (countStandard > 0) {
            log.info("Se crearon {} ubicaciones estándar.", countStandard);
        }

        // 2. Ubicaciones Especiales (Estantes P3)
        // Usamos LinkedHashMap para garantizar el orden de los IDs según la realidad
        // física
        Map<Integer, Integer> configEstantes = new java.util.LinkedHashMap<>();
        configEstantes.put(13, 10);
        configEstantes.put(15, 10);
        configEstantes.put(29, 5);
        configEstantes.put(31, 3);
        configEstantes.put(30, 5);
        configEstantes.put(9, 10);
        configEstantes.put(11, 10);

        int countSpecial = 0;
        for (Map.Entry<Integer, Integer> entry : configEstantes.entrySet()) {
            Integer numero = entry.getKey();
            Integer niveles = entry.getValue();
            for (int nivel = 1; nivel <= niveles; nivel++) {
                String codigo = String.format("P3-A/EST-%d,%d", numero, nivel);
                if (!ubicacionRepository.existsByCodigoUbicacion(codigo)) {
                    Ubicacion ubicacion = new Ubicacion();
                    ubicacion.setCodigoUbicacion(codigo);
                    ubicacion.setPasillo(3);
                    ubicacion.setPiso('A');
                    ubicacion.setNumero(numero);
                    ubicacion.setNivel(nivel);
                    ubicacion.setEsEstante(true);

                    ubicacionRepository.save(ubicacion);
                    countSpecial++;
                }
            }
        }
        if (countSpecial > 0) {
            log.info("Se crearon {} ubicaciones de estantes especiales.", countSpecial);
        }

        log.info("Finalizada la inicialización de la tabla de ubicaciones.");
    }
}
