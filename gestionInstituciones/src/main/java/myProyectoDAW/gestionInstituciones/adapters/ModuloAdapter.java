package myProyectoDAW.gestionInstituciones.adapters;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import myProyectoDAW.gestionInstituciones.adapters.entitys.AlumnoEntity;
import myProyectoDAW.gestionInstituciones.adapters.entitys.AsignaturaEntity;
import myProyectoDAW.gestionInstituciones.adapters.entitys.ModuloEntity;
import myProyectoDAW.gestionInstituciones.adapters.jpas.AlumnoJpaRepository;
import myProyectoDAW.gestionInstituciones.adapters.jpas.AsignaturaJpaRepository;
import myProyectoDAW.gestionInstituciones.adapters.jpas.ModuloJpaRepository;
import myProyectoDAW.gestionInstituciones.applications.ports.RepositoryModulo;
import myProyectoDAW.gestionInstituciones.domain.models.Modulo;

/* Adaptador para la entidad Modulo */
@Component
public class ModuloAdapter implements RepositoryModulo {

    private Optional<ModuloEntity> moduloEntityOptional;
    private Optional<AlumnoEntity> alumnoEntityOptional;
    private Optional<AsignaturaEntity> asignaturaEntityOptional;

    @Autowired
    private ModuloJpaRepository moduloJpaRepository;

    @Autowired
    private AlumnoJpaRepository alumnoJpaRepository;

    @Autowired
    private AsignaturaJpaRepository asignaturaJpaRepository;

    /*
     * Implementación del método que obtiene una lista de todos los módulos
     * existentes en el sistema
     */
    @Override
    public List<Modulo> obtenerTodosLosModulos() {
        return moduloJpaRepository.findAll().stream()
                .map(this::convertirEntityAModulo)
                .collect(Collectors.toList());
    }

    /* Implementación del método que crea un nuevo modulo */
    @Override
    public ResponseEntity<String> crearNuevoModulo(ModuloEntity moduloEntity) {

        if (moduloJpaRepository.findByCodigoModulo(moduloEntity.getCodigoModulo()).isPresent()) {
            return new ResponseEntity<>("El módulo " + moduloEntity.getNombreModulo() + " ya existe en el sistema",
                    HttpStatus.CONFLICT);

        }
        moduloJpaRepository.save(moduloEntity);
        return new ResponseEntity<>("Se ha dado de alta el módulo " + moduloEntity.getNombreModulo() + " correctamente",
                HttpStatus.OK);
    }

    /* Implementación del método que actualiza los datos de un modulo */
    @Override
    public ResponseEntity<String> actualizarDatosEnModulo(ModuloEntity moduloEntity) {

        /* Nos traemos o no el modulo dependiendo de si existe. */
        moduloEntityOptional = moduloJpaRepository.findByCodigoModulo(moduloEntity.getCodigoModulo());

        if (moduloEntityOptional.isPresent()) {

            ModuloEntity moduloEntityAActualizar = moduloEntityOptional.get();

            moduloEntityAActualizar.setCodigoModulo(moduloEntity.getCodigoModulo());
            moduloEntityAActualizar.setNombreModulo(moduloEntity.getNombreModulo());
            moduloEntityAActualizar.setCurso(moduloEntity.getCurso());
            moduloEntityAActualizar.setGrupo(moduloEntity.getGrupo());

            moduloJpaRepository.save(moduloEntityAActualizar);
            return new ResponseEntity<>(
                    "Se han modificados los datos del módulo correctamente",
                    HttpStatus.OK);

        }
        return new ResponseEntity<>(
                "Parece que ha habido un error con la actualización, puede que dicho mòdulo no exista",
                HttpStatus.NOT_FOUND);

    }

