package io.matic.websocketchat.Fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import io.matic.websocketchat.Entity.Conversation;
import io.matic.websocketchat.MainActivity;
import io.matic.websocketchat.R;
import io.matic.websocketchat.Recycler.RecyclerViewAdapterConversations;
import io.matic.websocketchat.WebSocketChatClient;


/**
 * Created by Matic on 13/09/2017.
 */

// fragment is a view inside tabbed activity
public class ConversationsFragment extends Fragment{
    private RecyclerView recyclerView;
    private static RecyclerViewAdapterConversations recyclerViewAdapter;
    private ArrayList<Conversation> conversations;

    // set adapter for the recycler view
    public static RecyclerViewAdapterConversations getRecyclerViewAdapter() {
        return recyclerViewAdapter;
    }

    public ConversationsFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        // inflate view
        View rootView = inflater.inflate(R.layout.fragment_conversations, container, false);
        // set empty list and fragment view
        conversations = new ArrayList<>();
        recyclerView = (RecyclerView) rootView.findViewById(R.id.conversationsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));
        recyclerViewAdapter = new RecyclerViewAdapterConversations(this.getActivity(), conversations);
        recyclerView.setAdapter(recyclerViewAdapter);
        // return the view of fragment
        return rootView;
    }

    // after activity and view are created
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // prepare json objects to request all previous conversations
        JSONObject requestActiveConversations = new JSONObject();
        JSONObject userFrom = new JSONObject();
        try {
            requestActiveConversations.put("flag", "requestActiveConversations");
            userFrom.put("id", MainActivity.getCurrentUser().getUserID());
            userFrom.put("email", MainActivity.getCurrentUser().getEmail());
            userFrom.put("fullname", MainActivity.getCurrentUser().getFullName());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if(WebSocketChatClient.getInstance() != null) {
            WebSocketChatClient.getInstance().send(requestActiveConversations.toString());
        } else {
            // notify to reconnect
            Toast.makeText(MainActivity.getInstance().getApplicationContext(),
                    "You are disconnected. Please reconnect!", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public String toString() {
        return "Conversations";
    }
}
