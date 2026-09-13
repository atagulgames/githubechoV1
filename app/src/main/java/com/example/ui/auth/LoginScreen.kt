package com.example.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import com.example.data.CloudSaveSyncManager
import com.example.data.EchoPreferences
import com.example.data.RenderAuthAndCloudSaveService
import com.example.data.security.SecureTokenManager
import kotlinx.coroutines.Dispatchers
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.audio.HarmonicAudioEngine
import com.example.data.AuthRepository
import kotlinx.coroutines.launch

enum class AuthTab {
    LOGIN,
    REGISTER
}

/**
 * Authentication & Registration Screen.
 * 
 * Strict constraints:
 * - No guest login or skip button. All user entries are saved to the persistent database.
 * - Each user account strictly preserves their own individual progress (level, stars, tokens, theme).
 * - Full Registration ("Kayıt Ol") and Login ("Giriş Yap") menus.
 */
@Composable
fun LoginScreen(
    initialUsername: String = "",
    initialRememberMe: Boolean = false,
    onLoginSuccess: (username: String, rememberMe: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val authRepository = remember { AuthRepository(context) }
    val cloudAuthService = remember { RenderAuthAndCloudSaveService.getInstance() }
    val syncManager = remember { CloudSaveSyncManager(context) }
    val prefs = remember { EchoPreferences(context) }

    val hasExistingUser = prefs.authenticatedUsername.isNotBlank() || initialUsername.isNotBlank()
    var selectedTab by remember { mutableStateOf(AuthTab.REGISTER) }

    // Login Fields (Kullanıcı Adı ve Şifre) - Varsayılan hesap verilmez
    var loginUsername by remember { mutableStateOf(if (prefs.rememberMe && prefs.isAuthenticated) prefs.authenticatedUsername else "") }
    var loginPassword by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(initialRememberMe) }
    var loginPasswordVisible by remember { mutableStateOf(false) }

    // Register Fields (Kullanıcı Adı, Şifre, Şifre Tekrar)
    var registerUsername by remember { mutableStateOf("") }
    var registerPassword by remember { mutableStateOf("") }
    var registerConfirmPassword by remember { mutableStateOf("") }
    var registerPasswordVisible by remember { mutableStateOf(false) }
    var registerConfirmPasswordVisible by remember { mutableStateOf(false) }

    // Feedback State
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    // Play login sound effect on enter
    LaunchedEffect(Unit) {
        HarmonicAudioEngine.playLoginEffect(context)
    }

    fun handleLogin() {
        if (isLoading) return
        val username = loginUsername.trim()
        val password = loginPassword.trim()

        if (username.isBlank()) {
            errorMessage = "Kullanıcı adı boş bırakılamaz."
            HarmonicAudioEngine.playHataSound()
            return
        }
        if (password.isBlank()) {
            errorMessage = "Şifre boş bırakılamaz."
            HarmonicAudioEngine.playHataSound()
            return
        }

        isLoading = true
        errorMessage = null
        successMessage = null

        coroutineScope.launch {
            // 1. First attempt login via Render Cloud Backend
            val cloudResult = cloudAuthService.login(username, password)
            if (cloudResult.isSuccess) {
                val (user, token) = cloudResult.getOrThrow()
                SecureTokenManager.saveToken(context, token)

                val derivedUsername = if (user.fullName.isNotBlank()) user.fullName else username
                prefs.setAuthenticatedUser(
                    username = derivedUsername,
                    remember = rememberMe,
                    passwordHash = "",
                    email = user.email,
                    fullName = user.fullName
                )

                // Sync with local account cache with password
                val existingAcc = authRepository.getUserAccount(derivedUsername)
                if (existingAcc == null) {
                    authRepository.register(
                        usernameInput = derivedUsername,
                        emailInput = user.email,
                        passwordInput = password
                    )
                } else {
                    authRepository.registerOrUpdateExternalUser(
                        username = derivedUsername,
                        email = user.email,
                        fullName = user.fullName
                    )
                }

                // Synchronize Cloud Save
                syncManager.syncOnLogin(token)

                // Also update score on Render leaderboard only if player has earned trophies (> 0)
                if (prefs.trophies > 0) {
                    coroutineScope.launch(Dispatchers.IO) {
                        try {
                            com.example.data.RenderLeaderboardService.getInstance().submitScore(derivedUsername, prefs.trophies)
                        } catch (_: Exception) {}
                    }
                }

                isLoading = false
                HarmonicAudioEngine.playLoginEffect(context)
                onLoginSuccess(derivedUsername, rememberMe)
            } else {
                // 2. Fallback to local persistent auth (offline support or local user)
                val localResult = authRepository.login(username, password, rememberMe)
                if (localResult.isSuccess) {
                    val localUser = localResult.getOrThrow()
                    val token = SecureTokenManager.getToken(context) ?: "render_jwt_${System.currentTimeMillis()}"
                    SecureTokenManager.saveToken(context, token)

                    prefs.setAuthenticatedUser(
                        username = localUser.username,
                        remember = rememberMe,
                        passwordHash = localUser.passwordHash,
                        email = localUser.email,
                        fullName = localUser.username
                    )

                    if (prefs.trophies > 0) {
                        coroutineScope.launch(Dispatchers.IO) {
                            try {
                                com.example.data.RenderLeaderboardService.getInstance().submitScore(localUser.username, prefs.trophies)
                            } catch (_: Exception) {}
                        }
                    }

                    isLoading = false
                    HarmonicAudioEngine.playLoginEffect(context)
                    onLoginSuccess(localUser.username, rememberMe)
                } else {
                    isLoading = false
                    val cloudErr = cloudResult.exceptionOrNull()?.message.orEmpty()
                    val localErr = localResult.exceptionOrNull()?.message.orEmpty()

                    errorMessage = when {
                        cloudErr.contains("hatalı", ignoreCase = true) || localErr.contains("şifre", ignoreCase = true) -> {
                            "Kullanıcı adı veya şifre hatalı."
                        }
                        cloudErr.contains("İnternet", ignoreCase = true) && localErr.contains("bulunamadı", ignoreCase = true) -> {
                            "İnternet bağlantısı yok veya sunucuya ulaşılamadı."
                        }
                        localErr.contains("bulunamadı", ignoreCase = true) -> {
                            "Kullanıcı adı veya şifre hatalı."
                        }
                        cloudErr.isNotBlank() -> cloudErr
                        else -> "Kullanıcı adı veya şifre hatalı."
                    }
                    HarmonicAudioEngine.playHataSound()
                }
            }
        }
    }

    fun handleRegister() {
        if (isLoading) return
        val username = registerUsername.trim()
        val password = registerPassword.trim()
        val confirmPassword = registerConfirmPassword.trim()

        if (username.isBlank()) {
            errorMessage = "Kullanıcı adı boş bırakılamaz."
            HarmonicAudioEngine.playHataSound()
            return
        }
        if (username.length < 3) {
            errorMessage = "Kullanıcı adı en az 3 karakter olmalıdır."
            HarmonicAudioEngine.playHataSound()
            return
        }
        if (username.length > 20) {
            errorMessage = "Kullanıcı adı en fazla 20 karakter olabilir."
            HarmonicAudioEngine.playHataSound()
            return
        }
        if (password.isBlank()) {
            errorMessage = "Şifre boş bırakılamaz."
            HarmonicAudioEngine.playHataSound()
            return
        }
        if (password.length < 4) {
            errorMessage = "Şifre en az 4 karakter olmalıdır."
            HarmonicAudioEngine.playHataSound()
            return
        }
        if (password != confirmPassword) {
            errorMessage = "Şifre tekrar alanı şifreyle aynı olmalıdır."
            HarmonicAudioEngine.playHataSound()
            return
        }

        isLoading = true
        errorMessage = null
        successMessage = null

        coroutineScope.launch {
            // Check local account first
            val existingLocal = authRepository.getUserAccount(username)
            if (existingLocal != null) {
                isLoading = false
                errorMessage = "Bu kullanıcı adı zaten kullanılıyor. Lütfen başka bir kullanıcı adı seçin."
                HarmonicAudioEngine.playHataSound()
                return@launch
            }

            // Register on Render Backend
            val cloudResult = cloudAuthService.register(username = username, email = "", password = password)
            if (cloudResult.isSuccess) {
                val (user, token) = cloudResult.getOrThrow()
                SecureTokenManager.saveToken(context, token)

                val derivedUsername = if (user.fullName.isNotBlank()) user.fullName else username
                prefs.setAuthenticatedUser(
                    username = derivedUsername,
                    remember = true,
                    passwordHash = "",
                    email = user.email,
                    fullName = user.fullName
                )

                // Save to local Room & Vault with secure password hash
                authRepository.register(
                    usernameInput = derivedUsername,
                    emailInput = user.email,
                    passwordInput = password
                )

                // Initial Cloud Save synchronization
                syncManager.syncOnLogin(token)

                isLoading = false
                HarmonicAudioEngine.playLoginEffect(context)
                successMessage = "Kayıt başarılı! Giriş yapılıyor..."
                onLoginSuccess(derivedUsername, true)
            } else {
                val cloudErr = cloudResult.exceptionOrNull()?.message.orEmpty()
                if (cloudErr.contains("zaten", ignoreCase = true) || cloudErr.contains("already", ignoreCase = true) || cloudErr.contains("kullanılıyor", ignoreCase = true)) {
                    isLoading = false
                    errorMessage = "Bu kullanıcı adı zaten kullanılıyor. Lütfen başka bir kullanıcı adı seçin."
                    HarmonicAudioEngine.playHataSound()
                    return@launch
                }

                // Cloud returned error/offline -> Fallback to robust local persistent registration
                val localReg = authRepository.register(
                    usernameInput = username,
                    emailInput = "",
                    passwordInput = password
                )
                if (localReg.isSuccess) {
                    val localUser = localReg.getOrThrow()
                    val token = "render_jwt_${System.currentTimeMillis()}"
                    SecureTokenManager.saveToken(context, token)

                    prefs.setAuthenticatedUser(
                        username = localUser.username,
                        remember = true,
                        passwordHash = localUser.passwordHash,
                        email = localUser.email,
                        fullName = localUser.username
                    )

                    isLoading = false
                    HarmonicAudioEngine.playLoginEffect(context)
                    successMessage = "Kayıt başarılı! Giriş yapılıyor..."
                    onLoginSuccess(localUser.username, true)
                } else {
                    isLoading = false
                    val regErr = localReg.exceptionOrNull()?.message
                    errorMessage = regErr ?: "Kayıt işlemi gerçekleştirilemedi. Lütfen bilgilerinizi kontrol edin."
                    HarmonicAudioEngine.playHataSound()
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF070B12),
                        Color(0xFF0F172A),
                        Color(0xFF020617)
                    )
                )
            )
            .testTag("login_screen_container"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 28.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Echo Game Logo & Header
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.linearGradient(
                            listOf(Color(0xFF0284C7), Color(0xFF0EA5E9), Color(0xFF38BDF8))
                        )
                    )
                    .shadow(16.dp, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.echo_game_logo),
                    contentDescription = "ECHO Logo",
                    modifier = Modifier.size(56.dp).clip(CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "ECHO FLUX",
                fontSize = 30.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 4.sp,
                color = Color.White
            )

            Text(
                text = "BULMACA & YANKI REZONANSI",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 2.sp,
                color = Color(0xFF38BDF8)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Tab Selector: KAYIT OL | GİRİŞ YAP
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF1E293B))
                    .border(1.dp, Color(0xFF334155), RoundedCornerShape(16.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                // Kayıt Ol Tab (İlk ve Zorunlu Adım)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (selectedTab == AuthTab.REGISTER) Color(0xFF0284C7) else Color.Transparent
                        )
                        .clickable {
                            selectedTab = AuthTab.REGISTER
                            errorMessage = null
                            successMessage = null
                        }
                        .padding(vertical = 10.dp)
                        .testTag("tab_register"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = null,
                            tint = if (selectedTab == AuthTab.REGISTER) Color.White else Color(0xFF94A3B8),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "KAYIT OL",
                            color = if (selectedTab == AuthTab.REGISTER) Color.White else Color(0xFF94A3B8),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Giriş Yap Tab (Önceden Kayıtlı Olanlar)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (selectedTab == AuthTab.LOGIN) Color(0xFF0284C7) else Color.Transparent
                        )
                        .clickable {
                            selectedTab = AuthTab.LOGIN
                            errorMessage = null
                            successMessage = null
                        }
                        .padding(vertical = 10.dp)
                        .testTag("tab_login"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Login,
                            contentDescription = null,
                            tint = if (selectedTab == AuthTab.LOGIN) Color.White else Color(0xFF94A3B8),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "GİRİŞ YAP",
                            color = if (selectedTab == AuthTab.LOGIN) Color.White else Color(0xFF94A3B8),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Card Form
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(16.dp, RoundedCornerShape(22.dp))
                    .testTag("auth_card"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (selectedTab == AuthTab.REGISTER) "Yeni Oyuncu Kaydı" else "Oyuncu Girişi",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Text(
                        text = if (selectedTab == AuthTab.REGISTER)
                            "Oyuna başlamak için ilk olarak kayıt olmanız zorunludur"
                        else
                            "Kayıtlı hesabınızla giriş yaparak devam edin",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Error Banner
                    if (errorMessage != null) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                                .testTag("login_error_banner"),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF7F1D1D)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = "Hata",
                                    tint = Color(0xFFFCA5A5),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = errorMessage.orEmpty(),
                                    color = Color(0xFFFEE2E2),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // Success Banner
                    if (successMessage != null) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF065F46)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = successMessage.orEmpty(),
                                color = Color(0xFFA7F3D0),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    if (selectedTab == AuthTab.LOGIN) {
                        // LOGIN FORM (Kullanıcı Adı ve Şifre)
                        OutlinedTextField(
                            value = loginUsername,
                            onValueChange = {
                                loginUsername = it
                                errorMessage = null
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_username_field"),
                            label = { Text("Kullanıcı Adı") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = Color(0xFF38BDF8)
                                )
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF0284C7),
                                unfocusedBorderColor = Color(0xFF475569),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedLabelColor = Color(0xFF38BDF8),
                                unfocusedLabelColor = Color(0xFF94A3B8)
                            ),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Next
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = loginPassword,
                            onValueChange = {
                                loginPassword = it
                                errorMessage = null
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_password_field"),
                            label = { Text("Şifre") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = Color(0xFF38BDF8)
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { loginPasswordVisible = !loginPasswordVisible }) {
                                    Icon(
                                        imageVector = if (loginPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Şifreyi Göster",
                                        tint = Color(0xFF94A3B8)
                                    )
                                }
                            },
                            singleLine = true,
                            visualTransformation = if (loginPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF0284C7),
                                unfocusedBorderColor = Color(0xFF475569),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedLabelColor = Color(0xFF38BDF8),
                                unfocusedLabelColor = Color(0xFF94A3B8)
                            ),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = { handleLogin() }
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Remember Me Checkbox
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { rememberMe = !rememberMe },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = rememberMe,
                                onCheckedChange = { rememberMe = it },
                                modifier = Modifier.testTag("login_remember_checkbox"),
                                colors = CheckboxDefaults.colors(
                                    checkedColor = Color(0xFF0284C7),
                                    uncheckedColor = Color(0xFF64748B),
                                    checkmarkColor = Color.White
                                )
                            )
                            Text(
                                text = "Beni Hatırla",
                                color = Color(0xFFCBD5E1),
                                fontSize = 13.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = { handleLogin() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("login_submit_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF0284C7),
                                contentColor = Color.White
                            ),
                            enabled = !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Login,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "GİRİŞ YAP",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        androidx.compose.material3.TextButton(
                            onClick = {
                                selectedTab = AuthTab.REGISTER
                                errorMessage = null
                                successMessage = null
                            }
                        ) {
                            Text(
                                text = "Hesabın yok mu? Önce Kayıt Ol",
                                color = Color(0xFF38BDF8),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    } else {
                        // REGISTER FORM (Kullanıcı Adı, Şifre, Şifre Tekrar)
                        OutlinedTextField(
                            value = registerUsername,
                            onValueChange = {
                                registerUsername = it
                                errorMessage = null
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("register_username_field"),
                            label = { Text("Kullanıcı Adı") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = Color(0xFF38BDF8)
                                )
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF0284C7),
                                unfocusedBorderColor = Color(0xFF475569),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedLabelColor = Color(0xFF38BDF8),
                                unfocusedLabelColor = Color(0xFF94A3B8)
                            ),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Next
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = registerPassword,
                            onValueChange = {
                                registerPassword = it
                                errorMessage = null
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("register_password_field"),
                            label = { Text("Şifre") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = Color(0xFF38BDF8)
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { registerPasswordVisible = !registerPasswordVisible }) {
                                    Icon(
                                        imageVector = if (registerPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Şifreyi Göster",
                                        tint = Color(0xFF94A3B8)
                                    )
                                }
                            },
                            singleLine = true,
                            visualTransformation = if (registerPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF0284C7),
                                unfocusedBorderColor = Color(0xFF475569),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedLabelColor = Color(0xFF38BDF8),
                                unfocusedLabelColor = Color(0xFF94A3B8)
                            ),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Next
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = registerConfirmPassword,
                            onValueChange = {
                                registerConfirmPassword = it
                                errorMessage = null
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("register_confirm_password_field"),
                            label = { Text("Şifre Tekrar") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = Color(0xFF38BDF8)
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { registerConfirmPasswordVisible = !registerConfirmPasswordVisible }) {
                                    Icon(
                                        imageVector = if (registerConfirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Şifreyi Göster",
                                        tint = Color(0xFF94A3B8)
                                    )
                                }
                            },
                            singleLine = true,
                            visualTransformation = if (registerConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF0284C7),
                                unfocusedBorderColor = Color(0xFF475569),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedLabelColor = Color(0xFF38BDF8),
                                unfocusedLabelColor = Color(0xFF94A3B8)
                            ),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = { handleRegister() }
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { handleRegister() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("register_submit_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF10B981),
                                contentColor = Color.White
                            ),
                            enabled = !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.PersonAdd,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "KAYIT OL",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        androidx.compose.material3.TextButton(
                            onClick = {
                                selectedTab = AuthTab.LOGIN
                                errorMessage = null
                                successMessage = null
                            }
                        ) {
                            Text(
                                text = "Zaten bir hesabın var mı? Giriş Yap",
                                color = Color(0xFF38BDF8),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Information Footnote & Version
            Text(
                text = "Oyuna başlamak için herkesin ilk olarak kayıt olması zorunludur. Her kullanıcının seviye ve skor ilerlemesi güvenle saklanır.",
                color = Color(0xFF64748B),
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "ECHO FLUX • v1.2",
                color = Color(0xFF475569),
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
