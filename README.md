# Descripción
Aquí encontraras un proyecto de ejemplo en el que se ha creado una API Rest que está securizada con Spring security, utilizando como metodo JWT (Json Web Token). 
Además, para mantener la persistencia de usuarios se ha utilizado MongoDB con Spring Data.

Para utilizar la API se exponen las siguientes URL (TODO completar):

|URL|Metodo|
|------|---|
|`GET`|`rest-api/user`|
|`GET`|`rest-api/user/{id}`|
|`POST`|`rest-api/user`|
|`PUT`|`rest-api/user`|
|`DELETE`|`rest-api/user/{id}`|
|`POST`|`rest-api/login?username={username}&password={password}`|
|`POST`|`rest-api/logout`|

## JWT (Json Web Token)

Es un estandar abierto ([RFC 7519](https://tools.ietf.org/html/rfc7519)) que se utilizar para transmitir información securizada y que se utilizará para mantener la información del usuario autenticado. 
Su estructura se componen de tres secciones separadas por `.` y codificadas en BASE64.
* La cabecera: contiene el tipo de algoritmo que se utiliza para codificar la firma y el tipo de token.
* Información util: contiene los valores que se desean transmitir bajo seguridad. Alguno de los campos que contienen son parte del estandar, como por ejemplo exp (expiration time). También puede contener campos con información privada.
* Firma: Contiene los datos para verificar que el que envía el token es quien dice ser.

Para probar el token podemos utilizar [https://jwt.io/] (https://jwt.io/)

Podemos encontrar un mayor detalle en la página [https://jwt.io/introduction/] (https://jwt.io/introduction/)

## Spring Security (TODO)

## Spring Data (TODO)

## Spring Validation (TODO)

## TODO
 * Solo puede modificar los datos el mismo usuario o el administrador.
 * Solo puede borrarse el mismo usuario o el administrador.
 * Solo puede ver el detalle el mismo usuario o el administrador.
 * Agregar fecha de expiración al token.
 * Agregar paginación a las búsquedas de usuario.
