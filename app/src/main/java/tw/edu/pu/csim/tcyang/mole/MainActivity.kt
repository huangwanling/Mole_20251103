package tw.edu.pu.csim.tcyang.mole

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import tw.edu.pu.csim.tcyang.mole.ui.theme.MoleTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MoleTheme {
                MoleScreen()
            }
        }
    }
}

@Composable
fun MoleScreen(moleViewModel: MoleViewModel = viewModel()) {
    // 從 ViewModel 取得狀態
    val counter = moleViewModel.counter // 分數
    val stay by moleViewModel.stay.collectAsState() // 遊戲已進行秒數 (0-60)
    val isGameOver by moleViewModel.isGameOver.collectAsState() // 遊戲結束狀態

    // DP-to-pixel轉換
    val density = LocalDensity.current

    // 地鼠Dp轉Px
    val moleSizeDp = 150.dp
    val moleSizePx = with(density) { moleSizeDp.roundToPx() }

    // 計算剩餘時間 (60 - 已進行秒數)
    val remainingTime = 60 - stay
    // 確保時間不會顯示負數
    val displayTime = if (remainingTime < 0) 0 else remainingTime


    Box(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { intSize ->
                // 用來獲取全螢幕尺寸px，並傳遞給 ViewModel
                moleViewModel.getArea(intSize, moleSizePx)
            },
        // 將分數和標題置於頂部中央
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 30.dp)
        ) {
            // 遊戲標題 (包含您的姓名)
            Text(
                text = "打地鼠遊戲(黃婉凌)",
                fontSize = 24.sp
            )
            // 分數與時間
            Text(
                text = "分數: $counter \n時間: $displayTime 秒",
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 10.dp)
            )

            // 遊戲結束訊息
            if (isGameOver) {
                Text(
                    text = "遊戲結束！\n您的分數是：$counter",
                    fontSize = 48.sp,
                    color = Color.Red,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 50.dp)
                )
            }
        }
    }

    // 地鼠圖片
    Image(
        painter = painterResource(id = R.drawable.mole), // 假設您的地鼠圖片 ID 為 R.drawable.mole
        contentDescription = "地鼠",
        modifier = Modifier
            // 設定地鼠位置 (由 ViewModel 控制)
            .offset { IntOffset(moleViewModel.offsetX, moleViewModel.offsetY) }
            .size(moleSizeDp)
            // 只有在遊戲未結束時 (isGameOver 為 false) 才允許點擊並加分
            .clickable(enabled = !isGameOver) { moleViewModel.incrementCounter() }
    )
}