    /* Da de baja (elimina) un módulo dado su código */
    @Override
    public ResponseEntity<String> bajaDeUnModuloDadoElCodigo(String codigoModulo) {

        /* Nos traemos o no el modulo dependiendo de si existe. */
        moduloEntityOptional = moduloJpaRepository.findByCodigoModulo(codigoModulo);

        if (moduloEntityOptional.isPresent()) {

            ModuloEntity moduloEntityAEliminar = moduloEntityOptional.get();

            // Verificamos si el modulo tiene alumnos asignados
            if (moduloEntityAEliminar.getAlumnos() != null && !moduloEntityAEliminar.getAlumnos().isEmpty()) {
                return new ResponseEntity<>(
                        "No se puede eliminar el modulo con codigo " + moduloEntityAEliminar.getNombreModulo()
                                + ". Tiene alumnos ya asignados.",
                        HttpStatus.CONFLICT);
            }
            // Verificamos si el modulo tiene asignaturas asociadasalumnoAdapter
            if (moduloEntityAEliminar.getAsignaturas() != null && !moduloEntityAEliminar.getAsignaturas().isEmpty()) {
                return new ResponseEntity<>("No se puede eliminar el modulo " + moduloEntityAEliminar.getNombreModulo()
                        + ". Tiene asignaturas ya registradas.", HttpStatus.CONFLICT);
            }

            moduloJpaRepository.delete(moduloEntityAEliminar);
            return new ResponseEntity<>(
                    "El módulo " + moduloEntityAEliminar.getNombreModulo() + " (" + codigoModulo
                            + ") se ha dado de baja correctamente.",
                    HttpStatus.OK);
        }
        return new ResponseEntity<>("Intento de baja fallido, puede que dicho mòdulo no exista", HttpStatus.NOT_FOUND);
    }

    /* Busca un módulo específico dado su código. */
    @Override
    public ResponseEntity<?> buscarModuloDadoElCodigo(String codigoModulo) {

        /* Nos traemos o no el modulo dependiendo de si existe. */
        moduloEntityOptional = moduloJpaRepository.findByCodigoModulo(codigoModulo);

        if (moduloEntityOptional.isPresent()) {
            return new ResponseEntity<>(moduloEntityOptional.get(), HttpStatus.OK);

        }
        return new ResponseEntity<>("Intento de búsqueda fallido, puede que dicho mòdulo no exista",
                HttpStatus.NOT_FOUND);
    }

    /* Obtiene todas las asignaturas asociadas a un módulo dado su código. */
    @Override
    public ResponseEntity<?> obtenerTodasLasAsignaturasDeUnModulo(String codigoModulo) {

        /* Nos traemos o no el modulo dependiendo de si existe. */
        moduloEntityOptional = moduloJpaRepository.findByCodigoModulo(codigoModulo);

        if (moduloEntityOptional.isPresent()) {

            ModuloEntity moduloAListarAsignaturas = moduloEntityOptional.get();

            List<AsignaturaEntity> asignaturas = moduloAListarAsignaturas.getAsignaturas();

            return new ResponseEntity<>(asignaturas, HttpStatus.OK);

        }
        return new ResponseEntity<>("Intento de búsqueda fallido, puede que dicho mòdulo no exista",
                HttpStatus.NOT_FOUND);
    }

    /* Obtiene todos los alumnos asociados a un módulo dado su código */
    @Override
    public ResponseEntity<?> obtenerTodosLosAlumnosDeUnModulo(String codigoModulo) {

        /* Nos traemos o no el modulo dependiendo de si existe */
        moduloEntityOptional = moduloJpaRepository.findByCodigoModulo(codigoModulo);

        if (moduloEntityOptional.isPresent()) {

            ModuloEntity moduloAListarAlumnos = moduloEntityOptional.get();
            List<AlumnoEntity> alumnos = moduloAListarAlumnos.getAlumnos();
            return new ResponseEntity<>(alumnos, HttpStatus.OK);

        }
        return new ResponseEntity<>("Intento de búsqueda fallido, puede que dicho mòdulo no exista",
                HttpStatus.NOT_FOUND);
    }

    /*
     * Metodo por el que se asigna un determinado modulo a un alumno (Relación
     * modulo - alumno)
     */
    @Override
    public ResponseEntity<String> asignarAlumnosAModulo(String codigoModulo, String dniAlumno) {

        alumnoEntityOptional = alumnoJpaRepository.findByDni(dniAlumno); // Obtengo al alumno.
        moduloEntityOptional = moduloJpaRepository.findByCodigoModulo(codigoModulo); // Obtengo el modulo

        ModuloEntity moduloAAsignarAlumno = moduloEntityOptional.get();

        if (moduloAAsignarAlumno.getAlumnos().contains(alumnoEntityOptional.get())) {
            return new ResponseEntity<>(
                    "El alumno con DNI " + dniAlumno + " ya está asignado al módulo " +
                            codigoModulo + ".",
                    HttpStatus.OK);
        }

        // Asigno el alumno al módulo (se modifica el lado dueño de la relación)
        moduloAAsignarAlumno.getAlumnos().add(alumnoEntityOptional.get());

        // Mantengo la consistencia bidireccional
        if (alumnoEntityOptional.get().getModulos() != null
                && !alumnoEntityOptional.get().getModulos().contains(moduloAAsignarAlumno)) {
            alumnoEntityOptional.get().getModulos().add(moduloAAsignarAlumno);
            alumnoJpaRepository.save(alumnoEntityOptional.get());
        }

        // Y se guarda el módulo para que los cambios persistan.
        moduloJpaRepository.save(moduloAAsignarAlumno);
        return new ResponseEntity<>("Se ha asignado al alumno con DNI: " + dniAlumno
                + " con exito", HttpStatus.OK);

    }

