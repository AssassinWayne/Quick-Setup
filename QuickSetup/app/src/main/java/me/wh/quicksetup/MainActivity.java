package me.wh.quicksetup;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

import me.wh.quicksetup.logic.GitHubLogic;

public class MainActivity extends RxAppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button followingBtn = (Button) findViewById(R.id.following_btn);
        followingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GitHubLogic.get().getFollowing(MainActivity.this, "AssassinWayne");
            }
        });
    }
}
