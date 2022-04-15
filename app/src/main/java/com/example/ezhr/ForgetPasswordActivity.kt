package com.example.ezhr

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.util.PatternsCompat
import com.bumptech.glide.Glide
import com.example.ezhr.databinding.ActivityForgetPasswordBinding
import com.example.ezhr.viewmodel.AccountViewModel
import com.example.ezhr.viewmodel.AccountViewModelFactory
import com.google.firebase.auth.FirebaseAuth

class ForgetPasswordActivity : AppCompatActivity() {
    private lateinit var listIntent: Intent
    private lateinit var binding: ActivityForgetPasswordBinding
    private lateinit var auth: FirebaseAuth

    private val viewModel: AccountViewModel by viewModels {
        val app = application as EZHRApp
        AccountViewModelFactory(app.accountRepo)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "Forget Password Activity Created")
        super.onCreate(savedInstanceState)
        binding = ActivityForgetPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()

        showGif()

        val progressbar = binding.forgetPasswordProgrssBar
        viewModel.progress.observe(this) { it ->
            progressbar.visibility = if (it) View.VISIBLE else View.GONE
        }

        binding.resetPassBtn.setOnClickListener {
            val emailET: EditText = binding.emailEdtText
            val email: Editable? = binding.emailEdtText.text
            if (isEmailEmpty(email.toString())) {
                Log.d(TAG, "Email is Empty")
                emailET.error = "Please enter email address"
            } else if (!isEmailValid(email.toString())) {
                Log.d(TAG, "Email is not valid")
                emailET.error = "Please enter valid email address"
            } else {
                Log.d(TAG, "${email.toString()}")
                viewModel.forgetPassword(email.toString())
            }
        }

        binding.backBtn.setOnClickListener {
            finish()
        }


        viewModel.passwordStatus.observe(this) {
            Log.d(TAG, "$it")
            if (it == true) {
                Toast.makeText(this, "Email Sent", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Incorrect Email Address", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Show Gif
     */
    private fun showGif() {
        val imageView = binding.imageView6
        Glide.with(this).load(R.drawable.forget_password).into(imageView)
    }

    companion object {
        private const val TAG = "ForgetPasswordActivity"

        /**
         * Check email is valid
         */
        fun isEmailValid(email: String): Boolean {
            return PatternsCompat.EMAIL_ADDRESS.matcher(email).matches()
        }

        /**
         * Check if input is empty
         */
        fun isEmailEmpty(email: String): Boolean {
            return email.isEmpty()
        }

    }

}



