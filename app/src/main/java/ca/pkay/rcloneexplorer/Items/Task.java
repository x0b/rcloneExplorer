package ca.pkay.rcloneexplorer.Items;

public class Task {

    public static String TABLE_NAME = "task_table";

    public static String COLUMN_NAME_ID= "task_id";
    public static String COLUMN_NAME_TITLE = "task_title";
    public static String COLUMN_NAME_REMOTE_ID = "task_remote_id";
    public static String COLUMN_NAME_REMOTE_TYPE = "task_remote_type";
    public static String COLUMN_NAME_REMOTE_PATH = "task_remote_path";
    public static String COLUMN_NAME_LOCAL_PATH = "task_local_path";
    public static String COLUMN_NAME_SYNC_DIRECTION = "task_direction";

    private Long id;

    private String title="";
    private String remoteId ="";
    private int remoteType =0;
    private String remotePath ="";
    private String localPath = "";
    private int direction=0;


    public Task(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getRemoteId() {
        return remoteId;
    }

    public void setRemoteId(String remoteId) {
        this.remoteId = remoteId;
    }

    public int getRemoteType() {
        return remoteType;
    }

    public void setRemoteType(int remoteType) {
        this.remoteType = remoteType;
    }

    public String getRemotePath() {
        return remotePath;
    }

    public void setRemotePath(String remotePath) {
        this.remotePath = remotePath;
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    public int getDirection() {
        return direction;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    public String toString(){
        return title + ": " + remoteId + ": " + remoteType + ": " + remotePath + ": " + localPath + ": " + direction;
    }
}
