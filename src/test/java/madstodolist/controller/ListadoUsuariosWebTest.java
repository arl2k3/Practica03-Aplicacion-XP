package madstodolist.controller;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;
import java.util.List;

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
    private ManagerUserSession managerUserSession;

    @MockBean
    private UsuarioService usuarioService;

    @Test
    public void listadoUsuariosMuestraTablaConUsuarios() throws Exception {
        // GIVEN
        // Lista de usuarios en el sistema
        UsuarioData usuario1 = new UsuarioData();
        usuario1.setId(1L);
        usuario1.setEmail("usuario1@example.com");

        UsuarioData usuario2 = new UsuarioData();
        usuario2.setId(2L);
        usuario2.setEmail("usuario2@example.com");

        List<UsuarioData> usuarios = Arrays.asList(usuario1, usuario2);

        when(usuarioService.findAll()).thenReturn(usuarios);
        when(managerUserSession.usuarioLogeado()).thenReturn(null);

        // WHEN, THEN
        // Al acceder al listado de usuarios, se muestra una tabla con los usuarios
        this.mockMvc.perform(get("/registrados"))
                .andExpect(status().isOk())
                .andExpect(content().string(allOf(
                        containsString("Listado de Usuarios"),
                        containsString("usuario1@example.com"),
                        containsString("usuario2@example.com")
                )))
                .andExpect(content().string(containsString("Ver detalles")));
    }

    @Test
    public void listadoUsuariosConLoginMuestraNavbarCorrecto() throws Exception {
        // GIVEN
        // Un usuario logueado y lista de usuarios
        Long usuarioId = 1L;
        UsuarioData usuarioLogeado = new UsuarioData();
        usuarioLogeado.setId(usuarioId);
        usuarioLogeado.setNombre("Usuario Logeado");
        usuarioLogeado.setEmail("logeado@example.com");

        UsuarioData otroUsuario = new UsuarioData();
        otroUsuario.setId(2L);
        otroUsuario.setEmail("otro@example.com");

        when(managerUserSession.usuarioLogeado()).thenReturn(usuarioId);
        when(usuarioService.findById(usuarioId)).thenReturn(usuarioLogeado);
        when(usuarioService.findAll()).thenReturn(Arrays.asList(usuarioLogeado, otroUsuario));

        // WHEN, THEN
        // Al acceder al listado de usuarios, se muestra el navbar con el usuario logueado
        this.mockMvc.perform(get("/registrados"))
                .andExpect(status().isOk())
                .andExpect(content().string(allOf(
                        containsString("Usuario Logeado"),
                        containsString("Tareas"),
                        containsString("Cuenta")
                )));
    }
} 