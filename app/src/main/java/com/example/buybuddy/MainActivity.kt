package com.example.buybuddy

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.buybuddy.sign_in_google.GoogleAuthUiClient
import com.example.buybuddy.ui.screens.AddElementsScreen
import com.example.buybuddy.ui.screens.AddListScreen
import com.example.buybuddy.ui.screens.ListElementsScreen
import com.example.buybuddy.ui.screens.MainScreen
import com.example.buybuddy.ui.screens.ProfileScreen
import com.example.buybuddy.ui.screens.SignInScreen
import com.example.buybuddy.ui.theme.BuyBuddyTheme
import com.example.buybuddy.viewmodels.AddElementsViewModel
import com.example.buybuddy.viewmodels.AddListViewModel
import com.example.buybuddy.viewmodels.MainViewModel
import com.example.buybuddy.viewmodels.AuthViewModel
import com.example.buybuddy.viewmodels.ListElementsViewModel
import com.example.buybuddy.viewmodels.ProfileViewModel
import com.google.android.gms.auth.api.identity.Identity
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val googleAuthUiClient by lazy {
        GoogleAuthUiClient(
            context = applicationContext,
            oneTapClient = Identity.getSignInClient(applicationContext)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BuyBuddyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val context = applicationContext
                    NavHost(
                        navController = navController,
                        startDestination = DestinationScreen.SignIn.route
                    ) {
                        composable(DestinationScreen.SignIn.route) {
                            val viewModel = AuthViewModel(navController, context)

                            LaunchedEffect(key1 = Unit) {
                                if (googleAuthUiClient.getSignedInUser() != null) {
                                    navController.navigate(DestinationScreen.Main.route)
                                }
                            }

                            val launcher = rememberLauncherForActivityResult(
                                contract = ActivityResultContracts.StartIntentSenderForResult(),
                                onResult = { result ->
                                    if (result.resultCode == RESULT_OK) {
                                        lifecycleScope.launch {
                                            val signInResult = googleAuthUiClient.signInWithIntent(
                                                intent = result.data ?: return@launch
                                            )
                                            viewModel.onSignInResult(signInResult)
                                        }
                                    }
                                }
                            )



                            SignInScreen(
                                context = context,
                                viewModel = viewModel,
                                navController = navController,
                                onSignInClick = {
                                    lifecycleScope.launch {
                                        val signInIntentSender = googleAuthUiClient.signIn()
                                        launcher.launch(
                                            IntentSenderRequest.Builder(
                                                signInIntentSender ?: return@launch
                                            ).build()
                                        )
                                    }
                                }
                            )
                        }

                        composable(DestinationScreen.Profile.route) {
                            val viewModel = ProfileViewModel(navController, context)

                            ProfileScreen(
                                userData = googleAuthUiClient.getSignedInUser(),
                                viewModel,
                                navController,
                                onSignOut = {
                                    lifecycleScope.launch {
                                        googleAuthUiClient.signOut()
                                        Toast.makeText(
                                            applicationContext,
                                            "Signed out",
                                            Toast.LENGTH_LONG
                                        ).show()

                                        navController.navigate(DestinationScreen.SignIn.route)
                                    }
                                }
                            )
                        }

                        composable(DestinationScreen.AddList.route) {
                            val viewModel = AddListViewModel(navController, context)

                            AddListScreen(
                                navController,
                                viewModel
                            )
                        }
                        composable(DestinationScreen.Main.route) {
                            val viewModel = MainViewModel(context, navController)

                            MainScreen(
                                userData = googleAuthUiClient.getSignedInUser(),
                                viewModel = viewModel,
                                navController = navController
                            )
                        }

                        composable(
                            DestinationScreen.ListElements.route,
                            arguments = listOf(navArgument("listId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val listId = backStackEntry.arguments?.getString("listId") ?: ""
                            val viewModel = ListElementsViewModel(listId, context)
                            ListElementsScreen(viewModel = viewModel, navController)
                        }

                        composable(
                            DestinationScreen.AddElement.route,
                            arguments = listOf(navArgument("listId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val listId = backStackEntry.arguments?.getString("listId") ?: ""
                            val viewModel = AddElementsViewModel(listId, context)
                            AddElementsScreen(viewModel = viewModel, navController)
                        }
                    }
                }
            }
        }
    }
}