package tw.edu.pu.csim.tcyang.mole

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

class MoleViewModel : ViewModel() {

    // 遊戲分數 (Score)
    // 沿用您的 mutableLongStateOf
    var counter by mutableLongStateOf(0)
        private set

    // 遊戲已進行秒數 (Elapsed time in seconds)
    // 改用 StateFlow 處理，因為它更適合長時間運行的數據流
    private val _stay = MutableStateFlow(0)
    val stay: StateFlow<Int> = _stay.asStateFlow()

    // 遊戲結束狀態 (Game Over state)
    private val _isGameOver = MutableStateFlow(false)
    val isGameOver: StateFlow<Boolean> = _isGameOver.asStateFlow()

    // 地鼠位置 (Mole position)
    // 改用 mutableIntStateOf
    var offsetX by mutableIntStateOf(0)
        private set
    var offsetY by mutableIntStateOf(0)
        private set

    // 移動邊界參數
    private var screenWidth = 0
    private var screenHeight = 0
    private var moleSizePx = 0

    // Coroutine Jobs 用於控制計時和移動
    private var gameJob: Job? = null
    private var moleMovementJob: Job? = null

    init {
        startGame()
    }

    private fun startGame() {
        if (gameJob?.isActive == true) return // 避免重複啟動計時器

        // 1. 計時器與遊戲結束邏輯 (Timer and Game Over Logic)
        gameJob = viewModelScope.launch {
            for (i in 0..60) { // 從 0 秒計數到 60 秒
                if (i == 60) {
                    _isGameOver.value = true // 達到 60 秒，遊戲結束
                    moleMovementJob?.cancel() // 停止地鼠的移動 Job
                    break // 停止計時
                }
                _stay.value = i // 更新已進行秒數
                delay(1000) // 等待 1 秒
            }
        }

        // 2. 地鼠移動邏輯 (Mole Movement Logic)
        moleMovementJob = viewModelScope.launch {
            while (!_isGameOver.value) {
                // 只有在遊戲未結束時才移動
                moveMole()
                delay(700) // 每 0.7 秒移動一次
            }
        }
    }

    // 設置遊戲區域尺寸
    fun getArea(intSize: androidx.compose.ui.unit.IntSize, moleSize: Int) {
        screenWidth = intSize.width
        screenHeight = intSize.height
        moleSizePx = moleSize
        // 第一次取得尺寸後，移動地鼠到初始位置
        if (offsetX == 0 && offsetY == 0) {
            moveMole()
        }
    }

    // 移動地鼠到隨機位置
    private fun moveMole() {
        if (_isGameOver.value) return // 遊戲結束則停止移動

        // 計算最大允許的 X, Y 偏移量
        val maxX = screenWidth - moleSizePx
        // 設置地鼠不會跑到頂部標題後面 (預留約 200px 高度)
        val maxY = screenHeight - moleSizePx - 200

        if (maxX > 0 && maxY > 0) {
            offsetX = Random.nextInt(maxX)
            // Y 軸從分數/標題區域下方開始隨機
            offsetY = Random.nextInt(maxY) + 100
        }
    }

    // 點擊地鼠，分數增加
    fun incrementCounter() {
        if (!_isGameOver.value) { // 只有在遊戲未結束時才加分
            counter++
        }
        // 點擊後立即移動地鼠，增強遊戲體驗
        moveMole()
    }

    override fun onCleared() {
        super.onCleared()
        // ViewModel 銷毀時，取消所有 Job
        gameJob?.cancel()
        moleMovementJob?.cancel()
    }
}

