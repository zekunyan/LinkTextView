package com.zekunyan.linktextview.sample;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.zekunyan.linktextview.LinkTextView;

public class MainActivity extends Activity implements View.OnClickListener {

    //UI controls
    LinkTextView linkTextView;
    TextView infoTextView;
    EditText editText;

    //Index
    int exampleLinkBegin = 0;
    int exampleLinkEnd = 12;

    int manualLinkBegin = 14;
    int manualLinkEnd = 25;

    //Link ID
    int exampleLinkID = -1;
    int manualLinkID = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        linkTextView = (LinkTextView) findViewById(R.id.linkTextView);
        infoTextView = (TextView) findViewById(R.id.textview_info);
        editText = (EditText) findViewById(R.id.edittext_attachment);

        //Bind click
        findViewById(R.id.button_change_normal_color).setOnClickListener(this);
        findViewById(R.id.button_change_pressed_color).setOnClickListener(this);
        findViewById(R.id.button_change_bg_color).setOnClickListener(this);
        findViewById(R.id.button_add).setOnClickListener(this);
        findViewById(R.id.button_remove).setOnClickListener(this);

        //Set text.
        linkTextView.setClickableText("Example link.\nManual link.");

        //Set example link
        exampleLinkID = linkTextView.addClick(exampleLinkBegin, exampleLinkEnd, new LinkTextView.OnClickInLinkText() {
            @Override
            public void onLinkTextClick(String clickText, int linkID, Object attachment) {
                infoTextView.setText("You click example link. It's attachment is: " + attachment);
            }
        }, "This is example link attachment", true, Color.BLACK, Color.YELLOW, Color.WHITE, Color.GREEN);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_change_normal_color:
                linkTextView.setTextNormalColor(exampleLinkID, Color.MAGENTA);
                break;
            case R.id.button_change_pressed_color:
                linkTextView.setTextPressedColor(exampleLinkID, Color.RED);
                break;
            case R.id.button_change_bg_color:
                linkTextView.setBackgroundNormalColor(exampleLinkID, Color.CYAN);
                break;
            case R.id.button_add:
                manualLinkID = linkTextView.addClick(manualLinkBegin, manualLinkEnd, new LinkTextView.OnClickInLinkText() {
                    @Override
                    public void onLinkTextClick(String clickText, int linkID, Object attachment) {
                        infoTextView.setText("You click manual link. It's attachment is: " + attachment);
                    }
                },editText.getText());
                break;
            case R.id.button_remove:
                linkTextView.removeLink(manualLinkID);
                infoTextView.setText("You have removed the link");
                break;
        }
    }
}
