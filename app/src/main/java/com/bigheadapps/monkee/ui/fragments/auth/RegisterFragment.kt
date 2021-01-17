package com.bigheadapps.monkee.ui.fragments.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.vvalidator.form
import com.bigheadapps.monkee.R
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.auth_page_heading.*
import kotlinx.android.synthetic.main.register_page.*

class RegisterFragment : Fragment() {

    private val mAuth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_register, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
    }

    private fun setupUI() {
        auth_page_heading_text?.setText(R.string.register_fragment_heading)
        close_button?.setOnClickListener {
            this.requireActivity().finish()
        }

        login_link?.setOnClickListener {
            val toLoginAction = RegisterFragmentDirections.actionRegisterFragmentToLoginFragment()
            this.findNavController().navigate(toLoginAction)
        }

        form {
            input(R.id.register_email_input) {
                isEmail()
            }

            input(R.id.register_password_input) {
                isNotEmpty()
                length().greaterThan(5)
            }

            input(R.id.register_confirm_password_input) {
                isNotEmpty()
            }

            submitWith(R.id.register_button) {
                if (register_confirm_password_input?.text.toString() == register_password_input?.text.toString()) {
                    register(
                        register_email_input?.text.toString(),
                        register_password_input?.text.toString()
                    )
                } else {
                    register_confirm_password_input?.error = "Passwords don't match"
                }
            }
        }
    }

    private fun register(email: String, password: String) {
        this.requireActivity().findViewById<ProgressBar>(R.id.auth_progress_bar)?.visibility =
            View.VISIBLE
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnFailureListener {
                this.requireActivity()
                    .findViewById<ProgressBar>(R.id.auth_progress_bar)?.visibility = View.GONE

                MaterialDialog(requireContext()).show {
                    title(R.string.oops)
                    message(text = it.message.toString())
                    positiveButton(R.string.okay)
                }
            }.addOnSuccessListener {
                this.requireActivity()
                    .findViewById<ProgressBar>(R.id.auth_progress_bar)?.visibility = View.GONE
                this.requireActivity().finish()
            }
    }
}