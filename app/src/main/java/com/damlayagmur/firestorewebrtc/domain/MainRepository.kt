package com.damlayagmur.firestorewebrtc.domain

interface MainRepository {
    fun checkMeetingId(meetingID: String, hasMeetingId: (Boolean) -> Unit)
}