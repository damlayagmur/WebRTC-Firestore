package com.damlayagmur.firestorewebrtc.presentation.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.damlayagmur.firestorewebrtc.R
import com.damlayagmur.firestorewebrtc.common.Constants.IS_JOIN
import com.damlayagmur.firestorewebrtc.common.Constants.MEETING_ID
import com.damlayagmur.firestorewebrtc.databinding.FragmentMainBinding
import com.damlayagmur.firestorewebrtc.presentation.call.CallActivity

class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {

            imgGenerateId.setOnClickListener {
                binding.etMeetingId.setText(mainViewModel.generateMeetingId())
            }

            btnJoin.setOnClickListener {
                mainViewModel.checkMeetingId(etMeetingId.text.toString()) { hasMeetingId ->
                    if (hasMeetingId) {
                        intentToCall(true)
                    } else {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.no_room_message),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            btnCreate.setOnClickListener { intentToCall(false) }
        }
    }

    private fun intentToCall(isJoin: Boolean) {
        val meetingId = binding.etMeetingId.text.toString()
        val intent = Intent(requireActivity(), CallActivity::class.java)
        intent.putExtra(MEETING_ID, meetingId)
        intent.putExtra(IS_JOIN, isJoin)
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}