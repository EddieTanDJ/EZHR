package com.example.ezhr.fragments.claims

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.camera.core.impl.utils.ContextUtil
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.ezhr.EZHRApp
import com.example.ezhr.R
import com.example.ezhr.data.Claim
import com.example.ezhr.databinding.FragmentClaimsEditBinding
import com.example.ezhr.viewmodel.ClaimDetailViewModel
import com.example.ezhr.viewmodel.ClaimDetailViewModelFactory
import com.example.ezhr.viewmodel.ClaimEditViewModel
import com.example.ezhr.viewmodel.ClaimEditViewModelFactory
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * A simple [Fragment] subclass.
 * Use the [ClaimEditFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ClaimEditFragment : Fragment()  , EasyPermissions.PermissionCallbacks {
    private var claimTypeIndex = 0
    private var claimID = ""
    private var currentUploadedImg = ""

    private lateinit var claimTitle: EditText
    private lateinit var claimAmt: EditText
    private lateinit var claimDesc: EditText
    private val CAMERA_REQUEST_CODE = 233
    private val GALLERY_REQUEST_CODE = 234
    private lateinit var uploadedFileURI: Uri
    private lateinit var addDocumentBtn: ImageButton
    private lateinit var cancelUploadBtn: ImageView
    private lateinit var imageText: TextView
    private var uploadedFileName = ""
    var userID = Firebase.auth.currentUser?.uid

    // NavController variable
    private lateinit var navController: NavController
    private var _binding: FragmentClaimsEditBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ClaimDetailViewModel by activityViewModels {
        ClaimDetailViewModelFactory()
    }

    private val claimEditViewModel: ClaimEditViewModel by activityViewModels {
        val application = requireActivity().application
        val app = application as EZHRApp
        ClaimEditViewModelFactory(app.claimEditRepo)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentClaimsEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SimpleDateFormat")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        claimTitle = binding.editTextClaimTitle
        claimAmt = binding.editTextAmount
        claimDesc = binding.editTextDescription
        imageText = binding.textViewUploadedFile

        addDocumentBtn = binding.buttonAddDocument
        cancelUploadBtn = binding.imageViewCancel

        navController = Navigation.findNavController(view)

        getClaimDetails()

        val spinner = binding.spinner
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.claim_types,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            spinner.prompt = "ClaimType"
            spinner.adapter = adapter
            spinner.setSelection(claimTypeIndex)
        }

        addDocumentBtn.setOnClickListener {
            selectImage()
        }

        cancelUploadBtn.setOnClickListener {
            markButtonEnabled(addDocumentBtn)
        }

        binding.buttonCancel.setOnClickListener {
            // Navigate back to the previous fragment
            navController.popBackStack()
        }

        binding.buttonResubmit.setOnClickListener {
            val claimType = spinner.selectedItem.toString()
            val sdf = SimpleDateFormat("dd/M/yyyy")
            val currentDate = sdf.format(Date()).toString()
            var valid = true

            if (TextUtils.isEmpty(claimTitle.text)) {
                claimTitle.error = "Please put in the claim title."
                valid = false
            }
            if (TextUtils.isEmpty(claimAmt.text)) {
                claimAmt.error = "Please put in the claim amount."
                valid = false
            }
            if (TextUtils.isEmpty(claimDesc.text)) {
                claimDesc.error = "Please put in the claim description."
                valid = false
            }

            if (valid) {
                startResubmissionConfirmationDialog(claimType, currentDate)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Prefill claim application form input fields based on previous claim submission
     */
    private fun getClaimDetails() {
        var claim: Claim? = Claim()
        // Get claims value from previous fragment
        claim = viewModel.claims.value
        Log.d(TAG, "Get Claim: $claim")
        claim?.let {
            viewModel.claimID.observe(viewLifecycleOwner) {
                claimID = it
            }
            claimID = viewModel.claimID.value!!
            currentUploadedImg = viewModel.uploadImage.value!!
            claimTitle.setText(claim.title)
            claimDesc.setText(claim.desc)
            claimAmt.setText(claim.amount.toString())
            uploadedFileName = currentUploadedImg

            if (claim.claimType == "Medical") {
                claimTypeIndex = 0
            } else if (claim.claimType == "Transportation") {
                claimTypeIndex = 1
            } else if (claim.claimType == "Food") {
                claimTypeIndex = 2
            } else {
                claimTypeIndex = 3
            }

            if (currentUploadedImg != "") {
                markButtonDisabled(addDocumentBtn, null)
            }
        }
    }


    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
    ) {
        super.onActivityResult(
            requestCode,
            resultCode,
            data
        )

        if (requestCode == GALLERY_REQUEST_CODE
            && resultCode == Activity.RESULT_OK
            && data != null
            && data.data != null
        ) {
            // Get the Uri of data
            val fileUri = data.data
            if (fileUri != null) {
                setCurrentFileURI(fileUri)
                markButtonDisabled(addDocumentBtn, fileUri)
            }
        }

        if (requestCode == CAMERA_REQUEST_CODE
            && resultCode == Activity.RESULT_OK
        ) {
            // Get the Uri of data
            val fileUri = mUri
            if (fileUri != null) {
                setCurrentFileURI(fileUri)
                markButtonDisabled(addDocumentBtn, fileUri)
            }
        }
    }

    /**
     * Disable upload button and display uploaded file name
     */
    fun markButtonDisabled(button: ImageButton, fileUri: Uri?) {
        button.isEnabled = false
        button.background.setTint(ContextCompat.getColor(requireContext(), R.color.uploadedColour))

        if (fileUri != null) {
            val context = requireContext()

            claimEditViewModel.getFileName(fileUri, context).observe(viewLifecycleOwner) {
                uploadedFileName = it.toString()

                val charDiff = uploadedFileName.count() - 30
                if (uploadedFileName.count() > 30) {
                    val extension = File(uploadedFileName).extension
                    val end = uploadedFileName.indexOf(".") - charDiff + extension.count()
                    uploadedFileName = uploadedFileName.substring(0, end) + ".$extension"
                }

                imageText.text = uploadedFileName
                cancelUploadBtn.visibility = View.VISIBLE
            }
        } else {
            imageText.text = uploadedFileName
            cancelUploadBtn.visibility = View.VISIBLE
        }
    }

    /**
     * Enable upload button and clear uploaded file name
     */
    fun markButtonEnabled(button: ImageButton) {
        button.isEnabled = true
        button.background.setTint(
            ContextCompat.getColor(
                requireContext(),
                R.color.colorSecondary
            )
        )

        imageText.text = ""
        uploadedFileName = ""
        cancelUploadBtn.visibility = View.INVISIBLE
    }

    private fun setCurrentFileURI(currentURI: Uri) {
        uploadedFileURI = currentURI
    }

    /**
     * Open up Dialog Picker to select image from gallery or camera
     */
    private fun selectImage() {
        startDialog()
    }


    /**
     * Allow user to choose between camera and gallery
     */
    @SuppressLint("RestrictedApi")
    private fun startDialog() {
        val myAlertDialog: AlertDialog.Builder = MaterialAlertDialogBuilder(requireContext())
        myAlertDialog.setTitle("Upload Pictures")
        myAlertDialog.setMessage("How do you want to upload your picture?")
        myAlertDialog.setPositiveButton("Gallery",
            DialogInterface.OnClickListener { arg0, arg1 ->
                val galleryIntent = Intent()
                galleryIntent.type = "image/*"
                galleryIntent.action = Intent.ACTION_GET_CONTENT
                startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE)
            })
        myAlertDialog.setNegativeButton("Camera",
            DialogInterface.OnClickListener { arg0, arg1 ->
                requestCameraPermission()
            })
        myAlertDialog.show()
    }

    /**
     * Request Camera permission. If permission is granted, open camera, else ask for permission
     */
    private fun requestCameraPermission() {
        if (hasCameraPermission(requireContext())) {
            setupCamera()
            return
        }
        EasyPermissions.requestPermissions(
            this,
            "Camera permission is needed to take pictures.",
            CAMERA_REQUEST_CODE,
            android.Manifest.permission.CAMERA
        )
    }


    /**
     * Setup Camera
     */
    @SuppressLint("RestrictedApi")
    private fun setupCamera() {
        val capturedImage = File(ContextUtil.getApplicationContext(requireContext()).getExternalFilesDir(""), "Claims_${System.currentTimeMillis()}.jpg")
        if(capturedImage.exists()) {
            capturedImage.delete()
        }
        capturedImage.createNewFile()
        mUri = if(Build.VERSION.SDK_INT >= 24){
            FileProvider.getUriForFile(requireContext(), "com.example.ezhr.fileprovider", capturedImage)
        } else {
            Uri.fromFile(capturedImage)
        }
        Log.d("TAG", "mUri: ${mUri}.toString()")
        val intent = Intent("android.media.action.IMAGE_CAPTURE")
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mUri)
        startActivityForResult(intent, CAMERA_REQUEST_CODE)
    }


    /**
     * Check if permission is already granted
     */

    private fun hasCameraPermission(context: Context) = EasyPermissions.hasPermissions(
        context,
        Manifest.permission.CAMERA
    )

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        Log.d(TAG, "onPermissionsGranted: Permission granted")
        setupCamera()
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        Log.d(TAG, "onPermissionDenied: Permission denied")
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            // AppSettingsDialog.Builder(this).build().show()
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Permissions Required")
                .setMessage("This app may not work properly without the requested permissions. Open the app settings scrreen to modify app permissions.")
                .setPositiveButton("Settings") { _, _ ->
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", requireContext().packageName, null)
                    intent.data = uri
                    startActivity(intent)
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .create().show()
        } else {
            requestCameraPermission()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    /**
     * Handle Dialog for confirmation of claim resubmission
     */
    private fun startResubmissionConfirmationDialog(claimType : String, currentDate: String) {
        val builder = MaterialAlertDialogBuilder(requireContext())
        builder.setTitle("Confirmation")
        builder.setMessage("Are you sure you want to resubmit this claim?")
        builder.setPositiveButton("Yes") { dialog, _ ->
            dialog.dismiss()
            claimEditViewModel.getCurrentClaimBalance(claimType).observe(viewLifecycleOwner) {
                if (claimAmt.text.toString().toDouble() <= it) {
                    var claim = Claim(
                        "",
                        claimTitle.text.toString(),
                        claimType,
                        "",
                        currentDate,
                        claimDesc.text.toString(),
                        claimAmt.text.toString().toDouble(),
                        uploadedFileName
                    )

                    var uri: Uri? = null
                    if (this::uploadedFileURI.isInitialized) {
                        uri = uploadedFileURI
                    } else {
                        uri = null
                    }

                    claimEditViewModel.updateClaim(
                        claim,
                        claimID,
                        uri,
                        currentUploadedImg,
                        uploadedFileName
                    ).observe(viewLifecycleOwner) {
                        if (it) {
                            Toast.makeText(
                                context,
                                "Claim application resubmitted.",
                                Toast.LENGTH_LONG
                            ).show()
                            // Navigate to claim status fragment
                            navController.navigate(R.id.action_claimsEditFragment_to_claimsStatusFragment)
                        } else {
                            Toast.makeText(
                                context,
                                "Failed to resubmit claim application.",
                                Toast.LENGTH_LONG
                            )
                                .show()
                        }
                    }
                } else {
                    Toast.makeText(
                        context,
                        "Claim balance for $claimType is too low.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }
        builder.create().show()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.gi
         * @return A new instance of fragment.
         */
        private val TAG = ClaimEditFragment::class.simpleName
        private var mUri: Uri? = null
        fun newInstance(): ClaimEditFragment {
            return ClaimEditFragment()
        }

        fun isClaimTitleEmpty(title: String): Boolean {
            return title.isEmpty()
        }

        fun isClaimAmountEmpty(amount: Int): Boolean {
            return amount == 0
        }

        fun isClaimDescEmpty(desc: String): Boolean {
            return desc.isEmpty()
        }
    }
}