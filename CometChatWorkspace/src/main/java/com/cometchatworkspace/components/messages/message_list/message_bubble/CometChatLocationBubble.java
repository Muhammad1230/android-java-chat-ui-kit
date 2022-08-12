package com.cometchatworkspace.components.messages.message_list.message_bubble;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.ColorInt;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.cometchat.pro.core.CometChat;
import com.cometchat.pro.models.BaseMessage;
import com.cometchat.pro.models.CustomMessage;
import com.cometchat.pro.models.User;
import com.cometchatworkspace.R;
import com.cometchatworkspace.components.messages.message_list.message_bubble.utils.Alignment;
import com.cometchatworkspace.components.messages.message_list.message_bubble.utils.MessageBubbleListener;
import com.cometchatworkspace.components.messages.message_list.message_bubble.utils.TimeAlignment;
import com.cometchatworkspace.components.messages.template.CometChatMessageTemplate;
import com.cometchatworkspace.components.shared.primaryComponents.configurations.CometChatMessagesConfigurations;
import com.cometchatworkspace.components.shared.primaryComponents.theme.Palette;
import com.cometchatworkspace.components.shared.primaryComponents.theme.Typography;
import com.cometchatworkspace.components.shared.secondaryComponents.CometChatMessageReceipt;
import com.cometchatworkspace.components.shared.secondaryComponents.cometchatAvatar.CometChatAvatar;
import com.cometchatworkspace.components.shared.secondaryComponents.cometchatDate.CometChatDate;
import com.cometchatworkspace.components.shared.secondaryComponents.cometchatReaction.CometChatMessageReaction;
import com.cometchatworkspace.components.shared.secondaryComponents.cometchatReaction.model.Reaction;
import com.cometchatworkspace.resources.constants.UIKitConstants;
import com.cometchatworkspace.resources.utils.FontUtils;
import com.cometchatworkspace.resources.utils.Utils;
import com.google.android.material.card.MaterialCardView;

import org.json.JSONException;

public class CometChatLocationBubble extends RelativeLayout {

    FontUtils fontUtils;
    private final User loggedInUser = CometChat.getLoggedInUser();
    private BaseMessage baseMessage;
    private Context context;
    private View view;


    private ImageView image;
    private TextView title;
    private TextView subtitle;

    private MaterialCardView cvMessageBubble;
    private RelativeLayout cvMessageBubbleLayout;

    private String alignment = Alignment.RIGHT;

    private final String TAG = "LocationMessageBubble";

    private MessageBubbleListener messageBubbleListener;

    private Palette palette;
    private Typography typography;
    private int layoutId;

    public CometChatLocationBubble(Context context) {
        super(context);
        initComponent(context,null);
    }

    public CometChatLocationBubble(Context context, AttributeSet attrs) {
        super(context, attrs);
        initComponent(context,attrs);
    }

    public CometChatLocationBubble(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initComponent(context,attrs);
    }

    public CometChatLocationBubble(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initComponent(context,attrs);
    }

    private void initComponent(Context context, AttributeSet attributeSet) {
        this.context = context;
        palette = Palette.getInstance(context);
        typography= Typography.getInstance();
        fontUtils=FontUtils.getInstance(context);
        TypedArray a = getContext().getTheme().obtainStyledAttributes(
                attributeSet,
                R.styleable.LocationMessageBubble,
                0, 0);
        float cornerRadius  = a.getFloat(R.styleable.LocationMessageBubble_corner_radius,12);
        int backgroundColor = a.getColor(R.styleable.LocationMessageBubble_backgroundColor,0);
        Drawable messageAvatar = a.getDrawable(R.styleable.LocationMessageBubble_avatar);
        int hideAvatar = a.getInt(R.styleable.LocationMessageBubble_avatarVisibility,View.VISIBLE);
        int hideUserName = a.getInt(R.styleable.LocationMessageBubble_userNameVisibility,View.VISIBLE);
        String userName = a.getString(R.styleable.LocationMessageBubble_userName);
        int color = a.getColor(R.styleable.LocationMessageBubble_userNameColor,0);
        alignment = Alignment.getValue(a.getInt(R.styleable.LocationMessageBubble_messageAlignment,0));

        String titleStr = a.getString(R.styleable.LocationMessageBubble_title);
        int titleColor = a.getColor(R.styleable.LocationMessageBubble_titleColor,0);

        String subtitleStr = a.getString(R.styleable.LocationMessageBubble_subtitle);
        int subtitleColor = a.getColor(R.styleable.LocationMessageBubble_subtitleColor,0);

        Drawable imageDrawable = a.getDrawable(R.styleable.LocationMessageBubble_image);

        int borderColor = a.getColor(R.styleable.LocationMessageBubble_borderColor,0);
        int borderWidth = a.getInt(R.styleable.LocationMessageBubble_borderWidth,0);

        view = LayoutInflater.from(getContext()).inflate(R.layout.message_right_location_bubble,null);

        initView(view);

        cornerRadius(cornerRadius);
        backgroundColor(backgroundColor);
        title(titleStr);
        titleColor(titleColor);
        subtitle(subtitleStr);
        subtitleColor(subtitleColor);
        setImage(imageDrawable);
        borderColor(borderColor);
        borderWidth(borderWidth);

        cvMessageBubble.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                messageBubbleListener.onLongCLick(baseMessage);
                return true;
            }
        });


