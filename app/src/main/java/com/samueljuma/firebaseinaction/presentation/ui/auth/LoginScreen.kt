package com.samueljuma.firebaseinaction.presentation.ui.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.samueljuma.firebaseinaction.core.utils.ObserveAsEvents
import com.samueljuma.firebaseinaction.core.utils.showSnackbar
import com.samueljuma.firebaseinaction.presentation.designsystem.AppTheme
import kotlinx.coroutines.launch
import com.samueljuma.firebaseinaction.R

@Composable
fun LoginScreenRot(
    viewModel: AuthViewModel,
    onNavigateToHome: () -> Unit,
    onNavigateToSignUp: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    LoginScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onAction = { action ->
            when(action){
                AuthAction.OnGoToSignUp -> onNavigateToSignUp()
                else -> Unit
            }
            viewModel.onAction(action)
        }
    )

    ObserveAsEvents(viewModel.events) { event ->
        when(event){
            AuthEvent.Login.LoginSuccess -> onNavigateToHome()
            is AuthEvent.Login.ShowSnackbar -> {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(event.message, context)
                }
            }
            else -> Unit
        }
    }
}

@Composable
private fun LoginScreen(
    state: AuthState,
    snackbarHostState: SnackbarHostState,
    onAction: (AuthAction) -> Unit
) {

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        content = {padding ->
            Column(modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Welcome Back",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Sign in to continue",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(32.dp))

                OutlinedTextField(
                    value = state.email,
                    onValueChange = { onAction(AuthAction.OnEmailChanged(it)) },
                    label = { Text("Email") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = state.password,
                    onValueChange = { onAction(AuthAction.OnPasswordChanged(it)) },
                    label = { Text("Password") },
                    visualTransformation = if (state.isPasswordVisible)
                        VisualTransformation.None
                    else
                        PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    trailingIcon = {
                        IconButton(
                            onClick = { onAction(AuthAction.OnTogglePasswordVisibility) }
                        ) {
                            Icon(
                                imageVector = if (state.isPasswordVisible)
                                    Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (state.isPasswordVisible) "Hide password" else "Show password"
                            )
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20)
                )
                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { onAction(AuthAction.OnSignInClicked) },
                    enabled = !state.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(20)
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Sign In")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                Text(
                    text = "or",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = { onAction(AuthAction.OnGoogleSignInClicked) },
                    enabled = !state.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    border = BorderStroke(
                        width = 1.5.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_google),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Continue with Google")
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(onClick = {onAction(AuthAction.OnGoToSignUp)}) {
                    Text("Don't have an account? Sign Up",)
                }

            }
        }
    )
}
@PreviewLightDark
@Composable
private fun LoginScreenPreview() {
    AppTheme{
       LoginScreen(
           state = AuthState(),
           snackbarHostState = remember { SnackbarHostState() },
           onAction = {}
       )
   }
}
