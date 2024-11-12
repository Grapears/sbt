libraryDependencies ++= Seq(
    ws,
    ehcache,
    "org.playframework" %% "play-slick" % "6.1.1",
    "org.playframework" %% "play-slick-evolutions" % "6.1.1",
    "org.postgresql" % "postgresql" % "42.7.4",
    "org.scalatestplus" %% "mockito-5-12" % "3.2.19.0" % "test"
)
-----------
play.filters {
    enabled += play.filters.cors.CORSFilter
    disabled += play.filters.csrf.CSRFFilter
}

slick.dbs.default {
    profile = "slick.jdbc.PostgresProfile$"
    db = {
        driver = "org.postgresql.Driver"
        url = "jdbc:postgresql://localhost:5432/db"
        user = "hncsamontiza"
    }
}
----------
package controllers

import javax.inject._
import play.api._
import play.api.mvc._
import play.api.libs.json._

import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global

import java.util.UUID

import models.domain._
import models.repo._

@Singleton
class UserController @Inject()(val controllerComponents: ControllerComponents, val userRepo: UserRepo) extends BaseController{

  def register = Action.async(parse.json){ implicit request: Request[JsValue] =>
    request.body.validate[User].fold(
      errors => Future.successful(BadRequest(Json.obj("message" -> "error", "error" -> errors.mkString(",")))),
      user => {
        userRepo.findByEmail(user.emailUser).flatMap{
          case Some(_) => Future.successful(BadRequest(Json.obj("message" -> "error", "error" -> "Email already exists")))
          case None => { 
            val newUser = user.copy(idUser = UUID.randomUUID())
            userRepo.add(newUser).map{
            case 1 => Ok(Json.obj("message" -> "success", "data" -> user))
            case _ => BadRequest(Json.obj("message" -> "error", "error" -> "Failed to register user"))
          }.recover{
            case e: Exception => BadRequest(Json.obj("message" -> "error", "error" -> e.getMessage))
          }}
        }.recover{
          case e: Exception => BadRequest(Json.obj("message" -> "error", "error" -> e.getMessage))
        }
      }
    )
  }

  def login = Action.async(parse.json){ implicit request: Request[JsValue] =>
    request.body.validate[UserLogin].fold(
      errors => Future.successful(BadRequest(Json.obj("message" -> "error", "error" -> errors.mkString(",")))),
      userLogin => {
        userRepo.findByEmail(userLogin.emailUser).flatMap{
          case Some(user) => {
            if(user.passwordUser == userLogin.passwordUser){
              Future.successful(Ok(Json.obj("message" -> "success", "data" -> user)).withSession("email" -> user.emailUser))
            }else{
              Future.successful(BadRequest(Json.obj("message" -> "error", "error" -> "Invalid password")))
            }
          }
          case None => Future.successful(BadRequest(Json.obj("message" -> "error", "error" -> "User not found")))
        }.recover{
          case e: Exception => BadRequest(Json.obj("message" -> "error", "error" -> e.getMessage))
        }
      }
    )
  }

  def logout = Action { implicit request: Request[AnyContent] =>
    Ok(Json.obj("message" -> "success")).withNewSession
  }

  def getUser = Action.async { implicit request: Request[AnyContent] => 
    request.session.get("email") match {
        case Some(email) => Future.successful(Ok(Json.obj("message" -> "success", "data" -> email)))
        case None => Future.successful(BadRequest(Json.obj("message" -> "error", "error" -> "No User Found")))
    }
  }

  def listUsers = Action.async { implicit request: Request[AnyContent] =>
    userRepo.list().map { users =>
      Ok(Json.toJson(users))
    }.recover {
      case e: Exception => BadRequest(Json.obj("message" -> "error", "error" -> e.getMessage))
    }
  }
}
-----------------------
package controllers

import javax.inject._
import play.api._
import play.api.mvc._
import play.api.libs.json._

import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global

import java.util.UUID

import models.domain._
import models.repo._

@Singleton
class MemberController @Inject()(val controllerComponents: ControllerComponents, val memberRepo: MemberRepo) extends BaseController{

