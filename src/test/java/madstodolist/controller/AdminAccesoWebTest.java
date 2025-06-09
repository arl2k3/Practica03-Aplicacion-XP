package madstodolist.controller;

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
public class AdminAccesoWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UsuarioService usuarioService;

    @MockBean
    private ManagerUserSession managerUserSession;

    @Test
    public void testAccesoListadoUsuariosSinLogin() throws Exception {
        // GIVEN
        // No hay usuario logueado
        when(managerUserSession.usuarioLogeado()).thenReturn(null);

        // WHEN, THEN
        // El acceso al listado de usuarios devuelve 401
        this.mockMvc.perform(get("/registrados"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testAccesoListadoUsuariosSinSerAdmin() throws Exception {
        // GIVEN
        // Un usuario no administrador logueado
        Long userId = 1L;
        UsuarioData usuario = new UsuarioData();
        usuario.setId(userId);
        usuario.setEmail("user@example.com");
        usuario.setEsAdmin(false);

        when(managerUserSession.usuarioLogeado()).thenReturn(userId);
        when(usuarioService.findById(userId)).thenReturn(usuario);

        // WHEN, THEN
        // El acceso al listado de usuarios devuelve 401
        this.mockMvc.perform(get("/registrados"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testAccesoListadoUsuariosComoAdmin() throws Exception {
        // GIVEN
        // Un usuario administrador logueado
        Long adminId = 1L;
        UsuarioData admin = new UsuarioData();
        admin.setId(adminId);
        admin.setEmail("admin@example.com");
        admin.setEsAdmin(true);

        when(managerUserSession.usuarioLogeado()).thenReturn(adminId);
        when(usuarioService.findById(adminId)).thenReturn(admin);
        when(usuarioService.findAll()).thenReturn(Arrays.asList(admin));

        // WHEN, THEN
        // El acceso al listado de usuarios es exitoso
        this.mockMvc.perform(get("/registrados"))
                .andExpect(status().isOk());
    }

    @Test
    public void testAccesoDetalleUsuarioSinLogin() throws Exception {
        // GIVEN
        // No hay usuario logueado
        when(managerUserSession.usuarioLogeado()).thenReturn(null);

        // WHEN, THEN
        // El acceso al detalle de usuario devuelve 401
        this.mockMvc.perform(get("/registrados/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testAccesoDetalleUsuarioSinSerAdmin() throws Exception {
        // GIVEN
        // Un usuario no administrador logueado
        Long userId = 1L;
        UsuarioData usuario = new UsuarioData();
        usuario.setId(userId);
        usuario.setEmail("user@example.com");
        usuario.setEsAdmin(false);

        when(managerUserSession.usuarioLogeado()).thenReturn(userId);
        when(usuarioService.findById(userId)).thenReturn(usuario);

        // WHEN, THEN
        // El acceso al detalle de usuario devuelve 401
        this.mockMvc.perform(get("/registrados/2"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testAccesoDetalleUsuarioComoAdmin() throws Exception {
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
        usuario.setEsAdmin(false);

        when(managerUserSession.usuarioLogeado()).thenReturn(adminId);
        when(usuarioService.findById(adminId)).thenReturn(admin);
        when(usuarioService.findById(userId)).thenReturn(usuario);

        // WHEN, THEN
        // El acceso al detalle de usuario es exitoso
        this.mockMvc.perform(get("/registrados/" + userId))
                .andExpect(status().isOk());
    }
} 