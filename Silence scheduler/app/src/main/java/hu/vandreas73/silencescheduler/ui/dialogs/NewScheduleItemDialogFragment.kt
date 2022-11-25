package hu.vandreas73.silencescheduler.ui.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.preference.PreferenceManager
import hu.vandreas73.silencescheduler.CalculatorNextWeekDay
import hu.vandreas73.silencescheduler.data.ScheduleItem
import hu.vandreas73.silencescheduler.R
import hu.vandreas73.silencescheduler.databinding.DialogNewScheduleItemBinding
import java.time.DayOfWeek


class NewScheduleItemDialogFragment : DialogFragment() {
    interface NewScheduleItemDialogListener {
        fun onScheduleItemCreated(newItem: ScheduleItem)
        fun onScheduleItemChanged(newItem: ScheduleItem)
    }

    private lateinit var listener: NewScheduleItemDialogListener

    private lateinit var binding: DialogNewScheduleItemBinding

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as NewScheduleItemDialogListener
        //?: throw RuntimeException("Activity must implement the NewShoppingItemDialogListener interface!")
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogNewScheduleItemBinding.inflate(LayoutInflater.from(context))
        binding.spMuteFromDay.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            resources.getStringArray(R.array.days),
        )
        binding.spMuteUntilDay.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            resources.getStringArray(R.array.days)
        )
        if (edit) {
            binding.spMuteFromDay.setSelection(item.muteFromDayOfWeek.ordinal)
            binding.spMuteUntilDay.setSelection(item.muteUntilDayOfWeek.ordinal)
            binding.tpMuteFrom.hour = item.muteFromHour
            binding.tpMuteFrom.minute = item.muteFromMin
            binding.tpMuteUntil.hour = item.muteUntilHour
            binding.tpMuteUntil.minute = item.muteUntilMin
        }

        val sp = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val hoursFormat24 = sp.getBoolean("24hour", true)
        binding.tpMuteUntil.setIs24HourView(hoursFormat24)
        binding.tpMuteFrom.setIs24HourView(hoursFormat24)

        return AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.new_period_dialog_title))
            .setView(binding.root)
            .setPositiveButton(getString(R.string.button_ok)) { _, _ ->
                okPressed()
            }
            .setNegativeButton(getString(R.string.button_cancel), null)
            .create()
    }

    private fun okPressed() {
        if (isValid()) {
            if (edit) {
                val alarmSetter = hu.vandreas73.silencescheduler.AlarmSetter(requireContext())
                val calc = CalculatorNextWeekDay()
                alarmSetter.cancelMuteAlarm(calc.getCalendar(item.muteFromDayOfWeek, item.muteFromHour, item.muteFromMin))
                alarmSetter.cancelUnmuteAlarm(calc.getCalendar(item.muteUntilDayOfWeek, item.muteUntilHour, item.muteUntilMin))

                val editedScheduleItem = getScheduleItem()
                editedScheduleItem.id = item.id
                listener.onScheduleItemChanged(editedScheduleItem)
                edit = false

                alarmSetter.setMuteAlarm(calc.getCalendar(editedScheduleItem.muteFromDayOfWeek, editedScheduleItem.muteFromHour, editedScheduleItem.muteFromMin))
                alarmSetter.setUnmuteAlarm(calc.getCalendar(editedScheduleItem.muteUntilDayOfWeek, editedScheduleItem.muteUntilHour, editedScheduleItem.muteUntilMin))
            } else
                listener.onScheduleItemCreated(getScheduleItem())
        }
    }

    private fun isValid() = true

    private fun getScheduleItem() = ScheduleItem(
        muteFromDayOfWeek = DayOfWeek.of(binding.spMuteFromDay.selectedItemPosition+1),
        muteUntilDayOfWeek = DayOfWeek.of(binding.spMuteUntilDay.selectedItemPosition+1),
        muteFromHour = binding.tpMuteFrom.hour,
        muteFromMin = binding.tpMuteFrom.minute,
        muteUntilHour = binding.tpMuteUntil.hour,
        muteUntilMin = binding.tpMuteUntil.minute
    )

    companion object {
        const val TAG = "NewShoppingItemDialogFragment"
    }

    var item: ScheduleItem = ScheduleItem(null, DayOfWeek.SUNDAY, 8, 45, DayOfWeek.SUNDAY, 10, 45)
    var edit: Boolean = false

    fun setParameters(scheduleItem: ScheduleItem) {

        this.item = scheduleItem
        edit = true
    }
}