  def addMember = Action.async(parse.json){ implicit request: Request[JsValue] =>
    request.body.validate[Member].fold(
      errors => Future.successful(BadRequest(Json.obj("message" -> "error", "error" -> errors.mkString(",")))),
      member => {
        val newMember = member.copy(idMember = 0)
        memberRepo.add(newMember).map{
          case 1 => Ok(Json.obj("message" -> "success", "data" -> member))
          case _ => BadRequest(Json.obj("message" -> "error", "error" -> "Failed to add member"))
        }
      }
    )
  }

  def getMember(id: Long) = Action.async { implicit request: Request[AnyContent] =>
    memberRepo.findById(id).map {
      case Some(member) => Ok(Json.toJson(member))
      case None => NotFound(Json.obj("message" -> "error", "error" -> "Member not found"))
    }.recover {
      case e: Exception => BadRequest(Json.obj("message" -> "error", "error" -> e.getMessage))
    }
  }

  def listMembers = Action.async { implicit request: Request[AnyContent] =>
    memberRepo.findAll().map { members =>
      Ok(Json.toJson(members))
    }.recover {
      case e: Exception => BadRequest(Json.obj("message" -> "error", "error" -> e.getMessage))
    }
  }

  def updateMember(id: Long) = Action.async(parse.json) { implicit request: Request[JsValue] =>
    request.body.validate[Member].fold(
      errors => Future.successful(BadRequest(Json.obj("message" -> "error", "error" -> errors.mkString(",")))),
      member => {
        memberRepo.update(member.copy(idMember = id)).map {
          case 1 => Ok(Json.obj("message" -> "success", "data" -> member))
          case _ => BadRequest(Json.obj("message" -> "error", "error" -> "Failed to update member"))
        }.recover {
          case e: Exception => BadRequest(Json.obj("message" -> "error", "error" -> e.getMessage))
        }
      }
    )
  }

  def deleteMember(id: Long) = Action.async { implicit request: Request[AnyContent] =>
    memberRepo.delete(id).map {
      case 1 => Ok(Json.obj("message" -> "success"))
      case _ => BadRequest(Json.obj("message" -> "error", "error" -> "Failed to delete member"))
    }.recover {
      case e: Exception => BadRequest(Json.obj("message" -> "error", "error" -> e.getMessage))
    }
  }

}
---------------------
package controllers

import javax.inject._
import play.api._
import play.api.mvc._
import play.api.libs.json._
import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global
import java.util.UUID

import models.domain._
import models.repo._

@Singleton
class ChecklistController @Inject()(val controllerComponents: ControllerComponents, val checklistRepo: ChecklistRepo) extends BaseController {
  
  def addChecklist = Action.async(parse.json) { implicit request: Request[JsValue] =>
    request.body.validate[Checklist].fold(
      errors => Future.successful(BadRequest(Json.obj("message" -> "error", "error" -> errors.mkString(",")))),
      checklist => {
        val newChecklist = checklist.copy(idChecklist = UUID.randomUUID())
        checklistRepo.add(newChecklist).map {
          case 1 => Ok(Json.obj("message" -> "success", "data" -> newChecklist))
          case _ => BadRequest(Json.obj("message" -> "error", "error" -> "Failed to add checklist"))
        }
      }
    )
  }

  def getChecklist(id: UUID) = Action.async { implicit request: Request[AnyContent] =>
    checklistRepo.get(id).map {
      case Some(checklist) => Ok(Json.toJson(checklist))
      case None => NotFound(Json.obj("message" -> "Checklist not found"))
    }
  }

  def listChecklists = Action.async { implicit request: Request[AnyContent] =>
    checklistRepo.list().map { checklists =>
      Ok(Json.toJson(checklists))
    }
  }

  def updateChecklist(id: UUID) = Action.async(parse.json) { implicit request: Request[JsValue] =>
    request.body.validate[Checklist].fold(
      errors => Future.successful(BadRequest(Json.obj("message" -> "error", "error" -> errors.mkString(",")))),
      checklist => {
        print("checklist", checklist)
        checklistRepo.update(checklist, id).map {
          case 1 => Ok(Json.obj("message" -> "success", "data" -> checklist))
          case _ => BadRequest(Json.obj("message" -> "error", "error" -> "Failed to update checklist"))
        }
      }
    )
  }

