package com.maranatha.skools.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table

object ClassesTable : Table("classes") {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 50)
    val code = varchar("code", 20)

    override val primaryKey = PrimaryKey(id)
}

object StreamsTable : Table("streams") {
    val id = integer("id").autoIncrement()
    val classId = integer("class_id").references(ClassesTable.id)
    val name = varchar("name", 50)

    override val primaryKey = PrimaryKey(id)
}

object StudentsTable : Table("students") {
    val id = integer("id").autoIncrement()
    val admissionNumber = varchar("admission_number", 50)
    val firstName = varchar("first_name", 100)
    val lastName = varchar("last_name", 100)
    val gender = varchar("gender", 10)
    val dateOfBirth = varchar("date_of_birth", 10).nullable()
    val classId = integer("class_id").references(ClassesTable.id)
    val streamId = integer("stream_id").references(StreamsTable.id).nullable()
    val status = varchar("status", 20).default("ACTIVE")

    override val primaryKey = PrimaryKey(id)
}

@Serializable
data class SchoolClass(val id: Int, val name: String, val code: String)

@Serializable
data class CreateClassRequest(val name: String, val code: String)

@Serializable
data class ClassStream(val id: Int, val classId: Int, val name: String)

@Serializable
data class CreateStreamRequest(val classId: Int, val name: String)

@Serializable
data class Student(
    val id: Int,
    val admissionNumber: String,
    val firstName: String,
    val lastName: String,
    val gender: String,
    val dateOfBirth: String? = null,
    val classId: Int,
    val streamId: Int? = null,
    val status: String = "ACTIVE"
)

@Serializable
data class RegisterStudentRequest(
    val admissionNumber: String,
    val firstName: String,
    val lastName: String,
    val gender: String,
    val dateOfBirth: String? = null,
    val classId: Int,
    val streamId: Int? = null
)