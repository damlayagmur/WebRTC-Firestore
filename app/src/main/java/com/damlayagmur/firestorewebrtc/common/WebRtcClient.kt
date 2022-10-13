package com.damlayagmur.firestorewebrtc.common

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.damlayagmur.firestorewebrtc.common.Constants.LOCAL_STREAM_ID
import com.damlayagmur.firestorewebrtc.common.Constants.LOCAL_TRACK_ID
import com.damlayagmur.firestorewebrtc.common.Constants.VIDEO_FPS
import com.damlayagmur.firestorewebrtc.common.Constants.VIDEO_HEIGHT
import com.damlayagmur.firestorewebrtc.common.Constants.VIDEO_WIDTH
import com.damlayagmur.firestorewebrtc.common.webrtc.observer.SdpObserverImpl
import com.damlayagmur.firestorewebrtc.common.webrtc.util.PeerConnectionUtil
import com.damlayagmur.firestorewebrtc.data.model.Sdp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.webrtc.*

@SuppressLint("StaticFieldLeak")
object WebRtcClient {

    private val db = Firebase.firestore

    private var localAudioTrack: AudioTrack? = null
    private var localVideoTrack: VideoTrack? = null

    private var peerConnectionUtil: PeerConnectionUtil? = null

    private var peerConnectionFactory: PeerConnectionFactory? = null

    private var mediaConstraints = MediaConstraints().apply {
        mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
        mandatory.add(MediaConstraints.KeyValuePair("RtpDataChannels", "true"))
        mandatory.add(MediaConstraints.KeyValuePair("DtlsSrtpkeyAgreement", "true"))
        mandatory.add(MediaConstraints.KeyValuePair("internalSctpDataChannels", "true"))
    }

    private var localAudioSource: AudioSource? = null
    private var localVideoSource: VideoSource? = null
    private var peerConnection: PeerConnection? = null

    private var videoCapturer: CameraVideoCapturer? = null

    private var isFrontCamera: Boolean = false

    private var eglBase: EglBase? = null

    fun initWebRtcClient(
        context: Context,
        eglBase: EglBase,
        peerConnectionObserver: PeerConnection.Observer
    ) {
        //cloudDBZone = CloudDbWrapper.cloudDBZone
        this.eglBase = eglBase

        peerConnectionUtil = PeerConnectionUtil(
            context,
            eglBase.eglBaseContext
        )

        peerConnectionFactory = peerConnectionUtil?.peerConnectionFactory

        mediaConstraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("RtpDataChannels", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("DtlsSrtpkeyAgreement", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("internalSctpDataChannels", "true"))
        }

        localAudioSource = peerConnectionFactory?.createAudioSource(MediaConstraints())
        localVideoSource = peerConnectionFactory?.createVideoSource(false)
        peerConnection = buildPeerConnection(peerConnectionObserver)

        videoCapturer = getFrontCameraCapturer(context)

        isFrontCamera = true
    }

    private fun buildPeerConnection(observer: PeerConnection.Observer) =
        peerConnectionFactory?.createPeerConnection(
            peerConnectionUtil?.iceServer,
            observer
        )

    private fun initSurfaceView(view: SurfaceViewRenderer) = view.run {
        setMirror(false)
        setEnableHardwareScaler(true)
        init(eglBase?.eglBaseContext, null)
    }

    fun initSurfaces(local: SurfaceViewRenderer, remote: SurfaceViewRenderer) {
        initSurfaceView(local)
        initSurfaceView(remote)
    }

    private fun getFrontCameraCapturer(context: Context) = Camera2Enumerator(context).run {
        deviceNames.find {
            isFrontFacing(it)
        }?.let {
            createCapturer(it, null)
        } ?: throw IllegalStateException()
    }

    private fun getBackCameraCapturer(context: Context) = Camera2Enumerator(context).run {
        deviceNames.find {
            isBackFacing(it)
        }?.let {
            createCapturer(it, null)
        } ?: throw IllegalStateException()
    }

    fun startLocalVideoCapture(localSurfaceView: SurfaceViewRenderer) {
        val surfaceTextureHelper =
            SurfaceTextureHelper.create(Thread.currentThread().name, eglBase?.eglBaseContext)
        (videoCapturer as VideoCapturer).initialize(
            surfaceTextureHelper,
            localSurfaceView.context,
            localVideoSource?.capturerObserver
        )
        videoCapturer?.startCapture(VIDEO_HEIGHT, VIDEO_WIDTH, VIDEO_FPS)

        localAudioTrack =
            peerConnectionFactory?.createAudioTrack(LOCAL_TRACK_ID + "_audio", localAudioSource)
        localVideoTrack =
            peerConnectionFactory?.createVideoTrack(LOCAL_TRACK_ID + "_video", localVideoSource)

        localVideoTrack?.addSink(localSurfaceView)

        val localStream = peerConnectionFactory?.createLocalMediaStream(LOCAL_STREAM_ID)
        localStream?.addTrack(localVideoTrack)
        localStream?.addTrack(localAudioTrack)

        peerConnection?.addStream(localStream)
    }

