package com.example.prototypevolunteerapp.ui.screens.register

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.prototypevolunteerapp.R
import com.example.prototypevolunteerapp.core.LocalBackStack
import com.example.prototypevolunteerapp.ui.components.AppFooter
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

private val White     = Color.White
private val HintColor = Color(0xAAFFFFFF)
private val InputBg   = Color(0x33FFFFFF)

@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel = hiltViewModel()
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
                android.util.Log.e("RegisterScreen", "Google sign-in failed: ${e.statusCode}")
            }
        }
    }

    LaunchedEffect(uiState.isRegistered) {
        if (uiState.isRegistered) {
            viewModel.onRegisteredHandled()
            backStack.removeLastOrNull()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF5E9CE7),
                        Color(0xFF7BB8F0),
                        Color(0xFFCEE4FF))
                )
            )
    ) {
        Column(
            modifier            = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(56.dp))
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                IconButton(onClick = { backStack.removeLastOrNull() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali", tint = White)
                }
            }
            Spacer(Modifier.height(8.dp))
            Text("Be a Part of Us", color = White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text("Bergabung dan mulai berkontribusi", color = White.copy(alpha = 0.8f), fontSize = 14.sp)

            Spacer(Modifier.height(24.dp))
            RoleToggle(selected = uiState.role, onSelect = { viewModel.onRoleChange(it) })

            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value         = uiState.name,
                onValueChange = { viewModel.onNameChange(it) },
                placeholder   = {
                    Text(
                        if (uiState.role == RegisterRole.VOLUNTEER) "Nama Lengkap" else "Nama PIC / Kontak Person",
                        color = HintColor
                    )
                },
                leadingIcon = { Icon(Icons.Default.Person, null, tint = HintColor) },
                singleLine  = true,
                colors      = fieldColors(),
                shape       = RoundedCornerShape(12.dp),
                modifier    = Modifier.fillMaxWidth()
            )

            if (uiState.role == RegisterRole.ORGANISASI) {
                Spacer(Modifier.height(14.dp))
                OutlinedTextField(
                    value         = uiState.organizationName,
                    onValueChange = { viewModel.onOrganizationNameChange(it) },
                    placeholder   = { Text("Nama Organisasi", color = HintColor) },
                    leadingIcon   = { Icon(Icons.Default.Business, null, tint = HintColor) },
                    singleLine    = true,
                    colors        = fieldColors(),
                    shape         = RoundedCornerShape(12.dp),
                    modifier      = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(14.dp))
            OutlinedTextField(
                value           = uiState.email,
                onValueChange   = { viewModel.onEmailChange(it) },
                placeholder     = { Text("Email", color = HintColor) },
                leadingIcon     = { Icon(Icons.Default.Email, null, tint = HintColor) },
                singleLine      = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                colors          = fieldColors(),
                shape           = RoundedCornerShape(12.dp),
                modifier        = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(14.dp))
            OutlinedTextField(
                value                = uiState.password,
                onValueChange        = { viewModel.onPasswordChange(it) },
                placeholder          = { Text("Password (min. 8 karakter)", color = HintColor) },
                leadingIcon          = { Icon(Icons.Default.Lock, null, tint = HintColor) },
                singleLine           = true,
                visualTransformation = if (uiState.passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions      = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon         = {
                    IconButton(onClick = { viewModel.onTogglePassword() }) {
                        Icon(
                            if (uiState.passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            "Toggle password", tint = HintColor
                        )
                    }
                },
                colors   = fieldColors(),
                shape    = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(14.dp))
            OutlinedTextField(
                value                = uiState.confirmPassword,
                onValueChange        = { viewModel.onConfirmChange(it) },
                placeholder          = { Text("Konfirmasi Password", color = HintColor) },
                leadingIcon          = { Icon(Icons.Default.LockOpen, null, tint = HintColor) },
                singleLine           = true,
                visualTransformation = if (uiState.confirmVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions      = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon         = {
                    IconButton(onClick = { viewModel.onToggleConfirm() }) {
                        Icon(
                            if (uiState.confirmVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            "Toggle confirm", tint = HintColor
                        )
                    }
                },
                colors   = fieldColors(),
                shape    = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            if (uiState.errorMessage.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                Text(uiState.errorMessage, color = Color(0xFFFFCDD2), fontSize = 13.sp, modifier = Modifier.fillMaxWidth())
            }

            Spacer(Modifier.height(28.dp))

            val isFormValid = uiState.name.isNotBlank() &&
                    uiState.email.isNotBlank() &&
                    uiState.password.isNotBlank() &&
                    uiState.confirmPassword.isNotBlank() &&
                    (uiState.role == RegisterRole.VOLUNTEER || uiState.organizationName.isNotBlank())

            Button(
                onClick  = { viewModel.onRegister() },
                enabled  = !uiState.isLoading && isFormValid,
                colors   = ButtonDefaults.buttonColors(
                    containerColor         = Color(0xFFFFFFFF),
                    disabledContainerColor = Color(0xCDFFFFFF).copy(alpha = 0.4f)
                ),
                shape    = RoundedCornerShape(50.dp),
                modifier = Modifier.width(200.dp).height(52.dp)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = White, strokeWidth = 2.dp)
                } else {
                    Text("Daftar Sekarang", color = Color(0xFF1E3B8B).copy(alpha = 0.8f), fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(24.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier          = Modifier.fillMaxWidth()
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f),
                    color = Color(0xFF1E3B8B).copy(alpha = 0.8f), thickness = 1.dp)
                Text("  Atau daftar dengan  ", color = Color(0xFF1E3B8B).copy(alpha = 0.8f), fontSize = 13.sp)
                HorizontalDivider(modifier = Modifier.weight(1f),
                    color    = Color(0xFF1E3B8B).copy(alpha = 0.8f), thickness = 1.dp)
            }

            Spacer(Modifier.height(16.dp))

            Surface(
                onClick = {
                    googleSignInClient.signOut().addOnCompleteListener {
                        googleLauncher.launch(googleSignInClient.signInIntent)
                    }
                },
                shape           = RoundedCornerShape(50.dp),
                color           = White,
                shadowElevation = 2.dp,
                modifier        = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier              = Modifier.fillMaxSize()
                ) {
                    Icon(
                        painter            = painterResource(id = R.drawable.gugel_icon),
                        contentDescription = "Google Icon",
                        tint               = Color.Unspecified,
                        modifier           = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(color = Color(0xFF3C3C3C), fontWeight = FontWeight.Medium)) {
                                append("Daftar dengan ")
                            }
                            withStyle(SpanStyle(color = Color(0xFF4285F4), fontWeight = FontWeight.SemiBold)) {
                                append("Google")
                            }
                        },
                        fontSize = 15.sp
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Sudah punya akun? ", color    = Color(0xFF1E3B8B).copy(alpha = 0.8f), fontSize = 14.sp)
                Text(
                    "Login",color    = Color(0xFF1E3B8B).copy(alpha = 0.8f), fontSize = 14.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { backStack.removeLastOrNull() }
                )
            }
            Spacer(Modifier.height(48.dp))
        }
    }
}

@Composable
private fun RoleToggle(selected: RegisterRole, onSelect: (RegisterRole) -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth().background(Color(0x22FFFFFF), RoundedCornerShape(50.dp)).padding(4.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            RoleButton("  Volunteer",  selected == RegisterRole.VOLUNTEER,  { onSelect(RegisterRole.VOLUNTEER)  }, Modifier.weight(1f))
            RoleButton("  Organisasi", selected == RegisterRole.ORGANISASI, { onSelect(RegisterRole.ORGANISASI) }, Modifier.weight(1f))
        }
    }
}

@Composable
private fun RoleButton(text: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier) {
    val bgColor   by animateColorAsState(if (isSelected) Color.White else Color.Transparent, tween(200), label = "rbg")
    val textColor by animateColorAsState(if (isSelected) Color(0xFF24408F) else Color.White.copy(alpha = 0.7f), tween(200), label = "rtxt")
    Box(
        modifier         = modifier.background(bgColor, RoundedCornerShape(50.dp)).clickable { onClick() }.padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = textColor, fontSize = 14.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor        = White,
    unfocusedTextColor      = White,
    focusedBorderColor      = White.copy(alpha = 0.6f),
    unfocusedBorderColor    = White.copy(alpha = 0.3f),
    focusedContainerColor   = InputBg,
    unfocusedContainerColor = InputBg,
    cursorColor             = White
)