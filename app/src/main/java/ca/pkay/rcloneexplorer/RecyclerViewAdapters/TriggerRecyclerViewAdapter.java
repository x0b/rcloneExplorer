package ca.pkay.rcloneexplorer.RecyclerViewAdapters;


import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import ca.pkay.rcloneexplorer.Database.DatabaseHandler;
import ca.pkay.rcloneexplorer.Items.Task;
import ca.pkay.rcloneexplorer.Items.Trigger;
import ca.pkay.rcloneexplorer.R;
import ca.pkay.rcloneexplorer.TriggerActivity;
import es.dmoral.toasty.Toasty;

public class TriggerRecyclerViewAdapter extends RecyclerView.Adapter<TriggerRecyclerViewAdapter.ViewHolder>{

    private List<Trigger> triggers;
    private final Context context;

    public TriggerRecyclerViewAdapter(List<Trigger> triggers, Context context) {
        this.triggers = triggers;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_trigger_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        final Trigger selectedTrigger = triggers.get(position);
        holder.triggerName.setText(selectedTrigger.getTitle());
        Task task = (new DatabaseHandler(context)).getTask(selectedTrigger.getId());
        String taskTitle = "ERR: NOTFOUND";
        if(task != null){
            taskTitle = task.getTitle();
        }
        holder.triggerTarget.setText(taskTitle);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, selectedTrigger.getTime()/60);
        calendar.set(Calendar.MINUTE, selectedTrigger.getTime()%60);

        DateFormat dateFormat = android.text.format.DateFormat.getTimeFormat(context);
        holder.time.setText(dateFormat.format(new Date(calendar.getTimeInMillis())));

        setTextViewValue(holder.mon, selectedTrigger.isEnabledAtDay(0));
        setTextViewValue(holder.tue, selectedTrigger.isEnabledAtDay(1));
        setTextViewValue(holder.wed, selectedTrigger.isEnabledAtDay(2));
        setTextViewValue(holder.thur, selectedTrigger.isEnabledAtDay(3));
        setTextViewValue(holder.fri, selectedTrigger.isEnabledAtDay(4));
        setTextViewValue(holder.sat, selectedTrigger.isEnabledAtDay(5));
        setTextViewValue(holder.sun, selectedTrigger.isEnabledAtDay(6));

        updateTriggerIcon(selectedTrigger,holder.triggerIcon);

        holder.triggerIcon.setOnClickListener(v-> {
            DatabaseHandler db = new DatabaseHandler(context);
            selectedTrigger.setEnabled(!selectedTrigger.isEnabled());
            db.updateTrigger(selectedTrigger);
            updateTriggerIcon(selectedTrigger, holder.triggerIcon);

            String message = context.getResources().getString(R.string.message_trigger_disabled);
            if(selectedTrigger.isEnabled()){
                message = context.getResources().getString(R.string.message_trigger_enabled);
            }
            Toasty.info(context, message).show();
        });

        holder.triggerOptions.setOnClickListener(v-> {
            showFileMenu(v, selectedTrigger);
        });

    }

    private void updateTriggerIcon(Trigger trigger, ImageButton button){
        Drawable d =ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_baseline_check_circle_outline_24, null);
        if(!trigger.isEnabled()){
            d =ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_baseline_block_24, null);
        }
        button.setImageDrawable(d);
    }

    private void setTextViewValue(TextView view, boolean disabled){
        if(!disabled){
            view.setPaintFlags(view.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }
    }
    public void setList(ArrayList<Trigger> data) {
        triggers=data;
        notifyDataSetChanged();
    }

    private void editTrigger(Trigger trigger){
        Intent intent = new Intent(context, TriggerActivity.class);
        intent.putExtra(TriggerActivity.ID_EXTRA, trigger.getId());
        context.startActivity(intent);
    }


    public void removeItem(Trigger trigger) {
        int index = triggers.indexOf(trigger);
        if (index >= 0) {
            triggers.remove(index);
            notifyItemRemoved(index);
        }
    }

    @Override
    public int getItemCount() {
        if (triggers == null) {
            return 0;
        }
        return triggers.size();
    }

    private void showFileMenu(View view, final Trigger trigger) {
        PopupMenu popupMenu = new PopupMenu(context, view);
        popupMenu.getMenuInflater().inflate(R.menu.trigger_item_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.action_edit_task:
                    editTrigger(trigger);
                    break;
                case R.id.action_delete_task:
                    new DatabaseHandler(context).deleteTrigger(trigger.getId());
                    notifyDataSetChanged();
                    removeItem(trigger);
                    break;
                default:
                    return false;
            }
            return true;
        });
        popupMenu.show();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        final View view;
        final ImageButton triggerIcon;
        final ImageButton triggerOptions;
        final TextView triggerName;
        final TextView time;
        final TextView triggerTarget;


        final TextView mon;
        final TextView tue;
        final TextView wed;
        final TextView thur;
        final TextView fri;
        final TextView sat;
        final TextView sun;

        ViewHolder(View itemView) {
            super(itemView);
            this.view = itemView;
            this.triggerIcon = view.findViewById(R.id.triggerIcon);
            this.triggerName = view.findViewById(R.id.triggerName);
            this.triggerOptions = view.findViewById(R.id.triggerOptions);
            this.triggerTarget = view.findViewById(R.id.trigger_target);
            this.time = view.findViewById(R.id.time_starttime);

            this.mon = view.findViewById(R.id.trigger_enabled_mon);
            this.tue = view.findViewById(R.id.trigger_enabled_tue);
            this.wed = view.findViewById(R.id.trigger_enabled_wed);
            this.thur = view.findViewById(R.id.trigger_enabled_thu);
            this.fri = view.findViewById(R.id.trigger_enabled_fri);
            this.sat = view.findViewById(R.id.trigger_enabled_sat);
            this.sun = view.findViewById(R.id.trigger_enabled_sun);
        }
    }
}
