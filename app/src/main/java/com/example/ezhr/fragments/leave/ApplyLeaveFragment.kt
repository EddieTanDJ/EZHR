package com.example.ezhr.fragments.leave

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.camera.core.impl.utils.ContextUtil
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.ezhr.R
import com.example.ezhr.data.LeaveStatus
import com.example.ezhr.databinding.FragmentApplyLeaveBinding
import com.example.ezhr.viewmodel.ApplyLeaveViewModel
import com.example.ezhr.viewmodel.ApplyLeaveViewModelFactory
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import pub.devrel.easypermissions.EasyPermissions
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.io.IOException
import java.util.concurrent.TimeUnit


/**
 * Apply leave fragment
 */
class ApplyLeaveFragment : Fragment(), EasyPermissions.PermissionCallbacks {
    private lateinit var database: DatabaseReference

    lateinit var startDate: String
    lateinit var endDate: String

    @SuppressLint("SimpleDateFormat")
    val sdf = SimpleDateFormat("dd/MM/yyyy")

    // NavController variable
    private lateinit var navController: NavController
    private lateinit var addDocumentBtn: ImageButton
    private lateinit var documentName: TextView
    private lateinit var cancelBtn: ImageView

    // assign the _binding variable initially to null and
    // also when the view is destroyed again it has to be set to null
    private var _binding: FragmentApplyLeaveBinding? = null
    lateinit var startDateFormatted: Date
    lateinit var endDateFormatted: Date
    var uid = Firebase.auth.currentUser?.uid
    private val binding get() = _binding!!

    private var uploadedFileName = ""
    private val CAMERA_REQUEST_CODE = 100
    private val GALLERY_REQUEST_CODE = 101
    private lateinit var uploadedFileURI: Uri

    lateinit var annualLeaveBalanceString: String
    lateinit var compassionateLeaveBalanceString: String
    lateinit var maternityLeaveBalanceString: String
    lateinit var medicalLeaveBalanceString: String

