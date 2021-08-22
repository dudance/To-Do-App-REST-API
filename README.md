# To-Do-App-REST-API
> Application for managing tasks using REST API

## Table of Contents
* [General Info](#general-information)
* [Technologies Used](#technologies-used)
* [Features](#features)
* [Usage](#usage)
* [Project Status](#project-status)
* [Room for Improvement](#room-for-improvement)

## General Information
The subject of the task is to implement a web application for task management. The application allows you to create users and manage simple tasks (create, display, delete, ...)


## Technologies Used
- Java 11 with HttpExchange and HttpServer of Java Standard Library
- Gson
- REST API
- JUnit tests


## Features
1. The application handles the following HTTP requests
    * _method_ - HTTP method of the request,
    * _address_ - URL path of the requested resource,
    * _headers_ - variables sent in the request header,
    * _parameters_ - variables sent in the resource path,
    * _contents_ - an example of the possible content of the request,
    * _responses_ - supported status codes and an example of possible response content
  
    method | address | headers | parameters | contents | responses
    ------ | ----- | -------- | --------- | ----- | ----------
    POST | /todo/user | | | <pre>{<br/>&#9;"username": "janKowalski",<br/>&#9;"password": "am!sK#123"<br/>}</pre> | <ul> <li>201</li><li>400</li><li>409</li> </ul>
    POST | /todo/task | auth | | <pre>{<br/>&#9;"description": "Buy milk",<br/>&#9;"due": "2021-06-30"<br/>}</pre> | <ul><li>201<pre>{<br/>&#9;"id": "237e9877-e79b-12d4-a765-321741963000"<br/>}</li><li>400</li><li>401</li><ul>
    GET | /todo/task | auth | | | <ul><li>200<pre>[<br/>&#9;{<br/>&#9;&#9;"id": "237e9877-e79b-12d4-a765-321741963000",<br/>&#9;&#9;"description": "Buy milk",<br/>&#9;&#9;"due": "2021-06-30"<br/>&#9;}<br/>]</pre></li><li>400</li><li>401</li></ul>
    GET | /todo/task/{id} | auth | id | | <ul><li>200<pre>{<br/>&#9;"id": "237e9877-e79b-12d4-a765-321741963000",<br/>&#9;"description": "Buy milk",<br/>&#9;"due": "2021-06-30"<br/>}</pre></li><li>400</li><li>401</li><li>403</li><li>404</li></ul>
    PUT | /todo/task/{id} | auth | id | <pre>{<br/>&#9;"description": "Buy milk",<br/>&#9;"due": "2021-06-30"<br/>}</pre> | <ul><li>200<pre>{<br/>&#9;"id": "237e9877-e79b-12d4-a765-321741963000",<br/>&#9;"description": "Buy milk",<br/>&#9;"due": "2021-06-30"<br/>}</pre></li><li>400</li><li>401</li><li>403</li><li>404</li></ul>
    DELETE | /todo/task/{id} | auth | id |  | <ul><li>200</li><li>400</li><li>401</li><li>403</li><li>404</li></ul>
  
    * **auth** - string 'base64(username):base64(password)', where base64() stands for the the Base64 encoding function. E.g., for
    the user `{ "username": "janKowalski", "password": "am!sK#123" }`, `auth` will be equal `amFuS293YWxza2k=:YW0hc0sjMTIz`
    * **id** - unique task identifier in UUID format.

    :warning:For information on which headers, parameters, or fields in JSON documents are required, see detailed [documentation of Swagger API](https://epam-online-courses.github.io/efs-task9-todo-app/)


## Usage
Please send an appropriate http request in accordance with the table above.


## Project Status
Project is: _complete_


## Room for Improvement

Room for improvement:
- Save/load data from/to file


