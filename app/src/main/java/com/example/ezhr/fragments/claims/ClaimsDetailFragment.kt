package com.example.ezhr.fragments.claims

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.example.ezhr.R
import com.example.ezhr.data.Claim
import com.example.ezhr.databinding.FragmentClaimsDetailBinding
import com.example.ezhr.viewmodel.ClaimDetailViewModel
import com.example.ezhr.viewmodel.ClaimDetailViewModelFactory

/**
 * A simple [Fragment] subclass.
 * Use the [ClaimsDetailFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ClaimsDetailFragment : Fragment() {
    private lateinit var item: Claim
    private var claimID = ""
    private var uploadedImg = ""

    // NavController variable
    private lateinit var navController: NavController
    private var _binding: FragmentClaimsDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ClaimDetailViewModel by activityViewModels {
        ClaimDetailViewModelFactory()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentClaimsDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        viewModel.claimID.observe(viewLifecycleOwner) {
            Log.d(TAG, "Claim ID from view model: $it")
            claimID = it
//            viewModel.uploadImage.observe(viewLifecycleOwner) { img ->
//                Log.d(TAG, "Uploaded Image file string from view model: $img")
//                uploadedImg = img
//            }
            if (viewModel.uploadImage.value != null) {
                uploadedImg = viewModel.uploadImage.value!!
                Log.d(TAG, "Uploaded Image file string: $uploadedImg")
            }
            getClaimDetails()
        }


        binding.imageButtonEdit.setOnClickListener {
            viewModel.setClaimID(claimID)
            viewModel.setUploadImage(uploadedImg)
            viewModel.setClaims(item)
            // TODO Navigate to edit fragment
            navController.navigate(R.id.action_claimsDetailFragment_to_claimsEditFragment)
        }

        binding.imageButtonDelete.setOnClickListener {
            val builder = AlertDialog.Builder(requireContext())
            builder.setMessage("Are you sure you want to cancel your claims?")
                .setCancelable(false)
                .setPositiveButton("Yes") { dialog, id ->
                    viewModel.deleteClaim(claimID, uploadedImg)

                    viewModel.deleteSuccess.observe(viewLifecycleOwner) {
                        if (it) {
                            Toast.makeText(
                                context,
                                "Claim application cancelled.",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                            navController.popBackStack()
                        } else {
                            Toast.makeText(
                                context,
                                "Failed to cancel claim application.",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }
                    }
                }
                .setNegativeButton("No") { dialog, id ->
                    dialog.dismiss()
                }
            val alert = builder.create()
            alert.show()
        }
    }

    /**
     * Retrieve and display details of current claim application
     */
    private fun getClaimDetails() {
        var claim: Claim = Claim()
        viewModel.claims.observe(viewLifecycleOwner) {
            claim = it
        }
        Log.d(TAG, "Get Claim: $claim")

        claim.let { claim ->
            item = claim
            binding.textViewClaimStatus.text = claim.status
            binding.textViewClaimTitle.text = claim.title
            binding.textViewClaimDate.text = claim.dateApplied
            binding.textViewClaimDesc.text = claim.desc
            binding.textViewClaimAmount.text =
                "$" + claim.amount.toString() + " (" + claim.claimType + ")"
            val url = claim.uploadedImg
            // If cannot use requireContext()
            Log.d(TAG, "Image URL: $url")
            Glide.with(this).load(url).into(binding.imageViewDocument)
            // Glide.with(requireActivity()).load(url).into(binding.imageViewDocument)

            if (claim.status != "PENDING") {
                binding.imageButtonEdit.isEnabled = false
                binding.imageButtonDelete.isEnabled = false

                binding.imageButtonEdit.background.setTint(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.uploadedColour
                    )
                )
                binding.imageButtonDelete.background.setTint(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.uploadedColour
                    )
                )
            } else {
                binding.imageButtonEdit.background.setTint(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.colorSecondary
                    )
                )
                binding.imageButtonDelete.background.setTint(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.colorSecondary
                    )
                )
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         */
        private val TAG = ClaimsDetailFragment::class.simpleName
        fun newInstance(): ClaimsDetailFragment {
            return ClaimsDetailFragment()
        }
    }
}