//        setColorFilter(baseMessage,cvMessageView);



    }

    private void initView(View view) {
        addView(view);

        image = view.findViewById(R.id.iv_map);
        title = view.findViewById(R.id.tvTitle);
        subtitle = view.findViewById(R.id.tvSubtitle);

        cvMessageBubble = view.findViewById(R.id.cv_message_container);

        //CustomView
//        CometChatMessageTemplate messageTemplate = CometChatMessagesConfigurations
//                .getMessageTemplateById(CometChatMessageTemplate.DefaultList.location);
//        if(messageTemplate!=null)
//            layoutId = messageTemplate.getView();
////        dataView = messageTemplate.getDataView();
//        if (layoutId != 0) {
//            View customView = LayoutInflater.from(context).inflate(layoutId,null);
//            cvMessageBubbleLayout.setVisibility(View.GONE);
//            if (customView.getParent()!=null)
//                ((ViewGroup)customView.getParent()).removeAllViewsInLayout();
//            cvMessageBubble.addView(customView);
//            image = customView.findViewById(R.id.iv_map);
//            title = customView.findViewById(R.id.tv_place_name);
//            subtitle = customView.findViewById(R.id.sender_location_txt);
//
//            customView.setOnLongClickListener(new OnLongClickListener() {
//                @Override
//                public boolean onLongClick(View view) {
//                    messageBubbleListener.onLongCLick(baseMessage);
//                    return true;
//                }
//            });
//            customView.setOnClickListener(new OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    messageBubbleListener.onClick(baseMessage);
//                }
//            });
//        }
    }

    public void setImage(Drawable imageDrawable) {
        image.setImageDrawable(imageDrawable);
    }
    public void setImage(String url) {
        Glide.with(context).load(url).into(image);
    }



    public void cornerRadius(float topLeft, float topRight, float bottomLeft, float bottomRight) {
        if (cvMessageBubble!=null)
            cvMessageBubble.setShapeAppearanceModel(cvMessageBubble.getShapeAppearanceModel()
                    .toBuilder()
                    .setTopLeftCornerSize(topLeft)
                    .setTopRightCornerSize(topRight)
                    .setBottomLeftCornerSize(bottomLeft)
                    .setBottomRightCornerSize(bottomRight)
                    .build());
    }
    public void cornerRadius(float radius) {
        if (cvMessageBubble!=null)
            cvMessageBubble.setRadius(radius);
    }

    public View getBubbleView() {
        return cvMessageBubble;
    }

    public void backgroundColor(int[] colorArray, GradientDrawable.Orientation orientation) {
        if (cvMessageBubble !=null) {
            GradientDrawable gd = new GradientDrawable(
                    orientation,
                    colorArray);
            gd.setCornerRadius(cvMessageBubble.getRadius());
            cvMessageBubble.setBackgroundDrawable(gd);
        }
    }
    public void backgroundColor(@ColorInt int bgColor) {
        if (cvMessageBubble !=null) {
            if (bgColor != 0)
                cvMessageBubble.setCardBackgroundColor(bgColor);
        }
    }

    public void messageObject(BaseMessage baseMessage) {
        this.baseMessage = baseMessage;

        try {
           double LATITUDE = ((CustomMessage) baseMessage).getCustomData().getDouble("latitude");
           double LONGITUDE = ((CustomMessage) baseMessage).getCustomData().getDouble("longitude");
           title.setText(Utils.getAddress(context, LATITUDE, LONGITUDE));
           String mapUrl = UIKitConstants.MapUrl.MAPS_URL +LATITUDE+","+LONGITUDE+"&key="+ UIKitConstants.MapUrl.MAP_ACCESS_KEY;
           Glide.with(context)
                   .load(mapUrl)
                   .diskCacheStrategy(DiskCacheStrategy.ALL)
                   .placeholder(R.drawable.default_map)
                   .into(image);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        subtitle.setText(String.format(context.getString(R.string.shared_location),baseMessage.getSender().getName()));
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                messageBubbleListener.onClick(baseMessage);
            }
        });
        cvMessageBubble.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                messageBubbleListener.onLongCLick(baseMessage);
                return true;
            }
        });
    }


    public void borderColor(@ColorInt int color) {
        if (color!=0 && cvMessageBubble!=null) {
            cvMessageBubble.setStrokeColor(color);
        }
    }

    public void borderWidth(int width) {
        if (cvMessageBubble!=null)
            cvMessageBubble.setStrokeWidth(width);
    }

    public void title(String str) {
        if (title!=null)
            title.setText(str);
    }
    public void titleColor(@ColorInt int color) {
        if (title!=null && color!=0)
            title.setTextColor(color);
    }
    public void titleFont(String font) {
        if (title!=null)
            title.setTypeface(fontUtils.getTypeFace(font));
    }

    public void subtitle(String subStr) {
        if (subtitle!=null)
            subtitle.setText(subStr);
    }
    public void subtitleColor(@ColorInt int color) {
        if (subtitle!=null && color!=0)
            subtitle.setTextColor(color);
    }
    public void subtitleFont(String font) {
        if (subtitle!=null)
            subtitle.setTypeface(fontUtils.getTypeFace(font));
    }

    public void setEventListener(MessageBubbleListener listener) {
        messageBubbleListener = listener;
    }
}
