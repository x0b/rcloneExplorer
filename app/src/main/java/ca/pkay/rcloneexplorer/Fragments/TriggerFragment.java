package ca.pkay.rcloneexplorer.Fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import ca.pkay.rcloneexplorer.Database.DatabaseHandler;
import ca.pkay.rcloneexplorer.Items.Trigger;
import ca.pkay.rcloneexplorer.R;
import ca.pkay.rcloneexplorer.RecyclerViewAdapters.TriggerRecyclerViewAdapter;
import ca.pkay.rcloneexplorer.Services.TriggerService;
import ca.pkay.rcloneexplorer.TriggerActivity;
import jp.wasabeef.recyclerview.animators.LandingAnimator;

public class TriggerFragment extends Fragment {

    private View fragmentView;
    private DatabaseHandler dbHandler;


    public TriggerFragment() {
        // Required empty public constructor
    }

    public static TriggerFragment newInstance() {
        return new TriggerFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((FragmentActivity) getContext()).setTitle(R.string.trigger);
        setHasOptionsMenu(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_trigger, container, false);
        fragmentView = view;

        populateTriggerList(fragmentView);

        if(dbHandler.getAllTasks().size() > 0 ){
            view.findViewById(R.id.layout_error).setVisibility(View.GONE);
            view.findViewById(R.id.layout_list).setVisibility(View.VISIBLE);
        } else {
            //disable all trigger when no tasks are available.
            TriggerService triggerService = new TriggerService(view.getContext());
            for(Trigger trigger : dbHandler.getAllTrigger()){
                triggerService.cancelTrigger(trigger.getId());
            }
        }

        view.findViewById(R.id.newTrigger).setOnClickListener(v -> {
            Intent intent = new Intent(view.getContext(), TriggerActivity.class);
            startActivity(intent);
        });

        view.findViewById(R.id.task_activity_button).setOnClickListener(v -> {
            Fragment fragment = new TasksFragment();
            FragmentManager fm = ((FragmentActivity) getContext()).getSupportFragmentManager();
            FragmentTransaction transaction = fm.beginTransaction();
            transaction.replace(R.id.flFragment, fragment);
            transaction.commit();
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        populateTriggerList(fragmentView);
    }

    private void populateTriggerList(View v){
        Context c = v.getContext();
        if(dbHandler == null){
            dbHandler = new DatabaseHandler(c);
        }
        RecyclerView recyclerView =  v.findViewById(R.id.trigger_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(c));
        recyclerView.setItemAnimator(new LandingAnimator());

        TriggerRecyclerViewAdapter recyclerViewAdapter = new TriggerRecyclerViewAdapter(dbHandler.getAllTrigger(), c);
        recyclerView.setAdapter(recyclerViewAdapter);
    }
}
