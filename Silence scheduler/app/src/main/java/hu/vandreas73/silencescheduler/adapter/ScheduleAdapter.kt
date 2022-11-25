package hu.vandreas73.silencescheduler.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import hu.vandreas73.silencescheduler.CalculatorNextWeekDay
import hu.vandreas73.silencescheduler.R
import hu.vandreas73.silencescheduler.data.ScheduleItem
import hu.vandreas73.silencescheduler.databinding.ItemScheduleListBinding
import hu.vandreas73.silencescheduler.ui.dialogs.NewScheduleItemDialogFragment
import hu.vandreas73.silencescheduler.ui.RecurringFragment


class ScheduleAdapter(private val listener: ScheduleItemClickListener, val recurringFragment: RecurringFragment) :
    RecyclerView.Adapter<ScheduleAdapter.ScheduleViewHolder>() {

    private val items = mutableListOf<ScheduleItem>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ScheduleViewHolder(
        ItemScheduleListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
        val scheduleItem = items[position]

        val d = recurringFragment.requireActivity().resources.getStringArray(R.array.days)
        holder.binding.tvMuteFrom.text = "${d[scheduleItem.muteFromDayOfWeek.value-1]} ${String.format("%02d", scheduleItem.muteFromHour)}:${String.format("%02d", scheduleItem.muteFromMin)}"
        holder.binding.tvMuteUntil.text = "${d[scheduleItem.muteUntilDayOfWeek.value-1]} ${String.format("%02d", scheduleItem.muteUntilHour)}:${String.format("%02d", scheduleItem.muteUntilMin)}"

        holder.binding.ibRemove.setOnClickListener {
            items.removeAt(position)
            listener.removeItem(scheduleItem)
            notifyItemRemoved(position)
        }

        holder.binding.ibEdit.setOnClickListener {
            val fragment = NewScheduleItemDialogFragment()
            scheduleItem.id = items[position].id
            fragment.setParameters(scheduleItem)
            fragment.show(
                recurringFragment.requireActivity().supportFragmentManager,
                NewScheduleItemDialogFragment.TAG
            )
        }

    }

    fun removeItem(item: ScheduleItem){
        val index = items.indexOf(item)
        items.remove(item)
        val alarmSetter = hu.vandreas73.silencescheduler.AlarmSetter(recurringFragment.requireContext())
        val calc = CalculatorNextWeekDay()
        val muteFrom = calc.getCalendar(item.muteFromDayOfWeek, item.muteFromHour, item.muteFromMin)
        alarmSetter.cancelMuteAlarm(muteFrom)
        val muteUntil = calc.getCalendar(item.muteUntilDayOfWeek, item.muteUntilHour, item.muteUntilMin)
        alarmSetter.cancelUnmuteAlarm(muteUntil)

        recurringFragment.requireActivity().runOnUiThread{
            notifyDataSetChanged()
        }
    }

    private fun getCalendar(dayOfYear: Int, hourOfDay: Int, minute: Int){

    }

    fun removeAll(){
        items.clear()
    }


    override fun getItemCount(): Int = items.size

    interface ScheduleItemClickListener {
        fun onItemChanged(item: ScheduleItem)

        fun removeItem(item: ScheduleItem)
    }

    inner class ScheduleViewHolder(val binding: ItemScheduleListBinding) : RecyclerView.ViewHolder(binding.root)

    fun addItem(item: ScheduleItem) {
        items.add(item)
        notifyItemInserted(items.size - 1)
    }

    fun update(scheduleItems: List<ScheduleItem>) {
        items.clear()
        items.addAll(scheduleItems)
        recurringFragment.requireActivity().runOnUiThread{
            notifyDataSetChanged()
        }
    }

}