    /* Meodo por el cual se le asigna una asignatura a un módulo específico. */
    @Override
    public ResponseEntity<String> asignarAsignaturasAModulo(String codigoModulo, String codigoAsignatura) {

        // 1. Busco el Módulo por su código
        moduloEntityOptional = moduloJpaRepository.findByCodigoModulo(codigoModulo);

        if (moduloEntityOptional.isEmpty()) {
            return new ResponseEntity<>("Intento de asignación fallida, puede que dicho mòdulo no exista",
                    HttpStatus.NOT_FOUND);
        }

        ModuloEntity moduloAAsignarAsignatura = moduloEntityOptional.get();

        // 2. Busco la Asignatura por su codigo
        asignaturaEntityOptional = asignaturaJpaRepository.findByCodigo(codigoAsignatura);

        if (asignaturaEntityOptional.isEmpty()) {
            return new ResponseEntity<>(
                    "La asignatura " + asignaturaEntityOptional.get().getNombre()
                            + " no esta dada de alta aún. No se la puede asignar al módulo",
                    HttpStatus.NOT_FOUND);
        }

        // 3. Verifico si la asignatura ya está asignada al módulo (relacion modulo -
        // asignatura)
        if (moduloAAsignarAsignatura.getAsignaturas().contains(asignaturaEntityOptional.get())) {
            return new ResponseEntity<>(
                    "La asignatura " + asignaturaEntityOptional.get().getNombre() + " ya está asignada al módulo "
                            + moduloEntityOptional.get().getNombreModulo()
                            + ".",
                    HttpStatus.CONFLICT);
        }
        // 4. Asigno la asignatura al módulo (modificamos el lado dueño de la relación)
        moduloAAsignarAsignatura.getAsignaturas().add(asignaturaEntityOptional.get());

        // 5. Mantengo la consistencia bidireccional
        if (asignaturaEntityOptional.get().getModulos() != null
                && !asignaturaEntityOptional.get().getModulos().contains(moduloAAsignarAsignatura)) {
            asignaturaEntityOptional.get().getModulos().add(moduloAAsignarAsignatura);
            asignaturaJpaRepository.save(asignaturaEntityOptional.get());
        }
        // 6. Y guardo el módulo para que los cambios persistan.
        moduloJpaRepository.save(moduloAAsignarAsignatura);
        return new ResponseEntity<>(
                "Se ha asignado la asignatura " + asignaturaEntityOptional.get().getNombre() + " con exito",
                HttpStatus.OK);
    }

    /*
     * Metodo para desasignar a un alumno de un modulo.
     * El metodo sigue casi las mismas pautas que el de asignar alumno a modulo
     */

    @Override
    public ResponseEntity<String> desasignarAlumnoDeModulo(String codigoModulo, String dniAlumno) {

        moduloEntityOptional = moduloJpaRepository.findByCodigoModulo(codigoModulo);
        alumnoEntityOptional = alumnoJpaRepository.findByDni(dniAlumno);

        ModuloEntity moduloADesAsignarAlumno = moduloEntityOptional.get();

        // Se elimina el alumno de la lista de alumnos del módulo.
        boolean asignado = moduloADesAsignarAlumno.getAlumnos().removeIf(a -> a.getDni().equals(dniAlumno));

        if (!asignado) {
            return new ResponseEntity<>(
                    "El alumno con DNI " + dniAlumno + " no está asignado al módulo " +
                            codigoModulo + ".",
                    HttpStatus.OK);
        }

        // Mantener la consistencia bidireccional
        if (alumnoEntityOptional.get().getModulos() != null) {
            alumnoEntityOptional.get().getModulos().removeIf(m -> m.getCodigoModulo().equals(codigoModulo));
            alumnoJpaRepository.save(alumnoEntityOptional.get());
        }

        // Y guardar el módulo para la persistencia en de los cambios.
        moduloJpaRepository.save(moduloADesAsignarAlumno);
        return new ResponseEntity<>("Se ha desasignado al alumno con DNI: " +
                dniAlumno + " con exito", HttpStatus.OK);
    }

