package com.example.mychat;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;

import java.util.List;

public class AwesomeMessageAdapter extends ArrayAdapter<AwesomeMessage> {

    List<AwesomeMessage> messages;
    private Activity activity;

    public AwesomeMessageAdapter(Activity context, int resource, List<AwesomeMessage> messages) {
        super(context, resource, messages);
       this.messages = messages;
       this.activity = context;

    }

    @Override
    public View getView(int position,  View convertView,  ViewGroup parent) {
        ViewHolder viewHolder;
        LayoutInflater layoutInflater = (LayoutInflater) activity.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        AwesomeMessage awesomeMessage = getItem(position);
        int layoutResource = 0;
        int viewType = getItemViewType(position);

        if(viewType == 0){
            layoutResource =R.layout.my_message_item;
        } else layoutResource = R.layout.you_meassage_item;



        if (convertView != null) {
            viewHolder = (ViewHolder) convertView.getTag();
        } else {
            convertView = layoutInflater.inflate(
                    layoutResource, parent, false
            );
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }

        boolean isText = awesomeMessage.getImageUrl() == null;

        if (isText) {
            viewHolder.messageTxtView.setVisibility(View.VISIBLE);
            viewHolder.photoImageView.setVisibility(View.GONE);
            viewHolder.messageTxtView.setText(awesomeMessage.getText());
           // viewHolder.nameTextView.setText(awesomeMessage.getName());
        } else {
            //viewHolder.nameTextView.setText(awesomeMessage.getName());
            viewHolder.messageTxtView.setVisibility(View.GONE);
            viewHolder.photoImageView.setVisibility(View.VISIBLE);
            Glide.with(viewHolder.photoImageView.getContext())
                    .load(awesomeMessage.getImageUrl())
                    .into(viewHolder.photoImageView);
        }
            /*if (convertView != null) {
            convertView = ((Activity)getContext())
                    .getLayoutInflater().inflate(R.layout.message_item,parent,false);
            ImageView photoImageView =convertView.findViewById(R.id.photoImageView);
            TextView textTextView = convertView.findViewById(R.id.textTextView);
            TextView nameTextView = convertView.findViewById(R.id.nameTextView);
          AwesomeMessage message = getItem(position);
            *//**  если message.getImageUrl() равен null, то присваиваем то это текст, переменная true .
             * *//*
          boolean isText = message.getImageUrl() == null;

          if(isText){
              textTextView.setVisibility((View.VISIBLE));
              photoImageView.setVisibility(View.GONE);
              textTextView.setText(message.getText());
          } else
          {
              textTextView.setVisibility(View.GONE);
              photoImageView.setVisibility(View.VISIBLE);
              Glide.with(photoImageView.getContext()).load(message.getImageUrl()).into(photoImageView);
          }
          nameTextView.setText(message.getName());
         }*/
        return convertView;
    }

    @Override
    public int getItemViewType(int position) {
        int flag;
        AwesomeMessage awesomeMessage = messages.get(position);
        if(awesomeMessage.isMine()){
            flag = 0;
        } else flag = 1;

        return flag;
    }

    @Override
    public int getViewTypeCount() {

        return 2;
    }

    private class ViewHolder {

        private ImageView photoImageView;
        private TextView messageTxtView;

          public ViewHolder(View view) {
          this.photoImageView = view.findViewById(R.id.photoImageView);
          this.messageTxtView = view.findViewById(R.id.messageTextView);
      }
  }
}
