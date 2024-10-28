package com.example.vitesse.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

/**
 * Data class representing a candidate in the database
 *
 * @param id the id of the candidate. Generated automatically
 * @param firstName the firstname of the candidate
 * @param surName the surname of the candidate
 * @param phoneNumbers the phone numbers of the candidate
 * @param email the email of the candidate
 * @param dateOfBirth the date of birth of the candidate
 * @param expectedSalaryEuros the expected salary in euros of the candidate
 * @param note the note regarding the candidate
 * @param profilePicture the profile picture of the candidate
 */
@Entity(tableName = "Candidate")
data class CandidateDto(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Long = 0,

    @ColumnInfo(name = "FirstName")
    var firstName: String,

    @ColumnInfo(name = "SurName")
    var surName: String,

    @ColumnInfo(name = "PhoneNumbers")
    var phoneNumbers: Long,

    @ColumnInfo(name = "Email")
    var email: String,

    @ColumnInfo(name = "DateOfBirth")
    var dateOfBirth: LocalDateTime,

    @ColumnInfo(name = "ExpectedSalaryEuros")
    var expectedSalaryEuros: Int,

    @ColumnInfo(name = "Note")
    var note: String,

    @ColumnInfo(name = "ProfilePicture")
    var profilePicture: String

    )