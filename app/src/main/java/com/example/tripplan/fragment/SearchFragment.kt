package com.example.tripplan.fragment
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.tripplan.R
import com.example.tripplan.board.BoardListLVAdapter
import com.example.tripplan.databinding.FragmentSearchGuesthouseBinding
import com.example.tripplan.detail.DetailFragment
import com.example.tripplan.repository.CategoryGuestHouse

class SearchFragment : Fragment() {
    private var _binding: FragmentSearchGuesthouseBinding? = null
    private val binding get() = _binding!!
    private val adapter by lazy {
        DataListAdapter { data ->
            detailFragment.arguments = Bundle().apply { putParcelable("guesthouse_data", data) }
            val transaction = parentFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container, detailFragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }
    }
    private val viewModel by viewModels<GuestMatchingViewModel>()
    private val detailFragment by lazy { DetailFragment() }
    private val categoryMap = mapOf(
        R.id.chip_jeju to CategoryGuestHouse.SEARCHKEYWORD,
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d("SearchFragment", "onCreateView called")
        _binding = FragmentSearchGuesthouseBinding.inflate(inflater, container, false)
        with(binding) {
            searchView.isIconified = false
            GuestHouseList.adapter = adapter // 바꿔야함
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    query ?: return true
                    val queryCategory =
                        categoryMap.get(chipGroup.checkedChipId) ?: CategoryGuestHouse.SEARCHKEYWORD
                    viewModel.getGuestHouseList(queryCategory, query)
                    searchView.hideKeyboard()
                    return true
                }

                override fun onQueryTextChange(newText: String?) = true
            })
        }
        viewModel.getGuestHouseList(CategoryGuestHouse.SEARCHKEYWORD, "게스트하우스") // 초기에 보여줄 화면
        viewModel.GuestHouseDataList.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }
}