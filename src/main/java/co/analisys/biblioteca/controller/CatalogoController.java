package co.analisys.biblioteca.controller;

import co.analisys.biblioteca.model.Libro;
import co.analisys.biblioteca.model.LibroId;
import co.analisys.biblioteca.service.CatalogoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/libros")
@Tag(name = "Catálogo", description = "API para gestión del catálogo de libros")
public class CatalogoController {
    private final CatalogoService catalogoService;

    @Autowired
    public CatalogoController(CatalogoService catalogoService) {
        this.catalogoService = catalogoService;
    }

    @Operation(
        summary = "Obtener libro por ID",
        description = "Obtiene la información completa de un libro por su identificador único. " +
                    "Requiere autenticación y rol de LIBRARIAN o USER."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Libro encontrado exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Libro.class)
            )
        ),
        @ApiResponse(responseCode = "401", description = "No autenticado"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado - Rol insuficiente"),
        @ApiResponse(responseCode = "404", description = "Libro no encontrado")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_LIBRARIAN', 'ROLE_USER')")
    public ResponseEntity<Libro> obtenerLibro(
            @Parameter(description = "ID único del libro", required = true, example = "1")
            @PathVariable String id) {
        LibroId libroId = new LibroId();
        libroId.setLibroid_value(id);
        Libro libro = catalogoService.obtenerLibro(libroId);
        return ResponseEntity.ok(libro);
    }

    @Operation(
        summary = "Verificar disponibilidad de libro",
        description = "Verifica si un libro está disponible para préstamo. " +
                    "Requiere autenticación y rol de LIBRARIAN o USER."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Disponibilidad verificada exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(type = "boolean", example = "true")
            )
        ),
        @ApiResponse(responseCode = "401", description = "No autenticado"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado - Rol insuficiente"),
        @ApiResponse(responseCode = "404", description = "Libro no encontrado")
    })
    @GetMapping("/{id}/disponible")
    @PreAuthorize("hasAnyRole('ROLE_LIBRARIAN', 'ROLE_USER')")
    public ResponseEntity<Boolean> isLibroDisponible(
            @Parameter(description = "ID único del libro", required = true, example = "1")
            @PathVariable String id) {
        LibroId libroId = new LibroId();
        libroId.setLibroid_value(id);
        Libro libro = catalogoService.obtenerLibro(libroId);
        return ResponseEntity.ok(libro != null && libro.isDisponible());
    }

    @Operation(
        summary = "Actualizar disponibilidad de libro",
        description = "Actualiza el estado de disponibilidad de un libro. " +
                    "Requiere rol de LIBRARIAN para acceder."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Disponibilidad actualizada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
        @ApiResponse(responseCode = "401", description = "No autenticado"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado - Rol insuficiente"),
        @ApiResponse(responseCode = "404", description = "Libro no encontrado")
    })
    @PutMapping("/{id}/disponibilidad")
    @PreAuthorize("hasRole('ROLE_LIBRARIAN')")
    public ResponseEntity<String> actualizarDisponibilidad(
            @Parameter(description = "ID único del libro", required = true, example = "1")
            @PathVariable String id,
            @Parameter(description = "Nuevo estado de disponibilidad", required = true, example = "true")
            @RequestBody boolean disponible) {
        LibroId libroId = new LibroId();
        libroId.setLibroid_value(id);
        catalogoService.actualizarDisponibilidad(libroId, disponible);
        return ResponseEntity.ok("Disponibilidad actualizada exitosamente");
    }

    @Operation(
        summary = "Buscar libros",
        description = "Busca libros en el catálogo por criterio de búsqueda. " +
                    "Requiere autenticación y rol de LIBRARIAN o USER."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Búsqueda realizada exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Libro.class)
            )
        ),
        @ApiResponse(responseCode = "401", description = "No autenticado"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado - Rol insuficiente")
    })
    @GetMapping("/buscar")
    @PreAuthorize("hasAnyRole('ROLE_LIBRARIAN', 'ROLE_USER')")
    public ResponseEntity<List<Libro>> buscarLibros(
            @Parameter(description = "Criterio de búsqueda", required = true, example = "Cien años")
            @RequestParam String criterio) {
        List<Libro> libros = catalogoService.buscarLibros(criterio);
        return ResponseEntity.ok(libros);
    }

    @Operation(
        summary = "Estado del servicio",
        description = "Endpoint público que permite verificar el estado del servicio de catálogo. " +
                    "No requiere autenticación."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Servicio funcionando correctamente",
            content = @Content(
                mediaType = "text/plain",
                schema = @Schema(type = "string")
            )
        )
    })
    @GetMapping("/public/status")
    public ResponseEntity<String> getPublicStatus() {
        return ResponseEntity.ok("El servicio de catálogo está funcionando correctamente");
    }

    @Operation(
        summary = "Información del servicio",
        description = "Endpoint público que proporciona información básica del servicio. " +
                    "No requiere autenticación."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Información del servicio obtenida exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(type = "object")
            )
        )
    })
    @GetMapping("/public/info")
    public ResponseEntity<Object> getServiceInfo() {
        return ResponseEntity.ok(Map.of(
            "service", "Catálogo Service",
            "version", "1.0.0",
            "description", "Microservicio para gestión del catálogo de libros",
            "status", "ACTIVE",
            "endpoints", List.of(
                "GET /libros/{id} - Obtener libro por ID (ROLE_LIBRARIAN/ROLE_USER)",
                "GET /libros/{id}/disponible - Verificar disponibilidad (ROLE_LIBRARIAN/ROLE_USER)",
                "PUT /libros/{id}/disponibilidad - Actualizar disponibilidad (ROLE_LIBRARIAN)",
                "GET /libros/buscar - Buscar libros (ROLE_LIBRARIAN/ROLE_USER)",
                "GET /libros/public/status - Estado del servicio (PÚBLICO)",
                "GET /libros/public/info - Información del servicio (PÚBLICO)"
            ),
            "roles", List.of(
                "ROLE_LIBRARIAN - Acceso completo a catálogo y actualizaciones",
                "ROLE_USER - Acceso solo a consultas del catálogo"
            )
        ));
    }
}