    fun call(meetingID: String) {
        peerConnection?.createOffer(
            SdpObserverImpl(
                onCreateSuccessCallback = { sdp ->
                    Log.d(TAG, "contacts: onCreateSuccessCallback called")
                    peerConnection?.setLocalDescription(SdpObserverImpl(
                        onSetSuccessCallback = {
                            Log.d(TAG, "contacts: onSetSuccess called")

                            val offerSdp =
                                Sdp(meetingID, sdp.description, sdp.type.name)

                            db.collection("calls").document(meetingID).set(offerSdp)
                                .addOnSuccessListener {
                                    Log.i(TAG, "Calls Sdp Upsert success: $it")
                                }.addOnFailureListener {
                                    Log.i(TAG, "Calls Sdp Upsert failed: ${it.message}")
                                }
                        }
                    ), sdp)
                }
            ), mediaConstraints
        )
    }

    fun answer(meetingID: String) {
        Log.d(TAG, "answer: called")

        peerConnection?.createAnswer(
            SdpObserverImpl(
                onCreateSuccessCallback = { sdp ->
                    Log.d(TAG, "answer: onCreateSuccessCallback called")

                    val answerSdp = Sdp(meetingID, sdp.description, sdp.type.name)

                    db.collection("calls").document(meetingID).set(answerSdp)
                        .addOnSuccessListener {
                            Log.i(TAG, "Calls Sdp Upsert success: $it")
                        }.addOnFailureListener {
                            Log.i(TAG, "Calls Sdp Upsert failed: ${it.message}")
                        }

                    peerConnection?.setLocalDescription(SdpObserverImpl(
                        onSetSuccessCallback = {
                            Log.d(TAG, "answer: onSetSuccessCallback called")
                        }
                    ), sdp)
                }
            ), mediaConstraints
        )
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun endCall(meetingID: String) {
        db.collection("calls").document(meetingID).collection("candidates")
            .get().addOnSuccessListener {
                val iceCandidateArray: MutableList<IceCandidate> = mutableListOf()
                for (dataSnapshot in it) {
                    if (dataSnapshot.contains("type") && dataSnapshot["type"] == "offerCandidate") {
                        val offerCandidate = dataSnapshot
                        iceCandidateArray.add(
                            IceCandidate(
                                offerCandidate["sdpMid"].toString(),
                                Math.toIntExact(offerCandidate["sdpMLineIndex"] as Long),
                                offerCandidate["sdp"].toString()
                            )
                        )
                    } else if (dataSnapshot.contains("type") && dataSnapshot["type"] == "answerCandidate") {
                        val answerCandidate = dataSnapshot
                        iceCandidateArray.add(
                            IceCandidate(
                                answerCandidate["sdpMid"].toString(),
                                Math.toIntExact(answerCandidate["sdpMLineIndex"] as Long),
                                answerCandidate["sdp"].toString()
                            )
                        )
                    }
                }
                peerConnection?.removeIceCandidates(iceCandidateArray.toTypedArray())
            }
        val endCall = hashMapOf(
            "type" to "END_CALL"
        )
        db.collection("calls").document(meetingID)
            .set(endCall)
            .addOnSuccessListener {
                Log.e(TAG, "DocumentSnapshot added")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error adding document", e)
            }

        peerConnection?.close()
    }


    fun setRemoteDescription(sessionDescription: SessionDescription) =
        peerConnection?.setRemoteDescription(SdpObserverImpl(), sessionDescription)

    fun addIceCandidate(iceCandidate: IceCandidate) = peerConnection?.addIceCandidate(iceCandidate)

    fun closePeerConnection() = peerConnection?.close()

    fun enableVideo(isVideoEnabled: Boolean) {
        localVideoTrack?.setEnabled(isVideoEnabled)
    }

    fun enableAudio(isAudioEnable: Boolean) {
        localAudioTrack?.setEnabled(isAudioEnable)
    }

    fun switchCamera(context: Context) {
        isFrontCamera = !isFrontCamera
        videoCapturer?.stopCapture()
        videoCapturer = if (isFrontCamera) getFrontCameraCapturer(context)
        else getBackCameraCapturer(context)
    }

    private const val TAG = "WebRtcClient"
}