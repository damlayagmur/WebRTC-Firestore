package com.damlayagmur.firestorewebrtc.data.repository

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.damlayagmur.firestorewebrtc.common.SignalingClient
import com.damlayagmur.firestorewebrtc.common.WebRtcClient
import com.damlayagmur.firestorewebrtc.common.webrtc.RTCAudioManager
import com.damlayagmur.firestorewebrtc.common.webrtc.listener.SignalingListener
import com.damlayagmur.firestorewebrtc.common.webrtc.util.PeerConnectionUtil
import com.damlayagmur.firestorewebrtc.domain.CallRepository
import org.webrtc.EglBase
import org.webrtc.IceCandidate
import org.webrtc.PeerConnection
import org.webrtc.SessionDescription
import javax.inject.Inject

class CallRepositoryImpl @Inject constructor(
    private val context: Context,
    private val eglBase: EglBase
) : CallRepository {

    private val webRtcClient = WebRtcClient
    private lateinit var signalingClient: SignalingClient
    private lateinit var peerConnectionUtil: PeerConnectionUtil

    private val audioManager by lazy { RTCAudioManager.create(context) }

    override fun initClasses(
        meetingID: String,
        peerConnectionObserver: PeerConnection.Observer,
        signalingListener: SignalingListener
    ) {
        peerConnectionUtil = PeerConnectionUtil(
            context,
            eglBase.eglBaseContext
        )

        initWebRtcClient(peerConnectionObserver)
        initSignalingClient(meetingID, signalingListener)
        audioManager.setDefaultAudioDevice(RTCAudioManager.AudioDevice.SPEAKER_PHONE)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun addAndSendIcaCandidate(iceCandidate: IceCandidate, isJoin: Boolean) {
        signalingClient.sendIceCandidate(iceCandidate, isJoin)
        webRtcClient.addIceCandidate(iceCandidate)
    }

    override fun switchCamera() = webRtcClient.switchCamera(context)

    override fun setAudioState(isMute: Boolean) = webRtcClient.enableAudio(isMute)

    override fun setVideoState(isVideoPaused: Boolean) = webRtcClient.enableVideo(isVideoPaused)

    override fun startCall(meetingID: String) = webRtcClient.call(meetingID)

    override fun endCall(meetingID: String) = webRtcClient.endCall(meetingID)

    override fun destroy(meetingID: String) {
        //webRtcClient.clearCandidates(meetingID)
        webRtcClient.closePeerConnection()
        signalingClient.destroy()
    }

    override fun offerReceived(meetingID: String, sessionDescription: SessionDescription) {
        webRtcClient.setRemoteDescription(sessionDescription)
        webRtcClient.answer(meetingID)
    }

    override fun answerReceived(sessionDescription: SessionDescription) {
        webRtcClient.setRemoteDescription(sessionDescription)
    }

    override fun iceCandidateReceived(iceCandidate: IceCandidate) {
        webRtcClient.addIceCandidate(iceCandidate)
    }

    override fun clearSdp(meetingID: String) {
        //webRtcClient.clearSdp(meetingID)
    }

    private fun initWebRtcClient(peerConnectionObserver: PeerConnection.Observer) {
        webRtcClient.initWebRtcClient(
            context = context,
            eglBase = eglBase,
            peerConnectionObserver = peerConnectionObserver
        )
    }

    private fun initSignalingClient(meetingID: String, signalingListener: SignalingListener) {
        signalingClient = SignalingClient(
            meetingID = meetingID,
            listener = signalingListener
        )
    }
}