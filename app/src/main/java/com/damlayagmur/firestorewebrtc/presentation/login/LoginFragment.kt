package com.damlayagmur.firestorewebrtc.presentation.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.damlayagmur.firestorewebrtc.databinding.FragmentLoginBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val loginViewModel: LoginViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launch {

            binding.imgLogo.apply {
                alpha = 0f
                animate()
                    .alpha(1f)
                    .setDuration(1500)
                    .setListener(null)
            }

            delay(2000)

            binding.tvLogin.apply {
                alpha = 1f
                visibility = View.VISIBLE
                animate()
                    .alpha(0f)
                    .setDuration(500)
                    .setListener(null)
            }

            binding.btnLogin.apply {
                alpha = 0f
                visibility = View.VISIBLE
                animate()
                    .alpha(1f)
                    .setDuration(500)
                    .setListener(null)
                setOnClickListener {
                    //TODO Firebase Auth
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}