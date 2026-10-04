# Gestion de Inventarios - SOFT-10

Proyecto grupal del curso SOFT-10 (Estructuras de Datos), CENFOTEC. Primer avance.
Aplicacion para administrar productos guardados en una **lista enlazada simple
implementada por nosotros** (`ListaProductos`). Se usa con el **menu de consola**
(`menu()` en `Main`) y, como extra, con una **interfaz grafica** que se abre desde ese menu
y trabaja sobre la misma lista.

## Clases

Estructura de datos y modelo:

- `Producto`: nombre, precio, categoria, fechaVencimiento (si aplica), cantidad y
  listaImagenes (`ArrayList<String>` con las rutas de las imagenes).
- `Nodo`: contiene un Producto y la referencia al siguiente nodo.
- `ListaProductos`: lista enlazada simple con insercion al inicio y al final, busqueda,
  modificacion (incluida la de agregar imagenes), eliminacion, `imprimirProductos()` e
  `imprimirReporteCostos()` (recorre la lista e imprime el costo total de cada producto y el
  costo total acumulado).
- `ReglasProducto`: catalogo de categorias y tipos, limites y validacion de cada dato.
  La usan el menu de consola y la interfaz grafica, asi las dos aceptan y rechazan lo mismo.

Menu de consola:

- `Main`: la rutina `main()` crea la `ListaProductos` e invoca el `menu()` de consola.

Interfaz grafica (Java Swing, viene incluido en el JDK; no hay dependencias externas):

- `VentanaPrincipal`: indicadores, busqueda, botones y la tabla de productos.
- `PanelBusquedaGuiada`: menu de casillas para filtrar por categoria, tipo y demas palabras del nombre.
- `DialogoProducto`: formulario para agregar o editar.
- `DialogoImagenes`: muestra las imagenes del producto y permite agregar nuevas.
- `DialogoReporte`: reporte de costos por producto y costo total acumulado.
- `Estilo`: colores, fuentes y componentes comunes (mismo aspecto en todas las ventanas).

## Menu de consola

```
===== Gestion de Inventarios =====
1. Insertar producto al inicio
2. Insertar producto al final
3. Modificar producto
4. Agregar imagen a un producto
5. Eliminar producto
6. Listar productos
7. Buscar producto por nombre
8. Reporte de costos
9. Abrir interfaz grafica
0. Salir
```

- La consola muestra todo sin tildes (algunas consolas no las muestran bien); los datos se guardan
  con sus tildes y la interfaz grafica las muestra normalmente.
- Categoria, tipo, producto e imagen se eligen **por numero**, no hay que escribir nombres exactos.
- Al modificar, cada dato muestra su valor actual entre `[ ]`; con Enter se conserva.
- Eliminar pide confirmacion (s/n). En las listas, `0` cancela.
- Si un dato es invalido se explica el error y se vuelve a pedir.
- La opcion 9 abre la ventana con la misma lista; la consola espera y, al cerrar la ventana,
  vuelve al menu con los cambios hechos.

## Que metodo de la lista usa cada accion

Ni la consola ni la ventana guardan los productos en otra estructura: cada accion llama a
`ListaProductos`, y para mostrar los productos se recorre la lista nodo por nodo.

| Accion (consola / ventana)                         | Metodo de `ListaProductos`                    |
|----------------------------------------------------|-----------------------------------------------|
| 1 y 2 / + Agregar producto (al inicio / al final)  | `insertarAlInicio` / `insertarAlFinal`        |
| 3 / Editar (o doble clic en una fila)              | `modificarProducto`                           |
| 4 / Imagenes -> + Agregar imagen                   | `agregarImagenAProducto`                      |
| 5 / Eliminar                                       | `eliminarProducto`                            |
| 6 / Tabla                                          | `imprimirProductos` / recorrido desde `getCabeza()` |
| 7 / Buscar                                         | `buscarPorNombre`                             |
| 8 / Reporte de costos                              | `imprimirReporteCostos` / `calcularCostoTotal` |
| Elegir un producto por su numero (consola)         | `obtenerProducto(posicion)`                   |
| Indicadores y busqueda guiada (ventana)            | `contarUnidades`, `contarPorVencer`, recorrido |