    private val viewModel: ApplyLeaveViewModel by activityViewModels {
        ApplyLeaveViewModelFactory()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentApplyLeaveBinding.inflate(inflater, container, false)
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
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val todayDate = ("" + day + " - " + (month + 1) + " - " + year)


        //Find all the view IDs from the XML
        val startDateButton = binding.setStartLeaveButton
        val startLeaveDate = binding.startLeaveDate
        val endDateButton = binding.setEndLeaveButton
        val endLeaveDate = binding.endLeaveDate
        val submitButton = binding.submitButton
        val leaveSpinner: Spinner = binding.selectLeaveSpinner
        val leaveBalanceButton = binding.leaveBalanceButton
        navController = Navigation.findNavController(view)
        cancelBtn = binding.imageViewCancel
        documentName = binding.textViewUploadedFile
        addDocumentBtn = binding.buttonAddDocument


        //Retrieving Leave balances
        database = FirebaseDatabase.getInstance().getReference("leave_balances")
        if (uid != null) {
            database.child(uid!!).get().addOnSuccessListener {
                if (it.exists()) {
                    val annualLeaveBalance = it.child("annual_balance").value
                    val compassionateLeaveBalance = it.child("compassionate_balance").value
                    val maternityLeaveBalance = it.child("maternity_balance").value
                    val medicalLeaveBalance = it.child("medical_balance").value

                    annualLeaveBalanceString = annualLeaveBalance.toString()
                    compassionateLeaveBalanceString = compassionateLeaveBalance.toString()
                    maternityLeaveBalanceString = maternityLeaveBalance.toString()
                    medicalLeaveBalanceString = medicalLeaveBalance.toString()
                }
            }
        }


        //Set the date for the start of leave date
        startDateButton.setOnClickListener {
            val datePickerDialog =
                DatePickerDialog(
                    requireContext(), DatePickerDialog.OnDateSetListener
                    { view, year, monthOfYear, dayOfMonth ->
                        startLeaveDate.text =
                            "" + dayOfMonth + " - " + (monthOfYear + 1) + " - " + year
                        startDate = "" + dayOfMonth + "/" + (monthOfYear + 1) + "/" + year
                        startDateFormatted = sdf.parse(startDate)
                    }, year, month, day
                )
            datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000
            datePickerDialog.show()
        }

        //When user clicks on leave balance button
        leaveBalanceButton.setOnClickListener {
            val builder = MaterialAlertDialogBuilder(requireContext())
            //set title for alert dialog
            builder.setTitle("Leave Balances")
            //set message for alert dialog
            builder.setMessage(
                "Annual Leaves : " + annualLeaveBalanceString + "\n" +
                        "Compassionate Leaves : " + compassionateLeaveBalanceString + "\n" +
                        "Maternity Leaves : " + maternityLeaveBalanceString + "\n" +
                        "Medical Leaves : " + medicalLeaveBalanceString + "\n"
            )

            builder.setPositiveButton("Done", null)
            builder.create()
            builder.show()

        }

        //Set the date for the end of leave date
        endDateButton.setOnClickListener {
            val datePickerDialog =
                DatePickerDialog(
                    requireContext(), DatePickerDialog.OnDateSetListener
                    { view, year, monthOfYear, dayOfMonth ->
                        endLeaveDate.text =
                            "" + dayOfMonth + " - " + (monthOfYear + 1) + " - " + year
                        endDate = "" + dayOfMonth + "/" + (monthOfYear + 1) + "/" + year
                        endDateFormatted = sdf.parse(endDate)
                    }, year, month, day
                )
            datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000
            datePickerDialog.show()
        }

        //When user click on the add document button
        addDocumentBtn.setOnClickListener {
            selectImage()
        }

        cancelBtn.setOnClickListener {
            markButtonEnabled(addDocumentBtn)
        }

        //When user click on submit button
        submitButton.setOnClickListener {
            if (startLeaveDate.text.isEmpty() or endLeaveDate.text.isEmpty() or (leaveSpinner.selectedItem.toString() == "no selection")) {
                Toast.makeText(
                    context,
                    "One of more mandatory fields missing!",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                val diff = endDateFormatted.time - startDateFormatted.time
                if (diff < 0) {
                    Toast.makeText(
                        context,
                        "End date cannot be earlier than start date!",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    var numberOfDays =
                        ((TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS)).toInt()) + 1
                    //Minimum 1 day of leave
                    if (numberOfDays == 0) {
                        numberOfDays = 1
                    }
                    val builder = MaterialAlertDialogBuilder(requireContext())
                    //set title for alert dialog
                    builder.setTitle(R.string.dialogTitle)
                    //set message for alert dialog
                    builder.setMessage(
                        "You are about to apply for $numberOfDays day(s) of leave from $startDate to $endDate\n" +
                                "Are you sure you want to apply for this leave?"
                    )
                    builder.setIcon(android.R.drawable.ic_dialog_alert)

                    //performing positive action
                    builder.setPositiveButton("Confirm") { dialogInterface, which ->
                        val leaveType = leaveSpinner.selectedItem.toString()
                        viewModel.getCurrentLeaveBalance(leaveType)
                        viewModel.currentLeaveBalance.observe(viewLifecycleOwner) {
                            val finalAmt = it.toString().toInt()
                            if (numberOfDays <= finalAmt) {
                                leaveStatusList.add(
                                    LeaveStatus(
                                        leaveSpinner.selectedItem.toString(),
                                        todayDate,
                                        "Pending",
                                        startLeaveDate.text.toString(),
                                        endLeaveDate.text.toString(),
                                        numberOfDays,
                                        uid,
                                        uploadedFileName
                                    )
                                )
                                var leaveStatus = LeaveStatus(
                                    leaveSpinner.selectedItem.toString(),
                                    todayDate,
                                    "Pending",
                                    startLeaveDate.text.toString(),
                                    endLeaveDate.text.toString(),
                                    numberOfDays,
                                    uid,
                                    uploadedFileName
                                )

                                var uri: Uri? = null
                                if (this::uploadedFileURI.isInitialized) {
                                    uri = uploadedFileURI
                                } else {
                                    uri = null
                                }

                                viewModel.uploadLeaveApplication(uri, leaveStatus, uploadedFileName)
                                viewModel.uploadSuccess.observe(viewLifecycleOwner) {
                                    if (it) {
                                        Toast.makeText(
                                            context,
                                            "Leave Application Submitted.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        navController.navigate(R.id.action_applyLeaveFragment_to_homeFragment)
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "Failed to Submit Leave Application.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            } else {
                                Toast.makeText(
                                    context,
                                    "Leave balance for $leaveType is too low.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }

                    //performing cancel action
                    builder.setNeutralButton("Cancel") { dialogInterface, which ->
                        dialogInterface.dismiss()
                    }
                    // Create the AlertDialog
                    val alertDialog = builder.create()
                    // Set other dialog properties
                    alertDialog.setCancelable(false)
                    alertDialog.show()
                }
            }
        }

        // access the items of the select leave types
        val leaveTypes = resources.getStringArray(R.array.leaveTypes)

        // access the spinner of the select leave types
        if (leaveSpinner != null) {
            val adapterLeaveType = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item, leaveTypes
            )
            leaveSpinner.adapter = adapterLeaveType
        }

        //access the items of view leave balance
        val balances = resources.getStringArray(R.array.Balances)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
    private fun markButtonDisabled(button: ImageButton, fileUri: Uri?) {
        button.isEnabled = false
        button.background.setTint(ContextCompat.getColor(requireContext(), R.color.uploadedColour))

        if (fileUri != null) {
            val context = requireContext()
            viewModel.getFileName(fileUri, context)
            viewModel.fileName.observe(viewLifecycleOwner) {
                uploadedFileName = it.toString()

                val charDiff = uploadedFileName.count() - 30
                if (uploadedFileName.count() > 30) {
                    val extension = File(uploadedFileName).extension
                    val end = uploadedFileName.indexOf(".") - charDiff + extension.count()
                    uploadedFileName = uploadedFileName.substring(0, end) + ".$extension"
                }

                documentName.text = uploadedFileName
                cancelBtn.visibility = View.VISIBLE
            }
        }
    }

    /**
     * Enable upload button and clear uploaded file name
     */
    private fun markButtonEnabled(button: ImageButton) {
        button.isEnabled = true
        button.background.setTint(
            ContextCompat.getColor(
                requireContext(),
                R.color.colorSecondary
            )
        )
        documentName.text = ""
        cancelBtn.visibility = View.INVISIBLE

        uploadedFileName = ""
    }

    private fun setCurrentFileURI(currentURI: Uri) {
        uploadedFileURI = currentURI
    }

    /**
     * Open up image gallery
     */
    private fun selectImage() {
        startDialog()
    }

    /**
     * Allow user to choose between camera and gallery
     */
    @SuppressLint("RestrictedApi")
    private fun startDialog() {
        val myAlertDialog = MaterialAlertDialogBuilder(requireContext())
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
        myAlertDialog.create().show()
    }

    /**
     * Request Camera permission. If permission is granted, open camera, else ask for permission
     */
    private fun requestCameraPermission() {
        if (hasCameraPermission(requireContext())) {
            Log.d(TAG, "Request Permission: Camera permission granted")
            setupCamera()
            return
        }
        Log.d(TAG, "Request Permission: Camera permission not granted")
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
        val capturedImage = File(ContextUtil.getApplicationContext(requireContext()).getExternalFilesDir(""), "Leave_${System.currentTimeMillis()}.jpg")
        if(capturedImage.exists()) {
            capturedImage.delete()
        }
        capturedImage.createNewFile()
        mUri = if(Build.VERSION.SDK_INT >= 24){
            FileProvider.getUriForFile(requireContext(), "com.example.ezhr.fileprovider", capturedImage)
        } else {
            Uri.fromFile(capturedImage)
        }
        Log.d(TAG, "mUri: ${mUri}.toString()")
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


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         * @return A new instance of fragment.
         */
        private val TAG = ApplyLeaveFragment::class.java.simpleName
        private var mUri: Uri? = null
        fun newInstance(): ApplyLeaveFragment {
            return ApplyLeaveFragment()
        }
    }
}