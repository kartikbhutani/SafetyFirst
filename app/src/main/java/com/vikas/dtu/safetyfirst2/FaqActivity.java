package com.vikas.dtu.safetyfirst2;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class FaqActivity extends AppCompatActivity {

    ListView list;
    int prevClickedPosition = 10000;
    int ansShown = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faq);

        list = (ListView) findViewById(R.id.listview);
        list.setAdapter(new CustAdapter(this,10000));

        /////////////////////////////REMOVED CODE /////////////////////////////
        /*list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Toast.makeText(MainActivity.this, "This is position " + position, Toast.LENGTH_SHORT).show();
                int lastPos = list.getFirstVisiblePosition();
                View v = list.getChildAt(0);
                int topOffset = (v==null)?0:v.getTop();
                if(prevClickedPosition!=position) {
                    list.setAdapter(new CustAdapter(FaqActivity.this, position));
                    ansShown = 1;
                }
                else{
                    if(ansShown==1) {
                        list.setAdapter(new CustAdapter(FaqActivity.this, 10000));
                        ansShown = 0;
                    }
                    else{
                        list.setAdapter(new CustAdapter(FaqActivity.this, position));
                        ansShown = 1;
                    }
                }
                list.setSelectionFromTop(lastPos,topOffset);
                prevClickedPosition = position;
            }
        });*/

        ////////////////////REMOVED CODE////////////////////////
    }
}

class FAQs{
    public String question;
    public String answer;

    FAQs(String question, String answer) {
        this.question = question;
        this.answer = answer;
    }
}

class CustAdapter extends BaseAdapter {

    Context context;
    int position;
    ArrayList<FAQs> list;
    CustAdapter(Context c,int position){
        context = c;
        this.position = position;
        list = new ArrayList<FAQs>();

        Resources res = c.getResources();
        String[] questions = res.getStringArray(R.array.faq_ques);
        String[] answers = res.getStringArray(R.array.faq_answers);
        for(int i=0;i<questions.length;i++){
            //////////////////CHANGED CODE ///////////////////////////
            //if(i==position){
            list.add(new FAQs("Question: "+questions[i],"Answer: " + answers[i]));
            //}
            /*else{
                list.add(new FAQs(questions[i],""));
            }*/

            ////////////////////CHANGED CODE ///////////////////////////
        }
    }
    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int i) {
        return list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row = inflater.inflate(R.layout.faq_row,viewGroup,false);

        TextView tvQues = (TextView) row.findViewById(R.id.tv_ques);
        TextView tvAnswer = (TextView) row.findViewById(R.id.tv_answer);

        FAQs faq = list.get(i);
        tvQues.setText(" \u2022 " + faq.question);
        tvAnswer.setText(faq.answer);

        return row;
    }
}


