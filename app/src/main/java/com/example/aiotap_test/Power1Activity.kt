package com.example.aiotap_test

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.graphics.toColorInt
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

class Power1Activity : AppCompatActivity() {
    // SharedPreferences 파일명 및 키 정의
    private val PREFS_NAME      = "power1_prefs"
    private val KEY_POWER1_NAME = "power1_name"
    // 차트 뷰
    private lateinit var lineChart: LineChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_power1)

        // 1) 뷰 바인딩
        val backImageView = findViewById<ImageView>(R.id.back)
        val editIcon      = findViewById<ImageView>(R.id.editname)
        val powerLabel    = findViewById<TextView>(R.id.power1)
        val accPowerTextView = findViewById<TextView>(R.id.acc_power1) //  누적 전력 텍스트뷰
        val accCostTextView = findViewById<TextView>(R.id.acc_cost1)   //  누적 요금 텍스트뷰
        val outCountTextView = findViewById<TextView>(R.id.out_count1) //  대기전력차단횟수

        // 대기전력차단횟수 기본값 0 설정
        outCountTextView.text = "0회" // 대기전력차단횟수 기본값

        // 2) SharedPreferences에서 저장된 이름 불러오기
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.getString(KEY_POWER1_NAME, null)
            ?.takeIf { it.isNotEmpty() }
            ?.let { powerLabel.text = it }

        // 뒤로 가기 클릭 리스너
        backImageView.setOnClickListener {
            finish()
        }

        // 수정 아이콘 클릭 리스너
        editIcon.setOnClickListener {
            val input = EditText(this).apply {
                hint = "새 이름을 입력하세요"
                setText(powerLabel.text)
                setSelection(text.length)
            }

            AlertDialog.Builder(this)
                .setTitle("콘센트 이름 변경")
                .setView(input)
                .setNegativeButton("취소", null)
                .setPositiveButton("확인") { _, _ ->
                    val newName = input.text.toString().trim()
                    if (newName.isNotEmpty()) {
                        powerLabel.text = newName
                        prefs.edit {
                            putString(KEY_POWER1_NAME, newName)
                        }
                    }
                }
                .show()
        }

        //  차트 설정
        lineChart = findViewById(R.id.line_chart1)

        //  Firebase logs에서 최신 10개 current 값 가져와 그래프에 그리기
        val logsRef = Firebase.database.getReference("logs1")
        logsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val logs = mutableListOf<Pair<Long, Double>>()  // timestamp, current

                for (child in snapshot.children) {
                    val current = child.child("current").value?.toString()?.toDoubleOrNull()
                    val timestampStr = child.child("timestamp").value?.toString()
                    val timestamp = parseTimestamp(timestampStr)
                    if (current != null && timestamp != null) {
                        logs.add(Pair(timestamp.time, current))
                    }
                }

                // 최신순 정렬 후 10개만 사용
                val latestLogs = logs.sortedByDescending { it.first }.take(10).reversed()
                // 전력 계산하여 Entry 생성
                val entries = latestLogs.mapIndexed { index, pair ->
                    val power = pair.second * 220.0  // 전류 * 220V = 전력(W)
                    Entry(index.toFloat() + 1f, power.toFloat())
                }

                //  ChartSetup.kt에서 설정 적용
                if (entries.isNotEmpty()) {
                    ChartSetup.setupLineChart(
                        chart = lineChart,
                        entries = entries,
                        label = getString(R.string.power_concent1),
                        lineColor = "#FFA500".toColorInt(),
                        circleColor = "#FF4500".toColorInt()
                    )
                }

                //  한달 누적 전력값 계산 및 표시
                val now = Date()
                val calendar = Calendar.getInstance().apply {
                    time = now
                    add(Calendar.DAY_OF_YEAR, -30)
                }
                val cutoff = calendar.time
                var accPowerWh = 0.0
                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

                for (child in snapshot.children) {
                    val current = child.child("current").value?.toString()?.toDoubleOrNull()
                    val timestampStr = child.child("timestamp").value?.toString()
                    val timestamp = try {
                        sdf.parse(timestampStr ?: "")
                    } catch (e: Exception) {
                        null
                    }

                    if (current != null && timestamp != null && timestamp.after(cutoff)) {
                        val power = current * 220.0 // W
                        val energyWh = power * (1.0 / 60.0) // Wh (1분간 사용)
                        accPowerWh += energyWh
                    }
                }

                // 소수점 둘째자리까지 출력
                accPowerTextView.text = String.format("%.2f kw", accPowerWh / 1000)

                // ✅ 누적 요금 계산 및 표시 (원)
                val ratePerKWh = 121.32  // 실제 단가로 변경하세요 (원/kWh)
                val accKWh = accPowerWh / 1000.0
                val accCost = accKWh * ratePerKWh
                accCostTextView.text = String.format("%.2f 원", accCost)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("FirebaseRead", "로그 불러오기 실패", error.toException())
            }
        })

        // ✅ 전력값 계산 및 표시
        val current1Ref = Firebase.database.getReference("current1")
        current1Ref.child("current").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val currentValue = (snapshot.value as? Number)?.toDouble() ?: 0.0
                val voltage = 220.0
                val power = currentValue * voltage  // 전력 W 단위

                // 전력 출력
                val real_power1 = getString(R.string.real_power1, power)
                findViewById<TextView>(R.id.real_power1).text = real_power1

                // 전기요금 계산 (1분 사용 기준)
                val ratePerKWh = 121.32  // 실제 단가로 변경하세요 (원/kWh)
                val usageKWh = (power / 1000.0) * (1.0 / 60.0)  // kWh 단위 (분 단위 사용)
                val cost = usageKWh * ratePerKWh

                val real_cost1 = getString(R.string.real_cost1, cost)
                findViewById<TextView>(R.id.real_cost1).text = real_cost1
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("FirebaseRead", "전력값 읽기 실패", error.toException())
            }
        })
    }

    private fun parseTimestamp(timestampStr: String?): Date? {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            sdf.parse(timestampStr ?: "")
        } catch (e: Exception) {
            null
        }
    }
}
