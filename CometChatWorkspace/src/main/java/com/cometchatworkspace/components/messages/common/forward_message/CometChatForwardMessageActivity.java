package com.cometchatworkspace.components.messages.common.forward_message;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.cometchat.pro.constants.CometChatConstants;
import com.cometchat.pro.core.CometChat;
import com.cometchat.pro.core.ConversationsRequest;
import com.cometchat.pro.exceptions.CometChatException;
import com.cometchat.pro.models.Attachment;
import com.cometchat.pro.models.Conversation;
import com.cometchat.pro.models.CustomMessage;
import com.cometchat.pro.models.Group;
import com.cometchat.pro.models.MediaMessage;
import com.cometchat.pro.models.TextMessage;
import com.cometchat.pro.models.User;
import com.cometchatworkspace.R;
import com.cometchatworkspace.components.shared.primaryComponents.theme.Typography;
import com.cometchatworkspace.components.shared.secondaryComponents.CometChatSnackBar;
import com.cometchatworkspace.components.shared.sdkDerivedComponents.cometchatConversationList.CometChatConversationList;
import com.cometchatworkspace.components.shared.sdkDerivedComponents.cometchatConversationList.CometChatConversationListAdapter;
import com.cometchatworkspace.resources.constants.UIKitConstants;
import com.cometchatworkspace.resources.utils.CometChatError;
import com.cometchatworkspace.resources.utils.FontUtils;
import com.cometchatworkspace.resources.utils.MediaUtils;
import com.cometchatworkspace.resources.utils.Utils;
import com.cometchatworkspace.resources.utils.item_clickListener.OnItemClickListener;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Purpose - CometChatForwardMessageScreenActivity class is a fragment used to display list of users to which
 * we will forward the message.
 * Created on - 20th December 2019
 *
 * Modified on  - 16th January 2020
 */


public class CometChatForwardMessageActivity extends AppCompatActivity {
    private static final String TAG = "CometChatForward";

    private CometChatConversationList rvConversationList;

    private final HashMap<String, Conversation> userList = new HashMap<>();

    private CometChatConversationListAdapter conversationListAdapter;

    private ConversationsRequest conversationsRequest;

    private EditText etSearch;

    private ImageView clearSearch;

    private String name,avatar;

    private MaterialButton forwardBtn;

    private ChipGroup selectedUsers;

    private String textMessage="";

    private FontUtils fontUtils;

    private String messageType;

    private double lat,lon;

    private String mediaMessageUrl,mediaMessageExtension,mediaMessageName,mediaMessageMime;

    private String messageCategory = CometChatConstants.CATEGORY_MESSAGE;

    private int mediaMessageSize;

