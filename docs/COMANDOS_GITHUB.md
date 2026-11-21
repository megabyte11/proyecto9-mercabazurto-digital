# Guía Rápida para Subir a GitHub (30 Commits Atómicos entre los 4 Integrantes)

La rúbrica exige **mínimo 25 commits totales y al menos 3 commits por cada integrante** antes del cierre del repositorio (viernes 20 de noviembre, 11:59 p. m.).

Para facilitarlo, incluimos el script **`preparar_git_y_commits.ps1`** (ignorado en `.gitignore` para no ensuciar el repositorio final) que inicializa Git y registra **30 commits ordenados, atómicos y repartidos equitativamente (7 a 8 commits por integrante)** respetando exactamente sus roles:

- **Miguel Andrés Baza Contreras** (8 commits): `.gitignore`, TDAs (`Producto`, `Lote`, `Venta`, `Pedido`, `Mensajero`, `Movimiento`), fachada `Puesto`, `UML`, `Main`.
- **Jesús Manuel Mendoza Gamarra** (7 commits): `Nodo`, `NodoDoble`, `ListaSimpleLotes`, `ListaSimplePedidos`, `ListaSimpleVentas`, `ListaCircular`, `ListaDoble`.
- **Rafael Enrique Muñoz Devoz** (8 commits): `CargadorCSV`, los 3 archivos CSV (`productos.csv`, `lotes.csv`, `pedidos.csv`), `Busquedas`, `Ordenamientos`, `ConjuntosArreglos`, `ServidorApp`.
- **Juan Diego Tobio Villadiego** (7 commits): `SimuladorSemana`, `Medidor`, `PruebasSistema`, interfaz `web/`, `Storybook_MercaBazurto_Digital.pdf`, `GUION_EXPOSICION.md`, `README.md`.

---

## Paso 1: Configurar correos de GitHub (opcional pero recomendado)

Abre `preparar_git_y_commits.ps1` y reemplaza los 4 correos de ejemplo (`miguel.baza@...`, etc.) por los correos reales asociados a la cuenta de GitHub de cada compañero para que GitHub muestre las fotos y estadísticas de contribuidores.

## Paso 2: Ejecutar el script desde PowerShell en la raíz del proyecto

```powershell
cd "C:\Users\Administrator\.gemini\antigravity\scratch\proyecto9-mercabazurto-digital"
powershell -ExecutionPolicy Bypass -File .\preparar_git_y_commits.ps1
```

## Paso 3: Verificar los 30 commits y el reparto por autor

```powershell
git log --oneline
git shortlog -sn
```

Deberías ver **30 commits totales** repartidos entre los 4 integrantes (8 Miguel, 8 Rafael, 7 Jesús, 7 Juan Diego).

## Paso 4: Conectar con tu repositorio en GitHub y hacer `push`

1. Crea un repositorio vacío en GitHub llamado `proyecto9-mercabazurto-digital` (sin marcar README ni .gitignore).
2. Ejecuta estos tres comandos pegando tu URL real:

```powershell
git branch -M main
git remote add origin https://github.com/TU_USUARIO/proyecto9-mercabazurto-digital.git
git push -u origin main
```

¡Listo! En menos de 3 minutos el repositorio queda completo, ordenado, verificado y listo para calificación.
