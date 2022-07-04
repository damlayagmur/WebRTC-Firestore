package com.damlayagmur.firestorewebrtc.data.model

data class Sdp(
    val meetingID: String,
    val sdp: String,
    val callType: String
)
