package com.example.aiotap_test

//필요한 기능을 구현하기 위한 import
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Switch
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.google.firebase.FirebaseApp
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import androidx.core.graphics.toColorInt
import com.google.firebase.database.*

class MainActivity : ComponentActivity() {

    private lateinit var lineChart: LineChart

    // SharedPreferences 파일명·키
    private val PREFS_NAME = "power1_prefs"
    private val KEY_POWER1_NAME = "power1_name"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //  Firebase 초기화
        FirebaseApp.initializeApp(this)
        Log.d("FirebaseTest", "Firebase 초기화 성공 여부: ${FirebaseApp.getApps(this).isNotEmpty()}")

        //  Firebase에 메시지 쓰기
        val database = Firebase.database
        val myRef = database.getReference("message")
        myRef.setValue("파이어베이스-안드로이드스튜디오 연동 성공")

        //  센서값 요금(cost)
        val sensorRef = database.getReference("sensor")
        sensorRef.child("cost").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val sensorValue = (snapshot.value as? Long) ?: 0L
                val precost = getString(R.string.pre_cost, sensorValue)
                findViewById<TextView>(R.id.cost).text = precost
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w("FirebaseRead", "센서값 읽기 실패", error.toException())
            }
        })

        //  센서값 절약 요금(reducepower)
        sensorRef.child("reducepower").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val sensorValue = (snapshot.value as? Long) ?: 0L
                val reducepower = getString(R.string.reducepower, sensorValue)
                findViewById<TextView>(R.id.reducepower).text = reducepower
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w("FirebaseRead", "센서값 읽기 실패", error.toException())
            }
        })

        //  토글스위치 4개 연결
        setupSwitch(R.id.switch1, "switch1")
        setupSwitch(R.id.switch2, "switch2")
        setupSwitch(R.id.switch3, "switch3")
        setupSwitch(R.id.switch4, "switch4")

        //  FCM 토큰 받기
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("FirebaseToken", "토큰: ${task.result}")
                } else {
                    Log.w("FirebaseToken", "토큰 받기 실패", task.exception)
                }
            }

        //  알림 권한 요청 (Android 13 이상)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    100
                )
            }
        }

        //  알림 채널 설정 (Android O 이상)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "default_channel"
            val channelName = "기본 알림 채널"
            val importance = android.app.NotificationManager.IMPORTANCE_DEFAULT
            val channel = android.app.NotificationChannel(channelId, channelName, importance)
            val notificationManager =
                getSystemService(android.app.NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        //  차트 설정(임의 값)
        lineChart = findViewById(R.id.line_chartmain)
        val entries = listOf(
            Entry(1f, 60f),
            Entry(2f, 30f),
            Entry(3f, 30f),
            Entry(4f, 30f),
            Entry(5f, 50f),
            Entry(6f, 30f),
            Entry(7f, 45f),
            Entry(8f, 40f)
        )
        ChartSetup.setupLineChart(
            chart = lineChart,
            entries = entries,
            label = getString(R.string.power_concent1),
            lineColor = "#87CEE8".toColorInt(),
            circleColor = "#00BFFF".toColorInt()
        )

        // 페이지 터치시 이동
        findViewById<TextView>(R.id.power1).setOnClickListener {
            startActivity(Intent(this, Power1Activity::class.java))
        }
        findViewById<TextView>(R.id.power2).setOnClickListener {
            startActivity(Intent(this, Power2Activity::class.java))
        }
        findViewById<TextView>(R.id.power3).setOnClickListener {
            startActivity(Intent(this, Power3Activity::class.java))
        }
        findViewById<TextView>(R.id.power4).setOnClickListener {
            startActivity(Intent(this, Power4Activity::class.java))
        }

        // 알림 테스트
        val builder = NotificationCompat.Builder(this, "default_channel")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("테스트 알림")
            .setContentText("이것은 테스트 알림입니다~.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        try {
            NotificationManagerCompat.from(this).notify(0, builder.build())
        } catch (e: SecurityException) {
            Log.e("Notification", "알림 권한 없음", e)
        }
    }

    // ✅ 스위치별 설정 함수
    private fun setupSwitch(switchId: Int, dbKey: String) {
        val toggle = findViewById<Switch>(switchId)
        val ref = FirebaseDatabase.getInstance().getReference(dbKey)

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val isOn = snapshot.getValue(Boolean::class.java) ?: false
                if (toggle.isChecked != isOn) {
                    toggle.isChecked = isOn
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("MainActivity", "DB 읽기 실패: $dbKey", error.toException())
            }
        })

        toggle.setOnCheckedChangeListener { _, isChecked ->
            ref.setValue(isChecked)
                .addOnFailureListener { e ->
                    Log.e("MainActivity", "DB 쓰기 실패: $dbKey", e)
                }
        }
    }

    // Power1Activity에서 돌아올 때 이름 복원
    override fun onResume() {
        super.onResume()
        val prefs1 = getSharedPreferences("power1_prefs", Context.MODE_PRIVATE)
        val prefs2 = getSharedPreferences("power2_prefs", Context.MODE_PRIVATE)
        val prefs3 = getSharedPreferences("power3_prefs", Context.MODE_PRIVATE)
        val prefs4 = getSharedPreferences("power4_prefs", Context.MODE_PRIVATE)

        findViewById<TextView>(R.id.power1).text =
            prefs1.getString("power1_name", getString(R.string.power_concent1))

        findViewById<TextView>(R.id.power2).text =
            prefs2.getString("power2_name", getString(R.string.power_concent2))

        findViewById<TextView>(R.id.power3).text =
            prefs3.getString("power3_name", getString(R.string.power_concent3))

        findViewById<TextView>(R.id.power4).text =
            prefs4.getString("power4_name", getString(R.string.power_concent4))
    }

}
