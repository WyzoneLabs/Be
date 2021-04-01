package utils;

import java.util.Comparator;

import model.Friend;

public class FriendSorter implements Comparator<Friend> {
    @Override
    public int compare(Friend o1, Friend o2) {
        return o1.name.compareToIgnoreCase(o2.name);
    }
}
