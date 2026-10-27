package com.rinconcitomarino.controller;

import com.rinconcitomarino.model.EstadoReserva;
import com.rinconcitomarino.model.Opinion;
import com.rinconcitomarino.model.Reserva;
import com.rinconcitomarino.model.RolUsuario;
import com.rinconcitomarino.model.UsuarioAdmin;
import com.rinconcitomarino.repository.UsuarioAdminRepository;
import com.rinconcitomarino.service.OpinionService;
import com.rinconcitomarino.service.ReservaService;
import com.rinconcitomarino.service.ReservaExcelService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


import java.time.LocalDate;
import java.io.IOException;
import java.util.List;

@Controller
public class WebController {

    private final ReservaService reservaService;
    private final ReservaExcelService reservaExcelService;
    private final OpinionService opinionService;
    private final UsuarioAdminRepository usuarioAdminRepository;
    private final PasswordEncoder passwordEncoder;

    public WebController(
            ReservaService reservaService,
            ReservaExcelService reservaExcelService,
            OpinionService opinionService,
            UsuarioAdminRepository usuarioAdminRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.reservaService = reservaService;
        this.reservaExcelService = reservaExcelService;
        this.opinionService = opinionService;
        this.usuarioAdminRepository = usuarioAdminRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/")
    public String index(@RequestParam(required = false) String opinion, Model model) {
        cargarInicio(model);
        model.addAttribute("opinionOk", "ok".equals(opinion));
        return "index";
    }

    @PostMapping("/opiniones")
    public String guardarOpinion(
            @Valid @ModelAttribute("opinion") Opinion opinion,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            cargarInicio(model);
            model.addAttribute("opinionError", true);
            return "index";
        }

        opinionService.guardar(opinion);
        redirectAttributes.addAttribute("opinion", "ok");
        return "redirect:/";
    }

    @GetMapping("/reservas")
    public String reservas(Model model) {
        if (!model.containsAttribute("reserva")) {
            model.addAttribute("reserva", new Reserva());
        }
        return "reservas";
    }

    @PostMapping("/reservas")
    public String guardarReserva(
            @Valid @ModelAttribute("reserva") Reserva reserva,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("validationError", true);
            return "reservas";
        }

