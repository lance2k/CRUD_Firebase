package com.example.morales;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import android.widget.SearchView;


import java.util.ArrayList;
import java.util.List;


public class ItemsActivity extends AppCompatActivity implements RecyclerAdapter.OnItemClickListener{

    private RecyclerView mRecyclerView;
    private RecyclerAdapter mAdapter;
    private ProgressBar mProgressBar;
    private FirebaseStorage mStorage;
    private DatabaseReference mDatabaseRef;
    private ValueEventListener mDBListener;
    private List<Product> mProducts;
    FirebaseAuth mAuth;

    private void openDetailActivity(String[] data){
        Intent intent = new Intent(this, UpdateActivity.class);
        intent.putExtra("ITEM_KEY",data[0]);
        intent.putExtra("NAME_KEY",data[1]);
        intent.putExtra("NAMELOWER_KEY",data[2]);
        intent.putExtra("IMAGE_KEY",data[3]);
        intent.putExtra("DESCRIPTION_KEY",data[4]);
        intent.putExtra("PRICE_KEY",data[5]);
        intent.putExtra("QUANTITY_KEY",data[6]);
        startActivity(intent);
        finish();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate ( savedInstanceState );
        setContentView ( R.layout.activity_items );

        mRecyclerView = findViewById(R.id.mRecyclerView);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mProgressBar = findViewById(R.id.myDataLoaderProgressBar);
        mProgressBar.setVisibility(View.VISIBLE);

        mProducts = new ArrayList<> ();
        mAdapter = new RecyclerAdapter (ItemsActivity.this, mProducts);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(ItemsActivity.this);

        mStorage = FirebaseStorage.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("products_uploads");

        mDBListener = mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                mProducts.clear();

                for (DataSnapshot productSnapshot : snapshot.getChildren()) {
                    Product upload = productSnapshot.getValue(Product.class);
                    upload.setKey(productSnapshot.getKey());
                    mProducts.add(upload);
                }
                mAdapter.notifyDataSetChanged();
                mProgressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ItemsActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                mProgressBar.setVisibility(View.INVISIBLE);
            }
        });

    }
    public void onItemClick(int position) {
        Product clickedProduct=mProducts.get(position);
        String[] productData={clickedProduct.getKey(), clickedProduct.getName(), clickedProduct.getNameLower(),
                clickedProduct.getImageUrl(), clickedProduct.getDescription(), clickedProduct.getPrice(), clickedProduct.getQuantity()};
        openDetailActivity(productData);
    }

    @Override
    public void onUpdateItemClick(int position) {
        Product clickedProduct=mProducts.get(position);
        String[] productData={clickedProduct.getKey(), clickedProduct.getName(), clickedProduct.getNameLower(),
                clickedProduct.getImageUrl(), clickedProduct.getDescription(), clickedProduct.getPrice(), clickedProduct.getQuantity()};
        openDetailActivity(productData);
    }

    @Override
    public void onDeleteItemClick(int position) {
        Product selectedItem = mProducts.get(position);
        final String selectedKey = selectedItem.getKey();

        StorageReference imageRef = mStorage.getReferenceFromUrl(selectedItem.getImageUrl());
        imageRef.delete().addOnSuccessListener(new OnSuccessListener<Void> () {
            @Override
            public void onSuccess(Void aVoid) {
                mDatabaseRef.child(selectedKey).removeValue();
                Toast.makeText(ItemsActivity.this, "Item deleted", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.search_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.search_menu);
        SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                txtSearch(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                txtSearch(newText);
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    private void txtSearch(String query) {

        Query firebaseSearchQuery = mDatabaseRef.orderByChild("nameLower").startAt(query.toLowerCase())
                .endAt(query.toLowerCase() + "\uf8ff");

        firebaseSearchQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mProducts.clear();

                if (snapshot.exists() && snapshot.getChildrenCount() > 0) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        //Now get Product Objects and populate our arraylist.
                        Product product = ds.getValue(Product.class);
                        product.setKey(ds.getKey());
                        mProducts.add(product);
                    }
                    mAdapter.notifyDataSetChanged();
                }else {

//                    Toast.makeText(ItemsActivity.this, "No item found", Toast.LENGTH_SHORT).show();
                }
                mProgressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ItemsActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                mProgressBar.setVisibility(View.INVISIBLE);
            }
        });

    }

    protected void onDestroy() {
        super.onDestroy();
        mDatabaseRef.removeEventListener(mDBListener);
    }

}