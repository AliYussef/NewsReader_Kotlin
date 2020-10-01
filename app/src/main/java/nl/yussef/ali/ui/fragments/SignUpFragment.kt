package nl.yussef.ali.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_sigup.*
import kotlinx.android.synthetic.main.fragment_sigup.loading
import nl.yussef.ali.R
import nl.yussef.ali.model.User
import nl.yussef.ali.ui.activities.HomeActivity
import nl.yussef.ali.ui.viewModel.UserViewModel
import nl.yussef.ali.util.Resource

class SignUpFragment : Fragment(R.layout.fragment_sigup) {
    lateinit var viewModel: UserViewModel
    lateinit var user: User

    companion object {
        const val TAG = "SignUpFragment"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = (activity as HomeActivity).userViewModel
        initializeUI()

        viewModel.signUpResponse.observe(viewLifecycleOwner, Observer { response ->
            loading.visibility = View.GONE
            when (response) {
                is Resource.Success -> {
                    confirmPasswordText.onEditorAction(EditorInfo.IME_ACTION_DONE)
                    findNavController().navigate(
                        R.id.action_signupFragment_to_loginFragment
                    )
                }
                is Resource.Error -> {
                    Toast.makeText(requireContext(), response.message, Toast.LENGTH_LONG).show()
                    response.message?.let { message ->
                        Log.e(TAG, "${getString(R.string.error_log)} $message")
                    }
                }
            }

        })
    }

    private fun initializeUI() {
        signUp.setOnClickListener {
            val username = usernameText.text.toString().trim()
            val password = passwordText.text.toString().trim()
            val confirmPassword = confirmPasswordText.text.toString().trim()
            if (isPasswordValid(password) && isPasswordMatching(password, confirmPassword)) {
                user = User(username, password)
                loading.visibility = View.VISIBLE
                viewModel.createUser(user)
            } else if (!isPasswordValid(password)) {
                passwordText.error = getString(R.string.password_length)
            } else {
                confirmPasswordText.error = getString(R.string.password_no_match)
            }
        }
    }

    private fun isPasswordValid(password: String): Boolean {
        return password.length > 5
    }

    private fun isPasswordMatching(password: String, confirmPassword: String): Boolean {
        return password == confirmPassword
    }
}