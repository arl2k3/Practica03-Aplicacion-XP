package madstodolist.controller;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;

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
public class ListadoUsuariosWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UsuarioService usuarioService;

    @MockBean
    private ManagerUserSession managerUserSession;

    @Test
    public void listadoUsuariosMuestraTablaConUsuarios() throws Exception {
        // GIVEN
        // Un usuario administrador logueado
        Long adminId = 1L;
        UsuarioData admin = new UsuarioData();
        admin.setId(adminId);
        admin.setEmail("admin@example.com");
        admin.setEsAdmin(true);

        // Un usuario normal
        UsuarioData usuario = new UsuarioData();
        usuario.setId(2L);
        usuario.setEmail("user@example.com");
        usuario.setNombre("Usuario Normal");
        usuario.setEsAdmin(false);
        usuario.setBloqueado(false);

        when(managerUserSession.usuarioLogeado()).thenReturn(adminId);
        when(usuarioService.findById(adminId)).thenReturn(admin);
        when(usuarioService.findAll()).thenReturn(Arrays.asList(admin, usuario));

        // WHEN, THEN
        // Se muestra la tabla con los usuarios
        this.mockMvc.perform(get("/registrados"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("admin@example.com")))
                .andExpect(content().string(containsString("user@example.com")))
                .andExpect(content().string(containsString("Sí"))) // Es administrador
                .andExpect(content().string(containsString("No"))); // No es administrador
    }

    @Test
    public void listadoUsuariosConLoginMuestraNavbarCorrecto() throws Exception {
        // GIVEN
        // Un usuario administrador logueado
        Long adminId = 1L;
        UsuarioData admin = new UsuarioData();
        admin.setId(adminId);
        admin.setEmail("admin@example.com");
        admin.setNombre("Admin");
        admin.setEsAdmin(true);

        when(managerUserSession.usuarioLogeado()).thenReturn(adminId);
        when(usuarioService.findById(adminId)).thenReturn(admin);
        when(usuarioService.findAll()).thenReturn(Arrays.asList(admin));

        // WHEN, THEN
        // Se muestra la página con el navbar correcto
        this.mockMvc.perform(get("/registrados"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Admin")))
                .andExpect(content().string(containsString("Cerrar sesión")));
    }
} 