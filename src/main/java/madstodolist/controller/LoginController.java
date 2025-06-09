package madstodolist.controller;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.dto.LoginData;
import madstodolist.dto.RegistroData;
import madstodolist.dto.UsuarioData;
import madstodolist.service.UsuarioService;
import madstodolist.service.UsuarioServiceException;

@Controller
public class LoginController {

    @Autowired
    UsuarioService usuarioService;

    @Autowired
    ManagerUserSession managerUserSession;

    @GetMapping("/")
    public String home(Model model) {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String loginForm(Model model) {
        model.addAttribute("loginData", new LoginData());
        return "formLogin";
    }

    @PostMapping("/login")
    public String loginSubmit(@ModelAttribute LoginData loginData, Model model, HttpSession session) {

        // Llamada al servicio para comprobar si el login es correcto
        UsuarioService.LoginStatus loginStatus = usuarioService.login(loginData.geteMail(), loginData.getPassword());

        if (loginStatus == UsuarioService.LoginStatus.LOGIN_OK) {
            UsuarioData usuario = usuarioService.findByEmail(loginData.geteMail());

            managerUserSession.logearUsuario(usuario.getId());

            // Si el usuario es administrador, redirigir al listado de usuarios
            if (usuario.isEsAdmin()) {
                return "redirect:/registrados";
            }

            return "redirect:/usuarios/" + usuario.getId() + "/tareas";
        } else if (loginStatus == UsuarioService.LoginStatus.USER_NOT_FOUND) {
            model.addAttribute("error", "No existe usuario");
            return "formLogin";
        } else if (loginStatus == UsuarioService.LoginStatus.ERROR_PASSWORD) {
            model.addAttribute("error", "Contraseña incorrecta");
            return "formLogin";
        }
        return "formLogin";
    }

    @GetMapping("/registro")
    public String registroForm(Model model) {
        model.addAttribute("registroData", new RegistroData());
        model.addAttribute("existeAdmin", usuarioService.existeAdmin());
        return "formRegistro";
    }

   @PostMapping("/registro")
   public String registroSubmit(@Valid RegistroData registroData, BindingResult result, Model model) {

        if (result.hasErrors()) {
            model.addAttribute("existeAdmin", usuarioService.existeAdmin());
            return "formRegistro";
        }

        if (usuarioService.findByEmail(registroData.getEmail()) != null) {
            model.addAttribute("registroData", registroData);
            model.addAttribute("existeAdmin", usuarioService.existeAdmin());
            model.addAttribute("error", "El usuario " + registroData.getEmail() + " ya existe");
            return "formRegistro";
        }

        try {
            UsuarioData usuario = new UsuarioData();
            usuario.setEmail(registroData.getEmail());
            usuario.setPassword(registroData.getPassword());
            usuario.setFechaNacimiento(registroData.getFechaNacimiento());
            usuario.setNombre(registroData.getNombre());
            usuario.setEsAdmin(registroData.isEsAdmin());

            usuarioService.registrar(usuario);
            return "redirect:/login";
        } catch (UsuarioServiceException e) {
            model.addAttribute("registroData", registroData);
            model.addAttribute("existeAdmin", usuarioService.existeAdmin());
            model.addAttribute("error", e.getMessage());
            return "formRegistro";
        }
   }

   @GetMapping("/logout")
   public String logout(HttpSession session) {
        managerUserSession.logout();
        return "redirect:/login";
   }
}
