package io.yxy.mobgroupchat;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.koushikdutta.async.http.socketio.ConnectCallback;

import java.util.List;

/**
 * Created by yxy on 15/7/2.
 */
public class MessageListAdapter extends BaseAdapter {

    private Context context;
    private List<Message> messageItems;

    public MessageListAdapter(Context context, List<Message> messageItems) {
        this.context = context;
        this.messageItems = messageItems;
    }

    @Override
    public int getCount() {
        return messageItems.size();
    }

    @Override
    public Object getItem(int position) {
        return messageItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Message m = messageItems.get(position);

        LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        // Identifying the message owner
        if (m.isSelf()) {
            // messages belongs to you, so load the right aligned layout
            convertView = mInflater.inflate(R.layout.list_item_message_right, null);
        } else {
            // message belongs to other person, load the left aligned layout
            convertView = mInflater.inflate(R.layout.list_item_message_left, null);
        }

        TextView lblFrom = (TextView) convertView.findViewById(R.id.lbl_msg_from);
        TextView txtMsg = (TextView) convertView.findViewById(R.id.txt_msg);

        txtMsg.setText(m.getFromName());
        txtMsg.setText(m.getMessage());
        return convertView;
    }
}
