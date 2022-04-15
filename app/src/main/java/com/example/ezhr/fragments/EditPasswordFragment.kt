package com.example.ezhr.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.ezhr.EZHRApp
import com.example.ezhr.LoginActivity
import com.example.ezhr.databinding.FragmentEditPasswordBinding
import com.example.ezhr.viewmodel.LoginViewModel
import com.example.ezhr.viewmodel.LoginViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


/**
 * Edit Password fragment
 */
class EditPasswordFragment : Fragment() {

    private lateinit var listIntent: Intent
    private lateinit var auth: FirebaseAuth

    // assign the _binding variable initially to null and
    // also when the view is destroyed again it has to be set to null
    private var _binding: FragmentEditPasswordBinding? = null

    // with the backing property of the kotlin we extract
    // the non null value of the _binding
    private val binding get() = _binding!!
    private val viewModel: LoginViewModel by activityViewModels {
        val application = requireActivity().application
        val app = application as EZHRApp
        LoginViewModelFactory(app.loginRepo)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Called immediately after [.onCreateView]
     * has returned, but before any saved state has been restored in to the view.
     * This gives subclasses a chance to initialize themselves once
     * they know their view hierarchy has been completely created.  The fragment's
     * view hierarchy is not however attached to its parent at this point.
     * @param view The View returned by [.onCreateView].
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val changePasswordButton: Button = binding.changePasswordBtn
        val user = Firebase.auth.currentUser
        Log.d(TAG, "User: $user")
        if (user != null) {
            auth = FirebaseAuth.getInstance()
            changePasswordButton.setOnClickListener {
                changePassword()
            }
        }
        val progressbar = binding.editPasswordProgressBar
        viewModel.progress.observe(viewLifecycleOwner) {
            progressbar.visibility = if (it) View.VISIBLE else View.GONE
        }

        viewModel.isAuthenticated.observe(viewLifecycleOwner) {
            if (it == true) {
                val newPasswordEditText = binding.newPasswordEt
                Log.d(TAG, "Re-authenticate Successful")
                // Toast.makeText(context, "Re-authenticate Successful",  Toast.LENGTH_SHORT).show()
                viewModel.changePassword(newPasswordEditText.text.toString())
            } else {
                Log.d(TAG, "Incorrect Password")
                Toast.makeText(context, "Incorrect Current Password", Toast.LENGTH_SHORT).show()
            }
        }
        viewModel.changePasswordStatus.observe(viewLifecycleOwner) {
            if (it == true) {
                Log.d(TAG, "Password change Successful")
                Toast.makeText(
                    context,
                    "Password changed successfully.",
                    Toast.LENGTH_SHORT
                ).show()
                auth.signOut()
                activity?.let { it ->
                    FirebaseAuth.getInstance().signOut()
                    listIntent = Intent(it, LoginActivity::class.java)
                    listIntent.flags =
                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    it.startActivity(listIntent)
                    viewModel.clearData()
                }
            } else {
                Log.d(TAG, "password less than 6 characters")
                Toast.makeText(
                    context,
                    "Please enter password that has more than 6 characters",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Change the password of the user
     */
    private fun changePassword() {
        // Declare all variable
        val currentPasswordEditText = binding.currentPasswordEt
        val newPasswordEditText = binding.newPasswordEt
        val confirmNewPasswordEditText = binding.confirmNewPasswordEt

        if (currentPasswordEditText.text.isNotEmpty() && newPasswordEditText.text.isNotEmpty()
            && confirmNewPasswordEditText.text.isNotEmpty()
        ) {
            if (isPasswordEqual(
                    newPasswordEditText.text.toString(),
                    confirmNewPasswordEditText.text.toString()
                )
            ) {
                viewModel.authentication(currentPasswordEditText.text.toString())
            } else {
                Log.d(TAG, "Password and confirm password does not match")
                Toast.makeText(
                    context,
                    "Password and confirm password does not match.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            Log.d(TAG, "Please enter all field.")
            if (isCurrentPasswordEmpty(currentPasswordEditText.text.toString())) {
                currentPasswordEditText.error = "Please enter current password"
            }
            if (isNewPasswordEmpty(newPasswordEditText.text.toString())) {
                newPasswordEditText.error = "Please enter new password"
            }
            if (isConfirmPasswordEmpty(confirmNewPasswordEditText.text.toString())) {
                confirmNewPasswordEditText.error = "Please enter confirm password"
            }
        }
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         * @return A new instance of fragment.
         */
        /**
         * Check if current  password is empty
         */
        fun isCurrentPasswordEmpty(currentPassword: String): Boolean {
            return currentPassword.isEmpty()
        }

        /**
         * Check if new  password is empty
         */
        fun isNewPasswordEmpty(newPassword: String): Boolean {
            return newPassword.isEmpty()
        }

        /**
         * Check if confirm  password is empty
         */
        fun isConfirmPasswordEmpty(confirmPassword: String): Boolean {
            return confirmPassword.isEmpty()
        }

        /**
         * Check if new password and confirm password is equal
         */
        fun isPasswordEqual(password: String, confirmPassword: String): Boolean {
            return password == confirmPassword
        }

        private val TAG = EditPasswordFragment::class.simpleName
        fun newInstance(): EditPasswordFragment {
            return EditPasswordFragment()
        }
    }

}