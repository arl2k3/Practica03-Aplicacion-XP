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

import madstodolist.dto.UsuarioData;
import madstodolist.service.UsuarioService;

@SpringBootTest
@AutoConfigureMockMvc
public class LoginWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UsuarioService usuarioService;

    @Test
    public void testLoginUsuarioBloqueado() throws Exception {
        // GIVEN
        // Un usuario bloqueado en el sistema
        UsuarioData usuario = new UsuarioData();
        usuario.setEmail("usuario.bloqueado@example.com");
        usuario.setPassword("12345678");
        usuario.setBloqueado(true);

        when(usuarioService.login("usuario.bloqueado@example.com", "12345678"))
                .thenReturn(UsuarioService.LoginStatus.USER_BLOCKED);

        // WHEN, THEN
        // El usuario intenta hacer login
        this.mockMvc.perform(post("/login")
                        .param("eMail", "usuario.bloqueado@example.com")
                        .param("password", "12345678"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Tu cuenta ha sido bloqueada")));
    }
} 