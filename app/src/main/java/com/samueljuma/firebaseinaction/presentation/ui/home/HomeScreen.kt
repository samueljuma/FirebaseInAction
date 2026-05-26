package com.samueljuma.firebaseinaction.presentation.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.samueljuma.firebaseinaction.presentation.designsystem.AppTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeScreenRoot(
    viewModel: HomeViewModel = koinViewModel(),
    onLogout: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    HomeScreenScreen(
        state = state,
        onAction = viewModel::onAction
    )
}

@Composable
private fun HomeScreenScreen(
    state: HomeState,
    onAction: (HomeAction) -> Unit
) {
    Scaffold(
        content = {paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Logged in as ${state.loggedInUser?.email}")
            }
        }
    )

}

@Preview
@Composable
private fun HomeScreenScreenPreview() {
    AppTheme{
       HomeScreenScreen(
           state = HomeState(),
           onAction = {}
       )
   }
}