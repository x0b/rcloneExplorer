package ca.pkay.rcloneexplorer.Fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import ca.pkay.rcloneexplorer.Database.DatabaseHandler;
import ca.pkay.rcloneexplorer.Items.Trigger;
import ca.pkay.rcloneexplorer.R;
import ca.pkay.rcloneexplorer.RecyclerViewAdapters.TasksRecyclerViewAdapter;
import ca.pkay.rcloneexplorer.Services.TriggerService;
import ca.pkay.rcloneexplorer.TaskActivity;
import es.dmoral.toasty.Toasty;
import jp.wasabeef.recyclerview.animators.LandingAnimator;

public class TasksFragment extends Fragment {

    private View fragmentView;

    public TasksFragment() {
        // Required empty public constructor
    }

    public static TasksFragment newInstance() {
        return new TasksFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((FragmentActivity) getContext()).setTitle(R.string.tasks);
        setHasOptionsMenu(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_tasks, container, false);
        fragmentView = view;
        populateTaskList(fragmentView);
        updateVisibilities(fragmentView);

        Intent intent = new Intent(view.getContext(), TaskActivity.class);
        view.findViewById(R.id.newTask).setOnClickListener(v -> {
            startActivity(intent);
        });
        view.findViewById(R.id.newTask_empty).setOnClickListener(v -> {
            startActivity(intent);
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        populateTaskList(fragmentView);
        updateVisibilities(fragmentView);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        Activity activity = getActivity();
        Context context = getContext();

        if(activity==null){
            Toasty.error(context, context.getResources().getString(R.string.importer_unknown_error), Toast.LENGTH_SHORT, true).show();
        }
    }

    private void updateVisibilities(View view){
        DatabaseHandler dbHandler = new DatabaseHandler(view.getContext());
        if(dbHandler.getAllTasks().size() > 0 ){
            view.findViewById(R.id.layout_error).setVisibility(View.GONE);
            view.findViewById(R.id.layout_tasklist).setVisibility(View.VISIBLE);
        }
    }

    private void populateTaskList(View v){
        Context c = v.getContext();
        DatabaseHandler dbHandler = new DatabaseHandler(c);

        RecyclerView recyclerView =  v.findViewById(R.id.task_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(c));
        recyclerView.setItemAnimator(new LandingAnimator());

        TasksRecyclerViewAdapter recyclerViewAdapter = new TasksRecyclerViewAdapter(dbHandler.getAllTasks(), c);
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerView.addOnChildAttachStateChangeListener(new RecyclerView.OnChildAttachStateChangeListener() {
            @Override public void onChildViewAttachedToWindow(final View view) { updateVisibilities(fragmentView); }
            @Override public void onChildViewDetachedFromWindow(View view) { updateVisibilities(fragmentView); }
        });
    }
}
