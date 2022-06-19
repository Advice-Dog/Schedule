package com.shortstack.hackertracker.ui.information.categories

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.shortstack.hackertracker.databinding.FragmentCategoryBinding
import com.shortstack.hackertracker.models.local.Type
import com.shortstack.hackertracker.ui.events.EventDetailsAdapter

class CategoryFragment : Fragment() {

    private var _binding: FragmentCategoryBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }

        val type = arguments?.getParcelable<Type>(EXTRA_TYPE) ?: error("type must not be null")
        showType(type)
    }

    private fun showType(type: Type) {
        binding.toolbar.title = type.fullName

        val body = type.description

        if (body.isNotBlank()) {
            // todo: binding.empty.visibility = View.GONE
            binding.description.text = body
        } else {
            // todo: binding.empty.visibility = View.VISIBLE
        }

        val adapter = EventDetailsAdapter()
        adapter.setElements(type.actions, emptyList())
        binding.links.adapter = adapter
        val gridLayoutManager = binding.links.layoutManager as GridLayoutManager

        gridLayoutManager.spanSizeLookup =
            object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return adapter.getSpanSize(position, gridLayoutManager.spanCount)
                }
            }
    }

    companion object {
        private const val EXTRA_TYPE = "EXTRA_CATEGORY"

        fun newInstance(type: Type): CategoryFragment {
            val fragment = CategoryFragment()

            val bundle = Bundle()
            bundle.putParcelable(EXTRA_TYPE, type)
            fragment.arguments = bundle

            return fragment
        }
    }
}