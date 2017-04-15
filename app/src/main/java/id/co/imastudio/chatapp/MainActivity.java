package id.co.imastudio.chatapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.github.curioustechizen.ago.RelativeTimeTextView;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Date;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    public static final String MESSAGES_CHILD = "messages";
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;

    private DatabaseReference mFirebaseDatabaseReference;
    private String mUsername;
    private String mPhotoUrl;
    private EditText mMessageEditText;
    private ImageView mSendButton;

    private RecyclerView mMessageRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private FirebaseRecyclerAdapter<ChatModel, MessageViewHolder> mFirebaseAdapter;

    private GoogleApiClient mGoogleApiClient;
    private long mTimestamp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        Toast.makeText(this, "Login sebagai " + mFirebaseUser.getDisplayName(), Toast.LENGTH_SHORT).show();

        //initialize database
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();

        //cek apakah user login? jika tidak maka login terlebih dahulu
        if (mFirebaseUser == null) {
            Toast.makeText(this, "Anda harus login", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        } else {
            mUsername = mFirebaseUser.getDisplayName();
            if (mFirebaseUser.getPhotoUrl() != null) {
                mPhotoUrl = mFirebaseUser.getPhotoUrl().toString();
            }
        }

        mMessageEditText = (EditText) findViewById(R.id.edit_text);

        mTimestamp = new Date().getTime();

        mSendButton = (ImageView) findViewById(R.id.btnSend);
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = mMessageEditText.getText().toString();
                //cek apakah teks ada isinya
                if (TextUtils.isEmpty(message)) {
                    Toast.makeText(MainActivity.this, "Tidak bisa mengirim teks kosong", Toast.LENGTH_SHORT).show();
                } else {
                    ChatModel chatMessage = new ChatModel(message, mUsername, mPhotoUrl, mTimestamp);
                    mFirebaseDatabaseReference.child(MESSAGES_CHILD).push().setValue(chatMessage);
                    mMessageEditText.setText("");
                    Toast.makeText(MainActivity.this, "Terkirim.", Toast.LENGTH_SHORT).show();

                }

            }
        });

        mMessageRecyclerView = (RecyclerView) findViewById(R.id.messageRecyclerView);
        mMessageRecyclerView.setHasFixedSize(true);
        mLinearLayoutManager = new LinearLayoutManager(this);
//        mLinearLayoutManager.setStackFromEnd(true);

        final ProgressDialog progressDialog =new ProgressDialog(this);
        progressDialog.setMessage("please wait...");
        progressDialog.show();

        mFirebaseAdapter = new FirebaseRecyclerAdapter<ChatModel, MessageViewHolder>(
                ChatModel.class,
                R.layout.item_chat_message,
                MessageViewHolder.class,
                mFirebaseDatabaseReference.child(MESSAGES_CHILD)) {
            @Override
            protected void populateViewHolder(final MessageViewHolder viewHolder,
                                              ChatModel model, int position) {
                progressDialog.dismiss();
                if (model.getText() != null) {

                    viewHolder.messageTextView.setText(model.getText());
                    viewHolder.messengerTextView.setText(model.getName());
                    viewHolder.timestamp.setReferenceTime(model.getTimestamp());

                    Log.i("", "+" + model.getText());
                    if (model.getPhotoUrl() == null) {
                        viewHolder.messengerImageView
                                .setImageDrawable(ContextCompat
                                        .getDrawable(MainActivity.this,
                                                R.drawable.ic_account_round));
                    } else {
                        Glide.with(MainActivity.this)
                                .load(model.getPhotoUrl())
                                .asBitmap()
                                .centerCrop()
                                .error(R.drawable.ic_account_round)
                                .into(new BitmapImageViewTarget(viewHolder.messengerImageView) {
                                    @Override
                                    protected void setResource(Bitmap resource) {
                                        RoundedBitmapDrawable rounded =
                                                RoundedBitmapDrawableFactory.create(MainActivity.this.getResources(), resource);
                                        rounded.setCircular(true);
                                        viewHolder.messengerImageView.setImageDrawable(rounded);
                                    }
                                });
                    }

                } else {
                    Toast.makeText(MainActivity.this, "Tidak ada data", Toast.LENGTH_SHORT).show();
                }
            }
        };

        mFirebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int modelCount = mFirebaseAdapter.getItemCount();
                int lastVisiblePosition = mLinearLayoutManager.findLastCompletelyVisibleItemPosition();
                // If the recycler view is initially being loaded or the
                // user is at the bottom of the list, scroll to the bottom
                // of the list to show the newly added message.
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (modelCount - 1) &&
                                lastVisiblePosition == (positionStart - 1))) {
                    mMessageRecyclerView.scrollToPosition(positionStart);
                }
            }
        });


        mMessageRecyclerView.setLayoutManager(mLinearLayoutManager);
        mMessageRecyclerView.setAdapter(mFirebaseAdapter);


    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "Koneksi gagal" + connectionResult, Toast.LENGTH_SHORT).show();
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        public TextView messageTextView;
        public TextView messengerTextView;
        public ImageView messengerImageView;
        public RelativeTimeTextView timestamp;

        public MessageViewHolder(View v) {
            super(v);
            messageTextView = (TextView) itemView.findViewById(R.id.messageTextView);
            messengerTextView = (TextView) itemView.findViewById(R.id.messengerTextView);
            messengerImageView = (ImageView) itemView.findViewById(R.id.messengerImageView);
            timestamp = (RelativeTimeTextView) itemView.findViewById(R.id.timestamp);

        }
    }
}
