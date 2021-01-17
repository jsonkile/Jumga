package com.bigheadapps.monkee.ui.fragments.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.vvalidator.form
import com.bigheadapps.monkee.R
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.auth_page_heading.*
import kotlinx.android.synthetic.main.login_page.*

class LoginFragment : Fragment() {

    private val mAuth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
    }

    private fun setupUI() {
        auth_page_heading_text?.setText(R.string.login_fragment_heading)
        close_button?.setOnClickListener {
            this.requireActivity().finish()
        }

        register_link?.setOnClickListener {
            val toRegisterAction = LoginFragmentDirections.actionLoginFragmentToRegisterFragment()
            this.findNavController().navigate(toRegisterAction)
        }

        form {
            input(R.id.login_email_input) {
                isEmail()
            }

            input(R.id.login_password_input) {
                isNotEmpty()
            }

            submitWith(R.id.login_button) {
                login(login_email_input?.text.toString(), login_password_input?.text.toString())
            }
        }


        form {
            input(R.id.login_email_input) {
                isEmail()
            }

            submitWith(R.id.reset_password_link) {
                resetPassword(login_email_input?.text.toString())
            }
        }
    }

    private fun login(email: String, password: String) {
        this.requireActivity().findViewById<ProgressBar>(R.id.auth_progress_bar)?.visibility =
            View.VISIBLE
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this.requireActivity()) { task ->
                if (task.isSuccessful) {
                    this.requireActivity()
                        .findViewById<ProgressBar>(R.id.auth_progress_bar)?.visibility = View.GONE
                    this.requireActivity().finish()
                } else {
                    this.requireActivity()
                        .findViewById<ProgressBar>(R.id.auth_progress_bar)?.visibility = View.GONE

                    MaterialDialog(requireContext()).show {
                        title(R.string.oops)
                        message(text = task.exception?.message.toString())
                        positiveButton(R.string.okay)
                    }
                }
            }
    }

    private fun resetPassword(email: String) {
        this.requireActivity().findViewById<ProgressBar>(R.id.auth_progress_bar)?.visibility =
            View.VISIBLE
        mAuth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    this.requireActivity()
                        .findViewById<ProgressBar>(R.id.auth_progress_bar)?.visibility = View.GONE
                    Toast.makeText(
                        this.requireContext(),
                        "Reset email was sent!",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    this.requireActivity()
                        .findViewById<ProgressBar>(R.id.auth_progress_bar)?.visibility = View.GONE
                    MaterialDialog(requireContext()).show {
                        title(R.string.oops)
                        message(text = task.exception?.message.toString())
                        positiveButton(R.string.okay)
                    }
                }
            }
    }
}