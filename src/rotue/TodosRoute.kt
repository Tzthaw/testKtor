package com.put.rotue


import com.put.API_VERSION
import com.put.auth.MySession
import com.put.repository.Repository
import io.ktor.application.application
import io.ktor.application.call
import io.ktor.application.log
import io.ktor.auth.authenticate
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.locations.*
import io.ktor.locations.post
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.*
import io.ktor.sessions.*

const val TODOS = "$API_VERSION/todos"

@KtorExperimentalLocationsAPI
@Location(TODOS)
class TodoRoute

@KtorExperimentalLocationsAPI
fun Route.todos(db:Repository){
    authenticate("jwt") {
        post<TodoRoute> {
            val todoParameter = call.receive<Parameters>()
            val todo = todoParameter["todo"]?:
                return@post call.respond(HttpStatusCode.BadRequest,"Missing Fields")
            val done = todoParameter["done"]?:"false"
            val user = call.sessions.get<MySession>()?.let {
                db.findUser(it.userId)
            }
            if (user == null) {
                call.respond(
                    HttpStatusCode.BadRequest, "Problems retrieving User")
                return@post
            }
            try {
                val currentTodo = db.addTodo(
                    user.userId,todo,done.toBoolean()
                )
                currentTodo?.id?.let {
                    call.respond(HttpStatusCode.OK, currentTodo)
                }
            }catch (e:Throwable){
                application.log.error("Failed to add todo", e)
                call.respond(HttpStatusCode.BadRequest, "Problems Saving Todo")
            }
        }
    }
}