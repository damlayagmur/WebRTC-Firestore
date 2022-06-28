package com.damlayagmur.firestorewebrtc.data.model

import org.w3c.dom.Text

data class Sdp(
    val meetingID: String,
    val sdp: String,
    val callType: String
)