    /*
     * Metodo para desasignar una asignatura de un modulo.
     * El metodo sigue casi las mismas pautas que el de asignar una asignatura a un
     * modulo
     */
    @Override
    public ResponseEntity<String> desasignarAsignaturaDeModulo(String codigoModulo, String codigoAsignatura) {

        // 1. Busco el Módulo por su código
        moduloEntityOptional = moduloJpaRepository.findByCodigoModulo(codigoModulo);

        if (moduloEntityOptional.isEmpty()) {
            return new ResponseEntity<>("Intento de desasignación fallida, puede que dicho mòdulo no exista",
                    HttpStatus.NOT_FOUND);
        }

        ModuloEntity moduloADesAsignarAsignatura = moduloEntityOptional.get();

        // 2. Busco la asignatura por el codigo
        asignaturaEntityOptional = asignaturaJpaRepository.findByCodigo(codigoAsignatura);

        if (asignaturaEntityOptional.isEmpty()) {
            return new ResponseEntity<>(
                    "La asignatura " + asignaturaEntityOptional.get().getNombre()
                            + " no esta dada de alta aún. No se la puede desasignar del módulo",
                    HttpStatus.NOT_FOUND);
        }

        AsignaturaEntity asignaturaADesasignar = asignaturaEntityOptional.get();

        // 3. Verifico si la asignatura está realmente asignada al módulo
        boolean asignada = moduloADesAsignarAsignatura.getAsignaturas()
                .removeIf(a -> a.getCodigo().equals(codigoAsignatura));

        if (!asignada) {
            return new ResponseEntity<>(
                    "La asignatura " + asignaturaADesasignar.getNombre() + " no está asignada al módulo "
                            + moduloADesAsignarAsignatura.getNombreModulo()
                            + ".",
                    HttpStatus.CONFLICT);
        }

        // 4. Mantengo la consistencia bidireccional
        if (asignaturaADesasignar.getModulos() != null) {
            asignaturaADesasignar.getModulos().removeIf(m -> m.getCodigoModulo().equals(codigoModulo));
            asignaturaJpaRepository.save(asignaturaADesasignar);
        }

        // 5. Y guardo el módulo para la persistencia en los cambios de datos
        moduloJpaRepository.save(moduloADesAsignarAsignatura);
        return new ResponseEntity<>(
                "Se ha desasignado la asignatura " + asignaturaADesasignar.getNombre() + " con exito",
                HttpStatus.OK);
    }

    /*
     * Opeeraacion para obtener todos los modulos a los que una asignatura está
     * asociada
     */
    @Override
    public ResponseEntity<?> obtenerTodosLosModulosDeUnaAsignatura(String codigoAsignatura) {

        /* Nos traemos o no la asignatura dependiendo de si existe. */

        asignaturaEntityOptional = asignaturaJpaRepository.findByCodigo(codigoAsignatura);

        if (asignaturaEntityOptional.isPresent()) {

            AsignaturaEntity asignaturaAListarModulos = asignaturaEntityOptional.get();

            List<ModuloEntity> modulos = asignaturaAListarModulos.getModulos();

            return new ResponseEntity<>(modulos, HttpStatus.OK);

        }
        return new ResponseEntity<>("La asignatura " + asignaturaEntityOptional.get().getNombre()
                + " no esta dada de alta aún.",
                HttpStatus.NOT_FOUND);

    }

    /*
     * Operación para obtener todos los modulos a los que un alumno esta asociado.
     */
    @Override
    public ResponseEntity<?> obtenerTodosLosModulosDeUnAlumno(String dniAlumno) {

        /* Nos traemos o no al alumno dependiendo de si existe */

        alumnoEntityOptional = alumnoJpaRepository.findByDni(dniAlumno);

        if (alumnoEntityOptional.isPresent()) {
            return new ResponseEntity<>(alumnoEntityOptional.get().getModulos(), HttpStatus.OK);
        }

        return new ResponseEntity<>("El alumno con DNI: " + dniAlumno + "no existe en BD", HttpStatus.NOT_FOUND);
    }

    /* METODO DE CONVERSION */

    private Modulo convertirEntityAModulo(ModuloEntity moduloEntity) {

        Modulo modulo = new Modulo();

        modulo.setCodigoModulo(moduloEntity.getCodigoModulo());
        modulo.setNombreModulo(moduloEntity.getNombreModulo());
        modulo.setCurso(moduloEntity.getCurso());
        modulo.setGrupo(moduloEntity.getGrupo());

        return modulo;
    }
}
