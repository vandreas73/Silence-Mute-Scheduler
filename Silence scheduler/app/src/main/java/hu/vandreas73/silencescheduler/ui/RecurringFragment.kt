package hu.vandreas73.silencescheduler.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import hu.vandreas73.silencescheduler.MainActivity
import hu.vandreas73.silencescheduler.accessManager.AllAccessManager
import hu.vandreas73.silencescheduler.adapter.ScheduleAdapter
import hu.vandreas73.silencescheduler.data.ScheduleItem
import hu.vandreas73.silencescheduler.data.ScheduleListDatabase
import hu.vandreas73.silencescheduler.databinding.FragmentRecurringBinding
import hu.vandreas73.silencescheduler.ui.dialogs.NewScheduleItemDialogFragment
import kotlin.concurrent.thread

class RecurringFragment : Fragment() , ScheduleAdapter.ScheduleItemClickListener {

    private var _binding: FragmentRecurringBinding? = null

    private val binding get() = _binding!!

    private lateinit var database: ScheduleListDatabase
    private lateinit var adapter: ScheduleAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentRecurringBinding.inflate(inflater, container, false)
        val root: View = binding.root

        database = ScheduleListDatabase.getDatabase(requireContext())
        binding.fab.setOnClickListener {
            if(AllAccessManager(requireContext()).checkAndRequestPermissions()) {
                NewScheduleItemDialogFragment().show(parentFragmentManager, NewScheduleItemDialogFragment.TAG)
            }
        }

        initRecyclerView()

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun removeAll() {
        thread {
            val items = database.shoppingItemDao().getAll()
            for (item in items)
                database.shoppingItemDao().deleteItem(item)
            adapter.removeAll()
            loadItemsInBackground()
        }
    }

    private fun initRecyclerView() {
        adapter = ScheduleAdapter(this, this)
        binding.rvMain.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMain.adapter = adapter
        loadItemsInBackground()
        MainActivity.setAdapter(adapter)
    }

    private fun loadItemsInBackground() {
        thread {
            val items = database.shoppingItemDao().getAll()
            adapter.update(items)
        }
    }

    override fun onItemChanged(item: ScheduleItem) {
        thread {
            database.shoppingItemDao().update(item)
        }
    }

    override fun removeItem(item: ScheduleItem) {
        thread {
            database.shoppingItemDao().deleteItem(item)
            activity?.runOnUiThread{
                adapter.removeItem(item)
            }
        }
    }

}