# Carlina Palius

App Android nativa (Kotlin + Jetpack Compose) per compartir coses entre els membres de la família.
De moment té un menú amb una sola opció: una **llista de tasques compartida**.

## Descarregar

📲 **[Baixa l'última versió (APK)](https://github.com/realbluehead/carlina-palius/releases/latest/download/carlina-palius.apk)**

Obre l'enllaç des del mòbil, accepta instal·lar apps d'aquesta font i toca **Instal·la**.

## Com funciona la sincronització

- Les dades es guarden a **Firebase Cloud Firestore** (`families/{codi}/todos`).
- La primera vegada, cadascú escriu el seu nom i, o bé **crea una família** (l'app genera un codi tipus `ABCD-EFGH`), o bé **s'hi uneix** amb el codi que li ha passat un altre membre.
- Tothom que té el mateix codi veu la mateixa llista, i els canvis arriben a l'instant.
- Funciona sense connexió: els canvis es guarden al mòbil i es pugen quan torna la xarxa.
- No cal fer cap compte: l'app inicia sessió de forma anònima a Firebase.

## Configurar Firebase (una sola vegada, gratuït)

1. Ves a <https://console.firebase.google.com> i crea un projecte (p. ex. `carlina-palius`). Pots desactivar Google Analytics.
2. **Afegeix una app Android** amb el nom de paquet `cat.carlinapalius`.
3. Descarrega el fitxer `google-services.json` i posa'l a `app/google-services.json`.
4. A **Build → Authentication → Sign-in method**, activa **Anònim**.
5. A **Build → Firestore Database**, crea la base de dades (ubicació `eur3` o `europe-west1`).
6. A la pestanya **Regles** de Firestore, enganxa el contingut de [`firestore.rules`](firestore.rules) i publica.

Sense el `google-services.json` l'app compila igualment, però mostra un avís que falta configurar la sincronització.

## Compilar i instal·lar

Cal l'Android SDK (el d'Android Studio va bé). Des d'aquesta carpeta:

```bash
JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew assembleRelease
```

L'APK queda a `app/build/outputs/apk/release/app-release.apk`. Envia'l als altres membres de la família (WhatsApp, correu…) i instal·leu-lo (cal permetre «instal·lar apps de fonts desconegudes»).

Per provar-ho directament en un mòbil connectat per USB:

```bash
JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew installDebug
```

## Actualitzacions automàtiques

Cada cop que s'obre, l'app mira a GitHub (`releases/latest/download/version.json`) si hi ha una versió amb un `versionCode` més alt. Si n'hi ha, la baixa i la instal·la.

- La **primera** actualització demana tocar «Actualitza» (i, si cal, permetre que Carlina Palius instal·li apps).
- A partir d'aleshores, a Android 12 o més, s'instal·len **soles**, sense preguntar.

Per publicar una versió nova: puja `versionCode` i `versionName` a `app/build.gradle.kts` i executa

```bash
./release.sh "Què hi ha de nou"
```

S'ha de compilar sempre des del mateix Mac: l'APK es signa amb la clau de debug local, i si la clau canvia els mòbils no poden actualitzar.

## Estructura

```
app/src/main/java/cat/carlinapalius/
├── MainActivity.kt          navegació: configuració → menú → llista
├── data/
│   ├── FamilyPrefs.kt       codi de família i nom, guardats al mòbil
│   ├── Updater.kt           comprova i instal·la versions noves des de GitHub
│   └── TodoRepository.kt    lectura/escriptura a Firestore en temps real
└── ui/
    ├── SetupScreen.kt       crear o unir-se a una família
    ├── MenuScreen.kt        menú principal (afegir-hi les opcions noves aquí)
    ├── TodoScreen.kt        la llista de tasques
    └── Theme.kt
```

## Seguretat

El codi de família fa de clau: qui el sàpiga pot llegir i escriure la llista. Té 8 caràcters aleatoris (~10¹² combinacions), més que suficient per a una llista familiar, però no hi guardeu res delicat.
