package com.example.aiotap_test.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

// 다크 모드 색상 테마 설정
private val DarkColorScheme = darkColorScheme(
    primary = Purple80,       // 기본 색상 (연보라)
    secondary = PurpleGrey80, // 보조 색상 (연한 회보라)
    tertiary = Pink80         // 강조 색상 (연한 분홍)
)

// 라이트 모드 색상 테마 설정
private val LightColorScheme = lightColorScheme(
    primary = Purple40,       // 기본 색상 (짙은 보라)
    secondary = PurpleGrey40, // 보조 색상 (짙은 회보라)
    tertiary = Pink40         // 강조 색상 (짙은 분홍)

    /* 다른 기본 색상을 변경하려면 아래 설정을 추가 가능
    background = Color(0xFFFFFBFE),  // 배경색
    surface = Color(0xFFFFFBFE),     // 표면 색상
    onPrimary = Color.White,         // 기본 색상 위의 텍스트/아이콘 색상
    onSecondary = Color.White,       // 보조 색상 위의 텍스트/아이콘 색상
    onTertiary = Color.White,        // 강조 색상 위의 텍스트/아이콘 색상
    onBackground = Color(0xFF1C1B1F), // 배경 위의 텍스트 색상
    onSurface = Color(0xFF1C1B1F),    // 표면 위의 텍스트 색상
    */
)

/**
 * AIOTapTest 앱의 전체 테마를 설정하는 함수
 * @param darkTheme 시스템의 다크 모드 여부 (기본값: 시스템 설정 따름)
 * @param dynamicColor 다이내믹 컬러 적용 여부 (Android 12 이상에서 사용 가능)
 * @param content 테마가 적용될 컴포저블 UI 요소
 */
@Composable
fun AIOTapTestTheme(
    darkTheme: Boolean = isSystemInDarkTheme(), // 시스템 다크 모드 여부를 기본값으로 사용
    dynamicColor: Boolean = true, // 다이내믹 컬러 활성화 여부
    content: @Composable () -> Unit
) {
    // Android 12(S) 이상에서는 다이내믹 컬러를 적용
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme // 다크 모드일 경우 다크 테마 적용
        else -> LightColorScheme     // 라이트 모드일 경우 라이트 테마 적용
    }

    // Material3 테마 적용
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // 앱의 타이포그래피 설정
        content = content // 적용할 UI 요소
    )
}
