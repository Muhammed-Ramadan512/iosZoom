package com.webcare.zoom.sdksample.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import java.util.ArrayList;
import java.util.List;

import us.zoom.sdk.ChatMessageDeleteType;
import us.zoom.sdk.FreeMeetingNeedUpgradeType;
import us.zoom.sdk.IAnswerItem;
import us.zoom.sdk.IQAItemInfo;
import us.zoom.sdk.IRequestLocalRecordingPrivilegeHandler;
import us.zoom.sdk.InMeetingAudioController;
import us.zoom.sdk.InMeetingChatController;
import us.zoom.sdk.InMeetingChatMessage;
import us.zoom.sdk.InMeetingEventHandler;
import us.zoom.sdk.InMeetingQAController;
import us.zoom.sdk.InMeetingServiceListener;
import us.zoom.sdk.InMeetingUserInfo;
import us.zoom.sdk.MeetingParameter;
import us.zoom.sdk.MeetingServiceListener;
import us.zoom.sdk.MeetingStatus;
import us.zoom.sdk.VideoQuality;
import us.zoom.sdk.ZoomSDK;
import com.webcare.zoom.sdksample.R;
import com.webcare.zoom.sdksample.inmeetingfunction.customizedmeetingui.SimpleInMeetingListener;

public class QAActivity extends FragmentActivity implements InMeetingQAController.InMeetingQAListener, MeetingServiceListener {


    final String TAG = QAActivity.class.getSimpleName();
    InMeetingQAController qaController;

    ListView listView;

    List<QAUIItem> list = new ArrayList<>();

    CheckBox box_enable_anonymous;
    CheckBox box_view_only_answered;
    CheckBox box_view_all;
    CheckBox box_enable_voteup;
    CheckBox box_enable_comment;

    View viewAllContain;
    View setting_group;

    private int viewType = 1;

