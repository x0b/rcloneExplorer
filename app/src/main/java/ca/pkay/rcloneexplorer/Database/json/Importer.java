package ca.pkay.rcloneexplorer.Database.json;


import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;

import ca.pkay.rcloneexplorer.Database.DatabaseHandler;
import ca.pkay.rcloneexplorer.Items.Task;
import ca.pkay.rcloneexplorer.Items.Trigger;

public class Importer {


    public static void importJson(String json, Context context) throws JSONException {
        DatabaseHandler dbHandler = new DatabaseHandler(context);
        dbHandler.deleteEveryting();
        for(Trigger trigger : createTriggerlist(json)){
            dbHandler.createTrigger(trigger);
        }
        for(Task task : createTasklist(json)){
            dbHandler.createTask(task);
        }
    }

    public static ArrayList<Trigger> createTriggerlist(String content) throws JSONException {
        ArrayList<Trigger> result = new ArrayList<>();
        JSONObject reader = new JSONObject(content);
        JSONArray array = reader.getJSONArray("trigger");
        for (int i = 0; i < array.length(); i++) {
            JSONObject triggerObject = array.getJSONObject(i);
            Trigger trigger = new Trigger(triggerObject.getLong("id"));
            trigger.setTitle(triggerObject.getString("title"));
            trigger.setTime(triggerObject.getInt("time"));
            trigger.setEnabled(triggerObject.getBoolean("enabled"));
            trigger.setWeekdays((byte) triggerObject.getInt("weekdays"));
            trigger.setWhatToTrigger(triggerObject.getLong("target"));
            result.add(trigger);
        }
        return result;
    }

    public static ArrayList<Task> createTasklist(String content) throws JSONException {
        ArrayList<Task> result = new ArrayList<>();
        JSONObject reader = new JSONObject(content);
        JSONArray array = reader.getJSONArray("tasks");
        for (int i = 0; i < array.length(); i++) {
            JSONObject taskObject = array.getJSONObject(i);
            Task task = new Task(taskObject.getLong("id"));
            task.setTitle(taskObject.getString("name"));
            task.setRemoteId(taskObject.getString("remoteId"));
            task.setRemotePath(taskObject.getString("remotePath"));
            task.setRemoteType(taskObject.getInt("remoteType"));
            task.setLocalPath(taskObject.getString("localPath"));
            task.setDirection(taskObject.getInt("syncDirection"));
            result.add(task);
        }
        return result;
    }
}
