package com.herokuapp.chatio_me.realchat_ios;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
/**
 * Created by Tandv on 15/10/2017.
 */

class TabsPagerAdapter extends FragmentPagerAdapter{
    public TabsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position)
        {
            case 0: RequestsFragment requestsFragment = new RequestsFragment();
                return requestsFragment;
            case 1: ChatsFragment chatsFragment = new ChatsFragment();
                return chatsFragment;
            case 2: FriendsFragment friendsFragment = new FriendsFragment();
                return friendsFragment;
            default: return null;
        }
    }

    @Override
    public int getCount() {
        return 3;
    }

    public CharSequence getPageTitle(int position){
        switch (position)
        {
            case 0 : return "Yêu Cầu";
            case 1: return "Trò Chuyện";
            case 2: return "Bạn Bè";
            default: return null;
        }
    }
}