  def deleteChecklist(id: UUID) = Action.async { implicit request: Request[AnyContent] =>
    checklistRepo.delete(id).map {
      case 1 => Ok(Json.obj("message" -> "success"))
      case _ => BadRequest(Json.obj("message" -> "error", "error" -> "Failed to delete checklist"))
    }
  }

 /* def getChecklistsByMemberId(memberId: Long) = Action.async { implicit request: Request[AnyContent] =>
    checklistRepo.findByMemberId(memberId).map { checklists =>
      Ok(Json.toJson(checklists))
    }
  }*/
}
-------------
package controllers
import javax.inject._
import play.api._
import play.api.mvc._
import play.api.libs.json._
import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global
import java.util.UUID
import models.domain._
import models.repo._
@Singleton
class ChecklistItemController @Inject()(val controllerComponents: ControllerComponents, val checklistItemRepo: ChecklistItemRepo) extends BaseController {
  
  def addChecklistItem = Action.async(parse.json) { implicit request: Request[JsValue] =>
    request.body.validate[ChecklistItem].fold(
      errors => Future.successful(BadRequest(Json.obj("message" -> "error", "error" -> errors.mkString(",")))),
      checklistItem => {
        val newChecklistItem = checklistItem.copy(idChecklistItem = UUID.randomUUID())
        checklistItemRepo.add(newChecklistItem).map {
          case 1 => Ok(Json.obj("message" -> "success", "data" -> newChecklistItem))
          case _ => BadRequest(Json.obj("message" -> "error", "error" -> "Failed to add Checklist Item"))
        }.recover {
          case e: Exception => BadRequest(Json.obj("message" -> "error", "error" -> e.getMessage))
        }
      }
    )
  }

  def getChecklistItem(id: UUID) = Action.async { implicit request: Request[AnyContent] =>
    checklistItemRepo.get(id).map {
      case Some(checklistItem) => Ok(Json.toJson(checklistItem))
      case None => NotFound(Json.obj("message" -> "error", "error" -> "ChecklistItem not found"))
    }.recover {
      case e: Exception => BadRequest(Json.obj("message" -> "error", "error" -> e.getMessage))
    }
  }

  def listChecklistItem = Action.async { implicit request: Request[AnyContent] =>
    checklistItemRepo.list().map { checklistItems =>
      Ok(Json.toJson(checklistItems))
    }.recover {
      case e: Exception => BadRequest(Json.obj("message" -> "error", "error" -> e.getMessage))
    }
  }

  def updateChecklistItem(id: UUID) = Action.async(parse.json) { implicit request: Request[JsValue] =>
    request.body.validate[ChecklistItem].fold(
      errors => Future.successful(BadRequest(Json.obj("message" -> "error", "error" -> errors.mkString(",")))),
      checklistItem => {
        checklistItemRepo.update(checklistItem, id).map {
          case 1 => Ok(Json.obj("message" -> "success", "data" -> checklistItem))
          case _ => BadRequest(Json.obj("message" -> "error", "error" -> "Failed to update Checklist Item"))
        }.recover {
          case e: Exception => BadRequest(Json.obj("message" -> "error", "error" -> e.getMessage))
        }
      }
    )
  }

  def deleteChecklistItem(id: UUID) = Action.async { implicit request: Request[AnyContent] =>
    checklistItemRepo.delete(id).map {
      case 1 => Ok(Json.obj("message" -> "success"))
      case _ => BadRequest(Json.obj("message" -> "error", "error" -> "Failed to delete Checklist Item"))
    }.recover {
      case e: Exception => BadRequest(Json.obj("message" -> "error", "error" -> e.getMessage))
    }
  }

  def getChecklistItemByChecklistsId(checklistsId: UUID) = Action.async { implicit request: Request[AnyContent] =>
    checklistItemRepo.findByChecklistsId(checklistsId).map { checklistItems =>
      Ok(Json.toJson(checklistItems))
    }.recover {
      case e: Exception => BadRequest(Json.obj("message" -> "error", "error" -> e.getMessage))
    }
  }

