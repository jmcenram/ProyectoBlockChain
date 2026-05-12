# BlockchainApp

BlockchainApp es una aplicación de escritorio desarrollada en JavaFX para gestionar documentos y registrar su integridad mediante hashes en blockchain.

El objetivo del proyecto es permitir que una entidad emisora registre documentos, controle usuarios y roles, y verifique posteriormente si un documento conserva su integridad o si ha sido revocado. Para ello combina una base de datos local SQLite con un contrato inteligente accesible mediante Web3j.

## Finalidad

El sistema está orientado a la trazabilidad documental. Permite:

- Crear y gestionar usuarios, roles y entidades emisoras.
- Subir documentos y calcular su hash SHA-256.
- Registrar hashes de documentos en blockchain.
- Revocar documentos registrados.
- Verificar documentos contra la base de datos local y contra blockchain.
- Generar códigos QR con información de verificación.
- Mantener auditoría de operaciones relevantes.

Si la configuración blockchain no está disponible, la aplicación puede arrancar en modo degradado para funcionalidades locales.

## Tecnologías

- Java 17
- JavaFX 17 y FXML
- Maven
- JPA con Hibernate 6
- SQLite
- Web3j
- Solidity
- BCrypt para hash de contraseñas
- Jakarta Mail para envío de correos
- ZXing para generación de QR
- Ikonli para iconos JavaFX
- JUnit 5 y Mockito para tests

## Requisitos

- JDK 17 instalado y configurado en `PATH`.
- Maven instalado y configurado en `PATH`.
- Windows, según la configuración actual de JavaFX en `pom.xml`:

```xml
<javafx.platform>win</javafx.platform>
```

- Nodo blockchain compatible con JSON-RPC si se quiere registrar o revocar documentos en blockchain.
- Contrato inteligente desplegado y configurado en la aplicación.

## Configuración

La configuración blockchain incluye:

- URL RPC del nodo.
- Dirección del contrato inteligente.
- Clave privada usada para firmar operaciones cuando corresponda.

La aplicación gestiona la configuración desde `ConfigManager`, que por defecto usa:

```text
%APPDATA%/BlockchainApp/config/blockchain.properties
```

También hay recursos de apoyo dentro del proyecto:

- `src/main/resources/app-config/default-config.json`
- `src/main/resources/META-INF/persistence.xml`
- `src/main/resources/db/schema.sql`
- `src/main/resources/db/data.sql`
- `src/main/resources/db/database.db`

No se deben usar claves privadas reales en archivos versionados.

## Estructura

```text
src/main/java/es/jmcenram/blockchain
├── config          Configuración de aplicación, JPA y blockchain
├── connection      Conexión SQLite
├── controller      Controladores JavaFX
├── contract        Wrapper Java del contrato Web3j
├── dto             Objetos de transferencia
├── mapper          Conversores de estados
├── model           Entidades del dominio
├── repository      DAOs y acceso a datos
├── service         Lógica de negocio
└── util            Utilidades comunes
```

Recursos principales:

```text
src/main/resources
├── contracts       Contrato Solidity
├── css             Estilos JavaFX
├── db              SQLite, schema y datos iniciales
├── img             Iconos e imágenes
├── view            Vistas FXML
└── messages*.properties  Internacionalización
```

## Compilación

Desde la raíz del proyecto:

```bash
mvn clean compile
```

Para ejecutar los tests:

```bash
mvn test
```

Para generar el paquete:

```bash
mvn clean package
```

El `pom.xml` configura `maven-shade-plugin`, por lo que el empaquetado genera un JAR sombreado en `target` con clasificador `shaded`.

## Instalación

En Windows, la aplicación también se puede instalar ejecutando el instalador:

```text
./BlockchainApp-Setup.exe
```

El instalador crea la aplicación de escritorio para usarla sin ejecutar Maven manualmente.

## Ejecución

Ejecutar con JavaFX desde Maven:

```bash
mvn javafx:run
```

La configuración actual del plugin JavaFX incluye arranque en modo debug con suspensión:

```text
-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:5005
```

Si la aplicación queda esperando al arrancar, conecta un depurador al puerto `5005` o elimina temporalmente esa opción del `pom.xml`.

## Base de datos

El proyecto usa SQLite. Los scripts principales son:

- `src/main/resources/db/schema.sql`: creación de tablas.
- `src/main/resources/db/data.sql`: datos iniciales.

Las entidades JPA están configuradas en:

```text
src/main/resources/META-INF/persistence.xml
```

## Contrato inteligente

El contrato Solidity está en:

```text
src/main/resources/contracts/RegistroDocumentos.sol
```

Web3j se utiliza para interactuar con el contrato desde Java. El wrapper Java generado o mantenido en el proyecto está en:

```text
src/main/java/es/jmcenram/blockchain/contract/RegistroDocumentos.java
```

## Autor

Jcena
