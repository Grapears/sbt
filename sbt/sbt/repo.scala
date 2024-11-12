package models.repo

import java.util.UUID
import javax.inject._
import slick.jdbc.PostgresProfile
import play.api.db.slick._
import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global

import models.domain._

@Singleton
class ChecklistItemRepo @Inject()(val dbConfigProvider: DatabaseConfigProvider, val checkListRepo: ChecklistRepo){
  val dbConfig = dbConfigProvider.get[PostgresProfile]
  import dbConfig._
  import profile.api._

  class ChecklistItemTable(tag: Tag) extends Table[ChecklistItem](tag, "CHECKLIST_ITEMS"){
    def idChecklistItem = column[UUID]("ID_CHECKLIST_ITEMS", O.PrimaryKey)
    def checklistsIdChecklistItem = column[UUID]("CHECKLISTS_ID_CHECKLIST_ITEMS")
    def parentIdChecklistItem = column[Option[UUID]]("PARENT_ID_CHECKLIST_ITEMS")
    def descriptionChecklistItem = column[String]("DESCRIPTION_CHECKLIST_ITEMS")
    def isCheckedChecklistItem = column[Boolean]("IS_CHECKED_CHECKLIST_ITEMS")

    def * = (idChecklistItem, checklistsIdChecklistItem, parentIdChecklistItem, descriptionChecklistItem, isCheckedChecklistItem).mapTo[ChecklistItem]
    
    def checklists = foreignKey("CHECKLIST_ITEMS_FK", checklistsIdChecklistItem, checkListRepo.checklists)(_.idChecklist)
  }

  val checklistItems = TableQuery[ChecklistItemTable]

  def add(checklistItem: ChecklistItem): Future[Int] =
    val query = checklistItems += checklistItem
    db.run(query)
  
  def get(id: UUID): Future[Option[ChecklistItem]] =
    val query = checklistItems.filter(_.idChecklistItem === id).result.headOption
    db.run(query)
  
  def list(): Future[Seq[ChecklistItem]] =
    val query = checklistItems.result
    db.run(query)
  
  def update(checklistItem: ChecklistItem, idChecklistItemUUID: UUID): Future[Int] =
    val newCheckListItem = checklistItem.copy(idChecklistItem = idChecklistItemUUID)
    val query = checklistItems.filter(_.idChecklistItem === idChecklistItemUUID).update(newCheckListItem)
    db.run(query)
  
  def delete(id: UUID): Future[Int] =
    val query = checklistItems.filter(_.idChecklistItem === id).delete
    db.run(query)
  
  def findByChecklistsId(checklistsId: UUID): Future[Seq[ChecklistItem]] =
    val query = checklistItems.filter(_.checklistsIdChecklistItem === checklistsId).result
    db.run(query)
  
  def findByParentId(parentId: UUID): Future[Seq[ChecklistItem]] =
    val query = checklistItems.filter(_.parentIdChecklistItem === Some(parentId)).result
    db.run(query)
}
---------------------------
package models.repo

import java.util.UUID
import javax.inject._
import slick.jdbc.PostgresProfile
import play.api.db.slick._
import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global
import models.domain._

@Singleton
class ChecklistRepo @Inject()(val dbConfigProvider: DatabaseConfigProvider, val memberRepo: MemberRepo){
  val dbConfig = dbConfigProvider.get[PostgresProfile]
  import dbConfig._
  import profile.api._

  class ChecklistTable(tag: Tag) extends Table[Checklist](tag, "CHECKLISTS"){
    def idChecklist = column[UUID]("ID_CHECKLISTS", O.PrimaryKey)
    def nameChecklist = column[String]("NAME_CHECKLISTS")
    def isCheckedChecklist = column[Boolean]("IS_CHECKED_CHECKLISTS")

    def * = (idChecklist, nameChecklist, isCheckedChecklist).mapTo[Checklist]
  }

