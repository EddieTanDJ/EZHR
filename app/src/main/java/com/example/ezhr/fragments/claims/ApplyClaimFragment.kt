package com.example.ezhr.fragments.claims

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
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
import com.example.ezhr.databinding.FragmentApplyClaimBinding
import com.example.ezhr.repository.ClaimApplicationManager
import com.example.ezhr.repository.Draft
import com.example.ezhr.viewmodel.ApplyClaimViewModel
import com.example.ezhr.viewmodel.ApplyClaimViewModelFactory
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.text.FirebaseVisionText
import com.opencsv.CSVReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/**
 * A simple [Fragment] subclass.
 * Use the [ApplyClaimFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ApplyClaimFragment : Fragment() {
    lateinit var claimManager: ClaimApplicationManager
    var userID = Firebase.auth.currentUser?.uid
    private var claimTypeIndex = 0

    private val GALLERY_REQUEST_CODE = 234
    private lateinit var uploadedFileURI: Uri
    private lateinit var addDocumentBtn: ImageButton
    private lateinit var cancelBtn: ImageView
    private lateinit var imageText: TextView

    private lateinit var claimTitle: EditText
    private lateinit var claimAmt: EditText
    private lateinit var claimDesc: EditText
    private lateinit var spinner: Spinner

    private var uploadedFileName = ""

    private var foodList = mutableListOf<String>()
    private var transportList = mutableListOf<String>()
    private var wordList = mutableListOf<String>()
    private var categoryCount: MutableList<Int> = mutableListOf(0, 0, 0, 0, 0)

    // NavController variable
    private lateinit var navController: NavController
    private var _binding: FragmentApplyClaimBinding? = null
    private val binding get() = _binding!!

    // view model
    private val viewModel: ApplyClaimViewModel by activityViewModels {
        val application = requireActivity().application
        val app = application as EZHRApp
        ApplyClaimViewModelFactory(app.applyClaimRepo)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentApplyClaimBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SimpleDateFormat")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "Apply Claim Activity Created")
        claimManager = ClaimApplicationManager(requireContext())
        navController = Navigation.findNavController(view)
        spinner = binding.spinner

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
        }

        claimTitle = binding.editTextClaimTitle
        claimAmt = binding.editTextAmount
        claimDesc = binding.editTextDescription
        imageText = binding.textViewUploadedFile

        getDraft()
        getDatasets()

        binding.buttonCamera.setOnClickListener {
            pickImage()
        }

        addDocumentBtn = binding.buttonAddDocument
        addDocumentBtn.setOnClickListener {
            selectImageFromGallery()
        }

        binding.buttonSave.setOnClickListener {
            val claimType = spinner.selectedItem.toString()
            val claimTitle = claimTitle.text.toString()

            var amount: Double
            if (claimAmt.text.toString() != "") {
                amount = claimAmt.text.toString().toDouble()
            } else {
                amount = 0.0
            }

            val desc = claimDesc.text.toString()

            GlobalScope.launch(Dispatchers.IO) {
                claimManager.savetoDataStore(
                    claim = Draft(
                        claimType = claimType,
                        title = claimTitle,
                        amount = amount,
                        desc = desc,
                        uploadedImg = uploadedFileName,
                    )
                )
            }
            Toast.makeText(context, "Draft Saved.", Toast.LENGTH_SHORT).show()
        }

        cancelBtn = binding.imageViewCancel
        cancelBtn.setOnClickListener {
            markButtonEnabled(addDocumentBtn)
        }

        binding.buttonSubmit.setOnClickListener {
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
                viewModel.getCurrentClaimBalance(claimType).observe(viewLifecycleOwner) {
                    if (claimAmt.text.toString().toDouble() <= it) {
                        var claim = Claim(
                            userID,
                            claimTitle.text.toString(),
                            claimType,
                            "PENDING",
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

                        viewModel.uploadClaimApplication(
                            uri,
                            claim,
                            uploadedFileName
                        ).observe(viewLifecycleOwner) {
                            if (it) {
                                Toast.makeText(
                                    context,
                                    "Claim application submitted.",
                                    Toast.LENGTH_LONG
                                ).show()
                                navController.navigate(R.id.action_applyClaimFragment_to_claimsFragment)
                            } else {
                                Toast.makeText(
                                    context,
                                    "Failed to submit claim application.",
                                    Toast.LENGTH_LONG
                                )
                                    .show()
                            }
                            // TODO: Change it to other scope. Is discourage to use global scope
                            GlobalScope.launch(Dispatchers.IO) {
                                claimManager.clearDataStore()
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
     * Load all data from each claim category csv into respective lists
     */
    private fun getDatasets() {
        var foodRows: List<Array<String?>> = ArrayList()
        var transportRows: List<Array<String?>> = ArrayList()
        val csvReader1 = CSVReader(
            FileReader(
                getFileFromAssets(
                    context = requireContext(),
                    fileName = "foods.csv"
                )
            )
        )
        val csvReader2 = CSVReader(
            FileReader(
                getFileFromAssets(
                    context = requireContext(),
                    fileName = "transportations.csv"
                )
            )
        )

        try {
            foodRows = csvReader1.readAll()
            transportRows = csvReader2.readAll()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        for (i in foodRows.indices) {
            foodList.add(foodRows[i][0].toString().lowercase())
        }

        for (i in transportRows.indices) {
            transportList.add(transportRows[i][0].toString())
        }

        Log.d(TAG, foodList.toString())
        Log.d(TAG, transportList.toString())
    }

    /**
     * Get specific filepath of a file in the assets folder
     */
    @Throws(IOException::class)
    fun getFileFromAssets(context: Context, fileName: String): File =
        File(context.cacheDir, fileName)
            .also {
                if (!it.exists()) {
                    it.outputStream().use { cache ->
                        context.assets.open(fileName).use { inputStream ->
                            inputStream.copyTo(cache)
                        }
                    }
                }
            }

    /**
     * Extract all text from selected image using Firebase ML
     */
    private fun processImage() {
        if (::uploadedFileURI.isInitialized) {
            Toast.makeText(context, "Processing image...", Toast.LENGTH_LONG).show()
            val bitmap =
                MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uploadedFileURI)
            val image = FirebaseVisionImage.fromBitmap(bitmap)
            val detector = FirebaseVision.getInstance().onDeviceTextRecognizer

            detector.processImage(image)
                .addOnSuccessListener { firebaseVisionText ->
                    processResultText(firebaseVisionText)
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(context, "Select an image first", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Get image from documents
     */
    private fun pickImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1)
    }

    /**
     * Populate (type, title, amount) input fields based on text taken from image
     */
    private fun processResultText(resultText: FirebaseVisionText) {
        spinner.setSelection(0)
        claimTitle.text.clear()
        claimAmt.text.clear()

        claimTitle.error = null
        claimAmt.error = null
        claimDesc.error = null

        if (resultText.textBlocks.size == 0) {
            Toast.makeText(context, "No text found.", Toast.LENGTH_SHORT).show()
            return
        }
        for (i in resultText.textBlocks.indices) {
            var textBlock = resultText.textBlocks[i].text.lowercase()
            wordList.add(textBlock)

            binding.editTextClaimTitle.setText(resultText.textBlocks[0].text)

            if (textBlock.lowercase().contains("total")) {
                if (i != resultText.textBlocks.size - 1) {
                    var amount = resultText.textBlocks[i + 1].text.replace(",", ".")
                    amount = amount.replace("$", "")
                    binding.editTextAmount.setText(amount)
                }
            }

            for (word in wordList) {
                if (foodList.contains(word) || transportList.contains(word)) {
                    classifyText(word)
                    Log.d(TAG, word)
                }
            }
        }
        Log.d(TAG, "Word List: $wordList")

        categoryCount.clear()
        categoryCount = mutableListOf(0, 0, 0, 0, 0)
        wordList.clear()
    }


    /**
     * Assign claim category based on text classification
     */
    private fun classifyText(text: String) {
        val class1Probability = findProbabilityGivenSample(text, foodList)
        val class2Probability = findProbabilityGivenSample(text, transportList)

        if (class1Probability > class2Probability) {
            categoryCount[2] += 1
            Log.d(TAG, "Category: Food")
        } else {
            categoryCount[1] += 1
            Log.d(TAG, "Category: Transportation")
        }

        val topCategory = categoryCount.indexOf(Collections.max(categoryCount))
        Log.d(TAG, categoryCount.toString())
        spinner.setSelection(topCategory)
    }

    private fun findProbabilityGivenSample(word: String, classVocab: List<String>): Float {
        var probabilityGivenClass = 1.0f
        probabilityGivenClass *= findProbabilityGivenClass(word, classVocab)

        return probabilityGivenClass
    }

    private fun findProbabilityGivenClass(x: String, classVocab: List<String>): Float {
        val xCount = classVocab.count { it.contains(x) or x.contains(it) }.toFloat()
        val classCount = classVocab.count().toFloat()
        return ((xCount / classCount) + 1)
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

        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                setCurrentFileURI(data.data!!)
                processImage()
                markButtonDisabled(addDocumentBtn, data.data!!)
            }
        }
    }

    /**
     * Get saved claim application draft from datastore preferences (ClaimApplicationManager)
     */
    private fun getDraft() {
        GlobalScope.launch(
            Dispatchers.IO
        ) {
            claimManager.getFromDataStore().catch { e ->
                e.printStackTrace()
            }.collect {
                withContext(Dispatchers.Main) {
                    claimTitle.setText(it.title)
                    claimDesc.setText(it.desc)

                    if (it.amount != 0.0) {
                        claimAmt.setText(it.amount.toString())
                    }

                    if (it.uploadedImg != "") {
                        uploadedFileName = it.uploadedImg.toString()
                        markButtonDisabled(addDocumentBtn, null)
                    }

                    if (it.claimType == "Medical") {
                        claimTypeIndex = 0
                    } else if (it.claimType == "Transportation") {
                        claimTypeIndex = 1
                    } else if (it.claimType == "Food") {
                        claimTypeIndex = 2
                    } else {
                        claimTypeIndex = 3
                    }
                    spinner.setSelection(claimTypeIndex)
                }
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

            viewModel.getFileName(fileUri, context).observe(viewLifecycleOwner) {
                uploadedFileName = it.toString()

                val charDiff = uploadedFileName.count() - 30
                if (uploadedFileName.count() > 30) {
                    val extension = File(uploadedFileName).extension
                    val end = uploadedFileName.indexOf(".") - charDiff + extension.count()
                    uploadedFileName = uploadedFileName.substring(0, end) + ".$extension"
                }

                imageText.text = uploadedFileName
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

        imageText.text = ""
        uploadedFileName = ""
        cancelBtn.visibility = View.INVISIBLE
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
         * this fragment using the provided parameters.
         * @return A new instance of fragment.
         */
        private val TAG = ApplyClaimFragment::class.simpleName
        fun newInstance(): ApplyClaimFragment {
            return ApplyClaimFragment()
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