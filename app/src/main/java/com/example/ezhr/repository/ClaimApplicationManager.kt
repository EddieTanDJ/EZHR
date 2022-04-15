package com.example.ezhr.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.ezhr.data.Claim
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("claimPrefs")

data class Draft(
    val claimType: String,
    val title: String,
    val amount: Double,
    val desc: String,
    val uploadedImg: String
)

class ClaimApplicationManager(val context: Context) {
    companion object {
        val CLAIM_TYPE = stringPreferencesKey("CLAIM_TYPE")
        val TITLE = stringPreferencesKey("TITLE")
        val AMOUNT = doublePreferencesKey("AMOUNT")
        val DESC = stringPreferencesKey("DESC")
        val UPLOADED_IMG = stringPreferencesKey("UPLOADED_IMG")
    }

    suspend fun savetoDataStore(claim: Draft) {
        context.dataStore.edit {
            it[CLAIM_TYPE] = claim.claimType
            it[TITLE] = claim.title
            it[AMOUNT] = claim.amount
            it[DESC] = claim.desc
            it[UPLOADED_IMG] = claim.uploadedImg
        }
    }

    suspend fun getFromDataStore() = context.dataStore.data.map {
        Claim(
            claimType = it[CLAIM_TYPE] ?: "",
            title = it[TITLE] ?: "",
            amount = it[AMOUNT] ?: 0.0,
            desc = it[DESC] ?: "",
            uploadedImg = it[UPLOADED_IMG] ?: ""
        )
    }

    suspend fun clearDataStore() {
        context.dataStore.edit {
            it[CLAIM_TYPE] = "Medical"
            it[TITLE] = ""
            it[AMOUNT] = 0.0
            it[DESC] = ""
            it[UPLOADED_IMG] = ""
        }
    }
}






