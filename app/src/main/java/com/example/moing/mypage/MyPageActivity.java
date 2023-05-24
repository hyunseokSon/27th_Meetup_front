package com.example.moing.mypage;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.moing.R;
import com.example.moing.Response.MyPageResponse;
import com.example.moing.retrofit.ChangeJwt;
import com.example.moing.retrofit.RetrofitAPI;
import com.example.moing.retrofit.RetrofitClientJwt;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyPageActivity extends AppCompatActivity {
    private static final String TAG = "MyPageActivity";
    private static final String PREF_NAME = "Token";
    private static final String JWT_ACCESS_TOKEN = "JWT_access_token";
    private ImageView ivProfile;
    private ImageView ivModify;
    private TextView tvNickName;
    private TextView tvIntroduction;
    private Button btnAlarms;
    private Button btnLogOut;
    private Button btnDropOut;
    private TextView tvTeamCnt;
    private RecyclerView rvTeamList;
    private MyPageTeamAdatper teamAdatper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_page);


        // 프로필
        ivProfile = findViewById(R.id.mypage_iv_profile);
        // 프로필 설정
        ivModify = findViewById(R.id.mypage_iv_modify);
        // 닉네임
        tvNickName = findViewById(R.id.mypage_tv_nickname);
        // 한줄다짐
        tvIntroduction = findViewById(R.id.mypage_tv_introduction);
        // 알림 설정
        btnAlarms = findViewById(R.id.mypage_btn_setting_alarms);
        // 로그 아웃
        btnLogOut = findViewById(R.id.mypage_btn_logout);
        // 회원 탈퇴
        btnDropOut = findViewById(R.id.mypage_btn_drop_out);
        // 참여한 소모임 개수
        tvTeamCnt = findViewById(R.id.mypage_tv_team_count);
        // 참여한 소모임 목록
        rvTeamList = findViewById(R.id.mypage_rv_team_list);


        // 프로필 설정 버튼 클릭 리스너
        ivModify.setOnClickListener(onModifyClickListener);
        // 알림 설정 버튼 클릭 리스너
        btnAlarms.setOnClickListener(onAlarmsClickListener);
        // 로그아웃 버튼 클릭 리스너
        btnLogOut.setOnClickListener(onLogOutClickListener);
        // 회원탈퇴 버튼 클릭 리스너
        btnDropOut.setOnClickListener(onDropOutClickListener);


        // 마이페이지에 필요한 정보들을 GET하여 화면상에 표시
        getMyPageInfo();
    }

    // 프로필 수정 버튼 클릭 - 프로필 정보 설정 액티비티로 이동
    View.OnClickListener onModifyClickListener = v -> {
        Intent intent = new Intent(getApplicationContext(), MyPageAlarmActivity.class);
        startActivity(intent);
    };

    // 로그아웃 버튼 클릭 - 로그아웃 절차 실행
    View.OnClickListener onLogOutClickListener = v -> {
        // TODO: 로그아웃 절차 구현
    };

    // 알림 설정 버튼 클릭 - 알림 설정 액티비티로 이동
    View.OnClickListener onAlarmsClickListener = v -> {
        Intent intent = new Intent(getApplicationContext(), MyPageAlarmActivity.class);
        startActivity(intent);
    };

    // 회원탈퇴 버튼 클릭 - 회원탈퇴 액티비티로 이동
    View.OnClickListener onDropOutClickListener = v -> {
        Intent intent = new Intent(getApplicationContext(), MyPageDropOutActivity.class);
        startActivity(intent);
    };

    // 마이페이지에 필요한 정보들을 GET하여 화면상에 표시
    private void getMyPageInfo() {
        // Token 을 가져오기 위한 SharedPreferences Token
        SharedPreferences sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String jwtAccessToken = sharedPreferences.getString(JWT_ACCESS_TOKEN, null);
        Log.d(TAG, jwtAccessToken);


        RetrofitAPI apiService = RetrofitClientJwt.getApiService(jwtAccessToken);
        Call<MyPageResponse> call = apiService.getMyPage(jwtAccessToken);
        call.enqueue(new Callback<MyPageResponse>() {
            @Override
            public void onResponse(@NonNull Call<MyPageResponse> call, @NonNull Response<MyPageResponse> response) {
                // 연결 성공
                if (response.isSuccessful()) {
                    if (response.body() != null) {

                        // 프로필 이미지 설정
                        Glide.with(getApplicationContext())
                                .load(response.body().getData().getProfileImg())
                                        .into(ivProfile);

                        // 닉네임 설정
                        tvNickName.setText(response.body().getData().getNickName());

                        // 한줄다짐 설정
                        tvIntroduction.setText(response.body().getData().getIntroduction());

                        // 참여한 소모임 목록
                        List<MyPageResponse.Team> teamList = response.body().getData().getTeamList();

                        // 참여한 소모임 개수 설정
                        String teamCntText = teamList.size()+"개";
                        tvTeamCnt.setText(teamCntText);

                        // 참여한 소모임 목록 리사이클러뷰 어댑터 설정
                        teamAdatper = new MyPageTeamAdatper(getApplicationContext(),teamList);

                        // 참여한 소모임 목록 리사이클러뷰 생성
                        setupRecyclerView();

                    }
                } else if (response.message().equals("만료된 토큰입니다.")) {
                    Log.d(TAG, response.message());
                    // 토큰 재발급 후 다시 호출
                    ChangeJwt.updateJwtToken(MyPageActivity.this);
                    getMyPageInfo();
                }
            }

            @Override
            public void onFailure(@NonNull Call<MyPageResponse> call, @NonNull Throwable t) {
                // 응답 실패
                Log.d(TAG, "마이페이지 조회 실패");
            }
        });
    }

    private void setupRecyclerView() {
        // 리사이클러뷰에 사용할 레이아웃매니저 - Grid 3개의 열
        GridLayoutManager layoutManager = new GridLayoutManager(getApplicationContext(), 3);
        rvTeamList.setLayoutManager(layoutManager);

        // 리사이클러뷰 아이템들 간 간격 설정
        rvTeamList.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@android.support.annotation.NonNull Rect outRect, @android.support.annotation.NonNull View view, @android.support.annotation.NonNull RecyclerView parent, @android.support.annotation.NonNull RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                outRect.set(0, 0, 0, 0);
            }
        });

        // 리사이클러뷰에 사용할 어댑터 설정
        rvTeamList.setAdapter(teamAdatper);
    }
}