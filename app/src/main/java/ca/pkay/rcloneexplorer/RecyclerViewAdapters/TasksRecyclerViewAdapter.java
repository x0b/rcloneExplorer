package ca.pkay.rcloneexplorer.RecyclerViewAdapters;


import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ca.pkay.rcloneexplorer.Database.DatabaseHandler;
import ca.pkay.rcloneexplorer.Items.Task;
import ca.pkay.rcloneexplorer.Items.RemoteItem;
import ca.pkay.rcloneexplorer.Items.SyncDirectionObject;
import ca.pkay.rcloneexplorer.R;
import ca.pkay.rcloneexplorer.Services.SyncService;
import ca.pkay.rcloneexplorer.Services.TaskStartService;
import ca.pkay.rcloneexplorer.TaskActivity;
import es.dmoral.toasty.Toasty;

public class TasksRecyclerViewAdapter extends RecyclerView.Adapter<TasksRecyclerViewAdapter.ViewHolder>{

    private static final String clipboardID = "rclone_explorer_task_id";

    private List<Task> tasks;
    private View view;
    private final Context context;


    public TasksRecyclerViewAdapter(List<Task> tasks, Context context) {
        this.tasks = tasks;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_tasks_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        final Task selectedTask = tasks.get(position);
        String remoteName = selectedTask.getTitle();

        holder.taskName.setText(remoteName);

        RemoteItem remote = new RemoteItem(selectedTask.getRemoteId(), String.valueOf(selectedTask.getRemoteType()));

        holder.taskIcon.setImageDrawable(AppCompatResources.getDrawable(context, remote.getRemoteIcon(selectedTask.getRemoteType())));

        int direction = selectedTask.getDirection();

        if(direction == SyncDirectionObject.SYNC_LOCAL_TO_REMOTE || direction == SyncDirectionObject.COPY_LOCAL_TO_REMOTE){
            holder.fromID.setVisibility(View.GONE);
            holder.fromPath.setText(selectedTask.getLocalPath());

            holder.toID.setText(String.format("%s:", selectedTask.getRemoteId()));
            holder.toPath.setText(selectedTask.getRemotePath());
        }

        if(direction == SyncDirectionObject.SYNC_REMOTE_TO_LOCAL || direction == SyncDirectionObject.COPY_REMOTE_TO_LOCAL){
            holder.fromID.setText(String.format("%s:", selectedTask.getRemoteId()));
            holder.fromPath.setText(selectedTask.getRemotePath());

            holder.toID.setVisibility(View.GONE);
            holder.toPath.setText(selectedTask.getLocalPath());
        }

        switch (direction){
            case SyncDirectionObject.COPY_LOCAL_TO_REMOTE:
            case SyncDirectionObject.COPY_REMOTE_TO_LOCAL:
                holder.taskSyncDirection.setText(view.getResources().getString(R.string.copy));
                break;
            case SyncDirectionObject.SYNC_REMOTE_TO_LOCAL:
            default:
                holder.taskSyncDirection.setText(view.getResources().getString(R.string.sync));
                break;
        }

        holder.fileOptions.setOnClickListener(v-> {
            showFileMenu(v, selectedTask);
        });

    }

    public void addTask(Task data) {
        tasks.add(data);
        notifyDataSetChanged();
    }

    public void setList(ArrayList<Task> data) {
        tasks = data;
        notifyDataSetChanged();
    }

    private void startTask(Task task){
        String path = task.getLocalPath();
        RemoteItem ri = new RemoteItem(task.getRemoteId(), task.getRemoteType(), "");
        Intent intent = new Intent(context, SyncService.class);
        intent.putExtra(SyncService.REMOTE_ARG, ri);
        intent.putExtra(SyncService.LOCAL_PATH_ARG, path);
        intent.putExtra(SyncService.SYNC_DIRECTION_ARG, task.getDirection());
        intent.putExtra(SyncService.REMOTE_PATH_ARG, task.getRemotePath());
        context.startService(intent);
    }

