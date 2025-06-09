package madstodolist.controller;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
public class BloqueoUsuarioWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UsuarioService usuarioService;

    @MockBean
    private ManagerUserSession managerUserSession;

    @Test
    public void testListadoUsuariosMuestraEstadoBloqueo() throws Exception {
        // GIVEN
        // Un usuario administrador logueado
        Long adminId = 1L;
        UsuarioData admin = new UsuarioData();
        admin.setId(adminId);
        admin.setEmail("admin@example.com");
        admin.setEsAdmin(true);

        // Un usuario bloqueado
        Long userId = 2L;
        UsuarioData usuario = new UsuarioData();
        usuario.setId(userId);
        usuario.setEmail("user@example.com");
        usuario.setEsAdmin(false);
        usuario.setBloqueado(true);

        when(managerUserSession.usuarioLogeado()).thenReturn(adminId);
        when(usuarioService.findById(adminId)).thenReturn(admin);
        when(usuarioService.findAll()).thenReturn(java.util.Arrays.asList(usuario));

        // WHEN, THEN
        // El listado muestra el estado de bloqueo
        this.mockMvc.perform(get("/registrados"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Bloqueado")));
    }

    @Test
    public void testBloquearUsuario() throws Exception {
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
        usuario.setBloqueado(false);

        when(managerUserSession.usuarioLogeado()).thenReturn(adminId);
        when(usuarioService.findById(adminId)).thenReturn(admin);
        when(usuarioService.findById(userId)).thenReturn(usuario);

        // WHEN
        // El administrador bloquea al usuario
        this.mockMvc.perform(post("/registrados/" + userId + "/bloquear"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/registrados"));

        // THEN
        // Se llama al servicio para cambiar el estado de bloqueo
        verify(usuarioService).cambiarEstadoBloqueo(userId);
    }

    @Test
    public void testLoginUsuarioBloqueado() throws Exception {
        // GIVEN
        // Un usuario bloqueado
        UsuarioData usuario = new UsuarioData();
        usuario.setEmail("user@example.com");
        usuario.setPassword("12345678");
        usuario.setBloqueado(true);

        when(usuarioService.login("user@example.com", "12345678"))
                .thenReturn(UsuarioService.LoginStatus.USER_BLOCKED);

        // WHEN, THEN
        // El login muestra mensaje de cuenta bloqueada
        this.mockMvc.perform(post("/login")
                        .param("eMail", "user@example.com")
                        .param("password", "12345678"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Tu cuenta ha sido bloqueada")));
    }

    @Test
    public void testBloquearUsuarioNoAutorizado() throws Exception {
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
        // El usuario intenta bloquear a otro usuario
        this.mockMvc.perform(post("/registrados/2/bloquear"))
                .andExpect(status().isUnauthorized());
    }
} 