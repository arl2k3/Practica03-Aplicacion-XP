# Documentación Técnica - Práctica 2

## Nuevas Funcionalidades Implementadas

### 1. Barra de Navegación (Navbar)
Se ha implementado una barra de navegación común para todas las páginas de la aplicación (excepto login y registro) utilizando Bootstrap. La barra incluye:
- Enlace a la página "Acerca de"
- Enlace a la lista de tareas del usuario
- Menú desplegable con el nombre del usuario que incluye opciones para gestionar la cuenta y cerrar sesión

La implementación del navbar se realiza mediante un fragmento Thymeleaf que se incluye en todas las páginas que requieren autenticación. El navbar se adapta dinámicamente según el estado de autenticación del usuario y sus permisos.

### 2. Gestión de Usuarios
Se ha implementado un sistema completo de gestión de usuarios que incluye:
- Listado de usuarios registrados
- Vista detallada de cada usuario
- Sistema de roles con usuario administrador
- Funcionalidad de bloqueo de usuarios

El sistema de gestión de usuarios implementa un patrón de diseño basado en roles (RBAC - Role-Based Access Control) que permite una clara separación de responsabilidades y un control granular de los permisos.

## Nuevas Clases y Métodos

### Modelo
- `Usuario`: Se han añadido nuevos campos:
  - `esAdmin`: boolean para indicar si el usuario es administrador
  - `bloqueado`: boolean para controlar el estado de bloqueo del usuario

La entidad Usuario se ha extendido para soportar el nuevo sistema de roles y estados:

```java
@Entity
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String email;
    private String password;
    private String nombre;
    private boolean esAdmin;
    private boolean bloqueado;
    
    // Getters y setters
}
```

### DTOs
- `UsuarioData`: Se han añadido los campos correspondientes al modelo:
  - `esAdmin`
  - `bloqueado`

El DTO UsuarioData se utiliza para transferir datos entre capas sin exponer la entidad directamente:

```java
public class UsuarioData {
    private Long id;
    private String email;
    private String nombre;
    private boolean esAdmin;
    private boolean bloqueado;
    
    // Constructor, getters y setters
}
```

### Controladores
- `UsuarioController`: Nuevos endpoints:
  - `GET /registrados`: Listado de usuarios
  - `GET /registrados/{id}`: Detalles de un usuario
  - `POST /registrados/{id}/bloquear`: Bloquear/desbloquear usuario

El controlador implementa la lógica de presentación y manejo de peticiones HTTP:

```java
@Controller
@RequestMapping("/registrados")
public class UsuarioController {
    @Autowired
    private UsuarioService usuarioService;
    
    @Autowired
    private ManagerUserSession managerUserSession;
    
    // Métodos del controlador
}
```

### Servicios
- `UsuarioService`: Nuevos métodos:
  - `existeAdmin()`: Verifica si existe un administrador
  - `cambiarEstadoBloqueo(Long idUsuario)`: Cambia el estado de bloqueo
  - Modificación de `login()` para manejar usuarios bloqueados

El servicio implementa la lógica de negocio y la interacción con el repositorio:

```java
@Service
@Transactional
public class UsuarioService {
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    // Métodos del servicio
}
```

## Plantillas Thymeleaf

### Nuevas Plantillas
- `listaUsuarios.html`: Muestra el listado de usuarios con sus estados
- `detalleUsuario.html`: Muestra los detalles de un usuario específico

### Modificaciones
- `formRegistro.html`: Añadido checkbox para registro como administrador
- `formLogin.html`: Añadido mensaje para usuarios bloqueados
- `fragments.html`: Implementación del navbar común

Ejemplo de la plantilla de listado de usuarios:

```html
<div class="container">
    <h2>Listado de Usuarios</h2>
    <table class="table">
        <thead>
            <tr>
                <th>ID</th>
                <th>Email</th>
                <th>Nombre</th>
                <th>Admin</th>
                <th>Estado</th>
                <th>Acciones</th>
            </tr>
        </thead>
        <tbody>
            <tr th:each="usuario : ${usuarios}">
                <td th:text="${usuario.id}"></td>
                <td th:text="${usuario.email}"></td>
                <td th:text="${usuario.nombre}"></td>
                <td th:text="${usuario.esAdmin ? 'Sí' : 'No'}"></td>
                <td th:text="${usuario.bloqueado ? 'Bloqueado' : 'Activo'}"></td>
                <td>
                    <a th:href="@{/registrados/{id}(id=${usuario.id})}" 
                       class="btn btn-info">Detalles</a>
                    <form th:if="${usuario.id != usuarioLogeado.id}" 
                          th:action="@{/registrados/{id}/bloquear(id=${usuario.id})}" 
                          method="post" style="display: inline;">
                        <button type="submit" class="btn btn-warning">
                            <span th:text="${usuario.bloqueado ? 'Desbloquear' : 'Bloquear'}">
                            </span>
                        </button>
                    </form>
                </td>
            </tr>
        </tbody>
    </table>
</div>
```

## Tests Implementados

### Tests de Controlador
- `RegistroWebTest`: Pruebas del registro de usuarios y administradores
- `UsuarioWebTest`: Pruebas de la gestión de usuarios
- `DetalleUsuarioWebTest`: Pruebas de la vista de detalles
- `ListadoUsuariosWebTest`: Pruebas del listado de usuarios
- `BloqueoUsuarioWebTest`: Pruebas del bloqueo de usuarios

Ejemplo de test de registro de administrador:

```java
@Test
public void testRegistroAdminExitoso() throws Exception {
    when(usuarioService.existeAdmin()).thenReturn(false);
    
    mockMvc.perform(post("/registro")
            .param("email", "admin@ua")
            .param("password", "123")
            .param("nombre", "Administrador")
            .param("esAdmin", "true"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/login"));
            
    verify(usuarioService).registrar(argThat(usuarioData -> 
        usuarioData.getEmail().equals("admin@ua") && 
        usuarioData.isEsAdmin()));
}
```

### Tests de Servicio
- `UsuarioServiceTest`: Pruebas de la lógica de negocio relacionada con usuarios

## Código Relevante

### Protección de Rutas de Administrador
```java
@GetMapping("/registrados")
public String listadoUsuarios(Model model) {
    Long idUsuario = managerUserSession.usuarioLogeado();
    if (idUsuario == null) {
        throw new UsuarioNoLogeadoException();
    }
    
    UsuarioData usuario = usuarioService.findById(idUsuario);
    if (!usuario.isEsAdmin()) {
        throw new UsuarioNoAutorizadoException();
    }

    model.addAttribute("usuario", usuario);
    model.addAttribute("usuarios", usuarioService.findAll());
    return "listaUsuarios";
}
```

Este código muestra cómo se protegen las rutas de administrador. Primero verifica que el usuario esté logueado y luego comprueba si tiene permisos de administrador. Si no cumple alguna condición, se lanza la excepción correspondiente.

### Bloqueo de Usuarios
```java
@Transactional
public void cambiarEstadoBloqueo(Long idUsuario) {
    Usuario usuario = usuarioRepository.findById(idUsuario)
            .orElseThrow(() -> new UsuarioServiceException("Usuario no encontrado"));
    usuario.setBloqueado(!usuario.isBloqueado());
    usuarioRepository.save(usuario);
}
```

Este método implementa la funcionalidad de bloqueo/desbloqueo de usuarios. Utiliza una transacción para asegurar la integridad de los datos y maneja el caso de usuario no encontrado.

## Consideraciones de Implementación

1. **Seguridad**: 
   - Se ha implementado un sistema de excepciones personalizadas para manejar los casos de acceso no autorizado
   - Se utiliza Spring Security para la gestión de sesiones
   - Las contraseñas se almacenan encriptadas usando BCrypt

2. **Persistencia**: 
   - Se utiliza JPA/Hibernate para la persistencia de datos con transacciones
   - Se implementan repositorios para cada entidad
   - Se utilizan DTOs para la transferencia de datos entre capas

3. **Interfaz de Usuario**: 
   - Se ha utilizado Bootstrap para mantener una interfaz consistente y responsive
   - Se implementan fragmentos Thymeleaf para reutilizar componentes comunes
   - Se utilizan mensajes flash para mostrar notificaciones al usuario

4. **Testing**: 
   - Se han implementado tests unitarios y de integración para asegurar la calidad del código
   - Se utilizan mocks para simular dependencias
   - Se prueban casos de éxito y error
   - Se verifica la correcta integración entre capas