    Button btnAll;
    Button btnOpen;
    Button btnMy;
    Button btnAnswered;
    Button btnDismissed;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qa_sample);
        listView = findViewById(R.id.question_list);

        listView.setAdapter(new ArrayAdapter(this, android.R.layout.simple_list_item_1, android.R.id.text1, list));

        qaController = ZoomSDK.getInstance().getInMeetingService().getInMeetingQAController();
        qaController.addQAListener(this);

        setting_group = findViewById(R.id.setting_group);
        viewAllContain = findViewById(R.id.group_view_all);
        box_enable_anonymous = findViewById(R.id.add_anonymous);
        box_view_only_answered = findViewById(R.id.view_only);
        box_view_all = findViewById(R.id.view_all);
        box_enable_voteup = findViewById(R.id.enable_upvote);
        box_enable_comment = findViewById(R.id.enable_comment);

        box_enable_anonymous.setChecked(qaController.isAskQuestionAnonymouslyEnabled());

        btnAll = findViewById(R.id.btn_all);
        btnOpen = findViewById(R.id.btn_open);
        btnMy = findViewById(R.id.btn_my);
        btnAnswered = findViewById(R.id.btn_answered);
        btnDismissed = findViewById(R.id.btn_dismissed);
        if (isMySelfHostOrCoHostOrPanelist()) {
            viewType = 2;
            btnOpen.setVisibility(View.VISIBLE);
            btnAnswered.setVisibility(View.VISIBLE);
            btnDismissed.setVisibility(View.VISIBLE);
        } else {
            viewType = 1;
            btnAll.setVisibility(View.VISIBLE);
            btnMy.setVisibility(View.VISIBLE);
        }

        ZoomSDK.getInstance().getInMeetingService().addListener(inMeetingListener);
        ZoomSDK.getInstance().getMeetingService().addListener(this);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                AlertDialog.Builder builder = new AlertDialog.Builder(QAActivity.this);
                builder.setTitle("Action");

                final QAUIItem item = list.get(position);

                List<String> menu = new ArrayList<>();
                menu.add("Answer public");
                menu.add("Answer private");
                menu.add("Attender Answer");
                menu.add("UpVote");
                menu.add("DownVote");
                menu.add("reopen");
                menu.add("Dismiss");

                menu.add("StartLiveAnswer");
                menu.add("EndLiveAnswer");
                menu.add("DeleteQuestion");
                menu.add("DeleteAnswer");


                String[] ts = new String[menu.size()];
                menu.toArray(ts);
                builder.setItems(ts, new DialogInterface.OnClickListener() {
                    boolean result = false;

                    @Override
                    public void onClick(DialogInterface dialogInterface, int index) {
                        switch (index) {
                            case 0: {
                                result = qaController.answerQuestionPublic(item.itemInfo.getQuestionID(), "Answer:" + index);
                                break;
                            }
                            case 1: {
                                result = qaController.answerQuestionPrivate(item.itemInfo.getQuestionID(), "Answer:" + index);
                                break;
                            }
                            case 2: {
                                result = qaController.commentQuestion(item.itemInfo.getQuestionID(), "Answer:" + index);
                                break;
                            }
                            case 3: {
                                result = qaController.voteupQuestion(item.itemInfo.getQuestionID(), true);
                                break;
                            }
                            case 4: {
                                result = qaController.voteupQuestion(item.itemInfo.getQuestionID(), false);
                                break;
                            }
                            case 5: {
                                result = qaController.reopenQuestion(item.itemInfo.getQuestionID());
                                break;
                            }
                            case 6: {
                                result = qaController.dismissQuestion(item.itemInfo.getQuestionID());
                                break;
                            }
                            case 7: {
                                result = qaController.startLiving(item.itemInfo.getQuestionID());
                                break;
                            }
                            case 8: {
                                result = qaController.endLiving(item.itemInfo.getQuestionID());
                                break;
                            }
                            case 9: {
                                result = qaController.deleteQuestion(item.itemInfo.getQuestionID());
                                break;
                            }
                            case 10: {
                                if(null!=item.itemInfo.getAnswerList()){
                                    List<IAnswerItem>  answerItems= item.itemInfo.getAnswerList();
                                    if(!answerItems.isEmpty()){
                                        result = qaController.deleteAnswer( answerItems.get(0).getAnswerID());
                                    }
                                }

                                break;
                            }
                        }
                        Toast.makeText(QAActivity.this, "action:" + result, Toast.LENGTH_SHORT).show();
                    }
                });
                builder.show();
            }
        });
        refresh();
    }


    CompoundButton.OnCheckedChangeListener listener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            boolean result = false;
            if (compoundButton == box_enable_anonymous) {
                result = qaController.enableAnonymousQuestion(b);
            } else if (compoundButton == box_view_only_answered) {
                if (b) {
                    result = qaController.enableAttendeeViewAllQuestion(false);
                    if (!result) {
                        box_view_only_answered.setChecked(false);
                    } else {
                        box_view_all.setChecked(false);
                    }
                }
            } else if (compoundButton == box_view_all) {
                if (b) {
                    result = qaController.enableAttendeeViewAllQuestion(true);
                    if (!result) {
                        box_view_all.setChecked(false);
                    } else {
                        box_view_only_answered.setChecked(false);
                    }
                }
            } else if (compoundButton == box_enable_voteup) {
                result = qaController.enableQAVoteup(b);
            } else if (compoundButton == box_enable_comment) {
                result = qaController.enableQAComment(b);
            }
            Toast.makeText(QAActivity.this, "action:" + result, Toast.LENGTH_SHORT).show();
            compoundButton.postDelayed(new Runnable() {
                @Override
                public void run() {
                    refreshSetting();
                }
            }, 500);

        }
    };

    public void onClickSettings(View view) {
        if (setting_group.getVisibility() == View.VISIBLE) {
            setting_group.setVisibility(View.GONE);
        } else {
            setting_group.setVisibility(View.VISIBLE);
            refreshSetting();
        }
    }

    public void onAllQuestion(View view) {
        viewType = 1;
        refresh();
    }

    public void onOpenQuestions(View view) {
        viewType = 2;
        refresh();
    }


    public void onMyQuestion(View view) {
        viewType = 3;
        refresh();
    }

    public void onAnsweredQuestions(View view) {
        if (!isMySelfHostOrCoHostOrPanelist()) {
            return;
        }
        viewType = 4;
        refresh();
    }

    public void onDismissedQuestion(View view) {
        if (!isMySelfHostOrCoHostOrPanelist()) {
            return;
        }
        viewType = 5;
        refresh();
    }

    private boolean isMySelfHostOrCoHostOrPanelist() {
        InMeetingUserInfo userInfo = ZoomSDK.getInstance().getInMeetingService().getMyUserInfo();
        if (null != userInfo) {
            if (userInfo.getInMeetingUserRole() == InMeetingUserInfo.InMeetingUserRole.USERROLE_HOST
                    || userInfo.getInMeetingUserRole() == InMeetingUserInfo.InMeetingUserRole.USERROLE_COHOST
                    || userInfo.getInMeetingUserRole() == InMeetingUserInfo.InMeetingUserRole.USERROLE_PANELIST) {
                return true;
            }
        }
        return false;
    }

    private boolean isMySelfHostOrCoHost() {
        InMeetingUserInfo userInfo = ZoomSDK.getInstance().getInMeetingService().getMyUserInfo();
        if (null != userInfo) {
            if (userInfo.getInMeetingUserRole() == InMeetingUserInfo.InMeetingUserRole.USERROLE_HOST
                    || userInfo.getInMeetingUserRole() == InMeetingUserInfo.InMeetingUserRole.USERROLE_COHOST) {
                return true;
            }
        }
        return false;
    }

    private void refreshSetting() {

        boolean isHost = isMySelfHostOrCoHost();
        box_enable_anonymous.setEnabled(isHost);
        box_view_only_answered.setEnabled(isHost);
        box_view_all.setEnabled(isHost);
        box_enable_voteup.setEnabled(isHost);
        box_enable_comment.setEnabled(isHost);

        CompoundButton.OnCheckedChangeListener tempListener = null;

        box_enable_anonymous.setOnCheckedChangeListener(tempListener);
        box_view_only_answered.setOnCheckedChangeListener(tempListener);
        box_view_all.setOnCheckedChangeListener(tempListener);
        box_enable_voteup.setOnCheckedChangeListener(tempListener);
        box_enable_comment.setOnCheckedChangeListener(tempListener);

        box_enable_anonymous.setChecked(qaController.isAskQuestionAnonymouslyEnabled());
        if (qaController.isAttendeeCanViewAllQuestions()) {
            viewAllContain.setVisibility(View.VISIBLE);
            box_view_only_answered.setChecked(false);
            box_view_all.setChecked(true);
            box_enable_voteup.setChecked(qaController.isQAVoteupEnabled());
            box_enable_comment.setChecked(qaController.isQACommentEnabled());
        } else {
            box_view_only_answered.setChecked(true);
            box_view_all.setChecked(false);
            box_enable_voteup.setChecked(false);
            box_enable_comment.setChecked(false);
            viewAllContain.setVisibility(View.GONE);
        }
        tempListener = listener;
        box_enable_anonymous.setOnCheckedChangeListener(tempListener);
        box_view_only_answered.setOnCheckedChangeListener(tempListener);
        box_view_all.setOnCheckedChangeListener(tempListener);
        box_enable_voteup.setOnCheckedChangeListener(tempListener);
        box_enable_comment.setOnCheckedChangeListener(tempListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        qaController.removeQAListener(this);
        ZoomSDK.getInstance().getInMeetingService().removeListener(inMeetingListener);
    }

    public void onClose(View view) {
        finish();
    }

    public void onAddQuestion(View view) {
        boolean isAllowAskQuestionAnonymously = qaController.isAskQuestionAnonymouslyEnabled();
        boolean anonymously = System.currentTimeMillis() % 2 == 0;
        if (!isAllowAskQuestionAnonymously) {
            anonymously = false;
        }
        boolean result = qaController.addQuestion("Test" + System.currentTimeMillis(), anonymously);
        if (result) {
            Toast.makeText(this, "Add success anonymously:" + anonymously, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Add fail anonymously:" + anonymously, Toast.LENGTH_LONG).show();
        }
    }

    private void updateSize() {
        if (isMySelfHostOrCoHostOrPanelist()) {
            btnOpen.setText("Open(" + qaController.getOpenQuestionCount() + ")");
            btnAnswered.setText("Answered(" + qaController.getAnsweredQuestionCount() + ")");
            btnDismissed.setText("Dismissed(" + qaController.getDismissedQuestionCount() + ")");
        } else {
            btnAll.setText("All(" + qaController.getAllQuestionCount() + ")");
            btnMy.setText("My(" + qaController.getMyQuestionCount() + ")");
        }
    }

    private void refresh() {
        refreshSetting();
        updateSize();
        List<IQAItemInfo> questions = null;
        if (viewType == 1) {
            questions = qaController.getAllQuestionList();
        } else if (viewType == 2) {
            questions = qaController.getOpenQuestionList();
        } else if (viewType == 3) {
            questions = qaController.getMyQuestionList();
        } else if (viewType == 4) {
            questions = qaController.getAnsweredQuestionList();
        } else if (viewType == 5) {
            questions = qaController.getDismissedQuestionList();
        }
        list.clear();
        if (null != questions) {
            for (IQAItemInfo itemInfo : questions) {
                list.add(new QAUIItem(itemInfo));
            }
        }
        ((ArrayAdapter) listView.getAdapter()).notifyDataSetChanged();
    }

    class QAUIItem {

        IQAItemInfo itemInfo;

        public QAUIItem(IQAItemInfo itemInfo) {
            this.itemInfo = itemInfo;
        }

        IQAItemInfo getItemInfo() {
            return itemInfo;
        }

        @Override
        public String toString() {
            List<IAnswerItem> answerItems = itemInfo.getAnswerList();
            String answer = "";
            if (null != answerItems) {
                StringBuilder builder = new StringBuilder();
                for (IAnswerItem answerItem : answerItems) {
                    builder.append(answerItem.getSenderName() + " --> " + answerItem.getText() + "\n");
                }
                answer = builder.toString();
            }

            String text = itemInfo.getSenderName() + ":\n" +
                    " title:" + itemInfo.getText() + " isAnonymous:" + itemInfo.isAnonymous()
                    + " isMarkedAsAnswered:" + itemInfo.isMarkedAsAnswered()
                    + " isMarkedAsDismissed:" + itemInfo.isMarkedAsDismissed()
                    + " getUpvoteNum:" + itemInfo.getUpvoteNum()
                    + " hasLiveAnswers:" + itemInfo.hasLiveAnswers()
                    + " hasTextAnswers:" + itemInfo.hasTextAnswers()
                    + " isMySelfUpvoted:" + itemInfo.isMySelfUpvoted();
            if (itemInfo.isLiveAnswering()) {
                text += " LiveAnswerName:" + itemInfo.getLiveAnswerName();
            }

            if (!TextUtils.isEmpty(answer)) {
                text += "\n answer -------------------------------------\n"
                        + answer;
            }
            return text;
        }
    }

    @Override
    public void onMeetingQAStatusChanged(boolean isMeetingQAFeatureOn) {
        Log.d(TAG, "onMeetingQAStatusChanged isMeetingQAFeatureOn: " + isMeetingQAFeatureOn);
    }

    @Override
    public void onAllowAskQuestionStatus(boolean bEnabled) {
        Log.d(TAG, "onAllowAskQuestionStatus bEnabled: " + bEnabled);
    }

    @Override
    public void onQAConnectStarted() {
        Log.d(TAG, "onQAConnectStarted");
    }


    @Override
    public void onAllowAskQuestionAnonymousStatus(boolean enable) {
        Log.d(TAG, "onAllowAskQuestionAnonymousStatus:" + enable);
        refreshSetting();
    }

    @Override
    public void onAllowAttendeeViewAllQuestionStatus(boolean enable) {
        Log.d(TAG, "onAllowAttendeeViewAllQuestionStatus:" + enable);
        refreshSetting();
    }

    @Override
    public void onAllowAttendeeVoteupQuestionStatus(boolean enable) {
        Log.d(TAG, "onAllowAttendeeVoteupQuestionStatus:" + enable);
        refreshSetting();
    }

    @Override
    public void onAllowAttendeeCommentQuestionStatus(boolean enable) {
        Log.d(TAG, "onAllowAttendeeCommentQuestionStatus:" + enable);
        refreshSetting();
    }

    @Override
    public void onQAConnected(boolean connected) {
        Log.d(TAG, "onQAConnected:" + connected);
    }

    @Override
    public void onAddQuestion(String questionId, boolean success) {
        Log.d(TAG, "onAddQuestion:" + questionId + ":" + success);
        refresh();
    }

    @Override
    public void onAddAnswer(String answerID, boolean success) {
        Log.d(TAG, "onAddAnswer:" + answerID + ":" + success);
        refresh();
    }

    @Override
    public void onReceiveQuestion(String questionID) {
        Log.d(TAG, "onReceiveQuestion:" + questionID);
        refresh();
    }

    @Override
    public void onReceiveAnswer(String answerID) {
        Log.d(TAG, "onReceiveAnswer:" + answerID);
        refresh();
    }


    @Override
    public void onQuestionMarkedAsDismissed(String questionId) {
        Log.d(TAG, "onQuestionMarkedAsDismissed:" + questionId);
        refresh();
    }

    @Override
    public void onReopenQuestion(String questionId) {
        Log.d(TAG, "onReopenQuestion:" + questionId);
        refresh();
    }

    @Override
    public void onUserLivingReply(String questionID) {
        Log.d(TAG, "onUserLivingReply:" + questionID);
        refresh();
    }

    @Override
    public void onUserEndLiving(String questionID) {
        Log.d(TAG, "onUserEndLiving:" + questionID);
        refresh();
    }

    @Override
    public void onUpvoteQuestion(String questionID, boolean order_changed) {
        Log.d(TAG, "onUpvoteQuestion:" + questionID + ":" + order_changed);
        refresh();
    }

    @Override
    public void onRevokeUpvoteQuestion(String questionID, boolean order_changed) {
        Log.d(TAG, "onRevokeUpvoteQuestion:" + questionID + ":" + order_changed);
        refresh();
    }


    /**
     * ----------------meeting callback
     **/




    @Override
    public void onDeleteQuestion(List<String> questionList) {
        refresh();
    }

    @Override
    public void onDeleteAnswer(List<String> answerList) {
        refresh();
    }



    @Override
    public void onMeetingStatusChanged(MeetingStatus meetingStatus, int errorCode, int internalErrorCode) {
        if (meetingStatus == MeetingStatus.MEETING_STATUS_RECONNECTING) {
            finish();
        }
    }

    @Override
    public void onMeetingParameterNotification(MeetingParameter meetingParameter) {

    }
    private SimpleInMeetingListener inMeetingListener=new SimpleInMeetingListener() {
        @Override
        public void onMeetingFail(int errorCode, int internalErrorCode) {
            finish();
        }

        @Override
        public void onMeetingLeaveComplete(long ret) {
            finish();

        }
    };
}
