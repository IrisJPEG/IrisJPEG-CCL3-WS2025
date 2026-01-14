package com.example.ccl3_ws2025_mindflow.ui.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.ccl3_ws2025_mindflow.ui.breathing.BreathingScreen
import com.example.ccl3_ws2025_mindflow.ui.breathing.breathingExercises
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
import com.example.ccl3_ws2025_mindflow.ui.history.NoteHistoryScreen
import com.example.ccl3_ws2025_mindflow.ui.history.NoteHistoryViewModel


@Composable
fun NavGraph(
    navController: NavHostController,
    homeViewModel: HomeViewModel,
    taskViewModel: TaskViewModel,
    historyViewModel: HistoryViewModel,
    moodJourneyViewModel: MoodJourneyViewModel,
    noteHistoryViewModel: NoteHistoryViewModel
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

        composable(
            route = "breathing/{exerciseId}",
            arguments = listOf(navArgument("exerciseId") { type = NavType.StringType })
        ) { backStack ->
            val exerciseId = backStack.arguments?.getString("exerciseId")

            val exercise = breathingExercises.find { it.id == exerciseId }

            if (exercise != null) {
                BreathingScreen(
                    exercise = exercise,
                    navController = navController // <- pass this!
                )
            } else {
                Text("Breathing exercise not found")
            }
        }
        composable("noteHistory") {
            NoteHistoryScreen(
                navController = navController,
                viewModel = noteHistoryViewModel
            )
        }


    }
}
