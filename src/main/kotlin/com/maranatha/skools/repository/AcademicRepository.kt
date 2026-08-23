package com.maranatha.skools.repository

import com.maranatha.skools.db.DatabaseFactory.dbQuery
import com.maranatha.skools.models.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class AcademicRepository {

    suspend fun createClass(req: CreateClassRequest): SchoolClass = dbQuery {
        val id = ClassesTable.insert {
            it[name] = req.name
            it[code] = req.code
        } get ClassesTable.id

        SchoolClass(id, req.name, req.code)
    }

    suspend fun getAllClasses(): List<SchoolClass> = dbQuery {
        ClassesTable.selectAll().map {
            SchoolClass(it[ClassesTable.id], it[ClassesTable.name], it[ClassesTable.code])
        }
    }

    suspend fun createStream(req: CreateStreamRequest): ClassStream = dbQuery {
        val id = StreamsTable.insert {
            it[classId] = req.classId
            it[name] = req.name
        } get StreamsTable.id

        ClassStream(id, req.classId, req.name)
    }

    suspend fun getStreamsByClass(classId: Int): List<ClassStream> = dbQuery {
        StreamsTable.selectAll().where { StreamsTable.classId eq classId }.map {
            ClassStream(it[StreamsTable.id], it[StreamsTable.classId], it[StreamsTable.name])
        }
    }

    suspend fun registerStudent(req: RegisterStudentRequest): Student = dbQuery {
        val id = StudentsTable.insert {
            it[admissionNumber] = req.admissionNumber
            it[firstName] = req.firstName
            it[lastName] = req.lastName
            it[gender] = req.gender
            it[dateOfBirth] = req.dateOfBirth
            it[classId] = req.classId
            it[streamId] = req.streamId
        } get StudentsTable.id

        Student(
            id = id,
            admissionNumber = req.admissionNumber,
            firstName = req.firstName,
            lastName = req.lastName,
            gender = req.gender,
            dateOfBirth = req.dateOfBirth,
            classId = req.classId,
            streamId = req.streamId
        )
    }

    suspend fun getAllStudents(): List<Student> = dbQuery {
        StudentsTable.selectAll().map {
            Student(
                id = it[StudentsTable.id],
                admissionNumber = it[StudentsTable.admissionNumber],
                firstName = it[StudentsTable.firstName],
                lastName = it[StudentsTable.lastName],
                gender = it[StudentsTable.gender],
                dateOfBirth = it[StudentsTable.dateOfBirth],
                classId = it[StudentsTable.classId],
                streamId = it[StudentsTable.streamId],
                status = it[StudentsTable.status]
            )
        }
    }

    suspend fun getStudentsByClass(classId: Int): List<Student> = dbQuery {
        StudentsTable.selectAll().where { StudentsTable.classId eq classId }.map {
            Student(
                id = it[StudentsTable.id],
                admissionNumber = it[StudentsTable.admissionNumber],
                firstName = it[StudentsTable.firstName],
                lastName = it[StudentsTable.lastName],
                gender = it[StudentsTable.gender],
                dateOfBirth = it[StudentsTable.dateOfBirth],
                classId = it[StudentsTable.classId],
                streamId = it[StudentsTable.streamId],
                status = it[StudentsTable.status]
            )
        }
    }
}