// FILE: app/src/main/java/com/example/ccl3_ws2025_mindflow/MainActivity.kt
package com.example.ccl3_ws2025_mindflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.example.ccl3_ws2025_mindflow.di.MoodyApp
import com.example.ccl3_ws2025_mindflow.ui.history.HistoryViewModel
import com.example.ccl3_ws2025_mindflow.ui.home.HomeViewModel
import com.example.ccl3_ws2025_mindflow.ui.moodjourney.MoodJourneyViewModel
import com.example.ccl3_ws2025_mindflow.ui.navigation.NavGraph
import com.example.ccl3_ws2025_mindflow.ui.tasks.TaskViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as MoodyApp



        val homeViewModel = HomeViewModel(
            moodRepo = app.container.moodRepository,
            noteRepo = app.container.noteRepository,
            taskRepo = app.container.taskRepository
        )

        val taskViewModel = TaskViewModel(app.container.taskRepository)
        val historyViewModel = HistoryViewModel(app.container.taskRepository)
        val moodJourneyViewModel = MoodJourneyViewModel(app.container.moodRepository)

        setContent {
            val navController = rememberNavController()
            NavGraph(
                navController = navController,
                homeViewModel = homeViewModel,
                taskViewModel = taskViewModel,
                historyViewModel = historyViewModel,
                moodJourneyViewModel = moodJourneyViewModel
            )
        }
    }
}
