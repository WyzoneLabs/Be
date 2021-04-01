package ui.viewholder;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.brimbay.be.R;
import com.tomash.androidcontacts.contactgetter.entity.ContactData;
import com.tomash.androidcontacts.contactgetter.entity.PhoneNumber;
import com.tomash.androidcontacts.contactgetter.main.contactsGetter.ContactsGetterBuilder;

import java.util.ArrayList;
import java.util.List;

import database.FriendDB;
import model.Friend;

import static utils.Configs.STR_DEFAULT_BASE64;
import static utils.Configs.STR_DEFAULT_ICON;

public class HomeViewModel extends ViewModel {
    private MutableLiveData<List<Friend>> mFriends;

    public HomeViewModel() {
        mFriends = new MutableLiveData<>();
    }

    public void initFriends(Context context){
        List<Friend> contactArrayList = FriendDB.getInstance(context).getListFriend();

        mFriends.postValue(contactArrayList);
    }

    public void initQuickContacts(Context selfRef, boolean show_all){
        List<Friend> contactArrayList = new ArrayList<>();
        ArrayList<ContactData> contacts = (ArrayList<ContactData>) new ContactsGetterBuilder(selfRef)
                .onlyWithPhones()
                .buildList();
        int i1 = 0;
        for (int i = 0; i < contacts.size(); i++) {

            ArrayList<PhoneNumber> phoneNumbers = (ArrayList<PhoneNumber>) contacts.get(i).getPhoneList();
            String name = contacts.get(i).getCompositeName();

            ArrayList<String> book = new ArrayList<>();

            for (int j = 0; j < phoneNumbers.size(); j++) {
                PhoneNumber n = phoneNumbers.get(j);
                String number = n.getMainData();
                if (number.contains(" ")) {
                    number = number.replace(" ", "");
                }

                if (number.length() > 9) {
//                    number = number.substring(number.length() - 8);
                    if (!book.contains(number)) book.add(number);
                }
            }

            for (String s : book) {
                if (show_all){
                    Friend friend = new Friend();
                    friend.name = name;
                    friend.phone = s;
                    friend.avata = STR_DEFAULT_BASE64;
                    friend.id = String.valueOf(s.hashCode());

                    contactArrayList.add(friend);
                }else{
                    if (i1 < 8) {
                        Friend friend = new Friend();
                        friend.name = name;
                        friend.phone = s;
                        friend.avata = STR_DEFAULT_BASE64;
                        friend.id = String.valueOf(s.hashCode());
                        contactArrayList.add(friend);
                    }
                    i1++;
                }
            }
        }
        mFriends.postValue(contactArrayList);
    }

    public LiveData<List<Friend>> getQuickContacts() {
        return mFriends;
    }
}