    private int id;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cometchat_forward_message);
        fontUtils= FontUtils.getInstance(this);
        handleIntent();
        init();
    }


    void handleText(Intent intent) {
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (sharedText != null) {
            messageType = CometChatConstants.MESSAGE_TYPE_TEXT;
            textMessage = sharedText;
        }
    }

    void handleImage(Intent intent) {
        Uri imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (imageUri != null) {
            messageType = UIKitConstants.IntentStrings.INTENT_MEDIA_IMAGE_MESSAGE;
            mediaMessageUrl = imageUri.toString();
        }
    }

    void handleVideo(Intent intent) {
        Uri videoUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (videoUri!=null) {
            messageType = UIKitConstants.IntentStrings.INTENT_MEDIA_VIDEO_MESSAGE;
            mediaMessageUrl = videoUri.toString();
        }
    }

    void handleAudio(Intent intent) {
        Uri audioUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (audioUri!=null) {
            messageType = UIKitConstants.IntentStrings.INTENT_MEDIA_AUDIO_MESSAGE;
            mediaMessageUrl = audioUri.toString();
        }
    }

    void handleFile(Intent intent) {
        Uri fileUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (fileUri!=null) {
            messageType = UIKitConstants.IntentStrings.INTENT_MEDIA_FILE_MESSAGE;
            mediaMessageUrl = fileUri.toString();
        }
    }
    /**
     * This method is used to handle parameter passed to this class.
     */
    private void handleIntent() {

        // Get intent, action and MIME type
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                handleText(intent); // Handle text being sent
            } else if (type.startsWith("image/")) {
                handleImage(intent); // Handle single image being sent
            } else if (type.startsWith("video/")) {
                handleVideo(intent);
            } else if(type.startsWith("audio/")) {
                handleAudio(intent);
            } else if (type.startsWith("application/")) {
                handleFile(intent);
            }
        }

        if (getIntent().hasExtra(UIKitConstants.IntentStrings.TYPE)) {
            messageType = getIntent().getStringExtra(UIKitConstants.IntentStrings.TYPE);
        }
        if (getIntent().hasExtra(CometChatConstants.MESSAGE_TYPE_TEXT)){
            textMessage = getIntent().getStringExtra(CometChatConstants.MESSAGE_TYPE_TEXT);
        }
        if (getIntent().hasExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE_IMAGE_URL)) {
            mediaMessageUrl = getIntent().getStringExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE_IMAGE_URL);
        }
        if (getIntent().hasExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE_IMAGE_SIZE)) {
            mediaMessageSize = getIntent().getIntExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE_IMAGE_SIZE,0);
        }
        if (getIntent().hasExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE_IMAGE_EXTENSION)) {
            mediaMessageExtension = getIntent().getStringExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE_IMAGE_EXTENSION);
        }
        if (getIntent().hasExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE_IMAGE_NAME)) {
            mediaMessageName = getIntent().getStringExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE_IMAGE_NAME);
        }
        if (getIntent().hasExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE_IMAGE_MIME_TYPE)) {
            mediaMessageMime = getIntent().getStringExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE_IMAGE_MIME_TYPE);
        }
        if (getIntent().hasExtra(UIKitConstants.IntentStrings.ID)) {
            id = getIntent().getIntExtra(UIKitConstants.IntentStrings.ID,0);
        }
        if (getIntent().hasExtra(UIKitConstants.IntentStrings.LOCATION_LATITUDE)) {
            lat = getIntent().getDoubleExtra(UIKitConstants.IntentStrings.LOCATION_LATITUDE,0);
        }
        if (getIntent().hasExtra(UIKitConstants.IntentStrings.LOCATION_LONGITUDE)) {
            lon = getIntent().getDoubleExtra(UIKitConstants.IntentStrings.LOCATION_LONGITUDE,0);
        }
        if (getIntent().hasExtra(UIKitConstants.IntentStrings.MESSAGE_CATEGORY)) {
            messageCategory = getIntent().getStringExtra(UIKitConstants.IntentStrings.MESSAGE_CATEGORY);
        }
    }

    /**
     * This method is used to initialize the views
     */
    public void init() {
        // Inflate the layout 
        MaterialToolbar toolbar = findViewById(R.id.forward_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (Utils.changeToolbarFont(toolbar)!=null){
            Utils.changeToolbarFont(toolbar).setTextAppearance(Typography.getInstance().getHeading());
        }
        selectedUsers = findViewById(R.id.selected_user);

        forwardBtn = findViewById(R.id.btn_forward);

        rvConversationList = findViewById(R.id.cometchat_conversation_list);

        etSearch = findViewById(R.id.search_bar);

        clearSearch = findViewById(R.id.clear_search);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() > 1)
                    clearSearch.setVisibility(View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                 if (editable.toString().length()!=0) {
                     if (conversationListAdapter != null)
                         conversationListAdapter.getFilter().filter(editable.toString());
                 }
            }
        });

        etSearch.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_SEARCH) {
                if (conversationListAdapter!=null)
                    conversationListAdapter.getFilter().filter(textView.getText().toString());
                clearSearch.setVisibility(View.VISIBLE);
                return true;
            }
            return false;
        });

        clearSearch.setOnClickListener(view1 -> {
            etSearch.setText("");
            clearSearch.setVisibility(View.GONE);
            InputMethodManager inputMethodManager = (InputMethodManager)
                    getSystemService(Context.INPUT_METHOD_SERVICE);
            // Hide the soft keyboard
            inputMethodManager.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
        });

        rvConversationList.setItemClickListener(new OnItemClickListener<Conversation>() {
            @Override
            public void OnItemClick(Conversation conversation, int position) {
                if (userList!=null&&userList.size()<5){
                    if (!userList.containsKey(conversation.getConversationId())) {
                        userList.put(conversation.getConversationId(), conversation);
                        Chip chip = new Chip(CometChatForwardMessageActivity.this);

                        if (conversation.getConversationType().equals(CometChatConstants.CONVERSATION_TYPE_USER)) {
                            name = ((User) conversation.getConversationWith()).getName();
                            avatar = ((User) conversation.getConversationWith()).getAvatar();
                        } else {
                            name = ((Group) conversation.getConversationWith()).getName();
                            avatar = ((Group) conversation.getConversationWith()).getIcon();
                        }
                        chip.setText(name);
                        Glide.with(CometChatForwardMessageActivity.this).load(avatar).placeholder(R.drawable.ic_contacts).transform(new CircleCrop()).into(new SimpleTarget<Drawable>() {
                            @Override
                            public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                                chip.setChipIcon(resource);
                            }
                        });
                        chip.setCloseIconVisible(true);
                        chip.setOnCloseIconClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View vw) {
                                userList.remove(conversation.getConversationId());
                                selectedUsers.removeView(vw);
                                checkUserList();

                            }
                        });
                        selectedUsers.addView(chip,0);
                    }
                    checkUserList();
                }
                else {
                   CometChatSnackBar.show(CometChatForwardMessageActivity.this,
                            selectedUsers,
                            getString(R.string.forward_to_5_at_a_time), CometChatSnackBar.WARNING);
                }
            }

        });

        //It sends message to selected users present in userList using thread. So UI thread doesn't get heavy.
        forwardBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (messageCategory.equals(CometChatConstants.CATEGORY_MESSAGE)) {
                    if (messageType != null && messageType.equals(CometChatConstants.MESSAGE_TYPE_TEXT)) {
                        new Thread(() -> {
                            for (int i = 0; i <= userList.size() - 1; i++) {
                                Conversation conversation = new ArrayList<>(userList.values()).get(i);
                                TextMessage message;
                                String uid;
                                String type;
                                if (conversation.getConversationType().equals(CometChatConstants.CONVERSATION_TYPE_USER)) {
                                    uid = ((User) conversation.getConversationWith()).getUid();
                                    type = CometChatConstants.RECEIVER_TYPE_USER;
                                } else {
                                    uid = ((Group) conversation.getConversationWith()).getGuid();
                                    type = CometChatConstants.RECEIVER_TYPE_GROUP;
                                }
                                message = new TextMessage(uid, textMessage, type);
                                sendMessage(message);
                                if (i == userList.size() - 1) {
//                                    Intent intent = new Intent(CometChatForwardMessageActivity.this, CometChatUI.class);
                                    finish();
                                }
                            }

                        }).start();
                    } else if (messageType != null &&
                            (messageType.equals(CometChatConstants.MESSAGE_TYPE_IMAGE) ||
                                    messageType.equals(CometChatConstants.MESSAGE_TYPE_AUDIO) ||
                                    messageType.equals(CometChatConstants.MESSAGE_TYPE_FILE) ||
                                    messageType.equals(CometChatConstants.MESSAGE_TYPE_VIDEO))) {
                        new Thread(() -> {
                            for (int i = 0; i <= userList.size() - 1; i++) {
                                Conversation conversation = new ArrayList<>(userList.values()).get(i);
                                MediaMessage message;
                                String uid;
                                String type;
                                if (conversation.getConversationType().equals(CometChatConstants.CONVERSATION_TYPE_USER)) {
                                    uid = ((User) conversation.getConversationWith()).getUid();
                                    type = CometChatConstants.RECEIVER_TYPE_USER;
                                } else {
                                    uid = ((Group) conversation.getConversationWith()).getGuid();
                                    type = CometChatConstants.RECEIVER_TYPE_GROUP;
                                }
                                message = new MediaMessage(uid, messageType, type);
                                List<Attachment> attachments= new ArrayList<>();
                                Attachment attachment = new Attachment();
                                attachment.setFileUrl(mediaMessageUrl);
                                attachment.setFileMimeType(mediaMessageMime);
                                attachment.setFileSize(mediaMessageSize);
                                attachment.setFileExtension(mediaMessageExtension);
                                attachment.setFileName(mediaMessageName);
                                attachments.add(attachment);
                                message.setAttachments(attachments);
                                sendMediaMessage(message, i);
                            }

                        }).start();
                    } else if (messageType != null &&
                            (messageType.equalsIgnoreCase(
                                    UIKitConstants.IntentStrings.INTENT_MEDIA_IMAGE_MESSAGE) ||
                                    messageType.equalsIgnoreCase(
                                            UIKitConstants.IntentStrings.INTENT_MEDIA_VIDEO_MESSAGE) ||
                                    messageType.equalsIgnoreCase(
                                            UIKitConstants.IntentStrings.INTENT_MEDIA_AUDIO_MESSAGE) ||
                                    messageType.equalsIgnoreCase(
                                            UIKitConstants.IntentStrings.INTENT_MEDIA_FILE_MESSAGE))) {
                        new Thread(() -> {
                            for (int i = 0; i <= userList.size() - 1; i++) {
                                Conversation conversation = new ArrayList<>(userList.values()).get(i);
                                MediaMessage message = null;
                                String uid;
                                String type;
                                if (conversation.getConversationType().equals(CometChatConstants.CONVERSATION_TYPE_USER)) {
                                    uid = ((User) conversation.getConversationWith()).getUid();
                                    type = CometChatConstants.RECEIVER_TYPE_USER;
                                } else {
                                    uid = ((Group) conversation.getConversationWith()).getGuid();
                                    type = CometChatConstants.RECEIVER_TYPE_GROUP;
                                }
                                File file = MediaUtils.getRealPath(
                                        CometChatForwardMessageActivity.this,
                                        Uri.parse(mediaMessageUrl), true);
                                if (messageType.equalsIgnoreCase(UIKitConstants.IntentStrings.INTENT_MEDIA_IMAGE_MESSAGE))
                                    message = new MediaMessage(uid, file, CometChatConstants.MESSAGE_TYPE_IMAGE, type);
                                else if (messageType.equalsIgnoreCase(UIKitConstants.IntentStrings.INTENT_MEDIA_VIDEO_MESSAGE))
                                    message = new MediaMessage(uid, file, CometChatConstants.MESSAGE_TYPE_VIDEO, type);
                                else if (messageType.equalsIgnoreCase(UIKitConstants.IntentStrings.INTENT_MEDIA_AUDIO_MESSAGE))
                                    message = new MediaMessage(uid, file, CometChatConstants.MESSAGE_TYPE_AUDIO, type);
                                else if (messageType.equalsIgnoreCase(UIKitConstants.IntentStrings.INTENT_MEDIA_FILE_MESSAGE))
                                    message = new MediaMessage(uid, file, CometChatConstants.MESSAGE_TYPE_FILE, type);
                                try {
                                    JSONObject jsonObject = new JSONObject();
                                    jsonObject.put("path", mediaMessageUrl);
                                    message.setMetadata(jsonObject);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                sendMediaMessage(message, i);
                            }
                        }).start();
                    }
                } else {
                    if (messageType != null && messageType.equalsIgnoreCase(UIKitConstants.IntentStrings.LOCATION)) {
                        new Thread(() -> {
                            for (int i = 0; i <= userList.size() - 1; i++) {
                                Conversation conversation = new ArrayList<>(userList.values()).get(i);
                                CustomMessage message;
                                String uid;
                                JSONObject customData = new JSONObject();
                                String type;
                                if (conversation.getConversationType().equals(CometChatConstants.CONVERSATION_TYPE_USER)) {
                                    uid = ((User) conversation.getConversationWith()).getUid();
                                    type = CometChatConstants.RECEIVER_TYPE_USER;
                                } else {
                                    uid = ((Group) conversation.getConversationWith()).getGuid();
                                    type = CometChatConstants.RECEIVER_TYPE_GROUP;
                                }

                                try {
                                    customData = new JSONObject();
                                    customData.put("latitude", lat);
                                    customData.put("longitude", lon);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                message = new CustomMessage(uid, type, UIKitConstants.IntentStrings.LOCATION, customData);

                                sendLocationMessage(message);
                                if (i == userList.size() - 1) {
//                                    Intent intent = new Intent(CometChatForwardMessageActivity.this, CometChatUI.class);
                                    Intent intent = new Intent(CometChatForwardMessageActivity.this,getApplication().getClass());
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    finish();
                                }
                            }

                        }).start();
                    }
                }
            }
        });
        rvConversationList.getRecyclerView().addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {

                if (!recyclerView.canScrollVertically(1)) {
                    makeConversationList();
                }

            }
        });

    }

    private void sendLocationMessage(CustomMessage message) {
        CometChat.sendCustomMessage(message, new CometChat.CallbackListener<CustomMessage>() {
            @Override
            public void onSuccess(CustomMessage customMessage) {
            }

            @Override
            public void onError(CometChatException e) {
            }
        });
    }

    private void sendMessage(TextMessage message) {

        CometChat.sendMessage(message, new CometChat.CallbackListener<TextMessage>() {
            @Override
            public void onSuccess(TextMessage textMessage) {
            }

            @Override
            public void onError(CometChatException e) {
            }
        });
    }

    public void sendMediaMessage(MediaMessage mediaMessage,int i)
    {
        CometChat.sendMediaMessage(mediaMessage, new CometChat.CallbackListener<MediaMessage>() {
            @Override
            public void onSuccess(MediaMessage mediaMessage) {
                if (i == userList.size() - 1) {
//                    Intent intent = new Intent(CometChatForwardMessageActivity.this, CometChatUI.class);
                    Intent intent = new Intent(CometChatForwardMessageActivity.this,getApplication().getClass());
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onError(CometChatException e) {
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

         if (item.getItemId()==android.R.id.home){
             onBackPressed();
         }
        return super.onOptionsItemSelected(item);
    }

    private void checkUserList() {
        if (userList.size()>0) {
            forwardBtn.setVisibility(View.VISIBLE);
        }
        else {
            forwardBtn.setVisibility(View.GONE);
        }
    }

    /**
     * This method is used to fetch conversations
     */
    private void makeConversationList() {

        if (conversationsRequest == null) {
            conversationsRequest = new ConversationsRequest.ConversationsRequestBuilder().setLimit(50).build();
        }
        conversationsRequest.fetchNext(new CometChat.CallbackListener<List<Conversation>>() {
            @Override
            public void onSuccess(List<Conversation> conversationsList) {
                if (conversationsList.size() != 0) {

                    setAdapter(conversationsList);
                }
            }

            @Override
            public void onError(CometChatException e) {
               CometChatSnackBar.show(CometChatForwardMessageActivity.this,
                        rvConversationList, CometChatError.localized(e), CometChatSnackBar.ERROR);
            }
        });
    }

    private void setAdapter(List<Conversation> conversations) {
        if (conversationListAdapter == null) {
            conversationListAdapter = new CometChatConversationListAdapter(this, conversations);
            rvConversationList.getRecyclerView().setAdapter(conversationListAdapter);
        } else {
            conversationListAdapter.updateList(conversations);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
        conversationsRequest = null;
        conversationListAdapter=null;
        makeConversationList();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause: ");
        super.onPause();
        CometChat.removeMessageListener(TAG);
        userList.clear();
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onStop() {
        super.onStop();

    }
}