    private void editTask(Task task) {
        Intent intent = new Intent(context, TaskActivity.class);
        intent.putExtra(TaskActivity.ID_EXTRA, task.getId());
        context.startActivity(intent);
    }


    public void removeItem(Task task) {
        int index = tasks.indexOf(task);
        if (index >= 0) {
            tasks.remove(index);
            notifyItemRemoved(index);
        }
    }

    @Override
    public int getItemCount() {
        if (tasks == null) {
            return 0;
        }
        return tasks.size();
    }

    private void showFileMenu(View view, final Task task) {
        PopupMenu popupMenu = new PopupMenu(context, view);
        popupMenu.getMenuInflater().inflate(R.menu.task_item_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.action_start_task:
                    startTask(task);
                    break;
                case R.id.action_edit_task:
                    editTask(task);
                    break;
                case R.id.action_delete_task:
                    new DatabaseHandler(context).deleteTask(task.getId());
                    notifyDataSetChanged();
                    removeItem(task);
                    break;
                case R.id.action_copy_id_task:
                    ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText(clipboardID, task.getId().toString());
                    clipboard.setPrimaryClip(clip);
                    Toasty.info(context, context.getResources().getString(R.string.task_copied_id_to_clipboard), Toast.LENGTH_SHORT, true).show();
                    break;
                case R.id.action_add_to_home_screen:
                    createShortcut(context, task);
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
        final ImageView taskIcon;
        final TextView taskName;
        final TextView toID;
        final TextView fromID;
        final TextView toPath;
        final TextView fromPath;
        final ImageButton fileOptions;
        final TextView taskSyncDirection;

        ViewHolder(View itemView) {
            super(itemView);
            this.view = itemView;
            this.taskIcon = view.findViewById(R.id.taskIcon);
            this.taskName = view.findViewById(R.id.taskName);
            this.toID = view.findViewById(R.id.toID);
            this.fromID = view.findViewById(R.id.fromID);
            this.toPath = view.findViewById(R.id.toPath);
            this.fromPath = view.findViewById(R.id.fromPath);
            this.taskSyncDirection = view.findViewById(R.id.task_sync_direction);
            this.fileOptions = view.findViewById(R.id.file_options);
        }
    }

    private static void createShortcut(Context c, Task task) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ShortcutManager shortcutManager = c.getSystemService(ShortcutManager.class);

            Intent i = new Intent(c, TaskStartService.class);
            i.putExtra("task", task.getId());
            i.setAction(TaskStartService.TASK_ACTION);

            ShortcutInfo shortcut = new ShortcutInfo.Builder(c, String.valueOf(task.getId()))
                    .setShortLabel(task.getTitle())
                    .setLongLabel(task.getRemotePath())
                    .setIcon(Icon.createWithResource(c, R.mipmap.ic_launcher))
                    .setIntent(i)
                    .build();

            //https://developer.android.com/guide/topics/ui/shortcuts/creating-shortcuts
            shortcutManager.setDynamicShortcuts(Arrays.asList(shortcut));

            if (shortcutManager.isRequestPinShortcutSupported()) {
                // Assumes there's already a shortcut with the ID "my-shortcut".
                // The shortcut must be enabled.

                // Create the PendingIntent object only if your app needs to be notified
                // that the user allowed the shortcut to be pinned. Note that, if the
                // pinning operation fails, your app isn't notified. We assume here that the
                // app has implemented a method called createShortcutResultIntent() that
                // returns a broadcast intent.
                Intent pinnedShortcutCallbackIntent = shortcutManager.createShortcutResultIntent(shortcut);

                // Configure the intent so that your app's broadcast receiver gets
                // the callback successfully.For details, see PendingIntent.getBroadcast().
                PendingIntent successCallback = PendingIntent.getBroadcast(c, 0, pinnedShortcutCallbackIntent, 0);

                shortcutManager.requestPinShortcut(shortcut, successCallback.getIntentSender());
            }

        }
    }

}
