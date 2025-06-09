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
//
// A diferencia de los tests web de tarea, donde usábamos los datos
// de prueba de la base de datos, aquí vamos a practicar otro enfoque:
// moquear el usuarioService.
public class UsuarioWebTest {

    @Autowired
    private MockMvc mockMvc;

    // Moqueamos el usuarioService.
    // En los tests deberemos proporcionar el valor devuelto por las llamadas
    // a los métodos de usuarioService que se van a ejecutar cuando se realicen
    // las peticiones a los endpoint.
    @MockBean
    private UsuarioService usuarioService;

    @MockBean
    private ManagerUserSession managerUserSession;

    @Test
    public void servicioLoginUsuarioOK() throws Exception {
        // GIVEN
        // Moqueamos la llamada a usuarioService.login para que
        // devuelva un LOGIN_OK y la llamada a usuarioServicie.findByEmail
        // para que devuelva un usuario determinado.

        UsuarioData anaGarcia = new UsuarioData();
        anaGarcia.setNombre("Ana García");
        anaGarcia.setId(1L);

        when(usuarioService.login("ana.garcia@gmail.com", "12345678"))
                .thenReturn(UsuarioService.LoginStatus.LOGIN_OK);
        when(usuarioService.findByEmail("ana.garcia@gmail.com"))
                .thenReturn(anaGarcia);

        // WHEN, THEN
        // Realizamos una petición POST al login pasando los datos
        // esperados en el mock, la petición devolverá una redirección a la
        // URL con las tareas del usuario

        this.mockMvc.perform(post("/login")
                        .param("eMail", "ana.garcia@gmail.com")
                        .param("password", "12345678"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/usuarios/1/tareas"));
    }

    @Test
    public void servicioLoginUsuarioNotFound() throws Exception {
        // GIVEN
        // Moqueamos el método usuarioService.login para que devuelva
        // USER_NOT_FOUND
        when(usuarioService.login("pepito.perez@gmail.com", "12345678"))
                .thenReturn(UsuarioService.LoginStatus.USER_NOT_FOUND);

        // WHEN, THEN
        // Realizamos una petición POST con los datos del usuario mockeado y
        // se debe devolver una página que contenga el mensaja "No existe usuario"
        this.mockMvc.perform(post("/login")
                        .param("eMail","pepito.perez@gmail.com")
                        .param("password","12345678"))
                .andExpect(content().string(containsString("No existe usuario")));
    }

    @Test
    public void servicioLoginUsuarioErrorPassword() throws Exception {
        // GIVEN
        // Moqueamos el método usuarioService.login para que devuelva
        // ERROR_PASSWORD
        when(usuarioService.login("ana.garcia@gmail.com", "000"))
                .thenReturn(UsuarioService.LoginStatus.ERROR_PASSWORD);

        // WHEN, THEN
        // Realizamos una petición POST con los datos del usuario mockeado y
        // se debe devolver una página que contenga el mensaja "Contraseña incorrecta"
        this.mockMvc.perform(post("/login")
                        .param("eMail","ana.garcia@gmail.com")
                        .param("password","000"))
                .andExpect(content().string(containsString("Contraseña incorrecta")));
    }

    @Test
    public void testCambiarEstadoBloqueo() throws Exception {
        // GIVEN
        // Un usuario administrador en el sistema
        Long adminId = 1L;
        UsuarioData admin = new UsuarioData();
        admin.setId(adminId);
        admin.setEmail("admin@example.com");
        admin.setEsAdmin(true);

        // Un usuario normal en el sistema
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
        // El administrador intenta bloquear al usuario
        this.mockMvc.perform(post("/registrados/" + userId + "/bloquear"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/registrados"));

        // THEN
        // Se llama al servicio para cambiar el estado de bloqueo
        verify(usuarioService).cambiarEstadoBloqueo(userId);
    }

    @Test
    public void testCambiarEstadoBloqueoNoAutorizado() throws Exception {
        // GIVEN
        // Un usuario no administrador en el sistema
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

    @Test
    public void testListadoUsuariosMuestraEstadoBloqueo() throws Exception {
        // GIVEN
        // Un usuario administrador en el sistema
        Long adminId = 1L;
        UsuarioData admin = new UsuarioData();
        admin.setId(adminId);
        admin.setEmail("admin@example.com");
        admin.setEsAdmin(true);

        // Un usuario normal bloqueado
        Long userId = 2L;
        UsuarioData usuario = new UsuarioData();
        usuario.setId(userId);
        usuario.setEmail("user@example.com");
        usuario.setEsAdmin(false);
        usuario.setBloqueado(true);

        when(managerUserSession.usuarioLogeado()).thenReturn(adminId);
        when(usuarioService.findById(adminId)).thenReturn(admin);
        when(usuarioService.findAll()).thenReturn(Arrays.asList(usuario));

        // WHEN, THEN
        // El administrador ve la lista de usuarios con el estado de bloqueo
        this.mockMvc.perform(get("/registrados"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Bloqueado")));
    }
}
