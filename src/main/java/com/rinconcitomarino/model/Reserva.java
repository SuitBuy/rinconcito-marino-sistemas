package com.rinconcitomarino.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.net.URLEncoder;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "reservas")
public class Reserva {

    private static final ZoneId LIMA_ZONE = ZoneId.of("America/Lima");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Los nombres son obligatorios")
    @Size(max = 80, message = "Los nombres no deben superar 80 caracteres")
    @Column(nullable = false, length = 80)
    private String nombres;

    @NotBlank(message = "Los apellidos son obligatorios")
    @Size(max = 80, message = "Los apellidos no deben superar 80 caracteres")
    @Column(nullable = false, length = 80)
    private String apellidos;

    @NotBlank(message = "El DNI es obligatorio")
    @Size(min = 8, max = 8, message = "El DNI debe tener 8 digitos")
    @Pattern(regexp = "\\d{8}", message = "El DNI solo debe contener numeros")
    @Column(nullable = false, length = 8)
    private String dni;

    @NotNull(message = "La edad es obligatoria")
    @Min(value = 18, message = "Debes ser mayor de edad para reservar")
    @Max(value = 120, message = "La edad ingresada no es valida")
    @Column(nullable = false)
    private Integer edad;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "Ingresa un correo valido")
    @Size(max = 120, message = "El correo no debe superar 120 caracteres")
    @Column(nullable = false, length = 120)
    private String email;

    @NotBlank(message = "El telefono es obligatorio")
    @Pattern(regexp = "\\d{9}", message = "El telefono debe tener 9 digitos")
    @Column(nullable = false, length = 9)
    private String telefono;

    @NotNull(message = "La cantidad de personas es obligatoria")
    @Min(value = 1, message = "Debe reservar al menos una persona")
    @Max(value = 20, message = "Para mas de 20 personas contactanos por WhatsApp")
    @Column(nullable = false)
    private Integer personas;

    @NotNull(message = "La fecha es obligatoria")
    @Column(nullable = false)
    private LocalDate fecha;

    @NotBlank(message = "La hora es obligatoria")
    @Size(max = 10, message = "La hora no es valida")
    @Column(nullable = false, length = 10)
    private String hora;

    @NotNull(message = "El estado es obligatorio")
    @Column(nullable = false, length = 32)
    private EstadoReserva estado = EstadoReserva.PENDIENTE;

    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    @PrePersist
    void prePersist() {
        if (fechaRegistro == null) {
            fechaRegistro = LocalDateTime.now(LIMA_ZONE);
        }
        if (estado == null) {
            estado = EstadoReserva.PENDIENTE;
        }
    }

    @Transient
    public String getNombreCompleto() {
        return (nombres + " " + apellidos).trim();
    }

    @Transient
    public String getWhatsappMensaje() {
        String fechaTexto = fecha == null ? "fecha pendiente" : fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        return "Hola " + getNombreCompleto()
                + ", tu reserva en Rinconcito Marino para " + personas
                + " persona(s) el " + fechaTexto + " a las " + hora
                + " esta pendiente. Para confirmarla, coordina el adelanto de S/ "
                + new BigDecimal("20.00") + ".";
    }

    @Transient
    public String getWhatsappClienteUrl() {
        if (telefono == null || telefono.isBlank()) {
            return null;
        }
        String telefonoLimpio = telefono.replaceAll("\\D", "");
        if (telefonoLimpio.length() == 9) {
            telefonoLimpio = "51" + telefonoLimpio;
        }
        String mensaje = URLEncoder.encode(getWhatsappMensaje(), StandardCharsets.UTF_8);
        return "https://wa.me/" + telefonoLimpio + "?text=" + mensaje;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public Integer getEdad() {
        return edad;
    }

    public void setEdad(Integer edad) {
        this.edad = edad;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public Integer getPersonas() {
        return personas;
    }

    public void setPersonas(Integer personas) {
        this.personas = personas;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public String getHora() {
        return hora;
    }

    public void setHora(String hora) {
        this.hora = hora;
    }

    public EstadoReserva getEstado() {
        return estado;
    }

    public void setEstado(EstadoReserva estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }
}
