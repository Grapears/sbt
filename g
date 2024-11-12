<template>
  <div class="register-container">
    <h2>Register</h2>
    <form @submit.prevent="onSubmit">
      <div class="form-group">
        <label for="username">Username</label>
        <input
          id="username"
          v-model="formData.username"
          type="text"
          placeholder="Enter username"
          required
        />
      </div>

      <div class="form-group">
        <label for="password">Password</label>
        <input
          id="password"
          v-model="formData.password"
          type="password"
          placeholder="Enter password"
          required
        />
      </div>

      <div class="form-group">
        <label for="confirmPassword">Confirm Password</label>
        <input
          id="confirmPassword"
          v-model="formData.confirmPassword"
          type="password"
          placeholder="Confirm password"
          required
        />
      </div>

      <div v-if="errorMessage" class="error-message">
        {{ errorMessage }}
      </div>

      <button type="submit" :disabled="isSubmitting">Register</button>
    </form>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import axios from 'axios';

interface RegisterFormData {
  username: string;
  password: string;
  confirmPassword: string;
}

const formData = ref<RegisterFormData>({
  username: '',
  password: '',
  confirmPassword: ''
});

const isSubmitting = ref(false);
const errorMessage = ref<string | null>(null);

const onSubmit = async () => {
  if (formData.value.password !== formData.value.confirmPassword) {
    errorMessage.value = 'Passwords do not match';
    return;
  }

  isSubmitting.value = true;
  errorMessage.value = null;

  try {
    const response = await axios.post('/api/register', formData.value);
    if (response.data.message === 'success') {
      // Handle success (e.g., redirect to login or home)
      alert('Registration successful!');
      // Reset form data after successful registration
      formData.value = { username: '', password: '', confirmPassword: '' };
    } else {
      errorMessage.value = response.data.error || 'An error occurred during registration';
    }
  } catch (error) {
    errorMessage.value = 'An unexpected error occurred. Please try again later.';
    console.error(error);
  } finally {
    isSubmitting.value = false;
  }
};
</script>

<style scoped>
.register-container {
  max-width: 400px;
  margin: 0 auto;
  padding: 2rem;
  border: 1px solid #ddd;
  border-radius: 8px;
  background-color: #f9f9f9;
}

h2 {
  text-align: center;
  margin-bottom: 1.5rem;
}

.form-group {
  margin-bottom: 1rem;
}

label {
  display: block;
  font-weight: bold;
}

input {
  width: 100%;
  padding: 0.8rem;
  margin-top: 0.5rem;
  border: 1px solid #ccc;
  border-radius: 4px;
}

button {
  width: 100%;
  padding: 0.8rem;
  background-color: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 1rem;
  cursor: pointer;
  margin-top: 1rem;
}

button:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

.error-message {
  color: red;
  font-size: 0.9rem;
  margin-top: 1rem;
}
</style>




package controllers

import javax.inject._
import play.api._
import play.api.mvc._
import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global
import java.util.UUID
import models.domains._
import models.repos._
import play.api.libs.json._

@Singleton
class UserController @Inject()(val controllerComponents: ControllerComponents, val userRepo: UserRepo)(ec: ExecutionContext) extends BaseController {

  def register = Action.async(parse.json) { implicit request: Request[JsValue] =>
    request.body.validate[User].fold(
      errors => Future.successful(BadRequest(Json.obj("message" -> "error", "errors" -> errors.mkString(",")))),
      users => {
        userRepo.findUser(users.username).flatMap{
          case Some(_) => Future.successful(BadRequest(Json.obj("message" -> "error", "error" -> "Username already taken")))
          case None => {
            userRepo.add(users).map{
              case 1 => Ok(Json.obj("message" -> "success", "data" -> users))
              case _ => BadRequest(Json.obj("message" -> "error", "error" -> "Failed to register"))
            }
          }
        }
      }
    )
  }

  def updateUser(id: UUID, username: String) = Action.async { implicit request: Request[AnyContent] =>
    userRepo.updateUser(id, username).fold(
      
    )
  }
}
-----------_


package models.repos

import java.util.UUID
import javax.inject._
import slick.jdbc.PostgresProfile
import play.api.db.slick._
import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global
import models.domains._


@Singleton
class UserRepo @Inject()(dbConfigProvider: DatabaseConfigProvider){
  val dbConfig = dbConfigProvider.get[PostgresProfile]
  import dbConfig._
  import profile.api._

  class UserTable(tag:Tag) extends Table[User](tag, "USERS"){

    def idUser = column[UUID]("ID_USERS" ,O.PrimaryKey)
    def username = column[String]("USERNAME")
    def password = column[String]("PASSWORD")

    def * = (idUser, username, password).mapTo[User]
  } 

  val users = TableQuery[UserTable]

  def add(user: User): Future[Int] =
    val query = users += user
    db.run(query)

  def get(id: UUID): Future[Option[User]] =
    val query = users.filter(_.idUser === id).result.headOption
    db.run(query)

  def findUser(username: String): Future[Option[User]] =
    val query = users.filter(_.username === username).result.headOption
    db.run(query)  

  def updateUser(id: UUID, username: String): Future[Int] =
    val query = users.filter(_.idUser === id).map(_.username).update(username)
    db.run(query)
  
}













