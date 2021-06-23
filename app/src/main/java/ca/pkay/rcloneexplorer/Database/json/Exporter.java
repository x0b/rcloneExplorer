package ca.pkay.rcloneexplorer.Database.json;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ca.pkay.rcloneexplorer.Database.DatabaseHandler;
import ca.pkay.rcloneexplorer.Items.Task;
import ca.pkay.rcloneexplorer.Items.Trigger;

public class Exporter {

    public static String create(Context context) throws JSONException {

        DatabaseHandler dbHandler = new DatabaseHandler(context);

        JSONObject main = new JSONObject();
        JSONArray tasks = new JSONArray();

        for(Task task : dbHandler.getAllTasks()){
            JSONObject taskObject = new JSONObject();
            taskObject.put("id", task.getId());
            taskObject.put("name", task.getTitle());
            taskObject.put("remoteId", task.getRemoteId());
            taskObject.put("remotePath", task.getRemotePath());
            taskObject.put("remoteType", task.getRemoteType());
            taskObject.put("localPath", task.getLocalPath());
            taskObject.put("syncDirection", task.getDirection());
            tasks.put(taskObject);
        }
        main.put("tasks", tasks);

        JSONArray triggers = new JSONArray();

        for(Trigger trigger : dbHandler.getAllTrigger()){
            JSONObject triggerObject = new JSONObject();
            triggerObject.put("id", trigger.getId());
            triggerObject.put("time", trigger.getTime());
            triggerObject.put("title", trigger.getTitle());
            triggerObject.put("weekdays", trigger.getWeekdays());
            triggerObject.put("target", trigger.getWhatToTrigger());
            triggerObject.put("enabled", trigger.isEnabled());
            triggers.put(triggerObject);
        }
        main.put("trigger", triggers);
        return main.toString();
    }
}
