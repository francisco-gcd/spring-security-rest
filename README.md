# Descripción
Aquí encontraras un proyecto de ejemplo en el que se ha creado una API Rest que está securizada con Spring security, utilizando como metodo JWT (Json Web Token). 
Además, para mantener la persistencia de usuarios se ha utilizado MongoDB con Spring Data.

Para utilizar la API se exponen las siguientes URL:

|URL|Metodo|Descripción|
|---|---|---|
|`GET`|`rest-api/user`| Todos los usuarios|
|`GET`|`rest-api/user/{id}`| Detalle usuario|
|`POST`|`rest-api/user`| Agregar usuario|
|`PUT`|`rest-api/user`| Modificar usuario|
|`DELETE`|`rest-api/user/{id}`|Borrar usuario|
|`POST`|`rest-api/login?username={username}&password={password}`|Autenticar|
|`POST`|`rest-api/logout`|Salir|

### Ejemplo de funcionamiento
Para comprender como debe utilizarse a continuación enumeramos un ejemplo explicativo.

* Registramos un usuario llamando a la url y enviando el siguiente JSON.
```
http://localhost:8080/rest-api/user

{
  "password": "123456789",
  "username": "user",
  "accountNonExpired": true,
  "accountNonLocked": true,
  "credentialsNonExpired": true,
  "enabled": true,
  "authorities": [
		{
			"authority" : "ROLE_USER"
		}
	]
}
```

* Nos autenticamos en la aplicación recibiendo en el campo `X-Auth-Token` de la cabecera el token.

```
http://localhost:8080/rest-api/login?username=user&password=123456789

X-Auth-Token →eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJhdXRob3JpdGllcyI6W3siYXV0aG9yaXR5IjoiUk9MRV9VU0VSIn1dLCJ1c2VybmFtZSI6InVzZXIifQ==.1MPTBOUc6AqC3IBqoxAnlG1WnX2FuTuRCR5+y95mxmw=
```

* Pedimos ver todos los usuarios para lo que pasamos el token por la cabecera de la llamada HTTP.

```
http://localhost:8080/rest-api/user

X-Auth-Token →eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJhdXRob3JpdGllcyI6W3siYXV0aG9yaXR5IjoiUk9MRV9VU0VSIn1dLCJ1c2VybmFtZSI6InVzZXIifQ==.1MPTBOUc6AqC3IBqoxAnlG1WnX2FuTuRCR5+y95mxmw=
	
[
  {
    "id": "585049a7f8982a0543b9c23f",
    "password": "$2a$07$WT.m6kpQ94y0wDAOrLhUv.hX3k/By7wifXbS.LFDyPXbRsIYFHH3S",
    "username": "user",
    "accountNonExpired": true,
    "accountNonLocked": true,
    "credentialsNonExpired": true,
    "enabled": true,
    "authorities": [
      {
	"authority": "ROLE_USER"
      }
    ]
  }
]
```
	
# Configuración
En la aplicación se encontrará el fichero app.properties que contiene los valores de configuración, en concreto serán:
* _mongodb.host_: la dirección ip donde estará el servidor MongoDB.
* _mongodb.port_: el puerto por el que se comunicará con MongoDB.
* _mongodb.database_: el nombre de la base de datos.
* _security.secret_: será una palabra que nos permitirá codificar las constraseñas y el token JWT.
* _security.strength_: será la fortaleza para codificar la contraseña.

# JWT (Json Web Token)

Es un estandar abierto ([RFC 7519](https://tools.ietf.org/html/rfc7519)) que se utilizar para transmitir información securizada y que se utilizará para mantener la información del usuario autenticado. 
Su estructura se componen de tres secciones separadas por `.` y codificadas en BASE64.
* La cabecera: contiene el tipo de algoritmo que se utiliza para codificar la firma y el tipo de token.
* Información util: contiene los valores que se desean transmitir bajo seguridad. Alguno de los campos que contienen son parte del estandar, como por ejemplo exp (expiration time). También puede contener campos con información privada.
* Firma: Contiene los datos para verificar que el que envía el token es quien dice ser.

Para probar el token podemos utilizar [https://jwt.io/] (https://jwt.io/)

Podemos encontrar un mayor detalle en la página [https://jwt.io/introduction/] (https://jwt.io/introduction/)

# Spring Security

La configuración de spring security se realizará en el fichero `SecurityConfiguration` en el paquete `es.pongo.configuration`. Además de la configuración común se destacará los siguientes puntos:

* No se matendrá el estado en el servidor, por lo que una vez logado y recibido el token, este deberá enviar junto a la petición en la cabecera.
* Se sustituye el metodo de autentificación en la pila de filtros para que utilice la autentificación por JWT.
* Se sobreescriben los métodos para el caso de "acceso denegado", "autenticacion fallido" y "comprobacion de acceso a una url" para que la respuesta del error se de en JSON.
* El token JWT se pasará a través de la cabecera, en el campo `X-Auth-Token`. 

# Spring Data

La configuración de spring security se realizará en el fichero `MongoDBConfiguration` en el paquete `es.pongo.configuration`. Para un mayor detalle se puede consultar [la documentación](http://docs.spring.io/spring-data/mongodb/docs/current/reference/html/) de spring.

# Spring Validation

Se agrega una clase de validación para la entidad `User` del modelo. Esta se encuentra en `es.pongo.controller.user` con nombre `UserValidator` y que será ligada en el controlador.
