# BlockchainApp

Aplicacion JavaFX para gestionar documentos, usuarios y entidades emisoras, con registro y verificacion de hashes en blockchain.

## Requisitos

- JDK 17
- Maven
- SQLite incluido en el proyecto
- Nodo blockchain compatible con Web3j si se quiere usar el registro en blockchain

## Ejecucion

Compilar y ejecutar tests:

```bash
mvn test
```

Ejecutar la aplicacion JavaFX:

```bash
mvn javafx:run
```

Generar el paquete:

```bash
mvn package
```

## Configuracion

La aplicacion carga la configuracion de blockchain al arrancar. Si no hay RPC o contrato valido, arranca en modo degradado sin blockchain.

Archivos relevantes:

- `blockchain.properties`: configuracion local de RPC, contrato y clave.
- `src/main/resources/app-config/default-config.json`: configuracion base de la aplicacion.
- `src/main/resources/META-INF/persistence.xml`: configuracion JPA/Hibernate.
- `src/main/resources/db/database.db`: base de datos SQLite local.

No uses claves privadas reales en archivos versionados.

## Estructura

- `src/main/java/es/jmcenram/blockchain/controller`: controladores JavaFX.
- `src/main/java/es/jmcenram/blockchain/service`: logica de negocio.
- `src/main/java/es/jmcenram/blockchain/repository`: DAOs y acceso a datos.
- `src/main/java/es/jmcenram/blockchain/model`: entidades del dominio.
- `src/main/resources/view`: vistas FXML.
- `src/main/resources/css`: estilos.
- `src/test/java`: tests.

## DAOs principales

- `EntidadEmisoraRepository`: entidades emisoras.
- `UsuarioRepository`: usuarios, autenticacion y roles.
- `RolRepository`: roles del sistema.
- `DocumentoRepository`: documentos y estados.
- `RegistroBlockchainRepository`: registros de documentos en blockchain.
- `AuditoriaRepository`: trazas de auditoria.
- `UsuarioRolRepository`: relacion usuario-rol.

Los repositorios heredan operaciones comunes de `BaseRepository`, incluyendo guardado, busqueda por id, listado, actualizacion y borrado logico.

## Tecnologias

- Java 17
- JavaFX
- JPA / Hibernate
- SQLite
- Web3j
- BCrypt
- JUnit 5
- Mockito