  def getChecklistItemByParentId(parentId: UUID) = Action.async { implicit request: Request[AnyContent] =>
    checklistItemRepo.findByParentId(parentId).map { checklistItems =>
      Ok(Json.toJson(checklistItems))
    }.recover {
      case e: Exception => BadRequest(Json.obj("message" -> "error", "error" -> e.getMessage))
    }
  }
}
------------------
package models.domain

import java.util.UUID
import play.api.libs.json._
import play.api.libs.functional.syntax._

case class Checklist(
  idChecklist: UUID,
  nameChecklist: String,
  isCheckedChecklist: Boolean
)

object Checklist {
  def create(nameChecklist: String, isCheckedChecklist: Boolean): Checklist = Checklist(UUID.randomUUID(), nameChecklist, isCheckedChecklist)

  implicit val checklistReads: Reads[Checklist] = (
    (JsPath \ "nameChecklist").read[String] and
    (JsPath \ "isCheckedChecklist").read[Boolean]
  )(Checklist.create _)

  implicit val checklistWrites: Writes[Checklist] = new Writes[Checklist] {
    def writes(checklist: Checklist): JsValue = Json.obj(
      "idChecklist" -> checklist.idChecklist,
      "nameChecklist" -> checklist.nameChecklist,
      "isCheckedChecklist" -> checklist.isCheckedChecklist
    )
  }
}
------------------
package models.domain
import java.util.UUID
import play.api.libs.json._
import play.api.libs.functional.syntax._
case class ChecklistItem(
  idChecklistItem: UUID,
  checklistsIdChecklistItem: UUID,
  parentIdChecklistItem: Option[UUID],
  descriptionChecklistItem: String,
  isCheckedChecklistItem: Boolean
)
object ChecklistItem{
  def create(checklistsIdChecklistItem: UUID, parentIdChecklistItem: Option[UUID], descriptionChecklistItem: String, isCheckedChecklistItem: Boolean): ChecklistItem = ChecklistItem(UUID.randomUUID(), checklistsIdChecklistItem, parentIdChecklistItem, descriptionChecklistItem, isCheckedChecklistItem)

  implicit val checklistItemReads: Reads[ChecklistItem] = (
    (JsPath \ "checklistsIdChecklistItem").read[UUID] and
    (JsPath \ "parentIdChecklistItem").readNullable[UUID] and
    (JsPath \ "descriptionChecklistItem").read[String] and
    (JsPath \ "isCheckedChecklistItem").read[Boolean]
  )(ChecklistItem.create _)

  implicit val checklistItemWrites: Writes[ChecklistItem] = new Writes[ChecklistItem] {
    def writes(checklistItem: ChecklistItem): JsValue = Json.obj(
      "idChecklistItem" -> checklistItem.idChecklistItem,
      "checklistsIdChecklistItem" -> checklistItem.checklistsIdChecklistItem,
      "parentIdChecklistItem" -> checklistItem.parentIdChecklistItem,
      "descriptionChecklistItem" -> checklistItem.descriptionChecklistItem,
      "isCheckedChecklistItem" -> checklistItem.isCheckedChecklistItem
    )
  }
}
--------------
package models.domain

import java.util.UUID
import play.api.libs.json._
import play.api.libs.functional.syntax._

case class User(
  idUser: UUID,
  emailUser: String,
  passwordUser: String
)

object User{
  def create(emailUser: String, passwordUser: String): User =
    User(UUID.randomUUID(), emailUser, passwordUser)

  implicit val userReads: Reads[User] = (
    (JsPath \ "emailUser").read[String] and
    (JsPath \ "passwordUser").read[String]
  )(User.create _)

  implicit val userWrites: Writes[User] = new Writes[User] {
    def writes(user: User): JsValue = Json.obj(
      "idUser" -> user.idUser,
      "emailUser" -> user.emailUser,
      "passwordUser" -> user.passwordUser
    )
  }
}
-----------------
package models.domain

import java.util.UUID
import play.api.libs.json._
import play.api.libs.functional.syntax._

case class Member(
  idMember: Long,
  userIdMember: UUID
)

object Member{
  implicit val memberFormat: Format[Member] = Json.format[Member]
}
--------------
