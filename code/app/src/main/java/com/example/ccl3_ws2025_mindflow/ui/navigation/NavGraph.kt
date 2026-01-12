package com.example.ccl3_ws2025_mindflow.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.ccl3_ws2025_mindflow.ui.history.HistoryScreen
import com.example.ccl3_ws2025_mindflow.ui.history.HistoryViewModel
import com.example.ccl3_ws2025_mindflow.ui.home.HomeScreen
import com.example.ccl3_ws2025_mindflow.ui.home.HomeViewModel
import com.example.ccl3_ws2025_mindflow.ui.notes.NoteToSelfScreen
import com.example.ccl3_ws2025_mindflow.ui.tasks.AddEditTaskScreen
import com.example.ccl3_ws2025_mindflow.ui.tasks.TaskListScreen
import com.example.ccl3_ws2025_mindflow.ui.tasks.TaskViewModel
import com.example.ccl3_ws2025_mindflow.ui.moodjourney.MoodJourneyScreen
import com.example.ccl3_ws2025_mindflow.ui.moodjourney.MoodJourneyViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    homeViewModel: HomeViewModel,
    taskViewModel: TaskViewModel,
    historyViewModel: HistoryViewModel,
    moodJourneyViewModel: MoodJourneyViewModel
) {
    NavHost(navController = navController, startDestination = "home") {

        composable("home") {
            HomeScreen(navController = navController, viewModel = homeViewModel)
        }

        composable("tasks") {
            TaskListScreen(navController = navController, viewModel = taskViewModel)
        }

        composable(
            route = "addEditTask/{id}",
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) { backStack ->
            val id = backStack.arguments?.getLong("id") ?: -1L
            AddEditTaskScreen(navController = navController, viewModel = taskViewModel, taskId = id)
        }

        composable("history") {
            HistoryScreen(navController = navController, viewModel = historyViewModel)
        }

        composable("noteToSelf") {
            NoteToSelfScreen(navController = navController, viewModel = homeViewModel)
        }

        // NEW
        composable("moodJourney") {
            MoodJourneyScreen(navController = navController, viewModel = moodJourneyViewModel)
        }
    }
}
