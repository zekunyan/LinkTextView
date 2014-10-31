package com.zekunyan.linktextview;

import android.content.Context;
import android.graphics.Color;
import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by zekunyan on 14-10-21.
 * Email: zekunyan@163.com
 */
public class LinkTextView extends TextView {
    //Map for linkID to holder.
    private HashMap<Integer, LinkTextHolder> mLinkIDMap = new HashMap<Integer, LinkTextHolder>();
    //Base id for each link.
    private int mBaseID = 1;
    //The plain content text.
    private String mOriginText = "";
    //Spannable string for adding link
    private SpannableString mSpannableString = null;
    //If the view has been attached to window
    private boolean mHasShown = false;

    private static class LinkTextHolder {
        private String mText = "";
        private int mLinkID = -1;
        private int mBeginIndex = 0;
        private int mEndIndex = 0;
        //Color config.
        private LinkTextConfig mLinkTextConfig;
        //On click callback
        private OnClickInLinkText mOnClickInLinkText = null;
        //Attachment data
        private Object mAttachment = null;
    }

    public static class LinkTextConfig {
        public boolean mIsLinkUnderLine = false;
        public int mTextNormalColor = Color.BLACK;
        public int mTextPressedColor = Color.BLUE;
        public int mBackgroundNormalColor = Color.WHITE;
        public int mBackgroundPressedColor = Color.WHITE;
    }

    public interface OnClickInLinkText {
        /**
         * Called when click the link.
         *
         * @param clickText Link text.
         * @param linkID Link ID.
         * @param attachment Data attached to the link.
         */
        public void onLinkTextClick(String clickText, int linkID, Object attachment);
    }

    //Constructors
    public LinkTextView(Context context) {
        super(context);
    }

    public LinkTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    //Set text and reset to default config

    /**
     * Set the text you want to show. This should be called before addClick.
     * @param text The text you want to show.
     */
    public void setClickableText(String text) {
        this.mHasShown = false;
        this.mOriginText = text;
        this.mLinkIDMap.clear();
        this.mSpannableString = new SpannableString(mOriginText);
    }

