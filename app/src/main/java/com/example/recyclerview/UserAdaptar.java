package com.example.recyclerview;

import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

import java.util.ArrayList;
public class UserAdaptar extends RecyclerView.Adapter<UserAdaptar.UserViewHolder> {

    private ArrayList<User> userslist;
    private MainActivity mainActivity;

    public UserAdaptar(ArrayList<User> userslist, MainActivity mainActivity) {
        this.userslist = userslist;
        this.mainActivity = mainActivity;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = View.inflate(parent.getContext(), R.layout.recyclerviewuser, null);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userslist.get(position);
        holder.textViewName.setText(user.getName());
        holder.textViewPassword.setText(user.getPassword());
        holder.textViewId.setText(user.getId());
        holder.imageViewPic.setImageResource(user.getPic());

        if(position % 2 == 0)
        {
            holder.mainRow.setBackgroundColor(ContextCompat.getColor(mainActivity, R.color.white));
        }
        else
        {
            holder.mainRow.setBackgroundColor(ContextCompat.getColor(mainActivity, R.color.black));
        }




    }

    @Override
    public int getItemCount() {
        return userslist.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewName;
        private TextView textViewPassword;
        private TextView textViewId;
        private ImageView imageViewPic;
        public ConstraintLayout mainRow;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.name);
            textViewPassword = itemView.findViewById(R.id.password);
            textViewId = itemView.findViewById(R.id.ID);
            imageViewPic = itemView.findViewById(R.id.pic);

            mainRow = itemView.findViewById(R.id.mainRow);


        }


    }
}