La columna `#` de la tabla y el numero del listado de consola son la posicion en la lista enlazada.

## Busqueda

- **Por nombre (consola opcion 7 / campo Buscar):** escriba el nombre completo. No importan las
  mayusculas, las tildes ni los espacios de mas: `cafe      MOLIDO` encuentra `Café molido`.
  La ñ si cuenta (`año` no es `ano`). Esto lo hace `ListaProductos.normalizar()`, que usan
  `buscarPorNombre` y `eliminarProducto`.
- **Busqueda guiada (menu de la izquierda de la ventana):** casillas por niveles: primero la categoria,
  despues el tipo y luego las demas palabras del nombre (ej. `Bebidas > Café > molido`). Al marcar una
  opcion desaparecen las demas de ese nivel y aparecen las del siguiente. Puede seguir hasta llegar a
  un producto (queda seleccionado) o detenerse a medias. **Quitar filtro** muestra todo otra vez.

## Categorias y tipos de producto

La categoria y el tipo se eligen del catalogo: solo aparecen los tipos de la categoria elegida, asi no
se puede registrar, por ejemplo, Leche como Carnes. El nombre del producto es el tipo mas un detalle
opcional: `Leche` + `entera 1 L` = `Leche entera 1 L`. El catalogo esta en `ReglasProducto`
(`CATEGORIAS` y `TIPOS`); para agregar una categoria o un tipo basta con editar esas dos listas.

| Categoria          | Tipos                                        |
|--------------------|----------------------------------------------|
| Bebidas            | Agua, Café, Jugo, Refresco, Té               |
| Carnes             | Cerdo, Pescado, Pollo, Res                   |
| Enlatados          | Atún, Sardinas, Sopa                         |
| Frutas y verduras  | Banano, Cebolla, Manzana, Papa, Tomate       |
| Granos             | Arroz, Frijoles, Garbanzos, Lentejas         |
| Lácteos            | Leche, Mantequilla, Natilla, Queso, Yogurt   |
| Limpieza           | Cloro, Detergente, Jabón                     |
| Panadería          | Galletas, Pan, Tortillas                     |

## Validaciones (iguales en consola y ventana)

- Detalle del nombre: opcional (`N/A` o vacio = sin detalle), sin repetir el tipo; se guarda con un
  solo espacio entre palabras y el nombre completo tiene maximo 50 caracteres. El nombre no se repite
  (tampoco cambiando mayusculas, tildes o espacios), porque buscar, modificar y eliminar usan el nombre.
- Precio: numero entre 0 y 10 000 000 colones, se guarda con 2 decimales (use punto decimal).
- Cantidad: numero entero entre 0 y 100 000.
- Fecha de vencimiento: si aplica, formato `dd/mm/aaaa` (`N/A` o vacio = no vence); se rechazan
  fechas que no existen (ej. 31/02/2025).

## Carpeta de imagenes

Las imagenes de los productos deben estar en la carpeta `imagenes/` del proyecto (lo pide la consigna).
En la consola, la opcion 4 muestra las imagenes de esa carpeta para elegir una por numero; en la
ventana, **Imagenes -> + Agregar imagen** abre esa carpeta. Solo se aceptan imagenes reales
(.jpg, .jpeg o .png) que esten dentro de `imagenes/`, y una misma imagen no se repite en un producto.
Se guarda la ruta relativa al proyecto, por ejemplo `imagenes/ejemplo.jpg`.
La carpeta del proyecto se busca sola (`ReglasProducto.buscarProyecto()`), asi las imagenes
funcionan aunque el IDE ejecute el programa desde otra carpeta (por ejemplo, la de arriba).

## Como ejecutar

Requiere Java 11 o superior. Desde la carpeta del proyecto:

```
javac -encoding UTF-8 -d bin $(find src -name "*.java")
java -cp bin com.cenfotec.inventario.Main
```

## Abrir en IntelliJ IDEA

File -> Open y elegir la carpeta del proyecto. Si `Main` no se puede ejecutar,
clic derecho en `src/main/java` -> Mark Directory as -> Sources Root. Luego ejecutar `Main`:
el menu aparece en la consola de IntelliJ y la opcion 9 abre la interfaz grafica.
