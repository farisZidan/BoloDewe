package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.data.repository.BoloDeweRepository
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.BoloDeweViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Edge to edge drawing enabled for full-bleed background as per Android styling guidelines
        enableEdgeToEdge()

        // Bootstrap Repository
        val repository = BoloDeweRepository(applicationContext)
        
        // Constructor injection of BoloDeweViewModel and initialization via Factory
        val viewModel = ViewModelProvider(
            this, 
            BoloDeweViewModel.Factory(repository)
        )[BoloDeweViewModel::class.java]

        setContent {
            MyApplicationTheme {
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = "splash"
                ) {
                    // Splash Screen
                    composable("splash") {
                        SplashScreen(
                            onAnimationFinished = {
                                val targetDest = if (repository.isLoggedIn()) "main" else "login"
                                navController.navigate(targetDest) {
                                    popUpTo("splash") { inclusive = true }
                                }
                            }
                        )
                    }

                    // 1. Auth Page
                    composable("login") {
                        LoginScreen(
                            viewModel = viewModel,
                            onLoginSuccess = {
                                navController.navigate("main") {
                                    popUpTo("login") { inclusive = true }
                                }
                            }
                        )
                    }

                    // 2. Main Tab View (Laporan, Transaksi, Produk)
                    composable("main") {
                        MainAppContainer(
                            viewModel = viewModel,
                            onNavigateToTransactionsDetail = {
                                navController.navigate("transaksi_terkini")
                            },
                            onNavigateToProductDetail = { id ->
                                navController.navigate("product_detail/$id")
                            },
                            onNavigateToProductCreate = {
                                navController.navigate("product_create")
                            },
                            onLogout = {
                                viewModel.logout()
                                navController.navigate("login") {
                                    popUpTo("main") { inclusive = true }
                                }
                            }
                        )
                    }

                    // 3. Transactions Detail View ("Lihat Semua")
                    composable("transaksi_terkini") {
                        TransaksiTerkiniScreen(
                            viewModel = viewModel,
                            onNavigateBack = {
                                navController.popBackStack()
                            }
                        )
                    }

                    // 4. Product Edit Detail Sheet
                    composable(
                        route = "product_detail/{id}",
                        arguments = listOf(navArgument("id") { type = NavType.IntType })
                    ) { backStackEntry ->
                        val id = backStackEntry.arguments?.getInt("id")
                        com.example.ui.screens.ProductDetailScreenNew(
                            productId = id,
                            viewModel = viewModel,
                            onNavigateBack = {
                                navController.popBackStack()
                            }
                        )
                    }

                    // 5. Product Creation Sheet
                    composable("product_create") {
                        com.example.ui.screens.ProductDetailScreenNew(
                            productId = null,
                            viewModel = viewModel,
                            onNavigateBack = {
                                navController.popBackStack()
                            }
                        )
                    }
                }
            }
        }
    }
}
