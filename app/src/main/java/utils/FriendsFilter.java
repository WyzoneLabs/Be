package utils;

import android.widget.Filter;

import java.util.ArrayList;
import java.util.List;

import adapter.ChatsAdapter;
import adapter.FriendsAdapter;
import model.Chat;
import model.Friend;

public class FriendsFilter extends Filter {

	private ChatsAdapter chatsAdapter;
	private FriendsAdapter friendsAdapter;
	private List<Friend> filterListFriends;
	private ArrayList<Chat> filterListChats;

	public FriendsFilter(List<Friend> filterList, FriendsAdapter adapter) {
		this.friendsAdapter = adapter;
		this.filterListFriends = filterList;
	}

	public FriendsFilter(ArrayList<Chat> filterList, ChatsAdapter adapter) {
		this.chatsAdapter = adapter;
		this.filterListChats = filterList;
	}

	//FILTERING OCURS
	@Override
	protected FilterResults performFiltering(CharSequence constraint) {
		FilterResults results = new FilterResults();

		//CHECK CONSTRAINT VALIDITY
        if (constraint != null) {
            //CHANGE TO UPPER
            constraint = constraint.toString().toUpperCase();
            //STORE OUR FILTERED PLAYERS
	        if(filterListChats != null && filterListChats.size() > 0) {
		        ArrayList<Chat> filteredChats = new ArrayList<>();
		        for (int i = 0; i < filterListChats.size(); i++) {
			        //CHECK
			        if (filterListChats.get(i).message.toUpperCase().contains(constraint) ||
					        filterListChats.get(i).friend.name.toUpperCase().contains(constraint)||
					        filterListChats.get(i).friend.phone.toUpperCase().contains(constraint)) {
				        //ADD PLAYER TO FILTERED PLAYERS
				        filteredChats.add(filterListChats.get(i));
			        }
		        }

		        results.count = filteredChats.size();
		        results.values = filteredChats;
	        }else if (filterListFriends != null && filterListFriends.size() > 0){
		        List<Friend> filteredPlayers = new ArrayList<>();
		        for (int i = 0; i < filterListFriends.size(); i++) {
			        //CHECK
			        if (filterListFriends.get(i).name.toUpperCase().contains(constraint) ||
					        filterListFriends.get(i).phone.toUpperCase().contains(constraint)) {
				        //ADD PLAYER TO FILTERED PLAYERS
				        filteredPlayers.add(filterListFriends.get(i));
			        }
		        }

		        results.count = filteredPlayers.size();
		        results.values = filteredPlayers;
	        }
        } else {
	        if(filterListChats != null && filterListChats.size() > 0) {
		        results.count = filterListChats.size();
		        results.values = filterListChats;
	        }else if (filterListFriends != null && filterListFriends.size() > 0){
		        results.count = filterListFriends.size();
		        results.values = filterListFriends;
	        }

        }

        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {

		if (chatsAdapter != null) {
			chatsAdapter.mChats = (ArrayList<Chat>) results.values;
			//REFRESH
			chatsAdapter.notifyDataSetChanged();
		}else if (friendsAdapter != null){
            friendsAdapter.mFriends = (List<Friend>) results.values;
            //REFRESH
            friendsAdapter.notifyDataSetChanged();
        }

	}
}
