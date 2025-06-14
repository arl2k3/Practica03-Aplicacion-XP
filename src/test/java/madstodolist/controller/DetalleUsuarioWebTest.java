package madstodolist.controller;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.dto.UsuarioData;
import madstodolist.service.UsuarioService;

@SpringBootTest
@AutoConfigureMockMvc
public class DetalleUsuarioWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UsuarioService usuarioService;

    @MockBean
    private ManagerUserSession managerUserSession;

    @Test
    public void detalleUsuarioMuestraDatosCorrectos() throws Exception {
        // GIVEN
        // Un usuario administrador logueado
        Long adminId = 1L;
        UsuarioData admin = new UsuarioData();
        admin.setId(adminId);
        admin.setEmail("admin@example.com");
        admin.setEsAdmin(true);

        // Un usuario normal
        Long userId = 2L;
        UsuarioData usuario = new UsuarioData();
        usuario.setId(userId);
        usuario.setEmail("user@example.com");
        usuario.setNombre("Usuario Normal");
        usuario.setEsAdmin(false);
        usuario.setBloqueado(false);

        when(managerUserSession.usuarioLogeado()).thenReturn(adminId);
        when(usuarioService.findById(adminId)).thenReturn(admin);
        when(usuarioService.findById(userId)).thenReturn(usuario);

        // WHEN, THEN
        // Se muestra la página de detalle del usuario
        this.mockMvc.perform(get("/registrados/" + userId))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("user@example.com")))
                .andExpect(content().string(containsString("Usuario Normal")))
                .andExpect(content().string(containsString("No"))); // No es administrador
    }

    @Test
    public void detalleUsuarioConLoginMuestraNavbarCorrecto() throws Exception {
        // GIVEN
        // Un usuario administrador logueado
        Long adminId = 1L;
        UsuarioData admin = new UsuarioData();
        admin.setId(adminId);
        admin.setEmail("admin@example.com");
        admin.setNombre("Admin");
        admin.setEsAdmin(true);

        // Un usuario normal
        Long userId = 2L;
        UsuarioData usuario = new UsuarioData();
        usuario.setId(userId);
        usuario.setEmail("user@example.com");
        usuario.setNombre("Usuario Normal");
        usuario.setEsAdmin(false);

        when(managerUserSession.usuarioLogeado()).thenReturn(adminId);
        when(usuarioService.findById(adminId)).thenReturn(admin);
        when(usuarioService.findById(userId)).thenReturn(usuario);

        // WHEN, THEN
        // Se muestra la página de detalle con el navbar correcto
        this.mockMvc.perform(get("/registrados/" + userId))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Admin")))
                .andExpect(content().string(containsString("Cerrar sesión")));
    }

    private Date parseDate(String dateStr) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.parse(dateStr);
    }
} 