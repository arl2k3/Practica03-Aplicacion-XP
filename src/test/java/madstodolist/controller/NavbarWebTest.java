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
public class NavbarWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ManagerUserSession managerUserSession;

    @MockBean
    private UsuarioService usuarioService;

    @Test
    public void navbarSinLoginMuestraEnlacesLoginRegistro() throws Exception {
        // GIVEN
        // No hay usuario logueado
        when(managerUserSession.usuarioLogeado()).thenReturn(null);

        // WHEN, THEN
        // Al acceder a cualquier página, el navbar muestra los enlaces de login y registro
        this.mockMvc.perform(get("/about"))
                .andExpect(status().isOk())
                .andExpect(content().string(allOf(
                        containsString("Login"),
                        containsString("Registro")
                )))
                .andExpect(content().string(not(containsString("Tareas"))))
                .andExpect(content().string(not(containsString("Cuenta"))));
    }

    @Test
    public void navbarConLoginMuestraEnlacesTareasYCuenta() throws Exception {
        // GIVEN
        // Un usuario logueado
        Long usuarioId = 1L;
        UsuarioData usuario = new UsuarioData();
        usuario.setId(usuarioId);
        usuario.setNombre("Usuario Prueba");
        usuario.setEmail("test@example.com");

        when(managerUserSession.usuarioLogeado()).thenReturn(usuarioId);
        when(usuarioService.findById(usuarioId)).thenReturn(usuario);

        // WHEN, THEN
        // Al acceder a cualquier página, el navbar muestra los enlaces de tareas y cuenta
        this.mockMvc.perform(get("/about"))
                .andExpect(status().isOk())
                .andExpect(content().string(allOf(
                        containsString("Tareas"),
                        containsString("Usuario Prueba"),
                        containsString("Cuenta"),
                        containsString("Cerrar sesión")
                )))
                .andExpect(content().string(not(containsString("Login"))))
                .andExpect(content().string(not(containsString("Registro"))));
    }
} 