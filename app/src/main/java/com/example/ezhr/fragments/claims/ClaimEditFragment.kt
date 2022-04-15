package com.example.ezhr.fragments.claims

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
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
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * A simple [Fragment] subclass.
 * Use the [ClaimEditFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ClaimEditFragment : Fragment() {
    private var claimTypeIndex = 0
    private var claimID = ""
    private var currentUploadedImg = ""

    private lateinit var claimTitle: EditText
    private lateinit var claimAmt: EditText
    private lateinit var claimDesc: EditText

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
            selectImageFromGallery()
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
            val currentDate = sdf.format(Date())
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
                claimEditViewModel.getCurrentClaimBalance(claimType).observe(viewLifecycleOwner) {
                    if (claimAmt.text.toString().toDouble() <= it) {
                        var claim = Claim(
                            "",
                            claimTitle.text.toString(),
                            claimType,
                            "",
                            currentDate.toString(),
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
     * Open up image gallery
     */
    private fun selectImageFromGallery() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(
            Intent.createChooser(
                intent,
                "Please select..."
            ),
            GALLERY_REQUEST_CODE
        )
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.gi
         * @return A new instance of fragment.
         */
        private val TAG = ClaimEditFragment::class.simpleName
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