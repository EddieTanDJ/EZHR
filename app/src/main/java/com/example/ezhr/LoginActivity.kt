package com.example.ezhr

import android.Manifest.permission.*
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.core.util.PatternsCompat
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.example.ezhr.databinding.ActivityLoginBinding
import com.example.ezhr.util.Resource
import com.example.ezhr.viewmodel.AccountViewModel
import com.example.ezhr.viewmodel.AccountViewModelFactory
import com.example.ezhr.viewmodel.LoginViewModel
import com.example.ezhr.viewmodel.LoginViewModelFactory
import com.google.firebase.auth.FirebaseAuth


class LoginActivity : AppCompatActivity() {
    private lateinit var listIntent: Intent
    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var biometricManager: BiometricManager
    private val viewModel: LoginViewModel by viewModels {
        val app = application as EZHRApp
        LoginViewModelFactory(app.loginRepo)
    }

    private val viewModelAccount: AccountViewModel by viewModels {
        val app = application as EZHRApp
        AccountViewModelFactory(app.accountRepo)
    }

    @RequiresApi(Build.VERSION_CODES.S)
    @RequiresPermission(allOf = [ACCESS_NETWORK_STATE, INTERNET, SCHEDULE_EXACT_ALARM])
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "Login Created")
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        setupBiometricAuthentication()
        checkBiometricFeatureState()
        showGif()
        // Check if user is already logged in
        binding.loginBtn.setOnClickListener {
            // Get all the variable
            val email: EditText = binding.emailEdtText
            val emailEditText: Editable? = binding.emailEdtText.text
            val password: EditText = binding.passEdtText
            val passwordEditText: Editable? = binding.passEdtText.text
            // Check if the user has entered the email and password
            if (isInputEmpty(emailEditText.toString(), passwordEditText.toString())) {
                Log.d(TAG, "email or password is empty")
                when (isEmailEmpty(emailEditText.toString()) && isPasswordEmpty(passwordEditText.toString())) {
                    true -> {
                        email.error = "Please enter email address"
                        password.error = "Please enter password"
                    }
                    else ->
                        when (isPasswordEmpty(passwordEditText.toString())) {
                            true -> password.error = "Please enter password"
                            else -> if (TextUtils.isEmpty(emailEditText.toString())) {
                                email.error = "Please enter email"
                            }
                        }
                }
            } else if (!isEmailValid(emailEditText.toString())) {
                Log.d(TAG, "email not valid")
                email.error = "Please enter valid email address"
            } else {
                if (checkForInternet(this)) {
                    Log.d(TAG, "Network connected")
                    viewModel.signInUser(emailEditText.toString(), passwordEditText.toString())
                } else {
                    Log.d(TAG, "Network not connected")
                    setMessage("Please connect to a network")
                }

            }
        }
        // Event handler for reset password
        binding.resetPassTv.setOnClickListener {
            Log.d("TAG", "Login button pressed")
            val intent = Intent(this, ForgetPasswordActivity::class.java)
            startActivity(intent)
        }
        // Event handler for biometric login
        binding.ivBiometricLogin.setOnClickListener {
            if (isBiometricFeatureAvailable()) {
                biometricPrompt.authenticate(buildBiometricPrompt())
            }
        }

        // Using MVVM structure to login
        viewModel.signInStatus.observe(this) {
            val emailEditText: Editable? = binding.emailEdtText.text
            val passwordEditText: Editable? = binding.passEdtText.text
            when (it) {
                is Resource.Loading -> {
                    binding.loginProgressBar.isVisible = true
                }
                is Resource.Success -> {
                    binding.loginProgressBar.isVisible = false
                    var mToast: Toast? = null
                    if (mToast == null) {
                        setMessage("Login Successfully")
                    }
                    if (!isInputEmpty(emailEditText.toString(), passwordEditText.toString())) {
                        Log.d(TAG, "Update the preference data store")
                        viewModel.updateCredentials(
                            emailEditText.toString(),
                            passwordEditText.toString()
                        )
                    }
                    // Fetch user data
                    viewModelAccount.fetchUserData().observe(this@LoginActivity) { user ->
                        if (user.exception != null) {
                            Toast.makeText(
                                this,
                                "Error: ${user.exception.toString()}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        Log.d(TAG, "Data: ${user.user}")
                        var userRole = user.user?.Role
                        Log.d(TAG, "Role: $userRole")
                        if (userRole == "Employee") {
                            Log.d(TAG, "Starting employee activity")
                            listIntent = Intent(this, BottomActivity::class.java)
                            startActivity(listIntent)
                            finish()
                        } else {
                            Log.d(TAG, "Starting manager activity")
                            listIntent = Intent(this, ManagerActivity::class.java)
                            startActivity(listIntent)
                            finish()
                        }
                    }
                }
                is Resource.Error -> {
                    binding.loginProgressBar.isVisible = false
                    var mToast: Toast? = null
                    if (mToast == null) {
                        setMessage("Login Failed")
                    }
                }

            }
        }

    }

    /**
     * Show gif image
     */
    private fun showGif() {
        val imageView = binding.imageView2
        Glide.with(this).load(R.drawable.login).into(imageView)
    }


    /**
     * Check if the phone is connected to network
     */
    @RequiresPermission(allOf = [ACCESS_NETWORK_STATE, INTERNET])
    private fun checkForInternet(context: Context): Boolean {

        // register activity with the connectivity manager service
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        // if the android version is equal to M
        // or greater we need to use the
        // NetworkCapabilities to check what type of
        // network has the internet connection
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            // Returns a Network object corresponding to
            // the currently active default data network.
            val network = connectivityManager.activeNetwork ?: return false

            // Representation of the capabilities of an active network.
            val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false

            return when {
                // Indicates this network uses a Wi-Fi transport,
                // or WiFi has network connectivity
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true

                // Indicates this network uses a Cellular transport. or
                // Cellular has network connectivity
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true

                // else return false
                else -> false
            }
        } else {
            // if the android version is below M
            @Suppress("DEPRECATION") val networkInfo =
                connectivityManager.activeNetworkInfo ?: return false
            @Suppress("DEPRECATION")
            return networkInfo.isConnected
        }
    }

    /**
     * Setup the biometric authentication
     */
    private fun setupBiometricAuthentication() {
        biometricManager = BiometricManager.from(this)
        val executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this, executor, biometricCallback)
    }

    /**
     * Callback for biometric authentication
     */
    private fun checkBiometricFeatureState() {
        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)) {
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                Log.d(TAG, "No biometric hardware.")
                setMessage("Biometric login is not supported.")
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                Log.d(TAG, "Hardware unavailable")
                setMessage("Biometric features are currently unavailable.")
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                Log.d(TAG, "No biometric credentials")
                setMessage("You does not have biometric credentials")
            }
            BiometricManager.BIOMETRIC_SUCCESS -> {}
            else -> {
                Log.d(TAG, "Unknown error")
                setMessage("Unknown error")
            }
        }
    }

    /**
     * This method is used to build the biometric prompt
     * @return BiometricPrompt.PromptInfo
     */
    private fun buildBiometricPrompt(): BiometricPrompt.PromptInfo {
        return BiometricPrompt.PromptInfo.Builder()
            .setTitle("Login with biometric")
            .setDescription("Use your biometric to authenticate with EZHR")
            .setNegativeButtonText("Use Password")
            .setConfirmationRequired(false) //Allows user to authenticate without performing an action, such as pressing a button, after their biometric credential is accepted.
            .build()
    }

    /**
     *  Check if biometric features is available
     */
    private fun isBiometricFeatureAvailable(): Boolean {
        return biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK) == BiometricManager.BIOMETRIC_SUCCESS
    }

    @RequiresPermission(allOf = [ACCESS_NETWORK_STATE, INTERNET])
    private val biometricCallback = object : BiometricPrompt.AuthenticationCallback() {
        @RequiresPermission(allOf = [ACCESS_NETWORK_STATE, INTERNET])
        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            super.onAuthenticationSucceeded(result)
            if (checkForInternet(applicationContext)) {
                Log.d(TAG, "Biometric login successful")
                // TODO RETRIEVE LOGIN CREDENTIALS FROM DATASTORE
                viewModel.getCredentials()
                viewModel.credentialsData.observe(this@LoginActivity) { credentials ->
                    if (credentials == null) {
                        setMessage("Please login via email and password first")
                    } else {
                        val email = credentials.email
                        val password = credentials.password
                        if (email == null || password == null) {
                            setMessage("Please login via email and password first")
                        } else {
                            Log.d(
                                TAG,
                                "Data Retrieve from data store: Email: $email, Password: $password"
                            )
                            viewModel.signInUser(email, password)
                        }
                    }
                }
            } else {
                setMessage("Please connect to a network")
            }
        }

        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            super.onAuthenticationError(errorCode, errString)
            if (errorCode != 13 && errorCode != 10) {
                setMessage(errString.toString())
            }
        }
    }

    private fun setMessage(errorMessage: String) {
        Toast.makeText(this, "$errorMessage", Toast.LENGTH_SHORT).show()
    }


    companion object {
        private const val TAG = "LoginActivity"

        /**
         * Check email is valid
         */
        fun isEmailValid(email: String): Boolean {
            return PatternsCompat.EMAIL_ADDRESS.matcher(email).matches()
        }

        /**
         * Check if input is empty
         */
        fun isInputEmpty(email: String, password: String): Boolean {
            return email.isEmpty() || password.isEmpty()
        }

        /**
         * Check if email field  is empty
         */
        fun isEmailEmpty(email: String): Boolean {
            return email.isEmpty()
        }

        /**
         * Check if password field is empty
         */
        fun isPasswordEmpty(password: String): Boolean {
            return password.isEmpty()
        }
    }

}
