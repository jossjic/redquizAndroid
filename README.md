# Proyecto RedQuiz (Versión Android 24)

RedQuiz es una aplicación de trivia diseñada para preparar a voluntarios novatos de la Cruz Roja en temas de salud. Ofrece una trivia con categorías como signos vitales, anatomía, etc. Además, motivan a los usuarios con recompensas y avatares personalizables. La aplicación permite a los usuarios rastrear su progreso mediante estadísticas detalladas y cuenta con un sistema de gestión de preguntas para que los administradores puedan mantener el contenido actualizado. En resumen, RedQuiz combina aprendizaje interactivo, motivación y seguimiento del progreso para una preparación efectiva de voluntarios.
## Librerías y Tecnologías Utilizadas

1. **Firebase Authentication:**
   - Firebase Authentication se utiliza para la autenticación de usuarios.
   - [Firebase Authentication](https://firebase.google.com/products/auth)

2. **Firebase Firestore:**
   - Firestore es una base de datos en tiempo real en la nube que se utiliza para almacenar y sincronizar datos entre usuarios y dispositivos.
   - [Firebase Firestore](https://firebase.google.com/products/firestore)

3. **MediaPlayer (Android):**
   - La clase `MediaPlayer` se utiliza para reproducir sonidos en la aplicación Android.
   - [MediaPlayer Android Documentation](https://developer.android.com/reference/android/media/MediaPlayer)

4. **AndroidX Libraries:**
   - `AppCompatActivity`, `Handler`, `Timer`, y otras clases de AndroidX son utilizadas en tu código.
   - [AndroidX Overview](https://developer.android.com/jetpack/androidx)

## Características Principales

- **Preguntas y Respuestas:** La aplicación muestra preguntas y respuestas relacionadas con diversas categorías médicas, como Signos Vitales, Curación, Síntomas, Anatomía y Bonus.

- **Barra de Progreso:** Se implementa una barra de progreso que mide el tiempo para responder cada pregunta. Si el tiempo se agota, se disminuye el número de vidas del usuario y se pasa a la siguiente pregunta.

- **Sonidos:** Se reproducen sonidos distintivos para indicar respuestas correctas, incorrectas y al finalizar la sesión.

- **Conexión a Internet:** Verifica la conexión a Internet al inicio de la actividad y notifica al usuario si no hay conexión.

- **Autenticación:** Utiliza Firebase Authentication para autenticar a los usuarios.

- **Vidas del Usuario:** Muestra el número de vidas restantes del usuario, disminuyendo en caso de respuestas incorrectas.

## Requisitos Previos

Antes de ejecutar la aplicación, asegúrate de tener acceso a Internet y haber iniciado sesión en la aplicación con tu cuenta.

## Configuración de Firebase

Este proyecto utiliza Firebase para almacenar datos y autenticar usuarios. Asegúrate de haber configurado correctamente tu proyecto en Firebase y haber agregado el archivo de configuración `google-services.json` en el directorio `app/`.

## Estructura del Proyecto

El código fuente está organizado en varias clases y funciones:

- **PreguntaActivity:** La actividad principal que maneja el cuestionario y la interacción del usuario.
- **SoundManager:** Clase estática que gestiona la reproducción de sonidos en la aplicación.

## Cómo Utilizar

1. **Inicio de Sesión:** Inicia sesión con tu cuenta.
2. **Cuestionario:** Responde las preguntas dentro del tiempo límite y acumula puntos.
3. **Vidas:** Tienes un número limitado de vidas. Cada respuesta incorrecta disminuirá el número de vidas.
4. **Finalización:** Al finalizar, se reproducirá un sonido indicando el resultado y se mostrarán estadísticas.

## Notas Importantes

- Asegúrate de tener una conexión a Internet estable para el correcto funcionamiento de la aplicación.
- Si no hay conexión a Internet al iniciar la aplicación, se te desconectará y se te redirigirá al menú principal.