        Reserva creada = reservaService.crearReserva(reserva);
        redirectAttributes.addFlashAttribute("reservaOk", true);
        redirectAttributes.addFlashAttribute("whatsappUrl", reservaService.generarWhatsappUrl(creada));
        return "redirect:/reservas";
    }

    @GetMapping("/login")
    public String login(@RequestParam(required = false) String error,
                        @RequestParam(required = false) String expired,
                        Model model) {
        model.addAttribute("loginRequest", new com.rinconcitomarino.dto.LoginRequest());
        model.addAttribute("loginError", error != null);
        model.addAttribute("sessionExpired", expired != null);
        return "login";
    }

    @GetMapping("/admin")
    public String dashboard(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dia, Model model) {
        reservaService.expirarPendientesVencidas();
        LocalDate seleccion = dia == null ? LocalDate.now(java.time.ZoneId.of("America/Lima")) : dia;
        List<Reserva> agenda = reservaService.listarAgenda(seleccion);
        model.addAttribute("dia", seleccion);
        model.addAttribute("pendientesDia", agenda.stream().filter(r -> r.getEstado() == EstadoReserva.PENDIENTE).count());
        model.addAttribute("confirmadasDia", agenda.stream().filter(r -> r.getEstado() == EstadoReserva.CONFIRMADO).count());
        model.addAttribute("atendidasDia", agenda.stream().filter(r -> r.getEstado() == EstadoReserva.ATENDIDO).count());
        model.addAttribute("personasDia", agenda.stream().filter(r -> r.getEstado() == EstadoReserva.CONFIRMADO).mapToInt(Reserva::getPersonas).sum());
        model.addAttribute("view", "dashboard");
        model.addAttribute("stats", reservaService.calcularStats());
        model.addAttribute("reservasHoy", agenda);
        model.addAttribute("reservasUrgentes", agenda.stream().filter(r -> r.getEstado() == EstadoReserva.PENDIENTE).toList());
        model.addAttribute("historialReciente", reservaService.listarHistorialReciente().stream().filter(item -> !"sistema-demo".equals(item.getUsuario())).limit(4).toList());
        model.addAttribute("opinionesRecientes", opinionService.listar(null).stream().limit(3).toList());
        return "admin";
    }

    @GetMapping("/admin/reservas")
    public String adminReservas(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) EstadoReserva estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(required = false) Integer personas,
            @RequestParam(required = false) String hora,
            Model model
    ) {
        reservaService.expirarPendientesVencidas();
        model.addAttribute("view", "reservas");
        model.addAttribute("q", q);
        model.addAttribute("estadoFiltro", estado);
        model.addAttribute("fechaDesde", fechaDesde);
        model.addAttribute("fechaHasta", fechaHasta);
        model.addAttribute("personasFiltro", personas);
        model.addAttribute("horaFiltro", hora);
        model.addAttribute("reservas", reservaService.listarReservas(q, estado, fechaDesde, fechaHasta, personas, hora));
        model.addAttribute("stats", reservaService.calcularStats());
        model.addAttribute("estados", EstadoReserva.values());
        return "admin";
    }

    @GetMapping("/admin/reservas/nueva")
    public String nuevaReserva(Model model) {
        model.addAttribute("reserva", new Reserva());
        return "reserva-form";
    }

    @GetMapping("/admin/reservas/{id}/editar")
    public String editarReserva(@PathVariable Long id, Model model) {
        model.addAttribute("reserva", reservaService.obtenerPorId(id));
        return "reserva-form";
    }

    @PostMapping("/admin/reservas/guardar")
    public String guardarDesdePanel(@RequestParam(required = false) Long reservaId,
            @Valid @ModelAttribute("reserva") Reserva reserva, BindingResult errores,
            Authentication authentication, RedirectAttributes mensajes) {
        reserva.setId(reservaId);
        if (reserva.getFecha() != null && reserva.getFecha().isBefore(LocalDate.now(java.time.ZoneId.of("America/Lima")))) {
            errores.rejectValue("fecha", "fecha.pasada", "Selecciona hoy o una fecha futura.");
        }
        if (errores.hasErrors()) {
            return "reserva-form";
        }
        reservaService.guardarDesdePanel(reservaId, reserva, authentication.getName());
        mensajes.addFlashAttribute("reservaMensaje", reservaId == null ? "Reserva registrada correctamente." : "Reserva actualizada correctamente.");
        return "redirect:/admin/reservas";
    }

    @PostMapping("/admin/reservas/{id}/estado")
    public String cambiarEstadoReserva(@PathVariable Long id, @RequestParam EstadoReserva estado, Authentication authentication) {
        reservaService.cambiarEstado(id, estado, authentication == null ? "sistema" : authentication.getName());
        return "redirect:/admin/reservas";
    }

    @PostMapping("/admin/reservas/{id}/eliminar")
    public String eliminarReserva(@PathVariable Long id, Authentication authentication) {
        reservaService.eliminar(id, authentication == null ? "sistema" : authentication.getName());
        return "redirect:/admin/reservas";
    }

    @GetMapping("/admin/reservas/exportar")
    public ResponseEntity<String> exportarReservas(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) EstadoReserva estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(required = false) Integer personas,
            @RequestParam(required = false) String hora
    ) {
        List<Reserva> reservas = reservaService.listarReservas(q, estado, fechaDesde, fechaHasta, personas, hora);
        StringBuilder csv = new StringBuilder("cliente,dni,email,telefono,personas,fecha,hora,estado\n");
        reservas.forEach(reserva -> csv.append(csv(reserva.getNombreCompleto())).append(',')
                .append(csv(reserva.getDni())).append(',')
                .append(csv(reserva.getEmail())).append(',')
                .append(csv(reserva.getTelefono())).append(',')
                .append(reserva.getPersonas()).append(',')
                .append(reserva.getFecha()).append(',')
                .append(csv(reserva.getHora())).append(',')
                .append(csv(reserva.getEstado().getEtiqueta())).append('\n'));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=reservas.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv.toString());
    }

    @GetMapping("/admin/reservas/excel")
    public ResponseEntity<byte[]> exportarExcel(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) EstadoReserva estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(required = false) Integer personas,
            @RequestParam(required = false) String hora
    ) throws IOException {
        byte[] archivo = reservaExcelService.generar(reservaService.listarReservas(q, estado, fechaDesde, fechaHasta, personas, hora));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=reservas.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(archivo);
    }

    @GetMapping("/admin/opiniones")
    public String adminOpiniones(@RequestParam(required = false) String q, Model model) {
        model.addAttribute("view", "opiniones");
        model.addAttribute("q", q);
        model.addAttribute("opiniones", opinionService.listar(q));
        model.addAttribute("stats", reservaService.calcularStats());
        return "admin";
    }

    @PostMapping("/admin/opiniones/{id}/moderacion")
    public String moderarOpinion(
            @PathVariable Long id,
            @RequestParam(defaultValue = "false") boolean visible,
            @RequestParam(defaultValue = "false") boolean destacado
    ) {
        opinionService.cambiarModeracion(id, visible, destacado);
        return "redirect:/admin/opiniones";
    }

    @GetMapping("/admin/usuarios")
    public String adminUsuarios(Model model, Authentication authentication) {
        model.addAttribute("view", "usuarios");
        model.addAttribute("usuarios", usuarioAdminRepository.findAll());
        model.addAttribute("roles", RolUsuario.values());
        model.addAttribute("currentUser", authentication == null ? "" : authentication.getName());
        model.addAttribute("stats", reservaService.calcularStats());
        return "admin";
    }

    @PostMapping("/admin/usuarios")
    public String crearUsuario(
            @RequestParam String usuario,
            @RequestParam String password,
            @RequestParam RolUsuario rol,
            RedirectAttributes redirectAttributes
    ) {
        String usuarioLimpio = usuario == null ? "" : usuario.trim();
        if (usuarioLimpio.length() < 3 || password == null || password.length() < 8) {
            redirectAttributes.addFlashAttribute("userError", "Usuario minimo 3 caracteres y contrasena minimo 8 caracteres.");
            return "redirect:/admin/usuarios";
        }
        if (usuarioAdminRepository.findByUsuario(usuarioLimpio).isPresent()) {
            redirectAttributes.addFlashAttribute("userError", "Ese usuario ya existe.");
            return "redirect:/admin/usuarios";
        }

        UsuarioAdmin nuevoUsuario = new UsuarioAdmin();
        nuevoUsuario.setUsuario(usuarioLimpio);
        nuevoUsuario.setPassword(passwordEncoder.encode(password));
        nuevoUsuario.setRol(rol);
        usuarioAdminRepository.save(nuevoUsuario);
        redirectAttributes.addFlashAttribute("userOk", "Usuario creado correctamente.");
        return "redirect:/admin/usuarios";
    }

    @PostMapping("/admin/usuarios/{id}/eliminar")
    public String eliminarUsuario(
            @PathVariable Long id,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        UsuarioAdmin usuario = usuarioAdminRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + id));
        String currentUser = authentication == null ? "" : authentication.getName();
        if (usuario.getUsuario().equalsIgnoreCase(currentUser)) {
            redirectAttributes.addFlashAttribute("userError", "No puedes eliminar tu propio usuario.");
            return "redirect:/admin/usuarios";
        }
        usuarioAdminRepository.delete(usuario);
        redirectAttributes.addFlashAttribute("userOk", "Usuario eliminado correctamente.");
        return "redirect:/admin/usuarios";
    }

    @PostMapping("/admin/opiniones/{id}/eliminar")
    public String eliminarOpinion(@PathVariable Long id) {
        opinionService.eliminar(id);
        return "redirect:/admin/opiniones";
    }

    @GetMapping("/carta")
    public String carta() {
        return "redirect:/assets/carta.pdf";
    }

    private void cargarInicio(Model model) {
        if (!model.containsAttribute("opinion")) {
            model.addAttribute("opinion", new Opinion());
        }
        model.addAttribute("platos", List.of(
                new PlatoView("Ceviche Clasico", "Pescado fresco, limon, aji limo y cebolla morada.", "/assets/ceviche.jpg"),
                new PlatoView("Chicharron de Pescado", "Crujiente por fuera, jugoso por dentro y perfecto para compartir.", "/assets/chicharron.jpg"),
                new PlatoView("Causa Marina", "Papa amarilla, crema suave y relleno inspirado en el mar peruano.", "/assets/causa.jpg")
        ));
    }

    public record PlatoView(String nombre, String descripcion, String imagen) {
    }

    private String csv(String value) {
        if (value == null) {
            return "";
        }
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
