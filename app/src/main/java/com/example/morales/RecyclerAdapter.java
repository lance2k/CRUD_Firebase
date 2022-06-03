package com.example.morales;
import android.content.Context;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


public  class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.RecyclerViewHolder>{
    private Context mContext;
    private List<Product> products;
    private OnItemClickListener mListener;

    public RecyclerAdapter(Context context, List<Product> uploads) {
        mContext = context;
        products = uploads;
    }

    @Override
    public RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.row_model, parent, false);
        return new RecyclerViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerViewHolder holder, int position) {
        Product currentProduct = products.get(position);
        holder.nameTextView.setText("Name: "+currentProduct.getName());
        holder.descriptionTextView.setText("Description: "+currentProduct.getDescription());
//        holder.dateTextView.setText("Date: "+getDateToday());
        holder.priceTextView.setText("Price: "+currentProduct.getPrice());
        holder.quantityTextView.setText("Quantity: "+currentProduct.getQuantity());
        Picasso.with(mContext)
                .load(currentProduct.getImageUrl())
                .placeholder(R.drawable.placeholder)
                .fit()
                .centerCrop()
                .into(holder.teacherImageView);
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public class RecyclerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
            View.OnCreateContextMenuListener, MenuItem.OnMenuItemClickListener {

        public TextView nameTextView,descriptionTextView,dateTextView, priceTextView, quantityTextView;
        public ImageView teacherImageView;

        public RecyclerViewHolder(View itemView) {
            super(itemView);
            nameTextView =itemView.findViewById ( R.id.nameTextView );
            descriptionTextView = itemView.findViewById(R.id.descriptionTextView);
//            dateTextView = itemView.findViewById(R.id.dateTextView);
            teacherImageView = itemView.findViewById(R.id.teacherImageView);
            priceTextView = itemView.findViewById(R.id.priceTextView);
            quantityTextView = itemView.findViewById(R.id.quantityTextView);

            itemView.setOnClickListener(this);
            itemView.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mListener != null) {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    mListener.onItemClick(position);
                }
            }
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            menu.setHeaderTitle("Select Action");
            MenuItem updateItem = menu.add( Menu.NONE, 1, 1, "Update");
            MenuItem deleteItem = menu.add(Menu.NONE, 2, 2, "Delete");

            updateItem.setOnMenuItemClickListener(this);
            deleteItem.setOnMenuItemClickListener(this);
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            if (mListener != null) {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {

                    switch (item.getItemId()) {
                        case 1:
                            mListener.onUpdateItemClick(position);
                            return true;
                        case 2:
                            mListener.onDeleteItemClick(position);
                            return true;
                    }
                }
            }
            return false;
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
        void onUpdateItemClick(int position);
        void onDeleteItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }
    private String getDateToday(){
        DateFormat dateFormat=new SimpleDateFormat("yyyy/MM/dd");
        Date date=new Date();
        String today= dateFormat.format(date);
        return today;
    }
}