    /**
     * Add a new clickable link.
     *
     * @param start Begin index for link.
     * @param end End index for link.
     * @param click Callback.
     * @param attachment Data attached to the link.
     * @return The ID for this clickable link.
     */
    public int addClick(int start, int end, OnClickInLinkText click, Object attachment) {
        if (start < 0 || end < 0 || start >= mOriginText.length() || end >= mOriginText.length()) {
            return -1;
        }

        final LinkTextHolder linkTextHolder = new LinkTextHolder();
        linkTextHolder.mLinkTextConfig = new LinkTextConfig();
        linkTextHolder.mAttachment = attachment;
        linkTextHolder.mBeginIndex = start;
        linkTextHolder.mEndIndex = end;
        linkTextHolder.mLinkID = mBaseID;
        linkTextHolder.mOnClickInLinkText = click;
        linkTextHolder.mText = mOriginText.substring(start, end);
        mLinkIDMap.put(mBaseID++, linkTextHolder);

        mSpannableString.setSpan(new TouchableSpan(linkTextHolder) {
            @Override
            public void onClick(View view) {
                if (linkTextHolder.mOnClickInLinkText != null) {
                    linkTextHolder.mOnClickInLinkText.onLinkTextClick(linkTextHolder.mText,
                            linkTextHolder.mLinkID, linkTextHolder.mAttachment);
                }
            }
        }, start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

        updateLinks();

        return linkTextHolder.mLinkID;
    }

    /**
     * Add a new clickable link with additional configurations.
     * Color is 0xARGB.
     *
     * @param start Begin index for link.
     * @param end End index for link.
     * @param click Callback.
     * @param attachment Data attached to the link.
     * @param isUnderLine Whether to show the under line.
     * @param textNormalColor Normal color for text.
     * @param textPressedColor Pressed color for text.
     * @param bgNormalColor Normal color for background.
     * @param bgPressedColor Pressed color for background.
     * @return The ID for this clickable link.
     */
    public int addClick(int start, int end, OnClickInLinkText click, Object attachment, boolean isUnderLine,
                        int textNormalColor, int textPressedColor,
                        int bgNormalColor, int bgPressedColor) {
        int id = this.addClick(start, end, click, attachment);
        if (id == -1) {
            return id;
        }

        LinkTextHolder linkTextHolder = mLinkIDMap.get(id);
        //Set custom color
        linkTextHolder.mLinkTextConfig.mIsLinkUnderLine = isUnderLine;
        linkTextHolder.mLinkTextConfig.mTextNormalColor = textNormalColor;
        linkTextHolder.mLinkTextConfig.mTextPressedColor = textPressedColor;
        linkTextHolder.mLinkTextConfig.mBackgroundNormalColor = bgNormalColor;
        linkTextHolder.mLinkTextConfig.mBackgroundPressedColor = bgPressedColor;

        updateLinks();

        return id;
    }

    /**
     * Remove link by link ID
     *
     * @param linkID
     */
    public void removeLink(int linkID) {
        if (!mLinkIDMap.containsKey(linkID)) {
            return;
        }

        try {
            mLinkIDMap.remove(linkID);
        } catch (Exception e) {
            //TODO: Log...
        }

        //Reset all links
        mSpannableString = new SpannableString(mOriginText);
        for (Integer id : mLinkIDMap.keySet()) {
            final LinkTextHolder linkTextHolder = mLinkIDMap.get(id);
            mSpannableString.setSpan(new TouchableSpan(linkTextHolder) {
                @Override
                public void onClick(View view) {
                    if (linkTextHolder.mOnClickInLinkText != null) {
                        linkTextHolder.mOnClickInLinkText.onLinkTextClick(linkTextHolder.mText,
                                linkTextHolder.mLinkID, linkTextHolder.mAttachment);
                    }
                }
            }, linkTextHolder.mBeginIndex, linkTextHolder.mEndIndex, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        }
        updateLinks();
    }

    /**
     * Remove all clickable links.
     */
    public void removeAllLink() {
        mLinkIDMap.clear();
        mSpannableString = new SpannableString(mOriginText);
        updateLinks();
    }

    /**
     * Set new config for specific link.
     *
     * @param linkID Clickable link id.
     * @param config Config.
     */
    public void setNewConfig(int linkID, LinkTextConfig config) {
        if (!mLinkIDMap.containsKey(linkID)) {
            //TODO: Log...
            return;
        }

        LinkTextHolder linkTextHolder = mLinkIDMap.get(linkID);

        if (linkTextHolder == null) {
            //TODO: Log...
            return;
        }

        linkTextHolder.mLinkTextConfig = config;
        invalidate();
    }

    /**
     * Get specific link config by linkID.
     *
     * @param linkID Clickable link id.
     * @return Config.
     */
    public LinkTextConfig getConfig(int linkID) {
        if (!mLinkIDMap.containsKey(linkID)) {
            return null;
        }
        return mLinkIDMap.get(linkID).mLinkTextConfig;
    }

    /**
     * Set specific link text normal color.
     *
     * @param linkID Clickable link id.
     * @param color Text normal color
     */
    public void setTextNormalColor(int linkID, int color) {
        if (!mLinkIDMap.containsKey(linkID)) {
            return;
        }
        LinkTextConfig config = getConfig(linkID);
        config.mTextNormalColor = color;
        invalidate();
    }

    /**
     * Set specific link text pressed color
     *
     * @param linkID Clickable link id.
     * @param color Text pressed color
     */
    public void setTextPressedColor(int linkID, int color) {
        if (!mLinkIDMap.containsKey(linkID)) {
            return;
        }
        LinkTextConfig config = getConfig(linkID);
        config.mTextPressedColor = color;
        invalidate();
    }

    /**
     * Set specific link background normal color
     *
     * @param linkID Clickable link id.
     * @param color Background normal color
     */
    public void setBackgroundNormalColor(int linkID, int color) {
        if (!mLinkIDMap.containsKey(linkID)) {
            return;
        }
        LinkTextConfig config = getConfig(linkID);
        config.mBackgroundNormalColor = color;
        invalidate();
    }

    /**
     * Set specific link background pressed color.
     *
     * @param linkID Clickable link id.
     * @param color Background pressed color
     */
    public void setBackgroundPressedColor(int linkID, int color) {
        if (!mLinkIDMap.containsKey(linkID)) {
            return;
        }
        LinkTextConfig config = getConfig(linkID);
        config.mBackgroundPressedColor = color;
        invalidate();
    }

    /**
     * Get IDs for a specific link text.
     *
     * @param linkText Link text.
     * @return IDs
     */
    public List<Integer> getSpecificLinkIDsByText(String linkText) {
        ArrayList<Integer> ids = new ArrayList<Integer>();

        for (Integer id : mLinkIDMap.keySet()) {
            final LinkTextHolder linkTextHolder = mLinkIDMap.get(id);

            if (linkTextHolder.mText.equals(linkText)) {
                ids.add(linkTextHolder.mLinkID);
            }
        }

        return ids;
    }

    //Update
    private void updateLinks() {
        //Refresh view
        if (mHasShown) {
            this.setText(mSpannableString);
            this.setMovementMethod(LinkTouchMovementMethod.getInstance());
            invalidate();
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        //Set all clickable spans
        if (!mHasShown) {
            mHasShown = true;
            this.setText(mSpannableString);
            this.setMovementMethod(LinkTouchMovementMethod.getInstance());
        }
    }

    //Inner class
    private static abstract class TouchableSpan extends ClickableSpan {
        private boolean mIsPressed;
        private LinkTextHolder linkTextHolder;

        public TouchableSpan(LinkTextHolder linkTextHolder) {
            this.linkTextHolder = linkTextHolder;
        }

        //For LinkTouchMovementMethod call
        public void setPressed(boolean isSelected) {
            mIsPressed = isSelected;
        }

        @Override
        public void updateDrawState(TextPaint textPaint) {
            super.updateDrawState(textPaint);

            textPaint.setColor(mIsPressed ? linkTextHolder.mLinkTextConfig.mTextPressedColor :
                    linkTextHolder.mLinkTextConfig.mTextNormalColor);
            textPaint.bgColor = mIsPressed ? linkTextHolder.mLinkTextConfig.mBackgroundPressedColor :
                    linkTextHolder.mLinkTextConfig.mBackgroundNormalColor;
            textPaint.setUnderlineText(linkTextHolder.mLinkTextConfig.mIsLinkUnderLine);
        }
    }

    private static class LinkTouchMovementMethod extends LinkMovementMethod {
        private TouchableSpan mPressedSpan;
        private static LinkTouchMovementMethod mInstance;

        //Override static method
        static public LinkTouchMovementMethod getInstance() {
            if (mInstance == null) {
                mInstance = new LinkTouchMovementMethod();
            }
            return mInstance;
        }

        @Override
        public boolean onTouchEvent(TextView textView, Spannable spannable, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mPressedSpan = getPressedSpan(textView, spannable, event);
                    if (mPressedSpan != null) {
                        mPressedSpan.setPressed(true);
                        Selection.setSelection(spannable, spannable.getSpanStart(mPressedSpan),
                                spannable.getSpanEnd(mPressedSpan));
                    }
                    break;

                case MotionEvent.ACTION_MOVE:
                    TouchableSpan touchedSpan = getPressedSpan(textView, spannable, event);
                    if (mPressedSpan != null && touchedSpan != mPressedSpan) {
                        mPressedSpan.setPressed(false);
                        mPressedSpan = null;
                        Selection.removeSelection(spannable);
                    }
                    break;

                default:
                    if (mPressedSpan != null) {
                        mPressedSpan.setPressed(false);
                        super.onTouchEvent(textView, spannable, event);
                    }
                    mPressedSpan = null;
                    Selection.removeSelection(spannable);
                    break;
            }

            return true;
        }

        private TouchableSpan getPressedSpan(TextView textView, Spannable spannable, MotionEvent event) {

            int x = (int) event.getX();
            int y = (int) event.getY();

            x -= textView.getTotalPaddingLeft();
            y -= textView.getTotalPaddingTop();

            x += textView.getScrollX();
            y += textView.getScrollY();

            Layout layout = textView.getLayout();
            int line = layout.getLineForVertical(y);
            int off = layout.getOffsetForHorizontal(line, x);

            TouchableSpan[] link = spannable.getSpans(off, off, TouchableSpan.class);
            TouchableSpan touchedSpan = null;
            if (link.length > 0) {
                touchedSpan = link[0];
            }
            return touchedSpan;
        }

    }
}
