package com.damlayagmur.firestorewebrtc.data.repository

import android.content.Context
import com.damlayagmur.firestorewebrtc.domain.MainRepository
import javax.inject.Inject

class MainRepositoryImpl @Inject constructor(private val context: Context) : MainRepository {

    override fun checkMeetingId(meetingID: String, hasMeetingId: (Boolean) -> Unit) {
        TODO("Not yet implemented")
    }

    /*companion object {
        private const val CHECK_MEETING_ID = "CHECK MEETING ID"
    }*/
}