  val checklists = TableQuery[ChecklistTable]

  def add(checklist: Checklist): Future[Int] =
    val query = checklists += checklist
    db.run(query)
  
  def get(id: UUID): Future[Option[Checklist]] =
    val query = checklists.filter(_.idChecklist === id).result.headOption
    db.run(query)
  
  def list(): Future[Seq[Checklist]] =
    val query = checklists.result
    db.run(query)
  
  def update(checklist: Checklist, checklistUUID: UUID): Future[Int] =
    val newCheckList = checklist.copy(idChecklist = checklistUUID)
    val query = checklists.filter(_.idChecklist === checklistUUID).update(newCheckList)
    db.run(query)
  
  def delete(id: UUID): Future[Int] =
    val query = checklists.filter(_.idChecklist === id).delete
    db.run(query)
}
----------------------
package models.repo

import java.util.UUID
import javax.inject._

import slick.jdbc.PostgresProfile
import play.api.db.slick._

import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global

import models.domain._

@Singleton
class MemberRepo @Inject()(val dbConfigProvider: DatabaseConfigProvider, val userRepo: UserRepo){
  val dbConfig = dbConfigProvider.get[PostgresProfile]
  import dbConfig._
  import profile.api._

  class MemberTable(tag: Tag) extends Table[Member](tag, "MEMBERS"){
    def idMember = column[Long]("ID_MEMBERS", O.PrimaryKey, O.AutoInc)
    def userIdMember = column[UUID]("USER_ID_MEMBERS")

    def * = (idMember, userIdMember).mapTo[Member]

    def userIdMemberPk = foreignKey("USER_ID_MEMBERS_PK", userIdMember, userRepo.users)(_.idUser)
  }

  val members = TableQuery[MemberTable]

  def add(member: Member): Future[Int] =
    val query = members += member
    db.run(query)

  def findById(id: Long): Future[Option[Member]] =
    db.run(members.filter(_.idMember === id).result.headOption)

  def findAll(): Future[Seq[Member]] =
    db.run(members.result)
  
  def update(member: Member): Future[Int] =
    val query = members.filter(_.idMember === member.idMember).update(member)
    db.run(query)
  
  def delete(id: Long): Future[Int] =
    val query = members.filter(_.idMember === id).delete
    db.run(query)
  
  def findByUserId(userId: UUID): Future[Option[Member]] =
    db.run(members.filter(_.userIdMember === userId).result.headOption)
}
------------------
package models.repo

import java.util.UUID
import javax.inject._

import slick.jdbc.PostgresProfile
import play.api.db.slick._

import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global

import models.domain._

@Singleton
class UserRepo @Inject()(val dbConfigProvider: DatabaseConfigProvider){
  val dbConfig = dbConfigProvider.get[PostgresProfile]
  import dbConfig._
  import profile.api._

  class UserTable(tag: Tag) extends Table[User](tag, "USERS"){
    def idUser = column[UUID]("ID_USERS", O.PrimaryKey)
    def emailUser = column[String]("EMAIL_USERS")
    def passwordUser = column[String]("PASSWORD_USERS")

    def * = (idUser, emailUser, passwordUser).mapTo[User]
  }

  val users = TableQuery[UserTable]

  def add(user: User): Future[Int] =
    val query = users += user
    db.run(query)
  
  def get(id: UUID): Future[Option[User]] =
    val query = users.filter(_.idUser === id).result.headOption
    db.run(query)
  
  def list(): Future[Seq[User]] =
    val query = users.result
    db.run(query)
  
  def update(user: User): Future[Int] =
    val query = users.filter(_.idUser === user.idUser).update(user)
    db.run(query)
  
  def delete(id: UUID): Future[Int] =
    val query = users.filter(_.idUser === id).delete
    db.run(query)
  
  def findByEmail(email: String): Future[Option[User]] =
    val query = users.filter(_.emailUser === email).result.headOption
    db.run(query)
}
-----------------