package madstodolist.controller;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import madstodolist.service.UsuarioService;

@SpringBootTest
@AutoConfigureMockMvc
public class RegistroWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UsuarioService usuarioService;

    @Test
    public void testRegistroFormSinAdminMuestraCheckbox() throws Exception {
        // GIVEN
        // No hay administrador en el sistema
        when(usuarioService.existeAdmin()).thenReturn(false);

        // WHEN, THEN
        // El formulario de registro muestra el checkbox de administrador
        this.mockMvc.perform(get("/registro"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("name=\"esAdmin\"")));
    }

    @Test
    public void testRegistroFormConAdminNoMuestraCheckbox() throws Exception {
        // GIVEN
        // Ya existe un administrador en el sistema
        when(usuarioService.existeAdmin()).thenReturn(true);

        // WHEN, THEN
        // El formulario de registro no muestra el checkbox de administrador
        this.mockMvc.perform(get("/registro"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("name=\"esAdmin\""))));
    }

    @Test
    public void testRegistroAdminExitoso() throws Exception {
        // GIVEN
        // No existe ningún administrador en el sistema
        when(usuarioService.existeAdmin()).thenReturn(false);

        // WHEN
        // Se registra un usuario como administrador
        this.mockMvc.perform(post("/registro")
                        .param("email", "admin@example.com")
                        .param("password", "12345678")
                        .param("nombre", "Admin")
                        .param("esAdmin", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        // THEN
        // Se llama al servicio para registrar el usuario
        verify(usuarioService).registrar(argThat(usuario -> 
            usuario.getEmail().equals("admin@example.com") &&
            usuario.getPassword().equals("12345678") &&
            usuario.getNombre().equals("Admin") &&
            usuario.isEsAdmin()
        ));
    }

    @Test
    public void testRegistroAdminCuandoYaExiste() throws Exception {
        // GIVEN
        // Ya existe un administrador en el sistema
        when(usuarioService.existeAdmin()).thenReturn(true);
        when(usuarioService.findByEmail("admin2@example.com")).thenReturn(null);

        // WHEN
        // Se intenta registrar un usuario como administrador
        this.mockMvc.perform(post("/registro")
                        .param("email", "admin2@example.com")
                        .param("password", "12345678")
                        .param("nombre", "Admin 2")
                        .param("esAdmin", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("formRegistro"))
                .andExpect(model().attribute("error", "Ya existe un administrador en el sistema"));

        // THEN
        // No se llama al servicio para registrar el usuario
        verify(usuarioService, never()).registrar(any());
    }
} 