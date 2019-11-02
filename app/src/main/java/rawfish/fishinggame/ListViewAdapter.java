package rawfish.fishinggame;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;


public class ListViewAdapter extends BaseAdapter {
    // 체크된 아이템들을 저장할 List

    private ArrayList<ListViewItem> listViewItemList = new ArrayList<>();


    public ListViewAdapter() {
    }

    public int getCount() {
        return listViewItemList.size();
    }

    public Object getItem(int position)
    {
        return listViewItemList.get(position);
    }

    public String getPlayerID(int position) {
        return listViewItemList.get(position).getPlayerID();
    }

    public long getItemId(int position) {
        return position;
    }

    // 아이템 데이터 추가를 위한 함수. 개발자가 원하는대로 작성 가능.
    public void add(String playerID) {
        ListViewItem item = new ListViewItem();

        item.setPlayerID(playerID);
        listViewItemList.add(item);
    }
    public void add(int index, String playerID) {
        ListViewItem item = new ListViewItem();

        item.setPlayerID(playerID);
        listViewItemList.add(index, item);
    }

    //아이템 데이터 삭제를 위한 함수
    public void removeItem(int num)
    {
        listViewItemList.remove(num);
    }

    public void removeAll()
    {
        listViewItemList.removeAll(listViewItemList);
    }

    // 각 항목의 뷰 생성
    public View getView(int position, View convertView, ViewGroup parent) {
        // 리스너에서 사용할 포지션 변수.
        Context context = parent.getContext();

        // "listview_item" Layout을 inflate하여 convertView 참조 획득.
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.login_player, parent, false);
        }

        // 화면에 표시될 View(Layout이 inflate된)으로부터 위젯에 대한 참조 획득
        TextView playerID = (TextView) convertView.findViewById(R.id.text01);

        // Data Set(listViewItemList)에서 position에 위치한 데이터 참조 획득
        ListViewItem listViewItem = listViewItemList.get(position);

        // 아이템 내 각 위젯에 데이터 반영
        playerID.setText(listViewItem.getPlayerID());

        return convertView;
    }
}

