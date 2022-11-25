package hu.vandreas73.silencescheduler.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import hu.vandreas73.silencescheduler.databinding.FragmentMuteNowBinding

class MuteNowFragment : Fragment() {

    private var _binding: FragmentMuteNowBinding? = null

    private val binding get() = _binding!!

    interface MainListener {
        fun mute(hour: Int, minute: Int)
        fun unmute()
    }
    private lateinit var listener: MainListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as? MainListener
            ?: throw RuntimeException("Activity must implement the NewShoppingItemDialogListener interface!")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMuteNowBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.btnMute.setOnClickListener{
            listener.mute(binding.timePicker1.hour, binding.timePicker1.minute)
        }

        binding.btnUnmute.setOnClickListener{
            listener.unmute()
        }

        val sp = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val hoursFormat24 = sp.getBoolean("24hour", true)
        binding.timePicker1.setIs24HourView(hoursFormat24)

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}