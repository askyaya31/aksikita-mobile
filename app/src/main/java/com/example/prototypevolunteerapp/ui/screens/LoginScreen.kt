package com.example.prototypevolunteerapp.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.prototypevolunteerapp.R
import com.example.prototypevolunteerapp.core.LocalBackStack
import com.example.prototypevolunteerapp.core.Routes

private val DarkGreen  = Color(0xFF3D5C2A)
private val MidGreen   = Color(0xFF5A7A5A)
private val LightGreen = Color(0xFF9EB589)
private val InputBg    = Color(0x33FFFFFF)
private val White      = Color.White
private val HintColor  = Color(0xAAFFFFFF)

enum class LoginRole { VOLUNTEER, ORGANISASI }

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel()
) {
    val backStack = LocalBackStack.current
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val webClientId = stringResource(R.string.default_web_client_id)

    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(webClientId)
        .requestEmail()
        .build()

    val googleSignInClient = GoogleSignIn.getClient(context, gso)

    val googleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account.idToken
                if (idToken != null) {
                    viewModel.onGoogleLogin(idToken)
                }
            } catch (e: ApiException) {
                android.util.Log.e("LoginScreen", "Google sign-in failed: ${e.statusCode}")
            }
        }
    }

    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn) {
            if (uiState.role == LoginRole.VOLUNTEER) {
                backStack.add(Routes.HomeRoute)
            } else {
                backStack.add(Routes.OrgDashboardRoute)
            }
            viewModel.onLoginHandled()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(colors = listOf(
                Color(0xFF5A7A5A),
                Color(0xFF6E8F6E),
                Color(0xE685A285)
            )))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(90.dp))

            Text("Login Account", color = White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))

            RoleToggle(
                selected = uiState.role,
                onSelect = { viewModel.onRoleChange(it) }
            )
            Spacer(modifier = Modifier.height(28.dp))
            OutlinedTextField(
                value         = uiState.email,
                onValueChange = { viewModel.onEmailChange(it) },
                placeholder   = {
                    Text(
                        if (uiState.role == LoginRole.VOLUNTEER) "Email Volunteer" else "Email Organisasi",
                        color = HintColor
                    )
                },
                singleLine      = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                colors          = textFieldColors(),
                shape           = RoundedCornerShape(12.dp),
                modifier        = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value         = uiState.password,
                onValueChange = { viewModel.onPasswordChange(it) },
                placeholder   = { Text("Password", color = HintColor) },
                singleLine    = true,
                visualTransformation = if (uiState.passwordVisible)
                    VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { viewModel.onTogglePasswordVisibility() }) {
                        Icon(
                            imageVector = if (uiState.passwordVisible)
                                Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Toggle password",
                            tint = HintColor
                        )
                    }
                },
                colors   = textFieldColors(),
                shape    = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )
            if (uiState.loginError.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text     = uiState.loginError,
                    color    = Color(0xFFFFCDD2),
                    fontSize = 13.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                Text(
                    text       = "Forgot Password?",
                    color      = White,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 13.sp,
                    modifier   = Modifier.clickable { /* mockup */ }
                )
            }
            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick  = { viewModel.onLogin() },
                enabled  = uiState.email.isNotBlank() && uiState.password.isNotBlank(),
                colors   = ButtonDefaults.buttonColors(
                    containerColor         = Color(0xFF1A1A1A),
                    disabledContainerColor = Color(0xFF1A1A1A).copy(alpha = 0.4f)
                ),
                shape    = RoundedCornerShape(50.dp),
                modifier = Modifier.width(160.dp).height(52.dp)
            ) {
                Text("Login", color = White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(40.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier          = Modifier.fillMaxWidth()
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f),
                    color = White.copy(alpha = 0.4f), thickness = 1.dp)
                Text("  Or Login With  ", color = White, fontSize = 13.sp)
                HorizontalDivider(modifier = Modifier.weight(1f),
                    color = White.copy(alpha = 0.4f), thickness = 1.dp)
            }
            Spacer(modifier = Modifier.height(20.dp))
            Surface(
                onClick = {
                    googleSignInClient.signOut().addOnCompleteListener {
                        googleLauncher.launch(googleSignInClient.signInIntent)
                    }
                },
                shape         = RoundedCornerShape(50.dp),
                color         = White,
                shadowElevation = 2.dp,
                modifier      = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier              = Modifier.fillMaxSize()
                ) {
                    Icon(
                        painter           = painterResource(id = R.drawable.gugel_icon),
                        contentDescription = "Google Icon",
                        tint              = Color.Unspecified,
                        modifier          = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(color = Color(0xFF3C3C3C), fontWeight = FontWeight.Medium)) {
                                append("Continue with ")
                            }
                            withStyle(SpanStyle(color = Color(0xFF4285F4), fontWeight = FontWeight.SemiBold)) {
                                append("Google")
                            }
                        },
                        fontSize = 15.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
            Row {
                Text("Belum memiliki akun? ", color = White, fontSize = 14.sp)
                Text(
                    text           = "Register",
                    color          = White,
                    fontSize       = 14.sp,
                    fontWeight     = FontWeight.Bold,
                    textDecoration = TextDecoration.Underline,
                    modifier       = Modifier.clickable { backStack.add(Routes.RegisterRoute) }
                )
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun RoleToggle(selected: LoginRole, onSelect: (LoginRole) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0x22FFFFFF), RoundedCornerShape(50.dp))
            .padding(4.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            RoleToggleButton("  Volunteer",  selected == LoginRole.VOLUNTEER,
                { onSelect(LoginRole.VOLUNTEER) },  Modifier.weight(1f))
            RoleToggleButton("  Organisasi", selected == LoginRole.ORGANISASI,
                { onSelect(LoginRole.ORGANISASI) }, Modifier.weight(1f))
        }
    }
}

@Composable
private fun RoleToggleButton(text: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier) {
    val bgColor   by animateColorAsState(
        if (isSelected) Color.White else Color.Transparent,
        tween(200), label = "toggle_bg")
    val textColor by animateColorAsState(
        if (isSelected) Color(0xFF3D5C2A) else Color.White.copy(alpha = 0.7f),
        tween(200), label = "toggle_text")
    Box(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(50.dp))
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = textColor, fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
private fun textFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor      = White,
    unfocusedTextColor    = White,
    focusedBorderColor    = White.copy(alpha = 0.6f),
    unfocusedBorderColor  = White.copy(alpha = 0.3f),
    focusedContainerColor = InputBg,
    unfocusedContainerColor = InputBg,
    cursorColor           = White
)